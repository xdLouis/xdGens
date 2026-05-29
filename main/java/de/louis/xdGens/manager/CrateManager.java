package de.louis.xdGens.manager;

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

    public CrateType rollCrate() {
        double total = 0.0;
        for (CrateType type : CrateType.values()) total += type.getWeight();

        double random = ThreadLocalRandom.current().nextDouble() * total;
        double current = 0.0;
        for (CrateType type : CrateType.values()) {
            current += type.getWeight();
            if (random <= current) return type;
        }
        return CrateType.COMMON;
    }

    public ItemStack createKey(CrateType type) {
        return PouchKeyItem.create(plugin, type);
    }

    public List<ItemStack> createRewards(CrateType type) {
        List<ItemStack> rewards = new ArrayList<>();
        rewards.add(PouchItem.create(plugin, PouchType.MONEY, randomBetween(moneyMin(type), moneyMax(type)), type));
        rewards.add(PouchItem.create(plugin, PouchType.XP, randomBetween(xpMin(type), xpMax(type)), type));
        rewards.add(PouchItem.create(plugin, PouchType.TOKENS, randomBetween(tokenMin(type), tokenMax(type)), type));
        return rewards;
    }

    public boolean tryGiveRandomKey(Player player) {
        double chance = plugin.getHoeUpgradeManager().getKeyFinderChance(player);
        if (ThreadLocalRandom.current().nextDouble() > chance) return false;

        CrateType crate = rollCrate();
        player.getInventory().addItem(createKey(crate));
        return true;
    }

    private long randomBetween(long min, long max) {
        return ThreadLocalRandom.current().nextLong(min, max + 1);
    }

    private long moneyMin(CrateType type) {
        return switch (type) {
            case COMMON -> 2_500;
            case UNCOMMON -> 8_000;
            case RARE -> 20_000;
            case EPIC -> 60_000;
            case LEGENDARY -> 175_000;
        };
    }

    private long moneyMax(CrateType type) {
        return switch (type) {
            case COMMON -> 6_000;
            case UNCOMMON -> 16_000;
            case RARE -> 40_000;
            case EPIC -> 110_000;
            case LEGENDARY -> 300_000;
        };
    }

    private long xpMin(CrateType type) {
        return switch (type) {
            case COMMON -> 100;
            case UNCOMMON -> 300;
            case RARE -> 800;
            case EPIC -> 2_000;
            case LEGENDARY -> 5_000;
        };
    }

    private long xpMax(CrateType type) {
        return switch (type) {
            case COMMON -> 250;
            case UNCOMMON -> 700;
            case RARE -> 1_500;
            case EPIC -> 3_500;
            case LEGENDARY -> 8_000;
        };
    }

    private long tokenMin(CrateType type) {
        return switch (type) {
            case COMMON -> 150;
            case UNCOMMON -> 400;
            case RARE -> 950;
            case EPIC -> 2_500;
            case LEGENDARY -> 6_000;
        };
    }

    private long tokenMax(CrateType type) {
        return switch (type) {
            case COMMON -> 350;
            case UNCOMMON -> 800;
            case RARE -> 1_800;
            case EPIC -> 4_500;
            case LEGENDARY -> 10_000;
        };
    }
}
