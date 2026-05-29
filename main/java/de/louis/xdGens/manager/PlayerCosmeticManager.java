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
 * Stores unlocked cosmetics + active selections for each player.
 *
 * Data layout in cosmetics.yml:
 *   players:
 *     <uuid>:
 *       unlocked: [TAG_FARMER, COLOR_AQUA, CHAT_COLOR_GREEN, ...]
 *       active_tag:        TAG_FARMER
 *       active_color:      COLOR_AQUA
 *       active_chat_color: CHAT_COLOR_GREEN
 */
public class PlayerCosmeticManager {

    private final Main plugin;
    private File file;
    private FileConfiguration cfg;

    private final Map<UUID, Set<String>> unlocked        = new HashMap<>();
    private final Map<UUID, String>      activeTag       = new HashMap<>();
    private final Map<UUID, String>      activeColor     = new HashMap<>();
    private final Map<UUID, String>      activeChatColor = new HashMap<>();

    public PlayerCosmeticManager(Main plugin) {
        this.plugin = plugin;
        setup();
        load();
    }

    // ── public API ──────────────────────────────────────────────────────

    public boolean hasCosmetic(Player player, CrateReward reward) {
        return unlocked.getOrDefault(player.getUniqueId(), Set.of()).contains(reward.name());
    }

    public boolean unlock(Player player, CrateReward reward) {
        Set<String> set = unlocked.computeIfAbsent(player.getUniqueId(), k -> new LinkedHashSet<>());
        boolean added = set.add(reward.name());
        save();
        return added;
    }

    public Set<CrateReward> getUnlockedTags(Player player)       { return resolveSet(player, CrateReward.Type.TAG); }
    public Set<CrateReward> getUnlockedColors(Player player)     { return resolveSet(player, CrateReward.Type.NAME_COLOR); }
    public Set<CrateReward> getUnlockedChatColors(Player player) { return resolveSet(player, CrateReward.Type.CHAT_COLOR); }

    public Optional<CrateReward> getActiveTag(Player player)       { return resolveReward(activeTag.get(player.getUniqueId())); }
    public Optional<CrateReward> getActiveColor(Player player)     { return resolveReward(activeColor.get(player.getUniqueId())); }
    public Optional<CrateReward> getActiveChatColor(Player player) { return resolveReward(activeChatColor.get(player.getUniqueId())); }

    public void setActiveTag(Player player, CrateReward r)       { setActive(activeTag,       player, r); }
    public void setActiveColor(Player player, CrateReward r)     { setActive(activeColor,     player, r); }
    public void setActiveChatColor(Player player, CrateReward r) { setActive(activeChatColor, player, r); }

    /** Returns the MiniMessage tag-string for the player's active chat tag (may be empty). */
    public String buildTagFormat(Player player) {
        return getActiveTag(player).map(CrateReward::getCosmeticFormat).orElse("");
    }

    /** Returns the name wrapped in the player's active name-color (or plain name). */
    public String buildNameFormat(Player player) {
        return getActiveColor(player)
                .map(r -> r.getCosmeticFormat().replace("{name}", player.getName()))
                .orElse(player.getName());
    }

    /** Returns the message wrapped in the player's active chat-color (or plain {msg}). */
    public String buildChatFormat(Player player, String escapedMsg) {
        return getActiveChatColor(player)
                .map(r -> r.getCosmeticFormat().replace("{msg}", escapedMsg))
                .orElse(escapedMsg);
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
                String tag       = cfg.getString("players." + raw + ".active_tag");
                String color     = cfg.getString("players." + raw + ".active_color");
                String chatColor = cfg.getString("players." + raw + ".active_chat_color");
                if (tag       != null) activeTag.put(uuid, tag);
                if (color     != null) activeColor.put(uuid, color);
                if (chatColor != null) activeChatColor.put(uuid, chatColor);
            } catch (IllegalArgumentException ignored) {}
        }
        plugin.getLogger().info("Cosmetics loaded for " + unlocked.size() + " players.");
    }

    public void save() {
        Set<UUID> all = new HashSet<>();
        all.addAll(unlocked.keySet());
        all.addAll(activeTag.keySet());
        all.addAll(activeColor.keySet());
        all.addAll(activeChatColor.keySet());
        for (UUID uuid : all) {
            String base = "players." + uuid;
            cfg.set(base + ".unlocked",           new ArrayList<>(unlocked.getOrDefault(uuid, Set.of())));
            cfg.set(base + ".active_tag",         activeTag.get(uuid));
            cfg.set(base + ".active_color",       activeColor.get(uuid));
            cfg.set(base + ".active_chat_color",  activeChatColor.get(uuid));
        }
        try { cfg.save(file); }
        catch (IOException e) { plugin.getLogger().severe("Cannot save cosmetics.yml: " + e.getMessage()); }
    }

    // ── helpers ──────────────────────────────────────────────────────────

    private void setActive(Map<UUID, String> map, Player player, CrateReward r) {
        if (r == null) map.remove(player.getUniqueId());
        else map.put(player.getUniqueId(), r.name());
        save();
    }

    private Set<CrateReward> resolveSet(Player player, CrateReward.Type type) {
        Set<CrateReward> result = new LinkedHashSet<>();
        for (String name : unlocked.getOrDefault(player.getUniqueId(), Set.of())) {
            try {
                CrateReward r = CrateReward.valueOf(name);
                if (r.getType() == type) result.add(r);
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
