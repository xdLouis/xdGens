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

    // ── slots (5 rows = 45) ─────────────────────────────────────────
    //  Row 0 (0-8)   : top border
    //  Row 1 (9-17)  : info bar (border | info-item | border)
    //  Row 2 (18-26) : separator
    //  Row 3 (27-35) : border | +1 | +5 | +10 | +25 | +50 | MAX | border
    //  Row 4 (36-44) : bottom border with BACK in middle
    public static final int SLOT_PLUS1  = 28;
    public static final int SLOT_PLUS5  = 29;
    public static final int SLOT_PLUS10 = 30;
    public static final int SLOT_PLUS25 = 31;
    public static final int SLOT_PLUS50 = 32;
    public static final int SLOT_MAX    = 33;
    public static final int SLOT_BACK   = 40;
    public static final int SLOT_INFO   = 13;

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
                null, 45,
                MessageUtil.parse(gradient() + "<bold>" + TITLE_PREFIX + label + "</bold></gradient>")
        );

        // ── borders ────────────────────────────────────────────────────
        ItemStack dark  = pane(Material.BLACK_STAINED_GLASS_PANE);
        ItemStack gray  = pane(Material.GRAY_STAINED_GLASS_PANE);
        ItemStack light = pane(Material.LIGHT_GRAY_STAINED_GLASS_PANE);

        for (int i = 0; i <= 8; i++)   inv.setItem(i, dark.clone());
        inv.setItem(9,  dark.clone());
        inv.setItem(10, gray.clone());
        inv.setItem(11, gray.clone());
        inv.setItem(12, gray.clone());
        inv.setItem(14, gray.clone());
        inv.setItem(15, gray.clone());
        inv.setItem(16, gray.clone());
        inv.setItem(17, dark.clone());
        for (int i = 18; i <= 26; i++) inv.setItem(i, dark.clone());
        inv.setItem(27, dark.clone());
        inv.setItem(34, dark.clone());
        for (int i = 36; i <= 44; i++) inv.setItem(i, dark.clone());
        inv.setItem(37, light.clone());
        inv.setItem(38, light.clone());
        inv.setItem(39, light.clone());
        inv.setItem(41, light.clone());
        inv.setItem(42, light.clone());
        inv.setItem(43, light.clone());

        int     current    = currentLevel(player);
        int     max        = maxLevel();
        long    tokens     = plugin.getCurrencyManager().getTokens(player);
        boolean maxed      = current >= max;
        int     affordable = computeAffordable(current, max, tokens);

        inv.setItem(SLOT_INFO,   buildInfoItem(current, max, tokens, affordable));
        inv.setItem(SLOT_PLUS1,  buildAmountItem(1,  current, max, tokens, maxed));
        inv.setItem(SLOT_PLUS5,  buildAmountItem(5,  current, max, tokens, maxed));
        inv.setItem(SLOT_PLUS10, buildAmountItem(10, current, max, tokens, maxed));
        inv.setItem(SLOT_PLUS25, buildAmountItem(25, current, max, tokens, maxed));
        inv.setItem(SLOT_PLUS50, buildAmountItem(50, current, max, tokens, maxed));
        inv.setItem(SLOT_MAX,    buildMaxItem(current, max, tokens, affordable, maxed));
        inv.setItem(SLOT_BACK,   buildBackItem());

        player.openInventory(inv);
    }

    // ──────────────────────────────────────────────────────────────────
    //  Info item (slot 13)
    // ──────────────────────────────────────────────────────────────────

    private ItemStack buildInfoItem(int current, int max, long tokens, int affordable) {
        ItemStack item = new ItemStack(upgradeIcon());
        ItemMeta  meta = item.getItemMeta();
        meta.displayName(MessageUtil.parse(gradient() + "<bold>" + upgradeLabel() + "</bold></gradient>"));

        List<Component> lore = new ArrayList<>();
        lore.add(Component.empty());
        lore.add(MessageUtil.parse("<dark_gray>\u2500\u2500\u2500\u2500\u2500\u2500\u2500\u2500\u2500\u2500\u2500\u2500\u2500\u2500\u2500\u2500\u2500\u2500"));
        lore.add(MessageUtil.parse("<gray>Current Level  <white>" + current + " <dark_gray>/ " + max));
        lore.add(MessageUtil.parse("<gray>Your Tokens    " + (tokens > 0 ? "<gold>" : "<red>") + NumberUtil.format(tokens)));
        if (current != max) {
            long nextCost = nextCost(current);
            lore.add(MessageUtil.parse("<gray>Next Level     <white>" + NumberUtil.format(nextCost) + " <dark_gray>Tokens"));
            lore.add(Component.empty());
            if (affordable > 0) {
                lore.add(MessageUtil.parse("<green>\u2714 You can afford <white><bold>" + affordable + "</bold></white><green> level" + (affordable == 1 ? "" : "s")));
            } else {
                lore.add(MessageUtil.parse("<red>\u2718 Cannot afford next level"));
            }
        } else {
            lore.add(Component.empty());
            lore.add(MessageUtil.parse("<gold>\u2605 Fully upgraded!"));
        }
        lore.add(MessageUtil.parse("<dark_gray>\u2500\u2500\u2500\u2500\u2500\u2500\u2500\u2500\u2500\u2500\u2500\u2500\u2500\u2500\u2500\u2500\u2500\u2500"));

        meta.lore(lore);
        meta.addItemFlags(ItemFlag.HIDE_ATTRIBUTES, ItemFlag.HIDE_ENCHANTS);
        item.setItemMeta(meta);
        return item;
    }

    // ──────────────────────────────────────────────────────────────────
    //  Amount buttons (+1 / +5 / +10 / +25 / +50)
    // ──────────────────────────────────────────────────────────────────

    private ItemStack buildAmountItem(int amount, int current, int max, long tokens, boolean maxed) {
        int  realAmount = Math.min(amount, max - current);
        long cost       = totalCost(current, realAmount);
        boolean valid     = !maxed && realAmount > 0 && cost >= 0;
        boolean canAfford = valid && tokens >= cost;

        Material  mat  = canAfford ? amountMaterialGreen(amount) : amountMaterialRed(amount);
        ItemStack item = new ItemStack(mat);
        ItemMeta  meta = item.getItemMeta();

        String titleColor = canAfford ? "<green>" : (valid ? "<red>" : "<dark_gray>");
        meta.displayName(MessageUtil.parse(titleColor + "<bold>+" + amount + " Level</bold>"));

        List<Component> lore = new ArrayList<>();
        lore.add(Component.empty());
        if (!valid) {
            lore.add(MessageUtil.parse(maxed ? "<gold>\u2605 Already maxed out" : "<red>\u2718 Not available"));
        } else {
            lore.add(MessageUtil.parse("<dark_gray>" + current + " \u27a1 " + (current + realAmount)
                    + (realAmount < amount ? " <dark_gray>(capped)" : "")));
            lore.add(Component.empty());
            lore.add(MessageUtil.parse("<gray>Cost   <white>" + NumberUtil.format(cost) + " Tokens"));
            lore.add(MessageUtil.parse("<gray>Yours  " + (canAfford ? "<green>" : "<red>") + NumberUtil.format(tokens)));
            lore.add(Component.empty());
            lore.add(canAfford
                    ? MessageUtil.parse("<green><bold>\u25BA Click to purchase")
                    : MessageUtil.parse("<red>\u2718 Not enough Tokens"));
        }
        meta.lore(lore);
        meta.addItemFlags(ItemFlag.HIDE_ATTRIBUTES);
        item.setItemMeta(meta);
        return item;
    }

    // ──────────────────────────────────────────────────────────────────
    //  MAX button — shows how many levels you can actually afford
    // ──────────────────────────────────────────────────────────────────

    private ItemStack buildMaxItem(int current, int max, long tokens, int affordable, boolean maxed) {
        ItemStack item = new ItemStack(affordable > 0 ? Material.NETHER_STAR : Material.COAL);
        ItemMeta  meta = item.getItemMeta();

        List<Component> lore = new ArrayList<>();
        lore.add(Component.empty());

        if (maxed) {
            meta.displayName(MessageUtil.parse("<gold><bold>MAX</bold>"));
            lore.add(MessageUtil.parse("<gold>\u2605 Already fully upgraded!"));
        } else if (affordable <= 0) {
            meta.displayName(MessageUtil.parse("<red><bold>MAX</bold>"));
            lore.add(MessageUtil.parse("<red>\u2718 Cannot afford any level"));
            lore.add(MessageUtil.parse("<gray>Yours  <red>" + NumberUtil.format(tokens)));
            long nextCost = nextCost(current);
            lore.add(MessageUtil.parse("<gray>Need   <white>" + NumberUtil.format(nextCost) + " Tokens"));
        } else {
            long cost       = totalCost(current, affordable);
            boolean isRealMax = (current + affordable) >= max;

            meta.displayName(MessageUtil.parse(gradient() + "<bold>MAX  <white>(" + affordable
                    + " level" + (affordable == 1 ? "" : "s") + ")</white></bold></gradient>"));
            lore.add(MessageUtil.parse("<dark_gray>" + current + " \u27a1 " + (current + affordable)
                    + (isRealMax ? " <gold>(max!)" : "")));
            lore.add(Component.empty());
            lore.add(MessageUtil.parse("<gray>Levels   <white><bold>" + affordable + "</bold></white>"));
            lore.add(MessageUtil.parse("<gray>Cost     <white>" + NumberUtil.format(cost) + " Tokens"));
            lore.add(MessageUtil.parse("<gray>Yours    <green>" + NumberUtil.format(tokens)));
            lore.add(Component.empty());
            lore.add(MessageUtil.parse("<green><bold>\u25BA Click to buy " + affordable
                    + " level" + (affordable == 1 ? "" : "s")));
        }

        meta.lore(lore);
        meta.addItemFlags(ItemFlag.HIDE_ATTRIBUTES);
        item.setItemMeta(meta);
        return item;
    }

    // ──────────────────────────────────────────────────────────────────
    //  Back button
    // ──────────────────────────────────────────────────────────────────

    private ItemStack buildBackItem() {
        ItemStack item = new ItemStack(Material.BARRIER);
        ItemMeta  meta = item.getItemMeta();
        meta.displayName(MessageUtil.parse("<gray>\u25C4 Back"));
        meta.lore(List.of(MessageUtil.parse("<dark_gray>Return to upgrades")));
        meta.addItemFlags(ItemFlag.HIDE_ATTRIBUTES);
        item.setItemMeta(meta);
        return item;
    }

    // ──────────────────────────────────────────────────────────────────
    //  Helpers
    // ──────────────────────────────────────────────────────────────────

    /**
     * Returns the total cost for buying {@code count} levels starting from {@code fromLevel}.
     * Returns -1 if any individual level cost is invalid or would overflow.
     * Costs are capped at MAX_LEVEL_COST per level, so summing is always safe.
     */
    private long totalCost(int fromLevel, int count) {
        long total = 0;
        for (int i = 0; i < count; i++) {
            long c = nextCost(fromLevel + i);
            if (c < 0) return -1;
            total += c;
            // guard: summing MAX_LEVEL_COST * 1000 = 1e15, still fits in long,
            // but guard anyway in case someone passes a huge count
            if (total < 0) return -1;
        }
        return total;
    }

    private int computeAffordable(int current, int max, long tokens) {
        long budget    = tokens;
        int  remaining = max - current;
        for (int i = 0; i < remaining; i++) {
            long c = nextCost(current + i);
            if (c < 0 || budget < c) return i;
            budget -= c;
        }
        return remaining;
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

    private String upgradeLabel() {
        return switch (type) {
            case "crop"      -> "Crop Harvest Upgrade";
            case "xp"        -> "XP Boost Upgrade";
            case "keyfinder" -> "Key Finder Upgrade";
            case "panda"     -> "Panda Roller Upgrade";
            case "tnt"       -> "TNT Bomber Upgrade";
            default          -> "Token Boost Upgrade";
        };
    }

    private Material upgradeIcon() {
        return switch (type) {
            case "crop"      -> Material.WHEAT;
            case "xp"        -> Material.EXPERIENCE_BOTTLE;
            case "keyfinder" -> Material.TRIPWIRE_HOOK;
            case "panda"     -> Material.BAMBOO;
            case "tnt"       -> Material.TNT;
            default          -> Material.SUNFLOWER;
        };
    }

    private Material amountMaterialGreen(int amount) {
        return switch (amount) {
            case 1  -> Material.LIME_DYE;
            case 5  -> Material.LIME_CONCRETE;
            case 10 -> Material.GREEN_DYE;
            case 25 -> Material.EMERALD;
            case 50 -> Material.EMERALD_BLOCK;
            default -> Material.NETHER_STAR;
        };
    }

    private Material amountMaterialRed(int amount) {
        return switch (amount) {
            case 1  -> Material.RED_DYE;
            case 5  -> Material.RED_CONCRETE;
            case 10 -> Material.ORANGE_DYE;
            case 25 -> Material.QUARTZ;
            case 50 -> Material.QUARTZ_BLOCK;
            default -> Material.COAL;
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
