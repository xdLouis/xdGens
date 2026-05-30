package de.louis.xdGens.skill;

import de.louis.xdGens.main.Main;
import de.louis.xdGens.util.MessageUtil;
import de.louis.xdGens.util.NumberUtil;
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
 * – Spawns 1.5 blocks above the highest block
 * – Adult size (no setBaby())
 * – Breaks nearby fully-grown wheat every 1 s in a ±4 block radius
 * – Wanders around the field by giving it a new move target every 3 s
 * – Pays a token reward that scales with panda level, and a FLAT XP reward
 *   so that higher panda levels do not trivialise prestige progression.
 *
 * XP design intent:
 *   BASE_XP_REWARD is intentionally flat (not multiplied by pandaLevel).
 *   At Prestige 3 one session gives ~2.5% of the XP needed for a prestige,
 *   so ~8 panda triggers ≈ 20% of a prestige — a nice bonus, not a skip.
 *   Token rewards still scale linearly with panda level so players feel
 *   the difference between a low-level and high-level panda.
 */
public class PandaRollSession {

    private static final Map<UUID, PandaRollSession> ACTIVE = new HashMap<>();

    /**
     * Token reward scales with panda level: flatTokens = BASE_TOKEN_REWARD * pandaLevel * prestigeMult.
     * A Lv-10 panda at Prestige 3 gives 1500 * 10 * 1.3 = 19 500 tokens.
     */
    private static final double BASE_TOKEN_REWARD = 1500.0;

    /**
     * XP reward is FLAT — independent of panda level.
     * flatXp = BASE_XP_REWARD * prestigeMult  (panda level NOT multiplied in).
     * This prevents high-level pandas from trivialising prestige progression.
     * At Prestige 3: 2000 * 1.3 = 2600 XP per session ≈ 2.5% of a prestige.
     */
    private static final double BASE_XP_REWARD = 2000.0;

    /** How often (in ticks) the panda tries to break wheat. */
    private static final long WHEAT_BREAK_INTERVAL = 20L;
    /** How many wheat blocks it breaks per interval. */
    private static final int  WHEAT_BREAK_COUNT    = 6;
    /** Search radius (blocks) around the panda for wheat. */
    private static final int  WHEAT_RADIUS         = 4;
    /** Wheat re-grow delay in ticks. */
    private static final long REGROW_DELAY         = 100L;
    /** How often (in ticks) the panda picks a new wander destination. */
    private static final int  WANDER_INTERVAL      = 60;

    private final Main   plugin;
    private final Player player;
    private final int    pandaLevel;
    private final int    durationTicks;

    private Panda      panda;
    private BukkitTask task;

    public PandaRollSession(Main plugin, Player player, int pandaLevel, int durationTicks) {
        this.plugin        = plugin;
        this.player        = player;
        this.pandaLevel    = pandaLevel;
        this.durationTicks = durationTicks;
    }

    public static boolean isActive(UUID uuid) { return ACTIVE.containsKey(uuid); }

    public static void addHarvest(UUID uuid, long tokens, double xp) { /* no-op */ }

    // ── Session lifecycle ──────────────────────────────────────────────────────

