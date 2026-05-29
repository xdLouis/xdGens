package de.louis.xdGens.manager;

import de.louis.xdGens.crate.CrateReward;
import de.louis.xdGens.main.Main;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.entity.Player;

import java.io.File;
import java.io.IOException;
import java.util.*;

/**
 * Stores which chat-tags and chat-colours each player has unlocked.
 * Active tag/colour is also persisted so it survives restarts.
 *
 * Data layout in cosmetics.yml:
 *   players:
 *     <uuid>:
 *       unlocked: [TAG_FARMER, COLOR_AQUA, ...]
 *       active_tag: TAG_FARMER          # optional
 *       active_color: COLOR_AQUA        # optional
 */
public class PlayerCosmeticManager {

    private final Main plugin;
    private File file;
    private FileConfiguration cfg;

    // uuid → set of unlocked CrateReward names
    private final Map<UUID, Set<String>> unlocked    = new HashMap<>();
    private final Map<UUID, String>      activeTag   = new HashMap<>();
    private final Map<UUID, String>      activeColor = new HashMap<>();

    public PlayerCosmeticManager(Main plugin) {
        this.plugin = plugin;
        setup();
        load();
    }

    // ── public API ──────────────────────────────────────────────────────

    public boolean hasCosmetic(Player player, CrateReward reward) {
        return unlocked.getOrDefault(player.getUniqueId(), Set.of()).contains(reward.name());
    }

    /** Returns true if this was a NEW unlock. */
    public boolean unlock(Player player, CrateReward reward) {
        Set<String> set = unlocked.computeIfAbsent(player.getUniqueId(), k -> new LinkedHashSet<>());
        boolean added = set.add(reward.name());
        save();
        return added;
    }

    public Set<CrateReward> getUnlockedTags(Player player) {
        return resolveSet(player, true);
    }

    public Set<CrateReward> getUnlockedColors(Player player) {
        return resolveSet(player, false);
    }

    public Optional<CrateReward> getActiveTag(Player player) {
        return resolveReward(activeTag.get(player.getUniqueId()));
    }

    public Optional<CrateReward> getActiveColor(Player player) {
        return resolveReward(activeColor.get(player.getUniqueId()));
    }

    public void setActiveTag(Player player, CrateReward reward) {
        if (reward == null) activeTag.remove(player.getUniqueId());
        else activeTag.put(player.getUniqueId(), reward.name());
        save();
    }

    public void setActiveColor(Player player, CrateReward reward) {
        if (reward == null) activeColor.remove(player.getUniqueId());
        else activeColor.put(player.getUniqueId(), reward.name());
        save();
    }

    /** Build the chat prefix component string for a player (may be empty). */
    public String buildTagFormat(Player player) {
        return getActiveTag(player)
                .map(CrateReward::getCosmeticFormat)
                .orElse("");
    }

    /** Build the name colour/gradient component string for a player. */
    public String buildNameFormat(Player player) {
        return getActiveColor(player)
                .map(r -> r.getCosmeticFormat().replace("{name}", player.getName()))
                .orElse(player.getName());
    }

    // ── persistence ─────────────────────────────────────────────────────

    private void setup() {
        if (!plugin.getDataFolder().exists()) plugin.getDataFolder().mkdirs();
        file = new File(plugin.getDataFolder(), "cosmetics.yml");
        if (!file.exists()) {
            try { file.createNewFile(); }
            catch (IOException e) { plugin.getLogger().severe("Cannot create cosmetics.yml: " + e.getMessage()); }
        }
        cfg = YamlConfiguration.loadConfiguration(file);
    }

    private void load() {
        if (!cfg.isConfigurationSection("players")) return;
        for (String raw : cfg.getConfigurationSection("players").getKeys(false)) {
            try {
                UUID uuid = UUID.fromString(raw);
                List<String> list = cfg.getStringList("players." + raw + ".unlocked");
                unlocked.put(uuid, new LinkedHashSet<>(list));
                String tag   = cfg.getString("players." + raw + ".active_tag");
                String color = cfg.getString("players." + raw + ".active_color");
                if (tag   != null) activeTag.put(uuid, tag);
                if (color != null) activeColor.put(uuid, color);
            } catch (IllegalArgumentException ignored) {}
        }
        plugin.getLogger().info("Cosmetics loaded for " + unlocked.size() + " players.");
    }

    public void save() {
        Set<UUID> all = new HashSet<>();
        all.addAll(unlocked.keySet());
        all.addAll(activeTag.keySet());
        all.addAll(activeColor.keySet());
        for (UUID uuid : all) {
            String base = "players." + uuid;
            cfg.set(base + ".unlocked", new ArrayList<>(unlocked.getOrDefault(uuid, Set.of())));
            cfg.set(base + ".active_tag",   activeTag.get(uuid));
            cfg.set(base + ".active_color", activeColor.get(uuid));
        }
        try { cfg.save(file); }
        catch (IOException e) { plugin.getLogger().severe("Cannot save cosmetics.yml: " + e.getMessage()); }
    }

    // ── helpers ──────────────────────────────────────────────────────────

    private Set<CrateReward> resolveSet(Player player, boolean tags) {
        Set<CrateReward> result = new LinkedHashSet<>();
        for (String name : unlocked.getOrDefault(player.getUniqueId(), Set.of())) {
            try {
                CrateReward r = CrateReward.valueOf(name);
                if (tags ? r.isTag() : r.isColor()) result.add(r);
            } catch (IllegalArgumentException ignored) {}
        }
        return result;
    }

    private Optional<CrateReward> resolveReward(String name) {
        if (name == null) return Optional.empty();
        try { return Optional.of(CrateReward.valueOf(name)); }
        catch (IllegalArgumentException e) { return Optional.empty(); }
    }
}
