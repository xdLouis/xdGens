package de.louis.xdGens.manager;

import de.louis.xdGens.crate.CosmeticVoucherItem;
import de.louis.xdGens.crate.CrateReward;
import de.louis.xdGens.crate.CrateType;
import de.louis.xdGens.crate.PouchItem;
import de.louis.xdGens.crate.PouchTier;
import de.louis.xdGens.crate.PouchType;
import de.louis.xdGens.main.Main;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ThreadLocalRandom;

public class CrateManager {

    private final Main plugin;

    public CrateManager(Main plugin) {
        this.plugin = plugin;
    }

    // ── roll a random crate type ─────────────────────────────────────────

    public CrateType rollCrate() {
        double total = 0;
        for (CrateType t : CrateType.values()) total += t.getWeight();
        double rand = ThreadLocalRandom.current().nextDouble() * total, acc = 0;
        for (CrateType t : CrateType.values()) { acc += t.getWeight(); if (rand <= acc) return t; }
        return CrateType.COMMON;
    }

    // ── open one crate ───────────────────────────────────────────────

    /**
     * Pouch tiers per crate type:
     *   COMMON    → T1 / T2          (80% / 20%)
     *   UNCOMMON  → T1 / T2 / T3    (50% / 35% / 15%)
     *   RARE      → T2 / T3 / T4    (50% / 35% / 15%)
     *   EPIC      → T3 / T4 / T5    (50% / 35% / 15%)
     *   LEGENDARY → T4 / T5         (30% / 70%)
     *
     * Cosmetic pools remain strictly tier-gated (unchanged).
     */
    public CrateOpenResult openCrate(Player player, CrateType type) {
        PouchTier tier = rollPouchTier(type);

        long money  = scaled(randomBetween(moneyMin(type),  moneyMax(type)),  tier);
        long xp     = scaled(randomBetween(xpMin(type),    xpMax(type)),     tier);
        long tokens = scaled(randomBetween(tokenMin(type), tokenMax(type)),  tier);

        List<ItemStack> pouches = new ArrayList<>();
        pouches.add(PouchItem.create(plugin, PouchType.MONEY,  tier, money));
        pouches.add(PouchItem.create(plugin, PouchType.XP,     tier, xp));
        pouches.add(PouchItem.create(plugin, PouchType.TOKENS, tier, tokens));

        CrateReward cosmetic    = rollCosmetic(type);
        ItemStack   voucherItem = cosmetic != null ? CosmeticVoucherItem.create(plugin, cosmetic) : null;
        return new CrateOpenResult(pouches, tier, voucherItem, cosmetic);
    }

    // ── pouch tier roll ─────────────────────────────────────────────

    private PouchTier rollPouchTier(CrateType crateType) {
        // weights: [tier, weight]
        double[][] table = switch (crateType) {
            case COMMON    -> new double[][]{{1,80},{2,20}};
            case UNCOMMON  -> new double[][]{{1,50},{2,35},{3,15}};
            case RARE      -> new double[][]{{2,50},{3,35},{4,15}};
            case EPIC      -> new double[][]{{3,50},{4,35},{5,15}};
            case LEGENDARY -> new double[][]{{4,30},{5,70}};
        };
        PouchTier[] tiers = PouchTier.values();
        double total = 0; for (double[] row : table) total += row[1];
        double rand = ThreadLocalRandom.current().nextDouble() * total, acc = 0;
        for (double[] row : table) {
            acc += row[1];
            if (rand <= acc) return tiers[(int) row[0] - 1];
        }
        return tiers[(int) table[table.length - 1][0] - 1];
    }

    private long scaled(long base, PouchTier tier) {
        return Math.round(base * tier.getMultiplier());
    }

    // ── cosmetic roll (strict tier-gated, unchanged) ─────────────────

    private CrateReward rollCosmetic(CrateType crateType) {
        double chance = switch (crateType) {
            case COMMON    -> 0.008;
            case UNCOMMON  -> 0.015;
            case RARE      -> 0.035;
            case EPIC      -> 0.070;
            case LEGENDARY -> 0.140;
        };
        if (ThreadLocalRandom.current().nextDouble() > chance) return null;

        int targetTier = switch (crateType) {
            case COMMON, UNCOMMON -> 1;
            case RARE, EPIC       -> 2;
            case LEGENDARY        -> 3;
        };
        List<CrateReward> pool = new ArrayList<>();
        for (CrateReward r : CrateReward.values()) {
            if (!r.isPouch() && r.getTier() == targetTier) pool.add(r);
        }
        if (pool.isEmpty()) return null;

        double total = 0; for (CrateReward r : pool) total += r.getWeight();
        double rand = ThreadLocalRandom.current().nextDouble() * total, acc = 0;
        for (CrateReward r : pool) { acc += r.getWeight(); if (rand <= acc) return r; }
        return pool.get(pool.size() - 1);
    }

    // ── key finder ──────────────────────────────────────────────────

    public boolean tryGiveRandomKey(Player player) {
        if (ThreadLocalRandom.current().nextDouble() > plugin.getHoeUpgradeManager().getKeyFinderChance(player)) return false;
        plugin.getVirtualKeyManager().addKey(player, rollCrate());
        return true;
    }

    // ── result record ───────────────────────────────────────────────

    public record CrateOpenResult(
            List<ItemStack> pouches,
            PouchTier       pouchTier,
            ItemStack       voucherItem,
            CrateReward     rolledCosmetic
    ) {
        public boolean hasVoucher()     { return voucherItem != null; }
        public boolean hasNewCosmetic() { return voucherItem != null; }
        public CrateReward newCosmetic(){ return rolledCosmetic; }
    }

    // ── reward tables ────────────────────────────────────────────────

    private long randomBetween(long min, long max) { return ThreadLocalRandom.current().nextLong(min, max + 1); }

    // base ranges (T1 baseline — multiplied by tier.getMultiplier() in openCrate)
    private long moneyMin(CrateType t) { return switch(t){case COMMON->2500;case UNCOMMON->5000;case RARE->10000;case EPIC->20000;case LEGENDARY->40000;}; }
    private long moneyMax(CrateType t) { return switch(t){case COMMON->4000;case UNCOMMON->8000;case RARE->16000;case EPIC->32000;case LEGENDARY->60000;}; }
    private long xpMin(CrateType t)    { return switch(t){case COMMON->80;case UNCOMMON->180;case RARE->400;case EPIC->900;case LEGENDARY->2000;}; }
    private long xpMax(CrateType t)    { return switch(t){case COMMON->150;case UNCOMMON->300;case RARE->700;case EPIC->1500;case LEGENDARY->3500;}; }
    private long tokenMin(CrateType t) { return switch(t){case COMMON->100;case UNCOMMON->220;case RARE->500;case EPIC->1100;case LEGENDARY->2500;}; }
    private long tokenMax(CrateType t) { return switch(t){case COMMON->200;case UNCOMMON->400;case RARE->900;case EPIC->2000;case LEGENDARY->4500;}; }
}
