package de.louis.xdGens.skill;

import de.louis.xdGens.main.Main;
import de.louis.xdGens.util.MessageUtil;
import org.bukkit.*;
import org.bukkit.block.Block;
import org.bukkit.block.data.Ageable;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.Panda;
import org.bukkit.entity.Player;
import org.bukkit.scheduler.BukkitRunnable;
import org.bukkit.scheduler.BukkitTask;

import java.util.*;

public class PandaRollSession {

    private static final Map<UUID, PandaRollSession> ACTIVE = new HashMap<>();

    // Fixed reward: BASE × pandaLevel × prestigeMultiplier
    // P1 lv1 = 500 tokens  →  P5 lv100 ≈ 75 000 tokens
    private static final double BASE_TOKEN_REWARD = 500.0;
    private static final double BASE_XP_REWARD    = 750.0;

    private static final long WHEAT_BREAK_INTERVAL = 40L;
    private static final int  WHEAT_BREAK_COUNT    = 3;
    private static final long REGROW_DELAY          = 100L;

    private final Main   plugin;
    private final Player player;
    private final int    pandaLevel;
    private final int    durationTicks;

    private Panda     panda;
    private BukkitTask task;

    public PandaRollSession(Main plugin, Player player, int pandaLevel, int durationTicks) {
        this.plugin        = plugin;
        this.player        = player;
        this.pandaLevel    = pandaLevel;
        this.durationTicks = durationTicks;
    }

    public static boolean isActive(UUID uuid) { return ACTIVE.containsKey(uuid); }
    public static void addHarvest(UUID uuid, long tokens, double xp) { /* no-op */ }

    public void start() {
        if (ACTIVE.containsKey(player.getUniqueId())) return;
        ACTIVE.put(player.getUniqueId(), this);

        Location base  = player.getLocation();
        double   offX  = (Math.random() - 0.5) * 8;
        double   offZ  = (Math.random() - 0.5) * 8;
        int      bx    = (int)(base.getX() + offX);
        int      bz    = (int)(base.getZ() + offZ);
        int      topY  = base.getWorld().getHighestBlockYAt(bx, bz);
        Location spawn = new Location(base.getWorld(), bx + 0.5, topY + 1.5, bz + 0.5);

        panda = (Panda) player.getWorld().spawnEntity(spawn, EntityType.PANDA);
        panda.setAI(true);   // AI ON → panda moves around
        panda.setInvulnerable(true);
        panda.setSilent(false);
        panda.setCustomNameVisible(true);
        panda.customName(MessageUtil.parse(
                "<gradient:#a8e6cf:#88d8b0>\uD83D\uDC3C Panda Roller</gradient>"));

        player.playSound(player.getLocation(), Sound.ENTITY_PANDA_CANT_BREED, 1f, 1.2f);
        MessageUtil.sendRaw(player, MessageUtil.PREFIX
                + " <gradient:#a8e6cf:#88d8b0>\uD83D\uDC3C A Panda appeared on your field!</gradient>");

        final int[] ticksLeft     = {durationTicks};
        final int[] wheatCooldown = {0};

        task = new BukkitRunnable() {
            @Override
            public void run() {
                if (ticksLeft[0] <= 0 || !panda.isValid()) {
                    finish();
                    cancel();
                    return;
                }

                spawnCropParticles(panda.getLocation());

                wheatCooldown[0] -= 4;
                if (wheatCooldown[0] <= 0) {
                    breakNearbyWheat(panda.getLocation());
                    wheatCooldown[0] = (int) WHEAT_BREAK_INTERVAL;
                }

                ticksLeft[0] -= 4;
            }
        }.runTaskTimer(plugin, 0L, 4L);
    }

    private void breakNearbyWheat(Location center) {
        World world = center.getWorld();
        if (world == null) return;

        List<Block> candidates = new ArrayList<>();
        for (int dx = -2; dx <= 2; dx++) {
            for (int dz = -2; dz <= 2; dz++) {
                for (int dy = -1; dy <= 0; dy++) {
                    Block b = world.getBlockAt(
                            center.getBlockX() + dx,
                            center.getBlockY() + dy,
                            center.getBlockZ() + dz);
                    if (b.getType() == Material.WHEAT
                            && b.getBlockData() instanceof Ageable ageable
                            && ageable.getAge() == ageable.getMaximumAge()) {
                        candidates.add(b);
                    }
                }
            }
        }

        Collections.shuffle(candidates);
        int broken = 0;
        for (Block b : candidates) {
            if (broken >= WHEAT_BREAK_COUNT) break;
            Location loc = b.getLocation().add(0.5, 0.5, 0.5);
            world.spawnParticle(Particle.BLOCK, loc, 12, 0.3, 0.2, 0.3, 0,
                    Material.WHEAT.createBlockData());
            world.playSound(loc, Sound.BLOCK_CROP_BREAK, 0.6f, 1.0f);
            b.setType(Material.AIR);
            scheduleRegrow(b);
            broken++;
        }
    }

    private void scheduleRegrow(Block block) {
        long delay = plugin.getConfig().getLong("field.regrow-delay-ticks", REGROW_DELAY);
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

    private void spawnCropParticles(Location center) {
        World world = center.getWorld();
        if (world == null) return;
        for (int dx = -2; dx <= 2; dx++) {
            for (int dz = -2; dz <= 2; dz++) {
                Block b = world.getBlockAt(
                        center.getBlockX() + dx,
                        center.getBlockY() - 1,
                        center.getBlockZ() + dz);
                if (b.getType() == Material.WHEAT) {
                    world.spawnParticle(Particle.BLOCK,
                            b.getLocation().add(0.5, 1.0, 0.5),
                            6, 0.3, 0.2, 0.3, 0,
                            Material.WHEAT.createBlockData());
                    return;
                }
            }
        }
    }

    private void finish() {
        ACTIVE.remove(player.getUniqueId());
        if (panda != null && panda.isValid()) panda.remove();
        if (task  != null) { try { task.cancel(); } catch (Exception ignored) {} }

        double prestigeMult = plugin.getProgressionManager().getPrestigeTokenMultiplier(player);
        long   flatTokens   = Math.round(BASE_TOKEN_REWARD * pandaLevel * prestigeMult);
        double flatXp       = BASE_XP_REWARD * pandaLevel * prestigeMult;

        player.playSound(player.getLocation(), Sound.ENTITY_PANDA_SNEEZE, 1f, 1.0f);
        player.spawnParticle(Particle.HAPPY_VILLAGER,
                player.getLocation().add(0, 1, 0), 30, 0.5, 0.5, 0.5, 0);

        plugin.getCurrencyManager().addTokens(player, (int) flatTokens);
        plugin.getProgressionManager().addXp(player, flatXp);

        MessageUtil.sendRaw(player, MessageUtil.PREFIX
                + " <gradient:#a8e6cf:#88d8b0>\uD83D\uDC3C Panda farmed for you!</gradient>"
                + " <yellow>+" + flatTokens + " Tokens</yellow>"
                + " <gray>\u00b7</gray> <aqua>+" + String.format("%.0f", flatXp) + " XP</aqua>"
                + " <dark_gray>(Lv " + pandaLevel + " \u00b7 \u00d7"
                + String.format("%.1f", prestigeMult) + " Prestige)");
    }
}
