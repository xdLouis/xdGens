package de.louis.xdGens.listener;

import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.ItemSpawnEvent;
import org.bukkit.event.block.BlockDropItemEvent;

/**
 * Verhindert, dass IRGENDWELCHE Items auf den Boden droppen.
 * Deckt normale Block-Drops und Item-Spawn-Events ab.
 */
public class DropListener implements Listener {

    @EventHandler(priority = EventPriority.HIGHEST, ignoreCancelled = false)
    public void onItemSpawn(ItemSpawnEvent event) {
        event.setCancelled(true);
    }

    @EventHandler(priority = EventPriority.HIGHEST, ignoreCancelled = false)
    public void onBlockDropItem(BlockDropItemEvent event) {
        event.setCancelled(true);
    }
}
