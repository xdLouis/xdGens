package de.louis.xdGens.gui;

import de.louis.xdGens.crate.CrateType;
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
 * /crates GUI  —  4 rows (36 slots)
 *
 * Row 0 (0-8):   top border
 * Row 1 (9-17):  crate items  [filler | COMMON | UNCOMMON | RARE | EPIC | LEGENDARY | filler | info | filler]
 * Row 2 (18-26): open-all buttons (one per crate, same column)
 * Row 3 (27-35): bottom border
 *
 * Clicks:
 *   LEFT  click on crate row  → open 1 key
 *   RIGHT click on crate row  → open preview
 *   SHIFT+LEFT / MIDDLE on crate row OR any click on open-all row → open ALL keys
 */
public class CratesGUI {

    public static final String TITLE = "🎁 Crates";

    // columns 1-5 for the 5 crate types
    public static final int[] CRATE_SLOTS    = {10, 11, 12, 13, 14};
    public static final int[] OPEN_ALL_SLOTS = {19, 20, 21, 22, 23};
    public static final int   SLOT_INFO      = 16;

    private final Main plugin;

    public CratesGUI(Main plugin) {
        this.plugin = plugin;
    }

    public void open(Player player) {
        Inventory inv = Bukkit.createInventory(
                null, 36,
                MessageUtil.parse("<gradient:#a18cd1:#fbc2eb><bold>\uD83C\uDF81 Crates</bold></gradient>")
        );
        fill(inv);

        CrateType[] types = CrateType.values();
        for (int i = 0; i < types.length && i < CRATE_SLOTS.length; i++) {
            inv.setItem(CRATE_SLOTS[i],    buildCrateItem(player, types[i]));
            inv.setItem(OPEN_ALL_SLOTS[i], buildOpenAllItem(player, types[i]));
        }
        inv.setItem(SLOT_INFO, buildInfoItem(player));

        player.openInventory(inv);
    }

    // ── item builders ────────────────────────────────────────────────────

    private ItemStack buildCrateItem(Player player, CrateType type) {
        int amount = plugin.getVirtualKeyManager().getKeys(player, type);

        ItemStack item = new ItemStack(type.getIcon());
        ItemMeta  meta = item.getItemMeta();
        meta.displayName(MessageUtil.parse(
                type.getGradient() + "<bold>\u2746 " + type.getDisplayName() + " Crate \u2746</bold></gradient>"
        ));

        List<Component> lore = new ArrayList<>();
        lore.add(Component.empty());
        lore.add(MessageUtil.parse("<gray>Keys: " + (amount > 0 ? "<white>" : "<red>") + amount + (amount > 0 ? "</white>" : "</red>")));
        lore.add(MessageUtil.parse("<gray>Rewards: <white>Pouches + Cosmetics</white>"));
        lore.add(Component.empty());
        if (amount > 0) {
            lore.add(MessageUtil.parse("<green>\u25b6 Left-click  <white>open 1 key</white>"));
            lore.add(MessageUtil.parse("<yellow>\u25b6 Shift+Click <white>open ALL " + amount + " key" + (amount == 1 ? "" : "s") + "</white>"));
        } else {
            lore.add(MessageUtil.parse("<red>\u2718 No keys — break wheat to find some"));
        }
        lore.add(MessageUtil.parse("<aqua>\u25b6 Right-click <white>preview rewards \u0026 odds</white>"));
        meta.lore(lore);

        if (amount > 0) {
            meta.addEnchant(Enchantment.UNBREAKING, 1, true);
            meta.addItemFlags(ItemFlag.HIDE_ENCHANTS);
        }
        meta.addItemFlags(ItemFlag.HIDE_ATTRIBUTES);
        item.setItemMeta(meta);
        return item;
    }

    private ItemStack buildOpenAllItem(Player player, CrateType type) {
        int amount = plugin.getVirtualKeyManager().getKeys(player, type);

        Material mat  = amount > 0 ? Material.LIME_STAINED_GLASS_PANE : Material.RED_STAINED_GLASS_PANE;
        ItemStack item = new ItemStack(mat);
        ItemMeta  meta = item.getItemMeta();

        if (amount > 0) {
            meta.displayName(MessageUtil.parse(
                    "<green><bold>\u25b6\u25b6 Open ALL " + amount + " " + type.getDisplayName() + " key" + (amount == 1 ? "" : "s") + "</bold></green>"
            ));
            List<Component> lore = new ArrayList<>();
            lore.add(Component.empty());
            lore.add(MessageUtil.parse("<gray>Opens all " + amount + " " + type.getDisplayName() + " keys at once."));
            lore.add(MessageUtil.parse("<dark_gray>Pouches are auto-redeemed."));
            lore.add(Component.empty());
            lore.add(MessageUtil.parse("<yellow>\u26a0 Click to confirm"));
            meta.lore(lore);
        } else {
            meta.displayName(MessageUtil.parse(
                    "<red><bold>\u2718 No " + type.getDisplayName() + " keys</bold></red>"
            ));
            List<Component> lore = new ArrayList<>();
            lore.add(MessageUtil.parse("<dark_gray>Break wheat to find keys."));
            meta.lore(lore);
        }

        meta.addItemFlags(ItemFlag.HIDE_ATTRIBUTES);
        item.setItemMeta(meta);
        return item;
    }

    private ItemStack buildInfoItem(Player player) {
        ItemStack item = new ItemStack(Material.BOOK);
        ItemMeta  meta = item.getItemMeta();
        meta.displayName(MessageUtil.parse("<gradient:#a18cd1:#fbc2eb><bold>Key Finder</bold></gradient>"));
        List<Component> lore = new ArrayList<>();
        lore.add(Component.empty());
        lore.add(MessageUtil.parse("<gray>Level: <white>"
                + plugin.getHoeUpgradeManager().getKeyFinderLevel(player)
                + "</white><dark_gray>/1000"));
        lore.add(MessageUtil.parse("<gray>Chance: <gold>"
                + NumberUtil.format(plugin.getHoeUpgradeManager().getKeyFinderChance(player) * 100.0)
                + "%</gold> per wheat break"));
        lore.add(Component.empty());
        lore.add(MessageUtil.parse("<dark_gray>Keys go into this menu — not your inventory."));
        lore.add(MessageUtil.parse("<dark_gray>Crates can also drop chat tags \u0026 colors!"));
        meta.lore(lore);
        meta.addItemFlags(ItemFlag.HIDE_ATTRIBUTES);
        item.setItemMeta(meta);
        return item;
    }

    // ── helpers ──────────────────────────────────────────────────────────

    private void fill(Inventory inv) {
        ItemStack pane = pane(Material.BLACK_STAINED_GLASS_PANE);
        for (int i = 0; i < inv.getSize(); i++) inv.setItem(i, pane.clone());
    }

    private ItemStack pane(Material mat) {
        ItemStack p = new ItemStack(mat);
        ItemMeta  m = p.getItemMeta();
        m.displayName(Component.empty());
        p.setItemMeta(m);
        return p;
    }
}
