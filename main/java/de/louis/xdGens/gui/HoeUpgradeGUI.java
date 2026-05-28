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
        Inventory inv = Bukkit.createInventory(null, 27,
                MessageUtil.parse("<gradient:#a18cd1:#fbc2eb><bold>⚡ Hoe Upgrades</bold></gradient>"));

        fillBorder(inv);

        inv.setItem(13, buildCropUpgradeItem(player));

        player.openInventory(inv);
    }

    private ItemStack buildCropUpgradeItem(Player player) {
        HoeUpgradeManager upgradeManager = plugin.getHoeUpgradeManager();
        int currentLevel = upgradeManager.getCropLevel(player);
        int maxLevel = HoeUpgradeManager.MAX_LEVEL;
        int tokens = plugin.getCurrencyManager().getTokens(player);

        ItemStack item = new ItemStack(Material.WHEAT);
        ItemMeta meta = item.getItemMeta();

        meta.displayName(MessageUtil.parse(
                "<gradient:#f6d365:#fda085><bold>🌾 Crop Harvest</bold></gradient>"));

        List<Component> lore = new ArrayList<>();
        lore.add(MessageUtil.parse("<gray>Harvest more crops per break.</gray>"));
        lore.add(MessageUtil.parse(" "));

        for (int i = 1; i <= maxLevel; i++) {
            int cost = upgradeManager.getTokenCost(i);

            if (i < currentLevel) {
                lore.add(MessageUtil.parse("<green>✔ Level " + i + " <dark_gray>+" + i + " crops</dark_gray></green>"));
            } else if (i == currentLevel) {
                lore.add(MessageUtil.parse("<gradient:#7afcff:#00c2ff>▶ Level " + i + " <dark_gray>+" + i + " crops</dark_gray> <gray>(current)</gray></gradient>"));
            } else if (i == currentLevel + 1) {
                boolean canAfford = tokens >= cost;
                String costColor = canAfford ? "<green>" : "<red>";
                String costClose = canAfford ? "</green>" : "</red>";
                lore.add(MessageUtil.parse("<yellow>➔ Level " + i + " <dark_gray>+" + i + " crops</dark_gray>"
                        + " " + costColor + NumberUtil.format(cost) + " Tokens" + costClose + "</yellow>"));
            } else {
                lore.add(MessageUtil.parse("<dark_gray>✦ Level " + i + " +" + i + " crops"
                        + " <gray>[" + NumberUtil.format(upgradeManager.getTokenCost(i)) + " T]</gray></dark_gray>"));
            }
        }

        lore.add(MessageUtil.parse(" "));

        if (currentLevel >= maxLevel) {
            lore.add(MessageUtil.parse("<gradient:#f6d365:#fda085><bold>MAXED OUT!</bold></gradient>"));
        } else {
            int nextCost = upgradeManager.getTokenCost(currentLevel + 1);
            boolean canAfford = tokens >= nextCost;

            if (canAfford) {
                lore.add(MessageUtil.parse("<green>Click to upgrade!</green>"));
            } else {
                int missing = nextCost - tokens;
                lore.add(MessageUtil.parse("<red>Need " + NumberUtil.format(missing) + " more Tokens.</red>"));
            }
        }

        meta.lore(lore);

        if (currentLevel > 0) {
            meta.addEnchant(Enchantment.UNBREAKING, 1, true);
            meta.addItemFlags(ItemFlag.HIDE_ENCHANTS);
        }

        meta.addItemFlags(ItemFlag.HIDE_ATTRIBUTES);
        item.setItemMeta(meta);
        return item;
    }

    private void fillBorder(Inventory inv) {
        ItemStack glass = new ItemStack(Material.GRAY_STAINED_GLASS_PANE);
        ItemMeta meta = glass.getItemMeta();
        meta.displayName(MessageUtil.parse("<gray> </gray>"));
        glass.setItemMeta(meta);

        int[] borderSlots = {0, 1, 2, 3, 4, 5, 6, 7, 8,
                9, 17,
                18, 19, 20, 21, 22, 23, 24, 25, 26};

        for (int slot : borderSlots) {
            if (slot == 13) continue;
            inv.setItem(slot, glass.clone());
        }

        ItemStack filler = new ItemStack(Material.BLACK_STAINED_GLASS_PANE);
        ItemMeta fillerMeta = filler.getItemMeta();
        fillerMeta.displayName(MessageUtil.parse(" "));
        filler.setItemMeta(fillerMeta);

        int[] innerSlots = {10, 11, 12, 14, 15, 16};
        for (int slot : innerSlots) {
            inv.setItem(slot, filler.clone());
        }
    }
}