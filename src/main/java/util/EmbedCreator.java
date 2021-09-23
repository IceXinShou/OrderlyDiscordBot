package main.java.util;

import net.dv8tion.jda.api.entities.EmbedType;
import net.dv8tion.jda.api.entities.MessageEmbed;
import net.dv8tion.jda.api.events.interaction.GenericInteractionCreateEvent;
import net.dv8tion.jda.api.events.message.guild.GuildMessageReceivedEvent;
import org.jetbrains.annotations.Contract;

import java.time.OffsetDateTime;
import java.util.Collections;
import java.util.List;

import static main.java.BotSetting.botOwnerID;

public class EmbedCreator {

    /**
     * boolean
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

    @Contract("_, _, _, _, _, _, _ -> new")
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

    @Contract("_, _, _, _, _, _ -> new")
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

    @Contract("_, _, _, _, _, _ -> new")
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

    public static MessageEmbed createEmbed(String title, List<MessageEmbed.Field> fields, int color) {
        return new MessageEmbed(null,
                title,
                null,
                EmbedType.RICH,
                null,
                color,
                null,
                null,
                null,
                null,
                null,
                null, fields);
    }

    @Contract("_, _, _, _, _, _, _, _ -> new")
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

    @Contract("_, _, _, _, _ -> new")
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

    @Contract("_, _, _, _, _, _, _ -> new")
    public static MessageEmbed createEmbed(String title, String url, String description, List<MessageEmbed.Field> fields, String footer, String nickname, String avatarUrl, int color) {
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
                null, fields);
    }

    @Contract("_, _, _, _, _, _, _ -> new")
    public static MessageEmbed createEmbed(String title, String url, String description, String footer, String nickname, String avatarUrl, int color) {
        return new MessageEmbed(null,
                title,
                description,
                EmbedType.RICH,
                null,
                color,
                null,
                null,
                new MessageEmbed.AuthorInfo(nickname, url, avatarUrl, null),
                null,
                new MessageEmbed.Footer(footer, null, null),
                null, null);
    }

    @Contract("_, _, _, _, _, _, _, _ -> new")
    public static MessageEmbed createEmbed(String title, String url, String description, String footer, String nickname, String avatarUrl, int color, String image) {
        return new MessageEmbed(null,
                title,
                description,
                EmbedType.IMAGE,
                null,
                color,
                null,
                null,
                new MessageEmbed.AuthorInfo(nickname, url, avatarUrl, null),
                null,
                new MessageEmbed.Footer(footer, null, null),
                new MessageEmbed.ImageInfo(image, null, 0, 0),
                null);
    }

    @Contract("_, _, _, _, _, _, _, _, _ -> new")
    public static MessageEmbed createEmbed(String title, String image, String url, String description, String footer, String nickname, String avatarUrl, int color, List<MessageEmbed.Field> fields) {
        return new MessageEmbed(url,
                title,
                description,
                EmbedType.IMAGE,
                null,
                color,
                null,
                null,
                new MessageEmbed.AuthorInfo(nickname, url, avatarUrl, null),
                null,
                new MessageEmbed.Footer(footer, null, null),
                new MessageEmbed.ImageInfo(image, null, 0, 0),
                fields);
    }

    @Contract("_, _, _, _, _, _, _, _ -> new")
    public static MessageEmbed createEmbed(String title, String image, String url, String description, String footer, String nickname, String avatarUrl, int color) {
        return new MessageEmbed(url,
                title,
                description,
                EmbedType.IMAGE,
                null,
                color,
                null,
                null,
                new MessageEmbed.AuthorInfo(nickname, url, avatarUrl, null),
                null,
                new MessageEmbed.Footer(footer, null, null),
                new MessageEmbed.ImageInfo(image, null, 0, 0), null);
    }

    @Contract("_, _, _, _, _, _, _, _ , _ -> new")
    public static MessageEmbed createEmbed(String title, String image, String titleUrl, String nicknameUrl, String description, String footer, int color, String nickname, String avatarUrl) {
        return new MessageEmbed(titleUrl,
                title,
                description,
                EmbedType.IMAGE,
                null,
                color,
                null,
                null,
                new MessageEmbed.AuthorInfo(nickname, nicknameUrl, avatarUrl, null),
                null,
                new MessageEmbed.Footer(footer, null, null),
                new MessageEmbed.ImageInfo(image, null, 0, 0), null);
    }

    @Contract("_, _ -> new")
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

    public static MessageEmbed createEmbed(String title, String description, int color) {
        return new MessageEmbed(null,
                title,
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

    @Contract("_, _ -> new")
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

    public static MessageEmbed createEmbed(String title, String url, String description, String footer, String nickname, String channelURL, String avatarUrl, String image, int color) {
        return new MessageEmbed(url,
                title,
                description,
                EmbedType.IMAGE,
                null,
                color,
                null,
                null,
                new MessageEmbed.AuthorInfo(nickname, channelURL, avatarUrl, null),
                null,
                new MessageEmbed.Footer(footer, null, null),
                new MessageEmbed.ImageInfo(image, null, 0, 0), null);
    }

    public static MessageEmbed createEmbed(String nickname, String avatarUrl, String image, int color) {
        return new MessageEmbed(null,
                "",
                "",
                EmbedType.IMAGE,
                null,
                color,
                null,
                null,
                new MessageEmbed.AuthorInfo(nickname, null, avatarUrl, null),
                null,
                null,
                new MessageEmbed.ImageInfo(image, null, 0, 0), null);
    }

    public static MessageEmbed createEmbed(String title, String description, String footer, String image, int color) {
        return new MessageEmbed(null,
                title,
                description,
                EmbedType.IMAGE,
                null,
                color,
                null,
                null,
                null,
                null,
                new MessageEmbed.Footer(footer, null, null),
                new MessageEmbed.ImageInfo(image, null, 0, 0), null);
    }
}

