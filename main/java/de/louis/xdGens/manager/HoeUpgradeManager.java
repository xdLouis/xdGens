package de.louis.xdGens.manager;

import de.louis.xdGens.main.Main;
import de.louis.xdGens.util.HoeUtil;
import org.bukkit.Material;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;

import java.io.File;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

public class HoeUpgradeManager {

    public static final int MAX_CROP_LEVEL  = 10;
    public static final int MAX_XP_LEVEL    = 1000;
    public static final int MAX_TOKEN_LEVEL = 1000;
    public static final int MAX_KEY_FINDER_LEVEL = 1000;
    public static final int MAX_HOE_LEVEL   = 18;

    private static final int[] CROP_COSTS = {
            500, 1200, 2500, 4500, 7500, 12000, 18500, 27000, 38000, 55000
    };

    private static final double XP_BASE_COST      = 750.0;
    private static final double XP_LINEAR_SCALE   = 0.18;
    private static final double XP_EXP_SCALE      = 1.035;
    private static final double XP_GAIN_PER_LEVEL = 0.02;

    private static final double TOKEN_BASE_COST      = 900.0;
    private static final double TOKEN_LINEAR_SCALE   = 0.20;
    private static final double TOKEN_EXP_SCALE      = 1.036;
    private static final double TOKEN_GAIN_PER_LEVEL = 0.02;

    private static final double KEY_FINDER_BASE_COST      = 1500.0;
    private static final double KEY_FINDER_LINEAR_SCALE   = 0.22;
    private static final double KEY_FINDER_EXP_SCALE      = 1.037;
    private static final double KEY_FINDER_BASE_CHANCE    = 0.00015;
    private static final double KEY_FINDER_MAX_CHANCE     = 0.0125;

    private static final double HOE_XP_GAIN_PER_LEVEL = 0.04;
    private static final float  BASE_WALK_SPEED       = 0.20f;
    private static final float  MAX_WALK_SPEED        = 0.70f;

    private static final long[] HOE_COSTS = {
            2500, 4000, 6000,
            9000, 13000, 18000,
            25000, 34000, 46000,
            62000, 82000, 108000,
            140000, 180000, 230000,
            290000, 360000
    };

    private final Main plugin;
    private final Map<UUID, Integer> cropLevels  = new HashMap<>();
    private final Map<UUID, Integer> xpLevels    = new HashMap<>();
    private final Map<UUID, Integer> tokenLevels = new HashMap<>();
    private final Map<UUID, Integer> keyFinderLevels = new HashMap<>();
    private final Map<UUID, Integer> hoeLevels   = new HashMap<>();

    private File              file;
    private FileConfiguration config;

    public HoeUpgradeManager(Main plugin) {
        this.plugin = plugin;
        setup();
        loadAll();
    }

    private void setup() {
        if (!plugin.getDataFolder().exists()) plugin.getDataFolder().mkdirs();
        file = new File(plugin.getDataFolder(), "upgrades.yml");
        if (!file.exists()) {
            try { file.createNewFile(); }
            catch (IOException e) { plugin.getLogger().severe("Could not create upgrades.yml: " + e.getMessage()); }
        }
        config = YamlConfiguration.loadConfiguration(file);
    }

    private void loadAll() {
        if (!config.isConfigurationSection("players")) return;
        for (String key : config.getConfigurationSection("players").getKeys(false)) {
            try {
                UUID uuid = UUID.fromString(key);
                cropLevels.put(uuid, clamp(config.getInt("players." + key + ".crop_harvest", 0), MAX_CROP_LEVEL));
                xpLevels.put(uuid, clamp(config.getInt("players." + key + ".xp_boost", 0), MAX_XP_LEVEL));
                tokenLevels.put(uuid, clamp(config.getInt("players." + key + ".token_boost", 0), MAX_TOKEN_LEVEL));
                keyFinderLevels.put(uuid, clamp(config.getInt("players." + key + ".key_finder", 0), MAX_KEY_FINDER_LEVEL));
                hoeLevels.put(uuid, clampHoe(config.getInt("players." + key + ".hoe_material", 1)));
            } catch (IllegalArgumentException ignored) {
                plugin.getLogger().warning("Invalid UUID in upgrades.yml: " + key);
            }
        }
        plugin.getLogger().info("Upgrade data loaded: " + getKnownPlayerCount());
    }

    public void saveAll() {
        for (UUID uuid : getAllKnownPlayers()) {
            String path = "players." + uuid;
            config.set(path + ".crop_harvest", getCropLevel(uuid));
            config.set(path + ".xp_boost", getXpLevel(uuid));
            config.set(path + ".token_boost", getTokenLevel(uuid));
            config.set(path + ".key_finder", getKeyFinderLevel(uuid));
            config.set(path + ".hoe_material", getHoeLevel(uuid));
        }
        try { config.save(file); }
        catch (IOException e) { plugin.getLogger().severe("Could not save upgrades.yml: " + e.getMessage()); }
    }

