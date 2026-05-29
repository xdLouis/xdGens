package de.louis.xdGens.util;

import de.louis.xdGens.main.Main;
import org.bukkit.Material;
import org.bukkit.NamespacedKey;
import org.bukkit.inventory.ItemFlag;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.persistence.PersistentDataType;

import java.util.ArrayList;
import java.util.List;

public class CustomItemUtil {

    public static final String ITEM_KEY        = "xdgens_item";
    public static final String ITEM_TYPE_KEY   = "xdgens_item_type";
    public static final String ITEM_AMOUNT_KEY = "xdgens_item_amount";

    public static ItemStack createFarmWheat(Main plugin, int amount) {
        ItemStack item = new ItemStack(Material.WHEAT, Math.max(1, amount));
        ItemMeta meta = item.getItemMeta();
        meta.displayName(MessageUtil.parse("<gradient:#f6d365:#fda085><bold>Farm Wheat</bold></gradient>"));
        List<net.kyori.adventure.text.Component> lore = new ArrayList<>();
        lore.add(MessageUtil.parse("<gray>Harvested crop material.</gray>"));
        meta.lore(lore);
        tag(plugin, meta, "farm_wheat", amount);
        meta.addItemFlags(ItemFlag.HIDE_ATTRIBUTES);
        item.setItemMeta(meta);
        return item;
    }

    public static ItemStack createCompressedWheatBlock(Main plugin, int amount) {
        ItemStack item = new ItemStack(Material.HAY_BLOCK, Math.max(1, amount));
        ItemMeta meta = item.getItemMeta();
        meta.displayName(MessageUtil.parse("<gradient:#7afcff:#00c2ff><bold>Compressed Wheat Block</bold></gradient>"));
        List<net.kyori.adventure.text.Component> lore = new ArrayList<>();
        lore.add(MessageUtil.parse("<gray>64 Farm Wheat compressed.</gray>"));
        meta.lore(lore);
        tag(plugin, meta, "compressed_wheat_block", amount);
        meta.addItemFlags(ItemFlag.HIDE_ATTRIBUTES);
        item.setItemMeta(meta);
        return item;
    }

    public static ItemStack createEnchantedWheatBale(Main plugin, int amount) {
        ItemStack item = new ItemStack(Material.HAY_BLOCK, Math.max(1, amount));
        ItemMeta meta = item.getItemMeta();
        meta.displayName(MessageUtil.parse("<gradient:#f093fb:#f5576c><bold>Enchanted Wheat Bale</bold></gradient>"));
        List<net.kyori.adventure.text.Component> lore = new ArrayList<>();
        lore.add(MessageUtil.parse("<gray>64 Compressed Wheat Blocks fused.</gray>"));
        meta.lore(lore);
        tag(plugin, meta, "enchanted_wheat_bale", amount);
        meta.addItemFlags(ItemFlag.HIDE_ATTRIBUTES);
        item.setItemMeta(meta);
        return item;
    }

    public static ItemStack createWorkstationItem(Main plugin) {
        ItemStack item = new ItemStack(Material.SMITHING_TABLE);
        ItemMeta meta = item.getItemMeta();
        meta.displayName(MessageUtil.parse("<gradient:#a18cd1:#fbc2eb><bold>Wheat Workstation</bold></gradient>"));
        List<net.kyori.adventure.text.Component> lore = new ArrayList<>();
        lore.add(MessageUtil.parse("<gray>Place to compress wheat.</gray>"));
        lore.add(MessageUtil.parse("<gray>Right-click to use.</gray>"));
        meta.lore(lore);
        tag(plugin, meta, "workstation", 1);
        meta.addItemFlags(ItemFlag.HIDE_ATTRIBUTES);
        item.setItemMeta(meta);
        return item;
    }

    public static ItemStack createBackpackItem(Main plugin, org.bukkit.entity.Player player) {
        ItemStack item = new ItemStack(Material.BUNDLE);
        ItemMeta meta = item.getItemMeta();
        meta.displayName(MessageUtil.parse("<gradient:#84fab0:#8fd3f4><bold>Crop Backpack</bold></gradient>"));
        List<net.kyori.adventure.text.Component> lore = new ArrayList<>();
        lore.add(MessageUtil.parse("<gray>Left-click to open.</gray>"));
        lore.add(MessageUtil.parse("<gray>Capacity: <green>" + plugin.getBackpackManager().getCapacity(player) + "</green>"));
        lore.add(MessageUtil.parse("<gray>Stored Wheat: <yellow>" + plugin.getBackpackManager().getStoredWheat(player) + "</yellow>"));
        meta.lore(lore);
        tag(plugin, meta, "backpack", 1);
        item.setItemMeta(meta);
        return item;
    }

    public static boolean isBackpack(Main plugin, ItemStack item)        { return hasItemType(plugin, item, "backpack"); }
    public static boolean isWorkstationItem(Main plugin, ItemStack item) { return hasItemType(plugin, item, "workstation"); }

    public static boolean hasItemType(Main plugin, ItemStack item, String type) {
        if (item == null || item.getType().isAir() || !item.hasItemMeta()) return false;
        var pdc = item.getItemMeta().getPersistentDataContainer();
        Byte tagged = pdc.get(new NamespacedKey(plugin, ITEM_KEY), PersistentDataType.BYTE);
        String t    = pdc.get(new NamespacedKey(plugin, ITEM_TYPE_KEY), PersistentDataType.STRING);
        return tagged != null && tagged == (byte) 1 && type.equalsIgnoreCase(t);
    }

    private static void tag(Main plugin, ItemMeta meta, String type, int amount) {
        meta.getPersistentDataContainer().set(new NamespacedKey(plugin, ITEM_KEY),        PersistentDataType.BYTE,    (byte) 1);
        meta.getPersistentDataContainer().set(new NamespacedKey(plugin, ITEM_TYPE_KEY),   PersistentDataType.STRING,  type);
        meta.getPersistentDataContainer().set(new NamespacedKey(plugin, ITEM_AMOUNT_KEY), PersistentDataType.INTEGER, amount);
    }
}
