package de.louis.xdGens.manager;

import de.louis.xdGens.main.Main;
import de.louis.xdGens.util.CustomItemUtil;
import de.louis.xdGens.util.MessageUtil;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.entity.Player;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

public class WorkstationManager {
    private final Main plugin;
    private final de.louis.xdGens.hologram.HologramManager hologramManager;
    private final Set<String> workstations = new HashSet<>();
    private File dataFile;
    private FileConfiguration dataConfig;

    public WorkstationManager(Main plugin, de.louis.xdGens.hologram.HologramManager hologramManager) {
        this.plugin = plugin;
        this.hologramManager = hologramManager;
        setupFile();
        loadAll();
    }

    private void setupFile() {
        dataFile = new File(plugin.getDataFolder(), "workstations.yml");
        try {
            if (!dataFile.exists()) dataFile.createNewFile();
        } catch (IOException e) {
            plugin.getLogger().severe("Could not create workstations.yml: " + e.getMessage());
        }
        dataConfig = YamlConfiguration.loadConfiguration(dataFile);
    }

    private void loadAll() {
        hologramManager.cleanupStaleHolograms();
        List<?> list = dataConfig.getList("workstations");
        if (list == null) return;

        for (Object obj : list) {
            if (!(obj instanceof String entry)) continue;
            Location loc = deserialize(entry);
            if (loc == null) continue;
            workstations.add(entry);
            Bukkit.getScheduler().runTaskLater(plugin, () -> {
                Location loaded = deserialize(entry);
                if (loaded != null) hologramManager.spawn(loaded);
            }, 40L);
        }
    }

    public void register(Location location) {
        workstations.add(serialize(location));
        hologramManager.spawn(location);
        save();
    }

    public void unregister(Location location) {
        workstations.remove(serialize(location));
        hologramManager.removeAt(location);
        save();
    }

    public boolean isWorkstation(Location location) {
        return workstations.contains(serialize(location));
    }

    public List<Location> getWorkstationLocations() {
        List<Location> locations = new ArrayList<>();
        for (String entry : workstations) {
            Location loc = deserialize(entry);
            if (loc != null) locations.add(loc);
        }
        return locations;
    }

    public void useWorkstation(Player player) {
        Inventory inv = player.getInventory();

        int wheat = count(inv, "farm_wheat");
        int wheatBlocksCreated = wheat / 64;
        if (wheatBlocksCreated > 0) {
            remove(inv, "farm_wheat", wheatBlocksCreated * 64);
            giveOrDrop(player, CustomItemUtil.createCompressedWheatBlock(plugin, wheatBlocksCreated));
        }

        int wheatBlocks = count(inv, "compressed_wheat_block");
        int enchantedCreated = wheatBlocks / 64;
        if (enchantedCreated > 0) {
            remove(inv, "compressed_wheat_block", enchantedCreated * 64);
            giveOrDrop(player, CustomItemUtil.createCompressedWheatBlock(plugin, enchantedCreated));
        }

        if (wheatBlocksCreated == 0 && enchantedCreated == 0) {
            MessageUtil.send(player, MessageUtil.PREFIX + " <red>Not enough materials.</red>");
            return;
        }

        MessageUtil.sendRaw(player, MessageUtil.PREFIX + " <gradient:#7afcff:#00c2ff>Auto craft complete</gradient>");
    }

    public void removeAll() {
        hologramManager.removeAll();
    }

    private void save() {
        dataConfig.set("workstations", List.copyOf(workstations));
        try {
            dataConfig.save(dataFile);
        } catch (IOException e) {
            plugin.getLogger().severe("Could not save workstations.yml: " + e.getMessage());
        }
    }

    private String serialize(Location loc) {
        return loc.getWorld().getName() + ":" + loc.getBlockX() + ":" + loc.getBlockY() + ":" + loc.getBlockZ();
    }

    private Location deserialize(String key) {
        String[] p = key.split(":");
        if (p.length != 4) return null;
        World world = Bukkit.getWorld(p[0]);
        if (world == null) return null;

        try {
            return new Location(world, Integer.parseInt(p[1]), Integer.parseInt(p[2]), Integer.parseInt(p[3]));
        } catch (NumberFormatException e) {
            return null;
        }
    }

    private int count(Inventory inv, String type) {
        int total = 0;
        for (ItemStack item : inv.getContents()) {
            if (CustomItemUtil.hasItemType(plugin, item, type)) total += item.getAmount();
        }
        return total;
    }

    private void remove(Inventory inv, String type, int amount) {
        int left = amount;
        for (int i = 0; i < inv.getSize(); i++) {
            ItemStack item = inv.getItem(i);
            if (!CustomItemUtil.hasItemType(plugin, item, type)) continue;

            int take = Math.min(left, item.getAmount());
            item.setAmount(item.getAmount() - take);
            left -= take;

            if (item.getAmount() <= 0) inv.setItem(i, null);
            if (left <= 0) return;
        }
    }

    private void giveOrDrop(Player player, ItemStack item) {
        var leftover = player.getInventory().addItem(item);
        leftover.values().forEach(left -> player.getWorld().dropItemNaturally(player.getLocation(), left));
    }
}