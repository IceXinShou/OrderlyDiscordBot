package main.java.util;

import net.dv8tion.jda.api.entities.EmbedType;
import net.dv8tion.jda.api.entities.MessageEmbed;

import java.time.OffsetDateTime;
import java.util.Collections;
import java.util.List;

public class EmbedUtil {

    /**
     * 嵌入介面
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
}
