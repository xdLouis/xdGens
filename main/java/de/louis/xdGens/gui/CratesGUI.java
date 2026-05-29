package de.louis.xdGens.gui;

import de.louis.xdGens.crate.CrateType;
import de.louis.xdGens.main.Main;
import de.louis.xdGens.manager.PouchKeyItem;
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

public class CratesGUI {

    public static final String TITLE = "🎁 Crates";
    private final Main plugin;

    public CratesGUI(Main plugin) {
        this.plugin = plugin;
    }

    public void open(Player player) {
        Inventory inv = Bukkit.createInventory(null, 27, MessageUtil.parse("<gradient:#a18cd1:#fbc2eb><bold>🎁 Crates</bold></gradient>"));
        fill(inv, Material.BLACK_STAINED_GLASS_PANE);

        int slot = 10;
        for (CrateType type : CrateType.values()) {
            inv.setItem(slot++, buildCrateItem(player, type));
        }

        inv.setItem(22, buildInfoItem(player));
        player.openInventory(inv);
    }

    private ItemStack buildCrateItem(Player player, CrateType type) {
        int amount = countKeys(player, type);
        ItemStack item = new ItemStack(type.getIcon());
        ItemMeta meta = item.getItemMeta();
        meta.displayName(MessageUtil.parse(type.getGradient() + "<bold>✦ " + type.getDisplayName() + " Crate ✦</bold></gradient>"));

        List<Component> lore = new ArrayList<>();
        lore.add(Component.empty());
        lore.add(MessageUtil.parse("<gray>Your keys: <white>" + amount + "</white>"));
        lore.add(MessageUtil.parse("<gray>Rewards: <white>Money, XP, Tokens Pouches</white>"));
        lore.add(Component.empty());
        lore.add(amount > 0
                ? MessageUtil.parse("<green>▶ Click to open one key")
                : MessageUtil.parse("<red>✘ You don't own this key"));
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
        ItemMeta meta = item.getItemMeta();
        meta.displayName(MessageUtil.parse("<gradient:#a18cd1:#fbc2eb><bold>Key Finder</bold></gradient>"));
        List<Component> lore = new ArrayList<>();
        lore.add(Component.empty());
        lore.add(MessageUtil.parse("<gray>Level: <white>" + plugin.getHoeUpgradeManager().getKeyFinderLevel(player) + "</white><dark_gray>/1000"));
        lore.add(MessageUtil.parse("<gray>Chance: <gold>" + NumberUtil.format(plugin.getHoeUpgradeManager().getKeyFinderChance(player) * 100.0) + "%</gold> per wheat break"));
        lore.add(Component.empty());
        lore.add(MessageUtil.parse("<dark_gray>Even at level 1000 keys stay a bit rare."));
        meta.lore(lore);
        meta.addItemFlags(ItemFlag.HIDE_ATTRIBUTES);
        item.setItemMeta(meta);
        return item;
    }

    private int countKeys(Player player, CrateType type) {
        int count = 0;
        for (ItemStack item : player.getInventory().getContents()) {
            if (!PouchKeyItem.isKey(plugin, item)) continue;
            CrateType found = PouchKeyItem.getCrateType(plugin, item);
            if (found == type) count += item.getAmount();
        }
        return count;
    }

    private void fill(Inventory inv, Material material) {
        ItemStack pane = new ItemStack(material);
        ItemMeta meta = pane.getItemMeta();
        meta.displayName(Component.empty());
        pane.setItemMeta(meta);
        for (int i = 0; i < inv.getSize(); i++) inv.setItem(i, pane.clone());
    }
}