    public int getCropLevel(Player player) { return getCropLevel(player.getUniqueId()); }
    public int getCropBonus(Player player) { return getCropLevel(player); }
    public int getXpLevel(Player player) { return getXpLevel(player.getUniqueId()); }
    public int getTokenLevel(Player player) { return getTokenLevel(player.getUniqueId()); }
    public int getKeyFinderLevel(Player player) { return getKeyFinderLevel(player.getUniqueId()); }
    public int getHoeLevel(Player player) { return getHoeLevel(player.getUniqueId()); }

    public double getXpMultiplier(Player player) { return 1.0 + (getXpLevel(player) * XP_GAIN_PER_LEVEL) + getHoeXpBonus(player); }
    public double getXpPercentBonus(Player player) { return (getXpMultiplier(player) - 1.0) * 100.0; }
    public double getTokenMultiplier(Player player) { return 1.0 + (getTokenLevel(player) * TOKEN_GAIN_PER_LEVEL); }
    public double getTokenPercentBonus(Player player) { return getTokenLevel(player) * (TOKEN_GAIN_PER_LEVEL * 100.0); }
    public double getKeyFinderChance(Player player) {
        int level = getKeyFinderLevel(player);
        double progress = (double) level / MAX_KEY_FINDER_LEVEL;
        return KEY_FINDER_BASE_CHANCE + ((KEY_FINDER_MAX_CHANCE - KEY_FINDER_BASE_CHANCE) * Math.pow(progress, 0.82));
    }

    public int getHoeStageInMaterial(Player player) { return ((getHoeLevel(player) - 1) % 3) + 1; }
    public String getHoeMaterialName(Player player) { return getHoeMaterialName(getHoeLevel(player)); }
    public Material getHoeMaterial(Player player) { return getHoeMaterial(getHoeLevel(player)); }
    public double getHoeXpBonus(Player player) { return (getHoeLevel(player) - 1) * HOE_XP_GAIN_PER_LEVEL; }
    public double getHoeXpPercentBonus(Player player) { return getHoeXpBonus(player) * 100.0; }

    public float getWalkSpeed(Player player) {
        int level = getHoeLevel(player);
        if (MAX_HOE_LEVEL <= 1) return BASE_WALK_SPEED;
        float progress = (float) (level - 1) / (MAX_HOE_LEVEL - 1);
        return BASE_WALK_SPEED + ((MAX_WALK_SPEED - BASE_WALK_SPEED) * progress);
    }

    public int getCropCost(int targetLevel) {
        if (targetLevel < 1 || targetLevel > MAX_CROP_LEVEL) return -1;
        return CROP_COSTS[targetLevel - 1];
    }

    public long getXpCost(int targetLevel) {
        if (targetLevel < 1 || targetLevel > MAX_XP_LEVEL) return -1;
        return Math.round(XP_BASE_COST * (1.0 + (XP_LINEAR_SCALE * targetLevel)) * Math.pow(XP_EXP_SCALE, targetLevel - 1));
    }

    public long getTokenCost(int targetLevel) {
        if (targetLevel < 1 || targetLevel > MAX_TOKEN_LEVEL) return -1;
        return Math.round(TOKEN_BASE_COST * (1.0 + (TOKEN_LINEAR_SCALE * targetLevel)) * Math.pow(TOKEN_EXP_SCALE, targetLevel - 1));
    }

    public long getKeyFinderCost(int targetLevel) {
        if (targetLevel < 1 || targetLevel > MAX_KEY_FINDER_LEVEL) return -1;
        return Math.round(KEY_FINDER_BASE_COST * (1.0 + (KEY_FINDER_LINEAR_SCALE * targetLevel)) * Math.pow(KEY_FINDER_EXP_SCALE, targetLevel - 1));
    }

    public long getHoeCost(int targetLevel) {
        if (targetLevel <= 1 || targetLevel > MAX_HOE_LEVEL) return -1;
        return HOE_COSTS[targetLevel - 2];
    }

    public boolean upgradeCrop(Player player) { return upgradeCropBulk(player, 1) == 1; }
    public boolean upgradeXp(Player player) { return upgradeXpBulk(player, 1) == 1; }
    public boolean upgradeToken(Player player) { return upgradeTokenBulk(player, 1) == 1; }
    public boolean upgradeKeyFinder(Player player) { return upgradeKeyFinderBulk(player, 1) == 1; }

    public boolean upgradeHoe(Player player) {
        int current = getHoeLevel(player);
        if (current >= MAX_HOE_LEVEL) return false;
        long cost = getHoeCost(current + 1);
        if (!plugin.getCurrencyManager().removeMoney(player, cost)) return false;
        hoeLevels.put(player.getUniqueId(), current + 1);
        applyHoeStats(player);
        saveAll();
        return true;
    }

