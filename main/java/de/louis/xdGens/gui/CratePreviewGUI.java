package de.louis.xdGens.gui;

import de.louis.xdGens.crate.CrateReward;
import de.louis.xdGens.crate.CrateType;
import de.louis.xdGens.crate.PouchTier;
import de.louis.xdGens.crate.PouchType;
import de.louis.xdGens.main.Main;
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

import java.util.*;
import java.util.stream.Collectors;

/**
 * Crate Preview GUI — 6 rows (54 slots)
 *
 * Layout (by row):
 *   Row 0 (0-8):   Category tabs / header
 *   Row 1-4 (9-44): Reward items grouped by category, paginated
 *   Row 5 (45-53): Navigation bar
 *
 * Navigation bar slots:
 *   45 — prev page
 *   46 — category: POUCHES
 *   47 — category: TAGS
 *   48 — category: NAME COLORS
 *   49 — BACK button
 *   50 — category: CHAT COLORS
 *   51 — category: GLOW
 *   52 — empty
 *   53 — next page
 *
 * Categories cycle on tab click. Pagination within each category.
 *
 * State is stored in two static maps keyed by player UUID.
 */
public class CratePreviewGUI {

    public static final String TITLE_PREFIX = "\uD83D\uDD0D ";

    // ── per-player state ─────────────────────────────────────────────────
    private static final Map<UUID, Category> playerCategory = new HashMap<>();
    private static final Map<UUID, Integer>  playerPage     = new HashMap<>();

    public enum Category {
        POUCHES("\uD83D\uDCB0 Pouches",     Material.EMERALD),
        TAGS("\uD83C\uDFF7 Tags",            Material.NAME_TAG),
        NAME_COLORS("\uD83C\uDFA8 Name Colors", Material.CYAN_DYE),
        CHAT_COLORS("\uD83D\uDCAC Chat Colors", Material.PURPLE_DYE),
        GLOW("\u2728 Glow",                  Material.GLOWSTONE_DUST);

        public final String label;
        public final Material icon;
        Category(String label, Material icon) { this.label = label; this.icon = icon; }
    }

    private static final int CONTENT_START = 9;
    private static final int CONTENT_END   = 45; // exclusive
    private static final int CONTENT_SIZE  = CONTENT_END - CONTENT_START; // 36 slots

    // nav bar slots
    private static final int SLOT_PREV    = 45;
    private static final int SLOT_CAT_1   = 46; // POUCHES
    private static final int SLOT_CAT_2   = 47; // TAGS
    private static final int SLOT_CAT_3   = 48; // NAME_COLORS
    private static final int SLOT_BACK    = 49;
    private static final int SLOT_CAT_4   = 50; // CHAT_COLORS
    private static final int SLOT_CAT_5   = 51; // GLOW
    private static final int SLOT_NEXT    = 53;

    private static final int[] CAT_SLOTS = {SLOT_CAT_1, SLOT_CAT_2, SLOT_CAT_3, SLOT_CAT_4, SLOT_CAT_5};

    // ── instance ─────────────────────────────────────────────────────────
    private final Main      plugin;
    private final CrateType crateType;

    public CratePreviewGUI(Main plugin, CrateType crateType) {
        this.plugin    = plugin;
        this.crateType = crateType;
    }

    // ── open (preserves category/page state) ────────────────────────────
    public void open(Player player) {
        Category cat  = playerCategory.getOrDefault(player.getUniqueId(), Category.POUCHES);
        int      page = playerPage.getOrDefault(player.getUniqueId(), 0);
        render(player, cat, page);
    }

    public void open(Player player, Category cat, int page) {
        playerCategory.put(player.getUniqueId(), cat);
        playerPage.put(player.getUniqueId(), page);
        render(player, cat, page);
    }

