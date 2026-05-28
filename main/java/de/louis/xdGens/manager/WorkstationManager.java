package de.louis.xdGens.workstation;

import de.louis.xdGens.hologram.HologramManager;
import de.louis.xdGens.main.Main;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;

import java.io.File;
import java.io.IOException;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

public class WorkstationManager {

    private final Main plugin;
    private final HologramManager hologramManager;
    private final Set<String> workstations = new HashSet<>();

    private File dataFile;
    private FileConfiguration dataConfig;

    public WorkstationManager(Main plugin, HologramManager hologramManager) {
        this.plugin = plugin;
        this.hologramManager = hologramManager;
        setupFile();
        loadAll();
    }

    private void setupFile() {
        dataFile = new File(plugin.getDataFolder(), "workstations.yml");
        if (!dataFile.exists()) {
            try {
                dataFile.createNewFile();
            } catch (IOException e) {
                plugin.getLogger().severe("Konnte workstations.yml nicht erstellen: " + e.getMessage());
            }
        }
        dataConfig = YamlConfiguration.loadConfiguration(dataFile);
    }

    /**
     * Wird beim Start aufgerufen — stellt alle Holograms neu her.
     */
    private void loadAll() {
        hologramManager.cleanupStaleHolograms();

        List<?> list = dataConfig.getList("workstations");
        if (list == null) return;

        for (Object obj : list) {
            if (!(obj instanceof String entry)) continue;

            Location loc = deserialize(entry);
            if (loc == null) {
                plugin.getLogger().warning("Konnte Workstation nicht laden: " + entry);
                continue;
            }

            workstations.add(entry);

            // Hologram respawnen (1 Tick verzögert, damit die Welt geladen ist)
            String finalEntry = entry;
            Bukkit.getScheduler().runTaskLater(plugin, () -> {
                Location loaded = deserialize(finalEntry);
                if (loaded != null) {
                    hologramManager.spawn(loaded);
                }
            }, 5L);
        }

        plugin.getLogger().info("Workstations geladen: " + workstations.size());
    }

    public void register(Location location) {
        String key = serialize(location);
        workstations.add(key);
        hologramManager.spawn(location);
        save();
    }

    public void unregister(Location location) {
        String key = serialize(location);
        workstations.remove(key);
        hologramManager.removeAt(location);
        save();
    }

    public boolean isWorkstation(Location location) {
        return workstations.contains(serialize(location));
    }

    public void removeAll() {
        hologramManager.removeAll();
    }

    private void save() {
        dataConfig.set("workstations", List.copyOf(workstations));
        try {
            dataConfig.save(dataFile);
        } catch (IOException e) {
            plugin.getLogger().severe("Konnte workstations.yml nicht speichern: " + e.getMessage());
        }
    }

    private String serialize(Location loc) {
        return loc.getWorld().getName()
                + ":" + loc.getBlockX()
                + ":" + loc.getBlockY()
                + ":" + loc.getBlockZ();
    }

    private Location deserialize(String key) {
        String[] parts = key.split(":");
        if (parts.length != 4) return null;

        World world = Bukkit.getWorld(parts[0]);
        if (world == null) return null;

        try {
            int x = Integer.parseInt(parts[1]);
            int y = Integer.parseInt(parts[2]);
            int z = Integer.parseInt(parts[3]);
            return new Location(world, x, y, z);
        } catch (NumberFormatException e) {
            return null;
        }
    }
}