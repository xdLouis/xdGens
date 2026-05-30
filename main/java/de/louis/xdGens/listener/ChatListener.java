package de.louis.xdGens.listener;

import de.louis.xdGens.main.Main;
import de.louis.xdGens.util.MessageUtil;
import io.papermc.paper.event.player.AsyncChatEvent;
import net.kyori.adventure.text.serializer.plain.PlainTextComponentSerializer;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;

public class ChatListener implements Listener {

    private final Main plugin;

    public ChatListener(Main plugin) {
        this.plugin = plugin;
    }

    @EventHandler(priority = EventPriority.HIGH, ignoreCancelled = true)
    public void onChat(AsyncChatEvent event) {
        Player player = event.getPlayer();
        String plain  = PlainTextComponentSerializer.plainText().serialize(event.message());
        String safe   = plain.replace("<", "\\<"); // prevent MiniMessage injection

        String tag     = plugin.getPlayerCosmeticManager().buildTagFormat(player);
        String name    = plugin.getPlayerCosmeticManager().buildNameFormat(player);
        String message = plugin.getPlayerCosmeticManager().buildChatFormat(player, safe);

        // Format: [Tag] ColoredName » <chat-colored message>
        String format = tag.isEmpty()
                ? name + " <dark_gray>\u00bb </dark_gray>" + message
                : tag + " " + name + " <dark_gray>\u00bb </dark_gray>" + message;

        event.renderer((source, sourceDisplayName, msg, viewer) ->
                MessageUtil.parse(format));
    }
}
