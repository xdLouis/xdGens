package de.louis.xdGens.skill;

import de.louis.xdGens.main.Main;
import de.louis.xdGens.util.MessageUtil;
import de.louis.xdGens.util.NumberUtil;
import org.bukkit.*;
import org.bukkit.block.Block;
import org.bukkit.block.data.Ageable;
import org.bukkit.entity.Player;
import org.bukkit.scheduler.BukkitRunnable;

import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicLong;

public class TntBombSession {

    private static final int    BOMB_COUNT        = 10;
    private static final int    SPAWN_RADIUS      = 15;
    private static final int    DROP_HEIGHT       = 15;
    private static final int    EXPLOSION_RADIUS  = 3;
    private static final int    BOMB_DELAY_TICKS  = 8;
    private static final double BASE_TOKEN_REWARD = 25.0;

    public static void trigger(Main plugin, Player player, int tntLevel) {
        MessageUtil.sendRaw(player, MessageUtil.PREFIX
                + " <gradient:#ff6b6b:#ffd93d>\uD83D\uDCA3 TNT Bomber activated!</gradient>");

        AtomicInteger totalCrops  = new AtomicInteger(0);
        AtomicLong    totalTokens = new AtomicLong(0L);
        AtomicInteger bombsDone   = new AtomicInteger(0);

        long lastBombLandTick = (long)(BOMB_COUNT - 1) * BOMB_DELAY_TICKS + DROP_HEIGHT * 2 + 5;

        for (int i = 0; i < BOMB_COUNT; i++) {
            final int bombIndex = i;
            Bukkit.getScheduler().runTaskLater(plugin, () ->
                    dropBomb(plugin, player, tntLevel, totalCrops, totalTokens, bombsDone),
                    (long) bombIndex * BOMB_DELAY_TICKS);
        }

        Bukkit.getScheduler().runTaskLater(plugin, () -> {
            long   tokens       = totalTokens.get();
            int    crops        = totalCrops.get();
            double prestigeMult = plugin.getProgressionManager().getPrestigeTokenMultiplier(player);

            // Feed ActionBar so the running counter picks up TNT tokens
            if (tokens > 0) {
                plugin.getActionBarManager().addHarvest(player, tokens, 0.0);
            }

            if (crops == 0) {
                MessageUtil.sendRaw(player, MessageUtil.PREFIX
                        + " <gradient:#ff6b6b:#ffd93d>\uD83D\uDCA3 TNT Bomber finished</gradient>"
                        + " <dark_gray>\u2014 no crops hit.</dark_gray>");
            } else {
                MessageUtil.sendRaw(player, MessageUtil.PREFIX
                        + " <gradient:#ff6b6b:#ffd93d>\uD83D\uDCA3 TNT Bomber finished!</gradient>"
                        + " <yellow>+" + NumberUtil.format(tokens) + " Tokens</yellow>"
                        + " <gray>\u00b7</gray> <dark_gray>(" + crops + " crops \u00b7 Lv " + tntLevel
                        + " \u00b7 Prestige \u00d7" + String.format("%.1f", prestigeMult) + ")</dark_gray>");
            }
        }, lastBombLandTick);
    }

