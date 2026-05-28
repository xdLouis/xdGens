package de.louis.xdGens.listener;

import de.louis.xdGens.gui.HoeUpgradeGUI;
import de.louis.xdGens.main.Main;
import de.louis.xdGens.util.HoeUtil;
import de.louis.xdGens.util.MessageUtil;
import de.louis.xdGens.util.NumberUtil;
import net.kyori.adventure.text.serializer.plain.PlainTextComponentSerializer;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.block.Action;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.inventory.InventoryCloseEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.inventory.EquipmentSlot;
import org.bukkit.inventory.ItemStack;

public class HoeUpgradeListener implements Listener {

    private final Main plugin;
    private final HoeUpgradeGUI gui;

    public HoeUpgradeListener(Main plugin) {
        this.plugin = plugin;
        this.gui = new HoeUpgradeGUI(plugin);
    }

    @EventHandler(priority = EventPriority.HIGH)
    public void onRightClick(PlayerInteractEvent event) {
        if (event.getHand() != EquipmentSlot.HAND) {
            return;
        }

        if (event.getAction() != Action.RIGHT_CLICK_AIR
                && event.getAction() != Action.RIGHT_CLICK_BLOCK) {
            return;
        }

        ItemStack item = event.getItem();
        if (!HoeUtil.isXdHoe(item)) {
            return;
        }

        if (event.getAction() == Action.RIGHT_CLICK_BLOCK) {
            event.setCancelled(true);
        }

        Player player = event.getPlayer();
        gui.open(player);
    }

    @EventHandler(priority = EventPriority.HIGH, ignoreCancelled = true)
    public void onInventoryClick(InventoryClickEvent event) {
        if (!(event.getWhoClicked() instanceof Player player)) {
            return;
        }

        if (event.getView().title() == null) {
            return;
        }

        String title = PlainTextComponentSerializer.plainText()
                .serialize(event.getView().title());

        if (!title.contains("Hoe Upgrades")) {
            return;
        }

        event.setCancelled(true);

        ItemStack clicked = event.getCurrentItem();
        if (clicked == null || clicked.getType().isAir()) {
            return;
        }

        if (event.getSlot() != 13) {
            return;
        }

        handleCropUpgradeClick(player);
    }

    private void handleCropUpgradeClick(Player player) {
        int currentLevel = plugin.getHoeUpgradeManager().getCropLevel(player);

        if (currentLevel >= plugin.getHoeUpgradeManager().MAX_LEVEL) {
            MessageUtil.sendRaw(player,
                    MessageUtil.PREFIX + " <gold>Crop Harvest is already maxed out!</gold>");
            return;
        }

        int nextLevel = currentLevel + 1;
        int cost = plugin.getHoeUpgradeManager().getTokenCost(nextLevel);
        int tokens = plugin.getCurrencyManager().getTokens(player);

        if (tokens < cost) {
            int missing = cost - tokens;
            MessageUtil.sendRaw(player,
                    MessageUtil.PREFIX + " <red>Not enough Tokens! You need "
                            + NumberUtil.format(missing) + " more.</red>");
            return;
        }

        boolean success = plugin.getHoeUpgradeManager().upgradeCrop(player);

        if (success) {
            int newLevel = plugin.getHoeUpgradeManager().getCropLevel(player);
            MessageUtil.sendRaw(player,
                    MessageUtil.PREFIX + " <gradient:#f6d365:#fda085>Crop Harvest upgraded to Level "
                            + newLevel + "!</gradient> <gray>(-" + NumberUtil.format(cost) + " Tokens)</gray>");

            gui.open(player);
        }
    }
}