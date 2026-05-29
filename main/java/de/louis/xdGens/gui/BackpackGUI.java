package de.louis.xdGens.gui;

import de.louis.xdGens.main.Main;
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

public class BackpackGUI {

    public static final String TITLE      = "Crop Backpack";
    public static final int SLOT_INFO     = 10;
    public static final int SLOT_WHEAT    = 11;
    public static final int SLOT_UPGRADE  = 15;
    public static final int SLOT_SELL     = 13;

    private final Main plugin;
    private final Player player;

    public BackpackGUI(Main plugin, Player player) {
        this.plugin = plugin;
        this.player = player;
    }

    public Inventory create() {
        Inventory inv = Bukkit.createInventory(null, 27,
                MessageUtil.parse("<gradient:#84fab0:#8fd3f4><bold>Crop Backpack</bold></gradient>"));
        fill(inv);
        inv.setItem(SLOT_INFO,    buildInfo());
        inv.setItem(SLOT_WHEAT,   buildWheatDisplay());
        inv.setItem(SLOT_UPGRADE, buildUpgrade());
        inv.setItem(SLOT_SELL,    buildSell());
        return inv;
    }

    private ItemStack buildInfo() {
        ItemStack item = new ItemStack(Material.BUNDLE);
        ItemMeta meta = item.getItemMeta();
        meta.displayName(MessageUtil.parse("<gradient:#84fab0:#8fd3f4><bold>Backpack Info</bold></gradient>"));
        List<Component> lore = new ArrayList<>();
        lore.add(MessageUtil.parse("<gray>Level: <aqua>" + plugin.getBackpackManager().getLevel(player) + "</aqua>"));
        lore.add(MessageUtil.parse("<gray>Capacity: <white>" + plugin.getBackpackManager().getCapacity(player) + "</white>"));
        lore.add(MessageUtil.parse("<gray>Free Space: <green>" + plugin.getBackpackManager().getFreeSpace(player) + "</green>"));
        meta.lore(lore);
        meta.addEnchant(Enchantment.UNBREAKING, 1, true);
        meta.addItemFlags(ItemFlag.HIDE_ENCHANTS, ItemFlag.HIDE_ATTRIBUTES);
        item.setItemMeta(meta);
        return item;
    }

    private ItemStack buildWheatDisplay() {
        ItemStack item = new ItemStack(Material.WHEAT);
        ItemMeta meta = item.getItemMeta();
        int stored = plugin.getBackpackManager().getStoredWheat(player);
        meta.displayName(MessageUtil.parse("<yellow><bold>Stored Crops</bold></yellow>"));
        List<Component> lore = new ArrayList<>();
        lore.add(MessageUtil.parse("<gray>Farm Wheat: <yellow>" + stored + "</yellow>"));
        meta.lore(lore);
        meta.addItemFlags(ItemFlag.HIDE_ATTRIBUTES);
        item.setItemMeta(meta);
        return item;
    }

    private ItemStack buildUpgrade() {
        ItemStack item = new ItemStack(Material.NETHER_STAR);
        ItemMeta meta = item.getItemMeta();
        boolean canUpgrade = plugin.getBackpackManager().canUpgrade(player);
        int level = plugin.getBackpackManager().getLevel(player);
        if (canUpgrade) {
            double cost = plugin.getBackpackManager().getUpgradeCost(player);
            meta.displayName(MessageUtil.parse("<gradient:#f6d365:#fda085><bold>Upgrade Backpack</bold></gradient>"));
            List<Component> lore = new ArrayList<>();
            lore.add(MessageUtil.parse("<gray>Current Level: <aqua>" + level + "</aqua>"));
            lore.add(MessageUtil.parse("<gray>Next Level: <aqua>" + (level + 1) + "</aqua>"));
            lore.add(MessageUtil.parse("<gray>Cost: <green>$" + NumberUtil.format(cost) + "</green>"));
            lore.add(MessageUtil.parse("<dark_gray>Click to upgrade!</dark_gray>"));
            meta.lore(lore);
            meta.addEnchant(Enchantment.UNBREAKING, 1, true);
            meta.addItemFlags(ItemFlag.HIDE_ENCHANTS, ItemFlag.HIDE_ATTRIBUTES);
        } else {
            meta.displayName(MessageUtil.parse("<gray><bold>Upgrade Backpack</bold></gray>"));
            List<Component> lore = new ArrayList<>();
            lore.add(MessageUtil.parse("<red>Max level reached!</red>"));
            meta.lore(lore);
            meta.addItemFlags(ItemFlag.HIDE_ATTRIBUTES);
        }
        item.setItemMeta(meta);
        return item;
    }

    private ItemStack buildSell() {
        ItemStack item = new ItemStack(Material.EMERALD);
        ItemMeta meta = item.getItemMeta();
        meta.displayName(MessageUtil.parse("<gradient:#7afcff:#00c2ff><bold>Sell Contents</bold></gradient>"));
        List<Component> lore = new ArrayList<>();
        lore.add(MessageUtil.parse("<gray>Sells all crops in your backpack.</gray>"));
        lore.add(MessageUtil.parse("<gray>Same prices as <white>/sell</white>.</gray>"));
        meta.lore(lore);
        meta.addEnchant(Enchantment.UNBREAKING, 1, true);
        meta.addItemFlags(ItemFlag.HIDE_ENCHANTS, ItemFlag.HIDE_ATTRIBUTES);
        item.setItemMeta(meta);
        return item;
    }

    private void fill(Inventory inv) {
        ItemStack border = new ItemStack(Material.GRAY_STAINED_GLASS_PANE);
        ItemMeta meta = border.getItemMeta();
        meta.displayName(MessageUtil.parse("<gray> </gray>"));
        border.setItemMeta(meta);
        for (int i = 0; i < inv.getSize(); i++) inv.setItem(i, border.clone());
    }
}
