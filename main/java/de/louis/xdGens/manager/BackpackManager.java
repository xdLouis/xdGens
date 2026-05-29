package de.louis.xdGens.manager;

import de.louis.xdGens.main.Main;
import de.louis.xdGens.util.CustomItemUtil;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.entity.Player;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;

import java.io.File;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

public class BackpackManager {

    private static final int BASE_CAPACITY      = 1000;
    private static final int CAPACITY_PER_LEVEL = 1000;
    private static final int MAX_LEVEL          = 25;
    private static final double BASE_UPGRADE_COST = 25000.0;
    private static final double COST_MULTIPLIER   = 1.35;

    private final Main plugin;
    private final Map<UUID, BackpackData> data = new HashMap<>();
    private File file;
    private FileConfiguration config;

    public BackpackManager(Main plugin) {
        this.plugin = plugin;
        setup();
        loadAll();
    }

    private void setup() {
        if (!plugin.getDataFolder().exists()) plugin.getDataFolder().mkdirs();
        file = new File(plugin.getDataFolder(), "backpacks.yml");
        try { if (!file.exists()) file.createNewFile(); }
        catch (IOException e) { plugin.getLogger().severe("Could not create backpacks.yml: " + e.getMessage()); }
        config = YamlConfiguration.loadConfiguration(file);
    }

    private void loadAll() {
        if (!config.isConfigurationSection("players")) return;
        for (String key : config.getConfigurationSection("players").getKeys(false)) {
            try {
                UUID uuid = UUID.fromString(key);
                BackpackData b = new BackpackData();
                b.level = Math.max(0, Math.min(MAX_LEVEL, config.getInt("players." + key + ".level", 0)));
                b.wheat = Math.max(0, config.getInt("players." + key + ".wheat", 0));
                data.put(uuid, b);
            } catch (IllegalArgumentException ignored) {}
        }
    }

    public void saveAll() {
        config.set("players", null);
        for (Map.Entry<UUID, BackpackData> e : data.entrySet()) {
            String path = "players." + e.getKey();
            config.set(path + ".level", e.getValue().level);
            config.set(path + ".wheat", e.getValue().wheat);
        }
        try { config.save(file); }
        catch (IOException e) { plugin.getLogger().severe("Could not save backpacks.yml: " + e.getMessage()); }
    }

    public void savePlayer(Player player) {
        BackpackData b = getOrCreate(player.getUniqueId());
        String path = "players." + player.getUniqueId();
        config.set(path + ".level", b.level);
        config.set(path + ".wheat", b.wheat);
        try { config.save(file); }
        catch (IOException e) { plugin.getLogger().severe("Could not save backpack: " + e.getMessage()); }
    }

    public int     getLevel(Player player)       { return getOrCreate(player.getUniqueId()).level; }
    public int     getStoredWheat(Player player)  { return getOrCreate(player.getUniqueId()).wheat; }
    public int     getCapacity(Player player)     { return BASE_CAPACITY + (getLevel(player) * CAPACITY_PER_LEVEL); }
    public int     getFreeSpace(Player player)    { return Math.max(0, getCapacity(player) - getStoredWheat(player)); }
    public boolean canUpgrade(Player player)      { return getLevel(player) < MAX_LEVEL; }

    public double getUpgradeCost(Player player) {
        int target = getLevel(player) + 1;
        if (target > MAX_LEVEL) return -1;
        return Math.round(BASE_UPGRADE_COST * Math.pow(COST_MULTIPLIER, target - 1));
    }

    public boolean upgrade(Player player) {
        if (!canUpgrade(player)) return false;
        double cost = getUpgradeCost(player);
        if (!plugin.getCurrencyManager().removeMoney(player, cost)) return false;
        getOrCreate(player.getUniqueId()).level++;
        savePlayer(player);
        plugin.getCurrencyManager().savePlayer(player);
        return true;
    }

    public int addWheat(Player player, int amount) {
        if (amount <= 0) return 0;
        if (!playerHasItem(player)) return 0;
        BackpackData b = getOrCreate(player.getUniqueId());
        int added = Math.min(getCapacity(player) - b.wheat, amount);
        if (added > 0) b.wheat += added;
        return Math.max(0, added);
    }

    public int removeWheat(Player player, int amount) {
        if (amount <= 0) return 0;
        BackpackData b = getOrCreate(player.getUniqueId());
        int removed = Math.min(b.wheat, amount);
        b.wheat -= removed;
        return removed;
    }

    public ItemStack createItem(Player player) { return CustomItemUtil.createBackpackItem(plugin, player); }

    public Inventory createInventory(Player player) {
        return new de.louis.xdGens.gui.BackpackGUI(plugin, player).create();
    }

    public boolean playerHasItem(Player player) {
        for (ItemStack item : player.getInventory().getContents()) {
            if (CustomItemUtil.isBackpack(plugin, item)) return true;
        }
        return false;
    }

    private BackpackData getOrCreate(UUID uuid) { return data.computeIfAbsent(uuid, k -> new BackpackData()); }

    public static class BackpackData {
        int level;
        int wheat;
    }
}
