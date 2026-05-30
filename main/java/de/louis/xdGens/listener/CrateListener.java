package de.louis.xdGens.listener;

import de.louis.xdGens.crate.*;
import de.louis.xdGens.gui.CratePreviewGUI;
import de.louis.xdGens.gui.CratesGUI;
import de.louis.xdGens.main.Main;
import de.louis.xdGens.manager.CrateManager;
import de.louis.xdGens.util.MessageUtil;
import de.louis.xdGens.util.NumberUtil;
import net.kyori.adventure.text.serializer.plain.PlainTextComponentSerializer;
import org.bukkit.Bukkit;
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

import java.util.ArrayList;
import java.util.EnumMap;
import java.util.List;
import java.util.Map;

public class CrateListener implements Listener {

    private final Main plugin;
    private static final String CRATES_TITLE = "\uD83C\uDF81 Crates";

    public CrateListener(Main plugin) { this.plugin = plugin; }

    @EventHandler(priority = EventPriority.HIGH, ignoreCancelled = true)
    public void onInventoryClick(InventoryClickEvent event) {
        if (!(event.getWhoClicked() instanceof Player player)) return;
        String title = PlainTextComponentSerializer.plainText().serialize(event.getView().title());

        if (title.equals(CRATES_TITLE)) {
            event.setCancelled(true);
            CrateType crate = resolveBySlot(event.getSlot(), CratesGUI.CRATE_SLOTS);
            if (crate == null) return;
            ClickType click = event.getClick();
            if (click == ClickType.RIGHT) {
                CratePreviewGUI.clearState(player.getUniqueId());
                new CratePreviewGUI(plugin, crate).open(player);
            } else if (click == ClickType.SHIFT_LEFT || click == ClickType.SHIFT_RIGHT) {
                handleOpenAll(player, crate);
            } else if (click == ClickType.LEFT || click == ClickType.MIDDLE) {
                handleOpenOne(player, crate);
            }
            return;
        }

        if (title.contains(" Crate") && !title.contains("Crates")) {
            event.setCancelled(true);
            CrateType previewType = CratePreviewGUI.resolveFromTitle(title);
            if (previewType == null) return;
            CratePreviewGUI gui     = new CratePreviewGUI(plugin, previewType);
            int             curPage = CratePreviewGUI.playerPage.getOrDefault(player.getUniqueId(), 0);
            switch (event.getSlot()) {
                case CratePreviewGUI.SLOT_BACK -> { CratePreviewGUI.clearState(player.getUniqueId()); new CratesGUI(plugin).open(player); }
                case CratePreviewGUI.SLOT_PREV -> gui.open(player, curPage - 1);
                case CratePreviewGUI.SLOT_NEXT -> gui.open(player, curPage + 1);
                default -> {}
            }
        }
    }

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

    private void handleOpenOne(Player player, CrateType type) {
        if (!plugin.getVirtualKeyManager().consumeKey(player, type)) {
            MessageUtil.sendRaw(player, MessageUtil.PREFIX
                    + " <red>You don't have a " + type.getDisplayName() + " Key.</red>");
            return;
        }
        CrateManager.CrateOpenResult result = plugin.getCrateManager().openCrate(player, type);
        redeemPouches(player, result);
        boolean isNew = autoRedeemOrStoreVoucher(player, result);
        playCrateSound(player);
        sendOpenMessage(player, type, result, isNew);

        // Broadcast new cosmetic to all players
        if (isNew && result.rolledCosmetic() != null) {
            broadcastCosmetics(player, type, List.of(result.rolledCosmetic()));
        }

        new CratesGUI(plugin).open(player);
    }

    private void handleOpenAll(Player player, CrateType type) {
        int total = plugin.getVirtualKeyManager().getKeys(player, type);
        if (total <= 0) {
            MessageUtil.sendRaw(player, MessageUtil.PREFIX
                    + " <red>You don't have any " + type.getDisplayName() + " Keys.</red>");
            return;
        }

        long totalMoney = 0, totalXp = 0, totalTokens = 0;
        int  newCosmetics = 0, dupVouchers = 0;
        Map<PouchTier, Integer> tierCounts = new EnumMap<>(PouchTier.class);
        List<CrateReward> newCosmeticList  = new ArrayList<>();

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
            if (result.hasVoucher()) {
                boolean isNew = autoRedeemOrStoreVoucher(player, result);
                if (isNew) {
                    newCosmetics++;
                    newCosmeticList.add(result.rolledCosmetic());
                } else {
                    dupVouchers++;
                }
            }
        }

