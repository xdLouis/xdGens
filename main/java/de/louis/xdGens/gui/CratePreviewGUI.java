package de.louis.xdGens.gui;

import de.louis.xdGens.crate.CrateReward;
import de.louis.xdGens.crate.CrateType;
import de.louis.xdGens.crate.PouchTier;
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
 * Crate Preview GUI  —  6 rows (54 slots)
 *
 * Tier rules per crate:
 *   COMMON     → Tier 0 only
 *   UNCOMMON   → Tier 0–1
 *   RARE       → Tier 0–2
 *   EPIC       → Tier 1–2
 *   LEGENDARY  → Tier 3 only
 */
public class CratePreviewGUI {

    public static final int SLOT_BACK = 8;
    public static final int SLOT_PREV = 45;
    public static final int SLOT_NEXT = 53;

    private static final int CONTENT_START = 9;
    private static final int CONTENT_SIZE  = 36;

    public static final Map<UUID, Integer> playerPage = new HashMap<>();

    private final Main      plugin;
    private final CrateType crateType;

    public CratePreviewGUI(Main plugin, CrateType crateType) {
        this.plugin    = plugin;
        this.crateType = crateType;
    }

    // ── open ───────────────────────────────────────────────────

    public void open(Player player) {
        open(player, playerPage.getOrDefault(player.getUniqueId(), 0));
    }

    public void open(Player player, int page) {
        List<ItemStack> content = buildContent();
        int totalPages = Math.max(1, (int) Math.ceil(content.size() / (double) CONTENT_SIZE));
        page = Math.max(0, Math.min(page, totalPages - 1));
        playerPage.put(player.getUniqueId(), page);

        Inventory inv = Bukkit.createInventory(
                null, 54,
                MessageUtil.parse("\uD83D\uDD0D " + crateType.getDisplayName() + " Crate")
        );

        ItemStack bg = bgPane();
        for (int i = 0; i < 54; i++) inv.setItem(i, bg.clone());

        inv.setItem(0, buildCrateHeader());
        inv.setItem(1, buildPouchInfo());
        if (totalPages > 1) inv.setItem(4, buildPageInfo(page, totalPages));
        inv.setItem(SLOT_BACK, buildBack());

        int from = page * CONTENT_SIZE;
        int to   = Math.min(from + CONTENT_SIZE, content.size());
        for (int i = from; i < to; i++) {
            inv.setItem(CONTENT_START + (i - from), content.get(i));
        }

        if (page > 0)              inv.setItem(SLOT_PREV, navArrow("<yellow>\u2190 Previous", page,     totalPages));
        if (page < totalPages - 1) inv.setItem(SLOT_NEXT, navArrow("<yellow>Next \u2192",     page + 2, totalPages));

        player.openInventory(inv);
    }

    // ── Content ──────────────────────────────────────────────────

    private List<ItemStack> buildContent() {
        List<ItemStack> list = new ArrayList<>();
        int minTier = minTierForCrate();
        int maxTier = maxTierForCrate();

        double totalWeight = 0;
        for (CrateReward r : CrateReward.values()) {
            if (r.getType() != CrateReward.Type.POUCH
                    && r.getTier() >= minTier && r.getTier() <= maxTier) {
                totalWeight += r.getWeight();
            }
        }
        double dropChance = baseDrop();
        final double tw = totalWeight;

        List<CrateReward> pool = new ArrayList<>();
        for (CrateReward r : CrateReward.values()) {
            if (r.getType() != CrateReward.Type.POUCH
                    && r.getTier() >= minTier && r.getTier() <= maxTier) {
                pool.add(r);
            }
        }
        pool.sort(Comparator.comparingInt(CrateReward::getTier)
                            .thenComparingInt(r -> r.getType().ordinal())
                            .thenComparingDouble(r -> -r.getWeight()));

        for (CrateReward r : pool) {
            double chance = dropChance * (r.getWeight() / tw) * 100.0;
            list.add(buildRewardItem(r, chance));
        }
        return list;
    }

    // ── Reward item ──────────────────────────────────────────────

    private ItemStack buildRewardItem(CrateReward reward, double chancePercent) {
        ItemStack item = new ItemStack(reward.getIcon());
        ItemMeta  meta = item.getItemMeta();

        meta.displayName(MessageUtil.parse(
                reward.tierLabel() + " <white>" + reward.getDisplayName() + "</white>"
        ));

        List<Component> lore = new ArrayList<>();
        lore.add(Component.empty());
        lore.add(MessageUtil.parse("<gray>Type: <white>" + typeLabel(reward.getType()) + "</white>"));
        lore.add(MessageUtil.parse("<gray>Rarity: " + reward.tierLabel()));
        lore.add(MessageUtil.parse("<gray>Chance: <gold>" + formatChance(chancePercent) + "</gold>"));
        lore.add(Component.empty());

        if (reward.isGlow()) {
            lore.add(MessageUtil.parse("<gray>Glow color: <white>" + reward.getCosmeticFormat() + "</white>"));
        } else {
            String preview = reward.getCosmeticFormat()
                    .replace("{name}", "Steve")
                    .replace("{msg}",  "Hello!");
            lore.add(MessageUtil.parse("<gray>Preview: " + preview));
        }

        meta.lore(lore);
        if (reward.getTier() >= 2) {
            meta.addEnchant(Enchantment.UNBREAKING, 1, true);
            meta.addItemFlags(ItemFlag.HIDE_ENCHANTS);
        }
        meta.addItemFlags(ItemFlag.HIDE_ATTRIBUTES);
        item.setItemMeta(meta);
        return item;
    }

