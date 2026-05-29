package de.louis.xdGens.listener;

import de.louis.xdGens.gui.HoeUpgradeAmountGUI;
import de.louis.xdGens.gui.HoeUpgradeGUI;
import de.louis.xdGens.main.Main;
import de.louis.xdGens.manager.HoeUpgradeManager;
import de.louis.xdGens.util.HoeUtil;
import de.louis.xdGens.util.MessageUtil;
import de.louis.xdGens.util.NumberUtil;
import net.kyori.adventure.text.serializer.plain.PlainTextComponentSerializer;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.block.Action;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.inventory.EquipmentSlot;
import org.bukkit.inventory.ItemStack;

public class HoeUpgradeListener implements Listener {

    private final Main         plugin;
    private final HoeUpgradeGUI gui;

    public HoeUpgradeListener(Main plugin) {
        this.plugin = plugin;
        this.gui    = new HoeUpgradeGUI(plugin);
    }

    @EventHandler(priority = EventPriority.HIGH)
    public void onRightClick(PlayerInteractEvent event) {
        if (event.getHand() != EquipmentSlot.HAND) return;
        if (event.getAction() != Action.RIGHT_CLICK_AIR
                && event.getAction() != Action.RIGHT_CLICK_BLOCK) return;

        ItemStack item = event.getItem();
        if (!HoeUtil.isXdHoe(item)) return;

        Player player  = event.getPlayer();
        Block  clicked = event.getClickedBlock();

        if (clicked != null
                && clicked.getType() == Material.SMITHING_TABLE
                && plugin.getWorkstationManager().isWorkstation(clicked.getLocation())) {
            event.setCancelled(true);
            plugin.getWorkstationManager().useWorkstation(player);
            return;
        }

        if (event.getAction() == Action.RIGHT_CLICK_BLOCK) event.setCancelled(true);
        gui.open(player);
    }

    @EventHandler(priority = EventPriority.HIGH, ignoreCancelled = true)
    public void onInventoryClick(InventoryClickEvent event) {
        if (!(event.getWhoClicked() instanceof Player player)) return;
        String title = PlainTextComponentSerializer.plainText().serialize(event.getView().title());

        // ── main GUI ─────────────────────────────────────────────────────────
        if (title.contains("Hoe Upgrades")) {
            event.setCancelled(true);
            int slot = event.getSlot();

            if (slot >= HoeUpgradeGUI.UPGRADE_SLOTS) return;

            ItemStack clicked = event.getCurrentItem();
            if (clicked == null || clicked.getType().isAir()
                    || clicked.getType() == Material.BLACK_STAINED_GLASS_PANE) return;

            switch (slot) {
                case HoeUpgradeGUI.SLOT_CROP       -> new HoeUpgradeAmountGUI(plugin, "crop").open(player);
                case HoeUpgradeGUI.SLOT_XP         -> new HoeUpgradeAmountGUI(plugin, "xp").open(player);
                case HoeUpgradeGUI.SLOT_TOKEN      -> new HoeUpgradeAmountGUI(plugin, "token").open(player);
                case HoeUpgradeGUI.SLOT_HOE        -> handleHoeUpgrade(player);
                case HoeUpgradeGUI.SLOT_KEY_FINDER -> new HoeUpgradeAmountGUI(plugin, "keyfinder").open(player);
                case HoeUpgradeGUI.SLOT_PANDA      -> handlePandaClick(player);
            }
            return;
        }

        // ── amount sub-menu ───────────────────────────────────────────────────
        if (title.contains(HoeUpgradeAmountGUI.TITLE_PREFIX)) {
            event.setCancelled(true);
            ItemStack clicked = event.getCurrentItem();
            if (clicked == null || clicked.getType().isAir()) return;

            int slot = event.getSlot();

            if (slot == HoeUpgradeAmountGUI.SLOT_BACK) {
                gui.open(player);
                return;
            }

            String type;
            if      (title.contains("Crop"))         type = "crop";
            else if (title.contains("XP"))           type = "xp";
            else if (title.contains("Key Finder"))   type = "keyfinder";
            else if (title.contains("Panda"))        type = "panda";
            else                                     type = "token";

            int amount;
            if      (slot == HoeUpgradeAmountGUI.SLOT_PLUS1)  amount = 1;
            else if (slot == HoeUpgradeAmountGUI.SLOT_PLUS10) amount = 10;
            else if (slot == HoeUpgradeAmountGUI.SLOT_PLUS25) amount = 25;
            else if (slot == HoeUpgradeAmountGUI.SLOT_PLUS50) amount = 50;
            else if (slot == HoeUpgradeAmountGUI.SLOT_MAX) {
                int max = switch (type) {
                    case "crop"      -> HoeUpgradeManager.MAX_CROP_LEVEL;
                    case "xp"        -> HoeUpgradeManager.MAX_XP_LEVEL;
                    case "keyfinder" -> HoeUpgradeManager.MAX_KEY_FINDER_LEVEL;
                    case "panda"     -> HoeUpgradeManager.MAX_PANDA_LEVEL;
                    default          -> HoeUpgradeManager.MAX_TOKEN_LEVEL;
                };
                int current = switch (type) {
                    case "crop"      -> plugin.getHoeUpgradeManager().getCropLevel(player);
                    case "xp"        -> plugin.getHoeUpgradeManager().getXpLevel(player);
                    case "keyfinder" -> plugin.getHoeUpgradeManager().getKeyFinderLevel(player);
                    case "panda"     -> plugin.getHoeUpgradeManager().getPandaLevel(player);
                    default          -> plugin.getHoeUpgradeManager().getTokenLevel(player);
                };
                amount = max - current;
            } else return;

            if (amount <= 0) {
                MessageUtil.sendRaw(player, MessageUtil.PREFIX + " <gold>Already maxed out!</gold>");
                return;
            }
            handleBulkUpgrade(player, type, amount);
        }
    }

