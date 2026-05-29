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

/**
 * Crate Preview GUI  —  6 Reihen (54 Slots)
 *
 * Layout:
 *   Slot 0-8  (Reihe 0): Header-Leiste
 *     [CrateIcon] [PouchInfo] [Sep] [Sep] [Sep] [Sep] [Sep] [Sep] [Back]
 *
 *   Slot 9-44 (Reihe 1-4): Reward-Items  (36 Content-Slots, paginiert)
 *
 *   Slot 45-53 (Reihe 5): Footer
 *     [Prev] [filler x7] [Next]
 *
 * Alle Rewards einer Crate auf einer Seite (max 36 pro Seite), nach Kategorie sortiert:
 *   Zuerst alle TAGs, dann NAME_COLORS, CHAT_COLORS, GLOW — jeweils mit einem
 *   einzelnen Glasscheiben-Trenner am Anfang jeder Kategorie.
 *   Tiers innerhalb einer Kategorie aufsteigend: Common → Rare → Very Rare → Legendary.
 */
public class CratePreviewGUI {

    // Navigation-Slots
    public static final int SLOT_BACK = 8;
    public static final int SLOT_PREV = 45;
    public static final int SLOT_NEXT = 53;

    private static final int CONTENT_START = 9;
    private static final int CONTENT_SIZE  = 36;  // Slots 9-44

    // Per-Player State
    public static final Map<UUID, Integer> playerPage = new HashMap<>();

    private final Main      plugin;
    private final CrateType crateType;

    public CratePreviewGUI(Main plugin, CrateType crateType) {
        this.plugin    = plugin;
        this.crateType = crateType;
    }

    // ── open ──────────────────────────────────────────────────────

    public void open(Player player) {
        open(player, playerPage.getOrDefault(player.getUniqueId(), 0));
    }

    public void open(Player player, int page) {
        List<ItemStack> content = buildContent();
        int totalPages = Math.max(1, (int) Math.ceil(content.size() / (double) CONTENT_SIZE));
        page = Math.max(0, Math.min(page, totalPages - 1));
        playerPage.put(player.getUniqueId(), page);

        // Titel: z.B. "🔍 Common Crate"
        Inventory inv = Bukkit.createInventory(
                null, 54,
                MessageUtil.parse("🔍 " + crateType.getDisplayName() + " Crate")
        );

        // Alles mit passender Glasscheibe füllen
        ItemStack bg = bgPane(crateType);
        for (int i = 0; i < 54; i++) inv.setItem(i, bg.clone());

        // Header (Reihe 0)
        inv.setItem(0, buildCrateHeader());
        inv.setItem(1, buildPouchInfo());
        inv.setItem(SLOT_BACK, buildBack());
        // Slots 2-7: Seiten-Info (falls mehr als 1 Seite)
        if (totalPages > 1) {
            inv.setItem(4, buildPageInfo(page, totalPages));
        }

        // Content
        int from = page * CONTENT_SIZE;
        int to   = Math.min(from + CONTENT_SIZE, content.size());
        for (int i = from; i < to; i++) {
            inv.setItem(CONTENT_START + (i - from), content.get(i));
        }

        // Footer Navigation
        if (page > 0)             inv.setItem(SLOT_PREV, navArrow("<yellow>\u2190 Zurück",    page,     totalPages));
        if (page < totalPages - 1) inv.setItem(SLOT_NEXT, navArrow("<yellow>Weiter \u2192", page + 2, totalPages));

        player.openInventory(inv);
    }

    // ── Content-Liste aufbauen ───────────────────────────────────────

    private List<ItemStack> buildContent() {
        List<ItemStack> list = new ArrayList<>();
        int maxTier = maxTierForCrate();

        // Kategorien in Reihenfolge: TAG → NAME_COLOR → CHAT_COLOR → GLOW
        CrateReward.Type[] order = {
            CrateReward.Type.TAG,
            CrateReward.Type.NAME_COLOR,
            CrateReward.Type.CHAT_COLOR,
            CrateReward.Type.GLOW
        };

        // Pool aller Cosmetics dieser Crate (ohne POUCH)
        // Gesamtgewicht für Wahrscheinlichkeitsberechnung
        List<CrateReward> allCosmetics = new ArrayList<>();
        for (CrateReward.Type t : order) {
            for (CrateReward r : CrateReward.values()) {
                if (r.getType() == t && r.getTier() <= maxTier) allCosmetics.add(r);
            }
        }
        double totalCosmeticWeight = allCosmetics.stream().mapToDouble(CrateReward::getWeight).sum();
        double dropChance = baseDrop();

        // Rewards pro Kategorie einfügen
        for (CrateReward.Type cat : order) {
            List<CrateReward> pool = new ArrayList<>();
            for (CrateReward r : CrateReward.values()) {
                if (r.getType() == cat && r.getTier() <= maxTier) pool.add(r);
            }
            if (pool.isEmpty()) continue;

            // Sortierung: Tier asc, Weight desc
            pool.sort(Comparator.comparingInt(CrateReward::getTier)
                                .thenComparingDouble(r -> -r.getWeight()));

            // Kategorie-Trenner
            list.add(categoryDivider(cat));

            // Items
            for (CrateReward r : pool) {
                double chance = dropChance * (r.getWeight() / totalCosmeticWeight) * 100.0;
                list.add(buildRewardItem(r, chance));
            }
        }

        return list;
    }

