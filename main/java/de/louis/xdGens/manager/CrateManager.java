package de.louis.xdGens.manager;

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

    // ── reward generation ────────────────────────────────────────────────

    /**
     * Roll all rewards for opening one crate.
     * Always includes 3 pouch rewards.
     * Additionally, has a per-reward-rarity chance to also grant a cosmetic.
     */
    public CrateOpenResult openCrate(Player player, CrateType type) {
        List<ItemStack> pouches = new ArrayList<>();
        pouches.add(PouchItem.create(plugin, PouchType.MONEY,  randomBetween(moneyMin(type),  moneyMax(type)),  type));
        pouches.add(PouchItem.create(plugin, PouchType.XP,     randomBetween(xpMin(type),    xpMax(type)),    type));
        pouches.add(PouchItem.create(plugin, PouchType.TOKENS, randomBetween(tokenMin(type), tokenMax(type)), type));

        // roll cosmetic
        CrateReward cosmetic = rollCosmetic(type);
        CrateReward newCosmetic = null;
        if (cosmetic != null) {
            boolean isNew = plugin.getPlayerCosmeticManager().unlock(player, cosmetic);
            if (isNew) newCosmetic = cosmetic;
            // if already owned – silently ignore (no duplicate message spam)
        }

        return new CrateOpenResult(pouches, newCosmetic);
    }

    /** Roll one optional cosmetic reward based on the crate tier. */
    private CrateReward rollCosmetic(CrateType crateType) {
        // chance to even attempt a cosmetic roll depends on crate rarity
        double cosmeticChance = switch (crateType) {
            case COMMON    -> 0.05;
            case UNCOMMON  -> 0.10;
            case RARE      -> 0.20;
            case EPIC      -> 0.40;
            case LEGENDARY -> 0.70;
        };
        if (ThreadLocalRandom.current().nextDouble() > cosmeticChance) return null;

        // weight-based roll among all cosmetics
        List<CrateReward> cosmetics = new ArrayList<>();
        for (CrateReward r : CrateReward.values()) {
            if (r.isTag() || r.isColor()) cosmetics.add(r);
        }
        double total = cosmetics.stream().mapToDouble(CrateReward::getWeight).sum();
        double rand  = ThreadLocalRandom.current().nextDouble() * total;
        double acc   = 0;
        for (CrateReward r : cosmetics) {
            acc += r.getWeight();
            if (rand <= acc) return r;
        }
        return cosmetics.get(cosmetics.size() - 1);
    }

    // ── legacy helper kept for FieldListener ────────────────────────────

    /** Gives a virtual key (no item in inventory). Returns true if key was granted. */
    public boolean tryGiveRandomKey(Player player) {
        double chance = plugin.getHoeUpgradeManager().getKeyFinderChance(player);
        if (ThreadLocalRandom.current().nextDouble() > chance) return false;
        CrateType crate = rollCrate();
        plugin.getVirtualKeyManager().addKey(player, crate);
        return true;
    }

    // ── result record ────────────────────────────────────────────────────

    public record CrateOpenResult(List<ItemStack> pouches, CrateReward newCosmetic) {
        public boolean hasNewCosmetic() { return newCosmetic != null; }
    }

    // ── reward tables ────────────────────────────────────────────────────

    private long randomBetween(long min, long max) {
        return ThreadLocalRandom.current().nextLong(min, max + 1);
    }

    private long moneyMin(CrateType type) {
        return switch (type) {
            case COMMON -> 2_500; case UNCOMMON -> 8_000; case RARE -> 20_000;
            case EPIC -> 60_000; case LEGENDARY -> 175_000;
        };
    }
    private long moneyMax(CrateType type) {
        return switch (type) {
            case COMMON -> 6_000; case UNCOMMON -> 16_000; case RARE -> 40_000;
            case EPIC -> 110_000; case LEGENDARY -> 300_000;
        };
    }
    private long xpMin(CrateType type) {
        return switch (type) {
            case COMMON -> 100; case UNCOMMON -> 300; case RARE -> 800;
            case EPIC -> 2_000; case LEGENDARY -> 5_000;
        };
    }
    private long xpMax(CrateType type) {
        return switch (type) {
            case COMMON -> 250; case UNCOMMON -> 700; case RARE -> 1_500;
            case EPIC -> 3_500; case LEGENDARY -> 8_000;
        };
    }
    private long tokenMin(CrateType type) {
        return switch (type) {
            case COMMON -> 150; case UNCOMMON -> 400; case RARE -> 950;
            case EPIC -> 2_500; case LEGENDARY -> 6_000;
        };
    }
    private long tokenMax(CrateType type) {
        return switch (type) {
            case COMMON -> 350; case UNCOMMON -> 800; case RARE -> 1_800;
            case EPIC -> 4_500; case LEGENDARY -> 10_000;
        };
    }
}
