package main.java.util;

import net.dv8tion.jda.api.entities.GuildChannel;
import org.jetbrains.annotations.Contract;

import java.util.List;

public class Tag {


    /**
     * Tag
     */

    @Contract(pure = true)
    public static String tagUserID(String ID) {
        return "<@!" + ID + '>';
    }

    @Contract(pure = true)
    public static String tagChannelID(String ID) {
        return "<#" + ID + '>';
    }

    public static String tagChannel(GuildChannel channel) {
        return "<#" + channel.getId() + '>';
    }

    @Contract(pure = true)
    public static String tagRoleID(String ID) {
        return "<@&" + ID + '>';
    }

    public static String tagUsersID(List<String> ID) {
        StringBuilder n = new StringBuilder();
        for (String str : ID) {
            n.append("<@&").append(str).append('>');
        }
        return n.toString();
    }

    public static String tagChannelsID(List<String> ID) {
        StringBuilder n = new StringBuilder();
        for (String str : ID) {
            n.append("<@&").append(str).append('>');
        }
        return n.toString();
    }

    public static String tagRolesID(List<String> ID) {
        StringBuilder n = new StringBuilder();
        for (String str : ID) {
            n.append("<@&").append(str).append('>');
        }
        return n.toString();
    }
}
