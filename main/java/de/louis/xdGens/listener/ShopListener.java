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
import org.bukkit.event.inventory.InventoryDragEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.inventory.ItemStack;

public class ShopListener implements Listener {

    private final Main plugin;

    public ShopListener(Main plugin) {
        this.plugin = plugin;
    }

    // ─── open backpack on RIGHT-CLICK ─────────────────────────────────────────
    @EventHandler(priority = EventPriority.HIGH, ignoreCancelled = true)
    public void onBackpackClick(PlayerInteractEvent event) {
        ItemStack item = event.getItem();
        if (item == null || !CustomItemUtil.isBackpack(plugin, item)) return;

        Action action = event.getAction();
        boolean rightClick = action == Action.RIGHT_CLICK_AIR || action == Action.RIGHT_CLICK_BLOCK;
        if (!rightClick) return;

        event.setCancelled(true);
        event.getPlayer().openInventory(new BackpackGUI(plugin, event.getPlayer()).create());
    }

    // ─── inventory click ─────────────────────────────────────────────────────
    @EventHandler(priority = EventPriority.HIGH)
    public void onInventoryClick(InventoryClickEvent event) {
        if (!(event.getWhoClicked() instanceof Player player)) return;
        String title = PlainTextComponentSerializer.plainText().serialize(event.getView().title());

        // ── Backpack GUI ──────────────────────────────────────────────────────
        if (title.contains(BackpackGUI.TITLE)) {
            int rawSlot = event.getRawSlot();

            // Button clicks (top inventory only, non-input slots)
            if (rawSlot < 54 && !BackpackGUI.isInputSlot(rawSlot)) {
                event.setCancelled(true);
                if (rawSlot == BackpackGUI.SLOT_SELL)    handleBackpackSell(player);
                else if (rawSlot == BackpackGUI.SLOT_UPGRADE) handleBackpackUpgrade(player);
                return;
            }

            // Input slots: allow placing items, but only farm crop types
            if (BackpackGUI.isInputSlot(rawSlot)) {
                ItemStack cursor = event.getCursor();
                if (cursor != null && !cursor.getType().isAir()) {
                    // Only allow recognised farm items
                    if (!CustomItemUtil.hasItemType(plugin, cursor, "farm_wheat")
                            && !CustomItemUtil.hasItemType(plugin, cursor, "compressed_wheat_block")
                            && !CustomItemUtil.hasItemType(plugin, cursor, "enchanted_wheat_bale")) {
                        event.setCancelled(true);
                        return;
                    }
                }
                // Allow the vanilla click (item goes into slot)
                return;
            }

            // Player's own bottom inventory (slots 54+): allow shift-clicking crops INTO backpack GUI
            if (rawSlot >= 54) {
                ItemStack clicked = event.getCurrentItem();
                if (clicked == null || clicked.getType().isAir()) { event.setCancelled(true); return; }
                // Only shift-click crops in
                if (event.isShiftClick()) {
                    if (CustomItemUtil.hasItemType(plugin, clicked, "farm_wheat")
                            || CustomItemUtil.hasItemType(plugin, clicked, "compressed_wheat_block")
                            || CustomItemUtil.hasItemType(plugin, clicked, "enchanted_wheat_bale")) {
                        // Allow shift-click – vanilla will move it to first free input slot
                        return;
                    }
                }
                event.setCancelled(true);
            }
            return;
        }

        // ── Shop GUI ──────────────────────────────────────────────────────────
        if (title.contains(ShopGUI.TITLE)) {
            event.setCancelled(true);
            if (event.getRawSlot() == ShopGUI.SLOT_BACKPACK) handleShopBuyBackpack(player);
        }
    }

    // ─── block dragging non-crop items into backpack GUI ─────────────────────
    @EventHandler(priority = EventPriority.HIGH)
    public void onInventoryDrag(InventoryDragEvent event) {
        if (!(event.getWhoClicked() instanceof Player)) return;
        String title = PlainTextComponentSerializer.plainText().serialize(event.getView().title());
        if (!title.contains(BackpackGUI.TITLE)) return;

        ItemStack dragged = event.getOldCursor();
        boolean cropItem = CustomItemUtil.hasItemType(plugin, dragged, "farm_wheat")
                || CustomItemUtil.hasItemType(plugin, dragged, "compressed_wheat_block")
                || CustomItemUtil.hasItemType(plugin, dragged, "enchanted_wheat_bale");

        for (int slot : event.getRawSlots()) {
            // Block dragging into stat/button rows or if item is not a crop
            if (!BackpackGUI.isInputSlot(slot) || !cropItem) {
                event.setCancelled(true);
                return;
            }
        }
    }

    // ─── on close: flush everything in input slots into backpack storage ──────
    @EventHandler
    public void onClose(InventoryCloseEvent event) {
        if (!(event.getPlayer() instanceof Player player)) return;
        String title = PlainTextComponentSerializer.plainText().serialize(event.getView().title());
        if (!title.contains(BackpackGUI.TITLE)) return;

        var topInv = event.getInventory();
        int addedWheat = 0;

        for (int slot = BackpackGUI.INPUT_START; slot <= BackpackGUI.INPUT_END; slot++) {
            ItemStack item = topInv.getItem(slot);
            if (item == null || item.getType().isAir()) continue;

            // farm_wheat → store directly in BackpackManager
            if (CustomItemUtil.hasItemType(plugin, item, "farm_wheat")) {
                int toStore  = item.getAmount();
                int stored   = plugin.getBackpackManager().addWheat(player, toStore);
                addedWheat  += stored;
                int leftover = toStore - stored;
                if (leftover > 0) {
                    item.setAmount(leftover);
                    var overflow = player.getInventory().addItem(item);
                    overflow.values().forEach(l -> player.getWorld().dropItemNaturally(player.getLocation(), l));
                } else {
                    topInv.setItem(slot, null);
                }
                continue;
            }

            // other crop types (blocks, bales): return to player inventory
            topInv.setItem(slot, null);
            var overflow = player.getInventory().addItem(item);
            overflow.values().forEach(l -> player.getWorld().dropItemNaturally(player.getLocation(), l));
        }

        if (addedWheat > 0) {
            MessageUtil.sendRaw(player, MessageUtil.PREFIX
                    + " <gradient:#84fab0:#8fd3f4>+" + addedWheat + " wheat stored in backpack.</gradient>");
        }

        plugin.getBackpackManager().savePlayer(player);
    }

    // ─── button handlers ─────────────────────────────────────────────────────
    private void handleBackpackSell(Player player) {
        int wheat = plugin.getBackpackManager().getStoredWheat(player);
        if (wheat <= 0) {
            MessageUtil.sendRaw(player, MessageUtil.PREFIX + " <red>Your backpack is empty.</red>");
            return;
        }
        double price  = plugin.getConfig().getDouble("sell.wheat.price", 12.0);
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
                + " <gray>New capacity: <white>" + NumberUtil.format(plugin.getBackpackManager().getCapacity(player)) + "</white></gray>");
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
