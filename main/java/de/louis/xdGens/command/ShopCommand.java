package de.louis.xdGens.command;

import de.louis.xdGens.gui.ShopGUI;
import de.louis.xdGens.main.Main;
import de.louis.xdGens.util.MessageUtil;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

public class ShopCommand implements CommandExecutor {

    private final Main plugin;

    public ShopCommand(Main plugin) {
        this.plugin = plugin;
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (!(sender instanceof Player player)) {
            MessageUtil.sendRaw(sender, MessageUtil.PREFIX + " <red>Only players can use this command.</red>");
            return true;
        }

        player.openInventory(new ShopGUI(plugin, player).create());
        return true;
    }
}