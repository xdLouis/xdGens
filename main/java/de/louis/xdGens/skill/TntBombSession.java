package de.louis.xdGens.skill;

import de.louis.xdGens.main.Main;
import de.louis.xdGens.util.MessageUtil;
import org.bukkit.*;
import org.bukkit.block.Block;
import org.bukkit.block.data.Ageable;
import org.bukkit.entity.Player;
import org.bukkit.scheduler.BukkitRunnable;

import java.util.*;

/**
 * Drops 10 fake TNT bombs around the player.
 * Each bomb:
 *  - Falls from ~15 blocks above a random position within ±15 blocks of the player
 *  - Has a particle trail while falling
 *  - On impact: explosion particles + sound, NO block damage
 *  - Harvests all fully-grown wheat within EXPLOSION_RADIUS and converts to tokens
 */
public class TntBombSession {

    private static final int    BOMB_COUNT        = 10;
    private static final int    SPAWN_RADIUS      = 15;   // horizontal radius around player
    private static final int    DROP_HEIGHT       = 15;   // blocks above ground
    private static final int    EXPLOSION_RADIUS  = 3;    // crop harvest radius on impact
    private static final int    BOMB_DELAY_TICKS  = 8;    // ticks between each bomb drop
    private static final double BASE_TOKEN_REWARD = 25.0; // tokens per harvested crop block

    public static void trigger(Main plugin, Player player, int tntLevel) {
        for (int i = 0; i < BOMB_COUNT; i++) {
            final int bombIndex = i;
            Bukkit.getScheduler().runTaskLater(plugin, () ->
                    dropBomb(plugin, player, tntLevel), (long) bombIndex * BOMB_DELAY_TICKS);
        }
    }

    private static void dropBomb(Main plugin, Player player, int tntLevel) {
        World world = player.getWorld();
        Location base = player.getLocation();

        // Random position within SPAWN_RADIUS around player
        double offX = (Math.random() - 0.5) * 2 * SPAWN_RADIUS;
        double offZ = (Math.random() - 0.5) * 2 * SPAWN_RADIUS;
        int    bx   = (int)(base.getX() + offX);
        int    bz   = (int)(base.getZ() + offZ);
        int    topY = world.getHighestBlockYAt(bx, bz);

        Location startLoc = new Location(world, bx + 0.5, topY + DROP_HEIGHT, bz + 0.5);
        Location landLoc  = new Location(world, bx + 0.5, topY + 1.0,        bz + 0.5);

        // Animate falling: particle trail every 2 ticks over DROP_HEIGHT steps
        final double[] currentY = {startLoc.getY()};
        final double fallStep   = (double) DROP_HEIGHT / (DROP_HEIGHT * 2); // 0.5 blocks per tick-pair

        new BukkitRunnable() {
            int ticks = 0;
            final int maxTicks = DROP_HEIGHT * 2; // 2 ticks per block

            @Override
            public void run() {
                if (ticks >= maxTicks) {
                    // Impact!
                    explode(plugin, player, landLoc, tntLevel);
                    cancel();
                    return;
                }
                // Trail particles
                Location trailLoc = new Location(world, bx + 0.5, currentY[0], bz + 0.5);
                world.spawnParticle(Particle.SMOKE, trailLoc, 4, 0.1, 0.1, 0.1, 0.01);
                world.spawnParticle(Particle.FLAME, trailLoc, 2, 0.1, 0.1, 0.1, 0.01);
                currentY[0] -= fallStep;
                ticks += 2;
            }
        }.runTaskTimer(plugin, 0L, 2L);
    }

    private static void explode(Main plugin, Player player, Location landLoc, int tntLevel) {
        World world = landLoc.getWorld();
        if (world == null) return;

        // Visual explosion — no block damage
        world.spawnParticle(Particle.EXPLOSION, landLoc, 1, 0, 0, 0, 0);
        world.spawnParticle(Particle.LARGE_SMOKE, landLoc, 20, 0.8, 0.4, 0.8, 0.05);
        world.spawnParticle(Particle.FLAME, landLoc, 30, 0.6, 0.3, 0.6, 0.1);
        world.playSound(landLoc, Sound.ENTITY_GENERIC_EXPLODE, 1.0f, 1.0f);

        // Harvest all fully-grown wheat in EXPLOSION_RADIUS
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
                        // Harvest particles
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

        // Tokens: BASE × harvested blocks × level × prestige multiplier
        double prestigeMult = plugin.getProgressionManager().getPrestigeTokenMultiplier(player);
        long   tokens       = Math.round(BASE_TOKEN_REWARD * harvested * tntLevel * prestigeMult);

        plugin.getCurrencyManager().addTokens(player, (int) tokens);

        // Floating +token text via actionbar (short flash)
        player.sendActionBar(MessageUtil.parse(
                "<gradient:#ff6b6b:#ffd93d>\uD83D\uDCA3 +" + tokens + " Tokens</gradient>"
                + " <dark_gray>(" + harvested + " crops)"));
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
