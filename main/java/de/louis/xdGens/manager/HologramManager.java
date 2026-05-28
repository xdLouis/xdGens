package de.louis.xdGens.hologram;

import de.louis.xdGens.main.Main;
import de.louis.xdGens.util.MessageUtil;
import org.bukkit.Location;
import org.bukkit.NamespacedKey;
import org.bukkit.World;
import org.bukkit.entity.ArmorStand;
import org.bukkit.entity.EntityType;
import org.bukkit.persistence.PersistentDataType;

import java.util.HashMap;
import java.util.Map;

public class HologramManager {

    public static final String HOLOGRAM_TAG = "xdgens_hologram";

    private final Main plugin;
    private final NamespacedKey holoKey;
    private final Map<String, ArmorStand> holograms = new HashMap<>();

    public HologramManager(Main plugin) {
        this.plugin = plugin;
        this.holoKey = new NamespacedKey(plugin, HOLOGRAM_TAG);
    }

    public ArmorStand spawn(Location location) {
        removeAt(location);

        Location holoLoc = location.clone().add(0.5, 1.4, 0.5);

        ArmorStand stand = (ArmorStand) holoLoc.getWorld().spawnEntity(holoLoc, EntityType.ARMOR_STAND);
        stand.setInvisible(true);
        stand.setInvulnerable(true);
        stand.setGravity(false);
        stand.setMarker(true);
        stand.setSmall(true);
        stand.setCustomNameVisible(true);
        stand.customName(MessageUtil.parse(
                "<gradient:#a18cd1:#fbc2eb><bold>⚙ Wheat Workstation</bold></gradient>"

        ));

        // PDC-Tag damit wir sie nach Crash aufräumen können
        stand.getPersistentDataContainer().set(holoKey, PersistentDataType.BYTE, (byte) 1);

        holograms.put(toKey(location), stand);
        return stand;
    }

    public void removeAt(Location location) {
        String key = toKey(location);
        ArmorStand existing = holograms.remove(key);
        if (existing != null && !existing.isDead()) {
            existing.remove();
        }
    }

    public void removeAll() {
        for (ArmorStand stand : holograms.values()) {
            if (stand != null && !stand.isDead()) {
                stand.remove();
            }
        }
        holograms.clear();
    }

    /**
     * Räumt alle alten xdGens-Hologram-Entities in allen Welten auf.
     * Wichtig nach Crashes, damit keine verwaisten ArmorStands übrig bleiben.
     */
    public void cleanupStaleHolograms() {
        for (World world : plugin.getServer().getWorlds()) {
            world.getEntitiesByClass(ArmorStand.class).forEach(stand -> {
                if (stand.getPersistentDataContainer().has(holoKey, PersistentDataType.BYTE)) {
                    stand.remove();
                }
            });
        }
    }

    public boolean hasHologram(Location location) {
        return holograms.containsKey(toKey(location));
    }

    private String toKey(Location loc) {
        return loc.getWorld().getName()
                + ":" + loc.getBlockX()
                + ":" + loc.getBlockY()
                + ":" + loc.getBlockZ();
    }
}