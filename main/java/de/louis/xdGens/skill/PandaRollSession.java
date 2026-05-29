package de.louis.xdGens.skill;

import de.louis.xdGens.main.Main;
import de.louis.xdGens.util.MessageUtil;
import org.bukkit.*;
import org.bukkit.block.Block;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.Panda;
import org.bukkit.entity.Player;
import org.bukkit.scheduler.BukkitRunnable;
import org.bukkit.scheduler.BukkitTask;

import java.util.*;

/**
 * One panda-roll event for a single player.
 *
 * The panda spawns on the field and wanders freely (AI on, no follow).
 * It is invisible to other players and follows its own random path.
 * After {@code durationTicks} it despawns and grants bonus rewards.
 *
 * Accumulated rewards come from FieldListener via {@link #addHarvest}.
 */
public class PandaRollSession {

    private static final Map<UUID, PandaRollSession> ACTIVE = new HashMap<>();

    private final Main   plugin;
    private final Player player;
    private final double bonusMultiplier;
    private final int    durationTicks;

    private long   accTokens = 0;
    private double accXp     = 0.0;

    private Panda     panda;
    private BukkitTask task;

    public PandaRollSession(Main plugin, Player player, double bonusMultiplier, int durationTicks) {
        this.plugin          = plugin;
        this.player          = player;
        this.bonusMultiplier = bonusMultiplier;
        this.durationTicks   = durationTicks;
    }

    // ── Static helpers ──────────────────────────────────────────────────────

    public static boolean isActive(UUID uuid)  { return ACTIVE.containsKey(uuid); }

    /** Called by FieldListener on every harvest while a session is active. */
    public static void addHarvest(UUID uuid, long tokens, double xp) {
        PandaRollSession s = ACTIVE.get(uuid);
        if (s != null) s.accumulate(tokens, xp);
    }

    // ── Session lifecycle ───────────────────────────────────────────────────

    public void start() {
        if (ACTIVE.containsKey(player.getUniqueId())) return;
        ACTIVE.put(player.getUniqueId(), this);

        // Spawn on the field near the player, NOT on the player
        Location base   = player.getLocation();
        double   offX   = (Math.random() - 0.5) * 8;
        double   offZ   = (Math.random() - 0.5) * 8;
        Location spawn  = new Location(
                base.getWorld(),
                base.getX() + offX,
                base.getWorld().getHighestBlockYAt(
                        (int)(base.getX() + offX),
                        (int)(base.getZ() + offZ)) + 1,
                base.getZ() + offZ);

        panda = (Panda) player.getWorld().spawnEntity(spawn, EntityType.PANDA);
        panda.setBaby();
        // AI ON so it wanders freely — it does NOT follow the player
        panda.setAI(true);
        panda.setInvulnerable(true);
        panda.setSilent(false);
        panda.setCustomNameVisible(true);
        panda.customName(MessageUtil.parse(
                "<gradient:#a8e6cf:#88d8b0>\uD83D\uDC3C Panda Roller!</gradient>"));

        player.playSound(player.getLocation(), Sound.ENTITY_PANDA_CANT_BREED, 1f, 1.2f);
        MessageUtil.sendRaw(player, MessageUtil.PREFIX
                + " <gradient:#a8e6cf:#88d8b0>\uD83D\uDC3C A Panda appeared on your field!</gradient>");

        final int[] ticksLeft = {durationTicks};

        task = new BukkitRunnable() {
            @Override
            public void run() {
                if (ticksLeft[0] <= 0 || !panda.isValid()) {
                    finish();
                    cancel();
                    return;
                }
                // Crop particles near the panda's current position
                spawnCropParticles(panda.getLocation());
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
        if (task != null) { try { task.cancel(); } catch (Exception ignored) {} }

        long   bonusTokens = Math.round(accTokens * bonusMultiplier);
        double bonusXp     = accXp * bonusMultiplier;

        player.playSound(player.getLocation(), Sound.ENTITY_PANDA_SNEEZE, 1f, 1.0f);
        player.spawnParticle(Particle.HAPPY_VILLAGER,
                player.getLocation().add(0, 1, 0), 30, 0.5, 0.5, 0.5, 0);

        if (bonusTokens > 0 || bonusXp > 0.1) {
            plugin.getCurrencyManager().addTokens(player, (int) bonusTokens);
            plugin.getProgressionManager().addXp(player, bonusXp);
            int bonusPct = (int) Math.round(bonusMultiplier * 100);
            MessageUtil.sendRaw(player, MessageUtil.PREFIX
                    + " <gradient:#a8e6cf:#88d8b0>\uD83D\uDC3C Panda Bonus <white>(+" + bonusPct
                    + "%)</white>:</gradient>"
                    + " <yellow>+" + bonusTokens + " Tokens</yellow>"
                    + " <gray>&</gray> <aqua>+"
                    + String.format("%.0f", bonusXp) + " XP</aqua>");
        } else {
            MessageUtil.sendRaw(player, MessageUtil.PREFIX
                    + " <gray>The panda left — harvest more next time for a bigger bonus!");
        }
    }
}
