package de.louis.xdGens.gui;

import de.louis.xdGens.crate.CrateReward;
import de.louis.xdGens.main.Main;
import de.louis.xdGens.manager.PlayerCosmeticManager;
import de.louis.xdGens.util.MessageUtil;
import net.kyori.adventure.text.Component;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

import java.util.*;

public class CosmeticsGUI {

    public static final String BASE_TAGS        = "\u2746 Cosmetics \u00bb Tags";
    public static final String BASE_NAME_COLORS = "\u2746 Cosmetics \u00bb Name Colors";
    public static final String BASE_CHAT_COLORS = "\u2746 Cosmetics \u00bb Chat Colors";
    public static final String BASE_GLOW        = "\u2746 Cosmetics \u00bb Glow";

    public static final String TITLE_TAGS        = BASE_TAGS        + "|TAGS|0|RARITY_DESC";
    public static final String TITLE_NAME_COLORS = BASE_NAME_COLORS + "|NAME_COLORS|0|RARITY_DESC";
    public static final String TITLE_CHAT_COLORS = BASE_CHAT_COLORS + "|CHAT_COLORS|0|RARITY_DESC";
    public static final String TITLE_GLOW        = BASE_GLOW        + "|GLOW|0|RARITY_DESC";

    private static final int SIZE = 54;

    private static final int SLOT_TAB_TAGS        = 0;
    private static final int SLOT_TAB_NAME_COLORS = 1;
    private static final int SLOT_TAB_CHAT_COLORS = 2;
    private static final int SLOT_TAB_GLOW        = 3;
    public  static final int SLOT_FILTER          = 7;
    private static final int SLOT_UNEQUIP         = 8;

    public static final int[] CONTENT_SLOTS = {
        10, 11, 12, 13, 14, 15, 16,
        19, 20, 21, 22, 23, 24, 25,
        28, 29, 30, 31, 32, 33, 34
    };
    private static final int PAGE_SIZE = CONTENT_SLOTS.length;

    public static final int SLOT_PREV      = 39;
    public static final int SLOT_PAGE_INFO = 40;
    public static final int SLOT_NEXT      = 41;

    public enum Tab { TAGS, NAME_COLORS, CHAT_COLORS, GLOW }

    public enum Sort {
        RARITY_DESC("\uD83D\uDD3D Rarity: Highest First", Material.NETHER_STAR),
        RARITY_ASC ("\uD83D\uDD3C Rarity: Lowest First",  Material.STONE),
        NAME_AZ    ("\uD83D\uDD24 Name: A \u2192 Z",       Material.OAK_SIGN),
        NAME_ZA    ("\uD83D\uDD24 Name: Z \u2192 A",       Material.OAK_SIGN),
        NEWEST     ("\uD83D\uDD52 Newest First",           Material.CLOCK),
        OLDEST     ("\uD83D\uDD53 Oldest First",           Material.CLOCK),
        UNLOCKED   ("\u2714 Unlocked First",               Material.LIME_DYE);

        public final String label;
        public final Material icon;
        Sort(String label, Material icon) { this.label = label; this.icon = icon; }
    }

    private final Main plugin;

    public CosmeticsGUI(Main plugin) { this.plugin = plugin; }

    public void openTags(Player p)       { open(p, Tab.TAGS,        0, Sort.RARITY_DESC); }
    public void openColors(Player p)     { open(p, Tab.NAME_COLORS, 0, Sort.RARITY_DESC); }
    public void openChatColors(Player p) { open(p, Tab.CHAT_COLORS, 0, Sort.RARITY_DESC); }
    public void openGlow(Player p)       { open(p, Tab.GLOW,        0, Sort.RARITY_DESC); }
    public void open(Player p)           { open(p, Tab.TAGS,        0, Sort.RARITY_DESC); }
    public void open(Player p, Tab tab)  { open(p, tab,             0, Sort.RARITY_DESC); }

