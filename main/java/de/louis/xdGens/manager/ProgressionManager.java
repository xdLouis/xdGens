package de.louis.xdGens.manager;

import de.louis.xdGens.main.Main;
import de.louis.xdGens.util.MessageUtil;
import de.louis.xdGens.util.NumberUtil;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.entity.Player;

import java.io.File;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

public class ProgressionManager {

    private static final int BASE_PRESTIGE_LEVEL = 50;
    private static final int PRESTIGE_LEVEL_STEP = 10;

    private static final double BASE_PRESTIGE_COST = 250000.0;
    private static final double PRESTIGE_COST_STEP = 150000.0;

    /** +10% tokens per prestige level (e.g. Prestige 3 → ×1.30) */
    private static final double PRESTIGE_TOKEN_BONUS_PER_LEVEL = 0.10;

    private final Main plugin;
    private final Map<UUID, PlayerProgress> data = new HashMap<>();

    private File file;
    private FileConfiguration config;

    public ProgressionManager(Main plugin) {
        this.plugin = plugin;
        setup();
        loadAll();
    }

    private void setup() {
        if (!plugin.getDataFolder().exists()) {
            plugin.getDataFolder().mkdirs();
        }

        file = new File(plugin.getDataFolder(), "progression.yml");
        if (!file.exists()) {
            try {
                file.createNewFile();
            } catch (IOException e) {
                plugin.getLogger().severe("Could not create progression.yml: " + e.getMessage());
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

                int level = config.getInt("players." + key + ".level", 1);
                int prestige = config.getInt("players." + key + ".prestige", 0);
                double xp = config.getDouble("players." + key + ".xp", 0.0);

                PlayerProgress progress = new PlayerProgress();
                progress.level = Math.max(1, level);
                progress.prestige = Math.max(0, prestige);
                progress.xp = Math.max(0.0, xp);

                int maxLevelForPrestige = getRequiredLevelForPrestige(progress.prestige);
                if (progress.level > maxLevelForPrestige) {
                    progress.level = maxLevelForPrestige;
                }

                int requiredXp = getRequiredXp(progress.level, progress.prestige);
                if (progress.xp > requiredXp) {
                    progress.xp = requiredXp;
                }

                data.put(uuid, progress);
            } catch (IllegalArgumentException ignored) {
                plugin.getLogger().warning("Invalid UUID in progression.yml: " + key);
            }
        }

        plugin.getLogger().info("Progression data loaded: " + data.size());
    }

    public void saveAll() {
        config.set("players", null);

        for (Map.Entry<UUID, PlayerProgress> entry : data.entrySet()) {
            String path = "players." + entry.getKey();
            PlayerProgress progress = entry.getValue();

            config.set(path + ".level", progress.level);
            config.set(path + ".prestige", progress.prestige);
            config.set(path + ".xp", progress.xp);
        }

        try {
            config.save(file);
        } catch (IOException e) {
            plugin.getLogger().severe("Could not save progression.yml: " + e.getMessage());
        }
    }

    public void savePlayer(Player player) {
        UUID uuid = player.getUniqueId();
        PlayerProgress progress = getOrCreate(uuid);

        String path = "players." + uuid;
        config.set(path + ".level", progress.level);
        config.set(path + ".prestige", progress.prestige);
        config.set(path + ".xp", progress.xp);

        try {
            config.save(file);
        } catch (IOException e) {
            plugin.getLogger().severe("Could not save player progression: " + e.getMessage());
        }
    }

    public void addXp(Player player, double amount) {
        if (amount <= 0) {
            return;
        }

        PlayerProgress progress = getOrCreate(player.getUniqueId());

        if (isMaxLevel(player)) {
            progress.xp = getRequiredXp(player);
            updateDisplays(player);
            return;
        }

        progress.xp += amount;

        while (!isMaxLevel(player) && progress.xp >= getRequiredXp(player)) {
            progress.xp -= getRequiredXp(player);
            progress.level++;

            MessageUtil.sendRaw(player,
                    MessageUtil.PREFIX + " <gray>You reached <gradient:#7afcff:#00c2ff>Level "
                            + progress.level + "</gradient><gray>!</gray>");
        }

        if (isMaxLevel(player)) {
            progress.xp = getRequiredXp(player);
            MessageUtil.sendRaw(player,
                    MessageUtil.PREFIX
                            + " <gradient:#f6d365:#fda085>You reached the required level for prestige!</gradient>"
                            + " <gray>Use</gray> <yellow>/prestige</yellow><gray> when you have </gray><green>$"
                            + NumberUtil.format(getPrestigeCost(player)) + "</green><gray>.</gray>");
        }

        updateDisplays(player);
    }

