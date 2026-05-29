package de.louis.xdGens.manager;

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
 * Special color keys (cycling):
 *   PRISMATIC → cycles all 8 rainbow colors
 *   INFERNO   → cycles DARK_RED / GOLD / RED
 *   VOID      → cycles BLACK / DARK_GRAY / DARK_PURPLE
 *   AURORA    → cycles AQUA / GREEN / LIGHT_PURPLE / BLUE
 *   DIVINE    → cycles WHITE / YELLOW / GOLD
 */
public class GlowManager {

    private static final ChatColor[] PRISMATIC_COLORS = {
        ChatColor.RED, ChatColor.GOLD, ChatColor.YELLOW,
        ChatColor.GREEN, ChatColor.AQUA, ChatColor.BLUE,
        ChatColor.LIGHT_PURPLE, ChatColor.WHITE
    };
    private static final ChatColor[] INFERNO_COLORS = {
        ChatColor.DARK_RED, ChatColor.GOLD, ChatColor.RED
    };
    private static final ChatColor[] VOID_COLORS = {
        ChatColor.BLACK, ChatColor.DARK_GRAY, ChatColor.DARK_PURPLE
    };
    private static final ChatColor[] AURORA_COLORS = {
        ChatColor.AQUA, ChatColor.GREEN, ChatColor.LIGHT_PURPLE, ChatColor.BLUE
    };
    private static final ChatColor[] DIVINE_COLORS = {
        ChatColor.WHITE, ChatColor.YELLOW, ChatColor.GOLD
    };

    private static final String TEAM_PREFIX = "glow_";

    private final Main plugin;
    private final Map<UUID, BukkitTask> cyclingTasks = new HashMap<>();
    private final Map<UUID, Integer>    cycleIndex   = new HashMap<>();

    public GlowManager(Main plugin) {
        this.plugin = plugin;
    }

    // ── public API ───────────────────────────────────────────────

    public void applyGlow(Player player) {
        removeGlow(player);

        String colorKey = plugin.getPlayerCosmeticManager().getGlowColor(player);
        if (colorKey == null) return;

        player.setGlowing(true);

        switch (colorKey) {
            case "PRISMATIC" -> startCycling(player, PRISMATIC_COLORS);
            case "INFERNO"   -> startCycling(player, INFERNO_COLORS);
            case "VOID"      -> startCycling(player, VOID_COLORS);
            case "AURORA"    -> startCycling(player, AURORA_COLORS);
            case "DIVINE"    -> startCycling(player, DIVINE_COLORS);
            default          -> applyStaticColor(player, colorKey);
        }
    }

    public void removeGlow(Player player) {
        stopCycling(player);
        player.setGlowing(false);
        clearTeam(player);
    }

    public void shutdown() {
        cyclingTasks.values().forEach(BukkitTask::cancel);
        cyclingTasks.clear();
        cycleIndex.clear();
    }

    // ── internals ───────────────────────────────────────────────

    private void applyStaticColor(Player player, String colorKey) {
        ChatColor color = parseChatColor(colorKey);
        if (color == null) {
            // Unknown key – fall back to WHITE so the glow at least shows
            color = ChatColor.WHITE;
        }
        setTeamColor(player, color);
    }

    private void startCycling(Player player, ChatColor[] colors) {
        UUID uuid = player.getUniqueId();
        cycleIndex.put(uuid, 0);

        BukkitTask task = Bukkit.getScheduler().runTaskTimer(plugin, () -> {
            Player p = Bukkit.getPlayer(uuid);
            if (p == null || !p.isOnline()) { stopCycling(uuid); return; }
            int idx = cycleIndex.getOrDefault(uuid, 0);
            setTeamColor(p, colors[idx]);
            cycleIndex.put(uuid, (idx + 1) % colors.length);
        }, 0L, 20L);

        cyclingTasks.put(uuid, task);
    }

    private void stopCycling(Player player) { stopCycling(player.getUniqueId()); }
    private void stopCycling(UUID uuid) {
        BukkitTask t = cyclingTasks.remove(uuid);
        if (t != null) t.cancel();
        cycleIndex.remove(uuid);
    }

    private void setTeamColor(Player player, ChatColor color) {
        Scoreboard board = resolveBoard(player);
        String teamName  = teamName(player);

        Team team = board.getTeam(teamName);
        if (team == null) team = board.registerNewTeam(teamName);

        team.setColor(color);
        if (!team.hasEntry(player.getName())) team.addEntry(player.getName());
    }

    private void clearTeam(Player player) {
        Scoreboard board = resolveBoard(player);
        Team team = board.getTeam(teamName(player));
        if (team != null) {
            team.removeEntry(player.getName());
            if (team.getEntries().isEmpty()) team.unregister();
        }
    }

    private Scoreboard resolveBoard(Player player) {
        ScoreboardManager sbm = plugin.getScoreboardManager();
        if (sbm != null) {
            Scoreboard board = sbm.getBoard(player);
            if (board != null) return board;
        }
        return Bukkit.getScoreboardManager().getMainScoreboard();
    }

    private String teamName(Player player) {
        String uuid = player.getUniqueId().toString().replace("-", "");
        return (TEAM_PREFIX + uuid).substring(0, 16);
    }

    private ChatColor parseChatColor(String key) {
        try { return ChatColor.valueOf(key); }
        catch (IllegalArgumentException e) { return null; }
    }
}
