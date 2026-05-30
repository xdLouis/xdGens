package de.louis.xdGens.manager;

import de.louis.xdGens.main.Main;
import de.louis.xdGens.util.MessageUtil;
import de.louis.xdGens.util.NumberUtil;
import net.kyori.adventure.text.Component;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.scheduler.BukkitTask;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

public class ActionBarManager {

    private static final int DISPLAY_TICKS = 60;

    private final Main plugin;
    private final Map<UUID, PendingBar> pending = new HashMap<>();
    private BukkitTask task;

    public ActionBarManager(Main plugin) {
        this.plugin = plugin;
        startTask();
    }

    public void addHarvest(Player player, long tokens, double xp) {
        UUID uuid = player.getUniqueId();
        PendingBar bar = pending.getOrDefault(uuid, new PendingBar());

        bar.tokens += tokens;
        bar.xp += xp;
        bar.ticksLeft = DISPLAY_TICKS;

        pending.put(uuid, bar);
    }

    private void startTask() {
        task = Bukkit.getScheduler().runTaskTimer(plugin, () ->
                pending.entrySet().removeIf(entry -> {
                    UUID uuid = entry.getKey();
                    PendingBar bar = entry.getValue();

                    Player player = Bukkit.getPlayer(uuid);
                    if (player == null || !player.isOnline()) {
                        return true;
                    }

                    bar.ticksLeft -= 2;
                    player.sendActionBar(buildBar(bar));

                    return bar.ticksLeft <= 0;
                }), 0L, 2L);
    }

    private Component buildBar(PendingBar bar) {
        String text =
                "<gray>⟡ </gray><gradient:#7afcff:#00c2ff><bold>"
                        + NumberUtil.format(bar.tokens)
                        + "</bold></gradient><gray> Tokens</gray> <dark_gray>|</dark_gray> "
                        + "<gradient:#f6d365:#fda085><bold>"
                        + NumberUtil.format(bar.xp)
                        + "</bold></gradient><gray> XP</gray>";

        return MessageUtil.parse(text);
    }

    public void stop() {
        if (task != null && !task.isCancelled()) {
            task.cancel();
        }
        pending.clear();
    }

    private static class PendingBar {
        long tokens = 0L;
        double xp = 0.0;
        int ticksLeft = 0;
    }
}