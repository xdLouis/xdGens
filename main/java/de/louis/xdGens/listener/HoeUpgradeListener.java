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

    // ─── open main GUI on right-click with xd hoe ─────────────────────────
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

    // ─── inventory click handler ──────────────────────────────────────────
    @EventHandler(priority = EventPriority.HIGH, ignoreCancelled = true)
    public void onInventoryClick(InventoryClickEvent event) {
        if (!(event.getWhoClicked() instanceof Player player)) return;
        String title = PlainTextComponentSerializer.plainText().serialize(event.getView().title());

        // ── Main upgrade GUI ──────────────────────────────────────────────
        if (title.contains("Hoe Upgrades")) {
            event.setCancelled(true);
            ItemStack clicked = event.getCurrentItem();
            if (clicked == null || clicked.getType().isAir()) return;

            // Crop / XP / Token → open amount sub-menu
            if (event.getSlot() == 11) {
                new HoeUpgradeAmountGUI(plugin, "crop").open(player);
            } else if (event.getSlot() == 13) {
                new HoeUpgradeAmountGUI(plugin, "xp").open(player);
            } else if (event.getSlot() == 15) {
                new HoeUpgradeAmountGUI(plugin, "token").open(player);
            }
            return;
        }

        // ── Amount sub-menu ───────────────────────────────────────────────
        if (title.contains(HoeUpgradeAmountGUI.TITLE_PREFIX)) {
            event.setCancelled(true);
            ItemStack clicked = event.getCurrentItem();
            if (clicked == null || clicked.getType().isAir()) return;

            int slot = event.getSlot();

            // Back button
            if (slot == HoeUpgradeAmountGUI.SLOT_BACK) {
                gui.open(player);
                return;
            }

            // Determine upgrade type from title
            String type;
            if      (title.contains("Crop"))  type = "crop";
            else if (title.contains("XP"))    type = "xp";
            else                              type = "token";

            // Map slot → amount (MAX uses levelsLeft)
            int amount;
            if      (slot == HoeUpgradeAmountGUI.SLOT_PLUS1)  amount = 1;
            else if (slot == HoeUpgradeAmountGUI.SLOT_PLUS10) amount = 10;
            else if (slot == HoeUpgradeAmountGUI.SLOT_PLUS25) amount = 25;
            else if (slot == HoeUpgradeAmountGUI.SLOT_PLUS50) amount = 50;
            else if (slot == HoeUpgradeAmountGUI.SLOT_MAX) {
                int max = switch (type) {
                    case "crop"  -> HoeUpgradeManager.MAX_CROP_LEVEL;
                    case "xp"   -> HoeUpgradeManager.MAX_XP_LEVEL;
                    default     -> HoeUpgradeManager.MAX_TOKEN_LEVEL;
                };
                int current = switch (type) {
                    case "crop"  -> plugin.getHoeUpgradeManager().getCropLevel(player);
                    case "xp"   -> plugin.getHoeUpgradeManager().getXpLevel(player);
                    default     -> plugin.getHoeUpgradeManager().getTokenLevel(player);
                };
                amount = max - current;
            } else return;

            if (amount <= 0) {
                MessageUtil.sendRaw(player, MessageUtil.PREFIX + " <gold>Already maxed out!</gold>");
                return;
            }

            handleBulkUpgrade(player, type, amount);
            return;
        }
    }

    // ─── bulk upgrade logic ───────────────────────────────────────────────

    private void handleBulkUpgrade(Player player, String type, int requestedAmount) {
        HoeUpgradeManager mgr = plugin.getHoeUpgradeManager();

        int bought = switch (type) {
            case "crop"  -> mgr.upgradeCropBulk(player,  requestedAmount);
            case "xp"    -> mgr.upgradeXpBulk(player,    requestedAmount);
            default      -> mgr.upgradeTokenBulk(player, requestedAmount);
        };

        if (bought <= 0) {
            MessageUtil.sendRaw(player,
                    MessageUtil.PREFIX + " <red>Not enough Tokens for even one level!</red>");
            // Refresh sub-menu so costs update
            new HoeUpgradeAmountGUI(plugin, type).open(player);
            return;
        }

        int newLevel = switch (type) {
            case "crop"  -> mgr.getCropLevel(player);
            case "xp"    -> mgr.getXpLevel(player);
            default      -> mgr.getTokenLevel(player);
        };

        String gradient = switch (type) {
            case "crop"  -> "<gradient:#f6d365:#fda085>";
            case "xp"    -> "<gradient:#7afcff:#00c2ff>";
            default      -> "<gradient:#ffd86f:#fc6262>";
        };
        String upgradeName = switch (type) {
            case "crop"  -> "Crop Harvest";
            case "xp"    -> "XP Boost";
            default      -> "Token Boost";
        };

        String suffix = bought < requestedAmount
                ? " <yellow>(only " + bought + " level" + (bought == 1 ? "" : "s") + " – out of Tokens)</yellow>"
                : "";

        MessageUtil.sendRaw(player,
                MessageUtil.PREFIX + " " + gradient + upgradeName + " → Level " + newLevel + "!</gradient>"
                + " <gray>(+" + bought + " level" + (bought == 1 ? "" : "s") + ")</gray>" + suffix);

        // Re-open sub-menu with updated numbers
        new HoeUpgradeAmountGUI(plugin, type).open(player);
    }
}
