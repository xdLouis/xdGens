package de.louis.xdGens.manager;

import de.louis.xdGens.crate.CrateType;
import de.louis.xdGens.util.MessageUtil;
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

public class PouchKeyItem {

    public static final String KEY_CRATE_KEY = "xdgens_crate_key";

    public static ItemStack create(JavaPlugin plugin, CrateType crateType) {
        ItemStack item = new ItemStack(Material.TRIPWIRE_HOOK);
        ItemMeta meta = item.getItemMeta();
        meta.displayName(MessageUtil.parse(crateType.getGradient() + "<bold>✦ " + crateType.getDisplayName() + " Key ✦</bold></gradient>"));

        List<Component> lore = new ArrayList<>();
        lore.add(Component.empty());
        lore.add(MessageUtil.parse("<gray>Opens a " + crateType.getGradient() + crateType.getDisplayName() + " Crate</gradient>"));
        lore.add(MessageUtil.parse("<gray>Use: <white>/crates</white>"));
        lore.add(Component.empty());
        lore.add(MessageUtil.parse("<green>▶ Click in /crates to open"));
        meta.lore(lore);
        meta.addItemFlags(ItemFlag.HIDE_ATTRIBUTES);

        NamespacedKey key = new NamespacedKey(plugin, KEY_CRATE_KEY);
        meta.getPersistentDataContainer().set(key, PersistentDataType.STRING, crateType.name());
        item.setItemMeta(meta);
        return item;
    }

    public static boolean isKey(JavaPlugin plugin, ItemStack item) {
        if (item == null || item.getType().isAir() || !item.hasItemMeta()) return false;
        NamespacedKey key = new NamespacedKey(plugin, KEY_CRATE_KEY);
        return item.getItemMeta().getPersistentDataContainer().has(key, PersistentDataType.STRING);
    }

    public static CrateType getCrateType(JavaPlugin plugin, ItemStack item) {
        if (!isKey(plugin, item)) return null;
        NamespacedKey key = new NamespacedKey(plugin, KEY_CRATE_KEY);
        String raw = item.getItemMeta().getPersistentDataContainer().get(key, PersistentDataType.STRING);
        if (raw == null) return null;
        try {
            return CrateType.valueOf(raw);
        } catch (IllegalArgumentException ex) {
            return null;
        }
    }
}
