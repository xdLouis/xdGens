package de.louis.xdGens.gui;

import de.louis.xdGens.main.Main;
import de.louis.xdGens.util.MessageUtil;
import de.louis.xdGens.util.NumberUtil;
import net.kyori.adventure.text.Component;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.entity.Player;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemFlag;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

import java.util.ArrayList;
import java.util.List;

/**
 * Layout (6 rows = 54 slots):
 *
 *  Row 0 (0-8)   – Stats bar: wheat icon | compressed icon | filler | ... | filler | level icon
 *  Row 1-3 (9-35) – FREE INPUT SLOTS (player drops crops here)
 *  Row 4 (36-44) – FREE INPUT SLOTS (continues)
 *  Row 5 (45-53) – Action bar: filler | filler | filler | SELL | filler | UPGRADE | filler | filler | filler
 *
 * Actual slot assignment:
 *   SLOT_WHEAT_STAT   = 0
 *   SLOT_BLOCK_STAT   = 1
 *   SLOT_BALE_STAT    = 2
 *   SLOT_LEVEL        = 8
 *   SLOT_SELL         = 48
 *   SLOT_UPGRADE      = 50
 *   Free input: 9-44
 */
public class BackpackGUI {

    public static final String TITLE         = "Crop Backpack";
    public static final int    SLOT_WHEAT_STAT = 0;
    public static final int    SLOT_BLOCK_STAT = 1;
    public static final int    SLOT_BALE_STAT  = 2;
    public static final int    SLOT_LEVEL      = 8;
    public static final int    SLOT_SELL       = 48;
    public static final int    SLOT_UPGRADE    = 50;

    // Slots 9-44 are the free input area (rows 1-4)
    public static final int INPUT_START = 9;
    public static final int INPUT_END   = 44;

    private final Main   plugin;
    private final Player player;

    public BackpackGUI(Main plugin, Player player) {
        this.plugin = plugin;
        this.player = player;
    }

    public Inventory create() {
        Inventory inv = Bukkit.createInventory(null, 54,
                MessageUtil.parse("<gradient:#84fab0:#8fd3f4><bold>Crop Backpack</bold></gradient>"));

        // Row 0: stats
        inv.setItem(SLOT_WHEAT_STAT, buildWheatStat());
        inv.setItem(SLOT_BLOCK_STAT, buildBlockStat());
        inv.setItem(SLOT_BALE_STAT,  buildBaleStat());
        inv.setItem(SLOT_LEVEL,      buildLevel());

        // Fill top-row gaps (slots 3-7) with filler
        ItemStack filler = buildFiller();
        for (int i = 3; i <= 7; i++) inv.setItem(i, filler.clone());

        // Row 5: action bar
        for (int i = 45; i <= 53; i++) inv.setItem(i, filler.clone());
        inv.setItem(SLOT_SELL,    buildSell());
        inv.setItem(SLOT_UPGRADE, buildUpgrade());

        // Slots 9-44 stay null = open for the player to put items in
        return inv;
    }

    // ─── stat items ─────────────────────────────────────────────────────────────

    private ItemStack buildWheatStat() {
        int stored = plugin.getBackpackManager().getStoredWheat(player);
        int cap    = plugin.getBackpackManager().getCapacity(player);
        ItemStack item = new ItemStack(Material.WHEAT);
        ItemMeta  meta = item.getItemMeta();
        meta.displayName(MessageUtil.parse("<yellow><bold>Farm Wheat</bold></yellow>"));
        List<Component> lore = new ArrayList<>();
        lore.add(MessageUtil.parse("<gray>Stored: <yellow>" + NumberUtil.format(stored) + "</yellow>"));
        lore.add(MessageUtil.parse("<gray>Capacity: <white>" + NumberUtil.format(cap) + "</white>"));
        lore.add(barComponent(stored, cap));
        lore.add(MessageUtil.parse("<dark_gray>Put crops in the slots below."));
        meta.lore(lore);
        meta.addItemFlags(ItemFlag.HIDE_ATTRIBUTES);
        item.setItemMeta(meta);
        return item;
    }

    private ItemStack buildBlockStat() {
        // Compressed wheat blocks are stored in inventory, not the BP – just counts items
        ItemStack item = new ItemStack(Material.HAY_BLOCK);
        ItemMeta  meta = item.getItemMeta();
        meta.displayName(MessageUtil.parse("<gradient:#7afcff:#00c2ff><bold>Compressed Blocks</bold></gradient>"));
        List<Component> lore = new ArrayList<>();
        lore.add(MessageUtil.parse("<dark_gray>Counted from your inventory."));
        meta.lore(lore);
        meta.addItemFlags(ItemFlag.HIDE_ATTRIBUTES);
        item.setItemMeta(meta);
        return item;
    }

    private ItemStack buildBaleStat() {
        ItemStack item = new ItemStack(Material.SPONGE);
        ItemMeta  meta = item.getItemMeta();
        meta.displayName(MessageUtil.parse("<gradient:#f093fb:#f5576c><bold>Enchanted Bales</bold></gradient>"));
        List<Component> lore = new ArrayList<>();
        lore.add(MessageUtil.parse("<dark_gray>Counted from your inventory."));
        meta.lore(lore);
        meta.addItemFlags(ItemFlag.HIDE_ATTRIBUTES);
        item.setItemMeta(meta);
        return item;
    }

