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

    public static final int MAX_LEVEL = 10;

    private static final int[] TOKEN_COSTS = {
            500, 1200, 2500, 4500, 7500, 12000, 18500, 27000, 38000, 55000
    };

    private final Main plugin;
    private final Map<UUID, Integer> cropLevels = new HashMap<>();

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
                cropLevels.put(uuid, Math.max(0, Math.min(MAX_LEVEL, cropLevel)));
            } catch (IllegalArgumentException ignored) {
                plugin.getLogger().warning("Invalid UUID in upgrades.yml: " + key);
            }
        }

        plugin.getLogger().info("Upgrade data loaded: " + cropLevels.size());
    }

    public void saveAll() {
        config.set("players", null);

        for (Map.Entry<UUID, Integer> entry : cropLevels.entrySet()) {
            config.set("players." + entry.getKey() + ".crop_harvest", entry.getValue());
        }

        try {
            config.save(file);
        } catch (IOException e) {
            plugin.getLogger().severe("Could not save upgrades.yml: " + e.getMessage());
        }
    }

    public int getCropLevel(Player player) {
        return cropLevels.getOrDefault(player.getUniqueId(), 0);
    }

    public int getCropBonus(Player player) {
        return getCropLevel(player);
    }

    public int getTokenCost(int targetLevel) {
        if (targetLevel < 1 || targetLevel > MAX_LEVEL) return -1;
        return TOKEN_COSTS[targetLevel - 1];
    }

    public boolean upgradeCrop(Player player) {
        int current = getCropLevel(player);

        if (current >= MAX_LEVEL) {
            return false;
        }

        int targetLevel = current + 1;
        int cost = getTokenCost(targetLevel);

        if (!plugin.getCurrencyManager().removeTokens(player, cost)) {
            return false;
        }

        cropLevels.put(player.getUniqueId(), targetLevel);
        saveAll();
        return true;
    }
}