    // ── render ───────────────────────────────────────────────────────────
    private void render(Player player, Category cat, int page) {
        String title = crateType.getGradient() + TITLE_PREFIX + crateType.getDisplayName() + " Crate</gradient>";
        Inventory inv = Bukkit.createInventory(null, 54, MessageUtil.parse(title));

        // fill content area
        ItemStack pane = glassPaneFor(crateType);
        for (int i = 0; i < 54; i++) inv.setItem(i, pane.clone());

        // content
        List<ItemStack> items = buildItems(cat);
        int totalPages = Math.max(1, (int) Math.ceil(items.size() / (double) CONTENT_SIZE));
        page = Math.max(0, Math.min(page, totalPages - 1));

        int start = page * CONTENT_SIZE;
        int end   = Math.min(start + CONTENT_SIZE, items.size());
        for (int i = start; i < end; i++) {
            inv.setItem(CONTENT_START + (i - start), items.get(i));
        }

        // header row — show current page
        placeHeader(inv, cat, page, totalPages);

        // nav bar
        placeNavBar(inv, cat, page, totalPages);

        player.openInventory(inv);
    }

    // ── header row (row 0, slots 0-8) ────────────────────────────────────
    private void placeHeader(Inventory inv, Category cat, int page, int totalPages) {
        // slot 0: crate icon with summary
        ItemStack crateIcon = new ItemStack(crateType.getIcon());
        ItemMeta  cm = crateIcon.getItemMeta();
        cm.displayName(MessageUtil.parse(
                crateType.getGradient() + "<bold>\u2746 " + crateType.getDisplayName() + " Crate \u2746</bold></gradient>"
        ));
        List<Component> cl = new ArrayList<>();
        cl.add(Component.empty());
        cl.add(MessageUtil.parse("<gray>Cosmetic drop rate: <gold>" + String.format("%.1f", baseDrop() * 100.0) + "%</gold> per open"));
        cl.add(MessageUtil.parse("<gray>Category: <white>" + cat.label + "</white>"));
        if (totalPages > 1)
            cl.add(MessageUtil.parse("<gray>Page: <white>" + (page + 1) + "</white><gray>/" + totalPages));
        cm.lore(cl);
        cm.addItemFlags(ItemFlag.HIDE_ATTRIBUTES);
        crateIcon.setItemMeta(cm);
        inv.setItem(0, crateIcon);

        // slots 1-7: tier legend
        int[] legendSlots = {1, 2, 3, 5, 6, 7};
        String[][] legends = {
            {"<gray>\u25cf Common</gray>",      "gray"},
            {"<gradient:#7afcff:#00c2ff>\u25cf Rare</gradient>",       "blue"},
            {"<gradient:#c471f5:#fa71cd>\u25cf Very Rare</gradient>",  "purple"},
            {"<gradient:#f6d365:#fda085><bold>\u2746 LEGENDARY \u2746</bold></gradient>", "orange"},
            {"<green>\u25cf Economy Pouch</green>", "green"},
            {"<white>click category tabs below</white>", "white"}
        };
        for (int i = 0; i < legendSlots.length && i < legends.length; i++) {
            ItemStack leg = new ItemStack(Material.PAPER);
            ItemMeta  lm  = leg.getItemMeta();
            lm.displayName(MessageUtil.parse(legends[i][0]));
            lm.addItemFlags(ItemFlag.HIDE_ATTRIBUTES);
            leg.setItemMeta(lm);
            inv.setItem(legendSlots[i], leg);
        }

        // slot 4: currently selected category summary
        ItemStack selItem = new ItemStack(cat.icon);
        ItemMeta  sm = selItem.getItemMeta();
        sm.displayName(MessageUtil.parse("<white><bold>" + cat.label + "</bold></white>"));
        sm.addEnchant(Enchantment.UNBREAKING, 1, true);
        sm.addItemFlags(ItemFlag.HIDE_ENCHANTS, ItemFlag.HIDE_ATTRIBUTES);
        selItem.setItemMeta(sm);
        inv.setItem(4, selItem);

        // slot 8: blank pane
    }

