package de.louis.xdGens.command;

import de.louis.xdGens.main.Main;
import de.louis.xdGens.util.CustomItemUtil;
import de.louis.xdGens.util.MessageUtil;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

public class WorkstationCommand implements CommandExecutor {

    private final Main plugin;

    public WorkstationCommand(Main plugin) {
        this.plugin = plugin;
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (!(sender instanceof Player player)) {
            sender.sendMessage("Only players can use this command.");
            return true;
        }

        player.getInventory().addItem(CustomItemUtil.createWorkstationItem(plugin));
        MessageUtil.sendRaw(player,
                MessageUtil.GRADIENT_PREFIX + " <gray>Du hast eine <gradient:#a18cd1:#fbc2eb>Wheat Workstation</gradient> erhalten.</gray>");
        return true;
    }
}