package de.louis.xdGens.gui;

import de.louis.xdGens.main.Main;
import de.louis.xdGens.manager.HoeUpgradeManager;
import de.louis.xdGens.skill.PandaRollerSkill;
import de.louis.xdGens.skill.SkillRegistry;
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

public class HoeUpgradeGUI {

    public static final String GUI_TITLE     = "⚡ Hoe Upgrades";
    public static final int    UPGRADE_SLOTS = 45;

    public static final int SLOT_CROP       = 0;
    public static final int SLOT_XP         = 1;
    public static final int SLOT_TOKEN      = 2;
    public static final int SLOT_HOE        = 3;
    public static final int SLOT_KEY_FINDER = 4;
    public static final int SLOT_PANDA      = 5;

    private final Main plugin;

    public HoeUpgradeGUI(Main plugin) {
        this.plugin = plugin;
    }

    public void open(Player player) {
        Inventory inv = Bukkit.createInventory(
                null, 54,
                MessageUtil.parse("<gradient:#a18cd1:#fbc2eb><bold>⚡ Hoe Upgrades</bold></gradient>")
        );

        List<ItemStack> upgrades = buildUpgradeItems(player);
        for (int i = 0; i < upgrades.size() && i < UPGRADE_SLOTS; i++) {
            inv.setItem(i, upgrades.get(i));
        }

        ItemStack dark = pane(Material.BLACK_STAINED_GLASS_PANE);
        for (int i = upgrades.size(); i < UPGRADE_SLOTS; i++) {
            inv.setItem(i, dark.clone());
        }

        buildFooter(inv, player);
        player.openInventory(inv);
    }

    private List<ItemStack> buildUpgradeItems(Player player) {
        List<ItemStack> list = new ArrayList<>();
        list.add(buildCropItem(player));      // slot 0
        list.add(buildXpItem(player));        // slot 1
        list.add(buildTokenItem(player));     // slot 2
        list.add(buildHoeItem(player));       // slot 3
        list.add(buildKeyFinderItem(player)); // slot 4
        list.add(buildPandaItem(player));     // slot 5
        return list;
    }

    // ── upgrade item builders ───────────────────────────────────────────

    private ItemStack buildCropItem(Player player) {
        HoeUpgradeManager m = plugin.getHoeUpgradeManager();
        int     lvl   = m.getCropLevel(player);
        int     maxLv = HoeUpgradeManager.MAX_CROP_LEVEL;
        boolean maxed = lvl >= maxLv;
        long    tokens = plugin.getCurrencyManager().getTokens(player);
        long    cost   = maxed ? 0L : m.getCropCost(lvl + 1);
        ItemStack item = new ItemStack(Material.WHEAT);
        ItemMeta  meta = item.getItemMeta();
        meta.displayName(MessageUtil.parse("<gradient:#f6d365:#fda085><bold>\uD83C\uDF3E Crop Harvest</bold></gradient>"));
        meta.lore(buildUpgradeLore(lvl, maxLv, tokens, cost, maxed,
                "<gradient:#f6d365:#fda085>+" + lvl + " crops/break</gradient>"));
        applyGlow(meta, lvl > 0);
        meta.addItemFlags(ItemFlag.HIDE_ATTRIBUTES);
        item.setItemMeta(meta);
        return item;
    }

    private ItemStack buildXpItem(Player player) {
        HoeUpgradeManager m = plugin.getHoeUpgradeManager();
        int     lvl   = m.getXpLevel(player);
        int     maxLv = HoeUpgradeManager.MAX_XP_LEVEL;
        boolean maxed = lvl >= maxLv;
        long    tokens = plugin.getCurrencyManager().getTokens(player);
        long    cost   = maxed ? 0L : m.getXpCost(lvl + 1);
        ItemStack item = new ItemStack(Material.EXPERIENCE_BOTTLE);
        ItemMeta  meta = item.getItemMeta();
        meta.displayName(MessageUtil.parse("<gradient:#7afcff:#00c2ff><bold>\u2728 XP Boost</bold></gradient>"));
        meta.lore(buildUpgradeLore(lvl, maxLv, tokens, cost, maxed,
                "<gradient:#7afcff:#00c2ff>+" + NumberUtil.format(m.getXpPercentBonus(player)) + "% XP</gradient>"));
        applyGlow(meta, lvl > 0);
        meta.addItemFlags(ItemFlag.HIDE_ATTRIBUTES);
        item.setItemMeta(meta);
        return item;
    }

