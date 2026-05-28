package de.louis.xdGens.listener;

import de.louis.xdGens.main.Main;
import de.louis.xdGens.util.HoeUtil;
import de.louis.xdGens.util.MessageUtil;
import org.bukkit.entity.Item;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.PlayerDeathEvent;
import org.bukkit.event.inventory.ClickType;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.inventory.InventoryCreativeEvent;
import org.bukkit.event.inventory.InventoryDragEvent;
import org.bukkit.event.player.PlayerDropItemEvent;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerRespawnEvent;
import org.bukkit.inventory.ItemStack;

public class HoeProtectionListener implements Listener {

    private final Main plugin;

    public HoeProtectionListener(Main plugin) {
        this.plugin = plugin;
    }

    @EventHandler(priority = EventPriority.HIGHEST, ignoreCancelled = false)
    public void onDrop(PlayerDropItemEvent event) {
        Item dropped = event.getItemDrop();
        if (HoeUtil.isXdHoe(dropped.getItemStack())) {
            event.setCancelled(true);
            ensureHoe(event.getPlayer());
        }
    }

    @EventHandler(priority = EventPriority.HIGHEST, ignoreCancelled = false)
    public void onCreative(InventoryCreativeEvent event) {
        if (!(event.getWhoClicked() instanceof Player player)) {
            return;
        }

        ItemStack cursor = event.getCursor();
        ItemStack current = event.getCurrentItem();

        if (HoeUtil.isXdHoe(cursor) || HoeUtil.isXdHoe(current)) {
            event.setCancelled(true);
            ensureHoe(player);
        }
    }

    @EventHandler(priority = EventPriority.HIGHEST, ignoreCancelled = false)
    public void onInventoryClick(InventoryClickEvent event) {
        if (!(event.getWhoClicked() instanceof Player player)) {
            return;
        }

        ItemStack current = event.getCurrentItem();
        ItemStack cursor = event.getCursor();

        if (HoeUtil.isXdHoe(current) || HoeUtil.isXdHoe(cursor)) {
            event.setCancelled(true);
            ensureHoe(player);
            return;
        }

        if (event.getClick() == ClickType.NUMBER_KEY) {
            int hotbar = event.getHotbarButton();
            if (hotbar >= 0 && hotbar < player.getInventory().getSize()) {
                ItemStack hotbarItem = player.getInventory().getItem(hotbar);
                if (HoeUtil.isXdHoe(hotbarItem)) {
                    event.setCancelled(true);
                    ensureHoe(player);
                }
            }
        }
    }

    @EventHandler(priority = EventPriority.HIGHEST, ignoreCancelled = false)
    public void onInventoryDrag(InventoryDragEvent event) {
        if (!(event.getWhoClicked() instanceof Player player)) {
            return;
        }

        if (HoeUtil.isXdHoe(event.getOldCursor())) {
            event.setCancelled(true);
            ensureHoe(player);
        }
    }

    @EventHandler(priority = EventPriority.HIGHEST)
    public void onDeath(PlayerDeathEvent event) {
        event.getDrops().removeIf(HoeUtil::isXdHoe);
        event.setKeepInventory(true);
    }

    @EventHandler
    public void onJoin(PlayerJoinEvent event) {
        ensureHoe(event.getPlayer());
    }

    @EventHandler
    public void onRespawn(PlayerRespawnEvent event) {
        plugin.getServer().getScheduler().runTaskLater(plugin, () -> ensureHoe(event.getPlayer()), 1L);
    }

    private void ensureHoe(Player player) {
        if (hasHoe(player)) {
            return;
        }

        player.getInventory().addItem(HoeUtil.createStarterHoe(plugin));
        MessageUtil.sendRaw(player,
                MessageUtil.PREFIX + " <gray>Your <gradient:#7afcff:#00c2ff>Starter Hoe</gradient> has been restored.</gray>");
    }

    private boolean hasHoe(Player player) {
        for (ItemStack item : player.getInventory().getContents()) {
            if (HoeUtil.isXdHoe(item)) {
                return true;
            }
        }
        return false;
    }
}