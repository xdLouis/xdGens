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
 * /crates GUI  —  3 rows (27 slots)
 *
 * Row 0+2: filler
 * Row 1 (slots 9-17):
 *   9  – filler
 *   10-14 – one slot per CrateType (COMMON→LEGENDARY)
 *   15 – filler
 *   16 – Key Finder info
 *   17 – filler
 *
 * When a crate item is clicked:
 *   LEFT  click → open 1 key
 *   RIGHT click → open preview GUI (all cosmetics + odds)
 */
public class CratesGUI {

    public static final String TITLE = "🎁 Crates";

    // crate slots (index = CrateType.ordinal())
    public static final int[] CRATE_SLOTS = {10, 11, 12, 13, 14};
    public static final int   SLOT_INFO   = 16;

    private final Main plugin;

    public CratesGUI(Main plugin) {
        this.plugin = plugin;
    }

    public void open(Player player) {
        Inventory inv = Bukkit.createInventory(
                null, 27,
                MessageUtil.parse("<gradient:#a18cd1:#fbc2eb><bold>\uD83C\uDF81 Crates</bold></gradient>")
        );
        fill(inv);

        CrateType[] types = CrateType.values();
        for (int i = 0; i < types.length && i < CRATE_SLOTS.length; i++) {
            inv.setItem(CRATE_SLOTS[i], buildCrateItem(player, types[i]));
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
        lore.add(MessageUtil.parse("<gray>Your keys: " + (amount > 0 ? "<white>" : "<red>") + amount + (amount > 0 ? "</white>" : "</red>")));
        lore.add(MessageUtil.parse("<gray>Rewards: <white>Money, XP, Tokens + Cosmetics</white>"));
        lore.add(Component.empty());
        if (amount > 0) {
            lore.add(MessageUtil.parse("<green>\u25b6 Left-click to open <white>1</white> key"));
        } else {
            lore.add(MessageUtil.parse("<red>\u2718 No keys — break wheat to find some"));
        }
        lore.add(MessageUtil.parse("<aqua>\u25b6 Right-click to preview <white>all rewards \u0026 odds</white>"));
        meta.lore(lore);

        if (amount > 0) {
            meta.addEnchant(Enchantment.UNBREAKING, 1, true);
            meta.addItemFlags(ItemFlag.HIDE_ENCHANTS);
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
