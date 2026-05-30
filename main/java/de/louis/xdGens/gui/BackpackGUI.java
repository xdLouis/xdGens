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
 *  Row 0 (0–8)   – Stats: [wheat] [blocks] [bales] [filler x4] [level]
 *  Rows 1–4 (9–44) – FREE INPUT SLOTS
 *  Row 5 (45–53) – [filler x3] [SELL] [filler] [UPGRADE] [filler x3]
 */
public class BackpackGUI {

    public static final String TITLE          = "Crop Backpack";
    public static final int    SLOT_WHEAT_STAT = 0;
    public static final int    SLOT_BLOCK_STAT = 1;
    public static final int    SLOT_BALE_STAT  = 2;
    public static final int    SLOT_LEVEL      = 8;
    public static final int    SLOT_SELL       = 48;
    public static final int    SLOT_UPGRADE    = 50;

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

        inv.setItem(SLOT_WHEAT_STAT, buildWheatStat());
        inv.setItem(SLOT_BLOCK_STAT, buildBlockStat());
        inv.setItem(SLOT_BALE_STAT,  buildBaleStat());
        inv.setItem(SLOT_LEVEL,      buildLevel());

        ItemStack filler = buildFiller();
        for (int i = 3; i <= 7; i++) inv.setItem(i, filler.clone());
        for (int i = 45; i <= 53; i++) inv.setItem(i, filler.clone());

        inv.setItem(SLOT_SELL,    buildSell());
        inv.setItem(SLOT_UPGRADE, buildUpgrade());

