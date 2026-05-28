package de.louis.xdGens.manager;

import de.louis.xdGens.main.Main;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.entity.Player;

import java.io.File;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

/**
 * Verwaltet Money, Tokens und Gems pro Spieler.
 * Daten werden in /plugins/xdGens/data/currencies.yml gespeichert.
 */
public class CurrencyManager {

    private final Main plugin;
    private final File dataFile;
    private YamlConfiguration data;

    // Cache: UUID → Werte
    private final Map<UUID, Double> money  = new HashMap<>();
    private final Map<UUID, Integer> tokens = new HashMap<>();
    private final Map<UUID, Integer> gems   = new HashMap<>();

    public CurrencyManager(Main plugin) {
        this.plugin = plugin;
        this.dataFile = new File(plugin.getDataFolder(), "data/currencies.yml");
        load();
    }

    // ── Laden & Speichern ──────────────────────────────────────────────────────

    private void load() {
        if (!dataFile.exists()) {
            dataFile.getParentFile().mkdirs();
        }
        data = YamlConfiguration.loadConfiguration(dataFile);
    }

    public void saveAll() {
        // Schreibe Cache → YAML
        money.forEach((uuid, val)  -> data.set(uuid + ".money",  val));
        tokens.forEach((uuid, val) -> data.set(uuid + ".tokens", val));
        gems.forEach((uuid, val)   -> data.set(uuid + ".gems",   val));
        try { data.save(dataFile); } catch (IOException e) { e.printStackTrace(); }
    }

    // ── Getter ─────────────────────────────────────────────────────────────────

    public double getMoney(Player p) {
        return money.computeIfAbsent(p.getUniqueId(),
            uuid -> data.getDouble(uuid + ".money", 0.0));
    }

    public int getTokens(Player p) {
        return tokens.computeIfAbsent(p.getUniqueId(),
            uuid -> data.getInt(uuid + ".tokens", 0));
    }

    public int getGems(Player p) {
        return gems.computeIfAbsent(p.getUniqueId(),
            uuid -> data.getInt(uuid + ".gems", 0));
    }

    // ── Adder ──────────────────────────────────────────────────────────────────

    public void addMoney(Player p, double amount) {
        money.merge(p.getUniqueId(), amount, Double::sum);
    }

    public void addTokens(Player p, int amount) {
        tokens.merge(p.getUniqueId(), amount, Integer::sum);
    }

    public void addGems(Player p, int amount) {
        gems.merge(p.getUniqueId(), amount, Integer::sum);
    }
}