    // ── nav bar (row 5, slots 45-53) ─────────────────────────────────────
    private void placeNavBar(Inventory inv, Category cat, int page, int totalPages) {
        // prev page
        if (page > 0) {
            inv.setItem(SLOT_PREV, navButton(Material.ARROW,
                    "<yellow>\u2190 Previous Page",
                    "<gray>Page " + page + " / " + totalPages));
        }

        // category tabs
        Category[] cats = Category.values();
        for (int i = 0; i < cats.length && i < CAT_SLOTS.length; i++) {
            Category c = cats[i];
            boolean  selected = c == cat;
            ItemStack tab = new ItemStack(c.icon);
            ItemMeta  tm  = tab.getItemMeta();
            tm.displayName(MessageUtil.parse(
                    selected ? "<white><bold>" + c.label + "</bold></white>"
                             : "<gray>" + c.label + "</gray>"
            ));
            if (selected) {
                tm.addEnchant(Enchantment.UNBREAKING, 1, true);
                tm.addItemFlags(ItemFlag.HIDE_ENCHANTS);
            }
            tm.addItemFlags(ItemFlag.HIDE_ATTRIBUTES);
            tab.setItemMeta(tm);
            inv.setItem(CAT_SLOTS[i], tab);
        }

        // back button
        inv.setItem(SLOT_BACK, navButton(Material.BARRIER,
                "<red>\u2190 Back to Crates",
                "<gray>Return to crates menu"));

        // next page
        if (page < totalPages - 1) {
            inv.setItem(SLOT_NEXT, navButton(Material.ARROW,
                    "<yellow>Next Page \u2192",
                    "<gray>Page " + (page + 2) + " / " + totalPages));
        }
    }

    // ── item lists per category ───────────────────────────────────────────
    private List<ItemStack> buildItems(Category cat) {
        return switch (cat) {
            case POUCHES     -> buildPouchItems();
            case TAGS        -> buildRewardItems(CrateReward.Type.TAG);
            case NAME_COLORS -> buildRewardItems(CrateReward.Type.NAME_COLOR);
            case CHAT_COLORS -> buildRewardItems(CrateReward.Type.CHAT_COLOR);
            case GLOW        -> buildRewardItems(CrateReward.Type.GLOW);
        };
    }

    // ── POUCHES tab ───────────────────────────────────────────────────────
    private List<ItemStack> buildPouchItems() {
        List<ItemStack> items = new ArrayList<>();
        double drop = baseDrop();

        // Section header: Economy Pouches
        items.add(sectionHeader("\uD83D\uDCB0 Economy Pouches", "<gray>You always receive pouches when opening a crate."));

        // Money/XP/Tokens pouch for each PouchTier — show which tiers this crate gives
        PouchTier[] tiersForCrate = tiersForCrate();
        for (PouchType pType : PouchType.values()) {
            for (PouchTier tier : tiersForCrate) {
                items.add(buildPouchPreviewItem(pType, tier, drop));
            }
        }

        // Section header: Cosmetic Vouchers
        items.add(sectionHeader("\uD83C\uDF9F Cosmetic Vouchers", "<gray>Chance to drop a cosmetic voucher on crate open."));
        items.add(buildCosmeticChanceItem(drop));

        return items;
    }

    private ItemStack buildPouchPreviewItem(PouchType pType, PouchTier tier, double baseDrop) {
        Material mat = switch (pType) {
            case MONEY  -> Material.EMERALD;
            case XP     -> Material.EXPERIENCE_BOTTLE;
            case TOKENS -> Material.GOLD_INGOT;
        };
        String typeName = switch (pType) {
            case MONEY  -> "Money Pouch";
            case XP     -> "XP Pouch";
            case TOKENS -> "Token Pouch";
        };

        ItemStack item = new ItemStack(mat);
        ItemMeta  meta = item.getItemMeta();
        meta.displayName(MessageUtil.parse(tier.getColorTag() + "<bold>" + tier.name() + " " + typeName + "</bold>" +
                (tier.getColorTag().startsWith("<gradient") ? "</gradient>" : tier.getColorTag().replace("<", "</"))));

        List<Component> lore = new ArrayList<>();
        lore.add(Component.empty());
        lore.add(MessageUtil.parse("<gray>Tier: " + tier.getDisplayName()));
        lore.add(MessageUtil.parse("<gray>Multiplier: <white>x" + tier.getMultiplier() + "</white>"));
        lore.add(Component.empty());
        lore.add(MessageUtil.parse("<dark_gray>Auto-redeemed when crate opens."));
        meta.lore(lore);
        meta.addItemFlags(ItemFlag.HIDE_ATTRIBUTES);
        item.setItemMeta(meta);
        return item;
    }

