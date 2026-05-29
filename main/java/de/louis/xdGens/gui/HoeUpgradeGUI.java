package de.louis.xdGens.gui;

import de.louis.xdGens.main.Main;
import de.louis.xdGens.manager.HoeUpgradeManager;
import de.louis.xdGens.util.MessageUtil;
import de.louis.xdGens.util.NumberUtil;
import net.kyori.adventure.text.Component;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.entity.Player;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemFlag;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

import java.util.ArrayList;
import java.util.List;

public class HoeUpgradeGUI {

    public static final String GUI_TITLE = "⚡ Hoe Upgrades";

    private final Main plugin;

    public HoeUpgradeGUI(Main plugin) {
        this.plugin = plugin;
    }

    public void open(Player player) {
        Inventory inv = Bukkit.createInventory(
                null,
                27,
                MessageUtil.parse("<gradient:#a18cd1:#fbc2eb><bold>⚡ Hoe Upgrades</bold></gradient>")
        );

        fillBackground(inv);

        inv.setItem(11, buildCropUpgradeItem(player));
        inv.setItem(13, buildXpUpgradeItem(player));
        inv.setItem(15, buildTokenUpgradeItem(player));

        player.openInventory(inv);
    }

    private ItemStack buildCropUpgradeItem(Player player) {
        HoeUpgradeManager manager = plugin.getHoeUpgradeManager();
        int current = manager.getCropLevel(player);
        long tokens = plugin.getCurrencyManager().getTokens(player);

        ItemStack item = new ItemStack(Material.WHEAT);
        ItemMeta meta = item.getItemMeta();
        meta.displayName(MessageUtil.parse("<gradient:#f6d365:#fda085><bold>🌾 Crop Harvest</bold></gradient>"));

        List<Component> lore = new ArrayList<>();
        lore.add(MessageUtil.parse("<gray>Harvest more wheat per break.</gray>"));
        lore.add(MessageUtil.parse("<dark_gray>Each level adds +1 extra crop.</dark_gray>"));
        lore.add(MessageUtil.parse(" "));

        lore.add(MessageUtil.parse("<gray>Current Level: <gradient:#f6d365:#fda085>" + current + "</gradient>"));
        lore.add(MessageUtil.parse("<gray>Current Bonus: <gradient:#f6d365:#fda085>+" + current + " crops</gradient>"));
        lore.add(MessageUtil.parse("<gray>Max Level: <gradient:#f6d365:#fda085>" + HoeUpgradeManager.MAX_CROP_LEVEL + "</gradient>"));
        lore.add(MessageUtil.parse(" "));

        if (current >= HoeUpgradeManager.MAX_CROP_LEVEL) {
            lore.add(MessageUtil.parse("<gradient:#f6d365:#fda085><bold>MAXED OUT</bold></gradient>"));
        } else {
            int nextLevel = current + 1;
            long nextCost = manager.getCropCost(nextLevel);
            boolean canAfford = tokens >= nextCost;

            lore.add(MessageUtil.parse("<gray>Next Level: <yellow>" + nextLevel + "</yellow>"));
            lore.add(MessageUtil.parse("<gray>Next Bonus: <yellow>+" + nextLevel + " crops</yellow>"));
            lore.add(MessageUtil.parse("<gray>Next Cost: "
                    + (canAfford ? "<green>" : "<red>")
                    + NumberUtil.format(nextCost) + " Tokens"
                    + (canAfford ? "</green>" : "</red>")));
            lore.add(MessageUtil.parse(" "));
            lore.add(MessageUtil.parse(canAfford
                    ? "<green>Click to upgrade.</green>"
                    : "<red>You cannot afford this upgrade.</red>"));
        }

        meta.lore(lore);

        if (current > 0) {
            meta.addEnchant(Enchantment.UNBREAKING, 1, true);
            meta.addItemFlags(ItemFlag.HIDE_ENCHANTS);
        }

        meta.addItemFlags(ItemFlag.HIDE_ATTRIBUTES);
        item.setItemMeta(meta);
        return item;
    }

