package de.louis.xdGens.util;

import de.louis.xdGens.main.Main;
import org.bukkit.Material;
import org.bukkit.NamespacedKey;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.inventory.ItemFlag;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.persistence.PersistentDataContainer;
import org.bukkit.persistence.PersistentDataType;

import java.util.List;

public class CustomItemUtil {

    public static final String ITEM_TYPE_KEY = "xdgens_item_type";
    public static final String WORKSTATION_KEY = "xdgens_workstation";

    public static ItemStack createFarmWheat(Main plugin, int amount) {
        ItemStack item = new ItemStack(Material.WHEAT, amount);
        ItemMeta meta = item.getItemMeta();

        meta.displayName(MessageUtil.parse("<gradient:#f6d365:#fda085><bold>Farm Wheat</bold></gradient>"));
        meta.lore(List.of(
                MessageUtil.parse("<gray>Geerntet auf deinem xdGens Feld.</gray>"),
                MessageUtil.parse("<dark_gray>Benutze die Workstation zum Verdichten.</dark_gray>")
        ));

        PersistentDataContainer pdc = meta.getPersistentDataContainer();
        pdc.set(new NamespacedKey(plugin, ITEM_TYPE_KEY), PersistentDataType.STRING, "farm_wheat");

        item.setItemMeta(meta);
        return item;
    }

    public static ItemStack createCompressedWheatBlock(Main plugin, int amount) {
        ItemStack item = new ItemStack(Material.HAY_BLOCK, amount);
        ItemMeta meta = item.getItemMeta();

        meta.displayName(MessageUtil.parse("<gradient:#ffd86f:#fc6262><bold>Compressed Wheat Block</bold></gradient>"));
        meta.lore(List.of(
                MessageUtil.parse("<gray>Hergestellt in der Workstation.</gray>"),
                MessageUtil.parse("<dark_gray>64 davon ergeben ein stärkeres Item.</dark_gray>")
        ));

        PersistentDataContainer pdc = meta.getPersistentDataContainer();
        pdc.set(new NamespacedKey(plugin, ITEM_TYPE_KEY), PersistentDataType.STRING, "compressed_wheat_block");

        item.setItemMeta(meta);
        return item;
    }

    public static ItemStack createEnchantedWheatBale(Main plugin, int amount) {
        ItemStack item = new ItemStack(Material.HAY_BLOCK, amount);
        ItemMeta meta = item.getItemMeta();

        meta.displayName(MessageUtil.parse("<gradient:#7afcff:#00c2ff><bold>Enchanted Wheat Bale</bold></gradient>"));
        meta.lore(List.of(
                MessageUtil.parse("<gray>Eine seltene, verdichtete Form von Weizen.</gray>"),
                MessageUtil.parse("<dark_gray>Später für bessere Crafts oder Sell-Multiplikator.</dark_gray>")
        ));

        meta.addEnchant(Enchantment.UNBREAKING, 1, true);
        meta.addItemFlags(ItemFlag.HIDE_ENCHANTS);

        PersistentDataContainer pdc = meta.getPersistentDataContainer();
        pdc.set(new NamespacedKey(plugin, ITEM_TYPE_KEY), PersistentDataType.STRING, "enchanted_wheat_bale");

        item.setItemMeta(meta);
        return item;
    }

    public static ItemStack createWorkstationItem(Main plugin) {
        ItemStack item = new ItemStack(Material.SMITHING_TABLE);
        ItemMeta meta = item.getItemMeta();

        meta.displayName(MessageUtil.parse("<gradient:#a18cd1:#fbc2eb><bold>Wheat Workstation</bold></gradient>"));
        meta.lore(List.of(
                MessageUtil.parse("<gray>Platziere diesen Block.</gray>"),
                MessageUtil.parse("<gray>Rechtsklick craftet automatisch alle Weizen-Items.</gray>")
        ));

        PersistentDataContainer pdc = meta.getPersistentDataContainer();
        pdc.set(new NamespacedKey(plugin, WORKSTATION_KEY), PersistentDataType.BYTE, (byte) 1);
        pdc.set(new NamespacedKey(plugin, ITEM_TYPE_KEY), PersistentDataType.STRING, "workstation_item");

        item.setItemMeta(meta);
        return item;
    }

    public static boolean hasItemType(Main plugin, ItemStack item, String type) {
        if (item == null || item.getType().isAir() || !item.hasItemMeta()) {
            return false;
        }

        String value = item.getItemMeta().getPersistentDataContainer()
                .get(new NamespacedKey(plugin, ITEM_TYPE_KEY), PersistentDataType.STRING);

        return value != null && value.equalsIgnoreCase(type);
    }

    public static boolean isWorkstationItem(Main plugin, ItemStack item) {
        if (item == null || item.getType().isAir() || !item.hasItemMeta()) {
            return false;
        }

        Byte placed = item.getItemMeta().getPersistentDataContainer()
                .get(new NamespacedKey(plugin, WORKSTATION_KEY), PersistentDataType.BYTE);

        return placed != null && placed == (byte) 1;
    }
}