package de.louis.xdGens.gui;

import de.louis.xdGens.main.Main;
import de.louis.xdGens.manager.HoeUpgradeManager;
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
 * Sub-menu that opens when a player clicks one of the three upgrade types.
 *
 * Layout (3 rows = 27 slots):
 *  Row 0: all filler
 *  Row 1: [filler] [+1] [+10] [+25] [+50] [MAX] [filler] [BACK] [filler]
 *  Row 2: all filler
 *
 * Type key: "crop" | "xp" | "token"
 */
public class HoeUpgradeAmountGUI {

    public static final String TITLE_PREFIX = "⚡ Upgrade – ";

    // Slot positions in the 27-slot inventory
    public static final int SLOT_PLUS1  = 10;
    public static final int SLOT_PLUS10 = 11;
    public static final int SLOT_PLUS25 = 12;
    public static final int SLOT_PLUS50 = 13;
    public static final int SLOT_MAX    = 14;
    public static final int SLOT_BACK   = 16;

    /** Amounts mapped to their slots in order. */
    public static final int[] AMOUNTS = {1, 10, 25, 50};

    private final Main   plugin;
    private final String type;   // "crop", "xp", "token"
    private final String label;  // display name
    private final String gradient;
    private final Material icon;

    public HoeUpgradeAmountGUI(Main plugin, String type) {
        this.plugin = plugin;
        this.type   = type;
        switch (type) {
            case "crop"  -> { label = "🌾 Crop Harvest";  gradient = "<gradient:#f6d365:#fda085>"; icon = Material.WHEAT;            }
            case "xp"    -> { label = "✨ XP Boost";      gradient = "<gradient:#7afcff:#00c2ff>"; icon = Material.EXPERIENCE_BOTTLE; }
            default      -> { label = "💰 Token Boost";   gradient = "<gradient:#ffd86f:#fc6262>"; icon = Material.GOLD_INGOT;        }
        }
    }

    public void open(Player player) {
        String titleTag = gradient + "<bold>" + TITLE_PREFIX + label + "</bold></gradient>";
        Inventory inv = Bukkit.createInventory(null, 27, MessageUtil.parse(titleTag));

        fillBackground(inv);

        inv.setItem(SLOT_PLUS1,  buildAmountButton(player,  1));
        inv.setItem(SLOT_PLUS10, buildAmountButton(player, 10));
        inv.setItem(SLOT_PLUS25, buildAmountButton(player, 25));
        inv.setItem(SLOT_PLUS50, buildAmountButton(player, 50));
        inv.setItem(SLOT_MAX,    buildMaxButton(player));
        inv.setItem(SLOT_BACK,   buildBackButton());

        player.openInventory(inv);
    }

    // ─────────────────────────────────────────────────────────────────────

    private ItemStack buildAmountButton(Player player, int amount) {
        int current  = getCurrentLevel(player);
        int maxLevel = getMaxLevel();
        int canBuy   = howManyCanAfford(player, current, maxLevel);
        int actualAmount = Math.min(amount, maxLevel - current);

        boolean affordable  = canBuy >= amount && actualAmount > 0;
        boolean possible    = actualAmount > 0;

        // Total cost for `amount` levels
        long totalCost = totalCostFor(current, amount, maxLevel);

        ItemStack item = new ItemStack(icon);
        ItemMeta  meta = item.getItemMeta();
        meta.displayName(MessageUtil.parse(
                gradient + "<bold>+" + amount + "</bold></gradient>"));

        List<Component> lore = new ArrayList<>();
        lore.add(MessageUtil.parse("<gray>Current Level: <white>" + current + "</white>"));

        if (!possible) {
            lore.add(MessageUtil.parse("<red>Not possible – already at or near max."));
        } else {
            lore.add(MessageUtil.parse("<gray>Upgrades: <white>" + current + " → " + (current + actualAmount) + "</white>"));
            if (amount != actualAmount)
                lore.add(MessageUtil.parse("<dark_gray>(Only " + actualAmount + " levels until max)"));
            lore.add(MessageUtil.parse("<gray>Total Cost: "
                    + (affordable ? "<green>" : "<red>")
                    + NumberUtil.format(totalCost) + " Tokens"
                    + (affordable ? "</green>" : "</red>")));
            lore.add(MessageUtil.parse(affordable
                    ? "<green>Click to upgrade."
                    : "<red>You cannot afford this."));
        }

        meta.lore(lore);
        if (affordable) {
            meta.addEnchant(Enchantment.UNBREAKING, 1, true);
            meta.addItemFlags(ItemFlag.HIDE_ENCHANTS);
        }
        meta.addItemFlags(ItemFlag.HIDE_ATTRIBUTES);
        item.setItemMeta(meta);
        return item;
    }