    // ── Header items ─────────────────────────────────────────────

    private ItemStack buildCrateHeader() {
        ItemStack item = new ItemStack(crateType.getIcon());
        ItemMeta  meta = item.getItemMeta();
        meta.displayName(MessageUtil.parse(
                crateType.getGradient() + "<bold>" + crateType.getDisplayName() + " Crate</bold></gradient>"
        ));
        List<Component> lore = new ArrayList<>();
        lore.add(Component.empty());
        lore.add(MessageUtil.parse("<gray>Cosmetic drop: <gold>" + String.format("%.1f", baseDrop() * 100.0) + "%</gold> per opening"));
        lore.add(MessageUtil.parse("<gray>Rarity range: " + tierRangeLabel()));
        meta.lore(lore);
        meta.addItemFlags(ItemFlag.HIDE_ATTRIBUTES);
        item.setItemMeta(meta);
        return item;
    }

    private ItemStack buildPouchInfo() {
        ItemStack item = new ItemStack(Material.EMERALD);
        ItemMeta  meta = item.getItemMeta();
        meta.displayName(MessageUtil.parse("<white><bold>Pouch Tiers</bold></white>"));

        List<Component> lore = new ArrayList<>();
        lore.add(Component.empty());
        lore.add(MessageUtil.parse("<gray>Every opening grants pouches."));
        lore.add(Component.empty());
        for (PouchTier tier : tiersForCrate()) {
            lore.add(MessageUtil.parse("  " + tier.getDisplayName() + "  <dark_gray>x" + tier.getMultiplier()));
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
        meta.displayName(MessageUtil.parse("<red>\u2190 Back"));
        meta.lore(List.of(MessageUtil.parse("<dark_gray>Back to Crates menu")));
        meta.addItemFlags(ItemFlag.HIDE_ATTRIBUTES);
        item.setItemMeta(meta);
        return item;
    }

    private ItemStack buildPageInfo(int page, int totalPages) {
        ItemStack item = new ItemStack(Material.PAPER);
        ItemMeta  meta = item.getItemMeta();
        meta.displayName(MessageUtil.parse("<gray>Page <white>" + (page + 1) + "</white> / " + totalPages));
        meta.addItemFlags(ItemFlag.HIDE_ATTRIBUTES);
        item.setItemMeta(meta);
        return item;
    }

    private ItemStack navArrow(String label, int targetPage, int totalPages) {
        ItemStack item = new ItemStack(Material.ARROW);
        ItemMeta  meta = item.getItemMeta();
        meta.displayName(MessageUtil.parse(label));
        meta.lore(List.of(MessageUtil.parse("<gray>Page " + targetPage + " / " + totalPages)));
        meta.addItemFlags(ItemFlag.HIDE_ATTRIBUTES);
        item.setItemMeta(meta);
        return item;
    }

    // ── Background ───────────────────────────────────────────────

    private ItemStack bgPane() {
        ItemStack p = new ItemStack(Material.GRAY_STAINED_GLASS_PANE);
        ItemMeta  m = p.getItemMeta();
        m.displayName(Component.empty());
        p.setItemMeta(m);
        return p;
    }

    // ── Helpers ───────────────────────────────────────────────────

    private String typeLabel(CrateReward.Type type) {
        return switch (type) {
            case TAG        -> "Tag";
            case NAME_COLOR -> "Name Color";
            case CHAT_COLOR -> "Chat Color";
            case GLOW       -> "Glow";
            default         -> "Cosmetic";
        };
    }

    private int minTierForCrate() {
        return switch (crateType) {
            case EPIC, RARE, UNCOMMON, COMMON -> CrateReward.TIER_COMMON;
            case LEGENDARY                    -> CrateReward.TIER_LEGENDARY;
        };
    }

    private int maxTierForCrate() {
        return switch (crateType) {
            case COMMON    -> CrateReward.TIER_COMMON;
            case UNCOMMON  -> CrateReward.TIER_RARE;
            case RARE      -> CrateReward.TIER_VERY_RARE;
            case EPIC      -> CrateReward.TIER_VERY_RARE;
            case LEGENDARY -> CrateReward.TIER_LEGENDARY;
        };
    }

    private String tierRangeLabel() {
        int min = minTierForCrate();
        int max = maxTierForCrate();
        if (min == max) return tierLabel(min);
        return tierLabel(min) + " <dark_gray>\u2192</dark_gray> " + tierLabel(max);
    }

    private String tierLabel(int tier) {
        return switch (tier) {
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