        // Slots 9-44 stay empty – free input area
        return inv;
    }

    // ─── stat items ─────────────────────────────────────────────────────────────

    private ItemStack buildWheatStat() {
        int stored  = plugin.getBackpackManager().getStoredWheat(player);
        int total   = plugin.getBackpackManager().getTotalStored(player);
        int cap     = plugin.getBackpackManager().getCapacity(player);
        ItemStack item = new ItemStack(Material.WHEAT);
        ItemMeta  meta = item.getItemMeta();
        meta.displayName(MessageUtil.parse("<gradient:#f6d365:#fda085><bold>Farm Wheat</bold></gradient>"));
        List<Component> lore = new ArrayList<>();
        lore.add(MessageUtil.parse("<gray>Stored: <yellow>" + NumberUtil.format(stored) + "</yellow>"));
        lore.add(MessageUtil.parse("<gray>Total used: <white>" + NumberUtil.format(total) + "</white><gray>/" + NumberUtil.format(cap)));
        lore.add(barComponent(total, cap));
        lore.add(MessageUtil.parse("<dark_gray>Put farm wheat into the slots below."));
        meta.lore(lore);
        meta.addItemFlags(ItemFlag.HIDE_ATTRIBUTES);
        item.setItemMeta(meta);
        return item;
    }

    private ItemStack buildBlockStat() {
        int stored = plugin.getBackpackManager().getStoredBlocks(player);
        ItemStack item = new ItemStack(Material.HAY_BLOCK);
        ItemMeta  meta = item.getItemMeta();
        meta.displayName(MessageUtil.parse("<gradient:#7afcff:#00c2ff><bold>Compressed Wheat Block</bold></gradient>"));
        List<Component> lore = new ArrayList<>();
        lore.add(MessageUtil.parse("<gray>Stored: <aqua>" + NumberUtil.format(stored) + "</aqua>"));
        lore.add(MessageUtil.parse("<dark_gray>Crafted at the Workstation."));
        meta.lore(lore);
        meta.addItemFlags(ItemFlag.HIDE_ATTRIBUTES);
        item.setItemMeta(meta);
        return item;
    }

    private ItemStack buildBaleStat() {
        int stored = plugin.getBackpackManager().getStoredBales(player);
        ItemStack item = new ItemStack(Material.SPONGE);
        ItemMeta  meta = item.getItemMeta();
        meta.displayName(MessageUtil.parse("<gradient:#f093fb:#f5576c><bold>Enchanted Wheat Bale</bold></gradient>"));
        List<Component> lore = new ArrayList<>();
        lore.add(MessageUtil.parse("<gray>Stored: <light_purple>" + NumberUtil.format(stored) + "</light_purple>"));
        lore.add(MessageUtil.parse("<dark_gray>Crafted at the Workstation."));
        meta.lore(lore);
        meta.addItemFlags(ItemFlag.HIDE_ATTRIBUTES);
        item.setItemMeta(meta);
        return item;
    }

    private ItemStack buildLevel() {
        int  level     = plugin.getBackpackManager().getLevel(player);
        int  free      = plugin.getBackpackManager().getFreeSpace(player);
        int  cap       = plugin.getBackpackManager().getCapacity(player);
        ItemStack item = new ItemStack(Material.NETHER_STAR);
        ItemMeta  meta = item.getItemMeta();
        meta.displayName(MessageUtil.parse("<aqua><bold>Backpack Level " + level + "</bold></aqua>"));
        List<Component> lore = new ArrayList<>();
        lore.add(MessageUtil.parse("<gray>Capacity: <white>" + NumberUtil.format(cap) + "</white>"));
        lore.add(MessageUtil.parse("<gray>Free: <green>" + NumberUtil.format(free) + "</green>"));
        meta.lore(lore);
        meta.addEnchant(Enchantment.UNBREAKING, 1, true);
        meta.addItemFlags(ItemFlag.HIDE_ENCHANTS, ItemFlag.HIDE_ATTRIBUTES);
        item.setItemMeta(meta);
        return item;
    }

    // ─── action buttons ──────────────────────────────────────────────────────────

    public ItemStack buildSell() {
        double wheatPrice = plugin.getConfig().getDouble("sell.wheat.price", 12.0);
        double blockPrice = plugin.getConfig().getDouble("sell.compressed_wheat_block.price", 900.0);
        double balePrice  = plugin.getConfig().getDouble("sell.enchanted_wheat_bale.price",  75000.0);
        int wheat  = plugin.getBackpackManager().getStoredWheat(player);
        int blocks = plugin.getBackpackManager().getStoredBlocks(player);
        int bales  = plugin.getBackpackManager().getStoredBales(player);
        double earned = (wheat * wheatPrice) + (blocks * blockPrice) + (bales * balePrice);

        ItemStack item = new ItemStack(Material.EMERALD);
        ItemMeta  meta = item.getItemMeta();
        meta.displayName(MessageUtil.parse("<gradient:#7afcff:#00c2ff><bold>Sell Contents</bold></gradient>"));
        List<Component> lore = new ArrayList<>();
        lore.add(MessageUtil.parse("<gray>Wheat: <yellow>"  + NumberUtil.format(wheat)  + "</yellow>"));
        lore.add(MessageUtil.parse("<gray>Blocks: <aqua>"   + NumberUtil.format(blocks) + "</aqua>"));
        lore.add(MessageUtil.parse("<gray>Bales: <light_purple>" + NumberUtil.format(bales) + "</light_purple>"));
        lore.add(MessageUtil.parse("<gray>Total: <green>$" + NumberUtil.format(earned) + "</green>"));
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
            double cost    = plugin.getBackpackManager().getUpgradeCost(player);
            double balance = plugin.getCurrencyManager().getMoney(player);
            meta.displayName(MessageUtil.parse("<gradient:#f6d365:#fda085><bold>Upgrade Backpack</bold></gradient>"));
            List<Component> lore = new ArrayList<>();
            lore.add(MessageUtil.parse("<gray>Level: <aqua>" + level + " → " + (level + 1) + "</aqua>"));
            lore.add(MessageUtil.parse("<gray>Cost: <green>$" + NumberUtil.format(cost) + "</green>"));
            lore.add(balance >= cost
                    ? MessageUtil.parse("<green>✔ You can afford this.")
                    : MessageUtil.parse("<red>✘ Not enough money."));
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
        int total  = 20;
        int filled = max > 0 ? (int) Math.round((double) current / max * total) : 0;
        StringBuilder sb = new StringBuilder("<gray>[");
        for (int i = 0; i < total; i++)
            sb.append(i < filled ? "<green>|" : "<dark_gray>|");
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

    public static boolean isInputSlot(int rawSlot) {
        return rawSlot >= INPUT_START && rawSlot <= INPUT_END;
    }
}