        playCrateSound(player);
        StringBuilder sb = new StringBuilder();
        sb.append(MessageUtil.PREFIX).append(" ")
          .append(type.getGradient()).append("<bold>").append(type.getDisplayName()).append(" Crate</bold></gradient>")
          .append(" <gray>\u00d7 ").append(total).append(" opened</gray>");
        sb.append("\n<dark_gray>\u250c\u2500 Pouches redeemed");
        if (totalMoney  > 0) sb.append("\n<dark_gray>\u2502</dark_gray> <white>+").append(NumberUtil.format(totalMoney)).append("</white> <gray>Money</gray>");
        if (totalXp     > 0) sb.append("\n<dark_gray>\u2502</dark_gray> <white>+").append(NumberUtil.format(totalXp)).append("</white> <gray>XP</gray>");
        if (totalTokens > 0) sb.append("\n<dark_gray>\u2502</dark_gray> <white>+").append(NumberUtil.format(totalTokens)).append("</white> <gray>Tokens</gray>");
        if (!tierCounts.isEmpty()) {
            sb.append("\n<dark_gray>\u2502</dark_gray> <gray>Tiers: ");
            tierCounts.forEach((tier, cnt) -> sb.append(tier.getDisplayName()).append(" <dark_gray>x").append(cnt).append("</dark_gray>  "));
        }
        if (newCosmetics > 0) sb.append("\n<dark_gray>\u251c\u2500</dark_gray> <green>\u2728 ").append(newCosmetics).append(" new cosmetic(s) auto-unlocked!</green>");
        if (dupVouchers  > 0) sb.append("\n<dark_gray>\u251c\u2500</dark_gray> <yellow>").append(dupVouchers).append(" duplicate(s) stored \u2192 cash out via /cosmetics</yellow>");
        sb.append("\n<dark_gray>\u2514\u2500</dark_gray>");
        MessageUtil.sendRaw(player, sb.toString());

        // Broadcast all newly unlocked cosmetics in one message
        if (!newCosmeticList.isEmpty()) {
            broadcastCosmetics(player, type, newCosmeticList);
        }

        new CratesGUI(plugin).open(player);
    }

    /**
     * Sends a server-wide broadcast when a player unlocks one or more new cosmetics.
     * Multiple cosmetics (multi-open) are listed in a single message.
     */
    private void broadcastCosmetics(Player player, CrateType crateType, List<CrateReward> cosmetics) {
        StringBuilder sb = new StringBuilder();
        sb.append("<gold>\uD83C\uDF89 <white><bold>")
          .append(player.getName())
          .append("</bold></white> <gold>hat ein Cosmetic aus einer</gold> ")
          .append(crateType.getGradient()).append("<bold>").append(crateType.getDisplayName())
          .append(" Crate</bold></gradient> <gold>gezogen!</gold>")
          .append("\n");

        if (cosmetics.size() == 1) {
            CrateReward r = cosmetics.get(0);
            sb.append("<dark_gray>\u2514</dark_gray> ")
              .append(r.tierLabel())
              .append(" <white>").append(r.getDisplayName()).append("</white>");
        } else {
            for (int i = 0; i < cosmetics.size(); i++) {
                CrateReward r = cosmetics.get(i);
                boolean last  = i == cosmetics.size() - 1;
                sb.append(last ? "<dark_gray>\u2514</dark_gray> " : "<dark_gray>\u251c</dark_gray> ")
                  .append(r.tierLabel())
                  .append(" <white>").append(r.getDisplayName()).append("</white>");
                if (!last) sb.append("\n");
            }
        }

        String msg = sb.toString();
        for (Player online : Bukkit.getOnlinePlayers()) {
            MessageUtil.sendRaw(online, msg);
        }
    }

    /**
     * New cosmetic  → unlock, return true.
     * Duplicate     → addVoucher (stored in manager), return false.
     */
    private boolean autoRedeemOrStoreVoucher(Player player, CrateManager.CrateOpenResult result) {
        if (!result.hasVoucher()) return false;
        CrateReward cosmetic = result.rolledCosmetic();
        boolean isNew = plugin.getPlayerCosmeticManager().unlock(player, cosmetic);
        if (!isNew) plugin.getPlayerCosmeticManager().addVoucher(player, cosmetic);
        return isNew;
    }

    private void sendOpenMessage(Player player, CrateType type, CrateManager.CrateOpenResult result, boolean cosmeticIsNew) {
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
            if (cosmeticIsNew) {
                sb.append("\n  <dark_gray>\u2514</dark_gray> <green>\u2728 Auto-unlocked:</green> ")
                  .append(result.rolledCosmetic().tierLabel())
                  .append(" <white>").append(result.rolledCosmetic().getDisplayName()).append("</white>");
            } else {
                int stored = plugin.getPlayerCosmeticManager().getVoucherCount(player, result.rolledCosmetic());
                sb.append("\n  <dark_gray>\u2514</dark_gray> <yellow>Duplicate stored:</yellow> ")
                  .append(result.rolledCosmetic().tierLabel())
                  .append(" <white>").append(result.rolledCosmetic().getDisplayName())
                  .append("</white> <dark_gray>(x").append(stored).append(" in /cosmetics)</dark_gray>");
            }
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

    private void playCrateSound(Player player) {
        player.playSound(player.getLocation(), Sound.ENTITY_PLAYER_LEVELUP, 0.9f, 1.2f);
    }

    private CrateType resolveBySlot(int slot, int[] slots) {
        CrateType[] types = CrateType.values();
        for (int i = 0; i < slots.length && i < types.length; i++)
            if (slots[i] == slot) return types[i];
        return null;
    }

    private String readable(PouchType type) {
        return switch (type) { case MONEY -> "Money"; case XP -> "XP"; case TOKENS -> "Tokens"; };
    }
}
