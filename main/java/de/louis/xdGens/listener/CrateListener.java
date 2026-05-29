package de.louis.xdGens.listener;

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

    // ── crate GUI click ───────────────────────────────────────────────

    @EventHandler(priority = EventPriority.HIGH, ignoreCancelled = true)
    public void onInventoryClick(InventoryClickEvent event) {
        if (!(event.getWhoClicked() instanceof Player player)) return;
        String title = PlainTextComponentSerializer.plainText().serialize(event.getView().title());
        if (!title.contains("Crates")) return;

        event.setCancelled(true);
        CrateType crateType = resolveBySlot(event.getSlot());
        if (crateType == null) return;

        if (event.getClick() == ClickType.RIGHT) handleOpenAll(player, crateType);
        else                                     handleOpenOne(player, crateType);
    }

    // ── pouch right-click → instant redemption ─────────────────────────

    @EventHandler(priority = EventPriority.HIGH)
    public void onPouchUse(PlayerInteractEvent event) {
        if (event.getHand() != EquipmentSlot.HAND) return;
        if (event.getAction() != Action.RIGHT_CLICK_AIR && event.getAction() != Action.RIGHT_CLICK_BLOCK) return;
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
        if (item.getAmount() <= 1) player.getInventory().setItemInMainHand(null);
        else item.setAmount(item.getAmount() - 1);

        player.playSound(player.getLocation(), Sound.ENTITY_EXPERIENCE_ORB_PICKUP, 1f, 1.1f);
        MessageUtil.sendRaw(player, MessageUtil.PREFIX + " <green>Opened pouch:</green> <white>+"
                + NumberUtil.format(value) + " " + readable(type) + "</white>");
    }

    // ── open one ───────────────────────────────────────────────────────

    private void handleOpenOne(Player player, CrateType type) {
        if (!plugin.getVirtualKeyManager().consumeKey(player, type)) {
            MessageUtil.sendRaw(player, MessageUtil.PREFIX
                    + " <red>You don't have a " + type.getDisplayName() + " key.</red>");
            return;
        }
        // check 1 free slot for potential voucher
        if (hasPotentialVoucher(type) && freeSlots(player) < 1) {
            // refund key and abort
            plugin.getVirtualKeyManager().addKey(player, type);
            MessageUtil.sendRaw(player, MessageUtil.PREFIX
                    + " <red>Your inventory is full! Clear at least 1 slot before opening.");
            return;
        }

        CrateManager.CrateOpenResult result = plugin.getCrateManager().openCrate(player, type);
        redeemPouches(player, result);
        giveVoucher(player, result);
        playCrateSound(player);

        String voucherNote = result.hasVoucher()
                ? " " + result.rolledCosmetic().tierLabel()
                + " <white>" + result.rolledCosmetic().getDisplayName() + "</white> cosmetic voucher!" : "";
        MessageUtil.sendRaw(player, MessageUtil.PREFIX + " "
                + type.getGradient() + type.getDisplayName() + " Crate opened!</gradient>"
                + " <gray>Rewards redeemed!</gray>" + voucherNote);
        new CratesGUI(plugin).open(player);
    }

    // ── open all ───────────────────────────────────────────────────────

    private void handleOpenAll(Player player, CrateType type) {
        int total = plugin.getVirtualKeyManager().keyCount(player, type);
        if (total <= 0) {
            MessageUtil.sendRaw(player, MessageUtil.PREFIX
                    + " <red>You don't have any " + type.getDisplayName() + " keys.</red>");
            return;
        }

        int opened   = 0;
        int vouchers = 0;

        for (int i = 0; i < total; i++) {
            // Before each crate: if this type can produce a voucher and inv is full, stop
            if (hasPotentialVoucher(type) && freeSlots(player) < 1) {
                MessageUtil.sendRaw(player, MessageUtil.PREFIX
                        + " <red>Inventory full — stopped after <white>" + opened + "</white> crate"
                        + (opened == 1 ? "" : "s") + ". Clear space and try again.</red>");
                break;
            }

            if (!plugin.getVirtualKeyManager().consumeKey(player, type)) break;

            CrateManager.CrateOpenResult result = plugin.getCrateManager().openCrate(player, type);
            redeemPouches(player, result);
            giveVoucher(player, result);
            if (result.hasVoucher()) vouchers++;
            opened++;
        }

        if (opened == 0) return;

        playCrateSound(player);
        String voucherNote = vouchers > 0
                ? " <gradient:#c471f5:#fa71cd>+" + vouchers + " cosmetic voucher" + (vouchers > 1 ? "s" : "") + "!</gradient>"
                : "";
        MessageUtil.sendRaw(player, MessageUtil.PREFIX + " "
                + type.getGradient() + "Opened " + opened + "x " + type.getDisplayName() + " Crate!</gradient>"
                + " <gray>All rewards redeemed!</gray>" + voucherNote);
        new CratesGUI(plugin).open(player);
    }

    // ── helpers ─────────────────────────────────────────────────────────

    private void redeemPouches(Player player, CrateManager.CrateOpenResult result) {
        for (ItemStack pouch : result.pouches()) {
            PouchType t   = PouchItem.getType(plugin, pouch);
            long      val = PouchItem.getValue(plugin, pouch);
            if (t == null || val <= 0) continue;
            switch (t) {
                case MONEY  -> plugin.getCurrencyManager().addMoney(player, val);
                case TOKENS -> plugin.getCurrencyManager().addTokens(player, (int) val);
                case XP     -> plugin.getProgressionManager().addXp(player, val);
            }
        }
    }

    private void giveVoucher(Player player, CrateManager.CrateOpenResult result) {
        if (!result.hasVoucher()) return;
        var leftovers = player.getInventory().addItem(result.voucherItem());
        // drop anything that didn’t fit (should rarely happen due to pre-check)
        leftovers.values().forEach(l -> player.getWorld().dropItemNaturally(player.getLocation(), l));
    }

    /**
     * Returns true if this crate type has a non-zero chance of giving a cosmetic voucher.
     * All crate types do, so we always need at least 1 free slot as a safety margin.
     */
    private boolean hasPotentialVoucher(CrateType type) {
        return true; // all crates can potentially roll a voucher
    }

    /** Counts empty slots in the player's main inventory (slots 0-35). */
    private int freeSlots(Player player) {
        int free = 0;
        for (int i = 0; i < 36; i++) {
            ItemStack item = player.getInventory().getItem(i);
            if (item == null || item.getType().isAir()) free++;
        }
        return free;
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

    private String readable(PouchType type) {
        return switch (type) { case MONEY -> "Money"; case XP -> "XP"; case TOKENS -> "Tokens"; };
    }
}
