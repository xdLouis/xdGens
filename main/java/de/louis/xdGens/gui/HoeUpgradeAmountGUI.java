package de.louis.xdGens.gui;

import de.louis.xdGens.main.Main;
import de.louis.xdGens.manager.HoeUpgradeManager;
import de.louis.xdGens.util.MessageUtil;
import de.louis.xdGens.util.NumberUtil;
import net.kyori.adventure.text.Component;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemFlag;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

import java.util.ArrayList;
import java.util.List;

public class HoeUpgradeAmountGUI {

    public static final String TITLE_PREFIX = "Buy Upgrade \u2013 ";
    public static final int SLOT_PLUS1  = 10;
    public static final int SLOT_PLUS10 = 11;
    public static final int SLOT_PLUS25 = 12;
    public static final int SLOT_PLUS50 = 13;
    public static final int SLOT_MAX    = 14;
    public static final int SLOT_BACK   = 22;

    private final Main   plugin;
    private final String type;

    public HoeUpgradeAmountGUI(Main plugin, String type) {
        this.plugin = plugin;
        this.type   = type;
    }

    public void open(Player player) {
        String label = switch (type) {
            case "crop"      -> "Crop Harvest";
            case "xp"        -> "XP Boost";
            case "keyfinder" -> "Key Finder";
            case "panda"     -> "Panda Roller";
            case "tnt"       -> "TNT Bomber";
            default          -> "Token Boost";
        };

        Inventory inv = Bukkit.createInventory(
                null, 27,
                MessageUtil.parse("<gradient:#a18cd1:#fbc2eb><bold>" + TITLE_PREFIX + label + "</bold></gradient>")
        );

        ItemStack filler = pane(Material.BLACK_STAINED_GLASS_PANE);
        for (int i = 0; i < 27; i++) inv.setItem(i, filler.clone());

        int     current = currentLevel(player);
        int     max     = maxLevel();
        long    tokens  = plugin.getCurrencyManager().getTokens(player);
        boolean maxed   = current >= max;

        inv.setItem(SLOT_PLUS1,  buildAmountItem(player, 1,  current, max, tokens, maxed));
        inv.setItem(SLOT_PLUS10, buildAmountItem(player, 10, current, max, tokens, maxed));
        inv.setItem(SLOT_PLUS25, buildAmountItem(player, 25, current, max, tokens, maxed));
        inv.setItem(SLOT_PLUS50, buildAmountItem(player, 50, current, max, tokens, maxed));
        inv.setItem(SLOT_MAX,    buildMaxItem(player, current, max, tokens, maxed));
        inv.setItem(SLOT_BACK,   buildBackItem());

        player.openInventory(inv);
    }

    private ItemStack buildAmountItem(Player player, int amount, int current, int max,
                                      long tokens, boolean maxed) {
        String grad = gradient();
        long   cost = 0;
        int    realAmount = Math.min(amount, max - current);

        if (!maxed && realAmount > 0) {
            for (int i = 1; i <= realAmount; i++) {
                long c = nextCost(current + i - 1);
                if (c >= 0) cost += c;
            }
        }

        boolean canAfford = !maxed && realAmount > 0 && tokens >= cost;
        Material mat = amountMaterial(amount);

        ItemStack item = new ItemStack(mat);
        ItemMeta  meta = item.getItemMeta();
        meta.displayName(MessageUtil.parse(grad + "<bold>+" + amount + " Level</bold></gradient>"));

        List<Component> lore = new ArrayList<>();
        lore.add(Component.empty());
        if (maxed || realAmount <= 0) {
            lore.add(MessageUtil.parse("<gold>Already at max level!"));
        } else {
            lore.add(MessageUtil.parse("<gray>Levels: <white>" + current + " \u2192 " + (current + realAmount)));
            lore.add(MessageUtil.parse("<gray>Cost:   <white>" + NumberUtil.format(cost) + " Tokens"));
            lore.add(MessageUtil.parse("<gray>Yours:  " + (tokens >= cost ? "<green>" : "<red>") + NumberUtil.format(tokens) + "</" + (tokens >= cost ? "green" : "red") + ">"));
            lore.add(Component.empty());
            lore.add(canAfford
                    ? MessageUtil.parse("<green>\u25BA Click to buy")
                    : MessageUtil.parse("<red>\u2718 Not enough Tokens"));
        }
        meta.lore(lore);
        meta.addItemFlags(ItemFlag.HIDE_ATTRIBUTES);
        item.setItemMeta(meta);
        return item;
    }