    // ── Reward-Item ────────────────────────────────────────────────

    private ItemStack buildRewardItem(CrateReward reward, double chancePercent) {
        ItemStack item = new ItemStack(reward.getIcon());
        ItemMeta  meta = item.getItemMeta();

        // Name: Tier-Label + Display-Name
        meta.displayName(MessageUtil.parse(
                reward.tierLabel() + " <white>" + reward.getDisplayName() + "</white>"
        ));

        List<Component> lore = new ArrayList<>();
        lore.add(Component.empty());
        lore.add(MessageUtil.parse("<gray>Rarity: " + reward.tierLabel()));
        lore.add(MessageUtil.parse("<gray>Chance: <gold>" + formatChance(chancePercent) + "</gold>"));
        lore.add(Component.empty());

        // Vorschau je nach Typ
        String preview = reward.getCosmeticFormat()
                .replace("{name}", "Steve")
                .replace("{msg}",  "Hallo!");
        if (reward.isGlow()) {
            lore.add(MessageUtil.parse("<gray>Glow-Farbe: <white>" + reward.getCosmeticFormat() + "</white>"));
        } else {
            lore.add(MessageUtil.parse("<gray>Vorschau: " + preview));
        }

        meta.lore(lore);

        // Glow bei Legendary/Very Rare
        if (reward.getTier() >= 2) {
            meta.addEnchant(Enchantment.UNBREAKING, 1, true);
            meta.addItemFlags(ItemFlag.HIDE_ENCHANTS);
        }
        meta.addItemFlags(ItemFlag.HIDE_ATTRIBUTES);
        item.setItemMeta(meta);
        return item;
    }

    // ── Header-Items ────────────────────────────────────────────────

    private ItemStack buildCrateHeader() {
        ItemStack item = new ItemStack(crateType.getIcon());
        ItemMeta  meta = item.getItemMeta();
        meta.displayName(MessageUtil.parse(
                crateType.getGradient() + "<bold>" + crateType.getDisplayName() + " Crate</bold></gradient>"
        ));
        List<Component> lore = new ArrayList<>();
        lore.add(Component.empty());
        lore.add(MessageUtil.parse("<gray>Cosmetic-Drop: <gold>" + String.format("%.1f", baseDrop() * 100.0) + "%</gold> pro Öffnung"));
        lore.add(MessageUtil.parse("<gray>Max Rarity: " + maxTierLabel()));
        meta.lore(lore);
        meta.addItemFlags(ItemFlag.HIDE_ATTRIBUTES);
        item.setItemMeta(meta);
        return item;
    }

    private ItemStack buildPouchInfo() {
        ItemStack item = new ItemStack(Material.EMERALD);
        ItemMeta  meta = item.getItemMeta();
        meta.displayName(MessageUtil.parse("<white><bold>Pouch-Tiers</bold></white>"));

        List<Component> lore = new ArrayList<>();
        lore.add(Component.empty());
        lore.add(MessageUtil.parse("<gray>Beim Öffnen erhältst du immer Pouches."));
        lore.add(Component.empty());

        // Welche Tiers diese Crate geben kann
        for (PouchTier tier : tiersForCrate()) {
            String multiplier = "x" + tier.getMultiplier();
            lore.add(MessageUtil.parse("  " + tier.getColorTag() + tier.getDisplayName() + "</" +
                (tier.getColorTag().startsWith("<gradient") ? "gradient" : tier.getColorTag().replace("<","").replace(">","")) +
                ">  <dark_gray>" + multiplier));
        }

        lore.add(Component.empty());
        lore.add(MessageUtil.parse("<dark_gray>Money / XP / Tokens — auto-redeemed."));
        meta.lore(lore);
        meta.addItemFlags(ItemFlag.HIDE_ATTRIBUTES);
        item.setItemMeta(meta);
        return item;
    }

    private ItemStack buildBack() {
        ItemStack item = new ItemStack(Material.ARROW);
        ItemMeta  meta = item.getItemMeta();
        meta.displayName(MessageUtil.parse("<red>\u2190 Zurück"));
        meta.lore(List.of(MessageUtil.parse("<dark_gray>Zurück zum Crates-Menü")));
        meta.addItemFlags(ItemFlag.HIDE_ATTRIBUTES);
        item.setItemMeta(meta);
        return item;
    }

