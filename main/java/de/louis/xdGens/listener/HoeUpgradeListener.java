package de.louis.xdGens.listener;

import de.louis.xdGens.gui.HoeUpgradeGUI;
import de.louis.xdGens.main.Main;
import de.louis.xdGens.util.HoeUtil;
import de.louis.xdGens.util.MessageUtil;
import de.louis.xdGens.util.NumberUtil;
import net.kyori.adventure.text.serializer.plain.PlainTextComponentSerializer;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.block.Action;
import org.bukkit.event.inventory.InventoryClickEvent;
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

        Player player = event.getPlayer();
        Block clicked = event.getClickedBlock();

        if (clicked != null
                && clicked.getType() == Material.SMITHING_TABLE
                && plugin.getWorkstationManager().isWorkstation(clicked.getLocation())) {
            event.setCancelled(true);
            plugin.getWorkstationManager().useWorkstation(player);
            return;
        }

        if (event.getAction() == Action.RIGHT_CLICK_BLOCK) {
            event.setCancelled(true);
        }

        gui.open(player);
    }

    @EventHandler(priority = EventPriority.HIGH, ignoreCancelled = true)
    public void onInventoryClick(InventoryClickEvent event) {
        if (!(event.getWhoClicked() instanceof Player player)) {
            return;
        }

        String title = PlainTextComponentSerializer.plainText().serialize(event.getView().title());
        if (!title.contains("Hoe Upgrades")) {
            return;
        }

        event.setCancelled(true);

        ItemStack clicked = event.getCurrentItem();
        if (clicked == null || clicked.getType().isAir()) {
            return;
        }

        if (event.getSlot() == 11) {
            handleCropUpgrade(player);
        } else if (event.getSlot() == 13) {
            handleXpUpgrade(player);
        } else if (event.getSlot() == 15) {
            handleTokenUpgrade(player);
        }
    }

    private void handleCropUpgrade(Player player) {
        int currentLevel = plugin.getHoeUpgradeManager().getCropLevel(player);

        if (currentLevel >= plugin.getHoeUpgradeManager().MAX_CROP_LEVEL) {
            MessageUtil.sendRaw(player,
                    MessageUtil.PREFIX + " <gold>Crop Harvest is already maxed out!</gold>");
            return;
        }

        int nextLevel = currentLevel + 1;
        long tokens = plugin.getCurrencyManager().getTokens(player);
        long cost = plugin.getHoeUpgradeManager().getCropCost(nextLevel);

        if (tokens < cost) {
            long missing = cost - tokens;
            MessageUtil.sendRaw(player,
                    MessageUtil.PREFIX + " <red>Not enough Tokens! You need "
                            + NumberUtil.format(missing) + " more.</red>");
            return;
        }

        if (plugin.getHoeUpgradeManager().upgradeCrop(player)) {
            MessageUtil.sendRaw(player,
                    MessageUtil.PREFIX + " <gradient:#f6d365:#fda085>Crop Harvest upgraded to Level "
                            + plugin.getHoeUpgradeManager().getCropLevel(player)
                            + "!</gradient> <gray>(-" + NumberUtil.format(cost) + " Tokens)</gray>");
            gui.open(player);
        }
    }

    private void handleXpUpgrade(Player player) {
        int currentLevel = plugin.getHoeUpgradeManager().getXpLevel(player);

        if (currentLevel >= plugin.getHoeUpgradeManager().MAX_XP_LEVEL) {
            MessageUtil.sendRaw(player,
                    MessageUtil.PREFIX + " <gold>XP Boost is already maxed out!</gold>");
            return;
        }

        int nextLevel = currentLevel + 1;
        long tokens = plugin.getCurrencyManager().getTokens(player);
        long cost = plugin.getHoeUpgradeManager().getXpCost(nextLevel);

        if (tokens < cost) {
            long missing = cost - tokens;
            MessageUtil.sendRaw(player,
                    MessageUtil.PREFIX + " <red>Not enough Tokens! You need "
                            + NumberUtil.format(missing) + " more.</red>");
            return;
        }

        if (plugin.getHoeUpgradeManager().upgradeXp(player)) {
            MessageUtil.sendRaw(player,
                    MessageUtil.PREFIX + " <gradient:#7afcff:#00c2ff>XP Boost upgraded to Level "
                            + plugin.getHoeUpgradeManager().getXpLevel(player)
                            + "!</gradient> <gray>(-" + NumberUtil.format(cost) + " Tokens)</gray>");
            gui.open(player);
        }
    }

    private void handleTokenUpgrade(Player player) {
        int currentLevel = plugin.getHoeUpgradeManager().getTokenLevel(player);

        if (currentLevel >= plugin.getHoeUpgradeManager().MAX_TOKEN_LEVEL) {
            MessageUtil.sendRaw(player,
                    MessageUtil.PREFIX + " <gold>Token Boost is already maxed out!</gold>");
            return;
        }

        int nextLevel = currentLevel + 1;
        long tokens = plugin.getCurrencyManager().getTokens(player);
        long cost = plugin.getHoeUpgradeManager().getTokenCost(nextLevel);

        if (tokens < cost) {
            long missing = cost - tokens;
            MessageUtil.sendRaw(player,
                    MessageUtil.PREFIX + " <red>Not enough Tokens! You need "
                            + NumberUtil.format(missing) + " more.</red>");
            return;
        }

        if (plugin.getHoeUpgradeManager().upgradeToken(player)) {
            MessageUtil.sendRaw(player,
                    MessageUtil.PREFIX + " <gradient:#ffd86f:#fc6262>Token Boost upgraded to Level "
                            + plugin.getHoeUpgradeManager().getTokenLevel(player)
                            + "!</gradient> <gray>(-" + NumberUtil.format(cost) + " Tokens)</gray>");
            gui.open(player);
        }
    }
}