    private ItemStack buildXpUpgradeItem(Player player) {
        HoeUpgradeManager manager = plugin.getHoeUpgradeManager();
        int current = manager.getXpLevel(player);
        long tokens = plugin.getCurrencyManager().getTokens(player);

        ItemStack item = new ItemStack(Material.EXPERIENCE_BOTTLE);
        ItemMeta meta = item.getItemMeta();
        meta.displayName(MessageUtil.parse("<gradient:#7afcff:#00c2ff><bold>✨ XP Boost</bold></gradient>"));

        List<Component> lore = new ArrayList<>();
        lore.add(MessageUtil.parse("<gray>Gain more XP from harvesting.</gray>"));
        lore.add(MessageUtil.parse("<dark_gray>Each level adds +2% XP.</dark_gray>"));
        lore.add(MessageUtil.parse(" "));

        lore.add(MessageUtil.parse("<gray>Current Level: <gradient:#7afcff:#00c2ff>" + current + "</gradient>"));
        lore.add(MessageUtil.parse("<gray>Current Bonus: <gradient:#7afcff:#00c2ff>+"
                + NumberUtil.format(manager.getXpPercentBonus(player)) + "% XP</gradient>"));
        lore.add(MessageUtil.parse("<gray>Max Level: <gradient:#7afcff:#00c2ff>" + HoeUpgradeManager.MAX_XP_LEVEL + "</gradient>"));
        lore.add(MessageUtil.parse(" "));

        if (current >= HoeUpgradeManager.MAX_XP_LEVEL) {
            lore.add(MessageUtil.parse("<gradient:#7afcff:#00c2ff><bold>MAXED OUT</bold></gradient>"));
        } else {
            int nextLevel = current + 1;
            long nextCost = manager.getXpCost(nextLevel);
            boolean canAfford = tokens >= nextCost;

            lore.add(MessageUtil.parse("<gray>Next Level: <yellow>" + nextLevel + "</yellow>"));
            lore.add(MessageUtil.parse("<gray>Next Bonus: <yellow>+" + (nextLevel * 2L) + "% XP</yellow>"));
            lore.add(MessageUtil.parse("<gray>Next Cost: "
                    + (canAfford ? "<green>" : "<red>")
                    + NumberUtil.format(nextCost) + " Tokens"
                    + (canAfford ? "</green>" : "</red>")));
            lore.add(MessageUtil.parse(" "));
            lore.add(MessageUtil.parse(canAfford
                    ? "<green>Click to upgrade.</green>"
                    : "<red>You cannot afford this upgrade.</red>"));
        }

        meta.lore(lore);

        if (current > 0) {
            meta.addEnchant(Enchantment.UNBREAKING, 1, true);
            meta.addItemFlags(ItemFlag.HIDE_ENCHANTS);
        }

        meta.addItemFlags(ItemFlag.HIDE_ATTRIBUTES);
        item.setItemMeta(meta);
        return item;
    }

    private ItemStack buildTokenUpgradeItem(Player player) {
        HoeUpgradeManager manager = plugin.getHoeUpgradeManager();
        int current = manager.getTokenLevel(player);
        long tokens = plugin.getCurrencyManager().getTokens(player);

        ItemStack item = new ItemStack(Material.GOLD_INGOT);
        ItemMeta meta = item.getItemMeta();
        meta.displayName(MessageUtil.parse("<gradient:#ffd86f:#fc6262><bold>💰 Token Boost</bold></gradient>"));

        List<Component> lore = new ArrayList<>();
        lore.add(MessageUtil.parse("<gray>Gain more tokens from harvesting.</gray>"));
        lore.add(MessageUtil.parse("<dark_gray>Each level adds +2% Tokens.</dark_gray>"));
        lore.add(MessageUtil.parse(" "));

        lore.add(MessageUtil.parse("<gray>Current Level: <gradient:#ffd86f:#fc6262>" + current + "</gradient>"));
        lore.add(MessageUtil.parse("<gray>Current Bonus: <gradient:#ffd86f:#fc6262>+"
                + NumberUtil.format(manager.getTokenPercentBonus(player)) + "% Tokens</gradient>"));
        lore.add(MessageUtil.parse("<gray>Max Level: <gradient:#ffd86f:#fc6262>" + HoeUpgradeManager.MAX_TOKEN_LEVEL + "</gradient>"));
        lore.add(MessageUtil.parse(" "));

        if (current >= HoeUpgradeManager.MAX_TOKEN_LEVEL) {
            lore.add(MessageUtil.parse("<gradient:#ffd86f:#fc6262><bold>MAXED OUT</bold></gradient>"));
        } else {
            int nextLevel = current + 1;
            long nextCost = manager.getTokenCost(nextLevel);
            boolean canAfford = tokens >= nextCost;

            lore.add(MessageUtil.parse("<gray>Next Level: <yellow>" + nextLevel + "</yellow>"));
            lore.add(MessageUtil.parse("<gray>Next Bonus: <yellow>+" + (nextLevel * 2L) + "% Tokens</yellow>"));
            lore.add(MessageUtil.parse("<gray>Next Cost: "
                    + (canAfford ? "<green>" : "<red>")
                    + NumberUtil.format(nextCost) + " Tokens"
                    + (canAfford ? "</green>" : "</red>")));
            lore.add(MessageUtil.parse(" "));
            lore.add(MessageUtil.parse(canAfford
                    ? "<green>Click to upgrade.</green>"
                    : "<red>You cannot afford this upgrade.</red>"));
        }

        meta.lore(lore);

        if (current > 0) {
            meta.addEnchant(Enchantment.UNBREAKING, 1, true);
            meta.addItemFlags(ItemFlag.HIDE_ENCHANTS);
        }

        meta.addItemFlags(ItemFlag.HIDE_ATTRIBUTES);
        item.setItemMeta(meta);
        return item;
    }

    private void fillBackground(Inventory inv) {
        ItemStack border = new ItemStack(Material.GRAY_STAINED_GLASS_PANE);
        ItemMeta borderMeta = border.getItemMeta();
        borderMeta.displayName(MessageUtil.parse("<gray> </gray>"));
        border.setItemMeta(borderMeta);

        for (int i = 0; i < inv.getSize(); i++) {
            inv.setItem(i, border.clone());
        }
    }
}