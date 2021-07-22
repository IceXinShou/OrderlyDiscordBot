package main.java.util;

import net.dv8tion.jda.api.entities.EmbedType;
import net.dv8tion.jda.api.entities.MessageEmbed;
import net.dv8tion.jda.api.events.interaction.GenericInteractionCreateEvent;
import net.dv8tion.jda.api.events.message.guild.GuildMessageReceivedEvent;

import java.time.OffsetDateTime;
import java.util.Collections;
import java.util.List;

import static main.java.BotSetting.botOwnerID;

public class Funtions {

    /**
     * boolean
     *
     * @param event
     * @return
     */

    public static boolean isBotOwner(GenericInteractionCreateEvent event) {
        return (botOwnerID.contains(event.getUser().getId()));
    }

    public static boolean isBotOwner(GuildMessageReceivedEvent event) {
        return (event.getMember() != null && botOwnerID.contains(event.getMember().getId()));
    }

    /**
     * Embed
     */

    public static MessageEmbed createEmbed(String title, String description, String footer, String nickname, String avatarUrl, OffsetDateTime timeStamp, int color) {
        return new MessageEmbed(null,
                title,
                description,
                EmbedType.RICH,
                timeStamp,
                color,
                null,
                null,
                new MessageEmbed.AuthorInfo(nickname, null, avatarUrl, null),
                null,
                new MessageEmbed.Footer(footer, null, null),
                null, null);
    }

    public static MessageEmbed createEmbed(String title, String description, String nickname, List<MessageEmbed.Field> fields, String avatarUrl, int color) {
        return new MessageEmbed(null,
                title,
                description,
                EmbedType.RICH,
                null,
                color,
                null,
                null,
                new MessageEmbed.AuthorInfo(nickname, null, avatarUrl, null),
                null,
                null,
                null, fields);
    }

    public static MessageEmbed createEmbed(String title, MessageEmbed.Field field, String footer, String nickname, String avatarUrl, int color) {
        return new MessageEmbed(null,
                title,
                null,
                EmbedType.RICH,
                null,
                color,
                null,
                null,
                new MessageEmbed.AuthorInfo(nickname, null, avatarUrl, null),
                null,
                new MessageEmbed.Footer(footer, null, null),
                null, Collections.singletonList(field));
    }

    public static MessageEmbed createEmbed(String title, String description, String footer, String nickname, String avatarUrl, List<MessageEmbed.Field> fields, OffsetDateTime timeStamp, int color) {
        return new MessageEmbed(null,
                title,
                description,
                EmbedType.RICH,
                timeStamp,
                color,
                null,
                null,
                new MessageEmbed.AuthorInfo(nickname, null, avatarUrl, null),
                null,
                new MessageEmbed.Footer(footer, null, null),
                null, fields);
    }

    public static MessageEmbed createEmbed(String title, String description, String footer, OffsetDateTime timeStamp, int color) {
        return new MessageEmbed(null,
                title,
                description,
                EmbedType.RICH,
                timeStamp,
                color,
                null,
                null,
                null,
                null,
                new MessageEmbed.Footer(footer, null, null),
                null, null);
    }

    public static MessageEmbed createEmbed(String title, String url, String description, String footer, String nickname, String avatarUrl, int color) {
        return new MessageEmbed(url,
                title,
                description,
                EmbedType.RICH,
                null,
                color,
                null,
                null,
                new MessageEmbed.AuthorInfo(nickname, null, avatarUrl, null),
                null,
                new MessageEmbed.Footer(footer, null, null),
                null, null);
    }

    public static MessageEmbed createEmbed(String description, int color) {
        return new MessageEmbed(null,
                "",
                description,
                EmbedType.RICH,
                null,
                color,
                null,
                null,
                null,
                null,
                null,
                null, null);
    }

    public static MessageEmbed createEmbed(int color, String title) {
        return new MessageEmbed(null,
                title,
                "",
                EmbedType.RICH,
                null,
                color,
                null,
                null,
                null,
                null,
                null,
                null, null);
    }

    /**
     * Tag
     *
     * @param ID
     * @return
     */

    public static String tagUser(String ID) {
        return "<@!" + ID + '>';
    }

    public static String tagChannel(String ID) {
        return "<#" + ID + '>';
    }

    public static String tagRole(String ID) {
        return "<@&" + ID + '>';
    }

    public static String tagUser(List<String> ID) {
        StringBuilder n = new StringBuilder();
        for (String str : ID) {
            n.append("<@&").append(str).append('>');
        }
        return n.toString();
    }

    public static String tagChannel(List<String> ID) {
        StringBuilder n = new StringBuilder();
        for (String str : ID) {
            n.append("<@&").append(str).append('>');
        }
        return n.toString();
    }

    public static String tagRole(List<String> ID) {
        StringBuilder n = new StringBuilder();
        for (String str : ID) {
            n.append("<@&").append(str).append('>');
        }
        return n.toString();
    }

}
