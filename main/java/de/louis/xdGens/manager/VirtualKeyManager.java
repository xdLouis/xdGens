package de.louis.xdGens.manager;

import de.louis.xdGens.crate.CrateType;
import de.louis.xdGens.main.Main;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.entity.Player;

import java.io.File;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

/**
 * Stores crate keys virtually (no physical item in player inventory).
 * Persisted to  plugins/xdGens/virtual_keys.yml
 *
 * Layout:
 *   players:
 *     <uuid>:
 *       COMMON: 3
 *       RARE: 1
 */
public class VirtualKeyManager {

    private final Main plugin;
    private File file;
    private FileConfiguration cfg;

    // uuid → (crateType → count)
    private final Map<UUID, Map<CrateType, Integer>> keys = new HashMap<>();

    public VirtualKeyManager(Main plugin) {
        this.plugin = plugin;
        setup();
        load();
    }

    // ── public API ─────────────────────────────────────────────

    public int getKeys(Player player, CrateType type) {
        return keys.getOrDefault(player.getUniqueId(), Map.of()).getOrDefault(type, 0);
    }

    /** Returns how many keys the player currently has for the given type. */
    public int keyCount(Player player, CrateType type) {
        return getKeys(player, type);
    }

    public void addKey(Player player, CrateType type) {
        Map<CrateType, Integer> map = keys.computeIfAbsent(player.getUniqueId(), k -> new HashMap<>());
        map.merge(type, 1, Integer::sum);
        save();
    }

    /** Returns false if player has no keys of that type. */
    public boolean consumeKey(Player player, CrateType type) {
        Map<CrateType, Integer> map = keys.getOrDefault(player.getUniqueId(), null);
        if (map == null) return false;
        int current = map.getOrDefault(type, 0);
        if (current <= 0) return false;
        if (current == 1) map.remove(type);
        else map.put(type, current - 1);
        save();
        return true;
    }

    /** Consume ALL keys of a given type. Returns how many were consumed. */
    public int consumeAll(Player player, CrateType type) {
        Map<CrateType, Integer> map = keys.getOrDefault(player.getUniqueId(), null);
        if (map == null) return 0;
        int count = map.getOrDefault(type, 0);
        if (count > 0) map.remove(type);
        save();
        return count;
    }

    // ── persistence ───────────────────────────────────────────

    private void setup() {
        if (!plugin.getDataFolder().exists()) plugin.getDataFolder().mkdirs();
        file = new File(plugin.getDataFolder(), "virtual_keys.yml");
        if (!file.exists()) {
            try { file.createNewFile(); }
            catch (IOException e) { plugin.getLogger().severe("Cannot create virtual_keys.yml: " + e.getMessage()); }
        }
        cfg = YamlConfiguration.loadConfiguration(file);
    }

    private void load() {
        if (!cfg.isConfigurationSection("players")) return;
        for (String raw : cfg.getConfigurationSection("players").getKeys(false)) {
            try {
                UUID uuid = UUID.fromString(raw);
                Map<CrateType, Integer> map = new HashMap<>();
                for (CrateType type : CrateType.values()) {
                    int count = cfg.getInt("players." + raw + "." + type.name(), 0);
                    if (count > 0) map.put(type, count);
                }
                if (!map.isEmpty()) keys.put(uuid, map);
            } catch (IllegalArgumentException ignored) {}
        }
        plugin.getLogger().info("Virtual keys loaded.");
    }

    public void save() {
        for (Map.Entry<UUID, Map<CrateType, Integer>> e : keys.entrySet()) {
            String base = "players." + e.getKey();
            for (CrateType type : CrateType.values()) {
                int count = e.getValue().getOrDefault(type, 0);
                if (count > 0) cfg.set(base + "." + type.name(), count);
                else           cfg.set(base + "." + type.name(), null);
            }
        }
        try { cfg.save(file); }
        catch (IOException ex) { plugin.getLogger().severe("Cannot save virtual_keys.yml: " + ex.getMessage()); }
    }
}
