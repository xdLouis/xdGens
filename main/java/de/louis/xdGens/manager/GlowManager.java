package de.louis.xdGens.manager;

import de.louis.xdGens.crate.CrateReward;
import de.louis.xdGens.main.Main;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.entity.Player;
import org.bukkit.scheduler.BukkitTask;
import org.bukkit.scoreboard.Scoreboard;
import org.bukkit.scoreboard.Team;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

/**
 * Applies entity-glow to players using scoreboard teams.
 *
 * Normal glows  → solid ChatColor via team color.
 * PRISMATIC glow → cycles through all colors every 20 ticks (1 second).
 * INFERNO glow   → cycles between DARK_RED and GOLD.
 * VOID glow      → cycles between BLACK and DARK_GRAY.
 *
 * Call applyGlow(player)  after equip / on join.
 * Call removeGlow(player) after unequip.
 */
public class GlowManager {

    private static final ChatColor[] PRISMATIC_COLORS = {
        ChatColor.RED, ChatColor.GOLD, ChatColor.YELLOW,
        ChatColor.GREEN, ChatColor.AQUA, ChatColor.BLUE,
        ChatColor.LIGHT_PURPLE, ChatColor.WHITE
    };
    private static final ChatColor[] INFERNO_COLORS = { ChatColor.DARK_RED, ChatColor.GOLD, ChatColor.RED };
    private static final ChatColor[] VOID_COLORS    = { ChatColor.BLACK, ChatColor.DARK_GRAY, ChatColor.DARK_PURPLE };

    private final Main plugin;
    private final Scoreboard scoreboard;

    // players with an active cycling task
    private final Map<UUID, BukkitTask> cyclingTasks  = new HashMap<>();
    private final Map<UUID, Integer>    cycleIndex     = new HashMap<>();

    public GlowManager(Main plugin) {
        this.plugin = plugin;
        // use main scoreboard so glow is visible to everyone
        this.scoreboard = Bukkit.getScoreboardManager().getMainScoreboard();
    }

    // ── public API ────────────────────────────────────────────────────

    /** Apply the player's currently equipped glow (call on equip + on join). */
    public void applyGlow(Player player) {
        removeGlow(player); // clean up old state first

        String colorKey = plugin.getPlayerCosmeticManager().getGlowColor(player);
        if (colorKey == null) return; // nothing equipped

        player.setGlowing(true);

        switch (colorKey) {
            case "PRISMATIC" -> startCycling(player, PRISMATIC_COLORS);
            case "INFERNO"   -> startCycling(player, INFERNO_COLORS);
            case "VOID"      -> startCycling(player, VOID_COLORS);
            default          -> applyStaticColor(player, colorKey);
        }
    }

    /** Remove glow entirely (call on unequip + on quit). */
    public void removeGlow(Player player) {
        stopCycling(player);
        player.setGlowing(false);
        clearTeam(player);
    }

    /** Refresh all online players' glows (e.g. after a reload). */
    public void refreshAll() {
        for (Player p : Bukkit.getOnlinePlayers()) applyGlow(p);
    }

    /** Stop all cycling tasks (call on plugin disable). */
    public void shutdown() {
        cyclingTasks.values().forEach(BukkitTask::cancel);
        cyclingTasks.clear();
        cycleIndex.clear();
    }

    // ── internals ─────────────────────────────────────────────────────

    private void applyStaticColor(Player player, String colorKey) {
        ChatColor color = parseChatColor(colorKey);
        if (color == null) return;
        setTeamColor(player, color);
    }

    private void startCycling(Player player, ChatColor[] colors) {
        UUID uuid = player.getUniqueId();
        cycleIndex.put(uuid, 0);

        BukkitTask task = Bukkit.getScheduler().runTaskTimer(plugin, () -> {
            Player p = Bukkit.getPlayer(uuid);
            if (p == null || !p.isOnline()) {
                stopCycling(uuid);
                return;
            }
            int idx = cycleIndex.getOrDefault(uuid, 0);
            setTeamColor(p, colors[idx]);
            cycleIndex.put(uuid, (idx + 1) % colors.length);
        }, 0L, 20L); // cycle every 1 second

        cyclingTasks.put(uuid, task);
    }

    private void stopCycling(Player player) { stopCycling(player.getUniqueId()); }
    private void stopCycling(UUID uuid) {
        BukkitTask task = cyclingTasks.remove(uuid);
        if (task != null) task.cancel();
        cycleIndex.remove(uuid);
    }

    private void setTeamColor(Player player, ChatColor color) {
        String teamName = "glow_" + player.getUniqueId().toString().replace("-", "").substring(0, 14);

        Team team = scoreboard.getTeam(teamName);
        if (team == null) team = scoreboard.registerNewTeam(teamName);

        team.setColor(color);
        team.addEntry(player.getName());
    }

    private void clearTeam(Player player) {
        String teamName = "glow_" + player.getUniqueId().toString().replace("-", "").substring(0, 14);
        Team team = scoreboard.getTeam(teamName);
        if (team != null) {
            team.removeEntry(player.getName());
            if (team.getEntries().isEmpty()) team.unregister();
        }
    }

    private ChatColor parseChatColor(String key) {
        try { return ChatColor.valueOf(key); }
        catch (IllegalArgumentException e) { return null; }
    }
}