    private ItemStack buildTokenItem(Player player) {
        HoeUpgradeManager m = plugin.getHoeUpgradeManager();
        int     lvl   = m.getTokenLevel(player);
        int     maxLv = HoeUpgradeManager.MAX_TOKEN_LEVEL;
        boolean maxed = lvl >= maxLv;
        long    tokens = plugin.getCurrencyManager().getTokens(player);
        long    cost   = maxed ? 0L : m.getTokenCost(lvl + 1);
        ItemStack item = new ItemStack(Material.GOLD_INGOT);
        ItemMeta  meta = item.getItemMeta();
        meta.displayName(MessageUtil.parse("<gradient:#ffd86f:#fc6262><bold>\uD83D\uDCB0 Token Boost</bold></gradient>"));
        meta.lore(buildUpgradeLore(lvl, maxLv, tokens, cost, maxed,
                "<gradient:#ffd86f:#fc6262>+" + NumberUtil.format(m.getTokenPercentBonus(player)) + "% Tokens</gradient>"));
        applyGlow(meta, lvl > 0);
        meta.addItemFlags(ItemFlag.HIDE_ATTRIBUTES);
        item.setItemMeta(meta);
        return item;
    }

    private ItemStack buildHoeItem(Player player) {
        HoeUpgradeManager m = plugin.getHoeUpgradeManager();
        int     lvl   = m.getHoeLevel(player);
        int     maxLv = HoeUpgradeManager.MAX_HOE_LEVEL;
        boolean maxed = lvl >= maxLv;
        double  money = plugin.getCurrencyManager().getMoney(player);
        long    cost  = maxed ? 0L : m.getHoeCost(lvl + 1);
        ItemStack item = new ItemStack(m.getHoeMaterial(player));
        ItemMeta  meta = item.getItemMeta();
        meta.displayName(MessageUtil.parse("<gradient:#c0c0c0:#ffffff><bold>\u2692 Hoe Material</bold></gradient>"));
        List<Component> lore = new ArrayList<>();
        lore.add(MessageUtil.parse("<dark_gray>\u2500\u2500\u2500\u2500\u2500\u2500\u2500\u2500\u2500\u2500\u2500\u2500\u2500\u2500\u2500\u2500\u2500\u2500\u2500\u2500\u2500"));
        lore.add(MessageUtil.parse("<gray>Material <dark_gray>\u2502 <white>" + m.getHoeMaterialName(player)));
        lore.add(MessageUtil.parse("<gray>Stage    <dark_gray>\u2502 <white>" + m.getHoeStageInMaterial(player) + "<dark_gray>/3"));
        lore.add(MessageUtil.parse("<gray>Speed    <dark_gray>\u2502 <aqua>+" + String.format("%.0f", (m.getWalkSpeed(player) - 0.2f) / (0.7f - 0.2f) * 100) + "% walk speed</aqua>"));
        lore.add(MessageUtil.parse("<gray>XP bonus <dark_gray>\u2502 <green>+" + String.format("%.0f", m.getHoeXpPercentBonus(player)) + "%</green>"));
        lore.add(MessageUtil.parse("<gray>Level    <dark_gray>\u2502 <white>" + lvl + "<dark_gray>/" + maxLv));
        lore.add(MessageUtil.parse("<gray>         \u2514 " + levelBar(lvl, maxLv, 10)));
        lore.add(MessageUtil.parse("<dark_gray>\u2500\u2500\u2500\u2500\u2500\u2500\u2500\u2500\u2500\u2500\u2500\u2500\u2500\u2500\u2500\u2500\u2500\u2500\u2500\u2500\u2500"));
        if (maxed) {
            lore.add(MessageUtil.parse("<gradient:#c0c0c0:#ffffff><bold>\u2726 MAXED OUT \u2726</bold></gradient>"));
        } else {
            lore.add(MessageUtil.parse("<gray>Next cost <dark_gray>\u2502 " + costTagMoney(money, cost) + " $"));
            lore.add(Component.empty());
            lore.add(money >= cost
                    ? MessageUtil.parse("<green>\u25b6 Click to upgrade")
                    : MessageUtil.parse("<red>\u2718 Not enough money"));
        }
        applyGlow(meta, lvl > 1);
        meta.addItemFlags(ItemFlag.HIDE_ATTRIBUTES);
        meta.lore(lore);
        item.setItemMeta(meta);
        return item;
    }

