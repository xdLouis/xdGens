package de.louis.xdGens.manager;

import de.louis.xdGens.crate.CrateReward;
import de.louis.xdGens.main.Main;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.entity.Player;

import java.io.File;
import java.io.IOException;
import java.util.*;

public class PlayerCosmeticManager {

    private final Main plugin;
    private File file;
    private FileConfiguration cfg;

    private final Map<UUID, Set<String>>          unlocked        = new HashMap<>();
    private final Map<UUID, Map<String, Long>>     unlockedDates   = new HashMap<>();
    private final Map<UUID, Map<String, Integer>>  vouchers        = new HashMap<>();   // stored duplicate vouchers
    private final Map<UUID, String>                activeTag       = new HashMap<>();
    private final Map<UUID, String>                activeColor     = new HashMap<>();
    private final Map<UUID, String>                activeChatColor = new HashMap<>();
    private final Map<UUID, String>                activeGlow      = new HashMap<>();

    public PlayerCosmeticManager(Main plugin) {
        this.plugin = plugin;
        setup();
        load();
    }

    // ── public API ───────────────────────────────────────────────────

    public boolean hasCosmetic(Player player, CrateReward reward) {
        return unlocked.getOrDefault(player.getUniqueId(), Set.of()).contains(reward.name());
    }

    /**
     * Unlocks the cosmetic.
     * @return true if newly added, false if already owned (duplicate → caller should addVoucher).
     */
    public boolean unlock(Player player, CrateReward reward) {
        UUID uuid = player.getUniqueId();
        Set<String> set = unlocked.computeIfAbsent(uuid, k -> new LinkedHashSet<>());
        boolean added = set.add(reward.name());
        if (added) {
            unlockedDates
                .computeIfAbsent(uuid, k -> new LinkedHashMap<>())
                .put(reward.name(), System.currentTimeMillis());
        }
        save();
        return added;
    }

    /** Removes the cosmetic from the collection and clears active slot if equipped. */
    public boolean revoke(Player player, CrateReward reward) {
        UUID uuid = player.getUniqueId();
        Set<String> set = unlocked.get(uuid);
        if (set == null || !set.remove(reward.name())) return false;
        unlockedDates.getOrDefault(uuid, Map.of()).remove(reward.name());
        clearIfActive(activeTag,       uuid, reward);
        clearIfActive(activeColor,     uuid, reward);
        clearIfActive(activeChatColor, uuid, reward);
        clearIfActive(activeGlow,      uuid, reward);
        save();
        return true;
    }

    private void clearIfActive(Map<UUID, String> map, UUID uuid, CrateReward reward) {
        if (reward.name().equals(map.get(uuid))) map.remove(uuid);
    }

    // ── voucher bank ──────────────────────────────────────────────────

    /** Returns how many duplicate vouchers the player has stored for this cosmetic. */
    public int getVoucherCount(Player player, CrateReward reward) {
        return vouchers
            .getOrDefault(player.getUniqueId(), Map.of())
            .getOrDefault(reward.name(), 0);
    }

    /** Adds +1 stored voucher for this cosmetic. */
    public void addVoucher(Player player, CrateReward reward) {
        UUID uuid = player.getUniqueId();
        vouchers
            .computeIfAbsent(uuid, k -> new LinkedHashMap<>())
            .merge(reward.name(), 1, Integer::sum);
        save();
    }

    /**
     * Removes 1 stored voucher for this cosmetic.
     * @return true if one was consumed, false if none left.
     */
    public boolean consumeVoucher(Player player, CrateReward reward) {
        UUID uuid = player.getUniqueId();
        Map<String, Integer> map = vouchers.get(uuid);
        if (map == null) return false;
        int current = map.getOrDefault(reward.name(), 0);
        if (current <= 0) return false;
        if (current == 1) map.remove(reward.name()); else map.put(reward.name(), current - 1);
        save();
        return true;
    }

    // ── timestamp ────────────────────────────────────────────────────

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

    // ── active getters / setters ────────────────────────────────────────

    public Optional<CrateReward> getActiveTag(Player player)       { return resolveReward(activeTag.get(player.getUniqueId())); }
    public Optional<CrateReward> getActiveColor(Player player)     { return resolveReward(activeColor.get(player.getUniqueId())); }
    public Optional<CrateReward> getActiveChatColor(Player player) { return resolveReward(activeChatColor.get(player.getUniqueId())); }
    public Optional<CrateReward> getActiveGlow(Player player)      { return resolveReward(activeGlow.get(player.getUniqueId())); }

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
                String base = "players." + raw;

                // unlocked
                unlocked.put(uuid, new LinkedHashSet<>(cfg.getStringList(base + ".unlocked")));

                // timestamps
                String datesPath = base + ".unlocked_dates";
                if (cfg.isConfigurationSection(datesPath)) {
                    Map<String, Long> dates = new LinkedHashMap<>();
                    for (String key : cfg.getConfigurationSection(datesPath).getKeys(false))
                        dates.put(key, cfg.getLong(datesPath + "." + key));
                    unlockedDates.put(uuid, dates);
                }

                // stored vouchers
                String vPath = base + ".vouchers";
                if (cfg.isConfigurationSection(vPath)) {
                    Map<String, Integer> vMap = new LinkedHashMap<>();
                    for (String key : cfg.getConfigurationSection(vPath).getKeys(false))
                        vMap.put(key, cfg.getInt(vPath + "." + key));
                    vouchers.put(uuid, vMap);
                }

                String tag       = cfg.getString(base + ".active_tag");
                String color     = cfg.getString(base + ".active_color");
                String chatColor = cfg.getString(base + ".active_chat_color");
                String glow      = cfg.getString(base + ".active_glow");
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
        all.addAll(unlocked.keySet()); all.addAll(vouchers.keySet());
        all.addAll(activeTag.keySet()); all.addAll(activeColor.keySet());
        all.addAll(activeChatColor.keySet()); all.addAll(activeGlow.keySet());
        for (UUID uuid : all) {
            String base = "players." + uuid;
            cfg.set(base + ".unlocked", new ArrayList<>(unlocked.getOrDefault(uuid, Set.of())));

            Map<String, Long> dates = unlockedDates.get(uuid);
            if (dates != null)
                for (Map.Entry<String, Long> e : dates.entrySet())
                    cfg.set(base + ".unlocked_dates." + e.getKey(), e.getValue());

            // stored vouchers
            Map<String, Integer> vMap = vouchers.get(uuid);
            if (vMap != null)
                for (Map.Entry<String, Integer> e : vMap.entrySet())
                    cfg.set(base + ".vouchers." + e.getKey(), e.getValue());

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
