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

/**
 * One panda-roll event for a single player.
 *
 * – Spawns 1.5 blocks above the highest block (no underground-bugfix needed)
 * – Adult size  (no setBaby())
 * – Breaks nearby fully-grown wheat every 2 s; wheat re-grows via scheduleRegrow
 * – Pays a FIXED token + XP reward based on panda level × prestige multiplier
 *   (independent of how much the player has farmed during the session)
 */
public class PandaRollSession {

    private static final Map<UUID, PandaRollSession> ACTIVE = new HashMap<>();

    /**
     * Base flat reward per panda visit at level 1.
     * Scales linearly: reward = BASE × level × prestigeMultiplier
     */
    private static final double BASE_TOKEN_REWARD = 80.0;   // tokens at lv 1 prestige 0
    private static final double BASE_XP_REWARD    = 120.0;  // xp     at lv 1 prestige 0

    /** How often (in ticks) the panda tries to break a wheat block around it. */
    private static final long WHEAT_BREAK_INTERVAL = 40L; // every 2 s
    /** How many wheat blocks it breaks per interval. */
    private static final int  WHEAT_BREAK_COUNT    = 3;
    /** Wheat re-grow delay in ticks (same as field regrow-delay). */
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

    // ── Static helpers ──────────────────────────────────────────────────

    public static boolean isActive(UUID uuid) { return ACTIVE.containsKey(uuid); }

    /** No longer used for reward accumulation — kept for API compatibility. */
    public static void addHarvest(UUID uuid, long tokens, double xp) { /* no-op */ }

    // ── Session lifecycle ───────────────────────────────────────────────

    public void start() {
        if (ACTIVE.containsKey(player.getUniqueId())) return;
        ACTIVE.put(player.getUniqueId(), this);

        // ── Spawn location: random offset + 1.5 blocks above surface ────────
        Location base  = player.getLocation();
        double   offX  = (Math.random() - 0.5) * 8;
        double   offZ  = (Math.random() - 0.5) * 8;
        int      bx    = (int)(base.getX() + offX);
        int      bz    = (int)(base.getZ() + offZ);
        int      topY  = base.getWorld().getHighestBlockYAt(bx, bz);
        Location spawn = new Location(base.getWorld(), bx + 0.5, topY + 1.5, bz + 0.5);

        // ── Spawn adult panda ─────────────────────────────────────────
        panda = (Panda) player.getWorld().spawnEntity(spawn, EntityType.PANDA);
        // Do NOT call setBaby() — default is already adult
        panda.setAI(true);
        panda.setInvulnerable(true);
        panda.setSilent(false);
        panda.setCustomNameVisible(true);
        panda.customName(MessageUtil.parse(
                "<gradient:#a8e6cf:#88d8b0>\uD83D\uDC3C Panda Roller!</gradient>"));

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

                // Crop particles
                spawnCropParticles(panda.getLocation());

                // Wheat breaking
                wheatCooldown[0] -= 4;
                if (wheatCooldown[0] <= 0) {
                    breakNearbyWheat(panda.getLocation());
                    wheatCooldown[0] = (int) WHEAT_BREAK_INTERVAL;
                }

                ticksLeft[0] -= 4;
            }
        }.runTaskTimer(plugin, 0L, 4L);
    }

    // ── Wheat breaking ──────────────────────────────────────────────────

    private void breakNearbyWheat(Location center) {
        World world = center.getWorld();
        if (world == null) return;

        List<Block> candidates = new ArrayList<>();
        for (int dx = -2; dx <= 2; dx++) {
            for (int dz = -2; dz <= 2; dz++) {
                // check at panda feet level and one below
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

        // shuffle so the panda doesn’t always eat the same corner
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

    // ── Particles ────────────────────────────────────────────────────────

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

    // ── Finish / reward ──────────────────────────────────────────────────

    private void finish() {
        ACTIVE.remove(player.getUniqueId());
        if (panda != null && panda.isValid()) panda.remove();
        if (task  != null) { try { task.cancel(); } catch (Exception ignored) {} }

        // Fixed flat reward: BASE × pandaLevel × prestigeMultiplier
        double prestigeMult  = plugin.getProgressionManager().getPrestigeTokenMultiplier(player);
        long   flatTokens    = Math.round(BASE_TOKEN_REWARD * pandaLevel * prestigeMult);
        double flatXp        = BASE_XP_REWARD    * pandaLevel * prestigeMult;

        player.playSound(player.getLocation(), Sound.ENTITY_PANDA_SNEEZE, 1f, 1.0f);
        player.spawnParticle(Particle.HAPPY_VILLAGER,
                player.getLocation().add(0, 1, 0), 30, 0.5, 0.5, 0.5, 0);

        plugin.getCurrencyManager().addTokens(player, (int) flatTokens);
        plugin.getProgressionManager().addXp(player, flatXp);

        MessageUtil.sendRaw(player, MessageUtil.PREFIX
                + " <gradient:#a8e6cf:#88d8b0>\uD83D\uDC3C Panda farmed for you!</gradient>"
                + " <yellow>+" + flatTokens + " Tokens</yellow>"
                + " <gray>\u00b7</gray> <aqua>+" + String.format("%.0f", flatXp) + " XP</aqua>"
                + " <dark_gray>(Lv " + pandaLevel + " \u00b7 Prestige \u00d7"
                + String.format("%.1f", prestigeMult) + ")");
    }
}
