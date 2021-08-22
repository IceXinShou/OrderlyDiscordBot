package main.java.util;

import net.dv8tion.jda.api.entities.Emoji;
import net.dv8tion.jda.api.entities.Emote;
import net.dv8tion.jda.api.entities.Guild;
import org.jetbrains.annotations.NotNull;

import java.util.List;

public class GetEmoji {

    public static Emoji toEmoji(@NotNull String emojiName, Guild guild) {
        int startIndex;
        if ((startIndex = emojiName.indexOf("<")) != -1) {
            int endIndex = emojiName.indexOf(">", startIndex);
            String[] ids = emojiName.substring(startIndex + 1, endIndex).split(":");
            Emote emoji;
            if (ids.length == 3 && (emoji = guild.getJDA().getEmoteById(ids[2])) != null)
                return Emoji.fromEmote(emoji);
            if (ids.length == 3)
                return null;
        }
        if ((emojiName.startsWith("U+") || emojiName.startsWith("u+")))
            return Emoji.fromUnicode(emojiName);
        Emote emote;
        if (isDigit(emojiName) && (emote = guild.getJDA().getEmoteById(emojiName)) != null)
            return Emoji.fromEmote(emote);

        List<Emote> emotes = guild.getEmotesByName(emojiName, false);
        if (emotes.size() > 0)
            return Emoji.fromEmote(emotes.get(0));
        return null;
    }

    private static boolean isDigit(@NotNull String emojiName) {
        for (char i : emojiName.toCharArray()) {
            if (i < '0' || i > '9')
                return false;
        }
        return true;
    }
}
