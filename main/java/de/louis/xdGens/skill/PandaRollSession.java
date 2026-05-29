package de.louis.xdGens.skill;

import de.louis.xdGens.main.Main;
import de.louis.xdGens.util.MessageUtil;
import org.bukkit.*;
import org.bukkit.block.Block;
import org.bukkit.block.data.Ageable;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Panda;
import org.bukkit.entity.Player;
import org.bukkit.scheduler.BukkitRunnable;
import org.bukkit.scheduler.BukkitTask;

import java.util.*;

/**
 * Handles one panda roll event for a player.
 *
 * Lifecycle:
 *   1. Spawn a baby panda near the player's field.
 *   2. Every 4 ticks: move it slightly, play break particles on a nearby crop,
 *      accumulate XP + Tokens the player earns during the window.
 *   3. After {@code durationTicks}: despawn panda, grant bonus rewards, send message.
 *
 * The accumulated rewards are read from FieldListener via
 * {@link #addHarvest(long tokens, double xp)} — FieldListener calls this
 * whenever a harvest happens and a roll session is active for that player.
 */
public class PandaRollSession {

    private final Main   plugin;
    private final Player player;
    private final double bonusMultiplier;   // e.g. 0.40 = +40% of accumulated XP+tokens
    private final int    durationTicks;

    private long   accTokens = 0;
    private double accXp     = 0.0;

    private Panda    panda;
    private BukkitTask task;

    /** All active sessions, keyed by player UUID. */
    private static final Map<UUID, PandaRollSession> ACTIVE = new HashMap<>();

    public PandaRollSession(Main plugin, Player player, double bonusMultiplier, int durationTicks) {
        this.plugin           = plugin;
        this.player           = player;
        this.bonusMultiplier  = bonusMultiplier;
        this.durationTicks    = durationTicks;
    }

    // ── Static helpers ─────────────────────────────────────────────

    public static boolean isActive(UUID uuid) {
        return ACTIVE.containsKey(uuid);
    }

    /** Called by FieldListener on every harvest while session is active. */
    public static void addHarvest(UUID uuid, long tokens, double xp) {
        PandaRollSession s = ACTIVE.get(uuid);
        if (s != null) s.accumulate(tokens, xp);
    }

    // ── Session lifecycle ──────────────────────────────────────────

    public void start() {
        if (ACTIVE.containsKey(player.getUniqueId())) return;  // already rolling
        ACTIVE.put(player.getUniqueId(), this);

        Location spawnLoc = player.getLocation().add(
                (Math.random() - 0.5) * 4, 0, (Math.random() - 0.5) * 4);
        spawnLoc.setY(player.getWorld().getHighestBlockYAt(spawnLoc) + 1);

        panda = (Panda) player.getWorld().spawnEntity(spawnLoc, EntityType.PANDA);
        panda.setBaby();
        panda.setAI(false);
        panda.setInvulnerable(true);
        panda.setSilent(false);
        panda.setCustomNameVisible(true);
        panda.customName(MessageUtil.parse("<gradient:#a8e6cf:#88d8b0>\uD83D\uDC3C Panda Roller!</gradient>"));

        player.playSound(player.getLocation(), Sound.ENTITY_PANDA_CANT_BREED, 1f, 1.2f);
        MessageUtil.sendRaw(player, MessageUtil.PREFIX
                + " <gradient:#a8e6cf:#88d8b0>\uD83D\uDC3C A Panda appeared and is rolling over your field!</gradient>");

        final int[] ticksLeft = {durationTicks};
        final double[] angle  = {0};

        task = new BukkitRunnable() {
            @Override
            public void run() {
                if (ticksLeft[0] <= 0 || !panda.isValid()) {
                    finish();
                    cancel();
                    return;
                }

                // Move panda in a circle around the player
                angle[0] += 0.3;
                double radius = 2.5;
                double nx = player.getLocation().getX() + Math.cos(angle[0]) * radius;
                double nz = player.getLocation().getZ() + Math.sin(angle[0]) * radius;
                Location pandaLoc = new Location(player.getWorld(), nx,
                        player.getWorld().getHighestBlockYAt((int) nx, (int) nz) + 0.1, nz);
                panda.teleport(pandaLoc);

                // Break particles on a nearby crop
                spawnCropParticles(pandaLoc);

                ticksLeft[0] -= 4;
            }
        }.runTaskTimer(plugin, 0L, 4L);
    }

    private void accumulate(long tokens, double xp) {
        accTokens += tokens;
        accXp     += xp;
    }

    private void spawnCropParticles(Location center) {
        World world = center.getWorld();
        if (world == null) return;
        // Find a wheat block nearby and show break particles
        for (int dx = -2; dx <= 2; dx++) {
            for (int dz = -2; dz <= 2; dz++) {
                Block b = world.getBlockAt(
                        center.getBlockX() + dx,
                        center.getBlockY() - 1,
                        center.getBlockZ() + dz);
                if (b.getType() == Material.WHEAT) {
                    world.spawnParticle(Particle.BLOCK, b.getLocation().add(0.5, 1, 0.5),
                            6, 0.3, 0.2, 0.3, 0,
                            Material.WHEAT.createBlockData());
                    return;  // one block per tick is enough
                }
            }
        }
    }

    private void finish() {
        ACTIVE.remove(player.getUniqueId());
        if (panda != null && panda.isValid()) panda.remove();

        long   bonusTokens = Math.round(accTokens * bonusMultiplier);
        double bonusXp     = accXp * bonusMultiplier;

        player.playSound(player.getLocation(), Sound.ENTITY_PANDA_SNEEZE, 1f, 1.0f);
        player.spawnParticle(Particle.HAPPY_VILLAGER, player.getLocation().add(0, 1, 0),
                30, 0.5, 0.5, 0.5, 0);

        if (bonusTokens > 0 || bonusXp > 0.1) {
            plugin.getCurrencyManager().addTokens(player, (int) bonusTokens);
            plugin.getProgressionManager().addXp(player, bonusXp);

            int bonusPct = (int) Math.round(bonusMultiplier * 100);
            MessageUtil.sendRaw(player, MessageUtil.PREFIX
                    + " <gradient:#a8e6cf:#88d8b0>\uD83D\uDC3C Panda Bonus <white>(+" + bonusPct + "%)</white>:</gradient>"
                    + " <yellow>+" + bonusTokens + " Tokens</yellow>"
                    + " <gray>&</gray> <aqua>+" + String.format("%.0f", bonusXp) + " XP</aqua>");
        } else {
            MessageUtil.sendRaw(player, MessageUtil.PREFIX
                    + " <gray>The panda left — harvest more next time for a bigger bonus!");
        }
    }
}
