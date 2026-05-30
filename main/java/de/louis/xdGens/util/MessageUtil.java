package de.louis.xdGens.util;

import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.minimessage.MiniMessage;
import net.kyori.adventure.text.serializer.plain.PlainTextComponentSerializer;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

public class MessageUtil {

    private static final MiniMessage MINI = MiniMessage.miniMessage();

    public static final String PREFIX = "<gradient:#a18cd1:#fbc2eb><bold>xdGens</bold></gradient><gray> »</gray>";
    public static final String INFO = PREFIX + " <gray>";
    public static final String SUCCESS = PREFIX + " <green>";
    public static final String ERROR = PREFIX + " <red>";
    public static final String CLOSE = "</red>";
    public static final String GRADIENT_PREFIX = "<gradient:#a18cd1:#fbc2eb><bold>xdGens</bold></gradient><gray> »</gray>";

    public static Component parse(String text) {
        return MINI.deserialize(text);
    }

    public static String strip(String text) {
        return PlainTextComponentSerializer.plainText().serialize(parse(text));
    }

    public static void send(CommandSender sender, String text) {
        sender.sendMessage(parse(text));
    }

    public static void sendRaw(CommandSender sender, String text) {
        sender.sendMessage(parse(text));
    }

    public static void actionbar(Player player, String text) {
        player.sendActionBar(parse(text));
    }
}