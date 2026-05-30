package de.louis.xdGens.skill;

import com.mojang.authlib.GameProfile;
import com.mojang.authlib.properties.Property;
import de.louis.xdGens.main.Main;
import de.louis.xdGens.util.MessageUtil;
import net.minecraft.network.protocol.game.*;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.item.enchantment.Enchantments;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.Sound;
import org.bukkit.craftbukkit.entity.CraftPlayer;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.HandlerList;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerMoveEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.scheduler.BukkitTask;

import java.util.*;

/**
 * Manages one Shadow Clone session for a single player.
 *
 * – 4 packet-only fake players surround the real player at +/-2 blocks on X/Z.
 * – Each fake player mirrors every movement via PlayerMoveEvent packets.
 * – The fake players hold the same hoe material as the real player (+ Unbreaking I glow).
 * – While the session is active, {@link #isActive(UUID)} returns true and
 *   FieldListener multiplies all token + XP gains by 4.
 */
public class ShadowCloneSession implements Listener {

    // ── Static session registry ────────────────────────────────────────────
    private static final Map<UUID, ShadowCloneSession> ACTIVE = new HashMap<>();

    public static boolean isActive(UUID uuid) { return ACTIVE.containsKey(uuid); }

    // Offsets for the 4 clones relative to the player (X, Z)
    private static final double[][] OFFSETS = {
            { 2.0,  0.0},
            {-2.0,  0.0},
            { 0.0,  2.0},
            { 0.0, -2.0}
    };

    // ── Instance fields ────────────────────────────────────────────────────
    private final Main     plugin;
    private final Player   player;
    private final int      level;
    private final int      durationTicks;
    private final int[]    entityIds   = new int[4];
    private final UUID[]   cloneUUIDs  = new UUID[4];
    private BukkitTask     task;

    public ShadowCloneSession(Main plugin, Player player, int level, int durationTicks) {
        this.plugin        = plugin;
        this.player        = player;
        this.level         = level;
        this.durationTicks = durationTicks;
    }

    // ── Lifecycle ──────────────────────────────────────────────────────────

    public void start() {
        if (ACTIVE.containsKey(player.getUniqueId())) return;
        ACTIVE.put(player.getUniqueId(), this);

        spawnClones();
        Bukkit.getPluginManager().registerEvents(this, plugin);

        player.playSound(player.getLocation(), Sound.ENTITY_ENDERMAN_TELEPORT, 1f, 0.8f);
        MessageUtil.sendRaw(player, MessageUtil.PREFIX
                + " <gradient:#9d50bb:#6e48aa>\uD83D\uDC64 Shadow Clones activated!</gradient>"
                + " <yellow>All gains \u00d74</yellow> <gray>for " + (durationTicks / 20) + "s</gray>");

        final int[] ticksLeft = {durationTicks};
        task = Bukkit.getScheduler().runTaskTimer(plugin, () -> {
            ticksLeft[0] -= 4;
            if (ticksLeft[0] <= 0) stop();
        }, 4L, 4L);
    }

    public void stop() {
        if (!ACTIVE.containsKey(player.getUniqueId())) return;
        ACTIVE.remove(player.getUniqueId());
        HandlerList.unregisterAll(this);
        if (task != null) { try { task.cancel(); } catch (Exception ignored) {} }
        despawnClones();
        MessageUtil.sendRaw(player, MessageUtil.PREFIX
                + " <gradient:#9d50bb:#6e48aa>\uD83D\uDC64 Shadow Clones faded.</gradient>");
    }

    // ── Clone spawning ─────────────────────────────────────────────────────

