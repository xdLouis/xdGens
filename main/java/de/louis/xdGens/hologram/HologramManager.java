package de.louis.xdGens.hologram;

import de.louis.xdGens.main.Main;
import de.louis.xdGens.util.MessageUtil;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.entity.ArmorStand;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

public class HologramManager {

    private final Main plugin;
    private final Map<String, ArmorStand> holograms = new HashMap<>();

    public HologramManager(Main plugin) {
        this.plugin = plugin;
    }

    public void spawn(Location baseLocation) {
        if (baseLocation == null || baseLocation.getWorld() == null) {
            return;
        }

        Location blockLocation = normalize(baseLocation);
        String key = key(blockLocation);

        removeAt(blockLocation);

        if (!blockLocation.isWorldLoaded()) {
            return;
        }

        if (!blockLocation.getChunk().isLoaded()) {
            blockLocation.getChunk().load();
        }

        Location holoLocation = blockLocation.clone().add(0.5, 1.35, 0.5);

        ArmorStand stand = blockLocation.getWorld().spawn(holoLocation, ArmorStand.class, armorStand -> {
            armorStand.setVisible(false);
            armorStand.setGravity(false);
            armorStand.setMarker(true);
            armorStand.setSmall(true);
            armorStand.setCustomNameVisible(true);
            armorStand.customName(MessageUtil.parse("<gradient:#a18cd1:#fbc2eb><bold>ᴡᴏʀᴋѕᴛᴀᴛɪᴏɴ</bold></gradient>"));
            armorStand.setPersistent(false);
            armorStand.setInvulnerable(true);
            armorStand.setCollidable(false);
            armorStand.setCanPickupItems(false);
            armorStand.setSilent(true);
        });

        holograms.put(key, stand);
    }

    public void removeAt(Location baseLocation) {
        if (baseLocation == null || baseLocation.getWorld() == null) {
            return;
        }

        String key = key(normalize(baseLocation));
        ArmorStand stand = holograms.remove(key);

        if (stand != null && !stand.isDead()) {
            stand.remove();
        }

        cleanupNearby(normalize(baseLocation));
    }

    public void cleanupStaleHolograms() {
        Set<String> deadKeys = new HashSet<>();

        for (Map.Entry<String, ArmorStand> entry : holograms.entrySet()) {
            ArmorStand stand = entry.getValue();
            if (stand == null || stand.isDead() || !stand.isValid()) {
                deadKeys.add(entry.getKey());
                continue;
            }

            if (stand.customName() == null) {
                stand.remove();
                deadKeys.add(entry.getKey());
            }
        }

        deadKeys.forEach(holograms::remove);

        for (World world : Bukkit.getWorlds()) {
            for (ArmorStand stand : world.getEntitiesByClass(ArmorStand.class)) {
                if (!stand.isMarker()) {
                    continue;
                }

                if (!stand.isCustomNameVisible() || stand.customName() == null) {
                    continue;
                }

                String plain = MessageUtil.strip(String.valueOf(stand.customName()));
                if (!plain.equalsIgnoreCase("ᴡᴏʀᴋѕᴛᴀᴛɪᴏɴ") && !plain.equalsIgnoreCase("workstation")) {
                    continue;
                }

                boolean tracked = holograms.values().stream().anyMatch(existing -> existing.getUniqueId().equals(stand.getUniqueId()));
                if (!tracked) {
                    stand.remove();
                }
            }
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

    private void cleanupNearby(Location baseLocation) {
        World world = baseLocation.getWorld();
        if (world == null) {
            return;
        }

        Location center = baseLocation.clone().add(0.5, 1.35, 0.5);

        for (ArmorStand stand : world.getEntitiesByClass(ArmorStand.class)) {
            if (stand.getLocation().distanceSquared(center) > 0.25) {
                continue;
            }

            if (!stand.isMarker()) {
                continue;
            }

            if (!stand.isCustomNameVisible() || stand.customName() == null) {
                continue;
            }

            String plain = MessageUtil.strip(String.valueOf(stand.customName()));
            if (plain.equalsIgnoreCase("ᴡᴏʀᴋѕᴛᴀᴛɪᴏɴ") || plain.equalsIgnoreCase("workstation")) {
                stand.remove();
            }
        }
    }

    private Location normalize(Location location) {
        return new Location(
                location.getWorld(),
                location.getBlockX(),
                location.getBlockY(),
                location.getBlockZ()
        );
    }

    private String key(Location location) {
        return location.getWorld().getName()
                + ":" + location.getBlockX()
                + ":" + location.getBlockY()
                + ":" + location.getBlockZ();
    }
}