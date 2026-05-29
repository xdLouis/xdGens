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
 * 4-tab cosmetics GUI with pagination.
 *
 * Row 0 (slots 0–8): tab buttons + unequip
 * Rows 1–3 (slots 9–35): 21 content slots (3×7 centered)
 * Row 4 (slots 36–44): prev / page-info / next + filler
 * Row 5 (slots 45–53): filler
 *
 * Title encoding: "<base>|<tab>|<page>"
 * e.g. "❆ Cosmetics » Tags|TAGS|0"
 */
public class CosmeticsGUI {

    public static final String BASE_TAGS        = "❆ Cosmetics » Tags";
    public static final String BASE_NAME_COLORS = "❆ Cosmetics » Name Colors";
    public static final String BASE_CHAT_COLORS = "❆ Cosmetics » Chat Colors";
    public static final String BASE_GLOW        = "❆ Cosmetics » Glow";

    // legacy title constants for the listener (page 0)
    public static final String TITLE_TAGS        = BASE_TAGS        + "|TAGS|0";
    public static final String TITLE_NAME_COLORS = BASE_NAME_COLORS + "|NAME_COLORS|0";
    public static final String TITLE_CHAT_COLORS = BASE_CHAT_COLORS + "|CHAT_COLORS|0";
    public static final String TITLE_GLOW        = BASE_GLOW        + "|GLOW|0";

    private static final int SIZE = 54;

    // top-row tab/control slots
    private static final int SLOT_TAB_TAGS        = 0;
    private static final int SLOT_TAB_NAME_COLORS = 1;
    private static final int SLOT_TAB_CHAT_COLORS = 2;
    private static final int SLOT_TAB_GLOW        = 3;
    private static final int SLOT_UNEQUIP         = 8;

    // 3 rows × 7 centered content slots
    public static final int[] CONTENT_SLOTS = {
        10, 11, 12, 13, 14, 15, 16,
        19, 20, 21, 22, 23, 24, 25,
        28, 29, 30, 31, 32, 33, 34
    };
    private static final int PAGE_SIZE = CONTENT_SLOTS.length; // 21

    // bottom navigation row (row 4)
    private static final int SLOT_PREV = 39;
    private static final int SLOT_PAGE_INFO = 40;
    private static final int SLOT_NEXT = 41;

    public enum Tab { TAGS, NAME_COLORS, CHAT_COLORS, GLOW }

    private final Main plugin;

    public CosmeticsGUI(Main plugin) { this.plugin = plugin; }

    public void openTags(Player p)       { open(p, Tab.TAGS, 0); }
    public void openColors(Player p)     { open(p, Tab.NAME_COLORS, 0); }
    public void openChatColors(Player p) { open(p, Tab.CHAT_COLORS, 0); }
    public void openGlow(Player p)       { open(p, Tab.GLOW, 0); }
    public void open(Player p)           { open(p, Tab.TAGS, 0); }
    public void open(Player p, Tab tab)  { open(p, tab, 0); }

    public void open(Player player, Tab tab, int page) {
        String baseTitle = switch (tab) {
            case TAGS        -> BASE_TAGS;
            case NAME_COLORS -> BASE_NAME_COLORS;
            case CHAT_COLORS -> BASE_CHAT_COLORS;
            case GLOW        -> BASE_GLOW;
        };
        // encode tab + page into the inventory title so the listener can decode it
        String encodedTitle = baseTitle + "|" + tab.name() + "|" + page;
        Inventory inv = Bukkit.createInventory(null, SIZE, MessageUtil.parse(encodedTitle));

        PlayerCosmeticManager mgr = plugin.getPlayerCosmeticManager();

        // background fill
        ItemStack filler = filler();
        for (int i = 0; i < SIZE; i++) inv.setItem(i, filler);
        for (int s : CONTENT_SLOTS)    inv.setItem(s, null);
        for (int i = 36; i < 54; i++)  inv.setItem(i, filler);

        // ── tab buttons ─────────────────────────────────────────────
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
        inv.setItem(SLOT_TAB_GLOW, tabBtn(
                tab == Tab.GLOW, Material.GLOWSTONE_DUST,
                "<gradient:#ffe259:#ffa751><bold>✨ Glow</bold></gradient>",
                tab == Tab.GLOW ? "<green>► Currently viewing" : "<gray>Click to switch"));

        for (int i = 4; i <= 7; i++) inv.setItem(i, filler());

        // ── unequip button ───────────────────────────────────────────
        Optional<CrateReward> active = switch (tab) {
            case TAGS        -> mgr.getActiveTag(player);
            case NAME_COLORS -> mgr.getActiveColor(player);
            case CHAT_COLORS -> mgr.getActiveChatColor(player);
            case GLOW        -> mgr.getActiveGlow(player);
        };
        boolean hasActive = active.isPresent();
        String previewStr = active.map(r -> {
            if (r.isColor())     return r.getCosmeticFormat().replace("{name}", player.getName());
            if (r.isChatColor()) return r.getCosmeticFormat().replace("{msg}", "Hello!");
            if (r.isGlow())      return "<yellow>" + r.getDisplayName() + "</yellow>";
            return r.getCosmeticFormat();
        }).orElse("<dark_gray>None");

        inv.setItem(SLOT_UNEQUIP, buildItem(
                hasActive ? Material.BARRIER : Material.LIGHT_GRAY_STAINED_GLASS_PANE,
                "<red><bold>✖ Remove Active</bold></red>",
                List.of("<gray>Active: " + previewStr, "",
                        hasActive ? "<red>Click to unequip." : "<dark_gray>Nothing equipped."),
                false));

        // ── collect + sort cosmetics ─────────────────────────────────
        CrateReward.Type type = switch (tab) {
            case TAGS        -> CrateReward.Type.TAG;
            case NAME_COLORS -> CrateReward.Type.NAME_COLOR;
            case CHAT_COLORS -> CrateReward.Type.CHAT_COLOR;
            case GLOW        -> CrateReward.Type.GLOW;
        };
        Set<CrateReward> collection = switch (tab) {
            case TAGS        -> mgr.getUnlockedTags(player);
            case NAME_COLORS -> mgr.getUnlockedColors(player);
            case CHAT_COLORS -> mgr.getUnlockedChatColors(player);
            case GLOW        -> mgr.getUnlockedGlows(player);
        };

        List<CrateReward> all = new ArrayList<>();
        for (CrateReward r : CrateReward.values()) if (r.getType() == type) all.add(r);
        all.sort((a, b) -> {
            boolean ua = collection.contains(a), ub = collection.contains(b);
            if (ua != ub) return ua ? -1 : 1;
            return Integer.compare(b.getTier(), a.getTier());
        });

        int totalPages = Math.max(1, (int) Math.ceil((double) all.size() / PAGE_SIZE));
        int safePage   = Math.max(0, Math.min(page, totalPages - 1));
        int start      = safePage * PAGE_SIZE;
        int end        = Math.min(start + PAGE_SIZE, all.size());

        for (int i = start; i < end; i++) {
            CrateReward r    = all.get(i);
            boolean unlocked = collection.contains(r);
            boolean isActive = active.isPresent() && active.get() == r;
            inv.setItem(CONTENT_SLOTS[i - start], buildCosmeticItem(r, unlocked, isActive, player));
        }

        // ── pagination row ───────────────────────────────────────────
        if (safePage > 0) {
            inv.setItem(SLOT_PREV, buildItem(Material.ARROW,
                    "<yellow><bold>◀ Previous Page</bold></yellow>",
                    List.of("<gray>Page " + safePage + " of " + totalPages), false));
        } else {
            inv.setItem(SLOT_PREV, filler());
        }

        inv.setItem(SLOT_PAGE_INFO, buildItem(Material.PAPER,
                "<white>Page " + (safePage + 1) + " / " + totalPages,
                List.of("<gray>" + all.size() + " cosmetics total"), false));

        if (safePage < totalPages - 1) {
            inv.setItem(SLOT_NEXT, buildItem(Material.ARROW,
                    "<yellow><bold>Next Page ►</bold></yellow>",
                    List.of("<gray>Page " + (safePage + 2) + " of " + totalPages), false));
        } else {
            inv.setItem(SLOT_NEXT, filler());
        }

        player.openInventory(inv);
    }

