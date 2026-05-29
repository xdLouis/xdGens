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
 * Hoe Upgrade GUI  —  6 rows (54 slots)
 *
 * Slots 0-44  ──  upgrade grid (5 rows × 9 cols), filled left-to-right, top-to-bottom.
 *              Empty grid slots get a dark filler pane.
 * Slots 45-53 ──  fixed footer row:
 *   45 – purple border
 *   46 – Tokens + Money info
 *   47 – purple border
 *   48-52 – level overview (one item per upgrade type)
 *   53 – purple border
 *
 * Adding a new upgrade later: just add another buildXxxItem() call in registerUpgrades()
 * and register its slot in HoeUpgradeListener.  Everything else auto-shifts.
 */
public class HoeUpgradeGUI {

    public static final String GUI_TITLE = "⚡ Hoe Upgrades";

    /** Maximum slots available for upgrades (rows 0-4 = 45 slots). */
    public static final int UPGRADE_SLOTS = 45;

    // Slots for the currently registered upgrades — referenced by the listener.
    // When you add more upgrades, just append to this list and keep in sync
    // with registerUpgrades() below.
    public static final int SLOT_CROP  = 0;
    public static final int SLOT_XP    = 1;
    public static final int SLOT_TOKEN = 2;
    public static final int SLOT_HOE   = 3;

    // Footer slots (row 5)
    private static final int FOOTER_START = 45;

    private final Main plugin;

    public HoeUpgradeGUI(Main plugin) {
        this.plugin = plugin;
    }

    public void open(Player player) {
        Inventory inv = Bukkit.createInventory(
                null, 54,
                MessageUtil.parse("<gradient:#a18cd1:#fbc2eb><bold>⚡ Hoe Upgrades</bold></gradient>")
        );

        // 1. Place upgrade items left-to-right
        List<ItemStack> upgrades = buildUpgradeItems(player);
        for (int i = 0; i < upgrades.size() && i < UPGRADE_SLOTS; i++) {
            inv.setItem(i, upgrades.get(i));
        }

        // 2. Fill remaining upgrade slots with dark pane
        ItemStack dark = pane(Material.BLACK_STAINED_GLASS_PANE);
        for (int i = upgrades.size(); i < UPGRADE_SLOTS; i++) {
            inv.setItem(i, dark.clone());
        }

        // 3. Build fixed footer
        buildFooter(inv, player);

        player.openInventory(inv);
    }

    // ── upgrade registry ─────────────────────────────────────────────────
    /**
     * Add all upgrade items here in the order they should appear.
     * Index in this list == slot in the inventory.
     * To add a new upgrade later: append buildMyNewItem(player) here.
     */
    private List<ItemStack> buildUpgradeItems(Player player) {
        List<ItemStack> list = new ArrayList<>();
        list.add(buildCropItem(player));   // slot 0
        list.add(buildXpItem(player));     // slot 1
        list.add(buildTokenItem(player));  // slot 2
        list.add(buildHoeItem(player));    // slot 3
        // list.add(buildMyNewUpgrade(player));  ← just append here
        return list;
    }

    // ── upgrade item builders ────────────────────────────────────────────

    private ItemStack buildCropItem(Player player) {
        HoeUpgradeManager m = plugin.getHoeUpgradeManager();
        int lvl   = m.getCropLevel(player);
        int maxLv = HoeUpgradeManager.MAX_CROP_LEVEL;
        boolean maxed = lvl >= maxLv;
        long tokens   = plugin.getCurrencyManager().getTokens(player);
        long cost     = maxed ? 0 : m.getCropCost(lvl + 1);

        ItemStack item = new ItemStack(Material.WHEAT);
        ItemMeta  meta = item.getItemMeta();
        meta.displayName(MessageUtil.parse("<gradient:#f6d365:#fda085><bold>🌾 Crop Harvest</bold></gradient>"));
        meta.lore(buildUpgradeLore(lvl, maxLv, tokens, cost, maxed,
                "<gradient:#f6d365:#fda085>+" + lvl + " crops/break</gradient>"));
        applyGlow(meta, lvl > 0);
        meta.addItemFlags(ItemFlag.HIDE_ATTRIBUTES);
        item.setItemMeta(meta);
        return item;
    }

