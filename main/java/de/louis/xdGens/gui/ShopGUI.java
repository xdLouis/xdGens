package de.louis.xdGens.gui;

import de.louis.xdGens.main.Main;
import de.louis.xdGens.util.CustomItemUtil;
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

public class ShopGUI {

    public static final String TITLE          = "xdGens Shop";
    public static final double BACKPACK_PRICE = 50000.0;
    public static final int    SLOT_BACKPACK  = 13;

    private final Main plugin;
    private final Player player;

    public ShopGUI(Main plugin, Player player) {
        this.plugin = plugin;
        this.player = player;
    }

    public Inventory create() {
        Inventory inv = Bukkit.createInventory(null, 27,
                MessageUtil.parse("<gradient:#f6d365:#fda085><bold>xdGens Shop</bold></gradient>"));
        fill(inv);
        inv.setItem(SLOT_BACKPACK, buildBackpackItem());
        return inv;
    }

    private ItemStack buildBackpackItem() {
        boolean hasBackpack = plugin.getBackpackManager().playerHasItem(player);
        ItemStack item = new ItemStack(Material.BUNDLE);
        ItemMeta meta = item.getItemMeta();
        meta.displayName(MessageUtil.parse("<gradient:#84fab0:#8fd3f4><bold>Crop Backpack</bold></gradient>"));
        List<Component> lore = new ArrayList<>();
        lore.add(MessageUtil.parse("<gray>Automatically stores harvested crops.</gray>"));
        lore.add(MessageUtil.parse("<gray>Left-click item to open the backpack GUI.</gray>"));
        lore.add(MessageUtil.parse("<gray>Upgradeable up to level 25.</gray>"));
        lore.add(MessageUtil.parse(" "));
        if (hasBackpack) {
            lore.add(MessageUtil.parse("<red>You already own a backpack!</red>"));
        } else {
            lore.add(MessageUtil.parse("<gray>Price: <green>$" + NumberUtil.format(BACKPACK_PRICE) + "</green>"));
            lore.add(MessageUtil.parse("<dark_gray>Click to buy!</dark_gray>"));
            meta.addEnchant(Enchantment.UNBREAKING, 1, true);
            meta.addItemFlags(ItemFlag.HIDE_ENCHANTS);
        }
        meta.lore(lore);
        meta.addItemFlags(ItemFlag.HIDE_ATTRIBUTES);
        item.setItemMeta(meta);
        return item;
    }

    private void fill(Inventory inv) {
        ItemStack border = new ItemStack(Material.GRAY_STAINED_GLASS_PANE);
        ItemMeta meta = border.getItemMeta();
        meta.displayName(MessageUtil.parse("<gray> </gray>"));
        border.setItemMeta(meta);
        for (int i = 0; i < inv.getSize(); i++) inv.setItem(i, border.clone());
    }
}
