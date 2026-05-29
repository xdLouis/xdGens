package de.louis.xdGens.command;

import de.louis.xdGens.gui.CosmeticsGUI;
import de.louis.xdGens.main.Main;
import de.louis.xdGens.util.MessageUtil;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabCompleter;
import org.bukkit.entity.Player;

import java.util.List;

public class CosmeticsCommand implements CommandExecutor, TabCompleter {

    private final Main plugin;

    public CosmeticsCommand(Main plugin) {
        this.plugin = plugin;
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (!(sender instanceof Player player)) {
            MessageUtil.sendRaw(sender, MessageUtil.PREFIX + " <red>Only players can use this command.</red>");
            return true;
        }

        CosmeticsGUI gui = new CosmeticsGUI(plugin);

        if (args.length > 0 && args[0].equalsIgnoreCase("colors")) {
            gui.openColors(player);
        } else {
            gui.openTags(player);
        }
        return true;
    }

    @Override
    public List<String> onTabComplete(CommandSender sender, Command command, String alias, String[] args) {
        if (args.length == 1) {
            return List.of("tags", "colors").stream()
                    .filter(s -> s.startsWith(args[0].toLowerCase()))
                    .toList();
        }
        return List.of();
    }
}
