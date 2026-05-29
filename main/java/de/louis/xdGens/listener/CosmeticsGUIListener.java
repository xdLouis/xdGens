package de.louis.xdGens.listener;

import de.louis.xdGens.crate.CrateReward;
import de.louis.xdGens.gui.CosmeticsGUI;
import de.louis.xdGens.main.Main;
import de.louis.xdGens.manager.PlayerCosmeticManager;
import de.louis.xdGens.util.MessageUtil;
import net.kyori.adventure.text.serializer.plain.PlainTextComponentSerializer;
import org.bukkit.Material;
import org.bukkit.Sound;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.inventory.InventoryCloseEvent;
import org.bukkit.inventory.ItemStack;

import java.util.Optional;

public class CosmeticsGUIListener implements Listener {

    private final Main plugin;

    public CosmeticsGUIListener(Main plugin) {
        this.plugin = plugin;
    }

    @EventHandler(priority = EventPriority.HIGH, ignoreCancelled = true)
    public void onClick(InventoryClickEvent event) {
        if (!(event.getWhoClicked() instanceof Player player)) return;

        String title = PlainTextComponentSerializer.plainText()
                .serialize(event.getView().title());

        boolean isTags   = title.equals(CosmeticsGUI.TITLE_TAGS);
        boolean isColors = title.equals(CosmeticsGUI.TITLE_COLORS);
        if (!isTags && !isColors) return;

        event.setCancelled(true);

        int slot = event.getRawSlot();
        if (slot < 0 || slot >= 54) return;

        PlayerCosmeticManager mgr = plugin.getPlayerCosmeticManager();
        CosmeticsGUI gui = new CosmeticsGUI(plugin);

        // ── tab switch ────────────────────────────────────────────────
        if (slot == 0) { gui.openTags(player);   return; }
        if (slot == 1) { gui.openColors(player); return; }

        // ── unequip ───────────────────────────────────────────────────
        if (slot == 8) {
            if (isTags)   mgr.setActiveTag(player, null);
            else          mgr.setActiveColor(player, null);
            player.playSound(player.getLocation(), Sound.BLOCK_NOTE_BLOCK_BASS, 0.8f, 0.8f);
            MessageUtil.sendRaw(player, MessageUtil.PREFIX + " <gray>Removed active " + (isTags ? "tag" : "color") + ".");
            if (isTags) gui.openTags(player); else gui.openColors(player);
            return;
        }

        // ── content slots ─────────────────────────────────────────────
        int[] contentSlots = {
            10, 11, 12, 13, 14, 15, 16,
            19, 20, 21, 22, 23, 24, 25,
            28, 29, 30, 31, 32, 33, 34
        };

        int contentIdx = -1;
        for (int i = 0; i < contentSlots.length; i++) {
            if (contentSlots[i] == slot) { contentIdx = i; break; }
        }
        if (contentIdx < 0) return;

        // rebuild sorted list (same order as GUI)
        java.util.List<CrateReward> all = new java.util.ArrayList<>();
        for (CrateReward r : CrateReward.values()) {
            if (isTags ? r.isTag() : r.isColor()) all.add(r);
        }
        java.util.Set<CrateReward> unlocked = isTags
                ? mgr.getUnlockedTags(player)
                : mgr.getUnlockedColors(player);
        all.sort((a, b) -> {
            boolean ua = unlocked.contains(a), ub = unlocked.contains(b);
            if (ua != ub) return ua ? -1 : 1;
            return Integer.compare(b.getTier(), a.getTier());
        });

        if (contentIdx >= all.size()) return;
        CrateReward reward = all.get(contentIdx);

        if (!mgr.hasCosmetic(player, reward)) {
            player.playSound(player.getLocation(), Sound.ENTITY_VILLAGER_NO, 0.8f, 1f);
            MessageUtil.sendRaw(player, MessageUtil.PREFIX + " <red>You haven't unlocked <white>"
                    + reward.getDisplayName() + "</white> yet.");
            return;
        }

        Optional<CrateReward> active = isTags
                ? mgr.getActiveTag(player)
                : mgr.getActiveColor(player);

        if (active.isPresent() && active.get() == reward) {
            // toggle off
            if (isTags) mgr.setActiveTag(player, null);
            else        mgr.setActiveColor(player, null);
            player.playSound(player.getLocation(), Sound.BLOCK_NOTE_BLOCK_BASS, 0.8f, 0.9f);
            MessageUtil.sendRaw(player, MessageUtil.PREFIX + " <gray>Unequipped <white>" + reward.getDisplayName() + "</white>.");
        } else {
            if (isTags) mgr.setActiveTag(player, reward);
            else        mgr.setActiveColor(player, reward);
            player.playSound(player.getLocation(), Sound.ENTITY_PLAYER_LEVELUP, 0.7f, 1.4f);
            String preview = reward.isColor()
                    ? reward.getCosmeticFormat().replace("{name}", player.getName())
                    : reward.getCosmeticFormat();
            MessageUtil.sendRaw(player, MessageUtil.PREFIX + " <green>Equipped: " + preview);
        }

        if (isTags) gui.openTags(player); else gui.openColors(player);
    }
}