    private ItemStack buildCosmeticChanceItem(double baseDrop) {
        ItemStack item = new ItemStack(Material.PAPER);
        ItemMeta  meta = item.getItemMeta();
        meta.displayName(MessageUtil.parse("<gradient:#c471f5:#fa71cd><bold>\uD83C\uDF9F Cosmetic Voucher</bold></gradient>"));
        List<Component> lore = new ArrayList<>();
        lore.add(Component.empty());
        lore.add(MessageUtil.parse("<gray>Drop chance: <gold>" + String.format("%.1f", baseDrop * 100.0) + "%</gold> per open"));
        lore.add(MessageUtil.parse("<gray>If dropped, rewards a random cosmetic"));
        lore.add(MessageUtil.parse("<gray>from the Tags, Name-Colors, Chat-Colors"));
        lore.add(MessageUtil.parse("<gray>or Glow category."));
        lore.add(Component.empty());
        lore.add(MessageUtil.parse("<dark_gray>Check other tabs to see exact odds."));
        meta.lore(lore);
        meta.addItemFlags(ItemFlag.HIDE_ATTRIBUTES);
        item.setItemMeta(meta);
        return item;
    }

    // ── cosmetic reward items ─────────────────────────────────────────────
    private List<ItemStack> buildRewardItems(CrateReward.Type type) {
        List<CrateReward> pool = getPool(type);
        double poolTotal = pool.stream().mapToDouble(CrateReward::getWeight).sum();
        double drop      = baseDrop();

        // Sort: tier asc, then weight desc
        pool.sort(Comparator.comparingInt(CrateReward::getTier).thenComparingDouble(r -> -r.getWeight()));

        List<ItemStack> items = new ArrayList<>();
        int lastTier = -1;
        for (CrateReward reward : pool) {
            if (reward.getTier() != lastTier) {
                lastTier = reward.getTier();
                items.add(tierDivider(reward.getTier()));
            }
            double chance = drop * (reward.getWeight() / poolTotal) * 100.0;
            items.add(buildRewardItem(reward, chance));
        }
        return items;
    }

    private ItemStack buildRewardItem(CrateReward reward, double chancePercent) {
        ItemStack item = new ItemStack(reward.getIcon());
        ItemMeta  meta = item.getItemMeta();
        meta.displayName(MessageUtil.parse(reward.tierLabel() + " <white>" + reward.getDisplayName() + "</white>"));
        List<Component> lore = new ArrayList<>();
        lore.add(Component.empty());
        lore.add(MessageUtil.parse("<gray>Rarity: " + reward.tierLabel()));
        lore.add(MessageUtil.parse("<gray>Drop chance: <gold>" + String.format("%.4f", chancePercent) + "%</gold>"));
        lore.add(Component.empty());
        String preview = reward.getCosmeticFormat()
                .replace("{name}", "Steve")
                .replace("{msg}",  "Hello!");
        lore.add(MessageUtil.parse("<dark_gray>Preview: " + preview));
        meta.lore(lore);
        meta.addEnchant(Enchantment.UNBREAKING, 1, true);
        meta.addItemFlags(ItemFlag.HIDE_ENCHANTS, ItemFlag.HIDE_ATTRIBUTES);
        item.setItemMeta(meta);
        return item;
    }

    // ── pool helpers ──────────────────────────────────────────────────────
    private List<CrateReward> getPool(CrateReward.Type type) {
        int maxTier = switch (crateType) {
            case COMMON, UNCOMMON -> CrateReward.TIER_RARE;
            case RARE, EPIC       -> CrateReward.TIER_VERY_RARE;
            case LEGENDARY        -> CrateReward.TIER_LEGENDARY;
        };
        List<CrateReward> list = new ArrayList<>();
        for (CrateReward r : CrateReward.values()) {
            if (r.getType() == type && r.getTier() <= maxTier) list.add(r);
        }
        return list;
    }

    private PouchTier[] tiersForCrate() {
        return switch (crateType) {
            case COMMON    -> new PouchTier[]{PouchTier.T1, PouchTier.T2};
            case UNCOMMON  -> new PouchTier[]{PouchTier.T1, PouchTier.T2, PouchTier.T3};
            case RARE      -> new PouchTier[]{PouchTier.T2, PouchTier.T3};
            case EPIC      -> new PouchTier[]{PouchTier.T3, PouchTier.T4};
            case LEGENDARY -> new PouchTier[]{PouchTier.T4, PouchTier.T5};
        };
    }

