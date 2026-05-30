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

    public static final String POUCH_TYPE_KEY  = "xdgens_pouch_type";
    public static final String POUCH_VALUE_KEY = "xdgens_pouch_value";
    public static final String POUCH_TIER_KEY  = "xdgens_pouch_tier";

    // ── creation ────────────────────────────────────────────────

    public static ItemStack create(JavaPlugin plugin, PouchType type, PouchTier tier, long value) {
        Material material = switch (type) {
            case MONEY  -> Material.EMERALD;
            case XP     -> Material.EXPERIENCE_BOTTLE;
            case TOKENS -> Material.GOLD_INGOT;
        };

        String typeName = switch (type) {
            case MONEY  -> "Money Pouch";
            case XP     -> "XP Pouch";
            case TOKENS -> "Token Pouch";
        };

        // Name: "<tierColor>T3 Rare Money Pouch"
        String nameTag = tier.getColorTag();
        String closingTag = nameTag.startsWith("<gradient") ? "</gradient>" : nameTag.replace("<", "</");
        String fullName = nameTag + "<bold>" + tier.name() + " " + typeName + "</bold>" + closingTag;

        ItemStack item = new ItemStack(material);
        ItemMeta  meta = item.getItemMeta();
        meta.displayName(MessageUtil.parse(fullName));

        List<Component> lore = new ArrayList<>();
        lore.add(Component.empty());
        lore.add(MessageUtil.parse("<gray>Tier: " + tier.getDisplayName()));
        lore.add(MessageUtil.parse("<gray>Reward: <white>+" + NumberUtil.format(value) + " " + readable(type) + "</white>"));
        lore.add(Component.empty());
        lore.add(MessageUtil.parse("<green>▶ Right click to open"));
        meta.lore(lore);
        meta.addItemFlags(ItemFlag.HIDE_ATTRIBUTES);

        NamespacedKey typeKey  = new NamespacedKey(plugin, POUCH_TYPE_KEY);
        NamespacedKey valueKey = new NamespacedKey(plugin, POUCH_VALUE_KEY);
        NamespacedKey tierKey  = new NamespacedKey(plugin, POUCH_TIER_KEY);
        meta.getPersistentDataContainer().set(typeKey,  PersistentDataType.STRING, type.name());
        meta.getPersistentDataContainer().set(valueKey, PersistentDataType.LONG,   value);
        meta.getPersistentDataContainer().set(tierKey,  PersistentDataType.STRING, tier.name());

        item.setItemMeta(meta);
        return item;
    }

    // ── readers ─────────────────────────────────────────────────

    public static boolean isPouch(JavaPlugin plugin, ItemStack item) {
        if (item == null || item.getType().isAir() || !item.hasItemMeta()) return false;
        return item.getItemMeta().getPersistentDataContainer()
                .has(new NamespacedKey(plugin, POUCH_TYPE_KEY), PersistentDataType.STRING);
    }

    public static PouchType getType(JavaPlugin plugin, ItemStack item) {
        if (!isPouch(plugin, item)) return null;
        String raw = item.getItemMeta().getPersistentDataContainer()
                .get(new NamespacedKey(plugin, POUCH_TYPE_KEY), PersistentDataType.STRING);
        try { return raw != null ? PouchType.valueOf(raw) : null; }
        catch (IllegalArgumentException e) { return null; }
    }

    public static long getValue(JavaPlugin plugin, ItemStack item) {
        if (!isPouch(plugin, item)) return 0L;
        Long v = item.getItemMeta().getPersistentDataContainer()
                .get(new NamespacedKey(plugin, POUCH_VALUE_KEY), PersistentDataType.LONG);
        return v == null ? 0L : v;
    }

    public static PouchTier getTier(JavaPlugin plugin, ItemStack item) {
        if (!isPouch(plugin, item)) return PouchTier.T1;
        String raw = item.getItemMeta().getPersistentDataContainer()
                .get(new NamespacedKey(plugin, POUCH_TIER_KEY), PersistentDataType.STRING);
        try { return raw != null ? PouchTier.valueOf(raw) : PouchTier.T1; }
        catch (IllegalArgumentException e) { return PouchTier.T1; }
    }

    // ── internal ────────────────────────────────────────────────

    private static String readable(PouchType type) {
        return switch (type) {
            case MONEY  -> "Money";
            case XP     -> "XP";
            case TOKENS -> "Tokens";
        };
    }
}