    private ItemStack buildKeyFinderItem(Player player) {
        HoeUpgradeManager m = plugin.getHoeUpgradeManager();
        int     lvl   = m.getKeyFinderLevel(player);
        int     maxLv = HoeUpgradeManager.MAX_KEY_FINDER_LEVEL;
        boolean maxed = lvl >= maxLv;
        long    tokens = plugin.getCurrencyManager().getTokens(player);
        long    cost   = maxed ? 0L : m.getKeyFinderCost(lvl + 1);
        double  chance = m.getKeyFinderChance(player) * 100.0;

        ItemStack item = new ItemStack(Material.TRIPWIRE_HOOK);
        ItemMeta  meta = item.getItemMeta();
        meta.displayName(MessageUtil.parse("<gradient:#a18cd1:#fbc2eb><bold>\uD83D\uDD11 Key Finder</bold></gradient>"));
        List<Component> lore = new ArrayList<>();
        lore.add(MessageUtil.parse("<dark_gray>\u2500\u2500\u2500\u2500\u2500\u2500\u2500\u2500\u2500\u2500\u2500\u2500\u2500\u2500\u2500\u2500\u2500\u2500\u2500\u2500\u2500"));
        lore.add(MessageUtil.parse("<gray>Chance <dark_gray>\u2502 <gradient:#a18cd1:#fbc2eb>" + String.format("%.4f", chance) + "% per wheat break</gradient>"));
        lore.add(MessageUtil.parse("<gray>Level  <dark_gray>\u2502 <white>" + lvl + "<dark_gray>/" + maxLv));
        lore.add(MessageUtil.parse("<gray>       \u2514 " + levelBar(lvl, maxLv, 10)));
        lore.add(MessageUtil.parse("<dark_gray>\u2500\u2500\u2500\u2500\u2500\u2500\u2500\u2500\u2500\u2500\u2500\u2500\u2500\u2500\u2500\u2500\u2500\u2500\u2500\u2500\u2500"));
        lore.add(MessageUtil.parse("<dark_gray>Drops keys for /crates."));
        lore.add(MessageUtil.parse("<dark_gray>Even at lv 1000: still rare!"));
        lore.add(MessageUtil.parse("<dark_gray>\u2500\u2500\u2500\u2500\u2500\u2500\u2500\u2500\u2500\u2500\u2500\u2500\u2500\u2500\u2500\u2500\u2500\u2500\u2500\u2500\u2500"));
        if (maxed) {
            lore.add(MessageUtil.parse("<gradient:#a18cd1:#fbc2eb><bold>\u2726 MAXED OUT \u2726</bold></gradient>"));
        } else {
            lore.add(MessageUtil.parse("<gray>Next cost <dark_gray>\u2502 " + costTag(tokens, cost) + " Tokens"));
            lore.add(Component.empty());
            lore.add(tokens >= cost
                    ? MessageUtil.parse("<green>\u25b6 Click to choose amount")
                    : MessageUtil.parse("<red>\u2718 Not enough Tokens"));
        }
        applyGlow(meta, lvl > 0);
        meta.addItemFlags(ItemFlag.HIDE_ATTRIBUTES);
        meta.lore(lore);
        item.setItemMeta(meta);
        return item;
    }

