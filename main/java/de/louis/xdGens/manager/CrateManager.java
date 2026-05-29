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

    // ── crate roll ────────────────────────────────────────────────────

    public CrateType rollCrate() {
        double total = 0.0;
        for (CrateType t : CrateType.values()) total += t.getWeight();
        double rand = ThreadLocalRandom.current().nextDouble() * total, acc = 0;
        for (CrateType t : CrateType.values()) { acc += t.getWeight(); if (rand <= acc) return t; }
        return CrateType.COMMON;
    }

    // ── open crate ────────────────────────────────────────────────────

    /**
     * Cosmetic chances:
     *   COMMON     0.8%   tier 1 only
     *   UNCOMMON   1.5%   tier 1 only
     *   RARE       3.5%   tier 1 + 2 (tier-2 at 30% weight)
     *   EPIC       7.0%   tier 1 + 2 + 3 (tier-3 at 20% weight)
     *   LEGENDARY 14.0%   all tiers, tier-3 at 200% weight
     */
    public CrateOpenResult openCrate(Player player, CrateType type) {
        List<ItemStack> pouches = new ArrayList<>();
        pouches.add(PouchItem.create(plugin, PouchType.MONEY,  randomBetween(moneyMin(type),  moneyMax(type)),  type));
        pouches.add(PouchItem.create(plugin, PouchType.XP,     randomBetween(xpMin(type),    xpMax(type)),     type));
        pouches.add(PouchItem.create(plugin, PouchType.TOKENS, randomBetween(tokenMin(type), tokenMax(type)),  type));

        CrateReward cosmetic    = rollCosmetic(type);
        ItemStack   voucherItem = cosmetic != null ? CosmeticVoucherItem.create(plugin, cosmetic) : null;

        return new CrateOpenResult(pouches, voucherItem, cosmetic);
    }

    private CrateReward rollCosmetic(CrateType crateType) {
        double chance = switch (crateType) {
            case COMMON    -> 0.008;
            case UNCOMMON  -> 0.015;
            case RARE      -> 0.035;
            case EPIC      -> 0.070;
            case LEGENDARY -> 0.140;
        };
        if (ThreadLocalRandom.current().nextDouble() > chance) return null;

        List<CrateReward> pool = new ArrayList<>();
        for (CrateReward r : CrateReward.values()) {
            if (!r.isTag() && !r.isColor() && !r.isChatColor()) continue;
            switch (crateType) {
                case COMMON, UNCOMMON -> { if (r.getTier() == 1) pool.add(r); }
                case RARE             -> { if (r.getTier() <= 2) pool.add(r); }
                case EPIC, LEGENDARY  -> pool.add(r);
            }
        }
        if (pool.isEmpty()) return null;

        double total = 0;
        for (CrateReward r : pool) total += adjustedWeight(r, crateType);
        double rand = ThreadLocalRandom.current().nextDouble() * total, acc = 0;
        for (CrateReward r : pool) {
            acc += adjustedWeight(r, crateType);
            if (rand <= acc) return r;
        }
        return pool.get(pool.size() - 1);
    }

    private double adjustedWeight(CrateReward r, CrateType crateType) {
        double w = r.getWeight();
        return switch (r.getTier()) {
            case 2 -> switch (crateType) { case RARE -> w * 0.3; case EPIC -> w; default -> w; };
            case 3 -> switch (crateType) { case EPIC -> w * 0.2; case LEGENDARY -> w * 2.0; default -> w; };
            default -> w;
        };
    }

    // ── key finder ────────────────────────────────────────────────────

    public boolean tryGiveRandomKey(Player player) {
        double chance = plugin.getHoeUpgradeManager().getKeyFinderChance(player);
        if (ThreadLocalRandom.current().nextDouble() > chance) return false;
        plugin.getVirtualKeyManager().addKey(player, rollCrate());
        return true;
    }

    // ── result record ─────────────────────────────────────────────────

    public record CrateOpenResult(
            List<ItemStack> pouches,
            ItemStack       voucherItem,
            CrateReward     rolledCosmetic
    ) {
        public boolean hasVoucher()      { return voucherItem != null; }
        public boolean hasNewCosmetic()  { return voucherItem != null; }
        public CrateReward newCosmetic() { return rolledCosmetic; }
    }

    // ── reward tables ─────────────────────────────────────────────────

    private long randomBetween(long min, long max) { return ThreadLocalRandom.current().nextLong(min, max + 1); }

    private long moneyMin(CrateType t) { return switch(t){case COMMON->2500;case UNCOMMON->8000;case RARE->20000;case EPIC->60000;case LEGENDARY->175000;}; }
    private long moneyMax(CrateType t) { return switch(t){case COMMON->6000;case UNCOMMON->16000;case RARE->40000;case EPIC->110000;case LEGENDARY->300000;}; }
    private long xpMin(CrateType t)    { return switch(t){case COMMON->100;case UNCOMMON->300;case RARE->800;case EPIC->2000;case LEGENDARY->5000;}; }
    private long xpMax(CrateType t)    { return switch(t){case COMMON->250;case UNCOMMON->700;case RARE->1500;case EPIC->3500;case LEGENDARY->8000;}; }
    private long tokenMin(CrateType t) { return switch(t){case COMMON->150;case UNCOMMON->400;case RARE->950;case EPIC->2500;case LEGENDARY->6000;}; }
    private long tokenMax(CrateType t) { return switch(t){case COMMON->350;case UNCOMMON->800;case RARE->1800;case EPIC->4500;case LEGENDARY->10000;}; }
}
