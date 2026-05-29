package de.louis.xdGens.listener;

import de.louis.xdGens.main.Main;
import de.louis.xdGens.util.MessageUtil;
import io.papermc.paper.event.player.AsyncChatEvent;
import net.kyori.adventure.text.Component;
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

        String tag   = plugin.getPlayerCosmeticManager().buildTagFormat(player);
        String name  = plugin.getPlayerCosmeticManager().buildNameFormat(player);
        String plain = PlainTextComponentSerializer.plainText().serialize(event.message());

        // Build: [Tag] ColoredName » message
        String format;
        if (!tag.isEmpty()) {
            format = tag + " " + name + " <dark_gray>» <white>" + escape(plain);
        } else {
            format = name + " <dark_gray>» <white>" + escape(plain);
        }

        event.renderer((source, sourceDisplayName, message, viewer) ->
                MessageUtil.parse(format)
        );
    }

    /** Escape < so players can't inject MiniMessage tags. */
    private String escape(String input) {
        return input.replace("<", "\\<");
    }
}
