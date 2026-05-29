package de.louis.xdGens.gui;

import de.louis.xdGens.crate.CrateReward;
import de.louis.xdGens.main.Main;
import de.louis.xdGens.manager.PlayerCosmeticManager;
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
import java.util.Optional;
import java.util.Set;

public class CosmeticsGUI {

    public static final String TITLE_TAGS   = "✦ Cosmetics » Tags";
    public static final String TITLE_COLORS = "✦ Cosmetics » Colors";

    // slots 0-8 = top bar, 9-44 = content (4 rows)
    // content starts at slot 9
    private static final int SIZE = 54;

    // tab button slots
    private static final int SLOT_TAB_TAGS   = 0;
    private static final int SLOT_TAB_COLORS = 1;
    private static final int SLOT_UNEQUIP    = 8;

    // content area: slots 9-44, we use 9-43 (3×12 = 36 minus filler)
    // cosmetic items go into slots 10-16, 19-25, 28-34 (3 rows of 7, centered in 9-wide grid)
    private static final int[] CONTENT_SLOTS = {
        10, 11, 12, 13, 14, 15, 16,
        19, 20, 21, 22, 23, 24, 25,
        28, 29, 30, 31, 32, 33, 34
    };

    private final Main plugin;

    public CosmeticsGUI(Main plugin) {
        this.plugin = plugin;
    }

    public void openTags(Player player) {
        open(player, true);
    }

    public void openColors(Player player) {
        open(player, false);
    }

    private void open(Player player, boolean tagsTab) {
        String rawTitle = tagsTab ? TITLE_TAGS : TITLE_COLORS;
        Inventory inv = Bukkit.createInventory(null, SIZE, MessageUtil.parse(rawTitle));

        PlayerCosmeticManager mgr = plugin.getPlayerCosmeticManager();
        Optional<CrateReward> activeTag   = mgr.getActiveTag(player);
        Optional<CrateReward> activeColor = mgr.getActiveColor(player);

        // ── border / filler ───────────────────────────────────────────
        ItemStack filler = filler();
        for (int i = 0; i < SIZE; i++) inv.setItem(i, filler);
        // clear content slots
        for (int s : CONTENT_SLOTS) inv.setItem(s, null);
        // clear bottom row except corners
        for (int i = 45; i < 54; i++) inv.setItem(i, filler);

        // ── tab buttons ───────────────────────────────────────────────
        inv.setItem(SLOT_TAB_TAGS,   tabItem(tagsTab,  tagsTab,
                Material.NAME_TAG,
                "<gradient:#7afcff:#00c2ff><bold>Chat Tags</bold></gradient>",
                List.of("<gray>Equip a chat prefix tag.",
                        tagsTab ? "<green>► Currently viewing" : "<gray>Click to switch")));

        inv.setItem(SLOT_TAB_COLORS, tabItem(!tagsTab, !tagsTab,
                Material.GLOW_INK_SAC,
                "<gradient:#f6d365:#fda085><bold>Name Colors</bold></gradient>",
                List.of("<gray>Equip a name colour.",
                        !tagsTab ? "<green>► Currently viewing" : "<gray>Click to switch")));

        // ── unequip button ────────────────────────────────────────────
        boolean hasActive = tagsTab ? activeTag.isPresent() : activeColor.isPresent();
        String activePreview = "<dark_gray>None";
        if (tagsTab && activeTag.isPresent())
            activePreview = activeTag.get().getCosmeticFormat();
        else if (!tagsTab && activeColor.isPresent())
            activePreview = activeColor.get().getCosmeticFormat().replace("{name}", player.getName());

        inv.setItem(SLOT_UNEQUIP, buildItem(
                hasActive ? Material.BARRIER : Material.LIGHT_GRAY_STAINED_GLASS_PANE,
                "<red><bold>Remove Active</bold></red>",
                List.of(
                        "<gray>Active: " + activePreview,
                        "",
                        hasActive ? "<red>Click to unequip." : "<dark_gray>Nothing equipped."
                ),
                false
        ));

        // ── cosmetics ─────────────────────────────────────────────────
        Set<CrateReward> collection = tagsTab
                ? mgr.getUnlockedTags(player)
                : mgr.getUnlockedColors(player);

        // all rewards of the right type, unlocked first
        List<CrateReward> all = new ArrayList<>();
        for (CrateReward r : CrateReward.values()) {
            if (tagsTab ? r.isTag() : r.isColor()) all.add(r);
        }
        // sort: unlocked first, then by tier desc
        all.sort((a, b) -> {
            boolean ua = collection.contains(a), ub = collection.contains(b);
            if (ua != ub) return ua ? -1 : 1;
            return Integer.compare(b.getTier(), a.getTier());
        });

        Optional<CrateReward> active = tagsTab ? activeTag : activeColor;

        for (int i = 0; i < CONTENT_SLOTS.length && i < all.size(); i++) {
            CrateReward r      = all.get(i);
            boolean unlocked   = collection.contains(r);
            boolean isActive   = active.isPresent() && active.get() == r;
            inv.setItem(CONTENT_SLOTS[i], buildCosmeticItem(r, unlocked, isActive, player));
        }

        player.openInventory(inv);
    }

