package de.louis.xdGens.command;

import de.louis.xdGens.gui.CratesGUI;
import de.louis.xdGens.main.Main;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

public class CratesCommand implements CommandExecutor {

    private final Main plugin;

    public CratesCommand(Main plugin) {
        this.plugin = plugin;
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (!(sender instanceof Player player)) return true;
        new CratesGUI(plugin).open(player);
        return true;
    }
}
