package de.louis.xdGens.command;

import de.louis.xdGens.util.MessageUtil;
import org.bukkit.Bukkit;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.InventoryCloseEvent;
import org.bukkit.inventory.Inventory;
import org.jetbrains.annotations.NotNull;

public class TrashCommand implements CommandExecutor, Listener {

    private static final String TITLE = "🗑 Trash";

    @Override
    public boolean onCommand(@NotNull CommandSender sender, @NotNull Command command,
                             @NotNull String label, @NotNull String[] args) {
        if (!(sender instanceof Player player)) {
            sender.sendMessage("Only players can use this command.");
            return true;
        }

        Inventory trash = Bukkit.createInventory(null, 9, MessageUtil.parse("<dark_gray>🗑 Trash</dark_gray>"));
        player.openInventory(trash);
        return true;
    }

    @EventHandler
    public void onClose(InventoryCloseEvent event) {
        if (!(event.getPlayer() instanceof Player player)) return;
        if (!event.getView().title().equals(MessageUtil.parse("<dark_gray>🗑 Trash</dark_gray>"))) return;

        // Clear all items — they are deleted
        event.getInventory().clear();
    }
}