    private ItemStack buildXpItem(Player player) {
        HoeUpgradeManager m = plugin.getHoeUpgradeManager();
        int lvl   = m.getXpLevel(player);
        int maxLv = HoeUpgradeManager.MAX_XP_LEVEL;
        boolean maxed = lvl >= maxLv;
        long tokens   = plugin.getCurrencyManager().getTokens(player);
        long cost     = maxed ? 0 : m.getXpCost(lvl + 1);

        ItemStack item = new ItemStack(Material.EXPERIENCE_BOTTLE);
        ItemMeta  meta = item.getItemMeta();
        meta.displayName(MessageUtil.parse("<gradient:#7afcff:#00c2ff><bold>✨ XP Boost</bold></gradient>"));
        meta.lore(buildUpgradeLore(lvl, maxLv, tokens, cost, maxed,
                "<gradient:#7afcff:#00c2ff>+" + NumberUtil.format(m.getXpPercentBonus(player)) + "% XP</gradient>"));
        applyGlow(meta, lvl > 0);
        meta.addItemFlags(ItemFlag.HIDE_ATTRIBUTES);
        item.setItemMeta(meta);
        return item;
    }

    private ItemStack buildTokenItem(Player player) {
        HoeUpgradeManager m = plugin.getHoeUpgradeManager();
        int lvl   = m.getTokenLevel(player);
        int maxLv = HoeUpgradeManager.MAX_TOKEN_LEVEL;
        boolean maxed = lvl >= maxLv;
        long tokens   = plugin.getCurrencyManager().getTokens(player);
        long cost     = maxed ? 0 : m.getTokenCost(lvl + 1);

        ItemStack item = new ItemStack(Material.GOLD_INGOT);
        ItemMeta  meta = item.getItemMeta();
        meta.displayName(MessageUtil.parse("<gradient:#ffd86f:#fc6262><bold>💰 Token Boost</bold></gradient>"));
        meta.lore(buildUpgradeLore(lvl, maxLv, tokens, cost, maxed,
                "<gradient:#ffd86f:#fc6262>+" + NumberUtil.format(m.getTokenPercentBonus(player)) + "% Tokens</gradient>"));
        applyGlow(meta, lvl > 0);
        meta.addItemFlags(ItemFlag.HIDE_ATTRIBUTES);
        item.setItemMeta(meta);
        return item;
    }

