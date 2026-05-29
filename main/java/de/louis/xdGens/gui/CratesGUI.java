package de.louis.xdGens.gui;

import de.louis.xdGens.crate.CrateType;
import de.louis.xdGens.main.Main;
import de.louis.xdGens.util.MessageUtil;
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
 * /crates  —  3 rows (27 slots)
 *
 *  Slot:  0  1  2  3  4  5  6  7  8
 *  R0:   [b][b][b][b][b][b][b][b][b]
 *  R1:   [b][C][U][R][E][L][b][i][b]    C–L = Crates, i = Key Finder info
 *  R2:   [b][b][b][b][b][b][b][b][b]
 *
 *  Click scheme:
 *    LEFT        → open 1 key
 *    SHIFT+LEFT  → open all keys
 *    RIGHT       → Rewards & Odds preview
 */
public class CratesGUI {

    public static final int[] CRATE_SLOTS = {10, 11, 12, 13, 14};
    public static final int   SLOT_INFO   = 16;

    private final Main plugin;

    public CratesGUI(Main plugin) {
        this.plugin = plugin;
    }

    public void open(Player player) {
        Inventory inv = Bukkit.createInventory(
                null, 27,
                MessageUtil.parse("\uD83C\uDF81 Crates")
        );

        ItemStack filler = pane(Material.BLACK_STAINED_GLASS_PANE);
        for (int i = 0; i < 27; i++) inv.setItem(i, filler.clone());

        CrateType[] types = CrateType.values();
        for (int i = 0; i < types.length && i < CRATE_SLOTS.length; i++) {
            inv.setItem(CRATE_SLOTS[i], buildCrateItem(player, types[i]));
        }

        inv.setItem(SLOT_INFO, buildInfoItem(player));
        player.openInventory(inv);
    }

    // ── Crate item ─────────────────────────────────────────────────

    private ItemStack buildCrateItem(Player player, CrateType type) {
        int keys = plugin.getVirtualKeyManager().getKeys(player, type);

        ItemStack item = new ItemStack(type.getIcon());
        ItemMeta  meta = item.getItemMeta();
        meta.displayName(MessageUtil.parse(
                type.getGradient() + "<bold>" + type.getDisplayName() + " Crate</bold></gradient>"
        ));

        List<Component> lore = new ArrayList<>();
        lore.add(Component.empty());

        if (keys > 0) {
            lore.add(MessageUtil.parse("<gray>Keys: <white>" + keys + "</white>"));
        } else {
            lore.add(MessageUtil.parse("<gray>Keys: <red>0</red> <dark_gray>— find them in wheat"));
        }

        lore.add(Component.empty());
        lore.add(MessageUtil.parse("<gray>\u25b6 <white>Left-click</white>         open 1 key"));
        lore.add(MessageUtil.parse("<gray>\u25b6 <white>Shift+Left-click</white>  open all" + (keys > 0 ? " (" + keys + ")" : "") + " keys"));
        lore.add(MessageUtil.parse("<gray>\u25b6 <white>Right-click</white>        Rewards & Odds"));

        meta.lore(lore);
        if (keys > 0) {
            meta.addEnchant(Enchantment.UNBREAKING, 1, true);
            meta.addItemFlags(ItemFlag.HIDE_ENCHANTS);
        }
        meta.addItemFlags(ItemFlag.HIDE_ATTRIBUTES);
        item.setItemMeta(meta);
        return item;
    }

    // ── Key Finder info ────────────────────────────────────────────

    private ItemStack buildInfoItem(Player player) {
        int    kfLevel  = plugin.getHoeUpgradeManager().getKeyFinderLevel(player);
        double kfChance = plugin.getHoeUpgradeManager().getKeyFinderChance(player) * 100.0;

        ItemStack item = new ItemStack(Material.BOOK);
        ItemMeta  meta = item.getItemMeta();
        meta.displayName(MessageUtil.parse("<white><bold>Key Finder</bold></white>"));

        List<Component> lore = new ArrayList<>();
        lore.add(Component.empty());
        lore.add(MessageUtil.parse("<gray>Level: <white>" + kfLevel + "</white><dark_gray>/1000"));
        lore.add(MessageUtil.parse("<gray>Chance: <gold>" + String.format("%.2f", kfChance) + "%</gold> per wheat"));
        lore.add(Component.empty());
        lore.add(MessageUtil.parse("<dark_gray>Keys are stored here, not in your inventory."));
        meta.lore(lore);
        meta.addItemFlags(ItemFlag.HIDE_ATTRIBUTES);
        item.setItemMeta(meta);
        return item;
    }

    // ── Helper ─────────────────────────────────────────────────────

    private ItemStack pane(Material mat) {
        ItemStack p = new ItemStack(mat);
        ItemMeta  m = p.getItemMeta();
        m.displayName(Component.empty());
        p.setItemMeta(m);
        return p;
    }
}
