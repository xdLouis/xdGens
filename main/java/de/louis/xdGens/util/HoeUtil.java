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

import java.util.ArrayList;
import java.util.List;

public class HoeUtil {

    public static final String TOOL_KEY = "xdgens_tool";
    public static final String TOOL_TYPE_KEY = "xdgens_tool_type";
    public static final String HOE_LEVEL_KEY = "xdgens_hoe_level";

    public static ItemStack createStarterHoe(Main plugin) {
        return createHoe(plugin, 1);
    }

    public static ItemStack createHoe(Main plugin, int level) {
        Material material = getHoeMaterial(level);

        ItemStack item = new ItemStack(material);
        ItemMeta meta = item.getItemMeta();

        String materialName = getHoeMaterialName(level);
        int stage = ((level - 1) % 3) + 1;

        meta.displayName(MessageUtil.parse(
                "<gradient:#7afcff:#00c2ff><bold>" + materialName + " Hoe</bold></gradient> <gray>[Lvl " + level + "]</gray>"
        ));

        List<net.kyori.adventure.text.Component> lore = new ArrayList<>();
        lore.add(MessageUtil.parse("<gray>Dein Tool für das</gray> <gradient:#f6d365:#fda085>Weizenfeld</gradient>"));
        lore.add(MessageUtil.parse("<dark_gray>•</dark_gray> <gray>Material:</gray> <gradient:#a18cd1:#fbc2eb>" + materialName + "</gradient>"));
        lore.add(MessageUtil.parse("<dark_gray>•</dark_gray> <gray>Stufe:</gray> <gradient:#7afcff:#00c2ff>" + stage + "/3</gradient>"));
        lore.add(MessageUtil.parse("<dark_gray>•</dark_gray> <gray>Gesamtlevel:</gray> <gradient:#7afcff:#00c2ff>" + level + "/18</gradient>"));
        lore.add(MessageUtil.parse("<dark_gray>•</dark_gray> <gray>Nur mit dieser Hoe kannst du ernten.</gray>"));

        meta.lore(lore);
        meta.addEnchant(Enchantment.UNBREAKING, 1, true);
        meta.addItemFlags(ItemFlag.HIDE_ENCHANTS, ItemFlag.HIDE_ATTRIBUTES);

        PersistentDataContainer pdc = meta.getPersistentDataContainer();
        pdc.set(new NamespacedKey(plugin, TOOL_KEY), PersistentDataType.BYTE, (byte) 1);
        pdc.set(new NamespacedKey(plugin, TOOL_TYPE_KEY), PersistentDataType.STRING, "hoe");
        pdc.set(new NamespacedKey(plugin, HOE_LEVEL_KEY), PersistentDataType.INTEGER, level);

        item.setItemMeta(meta);
        return item;
    }

    public static ItemStack updateHoeItem(Main plugin, ItemStack current, int level) {
        ItemStack updated = createHoe(plugin, level);

        if (current != null && current.hasItemMeta()) {
            updated.setAmount(current.getAmount());
        }

        return updated;
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

    private static Material getHoeMaterial(int level) {
        int group = (Math.max(1, level) - 1) / 3;
        return switch (group) {
            case 0 -> Material.WOODEN_HOE;
            case 1 -> Material.STONE_HOE;
            case 2 -> Material.IRON_HOE;
            case 3 -> Material.GOLDEN_HOE;
            case 4 -> Material.DIAMOND_HOE;
            default -> Material.NETHERITE_HOE;
        };
    }

    private static String getHoeMaterialName(int level) {
        int group = (Math.max(1, level) - 1) / 3;
        return switch (group) {
            case 0 -> "Wooden";
            case 1 -> "Stone";
            case 2 -> "Iron";
            case 3 -> "Gold";
            case 4 -> "Diamond";
            default -> "Netherite";
        };
    }
}