    public void open(Player player, Tab tab, int page, Sort sort) {
        String baseTitle = switch (tab) {
            case TAGS        -> BASE_TAGS;
            case NAME_COLORS -> BASE_NAME_COLORS;
            case CHAT_COLORS -> BASE_CHAT_COLORS;
            case GLOW        -> BASE_GLOW;
        };
        String encodedTitle = baseTitle + "|" + tab.name() + "|" + page + "|" + sort.name();
        Inventory inv = Bukkit.createInventory(null, SIZE, MessageUtil.parse(encodedTitle));

        PlayerCosmeticManager mgr = plugin.getPlayerCosmeticManager();

        ItemStack filler = filler();
        for (int i = 0; i < SIZE; i++) inv.setItem(i, filler);
        for (int s : CONTENT_SLOTS)    inv.setItem(s, null);
        for (int i = 36; i < 54; i++)  inv.setItem(i, filler);

        inv.setItem(SLOT_TAB_TAGS, tabBtn(
                tab == Tab.TAGS, Material.NAME_TAG,
                "<gradient:#7afcff:#00c2ff><bold>Chat Tags</bold></gradient>",
                tab == Tab.TAGS ? "<green>\u25ba Currently viewing" : "<gray>Click to switch"));
        inv.setItem(SLOT_TAB_NAME_COLORS, tabBtn(
                tab == Tab.NAME_COLORS, Material.GLOW_INK_SAC,
                "<gradient:#f6d365:#fda085><bold>Name Colors</bold></gradient>",
                tab == Tab.NAME_COLORS ? "<green>\u25ba Currently viewing" : "<gray>Click to switch"));
        inv.setItem(SLOT_TAB_CHAT_COLORS, tabBtn(
                tab == Tab.CHAT_COLORS, Material.BOOK,
                "<gradient:#c471f5:#fa71cd><bold>Chat Colors</bold></gradient>",
                tab == Tab.CHAT_COLORS ? "<green>\u25ba Currently viewing" : "<gray>Click to switch"));
        inv.setItem(SLOT_TAB_GLOW, tabBtn(
                tab == Tab.GLOW, Material.GLOWSTONE_DUST,
                "<gradient:#ffe259:#ffa751><bold>\u2728 Glow</bold></gradient>",
                tab == Tab.GLOW ? "<green>\u25ba Currently viewing" : "<gray>Click to switch"));

        for (int i = 4; i <= 6; i++) inv.setItem(i, filler());

        inv.setItem(SLOT_FILTER, buildItem(Material.HOPPER,
                "<yellow><bold>\uD83D\uDD0D Filter / Sort</bold></yellow>",
                List.of("<gray>Current: <white>" + sort.label, "",
                        "<yellow>Click to change sort order"),
                false));

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
                "<red><bold>\u2716 Remove Active</bold></red>",
                List.of("<gray>Active: " + previewStr, "",
                        hasActive ? "<red>Click to unequip." : "<dark_gray>Nothing equipped."),
                false));

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
        all.sort(buildComparator(sort, collection, player, mgr));

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

        inv.setItem(SLOT_PREV, safePage > 0
                ? buildItem(Material.ARROW, "<yellow><bold>\u25c4 Previous Page</bold></yellow>",
                            List.of("<gray>Page " + safePage + " of " + totalPages), false)
                : filler());
        inv.setItem(SLOT_PAGE_INFO, buildItem(Material.PAPER,
                "<white>Page " + (safePage + 1) + " / " + totalPages,
                List.of("<gray>" + all.size() + " cosmetics total"), false));
        inv.setItem(SLOT_NEXT, safePage < totalPages - 1
                ? buildItem(Material.ARROW, "<yellow><bold>Next Page \u25ba</bold></yellow>",
                            List.of("<gray>Page " + (safePage + 2) + " of " + totalPages), false)
                : filler());

        player.openInventory(inv);
    }

    // ── decode ────────────────────────────────────────────────────────

    public static TabPage decode(String plainTitle) {
        String[] parts = plainTitle.split("\\|");
        if (parts.length < 3) return null;
        try {
            if (parts.length >= 4) {
                Tab  tab  = Tab.valueOf(parts[parts.length - 3]);
                int  page = Integer.parseInt(parts[parts.length - 2]);
                Sort sort = Sort.valueOf(parts[parts.length - 1]);
                return new TabPage(tab, page, sort);
            } else {
                Tab tab  = Tab.valueOf(parts[parts.length - 2]);
                int page = Integer.parseInt(parts[parts.length - 1]);
                return new TabPage(tab, page, Sort.RARITY_DESC);
            }
        } catch (Exception e) { return null; }
    }

    public record TabPage(Tab tab, int page, Sort sort) {}

    // ── PUBLIC STATIC comparator (shared with Listener) ───────────────

    public static Comparator<CrateReward> buildComparator(Sort sort,
                                                           Set<CrateReward> collection,
                                                           Player player,
                                                           PlayerCosmeticManager mgr) {
        return switch (sort) {
            case RARITY_DESC -> (a, b) -> {
                boolean ua = collection.contains(a), ub = collection.contains(b);
                if (ua != ub) return ua ? -1 : 1;
                return Integer.compare(b.getTier(), a.getTier());
            };
            case RARITY_ASC -> (a, b) -> {
                boolean ua = collection.contains(a), ub = collection.contains(b);
                if (ua != ub) return ua ? -1 : 1;
                return Integer.compare(a.getTier(), b.getTier());
            };
            case NAME_AZ  -> Comparator.comparing(r -> plainName(r.getDisplayName()));
            case NAME_ZA  -> (a, b) -> plainName(b.getDisplayName()).compareTo(plainName(a.getDisplayName()));
            case NEWEST   -> (a, b) -> {
                long ta = mgr.getUnlockTimestamp(player, a);
                long tb = mgr.getUnlockTimestamp(player, b);
                if (ta == 0 && tb == 0) return 0;
                if (ta == 0) return 1;
                if (tb == 0) return -1;
                return Long.compare(tb, ta);
            };
            case OLDEST   -> (a, b) -> {
                long ta = mgr.getUnlockTimestamp(player, a);
                long tb = mgr.getUnlockTimestamp(player, b);
                if (ta == 0 && tb == 0) return 0;
                if (ta == 0) return 1;
                if (tb == 0) return -1;
                return Long.compare(ta, tb);
            };
            case UNLOCKED -> (a, b) -> {
                boolean ua = collection.contains(a), ub = collection.contains(b);
                if (ua != ub) return ua ? -1 : 1;
                return Integer.compare(b.getTier(), a.getTier());
            };
        };
    }

    public static String plainName(String s) {
        return s.replaceAll("[^\\p{L}\\p{N} ]", "").trim().toLowerCase(Locale.ROOT);
    }

    // ── item builders ─────────────────────────────────────────────────

    private ItemStack buildCosmeticItem(CrateReward r, boolean unlocked, boolean active, Player player) {
        Material mat = unlocked ? r.getIcon() : Material.GRAY_STAINED_GLASS_PANE;
        String previewRaw;
        if (r.isColor())          previewRaw = r.getCosmeticFormat().replace("{name}", player.getName());
        else if (r.isChatColor()) previewRaw = r.getCosmeticFormat().replace("{msg}", "Hello, world!");
        else if (r.isGlow()) {
            previewRaw = switch (r.getCosmeticFormat()) {
                case "PRISMATIC" -> "<gradient:#ff0000:#ff7700:#ffff00:#00ff00:#0000ff:#8b00ff>" + r.getDisplayName() + "</gradient>";
                case "AURORA"    -> "<gradient:#00d2ff:#a8ff3e:#c471f5:#1a6dff>" + r.getDisplayName() + "</gradient>";
                case "DIVINE"    -> "<gradient:#fffde4:#ffd200:#ff8c00>" + r.getDisplayName() + "</gradient>";
                case "INFERNO"   -> "<gradient:#8e0000:#ff4e00:#ffd200>" + r.getDisplayName() + "</gradient>";
                case "VOID"      -> "<gradient:#000000:#434343:#6a3093>" + r.getDisplayName() + "</gradient>";
                default          -> "<yellow>\u2728 " + r.getDisplayName() + "</yellow>";
            };
        } else {
            previewRaw = r.getCosmeticFormat();
        }

        List<String> lore = new ArrayList<>();
        lore.add("<gray>Rarity: " + r.tierLabel());
        if (r.isGlow()) {
            String cycleHint = switch (r.getCosmeticFormat()) {
                case "PRISMATIC" -> "<gradient:#ff0000:#ffff00:#00ff00:#0000ff:#8b00ff>\u2605 Cycles all rainbow colors!</gradient>";
                case "AURORA"    -> "<gradient:#00d2ff:#a8ff3e:#c471f5>\u2605 Cycles aurora colors!</gradient>";
                case "DIVINE"    -> "<gradient:#fffde4:#ffd200>\u2605 Cycles divine colors!</gradient>";
                case "INFERNO"   -> "<gradient:#8e0000:#ff4e00>\u2605 Cycles inferno colors!</gradient>";
                case "VOID"      -> "<gradient:#000000:#6a3093>\u2605 Cycles void colors!</gradient>";
                default          -> null;
            };
            if (cycleHint != null) lore.add(cycleHint);
        }
        lore.add("");
        if (unlocked) {
            lore.add("<gray>Preview:");
            lore.add("  " + previewRaw);
            lore.add("");
            lore.add(active ? "<green>\u2714 Equipped \u2014 Click to <red>unequip</red>" : "<yellow>\u25ba Click to equip!");
        } else {
            lore.add("<red>\u2718 Locked");
            lore.add("<dark_gray>Earn from crates.");
            lore.add("");
            lore.add("<dark_gray>Rarity hint: " + r.tierLabel());
        }
        return buildItem(mat, r.getDisplayName(), lore, active);
    }

    private ItemStack tabBtn(boolean active, Material mat, String name, String hint) {
        return buildItem(active ? Material.LIME_STAINED_GLASS_PANE : mat, name, List.of(hint), false);
    }

    public static ItemStack buildItem(Material mat, String name, List<String> lore, boolean enchanted) {
        ItemStack item = new ItemStack(mat);
        ItemMeta  meta = item.getItemMeta();
        meta.displayName(MessageUtil.parse(name));
        List<Component> loreCmp = new ArrayList<>();
        for (String l : lore) loreCmp.add(MessageUtil.parse(l));
        meta.lore(loreCmp);
        if (enchanted) {
            meta.addEnchant(org.bukkit.enchantments.Enchantment.LUCK, 1, true);
            meta.addItemFlags(org.bukkit.inventory.ItemFlag.HIDE_ENCHANTS);
        }
        item.setItemMeta(meta);
        return item;
    }

    public static ItemStack filler() {
        return buildItem(Material.GRAY_STAINED_GLASS_PANE, "<gray>\u00a0", List.of(), false);
    }
}