    private void spawnClones() {
        ServerPlayer nmsPlayer = ((CraftPlayer) player).getHandle();
        ItemStack hoe = buildCloneHoe(player);
        net.minecraft.world.item.ItemStack nmsHoe = CraftItemStackBridge.asNMSCopy(hoe);

        for (int i = 0; i < 4; i++) {
            cloneUUIDs[i] = UUID.randomUUID();
            // Allocate a unique entity ID using the server's ID counter
            entityIds[i] = net.minecraft.world.entity.Entity.nextEntityId();

            org.bukkit.Location loc = player.getLocation().clone().add(OFFSETS[i][0], 0, OFFSETS[i][1]);

            // Build a GameProfile for the fake player (copy of real player's skin)
            GameProfile profile = new GameProfile(cloneUUIDs[i], player.getName().substring(0, Math.min(16, player.getName().length())) + "_" + i);
            // Copy skin textures so the clone looks like the player
            for (Property p : nmsPlayer.getGameProfile().getProperties().get("textures")) {
                profile.getProperties().put("textures", p);
            }

            // ClientboundPlayerInfoUpdatePacket — ADD_PLAYER action
            var infoPacket = ClientboundPlayerInfoUpdatePacket.createSinglePlayerInitializing(
                    new net.minecraft.server.level.ServerPlayer(
                            nmsPlayer.server,
                            nmsPlayer.serverLevel(),
                            profile,
                            net.minecraft.world.entity.player.ProfilePublicKey.MISSING_PROFILE_PUBLIC_KEY
                    )
            );

            // ClientboundAddEntityPacket for a player entity (type 128 = player)
            // We use ClientboundAddPlayerPacket which is the dedicated packet for fake players
            var spawnPacket = new ClientboundAddPlayerPacket(
                    entityIds[i],
                    cloneUUIDs[i],
                    loc.getX(), loc.getY(), loc.getZ(),
                    (byte) Math.round(loc.getYaw() * 256f / 360f),
                    (byte) Math.round(loc.getPitch() * 256f / 360f)
            );

            // Equipment packet — hold hoe in main hand
            List<com.mojang.datafixers.util.Pair<EquipmentSlot, net.minecraft.world.item.ItemStack>> equipment = new ArrayList<>();
            equipment.add(com.mojang.datafixers.util.Pair.of(EquipmentSlot.MAINHAND, nmsHoe));
            var equipPacket = new ClientboundSetEquipmentPacket(entityIds[i], equipment);

            // Send to all players in range
            sendToNearby(player, infoPacket);
            sendToNearby(player, spawnPacket);
            sendToNearby(player, equipPacket);
        }
    }

    private void despawnClones() {
        var removePacket = new ClientboundRemoveEntitiesPacket(Arrays.stream(entityIds).boxed().mapToInt(Integer::intValue).toArray());
        sendToNearby(player, removePacket);
    }

    // ── Movement mirroring ─────────────────────────────────────────────────

    @EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
    public void onMove(PlayerMoveEvent event) {
        if (!event.getPlayer().getUniqueId().equals(player.getUniqueId())) return;
        if (!ACTIVE.containsKey(player.getUniqueId())) return;

        org.bukkit.Location to = event.getTo();
        if (to == null) return;

        for (int i = 0; i < 4; i++) {
            org.bukkit.Location cloneLoc = to.clone().add(OFFSETS[i][0], 0, OFFSETS[i][1]);
            var teleportPacket = new ClientboundTeleportEntityPacket(
                    entityIds[i],
                    cloneLoc.getX(), cloneLoc.getY(), cloneLoc.getZ(),
                    (byte) Math.round(to.getYaw() * 256f / 360f),
                    (byte) Math.round(to.getPitch() * 256f / 360f),
                    false
            );
            sendToNearby(player, teleportPacket);
        }
    }

    // ── Helpers ────────────────────────────────────────────────────────────

    /**
     * Builds a clone hoe: same material as the player's held hoe, plus Unbreaking I for the glow.
     */
    private static ItemStack buildCloneHoe(Player player) {
        ItemStack held = player.getInventory().getItemInMainHand();
        Material mat = held.getType().name().endsWith("_HOE")
                ? held.getType()
                : Material.WOODEN_HOE;
        ItemStack clone = new ItemStack(mat);
        ItemMeta meta = clone.getItemMeta();
        if (meta != null) {
            meta.addEnchant(Enchantment.UNBREAKING, 1, true);
            clone.setItemMeta(meta);
        }
        return clone;
    }

    private static void sendToNearby(Player origin, net.minecraft.network.protocol.Packet<?> packet) {
        for (Player p : origin.getWorld().getPlayers()) {
            if (p.getLocation().distanceSquared(origin.getLocation()) <= 1024) { // 32 blocks
                ((CraftPlayer) p).getHandle().connection.send(packet);
            }
        }
    }

    // ── Inner bridge helper (avoids direct import of CraftItemStack) ───────
    private static class CraftItemStackBridge {
        static net.minecraft.world.item.ItemStack asNMSCopy(ItemStack bukkit) {
            return org.bukkit.craftbukkit.inventory.CraftItemStack.asNMSCopy(bukkit);
        }
    }
}
