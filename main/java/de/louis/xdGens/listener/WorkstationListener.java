package de.louis.xdGens.listener;

import de.louis.xdGens.main.Main;
import de.louis.xdGens.manager.WorkstationManager;
import de.louis.xdGens.util.CustomItemUtil;
import de.louis.xdGens.util.MessageUtil;
import org.bukkit.Bukkit;
import org.bukkit.Chunk;
import org.bukkit.Location;
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
import org.bukkit.event.world.ChunkLoadEvent;

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
        MessageUtil.sendRaw(event.getPlayer(), MessageUtil.PREFIX + " <gray>Workstation placed.</gray>");
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
                CustomItemUtil.createWorkstationItem(plugin));
    }

    @EventHandler
    public void onChunkLoad(ChunkLoadEvent event) {
        Chunk chunk = event.getChunk();
        for (Location loc : workstationManager.getWorkstationLocations()) {
            if (!loc.getWorld().equals(chunk.getWorld())) continue;
            if ((loc.getBlockX() >> 4) != chunk.getX()) continue;
            if ((loc.getBlockZ() >> 4) != chunk.getZ()) continue;
            Bukkit.getScheduler().runTaskLater(plugin, () -> plugin.getHologramManager().spawn(loc), 2L);
        }
    }

    // Priority NORMAL + ignoreCancelled=false so vanilla SmithingTable GUI open
    // gets cancelled by us regardless of other event handlers
    @EventHandler(priority = EventPriority.NORMAL)
    public void onInteract(PlayerInteractEvent event) {
        if (event.getClickedBlock() == null) return;
        if (event.getAction() != Action.RIGHT_CLICK_BLOCK) return;

        Block block = event.getClickedBlock();
        if (block.getType() != Material.SMITHING_TABLE) return;
        if (!workstationManager.isWorkstation(block.getLocation())) return;

        event.setCancelled(true);
        workstationManager.useWorkstation(event.getPlayer());
    }
}
