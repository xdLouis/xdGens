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
 *       unlocked:          [TAG_FARMER, COLOR_AQUA, ...]
 *       unlocked_dates:    {TAG_FARMER: 1234567890, COLOR_AQUA: 1234567891, ...}
 *       active_tag:        TAG_FARMER
 *       active_color:      COLOR_AQUA
 *       active_chat_color: CHAT_GREEN
 *       active_glow:       GLOW_RED
 */
public class PlayerCosmeticManager {

    private final Main plugin;
    private File file;
    private FileConfiguration cfg;

    private final Map<UUID, Set<String>>  unlocked        = new HashMap<>();
    private final Map<UUID, Map<String, Long>> unlockedDates = new HashMap<>();
    private final Map<UUID, String>       activeTag       = new HashMap<>();
    private final Map<UUID, String>       activeColor     = new HashMap<>();
    private final Map<UUID, String>       activeChatColor = new HashMap<>();
    private final Map<UUID, String>       activeGlow      = new HashMap<>();

    public PlayerCosmeticManager(Main plugin) {
        this.plugin = plugin;
        setup();
        load();
    }

    // ── public API ───────────────────────────────────────────────────

    public boolean hasCosmetic(Player player, CrateReward reward) {
        return unlocked.getOrDefault(player.getUniqueId(), Set.of()).contains(reward.name());
    }

    public boolean unlock(Player player, CrateReward reward) {
        UUID uuid = player.getUniqueId();
        Set<String> set = unlocked.computeIfAbsent(uuid, k -> new LinkedHashSet<>());
        boolean added = set.add(reward.name());
        if (added) {
            // record timestamp
            unlockedDates
                .computeIfAbsent(uuid, k -> new LinkedHashMap<>())
                .put(reward.name(), System.currentTimeMillis());
        }
        save();
        return added;
    }

    /** Returns the epoch-millis when the player unlocked the reward, or 0 if not unlocked. */
    public long getUnlockTimestamp(Player player, CrateReward reward) {
        Map<String, Long> dates = unlockedDates.get(player.getUniqueId());
        if (dates == null) return 0L;
        return dates.getOrDefault(reward.name(), 0L);
    }

    // ── unlocked getters ─────────────────────────────────────────────

    public Set<CrateReward> getUnlockedTags(Player player)       { return resolveSet(player, CrateReward.Type.TAG); }
    public Set<CrateReward> getUnlockedColors(Player player)     { return resolveSet(player, CrateReward.Type.NAME_COLOR); }
    public Set<CrateReward> getUnlockedChatColors(Player player) { return resolveSet(player, CrateReward.Type.CHAT_COLOR); }
    public Set<CrateReward> getUnlockedGlows(Player player)      { return resolveSet(player, CrateReward.Type.GLOW); }

    // ── active getters ────────────────────────────────────────────────

    public Optional<CrateReward> getActiveTag(Player player)       { return resolveReward(activeTag.get(player.getUniqueId())); }
    public Optional<CrateReward> getActiveColor(Player player)     { return resolveReward(activeColor.get(player.getUniqueId())); }
    public Optional<CrateReward> getActiveChatColor(Player player) { return resolveReward(activeChatColor.get(player.getUniqueId())); }
    public Optional<CrateReward> getActiveGlow(Player player)      { return resolveReward(activeGlow.get(player.getUniqueId())); }

    // ── active setters ────────────────────────────────────────────────

    public void setActiveTag(Player player, CrateReward r)       { setActive(activeTag,       player, r); }
    public void setActiveColor(Player player, CrateReward r)     { setActive(activeColor,     player, r); }
    public void setActiveChatColor(Player player, CrateReward r) { setActive(activeChatColor, player, r); }
    public void setActiveGlow(Player player, CrateReward r)      { setActive(activeGlow,      player, r); }

    // ── format builders ───────────────────────────────────────────────

    public String buildTagFormat(Player player) {
        return getActiveTag(player).map(CrateReward::getCosmeticFormat).orElse("");
    }

    public String buildNameFormat(Player player) {
        return getActiveColor(player)
                .map(r -> r.getCosmeticFormat().replace("{name}", player.getName()))
                .orElse(player.getName());
    }

    public String buildChatFormat(Player player, String escapedMsg) {
        return getActiveChatColor(player)
                .map(r -> r.getCosmeticFormat().replace("{msg}", escapedMsg))
                .orElse(escapedMsg);
    }

    public String getGlowColor(Player player) {
        return getActiveGlow(player).map(CrateReward::getGlowColor).orElse(null);
    }

    // ── persistence ───────────────────────────────────────────────────

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

                // load unlock timestamps
                String datesPath = "players." + raw + ".unlocked_dates";
                if (cfg.isConfigurationSection(datesPath)) {
                    Map<String, Long> dates = new LinkedHashMap<>();
                    for (String key : cfg.getConfigurationSection(datesPath).getKeys(false)) {
                        dates.put(key, cfg.getLong(datesPath + "." + key));
                    }
                    unlockedDates.put(uuid, dates);
                }

                String tag       = cfg.getString("players." + raw + ".active_tag");
                String color     = cfg.getString("players." + raw + ".active_color");
                String chatColor = cfg.getString("players." + raw + ".active_chat_color");
                String glow      = cfg.getString("players." + raw + ".active_glow");
                if (tag       != null) activeTag.put(uuid, tag);
                if (color     != null) activeColor.put(uuid, color);
                if (chatColor != null) activeChatColor.put(uuid, chatColor);
                if (glow      != null) activeGlow.put(uuid, glow);
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
        all.addAll(activeGlow.keySet());
        for (UUID uuid : all) {
            String base = "players." + uuid;
            cfg.set(base + ".unlocked", new ArrayList<>(unlocked.getOrDefault(uuid, Set.of())));

            // save unlock timestamps
            Map<String, Long> dates = unlockedDates.get(uuid);
            if (dates != null) {
                for (Map.Entry<String, Long> e : dates.entrySet()) {
                    cfg.set(base + ".unlocked_dates." + e.getKey(), e.getValue());
                }
            }

            cfg.set(base + ".active_tag",         activeTag.get(uuid));
            cfg.set(base + ".active_color",       activeColor.get(uuid));
            cfg.set(base + ".active_chat_color",  activeChatColor.get(uuid));
            cfg.set(base + ".active_glow",        activeGlow.get(uuid));
        }
        try { cfg.save(file); }
        catch (IOException e) { plugin.getLogger().severe("Cannot save cosmetics.yml: " + e.getMessage()); }
    }

    // ── helpers ───────────────────────────────────────────────────────

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
