package de.louis.xdGens.manager;

import de.louis.xdGens.main.Main;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.entity.Player;

import java.io.File;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

public class HoeUpgradeManager {

    public static final int MAX_CROP_LEVEL = 10;
    public static final int MAX_XP_LEVEL = 1000;
    public static final int MAX_TOKEN_LEVEL = 1000;

    private static final int[] CROP_COSTS = {
            500, 1200, 2500, 4500, 7500, 12000, 18500, 27000, 38000, 55000
    };

    private static final double XP_BASE_COST = 750.0;
    private static final double XP_LINEAR_SCALE = 0.18;
    private static final double XP_EXP_SCALE = 1.035;
    private static final double XP_GAIN_PER_LEVEL = 0.02;

    private static final double TOKEN_BASE_COST = 900.0;
    private static final double TOKEN_LINEAR_SCALE = 0.20;
    private static final double TOKEN_EXP_SCALE = 1.036;
    private static final double TOKEN_GAIN_PER_LEVEL = 0.02;

    private final Main plugin;
    private final Map<UUID, Integer> cropLevels = new HashMap<>();
    private final Map<UUID, Integer> xpLevels = new HashMap<>();
    private final Map<UUID, Integer> tokenLevels = new HashMap<>();

    private File file;
    private FileConfiguration config;

    public HoeUpgradeManager(Main plugin) {
        this.plugin = plugin;
        setup();
        loadAll();
    }

    private void setup() {
        if (!plugin.getDataFolder().exists()) {
            plugin.getDataFolder().mkdirs();
        }

        file = new File(plugin.getDataFolder(), "upgrades.yml");
        if (!file.exists()) {
            try {
                file.createNewFile();
            } catch (IOException e) {
                plugin.getLogger().severe("Could not create upgrades.yml: " + e.getMessage());
            }
        }

        config = YamlConfiguration.loadConfiguration(file);
    }

    private void loadAll() {
        if (!config.isConfigurationSection("players")) {
            return;
        }

        for (String key : config.getConfigurationSection("players").getKeys(false)) {
            try {
                UUID uuid = UUID.fromString(key);

                int cropLevel = config.getInt("players." + key + ".crop_harvest", 0);
                int xpLevel = config.getInt("players." + key + ".xp_boost", 0);
                int tokenLevel = config.getInt("players." + key + ".token_boost", 0);

                cropLevels.put(uuid, clamp(cropLevel, MAX_CROP_LEVEL));
                xpLevels.put(uuid, clamp(xpLevel, MAX_XP_LEVEL));
                tokenLevels.put(uuid, clamp(tokenLevel, MAX_TOKEN_LEVEL));
            } catch (IllegalArgumentException ignored) {
                plugin.getLogger().warning("Invalid UUID in upgrades.yml: " + key);
            }
        }

        plugin.getLogger().info("Upgrade data loaded: " + getKnownPlayerCount());
    }

    public void saveAll() {
        config.set("players", null);

        for (UUID uuid : getAllKnownPlayers()) {
            config.set("players." + uuid + ".crop_harvest", getCropLevel(uuid));
            config.set("players." + uuid + ".xp_boost", getXpLevel(uuid));
            config.set("players." + uuid + ".token_boost", getTokenLevel(uuid));
        }

        try {
            config.save(file);
        } catch (IOException e) {
            plugin.getLogger().severe("Could not save upgrades.yml: " + e.getMessage());
        }
    }

    public int getCropLevel(Player player) {
        return getCropLevel(player.getUniqueId());
    }

    public int getCropBonus(Player player) {
        return getCropLevel(player);
    }

    public int getXpLevel(Player player) {
        return getXpLevel(player.getUniqueId());
    }

    public double getXpMultiplier(Player player) {
        return 1.0 + (getXpLevel(player) * XP_GAIN_PER_LEVEL);
    }

