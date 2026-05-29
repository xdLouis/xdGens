package de.louis.xdGens.listener;

import de.louis.xdGens.crate.CrateReward;
import de.louis.xdGens.crate.CrateType;
import de.louis.xdGens.crate.PouchItem;
import de.louis.xdGens.crate.PouchType;
import de.louis.xdGens.gui.CratesGUI;
import de.louis.xdGens.main.Main;
import de.louis.xdGens.manager.CrateManager;
import de.louis.xdGens.util.MessageUtil;
import de.louis.xdGens.util.NumberUtil;
import net.kyori.adventure.text.serializer.plain.PlainTextComponentSerializer;
import org.bukkit.Sound;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.block.Action;
import org.bukkit.event.inventory.ClickType;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.inventory.EquipmentSlot;
import org.bukkit.inventory.ItemStack;

public class CrateListener implements Listener {

    private final Main plugin;

    public CrateListener(Main plugin) {
        this.plugin = plugin;
    }

    // ── GUI clicks ───────────────────────────────────────────────────────

    @EventHandler(priority = EventPriority.HIGH, ignoreCancelled = true)
    public void onInventoryClick(InventoryClickEvent event) {
        if (!(event.getWhoClicked() instanceof Player player)) return;
        String title = PlainTextComponentSerializer.plainText().serialize(event.getView().title());
        if (!title.contains("Crates")) return;

        event.setCancelled(true);
        int slot = event.getSlot();

        // resolve which crate was clicked
        CrateType crateType = resolveBySlot(slot);
        if (crateType == null) return;

        boolean openAll = event.getClick() == ClickType.RIGHT;

        if (openAll) {
            handleOpenAll(player, crateType);
        } else {
            handleOpenOne(player, crateType);
        }
    }

    // ── pouch right-click use ────────────────────────────────────────────

    @EventHandler(priority = EventPriority.HIGH)
    public void onPouchUse(PlayerInteractEvent event) {
        if (event.getHand() != EquipmentSlot.HAND) return;
        if (event.getAction() != Action.RIGHT_CLICK_AIR
                && event.getAction() != Action.RIGHT_CLICK_BLOCK) return;

        Player    player = event.getPlayer();
        ItemStack item   = event.getItem();
        if (!PouchItem.isPouch(plugin, item)) return;
        event.setCancelled(true);

        PouchType type  = PouchItem.getType(plugin, item);
        long      value = PouchItem.getValue(plugin, item);
        if (type == null || value <= 0) return;

        switch (type) {
            case MONEY  -> plugin.getCurrencyManager().addMoney(player, value);
            case TOKENS -> plugin.getCurrencyManager().addTokens(player, (int) value);
            case XP     -> plugin.getProgressionManager().addXp(player, value);
        }
        consumeOne(item);
        player.playSound(player.getLocation(), Sound.ENTITY_EXPERIENCE_ORB_PICKUP, 1f, 1.1f);
        MessageUtil.sendRaw(player, MessageUtil.PREFIX
                + " <green>Opened pouch:</green> <white>+" + NumberUtil.format(value)
                + " " + readable(type) + "</white>");
    }

    // ── handlers ─────────────────────────────────────────────────────────

    private void handleOpenOne(Player player, CrateType type) {
        if (!plugin.getVirtualKeyManager().consumeKey(player, type)) {
            MessageUtil.sendRaw(player, MessageUtil.PREFIX
                    + " <red>You don't have a " + type.getDisplayName() + " key.</red>");
            return;
        }
        CrateManager.CrateOpenResult result = plugin.getCrateManager().openCrate(player, type);
        giveRewards(player, result);
        playCrateSound(player);
        MessageUtil.sendRaw(player, MessageUtil.PREFIX + " "
                + type.getGradient() + type.getDisplayName() + " Crate opened!</gradient>"
                + " <gray>You received 3 pouches.</gray>"
                + buildCosmeticSuffix(result));
        new CratesGUI(plugin).open(player);
    }

    private void handleOpenAll(Player player, CrateType type) {
        int count = plugin.getVirtualKeyManager().consumeAll(player, type);
        if (count <= 0) {
            MessageUtil.sendRaw(player, MessageUtil.PREFIX
                    + " <red>You don't have any " + type.getDisplayName() + " keys.</red>");
            return;
        }
        int newCosmeticsTotal = 0;
        for (int i = 0; i < count; i++) {
            CrateManager.CrateOpenResult result = plugin.getCrateManager().openCrate(player, type);
            giveRewards(player, result);
            if (result.hasNewCosmetic()) newCosmeticsTotal++;
        }
        playCrateSound(player);
        String cosmeticNote = newCosmeticsTotal > 0
                ? " <gradient:#c471f5:#fa71cd>+" + newCosmeticsTotal + " new cosmetic" + (newCosmeticsTotal > 1 ? "s" : "") + "!</gradient>"
                : "";
        MessageUtil.sendRaw(player, MessageUtil.PREFIX + " "
                + type.getGradient() + "Opened " + count + "x " + type.getDisplayName() + " Crate!</gradient>"
                + " <gray>You received " + (count * 3) + " pouches.</gray>" + cosmeticNote);
        new CratesGUI(plugin).open(player);
    }

    // ── helpers ──────────────────────────────────────────────────────────

    private void giveRewards(Player player, CrateManager.CrateOpenResult result) {
        result.pouches().forEach(p -> {
            var leftovers = player.getInventory().addItem(p);
            leftovers.values().forEach(left -> player.getWorld().dropItemNaturally(player.getLocation(), left));
        });
    }

    private String buildCosmeticSuffix(CrateManager.CrateOpenResult result) {
        if (!result.hasNewCosmetic()) return "";
        CrateReward c = result.newCosmetic();
        return " " + c.tierLabel() + " cosmetic: " + c.getCosmeticFormat()
                .replace("{name}", c.getDisplayName());
    }

    private void playCrateSound(Player player) {
        player.playSound(player.getLocation(), Sound.ENTITY_PLAYER_LEVELUP, 0.9f, 1.2f);
    }

    private CrateType resolveBySlot(int slot) {
        CrateType[] types = CrateType.values();
        int[] slots = CratesGUI.CRATE_SLOTS;
        for (int i = 0; i < slots.length && i < types.length; i++) {
            if (slots[i] == slot) return types[i];
        }
        return null;
    }

    private void consumeOne(ItemStack item) {
        item.setAmount(item.getAmount() - 1);
    }

    private String readable(PouchType type) {
        return switch (type) {
            case MONEY  -> "Money";
            case XP     -> "XP";
            case TOKENS -> "Tokens";
        };
    }
}
