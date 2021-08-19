package main.java.util;

import net.dv8tion.jda.api.entities.GuildChannel;
import org.jetbrains.annotations.Contract;
import org.jetbrains.annotations.NotNull;

import java.util.List;

public class Tag {


    /**
     * Tag
     */

    @Contract(pure = true)
    public static @NotNull String tagUserID(String ID) {
        return "<@!" + ID + '>';
    }

    @Contract(pure = true)
    public static @NotNull String tagChannelID(String ID) {
        return "<#" + ID + '>';
    }

    public static @NotNull String tagChannel(@NotNull GuildChannel channel) {
        return "<#" + channel.getId() + '>';
    }

    @Contract(pure = true)
    public static @NotNull String tagRoleID(String ID) {
        return "<@&" + ID + '>';
    }

    public static @NotNull String tagUsersID(@NotNull List<String> ID) {
        StringBuilder n = new StringBuilder();
        for (String str : ID) {
            n.append("<@&").append(str).append('>');
        }
        return n.toString();
    }

    public static @NotNull String tagChannelsID(@NotNull List<String> ID) {
        StringBuilder n = new StringBuilder();
        for (String str : ID) {
            n.append("<@&").append(str).append('>');
        }
        return n.toString();
    }

    public static @NotNull String tagRolesID(@NotNull List<String> ID) {
        StringBuilder n = new StringBuilder();
        for (String str : ID) {
            n.append("<@&").append(str).append('>');
        }
        return n.toString();
    }


}
