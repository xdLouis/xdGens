package de.louis.xdGens.listener;

import de.louis.xdGens.gui.BackpackGUI;
import de.louis.xdGens.gui.ShopGUI;
import de.louis.xdGens.main.Main;
import de.louis.xdGens.util.CustomItemUtil;
import de.louis.xdGens.util.MessageUtil;
import de.louis.xdGens.util.NumberUtil;
import net.kyori.adventure.text.serializer.plain.PlainTextComponentSerializer;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.block.Action;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.inventory.InventoryCloseEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.inventory.ItemStack;

public class ShopListener implements Listener {

    private final Main plugin;

    public ShopListener(Main plugin) {
        this.plugin = plugin;
    }

    // Backpack item: LEFT-click opens GUI
    @EventHandler(priority = EventPriority.HIGH, ignoreCancelled = true)
    public void onBackpackClick(PlayerInteractEvent event) {
        ItemStack item = event.getItem();
        if (item == null || !CustomItemUtil.isBackpack(plugin, item)) return;

        Action action = event.getAction();
        boolean leftClick = action == Action.LEFT_CLICK_AIR || action == Action.LEFT_CLICK_BLOCK;
        if (!leftClick) return;

        event.setCancelled(true);
        event.getPlayer().openInventory(new BackpackGUI(plugin, event.getPlayer()).create());
    }

    @EventHandler(priority = EventPriority.HIGH, ignoreCancelled = true)
    public void onInventoryClick(InventoryClickEvent event) {
        if (!(event.getWhoClicked() instanceof Player player)) return;
        String title = PlainTextComponentSerializer.plainText().serialize(event.getView().title());

        // Backpack GUI
        if (title.contains("Crop Backpack")) {
            event.setCancelled(true);
            int slot = event.getRawSlot();
            if (slot == BackpackGUI.SLOT_SELL)    handleBackpackSell(player);
            else if (slot == BackpackGUI.SLOT_UPGRADE) handleBackpackUpgrade(player);
            return;
        }

        // Shop GUI
        if (title.contains("xdGens Shop")) {
            event.setCancelled(true);
            if (event.getRawSlot() == ShopGUI.SLOT_BACKPACK) handleShopBuyBackpack(player);
        }
    }

    @EventHandler
    public void onClose(InventoryCloseEvent event) {
        if (!(event.getPlayer() instanceof Player player)) return;
        String title = PlainTextComponentSerializer.plainText().serialize(event.getView().title());
        if (title.contains("Crop Backpack")) plugin.getBackpackManager().savePlayer(player);
    }

    private void handleBackpackSell(Player player) {
        int wheat = plugin.getBackpackManager().getStoredWheat(player);
        if (wheat <= 0) {
            MessageUtil.sendRaw(player, MessageUtil.PREFIX + " <red>Your backpack is empty.</red>");
            return;
        }
        double price = plugin.getConfig().getDouble("sell.wheat.price", 12.0);
        double earned = wheat * price;
        plugin.getBackpackManager().removeWheat(player, wheat);
        plugin.getCurrencyManager().addMoney(player, earned);
        plugin.getBackpackManager().savePlayer(player);
        plugin.getCurrencyManager().savePlayer(player);
        MessageUtil.sendRaw(player, MessageUtil.PREFIX
                + " <gradient:#7afcff:#00c2ff>Sold backpack contents</gradient>"
                + " <gray>for</gray> <green>$" + NumberUtil.format(earned) + "</green>"
                + " <gray>(Wheat: " + wheat + ")</gray>");
        player.closeInventory();
    }

    private void handleBackpackUpgrade(Player player) {
        if (!plugin.getBackpackManager().canUpgrade(player)) {
            MessageUtil.sendRaw(player, MessageUtil.PREFIX + " <red>Backpack is already max level.</red>");
            return;
        }
        double cost = plugin.getBackpackManager().getUpgradeCost(player);
        if (plugin.getCurrencyManager().getMoney(player) < cost) {
            MessageUtil.sendRaw(player, MessageUtil.PREFIX
                    + " <red>Not enough money. Need: <white>$" + NumberUtil.format(cost) + "</white></red>");
            return;
        }
        plugin.getBackpackManager().upgrade(player);
        int newLevel = plugin.getBackpackManager().getLevel(player);
        MessageUtil.sendRaw(player, MessageUtil.PREFIX
                + " <gradient:#84fab0:#8fd3f4>Backpack upgraded to Level " + newLevel + "!</gradient>"
                + " <gray>New capacity: <white>" + plugin.getBackpackManager().getCapacity(player) + "</white></gray>");
        player.openInventory(new BackpackGUI(plugin, player).create());
    }

    private void handleShopBuyBackpack(Player player) {
        if (plugin.getBackpackManager().playerHasItem(player)) {
            MessageUtil.sendRaw(player, MessageUtil.PREFIX + " <red>You already have a backpack!</red>");
            return;
        }
        double cost = ShopGUI.BACKPACK_PRICE;
        if (plugin.getCurrencyManager().getMoney(player) < cost) {
            MessageUtil.sendRaw(player, MessageUtil.PREFIX
                    + " <red>Not enough money. Need: <white>$" + NumberUtil.format(cost) + "</white></red>");
            return;
        }
        plugin.getCurrencyManager().removeMoney(player, cost);
        plugin.getCurrencyManager().savePlayer(player);
        ItemStack bp = plugin.getBackpackManager().createItem(player);
        var leftover = player.getInventory().addItem(bp);
        leftover.values().forEach(l -> player.getWorld().dropItemNaturally(player.getLocation(), l));
        MessageUtil.sendRaw(player, MessageUtil.PREFIX
                + " <gradient:#84fab0:#8fd3f4>Crop Backpack purchased!</gradient>"
                + " <gray>Cost: <red>$" + NumberUtil.format(cost) + "</red></gray>");
        player.closeInventory();
    }
}
