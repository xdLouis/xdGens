package de.louis.xdGens.util;

import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.minimessage.MiniMessage;
import net.kyori.adventure.text.serializer.plain.PlainTextComponentSerializer;
import org.bukkit.entity.Player;

public class MessageUtil {

    private static final MiniMessage MM = MiniMessage.miniMessage();

    // Prefix mit Gold→Gelb Gradient
    public static final String GRADIENT_PREFIX = "<gradient:#FFD700:#FFA500><bold>xdGens</bold></gradient> <dark_gray>»</dark_gray>";

    // Farben
    public static final String SUCCESS  = "<gradient:#00FF87:#00D4FF>";
    public static final String ERROR    = "<gradient:#FF4757:#FF6B81>";
    public static final String INFO     = "<gradient:#A29BFE:#6C5CE7>";
    public static final String CURRENCY = "<gradient:#FFD700:#FFA500>";
    public static final String CLOSE    = "</gradient>";

    public static void send(Player player, String miniMessage) {
        player.sendMessage(MM.deserialize(GRADIENT_PREFIX + " <gray>" + miniMessage + "</gray>"));
    }

    public static void sendRaw(Player player, String miniMessage) {
        player.sendMessage(MM.deserialize(miniMessage));
    }

    public static Component parse(String miniMessage) {
        return MM.deserialize(miniMessage);
    }

    public static String strip(String miniMessage) {
        return PlainTextComponentSerializer.plainText().serialize(MM.deserialize(miniMessage));
    }

    /**
     * Baut eine farbige Reward-Nachricht.
     * Beispiel: +$50.00  |  +120 Tokens  |  +3 Gems
     */
    public static void sendHarvestReward(Player player, double money, int tokens, int gems) {
        String msg = GRADIENT_PREFIX + " ";
        if (money > 0)  msg += CURRENCY  + "+$"      + String.format("%.2f", money) + CLOSE + "  ";
        if (tokens > 0) msg += "<gradient:#00CFFF:#0077FF>" + "+" + tokens + " Tokens" + CLOSE + "  ";
        if (gems > 0)   msg += "<gradient:#FF66CC:#CC00FF>" + "+" + gems   + " Gems"   + CLOSE;
        sendRaw(player, msg.stripTrailing());
    }
}
