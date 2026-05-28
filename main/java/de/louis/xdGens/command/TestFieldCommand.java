package de.louis.xdGens.command;

import de.louis.xdGens.field.FieldManager;
import de.louis.xdGens.util.MessageUtil;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.block.data.Ageable;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabExecutor;
import org.bukkit.entity.Player;

import java.util.Collections;
import java.util.List;

public class TestFieldCommand implements CommandExecutor, TabExecutor {

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (!(sender instanceof Player player)) {
            sender.sendMessage("Only players can use this command.");
            return true;
        }

        int radius = 25;
        int y = player.getLocation().getBlockY() - 1;

        if (args.length >= 1) {
            try {
                radius = Integer.parseInt(args[0]);
            } catch (NumberFormatException ignored) {
                MessageUtil.send(player, MessageUtil.ERROR + "Radius muss eine Zahl sein." + MessageUtil.CLOSE);
                return true;
            }
        }

        if (args.length >= 2) {
            try {
                y = Integer.parseInt(args[1]);
            } catch (NumberFormatException ignored) {
                MessageUtil.send(player, MessageUtil.ERROR + "Y muss eine Zahl sein." + MessageUtil.CLOSE);
                return true;
            }
        }

        if (radius < 1 || radius > 200) {
            MessageUtil.send(player, MessageUtil.ERROR + "Radius muss zwischen 1 und 200 liegen." + MessageUtil.CLOSE);
            return true;
        }

        int centerX = player.getLocation().getBlockX();
        int centerZ = player.getLocation().getBlockZ();
        int changed = 0;

        for (int x = centerX - radius; x <= centerX + radius; x++) {
            for (int z = centerZ - radius; z <= centerZ + radius; z++) {
                Block farmland = player.getWorld().getBlockAt(x, y, z);
                Block crop = player.getWorld().getBlockAt(x, y + 1, z);

                farmland.setType(Material.FARMLAND);
                FieldManager.moisturizeFarmland(farmland);

                crop.setType(Material.WHEAT);
                if (crop.getBlockData() instanceof Ageable ageable) {
                    ageable.setAge(ageable.getMaximumAge());
                    crop.setBlockData(ageable, false);
                }

                changed++;
            }
        }

        MessageUtil.sendRaw(player,
                MessageUtil.GRADIENT_PREFIX
                        + " <gradient:#7afcff:#00c2ff>Testfeld erstellt</gradient>"
                        + " <gray>(" + changed + " Blöcke, Radius " + radius + ", Y " + y + ")</gray>");
        return true;
    }

    @Override
    public List<String> onTabComplete(CommandSender sender, Command command, String alias, String[] args) {
        return Collections.emptyList();
    }
}