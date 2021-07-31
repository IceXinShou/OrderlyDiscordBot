package main.java.util;

import net.dv8tion.jda.api.entities.EmbedType;
import net.dv8tion.jda.api.entities.MessageEmbed;
import net.dv8tion.jda.api.events.interaction.GenericInteractionCreateEvent;
import net.dv8tion.jda.api.events.message.guild.GuildMessageReceivedEvent;
import org.jetbrains.annotations.Contract;
import org.jetbrains.annotations.NotNull;

import java.time.OffsetDateTime;
import java.util.Collections;
import java.util.List;

import static main.java.BotSetting.botOwnerID;

public class Funtions {

    /**
     * boolean
     */

    public static boolean isBotOwner(@NotNull GenericInteractionCreateEvent event) {
        return (botOwnerID.contains(event.getUser().getId()));
    }

    public static boolean isBotOwner(@NotNull GuildMessageReceivedEvent event) {
        return (event.getMember() != null && botOwnerID.contains(event.getMember().getId()));
    }

    /**
     * Embed
     */

    @Contract("_, _, _, _, _, _, _ -> new")
    public static @NotNull MessageEmbed createEmbed(String title, String description, String footer, String nickname, String avatarUrl, OffsetDateTime timeStamp, int color) {
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
    public static @NotNull MessageEmbed createEmbed(String title, String description, String nickname, List<MessageEmbed.Field> fields, String avatarUrl, int color) {
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
    public static @NotNull MessageEmbed createEmbed(String title, MessageEmbed.Field field, String footer, String nickname, String avatarUrl, int color) {
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

    @Contract("_, _, _, _, _, _, _, _ -> new")
    public static @NotNull MessageEmbed createEmbed(String title, String description, String footer, String nickname, String avatarUrl, List<MessageEmbed.Field> fields, OffsetDateTime timeStamp, int color) {
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
    public static @NotNull MessageEmbed createEmbed(String title, String description, String footer, OffsetDateTime timeStamp, int color) {
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
    public static @NotNull MessageEmbed createEmbed(String title, String url, String description, String footer, String nickname, String avatarUrl, int color) {
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

    @Contract("_, _ -> new")
    public static @NotNull MessageEmbed createEmbed(String description, int color) {
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

    @Contract("_, _ -> new")
    public static @NotNull MessageEmbed createEmbed(int color, String title) {
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

    @Contract("_, _, _, _, _, _, _, _ -> new")
    public static @NotNull MessageEmbed createEmbed(String title, String url, String description, String footer, String nickname, String avatarUrl, String image, int color) {
        return new MessageEmbed(url,
                title,
                description,
                EmbedType.IMAGE,
                null,
                color,
                null,
                null,
                new MessageEmbed.AuthorInfo(nickname, null, avatarUrl, null),
                null,
                new MessageEmbed.Footer(footer, null, null),
                new MessageEmbed.ImageInfo(image, null, 0, 0), null);
    }

    /**
     * Tag
     */

    @Contract(pure = true)
    public static @NotNull String tagUser(String ID) {
        return "<@!" + ID + '>';
    }

    @Contract(pure = true)
    public static @NotNull String tagChannel(String ID) {
        return "<#" + ID + '>';
    }

    @Contract(pure = true)
    public static @NotNull String tagRole(String ID) {
        return "<@&" + ID + '>';
    }

    public static @NotNull String tagUsers(@NotNull List<String> ID) {
        StringBuilder n = new StringBuilder();
        for (String str : ID) {
            n.append("<@&").append(str).append('>');
        }
        return n.toString();
    }

    public static @NotNull String tagChannels(@NotNull List<String> ID) {
        StringBuilder n = new StringBuilder();
        for (String str : ID) {
            n.append("<@&").append(str).append('>');
        }
        return n.toString();
    }

    public static @NotNull String tagRoles(@NotNull List<String> ID) {
        StringBuilder n = new StringBuilder();
        for (String str : ID) {
            n.append("<@&").append(str).append('>');
        }
        return n.toString();
    }

    public static @NotNull String toUnicode(@NotNull String input) {
        StringBuilder b = new StringBuilder();
        for (char c : input.toCharArray()) {
            if (c >= 128)
                b.append("\\u").append(String.format("%04X", (int) c));
            else
                b.append(c);
        }
        return b.toString();
    }

}