    private static void dropBomb(Main plugin, Player player, int tntLevel,
                                  AtomicInteger totalCrops, AtomicLong totalTokens,
                                  AtomicInteger bombsDone) {
        World world = player.getWorld();
        Location base = player.getLocation();

        double offX = (Math.random() - 0.5) * 2 * SPAWN_RADIUS;
        double offZ = (Math.random() - 0.5) * 2 * SPAWN_RADIUS;
        int    bx   = (int)(base.getX() + offX);
        int    bz   = (int)(base.getZ() + offZ);
        int    topY = world.getHighestBlockYAt(bx, bz);

        Location startLoc = new Location(world, bx + 0.5, topY + DROP_HEIGHT, bz + 0.5);
        Location landLoc  = new Location(world, bx + 0.5, topY + 1.0,        bz + 0.5);

        final double[] currentY = {startLoc.getY()};
        final double fallStep   = (double) DROP_HEIGHT / (DROP_HEIGHT * 2);

        new BukkitRunnable() {
            int ticks = 0;
            final int maxTicks = DROP_HEIGHT * 2;

            @Override
            public void run() {
                if (ticks >= maxTicks) {
                    explode(plugin, player, landLoc, tntLevel, totalCrops, totalTokens);
                    bombsDone.incrementAndGet();
                    cancel();
                    return;
                }
                Location trailLoc = new Location(world, bx + 0.5, currentY[0], bz + 0.5);
                world.spawnParticle(Particle.SMOKE, trailLoc, 6, 0.1, 0.1, 0.1, 0.01);
                world.spawnParticle(Particle.FLAME, trailLoc, 3, 0.1, 0.1, 0.1, 0.02);
                if (ticks % 6 == 0) {
                    world.playSound(trailLoc, Sound.BLOCK_NOTE_BLOCK_BASEDRUM, 0.4f, 1.2f);
                }
                currentY[0] -= fallStep;
                ticks += 2;
            }
        }.runTaskTimer(plugin, 0L, 2L);
    }

    private static void explode(Main plugin, Player player, Location landLoc, int tntLevel,
                                 AtomicInteger totalCrops, AtomicLong totalTokens) {
        World world = landLoc.getWorld();
        if (world == null) return;

        world.spawnParticle(Particle.EXPLOSION, landLoc, 1, 0, 0, 0, 0);
        world.spawnParticle(Particle.LARGE_SMOKE, landLoc, 20, 0.8, 0.4, 0.8, 0.05);
        world.spawnParticle(Particle.FLAME, landLoc, 30, 0.6, 0.3, 0.6, 0.1);
        world.playSound(landLoc, Sound.ENTITY_GENERIC_EXPLODE, 1.0f, 1.0f);

        int harvested = 0;
        for (int dx = -EXPLOSION_RADIUS; dx <= EXPLOSION_RADIUS; dx++) {
            for (int dz = -EXPLOSION_RADIUS; dz <= EXPLOSION_RADIUS; dz++) {
                for (int dy = -1; dy <= 1; dy++) {
                    Block b = world.getBlockAt(
                            landLoc.getBlockX() + dx,
                            landLoc.getBlockY() + dy,
                            landLoc.getBlockZ() + dz);
                    if (b.getType() == Material.WHEAT
                            && b.getBlockData() instanceof Ageable ageable
                            && ageable.getAge() == ageable.getMaximumAge()) {
                        Location loc = b.getLocation().add(0.5, 0.5, 0.5);
                        world.spawnParticle(Particle.BLOCK, loc, 8, 0.3, 0.2, 0.3, 0,
                                Material.WHEAT.createBlockData());
                        b.setType(Material.AIR);
                        scheduleRegrow(plugin, b);
                        harvested++;
                    }
                }
            }
        }

        if (harvested == 0) return;

        double prestigeMult = plugin.getProgressionManager().getPrestigeTokenMultiplier(player);
        long   tokens       = Math.round(BASE_TOKEN_REWARD * harvested * tntLevel * prestigeMult);

        plugin.getCurrencyManager().addTokens(player, (int) tokens);
        totalCrops.addAndGet(harvested);
        totalTokens.addAndGet(tokens);
    }

    private static void scheduleRegrow(Main plugin, Block block) {
        long delay = plugin.getConfig().getLong("field.regrow-delay-ticks", 100L);
        Bukkit.getScheduler().runTaskLater(plugin, () -> {
            if (block.getType() == Material.AIR) {
                block.setType(Material.WHEAT);
                if (block.getBlockData() instanceof Ageable ageable) {
                    ageable.setAge(ageable.getMaximumAge());
                    block.setBlockData(ageable);
                }
                Block farmland = block.getRelative(0, -1, 0);
                if (farmland.getType() != Material.FARMLAND)
                    farmland.setType(Material.FARMLAND);
                de.louis.xdGens.field.FieldManager.moisturizeFarmland(farmland);
            }
        }, delay);
    }
}
