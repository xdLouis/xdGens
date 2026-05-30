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

public class CurrencyManager {

    private final Main plugin;
    private final Map<UUID, PlayerData> data = new HashMap<>();

    private File file;
    private FileConfiguration config;

    public CurrencyManager(Main plugin) {
        this.plugin = plugin;
        setup();
        loadAll();
    }

    private void setup() {
        if (!plugin.getDataFolder().exists()) {
            plugin.getDataFolder().mkdirs();
        }

        file = new File(plugin.getDataFolder(), "currency.yml");
        if (!file.exists()) {
            try {
                file.createNewFile();
            } catch (IOException e) {
                plugin.getLogger().severe("Could not create currency.yml: " + e.getMessage());
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

                double money = config.getDouble("players." + key + ".money", 0.0);
                long tokens = config.getLong("players." + key + ".tokens", 0L);

                PlayerData playerData = new PlayerData();
                playerData.money = money;
                playerData.tokens = tokens;

                data.put(uuid, playerData);
            } catch (IllegalArgumentException ignored) {
                plugin.getLogger().warning("Invalid UUID in currency.yml: " + key);
            }
        }
    }

    public void saveAll() {
        config.set("players", null);

        for (Map.Entry<UUID, PlayerData> entry : data.entrySet()) {
            String path = "players." + entry.getKey();
            PlayerData playerData = entry.getValue();

            config.set(path + ".money", playerData.money);
            config.set(path + ".tokens", playerData.tokens);
        }

        try {
            config.save(file);
        } catch (IOException e) {
            plugin.getLogger().severe("Could not save currency.yml: " + e.getMessage());
        }
    }

    public void savePlayer(Player player) {
        UUID uuid = player.getUniqueId();
        PlayerData playerData = getOrCreate(uuid);

        String path = "players." + uuid;
        config.set(path + ".money", playerData.money);
        config.set(path + ".tokens", playerData.tokens);

        try {
            config.save(file);
        } catch (IOException e) {
            plugin.getLogger().severe("Could not save player currency: " + e.getMessage());
        }
    }

    public double getMoney(Player player) {
        return getOrCreate(player.getUniqueId()).money;
    }

    public long getTokens(Player player) {
        return getOrCreate(player.getUniqueId()).tokens;
    }

    public void addMoney(Player player, double amount) {
        if (amount <= 0) {
            return;
        }

        getOrCreate(player.getUniqueId()).money += amount;
        updateScoreboard(player);
    }

    public boolean removeMoney(Player player, double amount) {
        if (amount <= 0) {
            return true;
        }

        PlayerData data = getOrCreate(player.getUniqueId());
        if (data.money < amount) {
            return false;
        }

        data.money -= amount;
        updateScoreboard(player);
        return true;
    }

    public void addTokens(Player player, long amount) {
        if (amount <= 0) {
            return;
        }

        getOrCreate(player.getUniqueId()).tokens += amount;
        updateScoreboard(player);
    }

    public boolean removeTokens(Player player, long amount) {
        if (amount <= 0) {
            return true;
        }

        PlayerData data = getOrCreate(player.getUniqueId());
        if (data.tokens < amount) {
            return false;
        }

        data.tokens -= amount;
        updateScoreboard(player);
        return true;
    }

    public void setMoney(Player player, double amount) {
        getOrCreate(player.getUniqueId()).money = Math.max(0.0, amount);
        updateScoreboard(player);
    }

    public void setTokens(Player player, long amount) {
        getOrCreate(player.getUniqueId()).tokens = Math.max(0L, amount);
        updateScoreboard(player);
    }

    private PlayerData getOrCreate(UUID uuid) {
        return data.computeIfAbsent(uuid, ignored -> new PlayerData());
    }

    private void updateScoreboard(Player player) {
        if (plugin.getScoreboardManager() != null) {
            plugin.getScoreboardManager().update(player);
        }
    }

    private static class PlayerData {
        double money = 0.0;
        long tokens = 0L;
    }
}