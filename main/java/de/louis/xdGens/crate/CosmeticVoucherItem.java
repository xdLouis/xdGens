package de.louis.xdGens.crate;

import de.louis.xdGens.main.Main;
import de.louis.xdGens.util.MessageUtil;
import net.kyori.adventure.text.Component;
import org.bukkit.Material;
import org.bukkit.NamespacedKey;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.inventory.ItemFlag;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.persistence.PersistentDataType;

import java.util.ArrayList;
import java.util.List;

/**
 * Factory for cosmetic voucher items.
 * PDC key "cosmetic_voucher" = CrateReward name (e.g. TAG_LEGEND)
 */
public final class CosmeticVoucherItem {

    public static final String PDC_KEY = "cosmetic_voucher";

    private CosmeticVoucherItem() {}

    public static ItemStack create(Main plugin, CrateReward reward) {
        NamespacedKey key = new NamespacedKey(plugin, PDC_KEY);

        Material mat = reward.isTag() ? Material.NAME_TAG : Material.GLOW_INK_SAC;
        ItemStack item = new ItemStack(mat);
        ItemMeta meta = item.getItemMeta();

        String tierColor = switch (reward.getTier()) {
            case CrateReward.TIER_VERY_RARE -> "<gradient:#c471f5:#fa71cd>";
            case CrateReward.TIER_RARE      -> "<gradient:#7afcff:#00c2ff>";
            default                         -> "<gray>";
        };
        String tierEnd = "</gradient>";

        meta.displayName(MessageUtil.parse(
                tierColor + (reward.isTag() ? "\uD83C\uDFF7 Tag Voucher: " : "\uD83C\uDFA8 Color Voucher: ")
                + reward.getDisplayName() + tierEnd
        ));

        List<Component> lore = new ArrayList<>();
        lore.add(Component.empty());
        lore.add(MessageUtil.parse("<gray>Type: <white>" + (reward.isTag() ? "Chat Tag" : "Chat Color") + "</white>"));
        lore.add(MessageUtil.parse("<gray>Rarity: " + reward.tierLabel()));
        lore.add(Component.empty());
        lore.add(MessageUtil.parse("<gray>Preview: " + reward.getCosmeticFormat().replace("{name}", "Steve")));
        lore.add(Component.empty());
        lore.add(MessageUtil.parse("<green>▶ Right-click to redeem!"));
        lore.add(MessageUtil.parse("<dark_gray>Saved permanently to your account."));
        meta.lore(lore);

        meta.addEnchant(Enchantment.UNBREAKING, 1, true);
        meta.addItemFlags(ItemFlag.HIDE_ENCHANTS, ItemFlag.HIDE_ATTRIBUTES);

        meta.getPersistentDataContainer().set(key, PersistentDataType.STRING, reward.name());
        item.setItemMeta(meta);
        return item;
    }

    /** Returns null if the item is not a cosmetic voucher. */
    public static CrateReward getReward(Main plugin, ItemStack item) {
        if (item == null || !item.hasItemMeta()) return null;
        NamespacedKey key = new NamespacedKey(plugin, PDC_KEY);
        String raw = item.getItemMeta().getPersistentDataContainer().get(key, PersistentDataType.STRING);
        if (raw == null) return null;
        try { return CrateReward.valueOf(raw); }
        catch (IllegalArgumentException e) { return null; }
    }

    public static boolean isVoucher(Main plugin, ItemStack item) {
        return getReward(plugin, item) != null;
    }
}
