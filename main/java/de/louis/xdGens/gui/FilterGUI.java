package de.louis.xdGens.gui;

import de.louis.xdGens.main.Main;
import de.louis.xdGens.util.MessageUtil;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;

import java.util.List;

/**
 * Small 3-row (27 slot) GUI to pick a sort order for the cosmetics GUI.
 * Title encodes the origin tab + page + current sort so the listener can
 * return to the right position after selection.
 *
 * Title format: "\u2746 Filter|<TAB>|<PAGE>|<SORT>"
 */
public class FilterGUI {

    public static final String FILTER_PREFIX = "\u2746 Filter";

    private static final int SIZE = 27;

    // slot layout for the 7 sort options (centered in a 9-wide grid)
    private static final int[] SORT_SLOTS = { 10, 11, 12, 13, 14, 15, 16 };

    public FilterGUI(Main plugin) {}

    public void open(Player player, CosmeticsGUI.Tab tab, int page, CosmeticsGUI.Sort current) {
        String encodedTitle = FILTER_PREFIX + "|" + tab.name() + "|" + page + "|" + current.name();
        Inventory inv = Bukkit.createInventory(null, SIZE, MessageUtil.parse(encodedTitle));

        // fill background
        ItemStack filler = CosmeticsGUI.buildItem(
                org.bukkit.Material.GRAY_STAINED_GLASS_PANE,
                "<gray>\u00a0", List.of(), false);
        for (int i = 0; i < SIZE; i++) inv.setItem(i, filler);

        // back button
        inv.setItem(4, CosmeticsGUI.buildItem(
                org.bukkit.Material.ARROW,
                "<yellow><bold>\u25c4 Back</bold></yellow>",
                List.of("<gray>Return without changing"), false));

        // one button per sort option
        CosmeticsGUI.Sort[] sorts = CosmeticsGUI.Sort.values();
        for (int i = 0; i < sorts.length && i < SORT_SLOTS.length; i++) {
            CosmeticsGUI.Sort s = sorts[i];
            boolean selected = s == current;
            String name = selected
                    ? "<green><bold>\u2714 " + s.label + "</bold></green>"
                    : "<white>" + s.label;
            List<String> lore = selected
                    ? List.of("<green>Currently selected")
                    : List.of("<yellow>Click to apply");
            inv.setItem(SORT_SLOTS[i], CosmeticsGUI.buildItem(s.icon, name, lore, selected));
        }

        player.openInventory(inv);
    }

    /** Returns TabPage if title belongs to FilterGUI, else null. */
    public static FilterPage decode(String plainTitle) {
        if (!plainTitle.startsWith(FILTER_PREFIX)) return null;
        String[] parts = plainTitle.split("\\|");
        if (parts.length < 4) return null;
        try {
            CosmeticsGUI.Tab  tab  = CosmeticsGUI.Tab.valueOf(parts[1]);
            int               page = Integer.parseInt(parts[2]);
            CosmeticsGUI.Sort sort = CosmeticsGUI.Sort.valueOf(parts[3]);
            return new FilterPage(tab, page, sort);
        } catch (Exception e) { return null; }
    }

    public record FilterPage(CosmeticsGUI.Tab tab, int page, CosmeticsGUI.Sort sort) {}
}
