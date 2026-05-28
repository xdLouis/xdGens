package de.louis.xdGens.listener;

import de.louis.xdGens.field.FieldManager;
import de.louis.xdGens.main.Main;
import de.louis.xdGens.manager.CurrencyManager;
import de.louis.xdGens.util.CustomItemUtil;
import de.louis.xdGens.util.HoeUtil;
import de.louis.xdGens.util.Rng;
import org.bukkit.Bukkit;
import org.bukkit.GameMode;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.block.data.Ageable;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.block.Action;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.event.block.BlockFadeEvent;
import org.bukkit.event.block.BlockFromToEvent;
import org.bukkit.event.block.MoistureChangeEvent;
import org.bukkit.event.entity.EntityChangeBlockEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.inventory.ItemStack;

public class FieldListener implements Listener {

    private final Main plugin;
    private final FieldManager fieldManager;
    private final CurrencyManager currency;

    public FieldListener(Main plugin) {
        this.plugin = plugin;
        this.fieldManager = plugin.getFieldManager();
        this.currency = plugin.getCurrencyManager();
    }

    @EventHandler(priority = EventPriority.HIGHEST, ignoreCancelled = true)
    public void onBlockBreak(BlockBreakEvent event) {
        Player player = event.getPlayer();
        Block block = event.getBlock();

        if (player.getGameMode() == GameMode.CREATIVE) {
            return;
        }

        if (block.getType() != Material.WHEAT) {
            return;
        }

        ItemStack tool = player.getInventory().getItemInMainHand();

        if (!HoeUtil.isXdHoe(tool)) {
            event.setCancelled(true);
            return;
        }

        if (!(block.getBlockData() instanceof Ageable ageable)) {
            event.setCancelled(true);
            return;
        }

        if (ageable.getAge() < ageable.getMaximumAge()) {
            event.setCancelled(true);
            return;
        }

        event.setDropItems(false);
        event.setExpToDrop(0);

        int tokenMin = plugin.getConfig().getInt("rewards.wheat.tokens.min", 1);
        int tokenMax = plugin.getConfig().getInt("rewards.wheat.tokens.max", 5);

        double xpMin = plugin.getConfig().getDouble("rewards.wheat.xp.min", 8.0);
        double xpMax = plugin.getConfig().getDouble("rewards.wheat.xp.max", 16.0);

        int tokens = Rng.between(tokenMin, tokenMax);
        double xp = Rng.between(xpMin, xpMax);

        player.getInventory().addItem(CustomItemUtil.createFarmWheat(plugin, 1));
        int cropBonus = plugin.getHoeUpgradeManager().getCropBonus(player);
        if (cropBonus > 0) {
            player.getInventory().addItem(CustomItemUtil.createFarmWheat(plugin, cropBonus));
        }
        currency.addTokens(player, tokens);
        plugin.getProgressionManager().addXp(player, xp);
        plugin.getActionBarManager().addHarvest(player, tokens, xp);

        scheduleRegrow(block);
    }

    @EventHandler(ignoreCancelled = true)
    public void onMoistureChange(MoistureChangeEvent event) {
        if (event.getBlock().getType() == Material.FARMLAND) {
            event.setCancelled(true);
            Bukkit.getScheduler().runTask(plugin, () -> FieldManager.moisturizeFarmland(event.getBlock()));
        }
    }

    @EventHandler(ignoreCancelled = true)
    public void onBlockFade(BlockFadeEvent event) {
        if (event.getBlock().getType() == Material.FARMLAND) {
            event.setCancelled(true);
        }
    }

    @EventHandler(ignoreCancelled = true)
    public void onWaterFlow(BlockFromToEvent event) {
        if (event.getToBlock().getType() == Material.FARMLAND) {
            event.setCancelled(true);
        }
    }

    @EventHandler(ignoreCancelled = true)
    public void onEntityChangeBlock(EntityChangeBlockEvent event) {
        if (event.getBlock().getType() == Material.FARMLAND) {
            event.setCancelled(true);
        }
    }

    @EventHandler(ignoreCancelled = true)
    public void onPhysicalInteract(PlayerInteractEvent event) {
        if (event.getAction() != Action.PHYSICAL || event.getClickedBlock() == null) {
            return;
        }

        if (event.getClickedBlock().getType() == Material.FARMLAND) {
            event.setCancelled(true);
        }
    }

    private void scheduleRegrow(Block block) {
        long delay = plugin.getConfig().getLong("field.regrow-delay-ticks", 100L);

        Bukkit.getScheduler().runTaskLater(plugin, () -> {
            if (block.getType() == Material.AIR || block.getType() == Material.WHEAT) {
                block.setType(Material.WHEAT);
                FieldManager.setWheatFullyGrown(block);

                Block farmland = block.getRelative(0, -1, 0);
                if (farmland.getType() != Material.FARMLAND) {
                    farmland.setType(Material.FARMLAND);
                }

                FieldManager.moisturizeFarmland(farmland);
            }
        }, delay);
    }
}