    // ── item builders ─────────────────────────────────────────────────

    private ItemStack buildCosmeticItem(CrateReward r, boolean unlocked, boolean active, Player player) {
        Material mat;
        if (!unlocked) {
            mat = Material.GRAY_STAINED_GLASS_PANE; // locked
        } else {
            mat = r.getIcon();
        }

        String preview = r.isColor()
                ? r.getCosmeticFormat().replace("{name}", player.getName())
                : r.getCosmeticFormat();

        List<String> lore = new ArrayList<>();
        lore.add("<gray>Rarity: " + r.tierLabel());
        lore.add("");
        if (unlocked) {
            lore.add("<gray>Preview: " + preview);
            lore.add("");
            if (active) {
                lore.add("<green>✔ Currently equipped");
                lore.add("<gray>Click to <red>unequip</red>.");
            } else {
                lore.add("<yellow>► Click to equip!");
            }
        } else {
            lore.add("<red>✘ Locked");
            lore.add("<dark_gray>Earn this from crates.");
        }

        String displayName;
        if (!unlocked) {
            displayName = "<gray>" + r.getDisplayName() + "</gray>";
        } else if (active) {
            displayName = "<gradient:#56ab2f:#a8e063><bold>" + r.getDisplayName() + "</bold></gradient>";
        } else {
            displayName = "<white><bold>" + r.getDisplayName() + "</bold></white>";
        }

        return buildItem(mat, displayName, lore, active && unlocked);
    }

    private ItemStack tabItem(boolean active, boolean isActive, Material mat, String name, List<String> lore) {
        ItemStack item = buildItem(mat, name, lore, isActive);
        return item;
    }

    private ItemStack buildItem(Material mat, String name, List<String> loreStrings, boolean glow) {
        ItemStack item = new ItemStack(mat);
        ItemMeta meta = item.getItemMeta();

        meta.displayName(MessageUtil.parse("<!italic>" + name));

        List<Component> lore = new ArrayList<>();
        for (String l : loreStrings) {
            if (l.isEmpty()) lore.add(Component.empty());
            else lore.add(MessageUtil.parse("<!italic><gray>" + l));
        }
        meta.lore(lore);

        if (glow) {
            meta.addEnchant(Enchantment.UNBREAKING, 1, true);
            meta.addItemFlags(ItemFlag.HIDE_ENCHANTS, ItemFlag.HIDE_ATTRIBUTES);
        } else {
            meta.addItemFlags(ItemFlag.HIDE_ATTRIBUTES);
        }

        item.setItemMeta(meta);
        return item;
    }

    private ItemStack filler() {
        ItemStack item = new ItemStack(Material.BLACK_STAINED_GLASS_PANE);
        ItemMeta meta = item.getItemMeta();
        meta.displayName(Component.empty());
        item.setItemMeta(meta);
        return item;
    }
}
