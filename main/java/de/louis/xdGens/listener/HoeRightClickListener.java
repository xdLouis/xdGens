package de.louis.xdGens.listener;

import de.louis.xdGens.main.Main;
import de.louis.xdGens.skill.ShadowCloneSession;
import de.louis.xdGens.skill.ShadowCloneSkill;
import de.louis.xdGens.util.HoeUtil;
import de.louis.xdGens.util.MessageUtil;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.block.Action;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.inventory.ItemStack;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

/**
 * Listens for right-click with the xdGens hoe and triggers the Shadow Clone skill.
 */
public class HoeRightClickListener implements Listener {

    private final Main plugin;
    private final Map<UUID, Long> lastUsed = new HashMap<>();

    public HoeRightClickListener(Main plugin) {
        this.plugin = plugin;
    }

    @EventHandler(priority = EventPriority.HIGH, ignoreCancelled = false)
    public void onInteract(PlayerInteractEvent event) {
        if (event.getAction() != Action.RIGHT_CLICK_AIR
                && event.getAction() != Action.RIGHT_CLICK_BLOCK) return;

        Player    player = event.getPlayer();
        ItemStack held   = player.getInventory().getItemInMainHand();
        if (!HoeUtil.isXdHoe(held)) return;

        int shadowLevel = plugin.getHoeUpgradeManager().getShadowCloneLevel(player);
        if (shadowLevel <= 0) return; // not unlocked

        // Prestige check
        if (plugin.getProgressionManager().getPrestige(player) < ShadowCloneSkill.REQUIRED_PRESTIGE) {
            MessageUtil.sendRaw(player, MessageUtil.PREFIX
                    + " <red>\u274C Shadow Clone requires Prestige "
                    + ShadowCloneSkill.REQUIRED_PRESTIGE + "!</red>");
            return;
        }

        // Already active
        if (ShadowCloneSession.isActive(player.getUniqueId())) {
            MessageUtil.sendRaw(player, MessageUtil.PREFIX
                    + " <gradient:#9d50bb:#6e48aa>\uD83D\uDC64 Shadow Clones are already active!</gradient>");
            return;
        }

        // Cooldown check
        ShadowCloneSkill skill = (ShadowCloneSkill) plugin.getSkillManager().getSkill("shadow_clone");
        long cooldownMs = skill.getCooldownMs(shadowLevel);
        Long last = lastUsed.get(player.getUniqueId());
        if (last != null) {
            long elapsed = System.currentTimeMillis() - last;
            if (elapsed < cooldownMs) {
                long remaining = (cooldownMs - elapsed) / 1000;
                MessageUtil.sendRaw(player, MessageUtil.PREFIX
                        + " <red>\u23F3 Shadow Clone on cooldown! " + remaining + "s remaining.</red>");
                return;
            }
        }

        // Activate
        lastUsed.put(player.getUniqueId(), System.currentTimeMillis());
        int durationTicks = skill.getDurationTicks(shadowLevel);
        new ShadowCloneSession(plugin, player, shadowLevel, durationTicks).start();
    }
}