    private ItemStack buildHoeItem(Player player) {
        HoeUpgradeManager m = plugin.getHoeUpgradeManager();
        int lvl   = m.getHoeLevel(player);
        int maxLv = HoeUpgradeManager.MAX_HOE_LEVEL;
        boolean maxed = lvl >= maxLv;
        double money  = plugin.getCurrencyManager().getMoney(player);
        long cost     = maxed ? 0 : m.getHoeCost(lvl + 1);

        ItemStack item = new ItemStack(m.getHoeMaterial(player));
        ItemMeta  meta = item.getItemMeta();
        meta.displayName(MessageUtil.parse("<gradient:#c0c0c0:#ffffff><bold>⚒ Hoe Material</bold></gradient>"));

        List<Component> lore = new ArrayList<>();
        lore.add(MessageUtil.parse("<dark_gray>─────────────────────"));
        lore.add(MessageUtil.parse("<gray>Material <dark_gray>│ <white>" + m.getHoeMaterialName(player)));
        lore.add(MessageUtil.parse("<gray>Stage    <dark_gray>│ <white>" + m.getHoeStageInMaterial(player) + "<dark_gray>/3"));
        lore.add(MessageUtil.parse("<gray>Speed    <dark_gray>│ <aqua>+" + String.format("%.0f", (m.getWalkSpeed(player) - 0.2f) / (0.7f - 0.2f) * 100) + "% walk speed</aqua>"));
        lore.add(MessageUtil.parse("<gray>XP bonus <dark_gray>│ <green>+" + String.format("%.0f", m.getHoeXpPercentBonus(player)) + "%</green>"));
        lore.add(MessageUtil.parse("<gray>Level    <dark_gray>│ <white>" + lvl + "<dark_gray>/" + maxLv));
        lore.add(MessageUtil.parse("<gray>         └ " + levelBar(lvl, maxLv, 10)));
        lore.add(MessageUtil.parse("<dark_gray>─────────────────────"));
        if (maxed) {
            lore.add(MessageUtil.parse("<gradient:#c0c0c0:#ffffff><bold>✦ MAXED OUT ✦</bold></gradient>"));
        } else {
            lore.add(MessageUtil.parse("<gray>Next cost <dark_gray>│ " + costTagMoney(money, cost) + " $"));
            lore.add(Component.empty());
            lore.add(money >= cost
                    ? MessageUtil.parse("<green>▶ Click to upgrade")
                    : MessageUtil.parse("<red>✘ Not enough money"));
        }
        applyGlow(meta, lvl > 1);
        meta.addItemFlags(ItemFlag.HIDE_ATTRIBUTES);
        meta.lore(lore);
        item.setItemMeta(meta);
        return item;
    }

    // ── shared lore builder for token-based upgrades ────────────────────

    private List<Component> buildUpgradeLore(int lvl, int maxLv, long tokens, long cost,
                                              boolean maxed, String bonusTag) {
        List<Component> lore = new ArrayList<>();
        lore.add(MessageUtil.parse("<dark_gray>─────────────────────"));
        lore.add(MessageUtil.parse("<gray>Bonus <dark_gray>│ " + bonusTag));
        lore.add(MessageUtil.parse("<gray>Level <dark_gray>│ <white>" + lvl + "<dark_gray>/" + maxLv));
        lore.add(MessageUtil.parse("<gray>      └ " + levelBar(lvl, maxLv, 10)));
        lore.add(MessageUtil.parse("<dark_gray>─────────────────────"));
        if (maxed) {
            lore.add(MessageUtil.parse("<green><bold>✦ MAXED OUT ✦</bold></green>"));
        } else {
            lore.add(MessageUtil.parse("<gray>Next cost <dark_gray>│ " + costTag(tokens, cost) + " Tokens"));
            lore.add(Component.empty());
            lore.add(tokens >= cost
                    ? MessageUtil.parse("<green>▶ Click to choose amount")
                    : MessageUtil.parse("<red>✘ Not enough Tokens"));
        }
        return lore;
    }

    // ── footer ──────────────────────────────────────────────────────

    /**
     * Fills row 5 (slots 45-53) with a fixed status bar:
     *   45: purple pane
     *   46: wallet (Tokens + Money)
     *   47: purple pane
     *   48-52: compact level overview (one item per active upgrade)
     *   53: purple pane
     */
    private void buildFooter(Inventory inv, Player player) {
        ItemStack border = pane(Material.PURPLE_STAINED_GLASS_PANE);
        inv.setItem(45, border.clone());
        inv.setItem(47, border.clone());
        inv.setItem(53, border.clone());

        inv.setItem(46, buildWalletItem(player));
        inv.setItem(48, buildFooterLevel(player, "crop"));
        inv.setItem(49, buildFooterLevel(player, "xp"));
        inv.setItem(50, buildFooterLevel(player, "token"));
        inv.setItem(51, buildFooterLevel(player, "hoe"));
        // slot 52: empty (reserve for future footer items)
        inv.setItem(52, pane(Material.BLACK_STAINED_GLASS_PANE));
    }