    public void start() {
        if (ACTIVE.containsKey(player.getUniqueId())) return;
        ACTIVE.put(player.getUniqueId(), this);

        Location base = player.getLocation();
        double   offX = (Math.random() - 0.5) * 8;
        double   offZ = (Math.random() - 0.5) * 8;
        int      bx   = (int)(base.getX() + offX);
        int      bz   = (int)(base.getZ() + offZ);
        int      topY = base.getWorld().getHighestBlockYAt(bx, bz);
        Location spawn = new Location(base.getWorld(), bx + 0.5, topY + 1.5, bz + 0.5);

        panda = (Panda) player.getWorld().spawnEntity(spawn, EntityType.PANDA);
        panda.setAI(true);
        panda.setInvulnerable(true);
        panda.setSilent(false);
        panda.setCustomNameVisible(true);
        panda.customName(MessageUtil.parse(
                "<gradient:#a8e6cf:#88d8b0>\uD83D\uDC3C Panda Roller!</gradient>"));

        player.playSound(player.getLocation(), Sound.ENTITY_PANDA_CANT_BREED, 1f, 1.2f);
        MessageUtil.sendRaw(player, MessageUtil.PREFIX
                + " <gradient:#a8e6cf:#88d8b0>\uD83D\uDC3C A Panda appeared on your field!</gradient>");

        final int[] ticksLeft      = {durationTicks};
        final int[] wheatCooldown  = {0};
        final int[] wanderCooldown = {0};

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

                wanderCooldown[0] -= 4;
                if (wanderCooldown[0] <= 0) {
                    wander(base);
                    wanderCooldown[0] = WANDER_INTERVAL;
                }

                ticksLeft[0] -= 4;
            }
        }.runTaskTimer(plugin, 0L, 4L);
    }

    // ── Wandering ────────────────────────────────────────────────────────────

    private void wander(Location center) {
        if (panda == null || !panda.isValid()) return;
        World world = panda.getWorld();
        if (world == null) return;

        double offX = (Math.random() - 0.5) * 10;
        double offZ = (Math.random() - 0.5) * 10;
        int    tx   = (int)(center.getX() + offX);
        int    tz   = (int)(center.getZ() + offZ);
        int    ty   = world.getHighestBlockYAt(tx, tz);
        panda.getPathfinder().moveTo(new Location(world, tx + 0.5, ty, tz + 0.5), 1.0);
    }

    // ── Wheat breaking ─────────────────────────────────────────────────────────

    private void breakNearbyWheat(Location center) {
        World world = center.getWorld();
        if (world == null) return;

        List<Block> candidates = new ArrayList<>();
        for (int dx = -WHEAT_RADIUS; dx <= WHEAT_RADIUS; dx++) {
            for (int dz = -WHEAT_RADIUS; dz <= WHEAT_RADIUS; dz++) {
                for (int dy = -1; dy <= 1; dy++) {
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

    // ── Particles ─────────────────────────────────────────────────────────────

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

    // ── Finish / reward ────────────────────────────────────────────────────────

    private void finish() {
        ACTIVE.remove(player.getUniqueId());
        if (panda != null && panda.isValid()) panda.remove();
        if (task  != null) { try { task.cancel(); } catch (Exception ignored) {} }

        double prestigeMult = plugin.getProgressionManager().getPrestigeTokenMultiplier(player);

        // Tokens scale with panda level — higher level panda = bigger token haul
        long flatTokens = Math.round(BASE_TOKEN_REWARD * pandaLevel * prestigeMult);

        // XP is flat — independent of panda level to prevent prestige skipping
        double flatXp = BASE_XP_REWARD * prestigeMult;

        plugin.getCurrencyManager().addTokens(player, (int) flatTokens);
        plugin.getProgressionManager().addXp(player, flatXp);

        // Feed the ActionBar accumulator so tokens + XP show up in the running counter
        plugin.getActionBarManager().addHarvest(player, flatTokens, flatXp);

        player.playSound(player.getLocation(), Sound.ENTITY_PANDA_SNEEZE, 1f, 1.0f);
        player.spawnParticle(Particle.HAPPY_VILLAGER,
                player.getLocation().add(0, 1, 0), 30, 0.5, 0.5, 0.5, 0);

        // NumberUtil.format() converts raw numbers to readable suffixes: 19500 → "19.5k"
        MessageUtil.sendRaw(player, MessageUtil.PREFIX
                + " <gradient:#a8e6cf:#88d8b0>\uD83D\uDC3C Panda farmed for you!</gradient>"
                + " <yellow>+" + NumberUtil.format(flatTokens) + " Tokens</yellow>"
                + " <gray>\u00b7</gray> <aqua>+" + NumberUtil.format(flatXp) + " XP</aqua>"
                + " <dark_gray>(Lv " + pandaLevel + " \u00b7 Prestige \u00d7"
                + String.format("%.1f", prestigeMult) + ")");
    }
}
