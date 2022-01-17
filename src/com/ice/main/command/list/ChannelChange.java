package com.ice.main.command.list;

import com.ice.main.Main;
import net.dv8tion.jda.api.Permission;
import net.dv8tion.jda.api.entities.GuildChannel;
import net.dv8tion.jda.api.events.interaction.SlashCommandEvent;

import java.util.List;

import static com.ice.main.lang.LangKey.*;
import static com.ice.main.util.EmbedCreator.createEmbed;
import static com.ice.main.util.PermissionERROR.hasPermission;

public class ChannelChange {
    public void setName(SlashCommandEvent event) {
        List<String> lang = Main.language.getGuildLang(event.getGuild().getId());
        if (!hasPermission(Permission.MANAGE_CHANNEL, event, true))
            return;
        GuildChannel channel;
        String newName;
        if (event.getOption("channel") != null)
            channel = event.getOption("channel").getAsGuildChannel();
        else
            channel = event.getGuildChannel();
        if ((newName = event.getOption("name").getAsString()).length() > 100) {
            event.getHook().editOriginalEmbeds(createEmbed(lang.get(ChannelChange_TOO_LONG), 0xFF0000)).queue();
            return;
        }

        channel.getManager().setName(newName).queue();
        event.getHook().editOriginalEmbeds(createEmbed(lang.get(ChannelChange_CHANGE_SUCCESSFULLY), 0x00FFFF)).queue();
    }


    public void setBitrate(SlashCommandEvent event) {
        List<String> lang = Main.language.getGuildLang(event.getGuild().getId());
        if (!hasPermission(Permission.MANAGE_CHANNEL, event, true))
            return;
        GuildChannel channel;
        int bitrate;
        if (!(channel = event.getOption("channel").getAsGuildChannel()).getType().isAudio()) {
            event.getHook().editOriginalEmbeds(createEmbed(lang.get(ChannelChange_CHANNEL_TYPE_ERROR), 0xFF0000)).queue();
            return;
        }
        if ((bitrate = (int) event.getOption("bitrate").getAsLong() * 1000) > event.getGuild().getMaxBitrate()) {
            event.getHook().editOriginalEmbeds(createEmbed(lang.get(ChannelChange_BITRATE_ERROR) +
                    " (8 ~ " + event.getGuild().getMaxBitrate() + " kbps", 0xFF0000)).queue();
            return;
        }
        event.getGuild().getVoiceChannelById(channel.getId()).getManager().setBitrate(bitrate).queue();
        event.getHook().editOriginalEmbeds(createEmbed(lang.get(ChannelChange_CHANGE_SUCCESSFULLY), 0x00FFFF)).queue();
    }
}