    private ItemStack buildPageInfo(int page, int totalPages) {
        ItemStack item = new ItemStack(Material.PAPER);
        ItemMeta  meta = item.getItemMeta();
        meta.displayName(MessageUtil.parse("<gray>Seite <white>" + (page + 1) + "</white> / " + totalPages));
        meta.addItemFlags(ItemFlag.HIDE_ATTRIBUTES);
        item.setItemMeta(meta);
        return item;
    }

    private ItemStack navArrow(String label, int targetPage, int totalPages) {
        ItemStack item = new ItemStack(Material.ARROW);
        ItemMeta  meta = item.getItemMeta();
        meta.displayName(MessageUtil.parse(label));
        meta.lore(List.of(MessageUtil.parse("<gray>Seite " + targetPage + " / " + totalPages)));
        meta.addItemFlags(ItemFlag.HIDE_ATTRIBUTES);
        item.setItemMeta(meta);
        return item;
    }

    // ── Kategorie-Trenner ─────────────────────────────────────────────

    private ItemStack categoryDivider(CrateReward.Type cat) {
        String label = switch (cat) {
            case TAG        -> "<white><bold>\uD83C\uDFF7 Tags</bold></white>";
            case NAME_COLOR -> "<white><bold>\uD83C\uDFA8 Name-Farben</bold></white>";
            case CHAT_COLOR -> "<white><bold>\uD83D\uDCAC Chat-Farben</bold></white>";
            case GLOW       -> "<white><bold>\u2728 Glow</bold></white>";
            default         -> "<white><bold>Rewards</bold></white>";
        };
        Material mat = switch (cat) {
            case TAG        -> Material.NAME_TAG;
            case NAME_COLOR -> Material.CYAN_DYE;
            case CHAT_COLOR -> Material.PURPLE_DYE;
            case GLOW       -> Material.GLOWSTONE_DUST;
            default         -> Material.PAPER;
        };
        ItemStack item = new ItemStack(mat);
        ItemMeta  meta = item.getItemMeta();
        meta.displayName(MessageUtil.parse(label));
        List<Component> lore = new ArrayList<>();
        lore.add(MessageUtil.parse("<dark_gray>────────────────────────"));
        meta.lore(lore);
        meta.addItemFlags(ItemFlag.HIDE_ATTRIBUTES);
        item.setItemMeta(meta);
        return item;
    }

    // ── Background-Pane (zur Crate passend, aber subtil) ───────────────

    private ItemStack bgPane(CrateType type) {
        Material mat = switch (type) {
            case COMMON    -> Material.GRAY_STAINED_GLASS_PANE;
            case UNCOMMON  -> Material.GRAY_STAINED_GLASS_PANE;
            case RARE      -> Material.GRAY_STAINED_GLASS_PANE;
            case EPIC      -> Material.GRAY_STAINED_GLASS_PANE;
            case LEGENDARY -> Material.GRAY_STAINED_GLASS_PANE;
        };
        ItemStack p = new ItemStack(mat);
        ItemMeta  m = p.getItemMeta();
        m.displayName(Component.empty());
        p.setItemMeta(m);
        return p;
    }

    // ── Hilfsmethoden ─────────────────────────────────────────────────

    private int maxTierForCrate() {
        return switch (crateType) {
            case COMMON, UNCOMMON -> CrateReward.TIER_RARE;
            case RARE, EPIC       -> CrateReward.TIER_VERY_RARE;
            case LEGENDARY        -> CrateReward.TIER_LEGENDARY;
        };
    }

    private String maxTierLabel() {
        return switch (maxTierForCrate()) {
            case CrateReward.TIER_RARE      -> "<gradient:#7afcff:#00c2ff>Rare</gradient>";
            case CrateReward.TIER_VERY_RARE -> "<gradient:#c471f5:#fa71cd>Very Rare</gradient>";
            case CrateReward.TIER_LEGENDARY -> "<gradient:#f6d365:#fda085><bold>\u2746 Legendary</bold></gradient>";
            default                          -> "<gray>Common</gray>";
        };
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

    private String formatChance(double pct) {
        if (pct < 0.001) return "< 0.001%";
        if (pct < 0.01)  return String.format("%.4f%%", pct);
        if (pct < 0.1)   return String.format("%.3f%%", pct);
        return String.format("%.2f%%", pct);
    }

    // ── Statische Helfer für den Listener ──────────────────────────────

    public static CrateType resolveFromTitle(String plainTitle) {
        for (CrateType t : CrateType.values()) {
            if (plainTitle.contains(t.getDisplayName() + " Crate")) return t;
        }
        return null;
    }

    public static void clearState(UUID uuid) {
        playerPage.remove(uuid);
    }
}