    private ItemStack buildPandaItem(Player player) {
        HoeUpgradeManager m      = plugin.getHoeUpgradeManager();
        int     lvl              = m.getPandaLevel(player);
        int     maxLv            = HoeUpgradeManager.MAX_PANDA_LEVEL;
        boolean maxed            = lvl >= maxLv;
        boolean locked           = !m.canUnlockPanda(player);
        long    tokens           = plugin.getCurrencyManager().getTokens(player);
        long    cost             = maxed ? 0L : m.getPandaCost(lvl + 1);  // long, not int!
        PandaRollerSkill skill   = (PandaRollerSkill) SkillRegistry.get("panda_roller");

        Material  mat  = locked ? Material.BARRIER : Material.BAMBOO;
        ItemStack item = new ItemStack(mat);
        ItemMeta  meta = item.getItemMeta();
        meta.displayName(MessageUtil.parse("<gradient:#a8e6cf:#88d8b0><bold>\uD83D\uDC3C Panda Roller</bold></gradient>"));

        List<Component> lore = new ArrayList<>();
        lore.add(MessageUtil.parse("<dark_gray>\u2500\u2500\u2500\u2500\u2500\u2500\u2500\u2500\u2500\u2500\u2500\u2500\u2500\u2500\u2500\u2500\u2500\u2500\u2500\u2500\u2500"));

        if (locked) {
            int prestige = plugin.getProgressionManager().getPrestige(player);
            lore.add(MessageUtil.parse("<red>\uD83D\uDD12 Locked!</red>"));
            lore.add(MessageUtil.parse("<gray>Requires <white>Prestige "
                    + HoeUpgradeManager.PANDA_REQUIRED_PRESTIGE + "</white> to unlock."));
            lore.add(MessageUtil.parse("<gray>Your prestige: <white>" + prestige + "</white>"));
            lore.add(MessageUtil.parse("<dark_gray>\u2500\u2500\u2500\u2500\u2500\u2500\u2500\u2500\u2500\u2500\u2500\u2500\u2500\u2500\u2500\u2500\u2500\u2500\u2500\u2500\u2500"));
            lore.add(MessageUtil.parse("<dark_gray>A panda wanders your field,"));
            lore.add(MessageUtil.parse("<dark_gray>granting bonus XP + Tokens."));
        } else {
            if (lvl > 0) {
                lore.add(MessageUtil.parse("<gray>Spawn chance <dark_gray>\u2502 <green>"
                        + skill.spawnChancePct(lvl) + "% per harvest</green>"));
                lore.add(MessageUtil.parse("<gray>Reward bonus <dark_gray>\u2502 <green>"
                        + skill.rewardBonusPct(lvl) + "% of session harvest</green>"));
            } else {
                lore.add(MessageUtil.parse("<gray>Not yet unlocked."));
            }
            lore.add(MessageUtil.parse("<gray>Level        <dark_gray>\u2502 <white>"
                    + lvl + "<dark_gray>/" + maxLv));
            lore.add(MessageUtil.parse("<gray>             \u2514 " + levelBar(lvl, maxLv, 10)));
            lore.add(MessageUtil.parse("<dark_gray>\u2500\u2500\u2500\u2500\u2500\u2500\u2500\u2500\u2500\u2500\u2500\u2500\u2500\u2500\u2500\u2500\u2500\u2500\u2500\u2500\u2500"));
            lore.add(MessageUtil.parse("<dark_gray>Reward scales with prestige."));
            lore.add(MessageUtil.parse("<dark_gray>\u2500\u2500\u2500\u2500\u2500\u2500\u2500\u2500\u2500\u2500\u2500\u2500\u2500\u2500\u2500\u2500\u2500\u2500\u2500\u2500\u2500"));

            if (maxed) {
                lore.add(MessageUtil.parse("<gradient:#a8e6cf:#88d8b0><bold>\u2726 MAXED OUT \u2726</bold></gradient>"));
            } else {
                int nextLv = lvl + 1;
                lore.add(MessageUtil.parse("<gray>Next: <green>"
                        + skill.spawnChancePct(nextLv) + "% spawn \u00b7 "
                        + skill.rewardBonusPct(nextLv) + "% bonus</green>"));
                lore.add(MessageUtil.parse("<gray>Cost <dark_gray>\u2502 " + costTag(tokens, cost) + " Tokens"));
                lore.add(Component.empty());
                lore.add(tokens >= cost
                        ? MessageUtil.parse("<green>\u25b6 Click to " + (lvl == 0 ? "unlock" : "upgrade"))
                        : MessageUtil.parse("<red>\u2718 Not enough Tokens"));
            }
        }

        applyGlow(meta, lvl > 0);
        meta.addItemFlags(ItemFlag.HIDE_ATTRIBUTES);
        meta.lore(lore);
        item.setItemMeta(meta);
        return item;
    }

    // ── shared lore builder ──────────────────────────────────────────────