    public double getXpPercentBonus(Player player) {
        return getXpLevel(player) * (XP_GAIN_PER_LEVEL * 100.0);
    }

    public int getTokenLevel(Player player) {
        return getTokenLevel(player.getUniqueId());
    }

    public double getTokenMultiplier(Player player) {
        return 1.0 + (getTokenLevel(player) * TOKEN_GAIN_PER_LEVEL);
    }

    public double getTokenPercentBonus(Player player) {
        return getTokenLevel(player) * (TOKEN_GAIN_PER_LEVEL * 100.0);
    }

    public int getCropCost(int targetLevel) {
        if (targetLevel < 1 || targetLevel > MAX_CROP_LEVEL) {
            return -1;
        }
        return CROP_COSTS[targetLevel - 1];
    }

    public long getXpCost(int targetLevel) {
        if (targetLevel < 1 || targetLevel > MAX_XP_LEVEL) {
            return -1;
        }

        double cost = XP_BASE_COST
                * (1.0 + (XP_LINEAR_SCALE * targetLevel))
                * Math.pow(XP_EXP_SCALE, targetLevel - 1);

        return Math.round(cost);
    }

    public long getTokenCost(int targetLevel) {
        if (targetLevel < 1 || targetLevel > MAX_TOKEN_LEVEL) {
            return -1;
        }

        double cost = TOKEN_BASE_COST
                * (1.0 + (TOKEN_LINEAR_SCALE * targetLevel))
                * Math.pow(TOKEN_EXP_SCALE, targetLevel - 1);

        return Math.round(cost);
    }

    public boolean upgradeCrop(Player player) {
        int current = getCropLevel(player);
        if (current >= MAX_CROP_LEVEL) {
            return false;
        }

        int next = current + 1;
        int cost = getCropCost(next);

        if (!plugin.getCurrencyManager().removeTokens(player, cost)) {
            return false;
        }

        cropLevels.put(player.getUniqueId(), next);
        saveAll();
        return true;
    }

    public boolean upgradeXp(Player player) {
        int current = getXpLevel(player.getUniqueId());
        if (current >= MAX_XP_LEVEL) return false;

        int next = current + 1;
        long cost = getXpCost(next);

        long tokens = plugin.getCurrencyManager().getTokens(player);
        if (tokens < cost) return false;

        plugin.getCurrencyManager().removeTokens(player, Math.toIntExact(cost));

        xpLevels.put(player.getUniqueId(), next);
        saveAll();
        return true;
    }

    public boolean upgradeToken(Player player) {
        int current = getTokenLevel(player);
        if (current >= MAX_TOKEN_LEVEL) {
            return false;
        }

        int next = current + 1;
        long cost = getTokenCost(next);

        if (!plugin.getCurrencyManager().removeTokens(player, Math.toIntExact(cost))) {
            return false;
        }

        tokenLevels.put(player.getUniqueId(), next);
        saveAll();
        return true;
    }

    private int getCropLevel(UUID uuid) {
        return cropLevels.getOrDefault(uuid, 0);
    }

    private int getXpLevel(UUID uuid) {
        return xpLevels.getOrDefault(uuid, 0);
    }

    private int getTokenLevel(UUID uuid) {
        return tokenLevels.getOrDefault(uuid, 0);
    }

    private int clamp(int value, int max) {
        return Math.max(0, Math.min(max, value));
    }

    private int getKnownPlayerCount() {
        int count = 0;
        for (UUID ignored : getAllKnownPlayers()) {
            count++;
        }
        return count;
    }

    private Iterable<UUID> getAllKnownPlayers() {
        Map<UUID, Boolean> all = new HashMap<>();
        cropLevels.keySet().forEach(uuid -> all.put(uuid, true));
        xpLevels.keySet().forEach(uuid -> all.put(uuid, true));
        tokenLevels.keySet().forEach(uuid -> all.put(uuid, true));
        return all.keySet();
    }
}