    public boolean canPrestige(Player player) {
        return getLevel(player) >= getRequiredLevelForPrestige(player)
                && plugin.getCurrencyManager().getMoney(player) >= getPrestigeCost(player);
    }

    public boolean isMaxLevel(Player player) {
        return getLevel(player) >= getMaxLevelForPlayer(player);
    }

    public int getRequiredLevelForPrestige(Player player) {
        return getRequiredLevelForPrestige(getPrestige(player));
    }

    private int getRequiredLevelForPrestige(int prestige) {
        return BASE_PRESTIGE_LEVEL + (prestige * PRESTIGE_LEVEL_STEP);
    }

    public int getMaxLevelForPlayer(Player player) {
        return getRequiredLevelForPrestige(player);
    }

    public double getPrestigeCost(Player player) {
        return BASE_PRESTIGE_COST + (getPrestige(player) * PRESTIGE_COST_STEP);
    }

    /**
     * Returns the token multiplier bonus from prestige.
     * Prestige 0 → 1.0x (no bonus), Prestige 1 → 1.10x, Prestige 3 → 1.30x, etc.
     */
    public double getPrestigeTokenMultiplier(Player player) {
        return 1.0 + (getPrestige(player) * PRESTIGE_TOKEN_BONUS_PER_LEVEL);
    }

    /** Returns the prestige token bonus as a readable percent, e.g. Prestige 3 → 30.0 */
    public double getPrestigeTokenBonusPercent(Player player) {
        return getPrestige(player) * (PRESTIGE_TOKEN_BONUS_PER_LEVEL * 100.0);
    }

    public boolean prestige(Player player) {
        int requiredLevel = getRequiredLevelForPrestige(player);
        double cost = getPrestigeCost(player);

        if (getLevel(player) < requiredLevel) {
            return false;
        }

        if (!plugin.getCurrencyManager().removeMoney(player, cost)) {
            return false;
        }

        PlayerProgress progress = getOrCreate(player.getUniqueId());
        progress.prestige++;
        progress.level = 1;
        progress.xp = 0.0;

        savePlayer(player);

        double bonusPct = getPrestigeTokenBonusPercent(player);
        MessageUtil.sendRaw(player,
                MessageUtil.PREFIX + " <gray>You advanced to <gradient:#f6d365:#fda085>Prestige "
                        + progress.prestige + "</gradient><gray>!</gray> <gray>(-</gray><green>$"
                        + NumberUtil.format(cost) + "</green><gray>)</gray>"
                        + " <gray>Token Bonus: </gray><yellow>+" + (int) bonusPct + "%</yellow>"
                        + " <gray>Next prestige at Level </gray><yellow>"
                        + getRequiredLevelForPrestige(player) + "</yellow><gray>.</gray>");

        updateDisplays(player);
        return true;
    }

    public int getLevel(Player player) {
        return getOrCreate(player.getUniqueId()).level;
    }

    public int getPrestige(Player player) {
        return getOrCreate(player.getUniqueId()).prestige;
    }

    public double getXp(Player player) {
        return getOrCreate(player.getUniqueId()).xp;
    }

    public int getRequiredXp(Player player) {
        return getRequiredXp(getLevel(player), getPrestige(player));
    }

    private int getRequiredXp(int level, int prestige) {
        int maxLevel = getRequiredLevelForPrestige(prestige);

        if (level >= maxLevel) {
            return 1000 + (prestige * 250);
        }

        return 100 + ((level - 1) * 25) + (prestige * 40);
    }

    public int getMaxLevel() {
        return BASE_PRESTIGE_LEVEL;
    }

    public void updateDisplays(Player player) {
        updateXpBar(player);

        if (plugin.getScoreboardManager() != null) {
            plugin.getScoreboardManager().update(player);
        }
    }

    public void updateXpBar(Player player) {
        int level = getLevel(player);
        double xp = getXp(player);
        int required = getRequiredXp(player);

        player.setLevel(level);

        if (isMaxLevel(player)) {
            player.setExp(1.0f);
            return;
        }

        float progress = required <= 0 ? 0.0f : (float) Math.min(1.0, xp / required);
        player.setExp(progress);
    }

    private PlayerProgress getOrCreate(UUID uuid) {
        return data.computeIfAbsent(uuid, ignored -> new PlayerProgress());
    }

    private static class PlayerProgress {
        int level = 1;
        int prestige = 0;
        double xp = 0.0;
    }
}