    private ItemStack buildMaxItem(Player player, int current, int max, long tokens, boolean maxed) {
        String grad      = gradient();
        int    remaining = max - current;
        long   cost      = 0;

        if (!maxed) {
            for (int i = 1; i <= remaining; i++) {
                long c = nextCost(current + i - 1);
                if (c >= 0) cost += c;
            }
        }

        boolean canAfford = !maxed && tokens >= cost;

        ItemStack item = new ItemStack(Material.NETHER_STAR);
        ItemMeta  meta = item.getItemMeta();
        meta.displayName(MessageUtil.parse(grad + "<bold>MAX</bold></gradient>"));

        List<Component> lore = new ArrayList<>();
        lore.add(Component.empty());
        if (maxed) {
            lore.add(MessageUtil.parse("<gold>Already at max level!"));
        } else {
            lore.add(MessageUtil.parse("<gray>Levels: <white>" + current + " \u2192 " + max));
            lore.add(MessageUtil.parse("<gray>Cost:   <white>" + NumberUtil.format(cost) + " Tokens"));
            lore.add(MessageUtil.parse("<gray>Yours:  " + (tokens >= cost ? "<green>" : "<red>") + NumberUtil.format(tokens) + "</" + (tokens >= cost ? "green" : "red") + ">"));
            lore.add(Component.empty());
            lore.add(canAfford
                    ? MessageUtil.parse("<green>\u25BA Click to buy all")
                    : MessageUtil.parse("<red>\u2718 Not enough Tokens"));
        }
        meta.lore(lore);
        meta.addItemFlags(ItemFlag.HIDE_ATTRIBUTES);
        item.setItemMeta(meta);
        return item;
    }

    private ItemStack buildBackItem() {
        ItemStack item = new ItemStack(Material.ARROW);
        ItemMeta  meta = item.getItemMeta();
        meta.displayName(MessageUtil.parse("<gray>\u25C4 Back"));
        meta.addItemFlags(ItemFlag.HIDE_ATTRIBUTES);
        item.setItemMeta(meta);
        return item;
    }

    private int currentLevel(Player player) {
        HoeUpgradeManager m = plugin.getHoeUpgradeManager();
        return switch (type) {
            case "crop"      -> m.getCropLevel(player);
            case "xp"        -> m.getXpLevel(player);
            case "keyfinder" -> m.getKeyFinderLevel(player);
            case "panda"     -> m.getPandaLevel(player);
            case "tnt"       -> m.getTntLevel(player);
            default          -> m.getTokenLevel(player);
        };
    }

    private int maxLevel() {
        return switch (type) {
            case "crop"      -> HoeUpgradeManager.MAX_CROP_LEVEL;
            case "xp"        -> HoeUpgradeManager.MAX_XP_LEVEL;
            case "keyfinder" -> HoeUpgradeManager.MAX_KEY_FINDER_LEVEL;
            case "panda"     -> HoeUpgradeManager.MAX_PANDA_LEVEL;
            case "tnt"       -> HoeUpgradeManager.MAX_TNT_LEVEL;
            default          -> HoeUpgradeManager.MAX_TOKEN_LEVEL;
        };
    }

    private long nextCost(int fromLevel) {
        HoeUpgradeManager m = plugin.getHoeUpgradeManager();
        return switch (type) {
            case "crop"      -> m.getCropCost(fromLevel + 1);
            case "xp"        -> m.getXpCost(fromLevel + 1);
            case "keyfinder" -> m.getKeyFinderCost(fromLevel + 1);
            case "panda"     -> m.getPandaCost(fromLevel + 1);
            case "tnt"       -> m.getTntCost(fromLevel + 1);
            default          -> m.getTokenCost(fromLevel + 1);
        };
    }

    private String gradient() {
        return switch (type) {
            case "crop"      -> "<gradient:#f6d365:#fda085>";
            case "xp"        -> "<gradient:#7afcff:#00c2ff>";
            case "keyfinder" -> "<gradient:#a18cd1:#fbc2eb>";
            case "panda"     -> "<gradient:#a8e6cf:#88d8b0>";
            case "tnt"       -> "<gradient:#ff6b6b:#ffd93d>";
            default          -> "<gradient:#ffd86f:#fc6262>";
        };
    }

    private Material amountMaterial(int amount) {
        return switch (amount) {
            case 1  -> Material.LIME_DYE;
            case 10 -> Material.GREEN_DYE;
            case 25 -> Material.EMERALD;
            case 50 -> Material.DIAMOND;
            default -> Material.NETHER_STAR;
        };
    }

    private ItemStack pane(Material mat) {
        ItemStack p = new ItemStack(mat);
        ItemMeta  m = p.getItemMeta();
        m.displayName(Component.empty());
        p.setItemMeta(m);
        return p;
    }
}
