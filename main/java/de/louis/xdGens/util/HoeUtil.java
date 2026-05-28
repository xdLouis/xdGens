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

public class HoeUtil {

    public static final String TOOL_KEY = "xdgens_tool";
    public static final String TOOL_TYPE_KEY = "xdgens_tool_type";
    public static final String HOE_LEVEL_KEY = "xdgens_hoe_level";

    public static ItemStack createStarterHoe(Main plugin) {
        ItemStack item = new ItemStack(Material.WOODEN_HOE);
        ItemMeta meta = item.getItemMeta();

        meta.displayName(MessageUtil.parse("<gradient:#7afcff:#00c2ff><bold>Starter Hoe</bold></gradient> <gray>[Lvl 1]</gray>"));
        meta.lore(List.of(
                MessageUtil.parse("<gray>Dein Start-Tool für das</gray> <gradient:#f6d365:#fda085>Weizenfeld</gradient>"),
                MessageUtil.parse("<dark_gray>•</dark_gray> <gray>Level:</gray> <gradient:#7afcff:#00c2ff>1</gradient>"),
                MessageUtil.parse("<dark_gray>•</dark_gray> <gray>Typ:</gray> <gradient:#a18cd1:#fbc2eb>Farm Hoe</gradient>"),
                MessageUtil.parse("<dark_gray>•</dark_gray> <gray>Nur mit dieser Hoe kannst du ernten.</gray>")
        ));

        meta.addEnchant(Enchantment.UNBREAKING, 1, true);
        meta.addItemFlags(ItemFlag.HIDE_ENCHANTS, ItemFlag.HIDE_ATTRIBUTES);

        PersistentDataContainer pdc = meta.getPersistentDataContainer();
        pdc.set(new NamespacedKey(plugin, TOOL_KEY), PersistentDataType.BYTE, (byte) 1);
        pdc.set(new NamespacedKey(plugin, TOOL_TYPE_KEY), PersistentDataType.STRING, "hoe");
        pdc.set(new NamespacedKey(plugin, HOE_LEVEL_KEY), PersistentDataType.INTEGER, 1);

        item.setItemMeta(meta);
        return item;
    }

    public static boolean isXdHoe(ItemStack item) {
        if (item == null || item.getType().isAir() || !item.hasItemMeta()) {
            return false;
        }

        ItemMeta meta = item.getItemMeta();
        PersistentDataContainer pdc = meta.getPersistentDataContainer();

        Byte tagged = pdc.get(new NamespacedKey(Main.get(), TOOL_KEY), PersistentDataType.BYTE);
        String type = pdc.get(new NamespacedKey(Main.get(), TOOL_TYPE_KEY), PersistentDataType.STRING);

        return tagged != null && tagged == (byte) 1 && "hoe".equalsIgnoreCase(type);
    }

    public static int getHoeLevel(ItemStack item) {
        if (!isXdHoe(item)) {
            return 0;
        }

        Integer level = item.getItemMeta().getPersistentDataContainer()
                .get(new NamespacedKey(Main.get(), HOE_LEVEL_KEY), PersistentDataType.INTEGER);

        return level == null ? 1 : level;
    }
}