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

    // ─── open backpack on RIGHT-CLICK ───────────────────────────────────────────
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

        // ── Backpack GUI ──────────────────────────────────────────────────
        if (title.contains(BackpackGUI.TITLE)) {
            int rawSlot = event.getRawSlot();

            // Button row (bottom): non-input top slots — always cancel
            if (rawSlot < 54 && !BackpackGUI.isInputSlot(rawSlot)) {
                event.setCancelled(true);
                if (rawSlot == BackpackGUI.SLOT_SELL)    handleBackpackSell(player);
                else if (rawSlot == BackpackGUI.SLOT_UPGRADE) handleBackpackUpgrade(player);
                return;
            }

            // Input slots: only allow recognised crop items
            if (BackpackGUI.isInputSlot(rawSlot)) {
                ItemStack cursor = event.getCursor();
                if (cursor != null && !cursor.getType().isAir()) {
                    if (!isCropItem(cursor)) {
                        event.setCancelled(true);
                        return;
                    }
                }
                return;
            }

            // Player bottom inventory: allow shift-click of crops only
            if (rawSlot >= 54) {
                ItemStack clicked = event.getCurrentItem();
                if (clicked == null || clicked.getType().isAir()) { event.setCancelled(true); return; }
                if (event.isShiftClick() && isCropItem(clicked)) return;
                event.setCancelled(true);
            }
            return;
        }

        // ── Shop GUI ──────────────────────────────────────────────────────
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
        boolean crop = isCropItem(dragged);
        for (int slot : event.getRawSlots()) {
            if (!BackpackGUI.isInputSlot(slot) || !crop) {
                event.setCancelled(true);
                return;
            }
        }
    }

    // ─── on close: flush input slots into backpack storage ────────────────────
    @EventHandler
    public void onClose(InventoryCloseEvent event) {
        if (!(event.getPlayer() instanceof Player player)) return;
        String title = PlainTextComponentSerializer.plainText().serialize(event.getView().title());
        if (!title.contains(BackpackGUI.TITLE)) return;

        var topInv = event.getInventory();
        int addedWheat = 0, addedBlocks = 0, addedBales = 0;

        for (int slot = BackpackGUI.INPUT_START; slot <= BackpackGUI.INPUT_END; slot++) {
            ItemStack item = topInv.getItem(slot);
            if (item == null || item.getType().isAir()) continue;
            topInv.setItem(slot, null);

            if (CustomItemUtil.hasItemType(plugin, item, "farm_wheat")) {
                int stored   = plugin.getBackpackManager().addWheat(player, item.getAmount());
                addedWheat  += stored;
                returnOverflow(player, item, stored);

            } else if (CustomItemUtil.hasItemType(plugin, item, "compressed_wheat_block")) {
                int stored    = plugin.getBackpackManager().addBlocks(player, item.getAmount());
                addedBlocks  += stored;
                returnOverflow(player, item, stored);

            } else if (CustomItemUtil.hasItemType(plugin, item, "enchanted_wheat_bale")) {
                int stored   = plugin.getBackpackManager().addBales(player, item.getAmount());
                addedBales  += stored;
                returnOverflow(player, item, stored);

            } else {
                // Unknown item: return to player
                var overflow = player.getInventory().addItem(item);
                overflow.values().forEach(l -> player.getWorld().dropItemNaturally(player.getLocation(), l));
            }
        }

        if (addedWheat > 0 || addedBlocks > 0 || addedBales > 0) {
            StringBuilder msg = new StringBuilder(MessageUtil.PREFIX + " <gradient:#84fab0:#8fd3f4>Stored in backpack:</gradient> <gray>");
            if (addedWheat  > 0) msg.append(NumberUtil.format(addedWheat)).append("x Wheat");
            if (addedBlocks > 0) { if (addedWheat > 0) msg.append(", "); msg.append(NumberUtil.format(addedBlocks)).append("x Block"); }
            if (addedBales  > 0) { if (addedWheat > 0 || addedBlocks > 0) msg.append(", "); msg.append(NumberUtil.format(addedBales)).append("x Bale"); }
            msg.append("</gray>");
            MessageUtil.sendRaw(player, msg.toString());
        }

        plugin.getBackpackManager().savePlayer(player);
    }

    // ─── button handlers ───────────────────────────────────────────────────
    private void handleBackpackSell(Player player) {
        int wheat  = plugin.getBackpackManager().getStoredWheat(player);
        int blocks = plugin.getBackpackManager().getStoredBlocks(player);
        int bales  = plugin.getBackpackManager().getStoredBales(player);

        if (wheat == 0 && blocks == 0 && bales == 0) {
            MessageUtil.sendRaw(player, MessageUtil.PREFIX + " <red>Your backpack is empty.</red>");
            return;
        }

        // Fallbacks match config.yml defaults exactly
        double wheatPrice = plugin.getConfig().getDouble("sell.wheat.price", 20.0);
        double blockPrice = plugin.getConfig().getDouble("sell.compressed_wheat_block.price", 1500.0);
        double balePrice  = plugin.getConfig().getDouble("sell.enchanted_wheat_bale.price",  120000.0);
        double earned     = (wheat * wheatPrice) + (blocks * blockPrice) + (bales * balePrice);

        plugin.getBackpackManager().removeWheat(player,  wheat);
        plugin.getBackpackManager().removeBlocks(player, blocks);
        plugin.getBackpackManager().removeBales(player,  bales);
        plugin.getCurrencyManager().addMoney(player, earned);
        plugin.getBackpackManager().savePlayer(player);
        plugin.getCurrencyManager().savePlayer(player);

        MessageUtil.sendRaw(player, MessageUtil.PREFIX
                + " <gradient:#7afcff:#00c2ff>Sold backpack contents</gradient>"
                + " <gray>for</gray> <green>$" + NumberUtil.format(earned) + "</green>"
                + " <gray>(Wheat: " + wheat + ", Blocks: " + blocks + ", Bales: " + bales + ")</gray>");
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

    // ─── helpers ──────────────────────────────────────────────────────────────
    private boolean isCropItem(ItemStack item) {
        return CustomItemUtil.hasItemType(plugin, item, "farm_wheat")
                || CustomItemUtil.hasItemType(plugin, item, "compressed_wheat_block")
                || CustomItemUtil.hasItemType(plugin, item, "enchanted_wheat_bale");
    }

    private void returnOverflow(Player player, ItemStack original, int stored) {
        int leftover = original.getAmount() - stored;
        if (leftover <= 0) return;
        original.setAmount(leftover);
        var overflow = player.getInventory().addItem(original);
        overflow.values().forEach(l -> player.getWorld().dropItemNaturally(player.getLocation(), l));
    }
}
