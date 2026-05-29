package de.louis.xdGens.listener;

import de.louis.xdGens.crate.CrateType;
import de.louis.xdGens.crate.PouchItem;
import de.louis.xdGens.crate.PouchTier;
import de.louis.xdGens.crate.PouchType;
import de.louis.xdGens.gui.CratePreviewGUI;
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

    private static final String CRATES_MENU_TITLE = "\uD83C\uDF81 Crates";

    public CrateListener(Main plugin) {
        this.plugin = plugin;
    }

    @EventHandler(priority = EventPriority.HIGH, ignoreCancelled = true)
    public void onInventoryClick(InventoryClickEvent event) {
        if (!(event.getWhoClicked() instanceof Player player)) return;
        String title = PlainTextComponentSerializer.plainText().serialize(event.getView().title());

        // ── Crates Hauptmenü ─────────────────────────────────────────────
        if (title.equals(CRATES_MENU_TITLE) || title.endsWith("Crates")) {
            event.setCancelled(true);
            int slot = event.getSlot();

            // ─ Kompass ─────────────────────────────────────────────────
            if (slot == CratesGUI.SLOT_COMPASS) {
                int cur = CratesGUI.focusIndex.getOrDefault(player.getUniqueId(), 0);
                int total = CrateType.values().length;

                if (event.getClick() == ClickType.SHIFT_LEFT
                        || event.getClick() == ClickType.SHIFT_RIGHT
                        || event.getClick() == ClickType.MIDDLE) {
                    // Preview der fokussierten Crate öffnen
                    CrateType focused = CrateType.values()[Math.floorMod(cur, total)];
                    CratePreviewGUI.clearState(player.getUniqueId());
                    new CratePreviewGUI(plugin, focused).open(player);
                } else if (event.getClick() == ClickType.RIGHT) {
                    // vorherige Crate
                    new CratesGUI(plugin).open(player, Math.floorMod(cur - 1, total));
                } else {
                    // nächste Crate (Links / alles andere)
                    new CratesGUI(plugin).open(player, Math.floorMod(cur + 1, total));
                }
                return;
            }

            // ─ Crate-Item ───────────────────────────────────────────────
            CrateType crateType = resolveBySlot(slot, CratesGUI.CRATE_SLOTS);
            if (crateType == null) return;

            // Beim Klick auf Crate auch den Fokus setzen
            int crateIdx = crateIndexOf(crateType);
            CratesGUI.focusIndex.put(player.getUniqueId(), crateIdx);

            if (event.getClick() == ClickType.RIGHT) {
                CratePreviewGUI.clearState(player.getUniqueId());
                new CratePreviewGUI(plugin, crateType).open(player);
            } else if (event.getClick() == ClickType.SHIFT_LEFT
                    || event.getClick() == ClickType.SHIFT_RIGHT
                    || event.getClick() == ClickType.MIDDLE) {
                handleOpenAll(player, crateType);
            } else {
                handleOpenOne(player, crateType);
            }
            return;
        }

        // ── Preview GUI ──────────────────────────────────────────────
        if (title.contains(" Crate") && !title.endsWith("Crates")) {
            event.setCancelled(true);
            CrateType previewType = CratePreviewGUI.resolveFromTitle(title);
            if (previewType == null) return;

            CratePreviewGUI gui     = new CratePreviewGUI(plugin, previewType);
            int             curPage = CratePreviewGUI.playerPage.getOrDefault(player.getUniqueId(), 0);

            switch (event.getSlot()) {
                case CratePreviewGUI.SLOT_BACK -> {
                    CratePreviewGUI.clearState(player.getUniqueId());
                    new CratesGUI(plugin).open(player);
                }
                case CratePreviewGUI.SLOT_PREV -> gui.open(player, curPage - 1);
                case CratePreviewGUI.SLOT_NEXT -> gui.open(player, curPage + 1);
                default -> { /* view only */ }
            }
        }
    }

    // ── Pouch Rechtsklick ───────────────────────────────────────────────

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
        MessageUtil.sendRaw(player, MessageUtil.PREFIX + " <green>Pouch geöffnet:</green> <white>+"
                + NumberUtil.format(value) + " " + readable(type) + "</white>");
    }

    // ── Open One ───────────────────────────────────────────────────────

    private void handleOpenOne(Player player, CrateType type) {
        if (!plugin.getVirtualKeyManager().consumeKey(player, type)) {
            MessageUtil.sendRaw(player, MessageUtil.PREFIX
                    + " <red>Du hast keinen " + type.getDisplayName() + " Key.</red>");
            return;
        }
        CrateManager.CrateOpenResult result = plugin.getCrateManager().openCrate(player, type);
        redeemPouches(player, result);
        giveVoucher(player, result);
        playCrateSound(player);
        sendOpenMessage(player, type, result);
        new CratesGUI(plugin).open(player);
    }

    // ── Open All ───────────────────────────────────────────────────────

    private void handleOpenAll(Player player, CrateType type) {
        int total = plugin.getVirtualKeyManager().getKeys(player, type);
        if (total <= 0) {
            MessageUtil.sendRaw(player, MessageUtil.PREFIX
                    + " <red>Du hast keine " + type.getDisplayName() + " Keys.</red>");
            return;
        }

        long totalMoney  = 0, totalXp = 0, totalTokens = 0;
        int  vouchers    = 0;
        Map<PouchTier, Integer> tierCounts = new EnumMap<>(PouchTier.class);

        for (int i = 0; i < total; i++) {
            if (!plugin.getVirtualKeyManager().consumeKey(player, type)) break;
            CrateManager.CrateOpenResult result = plugin.getCrateManager().openCrate(player, type);
            tierCounts.merge(result.pouchTier(), 1, Integer::sum);
            for (ItemStack pouch : result.pouches()) {
                PouchType pt  = PouchItem.getType(plugin, pouch);
                long      val = PouchItem.getValue(plugin, pouch);
                if (pt == null || val <= 0) continue;
                switch (pt) {
                    case MONEY  -> { totalMoney  += val; plugin.getCurrencyManager().addMoney(player, val); }
                    case TOKENS -> { totalTokens += val; plugin.getCurrencyManager().addTokens(player, (int) val); }
                    case XP     -> { totalXp     += val; plugin.getProgressionManager().addXp(player, val); }
                }
            }
            if (result.hasVoucher()) { giveVoucher(player, result); vouchers++; }
        }

        playCrateSound(player);
        StringBuilder sb = new StringBuilder();
        sb.append(MessageUtil.PREFIX).append(" ")
          .append(type.getGradient()).append("<bold>").append(type.getDisplayName()).append(" Crate</bold></gradient>")
          .append(" <gray>\u00d7 ").append(total).append(" geöffnet</gray>");
        sb.append("\n<dark_gray>\u250c\u2500 Pouches eingelöst");
        if (totalMoney  > 0) sb.append("\n<dark_gray>\u2502</dark_gray> <white>+").append(NumberUtil.format(totalMoney)).append("</white> <gray>Money</gray>");
        if (totalXp     > 0) sb.append("\n<dark_gray>\u2502</dark_gray> <white>+").append(NumberUtil.format(totalXp)).append("</white> <gray>XP</gray>");
        if (totalTokens > 0) sb.append("\n<dark_gray>\u2502</dark_gray> <white>+").append(NumberUtil.format(totalTokens)).append("</white> <gray>Tokens</gray>");
        if (!tierCounts.isEmpty()) {
            sb.append("\n<dark_gray>\u2502</dark_gray> <gray>Tiers: ");
            tierCounts.forEach((tier, cnt) -> sb.append(tier.getDisplayName()).append(" <dark_gray>x").append(cnt).append("</dark_gray>  "));
        }
        if (vouchers > 0) sb.append("\n<dark_gray>\u251c\u2500 Cosmetic Vouchers: <white>").append(vouchers).append("</white> <gray>(im Inventar)</gray>");
        sb.append("\n<dark_gray>\u2514\u2500</dark_gray>");
        MessageUtil.sendRaw(player, sb.toString());
        new CratesGUI(plugin).open(player);
    }

    // ── Helpers ───────────────────────────────────────────────────────

    private void sendOpenMessage(Player player, CrateType type, CrateManager.CrateOpenResult result) {
        StringBuilder sb = new StringBuilder();
        sb.append(MessageUtil.PREFIX).append(" ")
          .append(type.getGradient()).append(type.getDisplayName()).append(" Crate</gradient>")
          .append(" <gray>\u2502</gray> ")
          .append(result.pouchTier().getDisplayName()).append(" <gray>Pouches</gray>");
        for (ItemStack pouch : result.pouches()) {
            PouchType pt  = PouchItem.getType(plugin, pouch);
            long      val = PouchItem.getValue(plugin, pouch);
            if (pt == null || val <= 0) continue;
            sb.append("\n  <dark_gray>\u2514</dark_gray> <white>+").append(NumberUtil.format(val))
              .append("</white> <gray>").append(readable(pt)).append("</gray>");
        }
        if (result.hasVoucher()) {
            sb.append("\n  <dark_gray>\u2514</dark_gray> ")
              .append(result.rolledCosmetic().tierLabel())
              .append(" <white>").append(result.rolledCosmetic().getDisplayName())
              .append("</white> <gray>Cosmetic Voucher!</gray>");
        }
        MessageUtil.sendRaw(player, sb.toString());
    }

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

    private void playCrateSound(Player player) {
        player.playSound(player.getLocation(), Sound.ENTITY_PLAYER_LEVELUP, 0.9f, 1.2f);
    }

    private CrateType resolveBySlot(int slot, int[] slots) {
        CrateType[] types = CrateType.values();
        for (int i = 0; i < slots.length && i < types.length; i++) {
            if (slots[i] == slot) return types[i];
        }
        return null;
    }

    private int crateIndexOf(CrateType type) {
        CrateType[] types = CrateType.values();
        for (int i = 0; i < types.length; i++) if (types[i] == type) return i;
        return 0;
    }

    private String readable(PouchType type) {
        return switch (type) { case MONEY -> "Money"; case XP -> "XP"; case TOKENS -> "Tokens"; };
    }
}
