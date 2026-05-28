package de.louis.xdGens.listener;

import de.louis.xdGens.main.Main;
import de.louis.xdGens.util.CustomItemUtil;
import de.louis.xdGens.util.MessageUtil;
import de.louis.xdGens.workstation.WorkstationManager;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.block.Action;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.event.block.BlockPlaceEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;

public class WorkstationListener implements Listener {

    private final Main plugin;
    private final WorkstationManager workstationManager;

    public WorkstationListener(Main plugin, WorkstationManager workstationManager) {
        this.plugin = plugin;
        this.workstationManager = workstationManager;
    }

    @EventHandler(priority = EventPriority.HIGHEST, ignoreCancelled = true)
    public void onPlace(BlockPlaceEvent event) {
        if (!CustomItemUtil.isWorkstationItem(plugin, event.getItemInHand())) return;

        workstationManager.register(event.getBlockPlaced().getLocation());

        MessageUtil.sendRaw(event.getPlayer(),
                MessageUtil.PREFIX + " <gray>Workstation placed and saved.</gray>");
    }

    @EventHandler(priority = EventPriority.HIGHEST, ignoreCancelled = true)
    public void onBreak(BlockBreakEvent event) {
        Block block = event.getBlock();
        if (block.getType() != Material.SMITHING_TABLE) return;
        if (!workstationManager.isWorkstation(block.getLocation())) return;

        event.setDropItems(false);
        workstationManager.unregister(block.getLocation());
        block.getWorld().dropItemNaturally(
                block.getLocation().clone().add(0.5, 0.5, 0.5),
                CustomItemUtil.createWorkstationItem(plugin)
        );
    }

    @EventHandler(priority = EventPriority.HIGHEST, ignoreCancelled = true)
    public void onInteract(PlayerInteractEvent event) {
        if (event.getAction() != Action.RIGHT_CLICK_BLOCK || event.getClickedBlock() == null) return;

        Block block = event.getClickedBlock();
        if (block.getType() != Material.SMITHING_TABLE) return;
        if (!workstationManager.isWorkstation(block.getLocation())) return;

        event.setCancelled(true);
        autoCraft(event.getPlayer());
    }

    private void autoCraft(Player player) {
        Inventory inv = player.getInventory();

        int wheat = count(inv, "farm_wheat");
        int wheatBlocksCreated = wheat / 64;

        if (wheatBlocksCreated > 0) {
            remove(inv, "farm_wheat", wheatBlocksCreated * 64);
            giveOrDrop(player, CustomItemUtil.createCompressedWheatBlock(plugin, wheatBlocksCreated));
        }

        int wheatBlocks = count(inv, "compressed_wheat_block");
        int enchantedCreated = wheatBlocks / 64;

        if (enchantedCreated > 0) {
            remove(inv, "compressed_wheat_block", enchantedCreated * 64);
            giveOrDrop(player, CustomItemUtil.createEnchantedWheatBale(plugin, enchantedCreated));
        }

        if (wheatBlocksCreated == 0 && enchantedCreated == 0) {
            MessageUtil.send(player, MessageUtil.PREFIX + " <red>Not enough materials.</red>");
            return;
        }

        MessageUtil.sendRaw(player,
                MessageUtil.PREFIX
                        + " <gradient:#7afcff:#00c2ff>Auto craft complete</gradient>"
                        + " <gray>(Blocks: " + wheatBlocksCreated + ", Enchanted: " + enchantedCreated + ")</gray>");
    }

    private int count(Inventory inv, String type) {
        int total = 0;
        for (ItemStack item : inv.getContents()) {
            if (CustomItemUtil.hasItemType(plugin, item, type)) total += item.getAmount();
        }
        return total;
    }

    private void remove(Inventory inv, String type, int amount) {
        int left = amount;
        for (int i = 0; i < inv.getSize(); i++) {
            ItemStack item = inv.getItem(i);
            if (!CustomItemUtil.hasItemType(plugin, item, type)) continue;
            int take = Math.min(left, item.getAmount());
            item.setAmount(item.getAmount() - take);
            left -= take;
            if (item.getAmount() <= 0) inv.setItem(i, null);
            if (left <= 0) return;
        }
    }

    private void giveOrDrop(Player player, ItemStack item) {
        var leftover = player.getInventory().addItem(item);
        leftover.values().forEach(l -> player.getWorld().dropItemNaturally(player.getLocation(), l));
    }
}