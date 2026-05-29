package de.louis.xdGens.gui;

import de.louis.xdGens.crate.CrateReward;
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
 * Preview GUI — shows every cosmetic reward that can drop from a given CrateType,
 * along with the individual drop chance (odds) per item.
 *
 * Title format: "🎁 <CrateName> Preview"
 * Size: 6 rows (54 slots)
 * Slot 49 = back button
 */
public class CratePreviewGUI {

    public static final String TITLE_SUFFIX = " Preview";

    private final Main      plugin;
    private final CrateType crateType;

    public CratePreviewGUI(Main plugin, CrateType crateType) {
        this.plugin    = plugin;
        this.crateType = crateType;
    }

    public void open(Player player) {
        String title = crateType.getGradient() + "\uD83C\uDF81 " + crateType.getDisplayName() + " Preview</gradient>";
        Inventory inv = Bukkit.createInventory(null, 54, MessageUtil.parse(title));

        // collect eligible cosmetics for this crate
        int targetTier = switch (crateType) {
            case COMMON, UNCOMMON -> CrateReward.TIER_COMMON;    // tier 0 only
            case RARE, EPIC       -> CrateReward.TIER_VERY_RARE; // tier 2
            case LEGENDARY        -> CrateReward.TIER_LEGENDARY; // tier 3
        };

        // also include tier-1 rewards for COMMON/UNCOMMON crates
        boolean includeTierOne = (crateType == CrateType.COMMON || crateType == CrateType.UNCOMMON);

        List<CrateReward> pool = new ArrayList<>();
        double poolTotal = 0;
        for (CrateReward r : CrateReward.values()) {
            if (r.isPouch()) continue;
            boolean match = r.getTier() == targetTier || (includeTierOne && r.getTier() == CrateReward.TIER_RARE);
            if (match) {
                pool.add(r);
                poolTotal += r.getWeight();
            }
        }

        // base chance that ANY cosmetic drops from this crate
        double baseDrop = switch (crateType) {
            case COMMON    -> 0.008;
            case UNCOMMON  -> 0.015;
            case RARE      -> 0.035;
            case EPIC      -> 0.070;
            case LEGENDARY -> 0.140;
        };

        // fill border
        ItemStack border = pane(Material.BLACK_STAINED_GLASS_PANE);
        for (int i = 0; i < 54; i++) inv.setItem(i, border.clone());

        // fill inner slots (1–7 per row, rows 0–4)
        int slot = 0;
        int[] inner = innerSlots();
        for (CrateReward reward : pool) {
            if (slot >= inner.length) break;
            double itemChance = baseDrop * (reward.getWeight() / poolTotal) * 100.0;
            inv.setItem(inner[slot], buildRewardItem(reward, itemChance));
            slot++;
        }

        // back button slot 49
        inv.setItem(49, buildBackButton());

        // info item slot 51
        inv.setItem(51, buildInfoItem(baseDrop, pool.size()));

        player.openInventory(inv);
    }

    // ── item builders ────────────────────────────────────────────────

    private ItemStack buildRewardItem(CrateReward reward, double chancePercent) {
        ItemStack item = new ItemStack(reward.getIcon());
        ItemMeta  meta = item.getItemMeta();

        meta.displayName(MessageUtil.parse(reward.tierLabel() + " <white>" + reward.getDisplayName() + "</white>"));

        List<Component> lore = new ArrayList<>();
        lore.add(Component.empty());
        lore.add(MessageUtil.parse("<gray>Type: <white>" + typeLabel(reward) + "</white>"));
        lore.add(MessageUtil.parse("<gray>Rarity: " + reward.tierLabel()));
        lore.add(Component.empty());
        lore.add(MessageUtil.parse("<gray>Drop chance: <gold>" + String.format("%.4f", chancePercent) + "%</gold>"));
        lore.add(Component.empty());
        lore.add(MessageUtil.parse("<dark_gray>Preview: " + reward.getCosmeticFormat().replace("{name}", "Steve").replace("{msg}", "Hello!")));

        meta.lore(lore);
        meta.addEnchant(Enchantment.UNBREAKING, 1, true);
        meta.addItemFlags(ItemFlag.HIDE_ENCHANTS, ItemFlag.HIDE_ATTRIBUTES);
        item.setItemMeta(meta);
        return item;
    }

    private ItemStack buildBackButton() {
        ItemStack item = new ItemStack(Material.ARROW);
        ItemMeta  meta = item.getItemMeta();
        meta.displayName(MessageUtil.parse("<red>\u2190 Back"));
        List<Component> lore = new ArrayList<>();
        lore.add(MessageUtil.parse("<gray>Return to Crates menu"));
        meta.lore(lore);
        meta.addItemFlags(ItemFlag.HIDE_ATTRIBUTES);
        item.setItemMeta(meta);
        return item;
    }

    private ItemStack buildInfoItem(double baseDrop, int poolSize) {
        ItemStack item = new ItemStack(Material.BOOK);
        ItemMeta  meta = item.getItemMeta();
        meta.displayName(MessageUtil.parse(crateType.getGradient() + crateType.getDisplayName() + " Crate</gradient> <gray>Info"));
        List<Component> lore = new ArrayList<>();
        lore.add(Component.empty());
        lore.add(MessageUtil.parse("<gray>Cosmetic drop rate: <gold>" + String.format("%.1f", baseDrop * 100.0) + "%</gold> per open"));
        lore.add(MessageUtil.parse("<gray>Unique cosmetics: <white>" + poolSize + "</white>"));
        lore.add(Component.empty());
        lore.add(MessageUtil.parse("<dark_gray>Each % shown is the chance to get"));
        lore.add(MessageUtil.parse("<dark_gray>that specific cosmetic per crate open."));
        meta.lore(lore);
        meta.addItemFlags(ItemFlag.HIDE_ATTRIBUTES);
        item.setItemMeta(meta);
        return item;
    }

    // ── helpers ──────────────────────────────────────────────────────

    /** Returns the 40 inner (non-border) slots of a 54-slot inventory. */
    private int[] innerSlots() {
        List<Integer> slots = new ArrayList<>();
        for (int row = 0; row < 6; row++) {
            for (int col = 1; col <= 7; col++) {
                int s = row * 9 + col;
                if (s == 49 || s == 51) continue; // reserved
                slots.add(s);
            }
        }
        int[] arr = new int[slots.size()];
        for (int i = 0; i < slots.size(); i++) arr[i] = slots.get(i);
        return arr;
    }

    private ItemStack pane(Material mat) {
        ItemStack p = new ItemStack(mat);
        ItemMeta  m = p.getItemMeta();
        m.displayName(Component.empty());
        p.setItemMeta(m);
        return p;
    }

    private String typeLabel(CrateReward r) {
        if (r.isTag())       return "Chat Tag";
        if (r.isColor())     return "Name Color";
        if (r.isChatColor()) return "Chat Color";
        if (r.isGlow())      return "Glow Effect";
        return "Cosmetic";
    }

    /** Resolve CrateType from a preview inventory title. Returns null if not a preview. */
    public static CrateType resolveFromTitle(String title) {
        for (CrateType t : CrateType.values()) {
            if (title.contains(t.getDisplayName() + " Preview")) return t;
        }
        return null;
    }
}