    public int upgradeCropBulk(Player player, int amount) {
        int bought = 0;
        for (int i = 0; i < amount; i++) {
            int current = getCropLevel(player);
            if (current >= MAX_CROP_LEVEL) break;
            int cost = getCropCost(current + 1);
            if (cost < 0 || !plugin.getCurrencyManager().removeTokens(player, cost)) break;
            cropLevels.put(player.getUniqueId(), current + 1);
            bought++;
        }
        if (bought > 0) saveAll();
        return bought;
    }

    public int upgradeXpBulk(Player player, int amount) {
        int bought = 0;
        for (int i = 0; i < amount; i++) {
            int current = getXpLevel(player);
            if (current >= MAX_XP_LEVEL) break;
            long cost = getXpCost(current + 1);
            if (cost < 0 || !plugin.getCurrencyManager().removeTokens(player, cost)) break;
            xpLevels.put(player.getUniqueId(), current + 1);
            bought++;
        }
        if (bought > 0) saveAll();
        return bought;
    }

    public int upgradeTokenBulk(Player player, int amount) {
        int bought = 0;
        for (int i = 0; i < amount; i++) {
            int current = getTokenLevel(player);
            if (current >= MAX_TOKEN_LEVEL) break;
            long cost = getTokenCost(current + 1);
            if (cost < 0 || !plugin.getCurrencyManager().removeTokens(player, cost)) break;
            tokenLevels.put(player.getUniqueId(), current + 1);
            bought++;
        }
        if (bought > 0) saveAll();
        return bought;
    }

    public int upgradeKeyFinderBulk(Player player, int amount) {
        int bought = 0;
        for (int i = 0; i < amount; i++) {
            int current = getKeyFinderLevel(player);
            if (current >= MAX_KEY_FINDER_LEVEL) break;
            long cost = getKeyFinderCost(current + 1);
            if (cost < 0 || !plugin.getCurrencyManager().removeTokens(player, cost)) break;
            keyFinderLevels.put(player.getUniqueId(), current + 1);
            bought++;
        }
        if (bought > 0) saveAll();
        return bought;
    }

    public void applyHoeStats(Player player) {
        player.setWalkSpeed(getWalkSpeed(player));
        syncHoeItem(player);
    }

    public void syncHoeItem(Player player) {
        ItemStack mainHand = player.getInventory().getItemInMainHand();
        if (!HoeUtil.isXdHoe(mainHand)) return;
        player.getInventory().setItemInMainHand(HoeUtil.updateHoeItem(plugin, mainHand, getHoeLevel(player)));
    }

    private Material getHoeMaterial(int level) {
        int group = (level - 1) / 3;
        return switch (group) {
            case 0 -> Material.WOODEN_HOE;
            case 1 -> Material.STONE_HOE;
            case 2 -> Material.IRON_HOE;
            case 3 -> Material.GOLDEN_HOE;
            case 4 -> Material.DIAMOND_HOE;
            default -> Material.NETHERITE_HOE;
        };
    }

    private String getHoeMaterialName(int level) {
        int group = (level - 1) / 3;
        return switch (group) {
            case 0 -> "Wooden";
            case 1 -> "Stone";
            case 2 -> "Iron";
            case 3 -> "Gold";
            case 4 -> "Diamond";
            default -> "Netherite";
        };
    }

    private int getCropLevel(UUID uuid) { return cropLevels.getOrDefault(uuid, 0); }
    private int getXpLevel(UUID uuid) { return xpLevels.getOrDefault(uuid, 0); }
    private int getTokenLevel(UUID uuid) { return tokenLevels.getOrDefault(uuid, 0); }
    private int getKeyFinderLevel(UUID uuid) { return keyFinderLevels.getOrDefault(uuid, 0); }
    private int getHoeLevel(UUID uuid) { return hoeLevels.getOrDefault(uuid, 1); }

    private int clamp(int v, int max) { return Math.max(0, Math.min(max, v)); }
    private int clampHoe(int v) { return Math.max(1, Math.min(MAX_HOE_LEVEL, v)); }

    private int getKnownPlayerCount() {
        return (int) getAllKnownPlayers().spliterator().estimateSize();
    }

    private Iterable<UUID> getAllKnownPlayers() {
        Map<UUID, Boolean> all = new HashMap<>();
        cropLevels.keySet().forEach(u -> all.put(u, true));
        xpLevels.keySet().forEach(u -> all.put(u, true));
        tokenLevels.keySet().forEach(u -> all.put(u, true));
        keyFinderLevels.keySet().forEach(u -> all.put(u, true));
        hoeLevels.keySet().forEach(u -> all.put(u, true));
        return all.keySet();
    }
}
