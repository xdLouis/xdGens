package de.louis.xdGens.listener;

import de.louis.xdGens.crate.CrateReward;
import de.louis.xdGens.gui.CosmeticsGUI;
import de.louis.xdGens.main.Main;
import de.louis.xdGens.manager.PlayerCosmeticManager;
import de.louis.xdGens.util.MessageUtil;
import net.kyori.adventure.text.serializer.plain.PlainTextComponentSerializer;
import org.bukkit.Sound;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.InventoryClickEvent;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.Set;

public class CosmeticsGUIListener implements Listener {

    private final Main plugin;

    public CosmeticsGUIListener(Main plugin) {
        this.plugin = plugin;
    }

    @EventHandler(priority = EventPriority.HIGH, ignoreCancelled = true)
    public void onClick(InventoryClickEvent event) {
        if (!(event.getWhoClicked() instanceof Player player)) return;

        String title = PlainTextComponentSerializer.plainText().serialize(event.getView().title());

        // decode() handles ALL pages, not just page 0
        CosmeticsGUI.TabPage tabPage = CosmeticsGUI.decode(title);
        if (tabPage == null) return;

        CosmeticsGUI.Tab tab = tabPage.tab();
        int currentPage      = tabPage.page();

        event.setCancelled(true);
        int slot = event.getRawSlot();
        if (slot < 0 || slot >= 54) return;

        PlayerCosmeticManager mgr = plugin.getPlayerCosmeticManager();
        CosmeticsGUI gui = new CosmeticsGUI(plugin);

        // ── tab switches (always go back to page 0) ────────────────
        if (slot == 0) { gui.openTags(player);       return; }
        if (slot == 1) { gui.openColors(player);     return; }
        if (slot == 2) { gui.openChatColors(player); return; }
        if (slot == 3) { gui.openGlow(player);       return; }

        // ── pagination buttons ──────────────────────────────────────
        if (slot == CosmeticsGUI.SLOT_PREV) {
            if (currentPage > 0) gui.open(player, tab, currentPage - 1);
            return;
        }
        if (slot == CosmeticsGUI.SLOT_NEXT) {
            gui.open(player, tab, currentPage + 1);
            return;
        }

        // ── unequip ─────────────────────────────────────────────────
        if (slot == 8) {
            switch (tab) {
                case TAGS        -> mgr.setActiveTag(player, null);
                case NAME_COLORS -> mgr.setActiveColor(player, null);
                case CHAT_COLORS -> mgr.setActiveChatColor(player, null);
                case GLOW        -> {
                    mgr.setActiveGlow(player, null);
                    plugin.getGlowManager().removeGlow(player);
                }
            }
            player.playSound(player.getLocation(), Sound.BLOCK_NOTE_BLOCK_BASS, 0.8f, 0.8f);
            MessageUtil.sendRaw(player, MessageUtil.PREFIX + " <gray>Removed active "
                    + tab.name().toLowerCase().replace('_', ' ') + ".");
            gui.open(player, tab, currentPage);
            return;
        }

        // ── cosmetic slots ─────────────────────────────────────────
        int contentIdx = slotToContentIdx(slot);
        if (contentIdx < 0) return;

        // apply page offset so page 1+ maps to the correct reward
        int pageSize  = CosmeticsGUI.CONTENT_SLOTS.length; // 21
        int globalIdx = currentPage * pageSize + contentIdx;

        CrateReward.Type type = switch (tab) {
            case TAGS        -> CrateReward.Type.TAG;
            case NAME_COLORS -> CrateReward.Type.NAME_COLOR;
            case CHAT_COLORS -> CrateReward.Type.CHAT_COLOR;
            case GLOW        -> CrateReward.Type.GLOW;
        };
        List<CrateReward> all = new ArrayList<>();
        for (CrateReward r : CrateReward.values()) if (r.getType() == type) all.add(r);

        Set<CrateReward> collection = switch (tab) {
            case TAGS        -> mgr.getUnlockedTags(player);
            case NAME_COLORS -> mgr.getUnlockedColors(player);
            case CHAT_COLORS -> mgr.getUnlockedChatColors(player);
            case GLOW        -> mgr.getUnlockedGlows(player);
        };
        all.sort((a, b) -> {
            boolean ua = collection.contains(a), ub = collection.contains(b);
            if (ua != ub) return ua ? -1 : 1;
            return Integer.compare(b.getTier(), a.getTier());
        });

        if (globalIdx >= all.size()) return;

        CrateReward reward = all.get(globalIdx);
        if (!mgr.hasCosmetic(player, reward)) {
            player.playSound(player.getLocation(), Sound.ENTITY_VILLAGER_NO, 0.8f, 1f);
            MessageUtil.sendRaw(player, MessageUtil.PREFIX + " <red>You haven't unlocked <white>"
                    + reward.getDisplayName() + "</white> yet.");
            return;
        }

        Optional<CrateReward> active = switch (tab) {
            case TAGS        -> mgr.getActiveTag(player);
            case NAME_COLORS -> mgr.getActiveColor(player);
            case CHAT_COLORS -> mgr.getActiveChatColor(player);
            case GLOW        -> mgr.getActiveGlow(player);
        };

        if (active.isPresent() && active.get() == reward) {
            // toggle off
            switch (tab) {
                case TAGS        -> mgr.setActiveTag(player, null);
                case NAME_COLORS -> mgr.setActiveColor(player, null);
                case CHAT_COLORS -> mgr.setActiveChatColor(player, null);
                case GLOW        -> {
                    mgr.setActiveGlow(player, null);
                    plugin.getGlowManager().removeGlow(player);
                }
            }
            player.playSound(player.getLocation(), Sound.BLOCK_NOTE_BLOCK_BASS, 0.8f, 0.9f);
            MessageUtil.sendRaw(player, MessageUtil.PREFIX + " <gray>Unequipped <white>"
                    + reward.getDisplayName() + "</white>.");
        } else {
            switch (tab) {
                case TAGS        -> mgr.setActiveTag(player, reward);
                case NAME_COLORS -> mgr.setActiveColor(player, reward);
                case CHAT_COLORS -> mgr.setActiveChatColor(player, reward);
                case GLOW        -> {
                    mgr.setActiveGlow(player, reward);
                    plugin.getGlowManager().applyGlow(player);
                }
            }
            player.playSound(player.getLocation(), Sound.ENTITY_PLAYER_LEVELUP, 0.7f, 1.4f);
            String preview;
            if (reward.isColor())          preview = reward.getCosmeticFormat().replace("{name}", player.getName());
            else if (reward.isChatColor()) preview = reward.getCosmeticFormat().replace("{msg}", "Hello!");
            else if (reward.isGlow())      preview = "<yellow>✨ " + reward.getDisplayName() + "</yellow>";
            else                           preview = reward.getCosmeticFormat();
            MessageUtil.sendRaw(player, MessageUtil.PREFIX + " <green>Equipped: " + preview);
        }
        gui.open(player, tab, currentPage);
    }

    // ── helpers ──────────────────────────────────────────────────

    private int slotToContentIdx(int slot) {
        int[] cs = CosmeticsGUI.CONTENT_SLOTS;
        for (int i = 0; i < cs.length; i++) if (cs[i] == slot) return i;
        return -1;
    }
}