    private ItemStack buildWalletItem(Player player) {
        long   tokens = plugin.getCurrencyManager().getTokens(player);
        double money  = plugin.getCurrencyManager().getMoney(player);
        ItemStack item = new ItemStack(Material.BOOK);
        ItemMeta  meta = item.getItemMeta();
        meta.displayName(MessageUtil.parse("<gradient:#a18cd1:#fbc2eb><bold>Wallet</bold></gradient>"));
        List<Component> lore = new ArrayList<>();
        lore.add(Component.empty());
        lore.add(MessageUtil.parse("<gray>Tokens <dark_gray>│ <gold>" + NumberUtil.format(tokens) + "</gold>"));
        lore.add(MessageUtil.parse("<gray>Money  <dark_gray>│ <green>$" + NumberUtil.format(money) + "</green>"));
        meta.lore(lore);
        meta.addItemFlags(ItemFlag.HIDE_ATTRIBUTES);
        item.setItemMeta(meta);
        return item;
    }

    private ItemStack buildFooterLevel(Player player, String type) {
        HoeUpgradeManager m = plugin.getHoeUpgradeManager();
        int lvl, max;
        String grad, label;
        Material mat;
        switch (type) {
            case "crop"  -> { lvl = m.getCropLevel(player);  max = HoeUpgradeManager.MAX_CROP_LEVEL;  grad = "<gradient:#f6d365:#fda085>"; label = "🌾 Crop";  mat = Material.WHEAT; }
            case "xp"    -> { lvl = m.getXpLevel(player);    max = HoeUpgradeManager.MAX_XP_LEVEL;    grad = "<gradient:#7afcff:#00c2ff>"; label = "✨ XP";    mat = Material.EXPERIENCE_BOTTLE; }
            case "token" -> { lvl = m.getTokenLevel(player); max = HoeUpgradeManager.MAX_TOKEN_LEVEL; grad = "<gradient:#ffd86f:#fc6262>"; label = "💰 Token"; mat = Material.GOLD_NUGGET; }
            default      -> { lvl = m.getHoeLevel(player);   max = HoeUpgradeManager.MAX_HOE_LEVEL;   grad = "<gradient:#c0c0c0:#ffffff>"; label = "⚒ Hoe";   mat = m.getHoeMaterial(player); }
        }
        ItemStack item = new ItemStack(mat);
        ItemMeta  meta = item.getItemMeta();
        meta.displayName(MessageUtil.parse(grad + "<bold>" + label + "</bold></gradient>"));
        meta.lore(List.of(
                MessageUtil.parse("<gray>Lv <white>" + lvl + "</white><dark_gray>/" + max),
                MessageUtil.parse(levelBar(lvl, max, 7))
        ));
        meta.addItemFlags(ItemFlag.HIDE_ATTRIBUTES);
        item.setItemMeta(meta);
        return item;
    }

    // ── helpers ────────────────────────────────────────────────────────

    private String levelBar(int current, int max, int segments) {
        int filled = max > 0 ? (int) Math.round((double) current / max * segments) : 0;
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < segments; i++)
            sb.append(i < filled ? "<green>█" : "<dark_gray>░");
        return sb.toString();
    }

    private String costTag(long balance, long cost) {
        return (balance >= cost ? "<green>" : "<red>") + NumberUtil.format(cost) + (balance >= cost ? "</green>" : "</red>");
    }
    private String costTagMoney(double balance, long cost) {
        return (balance >= cost ? "<green>" : "<red>") + NumberUtil.format(cost) + (balance >= cost ? "</green>" : "</red>");
    }

    private void applyGlow(ItemMeta meta, boolean glow) {
        if (!glow) return;
        meta.addEnchant(Enchantment.UNBREAKING, 1, true);
        meta.addItemFlags(ItemFlag.HIDE_ENCHANTS);
    }

    private ItemStack pane(Material mat) {
        ItemStack p = new ItemStack(mat);
        ItemMeta  m = p.getItemMeta();
        m.displayName(Component.empty());
        p.setItemMeta(m);
        return p;
    }
}