    // ── upgrade handlers ─────────────────────────────────────────────────

    private void handlePandaClick(Player player) {
        HoeUpgradeManager mgr = plugin.getHoeUpgradeManager();
        if (!mgr.canUnlockPanda(player)) {
            MessageUtil.sendRaw(player, MessageUtil.PREFIX
                    + " <red>🔒 Panda Roller requires <white>Prestige "
                    + HoeUpgradeManager.PANDA_REQUIRED_PRESTIGE + "</white> to unlock!</red>");
            return;
        }
        // Open the amount GUI — same as other upgrades
        new HoeUpgradeAmountGUI(plugin, "panda").open(player);
    }

    private void handleBulkUpgrade(Player player, String type, int requestedAmount) {
        HoeUpgradeManager mgr = plugin.getHoeUpgradeManager();

        int bought = switch (type) {
            case "crop"      -> mgr.upgradeCropBulk(player, requestedAmount);
            case "xp"        -> mgr.upgradeXpBulk(player, requestedAmount);
            case "keyfinder" -> mgr.upgradeKeyFinderBulk(player, requestedAmount);
            case "panda"     -> mgr.upgradePandaBulk(player, requestedAmount);
            default          -> mgr.upgradeTokenBulk(player, requestedAmount);
        };

        if (bought <= 0) {
            MessageUtil.sendRaw(player, MessageUtil.PREFIX + " <red>Not enough Tokens!</red>");
            new HoeUpgradeAmountGUI(plugin, type).open(player);
            return;
        }

        int newLevel = switch (type) {
            case "crop"      -> mgr.getCropLevel(player);
            case "xp"        -> mgr.getXpLevel(player);
            case "keyfinder" -> mgr.getKeyFinderLevel(player);
            case "panda"     -> mgr.getPandaLevel(player);
            default          -> mgr.getTokenLevel(player);
        };
        String grad = switch (type) {
            case "crop"      -> "<gradient:#f6d365:#fda085>";
            case "xp"        -> "<gradient:#7afcff:#00c2ff>";
            case "keyfinder" -> "<gradient:#a18cd1:#fbc2eb>";
            case "panda"     -> "<gradient:#a8e6cf:#88d8b0>";
            default          -> "<gradient:#ffd86f:#fc6262>";
        };
        String name = switch (type) {
            case "crop"      -> "Crop Harvest";
            case "xp"        -> "XP Boost";
            case "keyfinder" -> "Key Finder";
            case "panda"     -> "Panda Roller";
            default          -> "Token Boost";
        };

        String extra = "";
        if (type.equals("panda")) {
            PandaRollerSkill skill = (PandaRollerSkill) SkillRegistry.get("panda_roller");
            extra = " <gray>(" + skill.spawnChancePct(newLevel) + "% spawn · "
                    + skill.rewardBonusPct(newLevel) + "% bonus)</gray>";
        }

        String partial = bought < requestedAmount
                ? " <yellow>(only " + bought + " level" + (bought == 1 ? "" : "s") + " — out of Tokens)</yellow>"
                : "";

        MessageUtil.sendRaw(player,
                MessageUtil.PREFIX + " " + grad + name + " → Level " + newLevel + "!</gradient>"
                + " <gray>(+" + bought + " level" + (bought == 1 ? "" : "s") + ")</gray>"
                + extra + partial);

        new HoeUpgradeAmountGUI(plugin, type).open(player);
    }

    private void handleHoeUpgrade(Player player) {
        HoeUpgradeManager mgr = plugin.getHoeUpgradeManager();
        if (mgr.getHoeLevel(player) >= HoeUpgradeManager.MAX_HOE_LEVEL) {
            MessageUtil.sendRaw(player, MessageUtil.PREFIX + " <gold>Hoe is already max level!</gold>");
            return;
        }
        long   cost = mgr.getHoeCost(mgr.getHoeLevel(player) + 1);
        double bal  = plugin.getCurrencyManager().getMoney(player);
        if (bal < cost) {
            MessageUtil.sendRaw(player, MessageUtil.PREFIX
                    + " <red>Not enough money. Need: <white>$" + NumberUtil.format(cost) + "</white></red>");
            return;
        }
        if (mgr.upgradeHoe(player)) {
            MessageUtil.sendRaw(player,
                    MessageUtil.PREFIX + " <gradient:#c0c0c0:#ffffff>Hoe upgraded to Level "
                    + mgr.getHoeLevel(player) + "!</gradient>"
                    + " <gray>(-$" + NumberUtil.format(cost) + ")</gray>");
            gui.open(player);
        }
    }
}
