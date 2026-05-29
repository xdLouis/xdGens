package de.louis.xdGens.crate;

import de.louis.xdGens.util.MessageUtil;
import de.louis.xdGens.util.NumberUtil;
import net.kyori.adventure.text.Component;
import org.bukkit.Material;
import org.bukkit.NamespacedKey;
import org.bukkit.inventory.ItemFlag;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.persistence.PersistentDataType;
import org.bukkit.plugin.java.JavaPlugin;

import java.util.ArrayList;
import java.util.List;

public class PouchItem {

    public static final String POUCH_TYPE_KEY = "xdgens_pouch_type";
    public static final String POUCH_VALUE_KEY = "xdgens_pouch_value";

    public static ItemStack create(JavaPlugin plugin, PouchType type, long value, CrateType crateType) {
        Material material = switch (type) {
            case MONEY -> Material.EMERALD;
            case XP -> Material.EXPERIENCE_BOTTLE;
            case TOKENS -> Material.GOLD_INGOT;
        };

        String name = switch (type) {
            case MONEY -> "Money Pouch";
            case XP -> "XP Pouch";
            case TOKENS -> "Token Pouch";
        };

        ItemStack item = new ItemStack(material);
        ItemMeta meta = item.getItemMeta();
        meta.displayName(MessageUtil.parse(crateType.getGradient() + "<bold>✦ " + name + " ✦</bold></gradient>"));

        List<Component> lore = new ArrayList<>();
        lore.add(Component.empty());
        lore.add(MessageUtil.parse("<gray>Crate: " + crateType.getGradient() + crateType.getDisplayName() + "</gradient>"));
        lore.add(MessageUtil.parse("<gray>Reward: <white>" + NumberUtil.format(value) + " " + readable(type) + "</white>"));
        lore.add(Component.empty());
        lore.add(MessageUtil.parse("<green>▶ Right click to open"));
        meta.lore(lore);
        meta.addItemFlags(ItemFlag.HIDE_ATTRIBUTES);

        NamespacedKey typeKey = new NamespacedKey(plugin, POUCH_TYPE_KEY);
        NamespacedKey valueKey = new NamespacedKey(plugin, POUCH_VALUE_KEY);
        meta.getPersistentDataContainer().set(typeKey, PersistentDataType.STRING, type.name());
        meta.getPersistentDataContainer().set(valueKey, PersistentDataType.LONG, value);

        item.setItemMeta(meta);
        return item;
    }

    public static boolean isPouch(JavaPlugin plugin, ItemStack item) {
        if (item == null || item.getType().isAir() || !item.hasItemMeta()) return false;
        NamespacedKey typeKey = new NamespacedKey(plugin, POUCH_TYPE_KEY);
        return item.getItemMeta().getPersistentDataContainer().has(typeKey, PersistentDataType.STRING);
    }

    public static PouchType getType(JavaPlugin plugin, ItemStack item) {
        if (!isPouch(plugin, item)) return null;
        NamespacedKey typeKey = new NamespacedKey(plugin, POUCH_TYPE_KEY);
        String raw = item.getItemMeta().getPersistentDataContainer().get(typeKey, PersistentDataType.STRING);
        if (raw == null) return null;
        try {
            return PouchType.valueOf(raw);
        } catch (IllegalArgumentException ex) {
            return null;
        }
    }

    public static long getValue(JavaPlugin plugin, ItemStack item) {
        if (!isPouch(plugin, item)) return 0L;
        NamespacedKey valueKey = new NamespacedKey(plugin, POUCH_VALUE_KEY);
        Long raw = item.getItemMeta().getPersistentDataContainer().get(valueKey, PersistentDataType.LONG);
        return raw == null ? 0L : raw;
    }

    private static String readable(PouchType type) {
        return switch (type) {
            case MONEY -> "Money";
            case XP -> "XP";
            case TOKENS -> "Tokens";
        };
    }
}
