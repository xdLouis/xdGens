package de.louis.xdGens.command;

import de.louis.xdGens.main.Main;
import de.louis.xdGens.util.MessageUtil;
import de.louis.xdGens.util.NumberUtil;
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

        int requiredLevel = plugin.getProgressionManager().getRequiredLevelForPrestige(player);
        int currentLevel = plugin.getProgressionManager().getLevel(player);
        double cost = plugin.getProgressionManager().getPrestigeCost(player);
        double money = plugin.getCurrencyManager().getMoney(player);

        if (currentLevel < requiredLevel) {
            MessageUtil.sendRaw(player,
                    MessageUtil.PREFIX + " <red>You need Level " + requiredLevel
                            + " to prestige.</red> <gray>(Current: " + currentLevel + ")</gray>");
            return true;
        }

        if (money < cost) {
            double missing = cost - money;
            MessageUtil.sendRaw(player,
                    MessageUtil.PREFIX + " <red>You need more money to prestige.</red> "
                            + "<gray>Missing:</gray> <green>$" + NumberUtil.format(missing) + "</green>");
            return true;
        }

        if (!plugin.getProgressionManager().prestige(player)) {
            MessageUtil.sendRaw(player,
                    MessageUtil.PREFIX + " <red>You could not prestige right now.</red>");
            return true;
        }

        return true;
    }
}