    private List<Component> buildUpgradeLore(int lvl, int maxLv, long tokens, long cost,
                                              boolean maxed, String bonusTag) {
        List<Component> lore = new ArrayList<>();
        lore.add(MessageUtil.parse("<dark_gray>\u2500\u2500\u2500\u2500\u2500\u2500\u2500\u2500\u2500\u2500\u2500\u2500\u2500\u2500\u2500\u2500\u2500\u2500\u2500\u2500\u2500"));
        lore.add(MessageUtil.parse("<gray>Bonus <dark_gray>\u2502 " + bonusTag));
        lore.add(MessageUtil.parse("<gray>Level <dark_gray>\u2502 <white>" + lvl + "<dark_gray>/" + maxLv));
        lore.add(MessageUtil.parse("<gray>      \u2514 " + levelBar(lvl, maxLv, 10)));
        lore.add(MessageUtil.parse("<dark_gray>\u2500\u2500\u2500\u2500\u2500\u2500\u2500\u2500\u2500\u2500\u2500\u2500\u2500\u2500\u2500\u2500\u2500\u2500\u2500\u2500\u2500"));
        if (maxed) {
            lore.add(MessageUtil.parse("<green><bold>\u2726 MAXED OUT \u2726</bold></green>"));
        } else {
            lore.add(MessageUtil.parse("<gray>Next cost <dark_gray>\u2502 " + costTag(tokens, cost) + " Tokens"));
            lore.add(Component.empty());
            lore.add(tokens >= cost
                    ? MessageUtil.parse("<green>\u25b6 Click to choose amount")
                    : MessageUtil.parse("<red>\u2718 Not enough Tokens"));
        }
        return lore;
    }

    // ── footer ───────────────────────────────────────────────────────────

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
        inv.setItem(52, buildFooterLevel(player, "keyfinder"));
    }

    private ItemStack buildWalletItem(Player player) {
        long   tokens = plugin.getCurrencyManager().getTokens(player);
        double money  = plugin.getCurrencyManager().getMoney(player);
        ItemStack item = new ItemStack(Material.BOOK);
        ItemMeta  meta = item.getItemMeta();
        meta.displayName(MessageUtil.parse("<gradient:#a18cd1:#fbc2eb><bold>Wallet</bold></gradient>"));
        List<Component> lore = new ArrayList<>();
        lore.add(Component.empty());
        lore.add(MessageUtil.parse("<gray>Tokens <dark_gray>\u2502 <gold>" + NumberUtil.format(tokens) + "</gold>"));
        lore.add(MessageUtil.parse("<gray>Money  <dark_gray>\u2502 <green>$" + NumberUtil.format(money) + "</green>"));
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
            case "crop"      -> { lvl = m.getCropLevel(player);      max = HoeUpgradeManager.MAX_CROP_LEVEL;       grad = "<gradient:#f6d365:#fda085>"; label = "\uD83C\uDF3E Crop";      mat = Material.WHEAT; }
            case "xp"        -> { lvl = m.getXpLevel(player);        max = HoeUpgradeManager.MAX_XP_LEVEL;         grad = "<gradient:#7afcff:#00c2ff>"; label = "\u2728 XP";          mat = Material.EXPERIENCE_BOTTLE; }
            case "token"     -> { lvl = m.getTokenLevel(player);     max = HoeUpgradeManager.MAX_TOKEN_LEVEL;      grad = "<gradient:#ffd86f:#fc6262>"; label = "\uD83D\uDCB0 Token";    mat = Material.GOLD_NUGGET; }
            case "keyfinder" -> { lvl = m.getKeyFinderLevel(player); max = HoeUpgradeManager.MAX_KEY_FINDER_LEVEL; grad = "<gradient:#a18cd1:#fbc2eb>"; label = "\uD83D\uDD11 Key Finder"; mat = Material.TRIPWIRE_HOOK; }
            default          -> { lvl = m.getHoeLevel(player);       max = HoeUpgradeManager.MAX_HOE_LEVEL;        grad = "<gradient:#c0c0c0:#ffffff>"; label = "\u2692 Hoe";         mat = m.getHoeMaterial(player); }
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

    // ── helpers ───────────────────────────────────────────────────────────

    private String levelBar(int current, int max, int segments) {
        int filled = max > 0 ? (int) Math.round((double) current / max * segments) : 0;
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < segments; i++)
            sb.append(i < filled ? "<green>\u2588" : "<dark_gray>\u2591");
        return sb.toString();
    }

    private String costTag(long balance, long cost) {
        return (balance >= cost ? "<green>" : "<red>") + NumberUtil.format(cost)
                + (balance >= cost ? "</green>" : "</red>");
    }

    private String costTagMoney(double balance, long cost) {
        return (balance >= cost ? "<green>" : "<red>") + NumberUtil.format(cost)
                + (balance >= cost ? "</green>" : "</red>");
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
