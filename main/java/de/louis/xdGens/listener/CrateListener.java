package de.louis.xdGens.listener;

import de.louis.xdGens.crate.CrateType;
import de.louis.xdGens.crate.PouchItem;
import de.louis.xdGens.crate.PouchTier;
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

import java.util.EnumMap;
import java.util.Map;

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
        MessageUtil.sendRaw(player, MessageUtil.PREFIX + " <green>Pouch opened:</green> <white>+"
                + NumberUtil.format(value) + " " + readable(type) + "</white>");
    }

    // ── open one ───────────────────────────────────────────────────────

    private void handleOpenOne(Player player, CrateType type) {
        if (freeSlots(player) < 1) {
            MessageUtil.sendRaw(player, MessageUtil.PREFIX
                    + " <red>Your inventory is full! Clear at least 1 slot before opening.</red>");
            return;
        }
        if (!plugin.getVirtualKeyManager().consumeKey(player, type)) {
            MessageUtil.sendRaw(player, MessageUtil.PREFIX
                    + " <red>You don't have a " + type.getDisplayName() + " key.</red>");
            return;
        }

        CrateManager.CrateOpenResult result = plugin.getCrateManager().openCrate(player, type);
        redeemPouches(player, result);
        giveVoucher(player, result);
        playCrateSound(player);

        // — single-open chat output —
        PouchTier tier = result.pouchTier();
        StringBuilder sb = new StringBuilder();
        sb.append(MessageUtil.PREFIX).append(" ")
          .append(type.getGradient()).append(type.getDisplayName()).append(" Crate</gradient>")
          .append(" <gray>│</gray> ")
          .append(tier.getDisplayName()).append(" <gray>pouches</gray>");

        for (ItemStack pouch : result.pouches()) {
            long val = PouchItem.getValue(plugin, pouch);
            PouchType pt = PouchItem.getType(plugin, pouch);
            if (pt == null || val <= 0) continue;
            sb.append("\n  <dark_gray>└</dark_gray> <white>+").append(NumberUtil.format(val))
              .append("</white> <gray>").append(readable(pt)).append("</gray>");
        }
        if (result.hasVoucher()) {
            sb.append("\n  <dark_gray>└</dark_gray> ")
              .append(result.rolledCosmetic().tierLabel())
              .append(" <white>").append(result.rolledCosmetic().getDisplayName()).append("</white> <gray>cosmetic voucher!</gray>");
        }
        MessageUtil.sendRaw(player, sb.toString());
        new CratesGUI(plugin).open(player);
    }

    // ── open all ───────────────────────────────────────────────────────

    private void handleOpenAll(Player player, CrateType type) {
        int total = plugin.getVirtualKeyManager().getKeys(player, type);
        if (total <= 0) {
            MessageUtil.sendRaw(player, MessageUtil.PREFIX
                    + " <red>You don't have any " + type.getDisplayName() + " keys.</red>");
            return;
        }

        int opened   = 0;
        int vouchers = 0;

        // tier counters for summary
        Map<PouchTier, Integer> tierCounts = new EnumMap<>(PouchTier.class);
        long totalMoney = 0, totalXp = 0, totalTokens = 0;

        for (int i = 0; i < total; i++) {
            if (freeSlots(player) < 1) {
                MessageUtil.sendRaw(player, MessageUtil.PREFIX
                        + " <red>Inventory full — stopped after <white>" + opened + "</white> crate"
                        + (opened == 1 ? "" : "s") + ". Clear space and try again.</red>");
                break;
            }
            if (!plugin.getVirtualKeyManager().consumeKey(player, type)) break;

            CrateManager.CrateOpenResult result = plugin.getCrateManager().openCrate(player, type);
            redeemPouches(player, result);
            giveVoucher(player, result);

            // track tier
            tierCounts.merge(result.pouchTier(), 1, Integer::sum);

            // accumulate totals
            for (ItemStack pouch : result.pouches()) {
                long val = PouchItem.getValue(plugin, pouch);
                PouchType pt = PouchItem.getType(plugin, pouch);
                if (pt == null) continue;
                switch (pt) {
                    case MONEY  -> totalMoney  += val;
                    case XP     -> totalXp     += val;
                    case TOKENS -> totalTokens += val;
                }
            }
            if (result.hasVoucher()) vouchers++;
            opened++;
        }

        if (opened == 0) return;
        playCrateSound(player);

        // — open-all summary —
        StringBuilder sb = new StringBuilder();
        sb.append(MessageUtil.PREFIX).append(" ")
          .append(type.getGradient()).append("Opened ").append(opened).append("x ")
          .append(type.getDisplayName()).append(" Crate</gradient>")
          .append(" <gray>— Summary:</gray>");

        // tier breakdown
        for (PouchTier tier : PouchTier.values()) {
            int count = tierCounts.getOrDefault(tier, 0);
            if (count == 0) continue;
            sb.append("\n  <dark_gray>▪</dark_gray> ")
              .append(tier.getDisplayName())
              .append(" <gray>x").append(count).append("</gray>");
        }

        // totals
        sb.append("\n  <gray>►</gray> <white>+").append(NumberUtil.format(totalMoney)).append("</white> <gray>Money</gray>")
          .append("  <white>+").append(NumberUtil.format(totalXp)).append("</white> <gray>XP</gray>")
          .append("  <white>+").append(NumberUtil.format(totalTokens)).append("</white> <gray>Tokens</gray>");

        if (vouchers > 0) {
            sb.append("\n  <gradient:#c471f5:#fa71cd>✨ ").append(vouchers)
              .append(" cosmetic voucher").append(vouchers > 1 ? "s" : "").append("!</gradient>");
        }

        MessageUtil.sendRaw(player, sb.toString());
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
        leftovers.values().forEach(l -> player.getWorld().dropItemNaturally(player.getLocation(), l));
    }

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
        int[] slots = CratesGUI.CRATE_SLOTS;
        CrateType[] types = CrateType.values();
        for (int i = 0; i < slots.length && i < types.length; i++) {
            if (slots[i] == slot) return types[i];
        }
        return null;
    }

    private String readable(PouchType type) {
        return switch (type) { case MONEY -> "Money"; case XP -> "XP"; case TOKENS -> "Tokens"; };
    }
}
