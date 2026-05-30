package de.louis.xdGens.listener;

import de.louis.xdGens.main.Main;
import de.louis.xdGens.skill.ShadowCloneSession;
import de.louis.xdGens.util.HoeUtil;
import de.louis.xdGens.util.MessageUtil;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.block.Action;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.inventory.ItemStack;

/**
 * Triggers the Shadow Clone skill when the player right-clicks with their
 * xdGens hoe in the main hand.
 */
public class HoeRightClickListener implements Listener {

    private final Main plugin;

    public HoeRightClickListener(Main plugin) {
        this.plugin = plugin;
    }

    @EventHandler(priority = EventPriority.NORMAL, ignoreCancelled = false)
    public void onRightClick(PlayerInteractEvent event) {
        Action action = event.getAction();
        // Only RIGHT_CLICK_AIR or RIGHT_CLICK_BLOCK
        if (action != Action.RIGHT_CLICK_AIR && action != Action.RIGHT_CLICK_BLOCK) return;

        Player    player = event.getPlayer();
        ItemStack item   = player.getInventory().getItemInMainHand();
        if (!HoeUtil.isXdHoe(item)) return;

        int cloneLevel = plugin.getHoeUpgradeManager().getShadowCloneLevel(player);
        if (cloneLevel <= 0) return; // skill not yet unlocked

        // Cooldown check
        if (ShadowCloneSession.isOnCooldown(player.getUniqueId())) {
            int remaining = ShadowCloneSession.remainingCooldownSeconds(player.getUniqueId());
            MessageUtil.sendRaw(player, MessageUtil.PREFIX
                    + " <gradient:#7f7fd5:#86a8e7>\uD83D\uDC64 Shadow Clone</gradient>"
                    + " <red>on cooldown</red> <gray>(" + remaining + "s remaining)</gray>");
            return;
        }

        // Already active?
        if (ShadowCloneSession.isActive(player.getUniqueId())) return;

        new ShadowCloneSession(plugin, player, cloneLevel).start();
    }
}
