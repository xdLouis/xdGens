package de.louis.xdGens.field;

import de.louis.xdGens.main.Main;
import org.bukkit.Bukkit;
import org.bukkit.Chunk;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.World;
import org.bukkit.block.Block;
import org.bukkit.block.data.Ageable;

/**
 * Verwaltet das Weizenfeld.
 * Das Feld wird beim ersten Laden automatisch gebaut falls noch nicht vorhanden.
 * Koordinaten, Welt und Größe kommen aus der config.yml.
 */
public class FieldManager {

    private final Main plugin;

    // Feld-Grenzen (aus config)
    private final String worldName;
    private final int centerX, centerZ;
    private final int radius;          // Feld = (2*radius+1) x (2*radius+1)
    private final int fieldY;          // Y-Koordinate des Farmland-Blocks

    public FieldManager(Main plugin) {
        this.plugin = plugin;

        worldName = plugin.getConfig().getString("field.world",  "world");
        centerX   = plugin.getConfig().getInt(   "field.center-x", 0);
        centerZ   = plugin.getConfig().getInt(   "field.center-z", 0);
        radius    = plugin.getConfig().getInt(   "field.radius",   25);
        fieldY    = plugin.getConfig().getInt(   "field.y",        64);

        // Baue das Feld verzögert, damit die Welt geladen ist
        Bukkit.getScheduler().runTaskLater(plugin, this::buildField, 20L);
    }

    // ── Feld bauen ─────────────────────────────────────────────────────────────

    public void buildField() {
        World world = Bukkit.getWorld(worldName);
        if (world == null) {
            plugin.getLogger().warning("Welt '" + worldName + "' nicht gefunden! Feld kann nicht gebaut werden.");
            return;
        }

        int built = 0;
        for (int x = centerX - radius; x <= centerX + radius; x++) {
            for (int z = centerZ - radius; z <= centerZ + radius; z++) {

                Block farmland = world.getBlockAt(x, fieldY, z);
                Block crop     = world.getBlockAt(x, fieldY + 1, z);

                // Farmland setzen (nur wenn nicht bereits vorhanden)
                if (farmland.getType() != Material.FARMLAND) {
                    farmland.setType(Material.FARMLAND);
                }

                // Farmland immer bewässert halten (moisture = 7)
                moisturizeFarmland(farmland);

                // Weizen setzen falls leer oder kein Weizen
                if (crop.getType() != Material.WHEAT) {
                    crop.setType(Material.WHEAT);
                    setWheatFullyGrown(crop);
                }

                built++;
            }
        }
        plugin.getLogger().info("Weizenfeld gebaut: " + built + " Blöcke bei Y=" + fieldY + " (Radius " + radius + ").");
    }

    // ── Hilfsmethoden ──────────────────────────────────────────────────────────

    /**
     * Setzt den Moisture-Wert des Farmland-Blocks auf 7 (= vollständig bewässert).
     */
    public static void moisturizeFarmland(Block farmland) {
        if (farmland.getType() != Material.FARMLAND) return;
        org.bukkit.block.data.type.Farmland data =
            (org.bukkit.block.data.type.Farmland) farmland.getBlockData();
        data.setMoisture(data.getMaximumMoisture());
        farmland.setBlockData(data, false);
    }

    /**
     * Setzt Weizen auf maximales Wachstum (age = 7).
     */
    public static void setWheatFullyGrown(Block wheat) {
        if (!(wheat.getBlockData() instanceof Ageable ageable)) return;
        ageable.setAge(ageable.getMaximumAge());
        wheat.setBlockData(ageable, false);
    }

    /**
     * Gibt true zurück wenn der Block innerhalb des Feldes liegt.
     */
    public boolean isInField(Location loc) {
        if (!loc.getWorld().getName().equals(worldName)) return false;
        int x = loc.getBlockX();
        int z = loc.getBlockZ();
        return x >= centerX - radius && x <= centerX + radius
            && z >= centerZ - radius && z <= centerZ + radius;
    }

    public int getFieldY() { return fieldY; }
}
