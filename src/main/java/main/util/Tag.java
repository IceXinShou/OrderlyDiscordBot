package main.util;

import net.dv8tion.jda.api.entities.GuildChannel;
import net.dv8tion.jda.api.events.guild.voice.GenericGuildVoiceEvent;
import net.dv8tion.jda.api.events.interaction.GenericComponentInteractionCreateEvent;
import net.dv8tion.jda.api.events.interaction.GenericInteractionCreateEvent;
import net.dv8tion.jda.api.events.message.guild.GuildMessageUpdateEvent;
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
        for (String str : ID)
            n.append("<@&").append(str).append('>');

        return n.toString();
    }

    public static String tagChannelsID(List<String> ID) {
        StringBuilder n = new StringBuilder();
        for (String str : ID)
            n.append("<@&").append(str).append('>');

        return n.toString();
    }

    public static String tagRolesID(List<String> ID) {
        StringBuilder n = new StringBuilder();
        for (String str : ID)
            n.append("<@&").append(str).append('>');

        return n.toString();
    }

    public static String getMemberName(GenericInteractionCreateEvent event) {
        return (event.getMember().getNickname() == null ?
                event.getUser().getAsTag() : String.format("%s (%s)", event.getMember().getNickname(), event.getUser().getAsTag()));
    }

    public static String getMemberName(GenericGuildVoiceEvent event) {
        return (event.getMember().getNickname() == null ?
                event.getMember().getUser().getAsTag() : String.format("%s (%s)", event.getMember().getNickname(), event.getMember().getUser().getAsTag()));
    }

    public static String getMemberName(GenericComponentInteractionCreateEvent event) {
        return (event.getMember().getNickname() == null ?
                event.getMember().getUser().getAsTag() : String.format("%s (%s)", event.getMember().getNickname(), event.getMember().getUser().getAsTag()));
    }

    public static String getMemberName(GuildMessageUpdateEvent event) {
        return (event.getMember().getNickname() == null ?
                event.getMember().getUser().getAsTag() : String.format("%s (%s)", event.getMember().getNickname(), event.getMember().getUser().getAsTag()));
    }
}
