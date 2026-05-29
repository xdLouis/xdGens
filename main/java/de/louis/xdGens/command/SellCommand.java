package de.louis.xdGens.command;

import de.louis.xdGens.main.Main;
import de.louis.xdGens.util.CustomItemUtil;
import de.louis.xdGens.util.MessageUtil;
import de.louis.xdGens.util.NumberUtil;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;

public class SellCommand implements CommandExecutor {

    private final Main plugin;

    public SellCommand(Main plugin) {
        this.plugin = plugin;
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (!(sender instanceof Player player)) {
            sender.sendMessage("Only players can use this command.");
            return true;
        }

        double farmWheatPrice         = plugin.getConfig().getDouble("sell.wheat.price", 30.0);
        double compressedBlockPrice   = plugin.getConfig().getDouble("sell.compressed_wheat_block.price", 2200.0);
        double enchantedBalePrice     = plugin.getConfig().getDouble("sell.enchanted_wheat_bale.price", 180000.0);

        int farmWheat        = countAndRemove(player, "farm_wheat");
        int compressedBlocks = countAndRemove(player, "compressed_wheat_block");
        int enchantedBales   = countAndRemove(player, "enchanted_wheat_bale");

        if (farmWheat == 0 && compressedBlocks == 0 && enchantedBales == 0) {
            MessageUtil.sendRaw(player, MessageUtil.PREFIX + " <red>You have nothing to sell.</red>");
            return true;
        }

        double earned =
                (farmWheat        * farmWheatPrice)
                + (compressedBlocks * compressedBlockPrice)
                + (enchantedBales   * enchantedBalePrice);

        plugin.getCurrencyManager().addMoney(player, earned);

        MessageUtil.sendRaw(player,
                MessageUtil.PREFIX
                        + " <gradient:#7afcff:#00c2ff>Sold your farm items</gradient>"
                        + " <gray>for</gray> <green>$" + NumberUtil.format(earned) + "</green>"
                        + " <gray>(Wheat: " + farmWheat
                        + ", Blocks: " + compressedBlocks
                        + ", Bales: " + enchantedBales + ")</gray>");

        return true;
    }

    private int countAndRemove(Player player, String type) {
        int total = 0;

        for (ItemStack item : player.getInventory().getContents()) {
            if (!CustomItemUtil.hasItemType(plugin, item, type)) continue;
            total += item.getAmount();
        }

        if (total <= 0) return 0;

        int left = total;
        for (int slot = 0; slot < player.getInventory().getSize(); slot++) {
            ItemStack item = player.getInventory().getItem(slot);
            if (!CustomItemUtil.hasItemType(plugin, item, type)) continue;

            int take = Math.min(left, item.getAmount());
            item.setAmount(item.getAmount() - take);
            left -= take;

            if (item.getAmount() <= 0) player.getInventory().setItem(slot, null);
            if (left <= 0) break;
        }

        return total;
    }
}
