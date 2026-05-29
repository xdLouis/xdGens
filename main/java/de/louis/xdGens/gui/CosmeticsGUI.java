package de.louis.xdGens.gui;

import de.louis.xdGens.crate.CrateReward;
import de.louis.xdGens.main.Main;
import de.louis.xdGens.manager.PlayerCosmeticManager;
import de.louis.xdGens.util.MessageUtil;
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
import java.util.Optional;
import java.util.Set;

/**
 * 4-tab cosmetics GUI:
 *   Slot 0 = Tags
 *   Slot 1 = Name Colors
 *   Slot 2 = Chat Colors
 *   Slot 8 = Unequip
 */
public class CosmeticsGUI {

    public static final String TITLE_TAGS        = "✦ Cosmetics » Tags";
    public static final String TITLE_NAME_COLORS = "✦ Cosmetics » Name Colors";
    public static final String TITLE_CHAT_COLORS = "✦ Cosmetics » Chat Colors";

    private static final int SIZE = 54;
    private static final int SLOT_TAB_TAGS        = 0;
    private static final int SLOT_TAB_NAME_COLORS = 1;
    private static final int SLOT_TAB_CHAT_COLORS = 2;
    private static final int SLOT_UNEQUIP         = 8;

    // 3 rows × 7 centered items
    public static final int[] CONTENT_SLOTS = {
        10, 11, 12, 13, 14, 15, 16,
        19, 20, 21, 22, 23, 24, 25,
        28, 29, 30, 31, 32, 33, 34
    };

    public enum Tab { TAGS, NAME_COLORS, CHAT_COLORS }

    private final Main plugin;

    public CosmeticsGUI(Main plugin) { this.plugin = plugin; }

    public void openTags(Player p)       { open(p, Tab.TAGS); }
    public void openColors(Player p)     { open(p, Tab.NAME_COLORS); }
    public void openChatColors(Player p) { open(p, Tab.CHAT_COLORS); }

    // default open: Tags tab
    public void open(Player p) { open(p, Tab.TAGS); }

    public void open(Player player, Tab tab) {
        String rawTitle = switch (tab) {
            case TAGS        -> TITLE_TAGS;
            case NAME_COLORS -> TITLE_NAME_COLORS;
            case CHAT_COLORS -> TITLE_CHAT_COLORS;
        };
        Inventory inv = Bukkit.createInventory(null, SIZE, MessageUtil.parse(rawTitle));

        PlayerCosmeticManager mgr = plugin.getPlayerCosmeticManager();

        // fill everything with glass, then clear content + bottom row
        ItemStack filler = filler();
        for (int i = 0; i < SIZE; i++) inv.setItem(i, filler);
        for (int s : CONTENT_SLOTS)    inv.setItem(s, null);
        for (int i = 36; i < 54; i++)  inv.setItem(i, filler);

        // ── tab buttons ───────────────────────────────────────────────
        inv.setItem(SLOT_TAB_TAGS, tabBtn(
                tab == Tab.TAGS, Material.NAME_TAG,
                "<gradient:#7afcff:#00c2ff><bold>Chat Tags</bold></gradient>",
                tab == Tab.TAGS ? "<green>► Currently viewing" : "<gray>Click to switch"));

        inv.setItem(SLOT_TAB_NAME_COLORS, tabBtn(
                tab == Tab.NAME_COLORS, Material.GLOW_INK_SAC,
                "<gradient:#f6d365:#fda085><bold>Name Colors</bold></gradient>",
                tab == Tab.NAME_COLORS ? "<green>► Currently viewing" : "<gray>Click to switch"));

        inv.setItem(SLOT_TAB_CHAT_COLORS, tabBtn(
                tab == Tab.CHAT_COLORS, Material.BOOK,
                "<gradient:#c471f5:#fa71cd><bold>Chat Colors</bold></gradient>",
                tab == Tab.CHAT_COLORS ? "<green>► Currently viewing" : "<gray>Click to switch"));

        // spacer slots 3-7
        for (int i = 3; i <= 7; i++) inv.setItem(i, filler());

        // ── unequip button ────────────────────────────────────────────
        Optional<CrateReward> active = switch (tab) {
            case TAGS        -> mgr.getActiveTag(player);
            case NAME_COLORS -> mgr.getActiveColor(player);
            case CHAT_COLORS -> mgr.getActiveChatColor(player);
        };
        boolean hasActive = active.isPresent();
        String previewStr = active.map(r -> {
            if (r.isColor())     return r.getCosmeticFormat().replace("{name}", player.getName());
            if (r.isChatColor()) return r.getCosmeticFormat().replace("{msg}", "Hello!");
            return r.getCosmeticFormat();
        }).orElse("<dark_gray>None");

        inv.setItem(SLOT_UNEQUIP, buildItem(
                hasActive ? Material.BARRIER : Material.LIGHT_GRAY_STAINED_GLASS_PANE,
                "<red><bold>✖ Remove Active</bold></red>",
                List.of("<gray>Active: " + previewStr, "",
                        hasActive ? "<red>Click to unequip." : "<dark_gray>Nothing equipped."),
                false));

        // ── fill cosmetic items ───────────────────────────────────────
        CrateReward.Type type = switch (tab) {
            case TAGS        -> CrateReward.Type.TAG;
            case NAME_COLORS -> CrateReward.Type.NAME_COLOR;
            case CHAT_COLORS -> CrateReward.Type.CHAT_COLOR;
        };
        Set<CrateReward> collection = switch (tab) {
            case TAGS        -> mgr.getUnlockedTags(player);
            case NAME_COLORS -> mgr.getUnlockedColors(player);
            case CHAT_COLORS -> mgr.getUnlockedChatColors(player);
        };

        List<CrateReward> all = new ArrayList<>();
        for (CrateReward r : CrateReward.values()) if (r.getType() == type) all.add(r);
        all.sort((a, b) -> {
            boolean ua = collection.contains(a), ub = collection.contains(b);
            if (ua != ub) return ua ? -1 : 1;
            return Integer.compare(b.getTier(), a.getTier());
        });

        for (int i = 0; i < CONTENT_SLOTS.length && i < all.size(); i++) {
            CrateReward r    = all.get(i);
            boolean unlocked = collection.contains(r);
            boolean isActive = active.isPresent() && active.get() == r;
            inv.setItem(CONTENT_SLOTS[i], buildCosmeticItem(r, unlocked, isActive, player));
        }

        player.openInventory(inv);
    }

