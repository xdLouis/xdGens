package de.louis.xdGens.listener;

import de.louis.xdGens.crate.CosmeticVoucherItem;
import de.louis.xdGens.crate.CrateReward;
import de.louis.xdGens.main.Main;
import de.louis.xdGens.util.MessageUtil;
import org.bukkit.Sound;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.block.Action;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.inventory.EquipmentSlot;
import org.bukkit.inventory.ItemStack;

public class CosmeticVoucherListener implements Listener {

    private final Main plugin;

    public CosmeticVoucherListener(Main plugin) {
        this.plugin = plugin;
    }

    @EventHandler(priority = EventPriority.HIGH)
    public void onVoucherRedeem(PlayerInteractEvent event) {
        // Filter: only main hand, only right-click
        if (event.getHand() != EquipmentSlot.HAND) return;
        if (event.getAction() != Action.RIGHT_CLICK_AIR
                && event.getAction() != Action.RIGHT_CLICK_BLOCK) return;

        Player player = event.getPlayer();
        ItemStack item = event.getItem();

        if (!CosmeticVoucherItem.isVoucher(plugin, item)) return;
        event.setCancelled(true);

        CrateReward reward = CosmeticVoucherItem.getReward(plugin, item);
        if (reward == null) return;

        boolean isNew = plugin.getPlayerCosmeticManager().unlock(player, reward);

        // consume one voucher
        if (item.getAmount() <= 1) {
            player.getInventory().setItemInMainHand(null);
        } else {
            item.setAmount(item.getAmount() - 1);
        }

        if (isNew) {
            player.playSound(player.getLocation(), Sound.UI_TOAST_CHALLENGE_COMPLETE, 0.7f, 1.2f);
            String preview = reward.getCosmeticFormat().replace("{name}", player.getName());
            MessageUtil.sendRaw(player, MessageUtil.PREFIX + " <green>\u2728 Unlocked: "
                    + reward.tierLabel() + " </green>" + preview);
            MessageUtil.sendRaw(player, MessageUtil.PREFIX + " <gray>Use <white>/cosmetics</white> to equip it.");
        } else {
            player.playSound(player.getLocation(), Sound.ENTITY_VILLAGER_NO, 0.8f, 1.0f);
            MessageUtil.sendRaw(player, MessageUtil.PREFIX + " <yellow>You already own <white>"
                    + reward.getDisplayName() + "</white>. Voucher consumed.");
        }
    }
}
