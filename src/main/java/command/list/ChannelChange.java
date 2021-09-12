package main.java.command.list;

import net.dv8tion.jda.api.Permission;
import net.dv8tion.jda.api.entities.GuildChannel;
import net.dv8tion.jda.api.events.interaction.SlashCommandEvent;

import static main.java.util.EmbedCreator.createEmbed;
import static main.java.util.PermissionERROR.hasPermission;

public class ChannelChange {
    public void setName(SlashCommandEvent event) {
        if (!hasPermission(Permission.MANAGE_CHANNEL, event, true))
            return;
        GuildChannel channel;
        String newName;
        if (event.getOption("channel") != null)
            channel = event.getOption("channel").getAsGuildChannel();
        else
            channel = event.getGuildChannel();
        if ((newName = event.getOption("name").getAsString()).length() > 100) {
            event.getHook().editOriginalEmbeds(createEmbed("名字長度不能超過 100 字", 0xFF0000)).queue();
            return;
        }

        channel.getManager().setName(newName).queue();
        event.getHook().editOriginalEmbeds(createEmbed("更改完成! ", 0x00FFFF)).queue();
    }


    public void setBitrate(SlashCommandEvent event) {
        if (!hasPermission(Permission.MANAGE_CHANNEL, event, true))
            return;
        GuildChannel channel;
        int bitrate;
        if (!(channel = event.getOption("channel").getAsGuildChannel()).getType().isAudio()) {
            event.getHook().editOriginalEmbeds(createEmbed("頻道類型錯誤", 0xFF0000)).queue();
            return;
        }
        if ((bitrate = (int) event.getOption("bitrate").getAsLong() * 1000) > event.getGuild().getMaxBitrate()) {
            event.getHook().editOriginalEmbeds(createEmbed("位元率不得超過最大上限 (" + event.getGuild().getMaxBitrate() + " kbps", 0xFF0000)).queue();
            return;
        }

        channel.getManager().setBitrate(bitrate).queue();
        event.getHook().editOriginalEmbeds(createEmbed("更改完成! ", 0x00FFFF)).queue();
    }
}