    // ── item builders ─────────────────────────────────────────────────

    private ItemStack buildCosmeticItem(CrateReward r, boolean unlocked, boolean active, Player player) {
        Material mat = unlocked ? r.getIcon() : Material.GRAY_STAINED_GLASS_PANE;

        String previewRaw;
        if (r.isColor())     previewRaw = r.getCosmeticFormat().replace("{name}", player.getName());
        else if (r.isChatColor()) previewRaw = r.getCosmeticFormat().replace("{msg}", "Hello, world!");
        else                 previewRaw = r.getCosmeticFormat();

        List<String> lore = new ArrayList<>();
        lore.add("<gray>Rarity: " + r.tierLabel());
        lore.add("");
        if (unlocked) {
            lore.add("<gray>Preview:");
            lore.add("  " + previewRaw);
            lore.add("");
            lore.add(active ? "<green>✔ Equipped — Click to <red>unequip</red>" : "<yellow>► Click to equip!");
        } else {
            lore.add("<red>✘ Locked");
            lore.add("<dark_gray>Earn from crates.");
            lore.add("");
            lore.add("<dark_gray>Rarity hint: " + r.tierLabel());
        }

        String displayName;
        if (!unlocked) {
            displayName = "<dark_gray>??? " + r.getDisplayName();
        } else if (active) {
            displayName = "<gradient:#56ab2f:#a8e063><bold>✔ " + r.getDisplayName() + "</bold></gradient>";
        } else {
            displayName = "<white><bold>" + r.getDisplayName() + "</bold></white>";
        }

        return buildItem(mat, displayName, lore, active && unlocked);
    }

    private ItemStack tabBtn(boolean active, Material mat, String name, String hint) {
        return buildItem(mat, name, List.of(hint), active);
    }

    private ItemStack buildItem(Material mat, String name, List<String> loreStrings, boolean glow) {
        ItemStack item = new ItemStack(mat);
        ItemMeta meta = item.getItemMeta();
        meta.displayName(MessageUtil.parse("<!italic>" + name));
        List<Component> lore = new ArrayList<>();
        for (String l : loreStrings) {
            lore.add(l.isEmpty() ? Component.empty() : MessageUtil.parse("<!italic>" + l));
        }
        meta.lore(lore);
        if (glow) {
            meta.addEnchant(Enchantment.UNBREAKING, 1, true);
            meta.addItemFlags(ItemFlag.HIDE_ENCHANTS, ItemFlag.HIDE_ATTRIBUTES);
        } else {
            meta.addItemFlags(ItemFlag.HIDE_ATTRIBUTES);
        }
        item.setItemMeta(meta);
        return item;
    }

    private ItemStack filler() {
        ItemStack item = new ItemStack(Material.BLACK_STAINED_GLASS_PANE);
        ItemMeta meta = item.getItemMeta();
        meta.displayName(Component.empty());
        item.setItemMeta(meta);
        return item;
    }
}
