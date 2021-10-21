package main.java.util;

import net.dv8tion.jda.api.entities.Member;

import static main.java.util.Tag.tagUserID;

public class PlaceholderReplacer {
    public static String placeholderReplacer(String input, Member member) {
        String fullName = member.getUser().getAsTag();
        return input
                .replace("%guild_name%", member.getGuild().getName())
                .replace("%user%", tagUserID(member.getId()))
                .replace("%user_name%", member.getUser().getName())
                .replace("%user_tag%", fullName.substring(fullName.lastIndexOf("#")))
                .replace("%nickname%", member.getNickname() == null ? member.getUser().getName() : member.getNickname());
    }
}