    private ItemStack buildLevel() {
        int  level      = plugin.getBackpackManager().getLevel(player);
        ItemStack item  = new ItemStack(Material.NETHER_STAR);
        ItemMeta  meta  = item.getItemMeta();
        meta.displayName(MessageUtil.parse("<aqua><bold>Backpack Level " + level + "</bold></aqua>"));
        List<Component> lore = new ArrayList<>();
        lore.add(MessageUtil.parse("<gray>Free space: <green>"
                + NumberUtil.format(plugin.getBackpackManager().getFreeSpace(player)) + "</green>"));
        meta.lore(lore);
        meta.addEnchant(Enchantment.UNBREAKING, 1, true);
        meta.addItemFlags(ItemFlag.HIDE_ENCHANTS, ItemFlag.HIDE_ATTRIBUTES);
        item.setItemMeta(meta);
        return item;
    }

    // ─── action buttons ──────────────────────────────────────────────────────────

    public ItemStack buildSell() {
        ItemStack item = new ItemStack(Material.EMERALD);
        ItemMeta  meta = item.getItemMeta();
        int wheat = plugin.getBackpackManager().getStoredWheat(player);
        double earned = wheat * plugin.getConfig().getDouble("sell.wheat.price", 12.0);
        meta.displayName(MessageUtil.parse("<gradient:#7afcff:#00c2ff><bold>Sell Contents</bold></gradient>"));
        List<Component> lore = new ArrayList<>();
        lore.add(MessageUtil.parse("<gray>Wheat: <yellow>" + NumberUtil.format(wheat) + "</yellow>"));
        lore.add(MessageUtil.parse("<gray>Earn: <green>$" + NumberUtil.format(earned) + "</green>"));
        lore.add(MessageUtil.parse("<dark_gray>Same prices as /sell."));
        meta.lore(lore);
        meta.addEnchant(Enchantment.UNBREAKING, 1, true);
        meta.addItemFlags(ItemFlag.HIDE_ENCHANTS, ItemFlag.HIDE_ATTRIBUTES);
        item.setItemMeta(meta);
        return item;
    }

    public ItemStack buildUpgrade() {
        boolean   canUpgrade = plugin.getBackpackManager().canUpgrade(player);
        int       level      = plugin.getBackpackManager().getLevel(player);
        ItemStack item       = new ItemStack(canUpgrade ? Material.NETHER_STAR : Material.COAL);
        ItemMeta  meta       = item.getItemMeta();
        if (canUpgrade) {
            double cost = plugin.getBackpackManager().getUpgradeCost(player);
            meta.displayName(MessageUtil.parse("<gradient:#f6d365:#fda085><bold>Upgrade Backpack</bold></gradient>"));
            List<Component> lore = new ArrayList<>();
            lore.add(MessageUtil.parse("<gray>Level: <aqua>" + level + " → " + (level + 1) + "</aqua>"));
            lore.add(MessageUtil.parse("<gray>Cost: <green>$" + NumberUtil.format(cost) + "</green>"));
            double balance = plugin.getCurrencyManager().getMoney(player);
            if (balance >= cost)
                lore.add(MessageUtil.parse("<green>✔ You can afford this."));
            else
                lore.add(MessageUtil.parse("<red>✘ Not enough money."));
            lore.add(MessageUtil.parse("<dark_gray>Click to upgrade!"));
            meta.lore(lore);
            meta.addEnchant(Enchantment.UNBREAKING, 1, true);
            meta.addItemFlags(ItemFlag.HIDE_ENCHANTS, ItemFlag.HIDE_ATTRIBUTES);
        } else {
            meta.displayName(MessageUtil.parse("<gray><bold>Upgrade Backpack</bold></gray>"));
            meta.lore(List.of(MessageUtil.parse("<red>Max level reached!</red>")));
            meta.addItemFlags(ItemFlag.HIDE_ATTRIBUTES);
        }
        item.setItemMeta(meta);
        return item;
    }

    // ─── helpers ─────────────────────────────────────────────────────────────────

    private Component barComponent(int current, int max) {
        int total = 20;
        int filled = max > 0 ? (int) Math.round((double) current / max * total) : 0;
        StringBuilder sb = new StringBuilder("<gray>[");
        for (int i = 0; i < total; i++) {
            sb.append(i < filled ? "<green>|" : "<dark_gray>|");
        }
        sb.append("<gray>]");
        return MessageUtil.parse(sb.toString());
    }

    private ItemStack buildFiller() {
        ItemStack border = new ItemStack(Material.GRAY_STAINED_GLASS_PANE);
        ItemMeta  meta   = border.getItemMeta();
        meta.displayName(Component.empty());
        border.setItemMeta(meta);
        return border;
    }

    /** Returns true if slot is within the free input area (rows 1-4). */
    public static boolean isInputSlot(int rawSlot) {
        return rawSlot >= INPUT_START && rawSlot <= INPUT_END;
    }
}