    private ItemStack buildMaxButton(Player player) {
        int current  = getCurrentLevel(player);
        int maxLevel = getMaxLevel();
        int canBuy   = howManyCanAfford(player, current, maxLevel);
        int levelsLeft = maxLevel - current;

        ItemStack item = new ItemStack(Material.NETHER_STAR);
        ItemMeta  meta = item.getItemMeta();
        meta.displayName(MessageUtil.parse(gradient + "<bold>MAX</bold></gradient>"));

        List<Component> lore = new ArrayList<>();
        if (levelsLeft <= 0) {
            lore.add(MessageUtil.parse("<gold><bold>Already maxed out!</bold></gold>"));
        } else {
            long total = totalCostFor(current, levelsLeft, maxLevel);
            boolean affordable = canBuy >= levelsLeft;
            lore.add(MessageUtil.parse("<gray>Current: <white>" + current + "</white> → Max: <white>" + maxLevel + "</white>"));
            lore.add(MessageUtil.parse("<gray>Levels to buy: <white>" + levelsLeft + "</white>"));
            lore.add(MessageUtil.parse("<gray>Total Cost: "
                    + (affordable ? "<green>" : "<red>")
                    + NumberUtil.format(total) + " Tokens"
                    + (affordable ? "</green>" : "</red>")));
            if (!affordable) {
                int affordLevels = Math.max(0, canBuy);
                lore.add(MessageUtil.parse("<yellow>You can afford " + affordLevels + " levels."));
            }
            lore.add(MessageUtil.parse(affordable
                    ? "<green>Click to max out!"
                    : "<red>Not enough Tokens for full max."));
        }
        meta.lore(lore);
        if (levelsLeft > 0) {
            meta.addEnchant(Enchantment.UNBREAKING, 1, true);
            meta.addItemFlags(ItemFlag.HIDE_ENCHANTS);
        }
        meta.addItemFlags(ItemFlag.HIDE_ATTRIBUTES);
        item.setItemMeta(meta);
        return item;
    }

    private ItemStack buildBackButton() {
        ItemStack item = new ItemStack(Material.ARROW);
        ItemMeta  meta = item.getItemMeta();
        meta.displayName(MessageUtil.parse("<gray><bold>← Back</bold></gray>"));
        meta.lore(List.of(MessageUtil.parse("<dark_gray>Return to all upgrades.")));
        meta.addItemFlags(ItemFlag.HIDE_ATTRIBUTES);
        item.setItemMeta(meta);
        return item;
    }

    // ─────────────────────────────────────────────────────────────────────

    public String getType() { return type; }

    /** How many levels can the player afford from currentLevel onward. */
    private int howManyCanAfford(Player player, int current, int max) {
        long budget = plugin.getCurrencyManager().getTokens(player);
        int count   = 0;
        HoeUpgradeManager mgr = plugin.getHoeUpgradeManager();
        for (int lvl = current + 1; lvl <= max && budget >= 0; lvl++) {
            long cost = getCost(mgr, lvl);
            if (budget < cost) break;
            budget -= cost;
            count++;
        }
        return count;
    }

    /** Sum of costs for `amount` levels starting at (current + 1). Caps at max. */
    private long totalCostFor(int current, int amount, int max) {
        HoeUpgradeManager mgr = plugin.getHoeUpgradeManager();
        long total = 0;
        for (int i = 1; i <= amount && (current + i) <= max; i++) {
            total += getCost(mgr, current + i);
        }
        return total;
    }

    private int getCurrentLevel(Player player) {
        return switch (type) {
            case "crop"  -> plugin.getHoeUpgradeManager().getCropLevel(player);
            case "xp"    -> plugin.getHoeUpgradeManager().getXpLevel(player);
            default      -> plugin.getHoeUpgradeManager().getTokenLevel(player);
        };
    }

    private int getMaxLevel() {
        return switch (type) {
            case "crop"  -> HoeUpgradeManager.MAX_CROP_LEVEL;
            case "xp"    -> HoeUpgradeManager.MAX_XP_LEVEL;
            default      -> HoeUpgradeManager.MAX_TOKEN_LEVEL;
        };
    }

    private long getCost(HoeUpgradeManager mgr, int targetLevel) {
        return switch (type) {
            case "crop"  -> mgr.getCropCost(targetLevel);
            case "xp"    -> mgr.getXpCost(targetLevel);
            default      -> mgr.getTokenCost(targetLevel);
        };
    }

    private void fillBackground(Inventory inv) {
        ItemStack pane = new ItemStack(Material.GRAY_STAINED_GLASS_PANE);
        ItemMeta  meta = pane.getItemMeta();
        meta.displayName(Component.empty());
        pane.setItemMeta(meta);
        for (int i = 0; i < inv.getSize(); i++) inv.setItem(i, pane.clone());
    }
}
