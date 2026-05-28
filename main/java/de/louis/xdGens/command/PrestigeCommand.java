package de.louis.xdGens.command;

import de.louis.xdGens.main.Main;
import de.louis.xdGens.util.MessageUtil;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

public class PrestigeCommand implements CommandExecutor {

    private final Main plugin;

    public PrestigeCommand(Main plugin) {
        this.plugin = plugin;
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (!(sender instanceof Player player)) {
            sender.sendMessage("Only players can use this command.");
            return true;
        }

        if (!plugin.getProgressionManager().canPrestige(player)) {
            MessageUtil.send(player,
                    MessageUtil.PREFIX + " <red>You must reach the maximum level before prestiging.</red>");
            return true;
        }

        plugin.getProgressionManager().prestige(player);
        return true;
    }
}