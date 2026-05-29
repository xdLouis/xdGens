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
        try { if (!dataFile.exists()) dataFile.createNewFile(); }
        catch (IOException e) { plugin.getLogger().severe("Could not create workstations.yml: " + e.getMessage()); }
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

    /**
     * Right-click craft logic:
     *  1. farm_wheat (inv + backpack) -> compressed_wheat_block  (64:1)
     *  2. compressed_wheat_block (inv) -> enchanted_wheat_bale   (64:1)
     */
    public void useWorkstation(Player player) {
        Inventory inv = player.getInventory();

        // Step 1: farm_wheat -> compressed_wheat_block
        int wheatInInv = count(inv, "farm_wheat");
        int wheatInBp  = plugin.getBackpackManager().getStoredWheat(player);
        int totalWheat = wheatInInv + wheatInBp;
        int blocksCreated = totalWheat / 64;

        if (blocksCreated > 0) {
            int wheatNeeded    = blocksCreated * 64;
            int removeFromInv  = Math.min(wheatInInv, wheatNeeded);
            int removeFromBp   = wheatNeeded - removeFromInv;
            remove(inv, "farm_wheat", removeFromInv);
            if (removeFromBp > 0) {
                plugin.getBackpackManager().removeWheat(player, removeFromBp);
                plugin.getBackpackManager().savePlayer(player);
            }
            for (int i = 0; i < blocksCreated; i++)
                giveOrDrop(player, CustomItemUtil.createCompressedWheatBlock(plugin, 1));
        }

        // Step 2: compressed_wheat_block -> enchanted_wheat_bale
        int blocksInInv  = count(inv, "compressed_wheat_block");
        int balesCreated = blocksInInv / 64;
        if (balesCreated > 0) {
            remove(inv, "compressed_wheat_block", balesCreated * 64);
            for (int i = 0; i < balesCreated; i++)
                giveOrDrop(player, CustomItemUtil.createEnchantedWheatBale(plugin, 1));
        }

        if (blocksCreated == 0 && balesCreated == 0) {
            MessageUtil.sendRaw(player, MessageUtil.PREFIX + " <red>Not enough materials. Need 64 Farm Wheat or 64 Compressed Wheat Blocks.</red>");
            return;
        }

        StringBuilder msg = new StringBuilder(MessageUtil.PREFIX + " <gradient:#7afcff:#00c2ff>Crafted!</gradient> <gray>");
        if (blocksCreated > 0) msg.append(blocksCreated).append("x Compressed Block");
        if (blocksCreated > 0 && balesCreated > 0) msg.append(", ");
        if (balesCreated > 0) msg.append(balesCreated).append("x Enchanted Bale");
        msg.append("</gray>");
        MessageUtil.sendRaw(player, msg.toString());
    }

    public void removeAll() { hologramManager.removeAll(); }

    private void save() {
        dataConfig.set("workstations", List.copyOf(workstations));
        try { dataConfig.save(dataFile); }
        catch (IOException e) { plugin.getLogger().severe("Could not save workstations.yml: " + e.getMessage()); }
    }

    private String serialize(Location loc) {
        return loc.getWorld().getName() + ":" + loc.getBlockX() + ":" + loc.getBlockY() + ":" + loc.getBlockZ();
    }

    private Location deserialize(String key) {
        String[] p = key.split(":");
        if (p.length != 4) return null;
        World world = Bukkit.getWorld(p[0]);
        if (world == null) return null;
        try { return new Location(world, Integer.parseInt(p[1]), Integer.parseInt(p[2]), Integer.parseInt(p[3])); }
        catch (NumberFormatException e) { return null; }
    }

    private int count(Inventory inv, String type) {
        int total = 0;
        for (ItemStack item : inv.getContents())
            if (CustomItemUtil.hasItemType(plugin, item, type)) total += item.getAmount();
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
        leftover.values().forEach(l -> player.getWorld().dropItemNaturally(player.getLocation(), l));
    }
}
