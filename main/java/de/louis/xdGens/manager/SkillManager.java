package de.louis.xdGens.manager;

import de.louis.xdGens.main.Main;
import de.louis.xdGens.skill.Skill;
import de.louis.xdGens.skill.SkillRegistry;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.entity.Player;

import java.io.File;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

/**
 * Persists skill levels for all players.
 * Storage: plugins/xdGens/skills.yml
 *
 * Key format: players.<uuid>.<skill-id>  → int level (0 = not unlocked)
 */
public class SkillManager {

    private final Main plugin;
    private final Map<UUID, Map<String, Integer>> data = new HashMap<>();

    private File file;
    private FileConfiguration config;

    public SkillManager(Main plugin) {
        this.plugin = plugin;
        setup();
        loadAll();
    }

    // ── I/O ───────────────────────────────────────────────────────

    private void setup() {
        plugin.getDataFolder().mkdirs();
        file = new File(plugin.getDataFolder(), "skills.yml");
        if (!file.exists()) {
            try { file.createNewFile(); } catch (IOException e) {
                plugin.getLogger().severe("Could not create skills.yml: " + e.getMessage());
            }
        }
        config = YamlConfiguration.loadConfiguration(file);
    }

    private void loadAll() {
        if (!config.isConfigurationSection("players")) return;
        for (String uuidStr : config.getConfigurationSection("players").getKeys(false)) {
            try {
                UUID uuid = UUID.fromString(uuidStr);
                Map<String, Integer> levels = new HashMap<>();
                String base = "players." + uuidStr + ".";
                for (Skill skill : SkillRegistry.all()) {
                    int lv = config.getInt(base + skill.id(), 0);
                    levels.put(skill.id(), Math.max(0, Math.min(lv, skill.maxLevel())));
                }
                data.put(uuid, levels);
            } catch (IllegalArgumentException ignored) {}
        }
        plugin.getLogger().info("Skills loaded for " + data.size() + " players.");
    }

    public void saveAll() {
        config.set("players", null);
        data.forEach((uuid, levels) -> {
            String base = "players." + uuid + ".";
            levels.forEach((id, lv) -> config.set(base + id, lv));
        });
        save();
    }

    public void savePlayer(Player player) {
        UUID uuid = player.getUniqueId();
        Map<String, Integer> levels = data.getOrDefault(uuid, Map.of());
        String base = "players." + uuid + ".";
        levels.forEach((id, lv) -> config.set(base + id, lv));
        save();
    }

    private void save() {
        try { config.save(file); } catch (IOException e) {
            plugin.getLogger().severe("Could not save skills.yml: " + e.getMessage());
        }
    }

    // ── API ──────────────────────────────────────────────────────────

    public int getLevel(Player player, String skillId) {
        return data.computeIfAbsent(player.getUniqueId(), k -> new HashMap<>())
                   .getOrDefault(skillId, 0);
    }

    public void setLevel(Player player, String skillId, int level) {
        data.computeIfAbsent(player.getUniqueId(), k -> new HashMap<>())
            .put(skillId, level);
        savePlayer(player);
    }

    /** Unlock a skill (level 0 → 1). Returns false if already unlocked. */
    public boolean unlock(Player player, String skillId) {
        if (getLevel(player, skillId) > 0) return false;
        setLevel(player, skillId, 1);
        return true;
    }

    /** Upgrade a skill by one level. Returns false if already max. */
    public boolean upgrade(Player player, String skillId) {
        Skill skill = SkillRegistry.get(skillId);
        if (skill == null) return false;
        int current = getLevel(player, skillId);
        if (current >= skill.maxLevel()) return false;
        setLevel(player, skillId, current + 1);
        return true;
    }

    public boolean isUnlocked(Player player, String skillId) {
        return getLevel(player, skillId) > 0;
    }
}
