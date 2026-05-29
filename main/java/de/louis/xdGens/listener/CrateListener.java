package de.louis.xdGens.listener;

import de.louis.xdGens.crate.CrateType;
import de.louis.xdGens.crate.PouchItem;
import de.louis.xdGens.crate.PouchType;
import de.louis.xdGens.gui.CratesGUI;
import de.louis.xdGens.main.Main;
import de.louis.xdGens.manager.PouchKeyItem;
import de.louis.xdGens.util.MessageUtil;
import de.louis.xdGens.util.NumberUtil;
import net.kyori.adventure.text.serializer.plain.PlainTextComponentSerializer;
import org.bukkit.Sound;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.block.Action;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.inventory.EquipmentSlot;
import org.bukkit.inventory.ItemStack;

import java.util.List;

public class CrateListener implements Listener {

    private final Main plugin;

    public CrateListener(Main plugin) {
        this.plugin = plugin;
    }

    @EventHandler(priority = EventPriority.HIGH, ignoreCancelled = true)
    public void onInventoryClick(InventoryClickEvent event) {
        if (!(event.getWhoClicked() instanceof Player player)) return;
        String title = PlainTextComponentSerializer.plainText().serialize(event.getView().title());
        if (!title.contains("Crates")) return;

        event.setCancelled(true);
        ItemStack clicked = event.getCurrentItem();
        if (clicked == null || clicked.getType().isAir()) return;

        CrateType crateType = resolveCrateBySlot(event.getSlot());
        if (crateType == null) return;

        ItemStack key = findOneKey(player, crateType);
        if (key == null) {
            MessageUtil.sendRaw(player, MessageUtil.PREFIX + " <red>You don't have a " + crateType.getDisplayName() + " key.</red>");
            return;
        }

        consumeOne(key);
        List<ItemStack> rewards = plugin.getCrateManager().createRewards(crateType);
        rewards.forEach(reward -> player.getInventory().addItem(reward));

        player.playSound(player.getLocation(), Sound.ENTITY_PLAYER_LEVELUP, 0.9f, 1.2f);
        MessageUtil.sendRaw(player, MessageUtil.PREFIX + " " + crateType.getGradient() + crateType.getDisplayName() + " Crate opened!</gradient> <gray>You received 3 pouches.</gray>");
        new CratesGUI(plugin).open(player);
    }

    @EventHandler(priority = EventPriority.HIGH)
    public void onPouchUse(PlayerInteractEvent event) {
        if (event.getHand() != EquipmentSlot.HAND) return;
        if (event.getAction() != Action.RIGHT_CLICK_AIR && event.getAction() != Action.RIGHT_CLICK_BLOCK) return;

        Player player = event.getPlayer();
        ItemStack item = event.getItem();
        if (!PouchItem.isPouch(plugin, item)) return;
        event.setCancelled(true);

        PouchType type = PouchItem.getType(plugin, item);
        long value = PouchItem.getValue(plugin, item);
        if (type == null || value <= 0) return;

        switch (type) {
            case MONEY -> plugin.getCurrencyManager().addMoney(player, value);
            case TOKENS -> plugin.getCurrencyManager().addTokens(player, value);
            case XP -> plugin.getProgressionManager().addXP(player, value);
        }

        consumeOne(item);
        player.playSound(player.getLocation(), Sound.ENTITY_EXPERIENCE_ORB_PICKUP, 1f, 1.1f);
        MessageUtil.sendRaw(player, MessageUtil.PREFIX + " <green>Opened pouch:</green> <white>+" + NumberUtil.format(value) + " " + readable(type) + "</white>");
    }

    private CrateType resolveCrateBySlot(int slot) {
        return switch (slot) {
            case 10 -> CrateType.COMMON;
            case 11 -> CrateType.UNCOMMON;
            case 12 -> CrateType.RARE;
            case 13 -> CrateType.EPIC;
            case 14 -> CrateType.LEGENDARY;
            default -> null;
        };
    }

    private ItemStack findOneKey(Player player, CrateType type) {
        for (ItemStack item : player.getInventory().getContents()) {
            if (!PouchKeyItem.isKey(plugin, item)) continue;
            CrateType found = PouchKeyItem.getCrateType(plugin, item);
            if (found == type) return item;
        }
        return null;
    }

    private void consumeOne(ItemStack item) {
        item.setAmount(item.getAmount() - 1);
    }

    private String readable(PouchType type) {
        return switch (type) {
            case MONEY -> "Money";
            case XP -> "XP";
            case TOKENS -> "Tokens";
        };
    }
}