    // ── helper: decode title back to tab+page for the click listener ──

    /** Returns null if the title doesn't belong to this GUI. */
    public static TabPage decode(String plainTitle) {
        String[] parts = plainTitle.split("\\|");
        if (parts.length < 3) return null;
        try {
            Tab  tab  = Tab.valueOf(parts[parts.length - 2]);
            int  page = Integer.parseInt(parts[parts.length - 1]);
            return new TabPage(tab, page);
        } catch (Exception e) { return null; }
    }

    public record TabPage(Tab tab, int page) {}

    // ── item builders ─────────────────────────────────────────────

    private ItemStack buildCosmeticItem(CrateReward r, boolean unlocked, boolean active, Player player) {
        Material mat = unlocked ? r.getIcon() : Material.GRAY_STAINED_GLASS_PANE;

        String previewRaw;
        if (r.isColor())          previewRaw = r.getCosmeticFormat().replace("{name}", player.getName());
        else if (r.isChatColor()) previewRaw = r.getCosmeticFormat().replace("{msg}", "Hello, world!");
        else if (r.isGlow()) {
            // Show a coloured preview hint for special cycling glows
            previewRaw = switch (r.getCosmeticFormat()) {
                case "PRISMATIC" -> "<gradient:#ff0000:#ff7700:#ffff00:#00ff00:#0000ff:#8b00ff>" + r.getDisplayName() + "</gradient>";
                case "AURORA"    -> "<gradient:#00d2ff:#a8ff3e:#c471f5:#1a6dff>" + r.getDisplayName() + "</gradient>";
                case "DIVINE"    -> "<gradient:#fffde4:#ffd200:#ff8c00>" + r.getDisplayName() + "</gradient>";
                case "INFERNO"   -> "<gradient:#8e0000:#ff4e00:#ffd200>" + r.getDisplayName() + "</gradient>";
                case "VOID"      -> "<gradient:#000000:#434343:#6a3093>" + r.getDisplayName() + "</gradient>";
                default          -> "<yellow>✨ " + r.getDisplayName() + "</yellow>";
            };
        } else {
            previewRaw = r.getCosmeticFormat();
        }

        List<String> lore = new ArrayList<>();
        lore.add("<gray>Rarity: " + r.tierLabel());
        if (r.isGlow()) {
            String cycleHint = switch (r.getCosmeticFormat()) {
                case "PRISMATIC" -> "<gradient:#ff0000:#ffff00:#00ff00:#0000ff:#8b00ff>★ Cycles all rainbow colors!</gradient>";
                case "AURORA"    -> "<gradient:#00d2ff:#a8ff3e:#c471f5>★ Cycles aurora colors!</gradient>";
                case "DIVINE"    -> "<gradient:#fffde4:#ffd200>★ Cycles divine colors!</gradient>";
                case "INFERNO"   -> "<gradient:#8e0000:#ff4e00>★ Cycles inferno colors!</gradient>";
                case "VOID"      -> "<gradient:#000000:#6a3093>★ Cycles void colors!</gradient>";
                default          -> null;
            };
            if (cycleHint != null) lore.add(cycleHint);
        }
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
