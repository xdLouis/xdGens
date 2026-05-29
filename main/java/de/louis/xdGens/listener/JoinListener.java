package de.louis.xdGens.listener;

import de.louis.xdGens.main.Main;
import de.louis.xdGens.util.HoeUtil;
import de.louis.xdGens.util.MessageUtil;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerQuitEvent;

public class JoinListener implements Listener {

    private final Main plugin;

    public JoinListener(Main plugin) {
        this.plugin = plugin;
    }

    @EventHandler
    public void onJoin(PlayerJoinEvent event) {
        Player player = event.getPlayer();

        if (!player.hasPlayedBefore() && !player.getInventory().containsAtLeast(HoeUtil.createStarterHoe(plugin), 1)) {
            player.getInventory().addItem(HoeUtil.createStarterHoe(plugin));
            MessageUtil.sendRaw(
                    player,
                    MessageUtil.PREFIX
                            + " <gray>Welcome to <gradient:#a18cd1:#fbc2eb>xdGens</gradient>! "
                            + "You received a <gradient:#7afcff:#00c2ff>Level 1 Hoe</gradient>.</gray>"
            );
        }

        if (plugin.getScoreboardManager() != null) {
            plugin.getScoreboardManager().setupPlayer(player);
        }

        if (plugin.getProgressionManager() != null) {
            plugin.getProgressionManager().updateDisplays(player);
        }

        if (plugin.getHoeUpgradeManager() != null) {
            plugin.getHoeUpgradeManager().applyHoeStats(player);
        }

        if (plugin.getGlowManager() != null) {
            plugin.getGlowManager().applyGlow(player);
        }
    }

    @EventHandler
    public void onQuit(PlayerQuitEvent event) {
        Player player = event.getPlayer();

        if (plugin.getScoreboardManager() != null) {
            plugin.getScoreboardManager().removePlayer(player);
        }

        if (plugin.getCurrencyManager() != null) {
            plugin.getCurrencyManager().savePlayer(player);
        }

        if (plugin.getProgressionManager() != null) {
            plugin.getProgressionManager().savePlayer(player);
        }

        if (plugin.getHoeUpgradeManager() != null) {
            plugin.getHoeUpgradeManager().saveAll();
        }

        if (plugin.getBackpackManager() != null) {
            plugin.getBackpackManager().savePlayer(player);
        }

        if (plugin.getGlowManager() != null) {
            plugin.getGlowManager().removeGlow(player);
        }
    }
}
