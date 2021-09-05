package main.java.event;

import main.java.util.file.GuildSettingHelper;
import net.dv8tion.jda.api.entities.Category;
import net.dv8tion.jda.api.entities.Guild;
import net.dv8tion.jda.api.entities.VoiceChannel;
import net.dv8tion.jda.api.events.guild.GuildReadyEvent;
import net.dv8tion.jda.api.events.guild.voice.GuildVoiceJoinEvent;
import net.dv8tion.jda.api.events.guild.voice.GuildVoiceLeaveEvent;
import net.dv8tion.jda.api.events.guild.voice.GuildVoiceMoveEvent;
import org.jetbrains.annotations.NotNull;
import org.json.JSONObject;

import static main.java.util.JsonKeys.AUTO_VC_NAME;
import static main.java.util.JsonKeys.AUTO_VC_SETTING;

public record VoiceChannelCreator(GuildSettingHelper settingHelper) {

    public void onGuildReady(@NotNull GuildReadyEvent event) {
        JSONObject data;
        if ((data = settingHelper.getSettingData(event.getGuild(), AUTO_VC_SETTING)) == null)
            return;
        for (String key : data.keySet()) {
            boolean has = false;
            Category category;
            if ((category = event.getGuild().getCategoryById(key)) == null) {
                data.remove(key);
                settingHelper.getGuildSettingManager(event.getGuild().getId()).saveFile();
                continue;
            }
            for (VoiceChannel channel : category.getVoiceChannels()) { // {[autoVC:{"1465416512":{n:"語音頻道"},c2:{n:"語音頻道"}}]}
                if (channel.getMembers().size() == 0 && channel.getName().equals(data.getJSONObject(key).getString(AUTO_VC_NAME)))
                    if (has)
                        channel.delete().queue();
                    else
                        has = true;
            }
            if (!has) {
                if (category.getVoiceChannels().size() == 0) {
                    category.createVoiceChannel(data.getJSONObject(key).getString(AUTO_VC_NAME)).queue();
                } else {
                    category.getVoiceChannels().get(0).createCopy().queue();
                }
            }
        }
    }

    public void onGuildVoiceJoin(@NotNull GuildVoiceJoinEvent event) {
        Guild guild = event.getGuild();
        if (event.getChannelJoined().getParent() == null)
            return;
        JSONObject data;
        if ((data = settingHelper.getSettingData(guild, AUTO_VC_SETTING)) == null)
            return;
        JSONObject categoryInfo;
        VoiceChannel channelJoined = event.getChannelJoined();
        if (data.has(channelJoined.getParent().getId())) {
            categoryInfo = data.getJSONObject(channelJoined.getParent().getId());
            if (channelJoined.getName().equals(categoryInfo.getString(AUTO_VC_NAME)) && channelJoined.getMembers().size() == 1) {
                guild.createCopyOfChannel(channelJoined).queue();
            }
        }
    }

    public void onGuildVoiceLeave(@NotNull GuildVoiceLeaveEvent event) {
        if (event.getChannelLeft().getParent() == null)
            return;
        JSONObject data;
        if ((data = settingHelper.getSettingData(event.getGuild(), AUTO_VC_SETTING)) == null)
            return;
        VoiceChannel channelLeft = event.getChannelLeft();
        JSONObject categoryInfo;
        if (data.has(channelLeft.getParent().getId())) {
            categoryInfo = data.getJSONObject(channelLeft.getParent().getId());
            if (channelLeft.getName().equals(categoryInfo.getString(AUTO_VC_NAME)) && channelLeft.getMembers().size() == 0) {
                try {
                    channelLeft.delete().complete();
                } catch (Exception ignored) {
                }
            }
        }
    }

    public void onGuildVoiceMove(@NotNull GuildVoiceMoveEvent event) {
        if (event.getChannelLeft().getParent() == null)
            return;

        JSONObject data;
        if ((data = settingHelper.getSettingData(event.getGuild(), AUTO_VC_SETTING)) == null)
            return;
        VoiceChannel channelLeft = event.getChannelLeft();
        VoiceChannel channelJoined = event.getChannelJoined();
        JSONObject categoryInfo;
        if (data.has(channelLeft.getParent().getId())) {
            categoryInfo = data.getJSONObject(channelLeft.getParent().getId());
            if (channelLeft.getName().equals(categoryInfo.getString(AUTO_VC_NAME)) && channelLeft.getMembers().size() == 0)
                try {
                    channelLeft.delete().complete();
                } catch (Exception ignored) {
                }
        }

        if (event.getChannelJoined().getParent() == null)
            return;

        if (data.has(channelJoined.getParent().getId())) {
            categoryInfo = data.getJSONObject(event.getChannelJoined().getParent().getId());
            if (channelJoined.getName().equals(categoryInfo.getString(AUTO_VC_NAME)) && channelJoined.getMembers().size() == 1)
                event.getGuild().createCopyOfChannel(channelJoined).queue();
        }
    }

}