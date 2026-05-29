package de.louis.xdGens.manager;

import de.louis.xdGens.crate.CosmeticVoucherItem;
import de.louis.xdGens.crate.CrateReward;
import de.louis.xdGens.crate.CrateType;
import de.louis.xdGens.crate.PouchItem;
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

    // ── crate roll ───────────────────────────────────────────────────────

    public CrateType rollCrate() {
        double total = 0.0;
        for (CrateType type : CrateType.values()) total += type.getWeight();
        double random  = ThreadLocalRandom.current().nextDouble() * total;
        double current = 0.0;
        for (CrateType type : CrateType.values()) {
            current += type.getWeight();
            if (random <= current) return type;
        }
        return CrateType.COMMON;
    }

    // ── open crate ───────────────────────────────────────────────────────

    /**
     * Opens one crate. Pouches carry the reward values (redeemed directly by CrateListener).
     * Cosmetic voucher item is given to inv if rolled.
     *
     * Cosmetic chances per crate tier:
     *   COMMON    1.2%  — only TIER_RARE cosmetics
     *   UNCOMMON  2.5%  — only TIER_RARE cosmetics
     *   RARE      5.0%  — TIER_RARE + TIER_VERY_RARE
     *   EPIC      9.0%  — TIER_RARE + TIER_VERY_RARE (higher very-rare weight)
     *   LEGENDARY 15.0% — all cosmetics, TIER_VERY_RARE heavily favored
     */
    public CrateOpenResult openCrate(Player player, CrateType type) {
        List<ItemStack> pouches = new ArrayList<>();
        pouches.add(PouchItem.create(plugin, PouchType.MONEY,  randomBetween(moneyMin(type),  moneyMax(type)),  type));
        pouches.add(PouchItem.create(plugin, PouchType.XP,     randomBetween(xpMin(type),    xpMax(type)),     type));
        pouches.add(PouchItem.create(plugin, PouchType.TOKENS, randomBetween(tokenMin(type), tokenMax(type)),  type));

        CrateReward cosmetic = rollCosmetic(type);
        ItemStack voucherItem = cosmetic != null ? CosmeticVoucherItem.create(plugin, cosmetic) : null;

        return new CrateOpenResult(pouches, voucherItem, cosmetic);
    }

    private CrateReward rollCosmetic(CrateType crateType) {
        // base chance per crate type — very low overall
        double chance = switch (crateType) {
            case COMMON    -> 0.012; // 1.2%
            case UNCOMMON  -> 0.025; // 2.5%
            case RARE      -> 0.050; // 5%
            case EPIC      -> 0.090; // 9%
            case LEGENDARY -> 0.150; // 15%
        };
        if (ThreadLocalRandom.current().nextDouble() > chance) return null;

        // build eligible pool based on crate tier
        List<CrateReward> pool = new ArrayList<>();
        for (CrateReward r : CrateReward.values()) {
            if (!r.isTag() && !r.isColor()) continue;
            switch (crateType) {
                // common/uncommon: only basic (TIER_RARE) cosmetics
                case COMMON, UNCOMMON -> { if (r.getTier() == CrateReward.TIER_RARE) pool.add(r); }
                // rare: all cosmetics, but very-rare at half weight
                case RARE -> pool.add(r);
                // epic/legendary: all cosmetics; very-rare at normal/boosted weight
                case EPIC, LEGENDARY -> pool.add(r);
            }
        }
        if (pool.isEmpty()) return null;

        // for RARE crates halve very-rare weights; for LEGENDARY boost them
        double total = 0;
        for (CrateReward r : pool) {
            total += adjustedWeight(r, crateType);
        }
        double rand = ThreadLocalRandom.current().nextDouble() * total;
        double acc  = 0;
        for (CrateReward r : pool) {
            acc += adjustedWeight(r, crateType);
            if (rand <= acc) return r;
        }
        return pool.get(pool.size() - 1);
    }

    private double adjustedWeight(CrateReward r, CrateType crateType) {
        double w = r.getWeight();
        if (r.getTier() == CrateReward.TIER_VERY_RARE) {
            return switch (crateType) {
                case RARE      -> w * 0.4;  // harder to get very-rare from rare crate
                case EPIC      -> w * 1.0;
                case LEGENDARY -> w * 2.5;  // boosted in legendary
                default        -> w;
            };
        }
        return w;
    }

    // ── key finder ───────────────────────────────────────────────────────

    public boolean tryGiveRandomKey(Player player) {
        double chance = plugin.getHoeUpgradeManager().getKeyFinderChance(player);
        if (ThreadLocalRandom.current().nextDouble() > chance) return false;
        CrateType crate = rollCrate();
        plugin.getVirtualKeyManager().addKey(player, crate);
        return true;
    }

    // ── result ───────────────────────────────────────────────────────────

    public record CrateOpenResult(
            List<ItemStack> pouches,
            ItemStack voucherItem,
            CrateReward rolledCosmetic
    ) {
        public boolean hasVoucher()     { return voucherItem != null; }
        public boolean hasNewCosmetic() { return voucherItem != null; }
        public CrateReward newCosmetic(){ return rolledCosmetic; }
    }

    // ── reward tables ─────────────────────────────────────────────────────

    private long randomBetween(long min, long max) {
        return ThreadLocalRandom.current().nextLong(min, max + 1);
    }

    private long moneyMin(CrateType t) { return switch(t){case COMMON->2500;case UNCOMMON->8000;case RARE->20000;case EPIC->60000;case LEGENDARY->175000;}; }
    private long moneyMax(CrateType t) { return switch(t){case COMMON->6000;case UNCOMMON->16000;case RARE->40000;case EPIC->110000;case LEGENDARY->300000;}; }
    private long xpMin(CrateType t)    { return switch(t){case COMMON->100;case UNCOMMON->300;case RARE->800;case EPIC->2000;case LEGENDARY->5000;}; }
    private long xpMax(CrateType t)    { return switch(t){case COMMON->250;case UNCOMMON->700;case RARE->1500;case EPIC->3500;case LEGENDARY->8000;}; }
    private long tokenMin(CrateType t) { return switch(t){case COMMON->150;case UNCOMMON->400;case RARE->950;case EPIC->2500;case LEGENDARY->6000;}; }
    private long tokenMax(CrateType t) { return switch(t){case COMMON->350;case UNCOMMON->800;case RARE->1800;case EPIC->4500;case LEGENDARY->10000;}; }
}