    private double baseDrop() {
        return switch (crateType) {
            case COMMON    -> 0.008;
            case UNCOMMON  -> 0.015;
            case RARE      -> 0.035;
            case EPIC      -> 0.070;
            case LEGENDARY -> 0.140;
        };
    }

    // ── section headers / dividers ────────────────────────────────────────
    private ItemStack sectionHeader(String title, String subtitle) {
        ItemStack item = new ItemStack(Material.LIME_STAINED_GLASS_PANE);
        ItemMeta  meta = item.getItemMeta();
        meta.displayName(MessageUtil.parse("<green><bold>" + title + "</bold></green>"));
        List<Component> lore = new ArrayList<>();
        lore.add(MessageUtil.parse(subtitle));
        meta.lore(lore);
        meta.addItemFlags(ItemFlag.HIDE_ATTRIBUTES);
        item.setItemMeta(meta);
        return item;
    }

    private ItemStack tierDivider(int tier) {
        String label = switch (tier) {
            case 0 -> "<gray>\u2500\u2500\u2500\u2500 Common \u2500\u2500\u2500\u2500</gray>";
            case 1 -> "<gradient:#7afcff:#00c2ff>\u2500\u2500\u2500\u2500 Rare \u2500\u2500\u2500\u2500</gradient>";
            case 2 -> "<gradient:#c471f5:#fa71cd>\u2500\u2500\u2500\u2500 Very Rare \u2500\u2500\u2500\u2500</gradient>";
            case 3 -> "<gradient:#f6d365:#fda085><bold>\u2500\u2500\u2500\u2500 \u2746 LEGENDARY \u2746 \u2500\u2500\u2500\u2500</bold></gradient>";
            default -> "<white>\u2500\u2500\u2500\u2500\u2500\u2500\u2500\u2500</white>";
        };
        Material mat = switch (tier) {
            case 1 -> Material.LIGHT_BLUE_STAINED_GLASS_PANE;
            case 2 -> Material.PURPLE_STAINED_GLASS_PANE;
            case 3 -> Material.ORANGE_STAINED_GLASS_PANE;
            default -> Material.WHITE_STAINED_GLASS_PANE;
        };
        ItemStack item = new ItemStack(mat);
        ItemMeta  meta = item.getItemMeta();
        meta.displayName(MessageUtil.parse(label));
        meta.addItemFlags(ItemFlag.HIDE_ATTRIBUTES);
        item.setItemMeta(meta);
        return item;
    }

    private ItemStack navButton(Material mat, String name, String loreText) {
        ItemStack item = new ItemStack(mat);
        ItemMeta  meta = item.getItemMeta();
        meta.displayName(MessageUtil.parse(name));
        meta.lore(List.of(MessageUtil.parse(loreText)));
        meta.addItemFlags(ItemFlag.HIDE_ATTRIBUTES);
        item.setItemMeta(meta);
        return item;
    }

    private ItemStack glassPaneFor(CrateType type) {
        Material mat = switch (type) {
            case COMMON    -> Material.WHITE_STAINED_GLASS_PANE;
            case UNCOMMON  -> Material.LIME_STAINED_GLASS_PANE;
            case RARE      -> Material.LIGHT_BLUE_STAINED_GLASS_PANE;
            case EPIC      -> Material.PURPLE_STAINED_GLASS_PANE;
            case LEGENDARY -> Material.ORANGE_STAINED_GLASS_PANE;
        };
        ItemStack p = new ItemStack(mat);
        ItemMeta  m = p.getItemMeta();
        m.displayName(Component.empty());
        p.setItemMeta(m);
        return p;
    }

    // ── static title resolver ─────────────────────────────────────────────
    public static CrateType resolveFromTitle(String title) {
        for (CrateType t : CrateType.values()) {
            if (title.contains(t.getDisplayName() + " Crate")) return t;
        }
        return null;
    }

    // ── state cleanup ─────────────────────────────────────────────────────
    public static void clearState(UUID uuid) {
        playerCategory.remove(uuid);
        playerPage.remove(uuid);
    }
}
