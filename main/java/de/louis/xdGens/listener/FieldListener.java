package de.louis.xdGens.listener;

import de.louis.xdGens.field.FieldManager;
import de.louis.xdGens.main.Main;
import de.louis.xdGens.manager.CurrencyManager;
import de.louis.xdGens.manager.HoeUpgradeManager;
import de.louis.xdGens.skill.PandaRollSession;
import de.louis.xdGens.skill.ShadowCloneSession;
import de.louis.xdGens.skill.TntBombSession;
import de.louis.xdGens.util.CustomItemUtil;
import de.louis.xdGens.util.HoeUtil;
import de.louis.xdGens.util.MessageUtil;
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
    private final Main            plugin;
    private final FieldManager    fieldManager;
    private final CurrencyManager currency;

    public FieldListener(Main plugin) {
        this.plugin       = plugin;
        this.fieldManager = plugin.getFieldManager();
        this.currency     = plugin.getCurrencyManager();
    }

    @EventHandler(priority = EventPriority.HIGHEST, ignoreCancelled = true)
    public void onBlockBreak(BlockBreakEvent event) {
        Player player = event.getPlayer();
        Block  block  = event.getBlock();

        if (player.getGameMode() == GameMode.CREATIVE || block.getType() != Material.WHEAT) return;

        ItemStack tool = player.getInventory().getItemInMainHand();
        if (!HoeUtil.isXdHoe(tool)) {
            event.setCancelled(true);
            return;
        }

        if (!(block.getBlockData() instanceof Ageable ageable) || ageable.getAge() < ageable.getMaximumAge()) {
            event.setCancelled(true);
            return;
        }

        event.setDropItems(false);
        event.setExpToDrop(0);

        int    tokenMin = plugin.getConfig().getInt("rewards.wheat.tokens.min", 3);
        int    tokenMax = plugin.getConfig().getInt("rewards.wheat.tokens.max", 10);
        double xpMin    = plugin.getConfig().getDouble("rewards.wheat.xp.min", 8.0);
        double xpMax    = plugin.getConfig().getDouble("rewards.wheat.xp.max", 16.0);

        int    baseTokens         = Rng.between(tokenMin, tokenMax);
        double hoeMultiplier      = plugin.getHoeUpgradeManager().getTokenMultiplier(player);
        double prestigeMultiplier = plugin.getProgressionManager().getPrestigeTokenMultiplier(player);

        // ── Shadow Clone ×4 multiplier ─────────────────────────────────────────
        double shadowMult = ShadowCloneSession.isActive(player.getUniqueId()) ? 4.0 : 1.0;

        long   finalTokens = Math.round(baseTokens * hoeMultiplier * prestigeMultiplier * shadowMult);
        double finalXp     = Rng.between(xpMin, xpMax)
                           * plugin.getHoeUpgradeManager().getXpMultiplier(player)
                           * shadowMult;

        int totalCrops = 1 + plugin.getHoeUpgradeManager().getCropBonus(player);
        int stored     = plugin.getBackpackManager().addWheat(player, totalCrops);
        int remaining  = totalCrops - stored;
        if (stored > 0) plugin.getBackpackManager().savePlayer(player);
        if (remaining > 0) {
            ItemStack cropReward = CustomItemUtil.createFarmWheat(plugin, remaining);
            var leftovers = player.getInventory().addItem(cropReward);
            leftovers.values().forEach(left -> player.getWorld().dropItemNaturally(player.getLocation(), left));
        }

        currency.addTokens(player, Math.toIntExact(finalTokens));
        plugin.getProgressionManager().addXp(player, finalXp);
        plugin.getActionBarManager().addHarvest(player, Math.toIntExact(finalTokens), finalXp);

        // ── Panda Roller ────────────────────────────────────────────────────────
        HoeUpgradeManager hoe = plugin.getHoeUpgradeManager();
        if (!PandaRollSession.isActive(player.getUniqueId()) && hoe.getPandaLevel(player) >= 1) {
            if (Math.random() < hoe.getPandaSpawnChance(player)) {
                new PandaRollSession(
                        plugin, player,
                        hoe.getPandaLevel(player),
                        140
                ).start();
            }
        }

        // ── TNT Bomber ─────────────────────────────────────────────────────────
        int tntLvl = hoe.getTntLevel(player);
        plugin.getLogger().info("[TNT-DEBUG] " + player.getName()
                + " tntLevel=" + tntLvl
                + " spawnChance=" + (tntLvl >= 1
                    ? String.format("%.5f", hoe.getTntSpawnChance(player)) : "n/a"));
        if (tntLvl >= 1) {
            double roll   = Math.random();
            double chance = hoe.getTntSpawnChance(player);
            plugin.getLogger().info("[TNT-DEBUG] roll=" + String.format("%.5f", roll)
                    + " chance=" + String.format("%.5f", chance)
                    + " trigger=" + (roll < chance));
            if (roll < chance) {
                TntBombSession.trigger(plugin, player, tntLvl);
            }
        }

        // ── Key Finder ─────────────────────────────────────────────────────────
        if (plugin.getHoeUpgradeManager().getKeyFinderLevel(player) > 0) {
            boolean found = plugin.getCrateManager().tryGiveRandomKey(player);
            if (found) {
                MessageUtil.sendRaw(player, MessageUtil.PREFIX
                        + " <gradient:#a18cd1:#fbc2eb>\uD83D\uDD11 Key found! Open it in <white>/crates</white>.</gradient>");
            }
        }

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
        if (event.getBlock().getType() == Material.FARMLAND) event.setCancelled(true);
    }

    @EventHandler(ignoreCancelled = true)
    public void onWaterFlow(BlockFromToEvent event) {
        if (event.getToBlock().getType() == Material.FARMLAND) event.setCancelled(true);
    }

    @EventHandler(ignoreCancelled = true)
    public void onEntityChangeBlock(EntityChangeBlockEvent event) {
        if (event.getBlock().getType() == Material.FARMLAND) event.setCancelled(true);
    }

    @EventHandler(ignoreCancelled = true)
    public void onPhysicalInteract(PlayerInteractEvent event) {
        if (event.getAction() != Action.PHYSICAL || event.getClickedBlock() == null) return;
        if (event.getClickedBlock().getType() == Material.FARMLAND) event.setCancelled(true);
    }

    private void scheduleRegrow(Block block) {
        long delay = plugin.getConfig().getLong("field.regrow-delay-ticks", 100L);
        Bukkit.getScheduler().runTaskLater(plugin, () -> {
            if (block.getType() == Material.AIR || block.getType() == Material.WHEAT) {
                block.setType(Material.WHEAT);
                FieldManager.setWheatFullyGrown(block);
                Block farmland = block.getRelative(0, -1, 0);
                if (farmland.getType() != Material.FARMLAND) farmland.setType(Material.FARMLAND);
                FieldManager.moisturizeFarmland(farmland);
            }
        }, delay);
    }
}
