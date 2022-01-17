package com.ice.main.event;

import com.ice.main.util.file.GuildSettingHelper;
import net.dv8tion.jda.api.entities.*;
import net.dv8tion.jda.api.events.guild.GuildReadyEvent;
import net.dv8tion.jda.api.events.guild.voice.GuildVoiceJoinEvent;
import net.dv8tion.jda.api.events.guild.voice.GuildVoiceLeaveEvent;
import net.dv8tion.jda.api.events.guild.voice.GuildVoiceMoveEvent;
import org.json.JSONObject;

import static com.ice.main.util.JsonKeys.AUTO_VC_NAME;
import static com.ice.main.util.JsonKeys.AUTO_VC_SETTING;

public record VoiceChannelCreator(GuildSettingHelper settingHelper) {

    public void onGuildReady(GuildReadyEvent event) {
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
                if (category.getVoiceChannels().size() == 0)
                    category.createVoiceChannel(data.getJSONObject(key).getString(AUTO_VC_NAME)).queue();
                else
                    category.getVoiceChannels().get(0).createCopy().queue();

            }
        }
    }

    public void onGuildVoiceJoin(GuildVoiceJoinEvent event) {
        Guild guild = event.getGuild();
        if (guild.getVoiceChannelById(event.getChannelJoined().getId()) == null)
            return;
        JSONObject data;
        if ((data = settingHelper.getSettingData(guild, AUTO_VC_SETTING)) == null)
            return;
        JSONObject categoryInfo;
        AudioChannel channelJoined = event.getChannelJoined();
        VoiceChannel VCchannelJoined = (VoiceChannel) event.getChannelJoined();
        if (data.has(VCchannelJoined.getParentCategoryId())) {
            categoryInfo = data.getJSONObject(VCchannelJoined.getParentCategoryId());
            if (channelJoined.getName().equals(categoryInfo.getString(AUTO_VC_NAME)) && channelJoined.getMembers().size() == 1)
                guild.createCopyOfChannel(VCchannelJoined).queue();
        }
    }

    public void onGuildVoiceLeave(GuildVoiceLeaveEvent event) {
        if (event.getChannelLeft().getType() != ChannelType.VOICE)
            return;
        VoiceChannel channelLeft = (VoiceChannel) event.getChannelLeft();
        if (channelLeft.getParentCategory() == null) {
            return;
        }
        JSONObject data;
        if ((data = settingHelper.getSettingData(event.getGuild(), AUTO_VC_SETTING)) == null)
            return;
        JSONObject categoryInfo;
        if (data.has(channelLeft.getParentCategoryId())) {
            categoryInfo = data.getJSONObject(channelLeft.getParentCategoryId());
            if (channelLeft.getName().equals(categoryInfo.getString(AUTO_VC_NAME)) && channelLeft.getMembers().size() == 0)
                try {
                    channelLeft.delete().complete();
                } catch (Exception ignored) {
                }
        }
    }

    public void onGuildVoiceMove(GuildVoiceMoveEvent event) {
        if (event.getChannelLeft().getType() != ChannelType.VOICE)
            return;
        if (((VoiceChannel) event.getChannelLeft()).getParentCategory() == null)
            return;

        JSONObject data;
        if ((data = settingHelper.getSettingData(event.getGuild(), AUTO_VC_SETTING)) == null)
            return;
        VoiceChannel channelLeft = (VoiceChannel) event.getChannelLeft();
        VoiceChannel channelJoined = (VoiceChannel) event.getChannelJoined();
        JSONObject categoryInfo;
        if (data.has(channelLeft.getParentCategoryId())) {
            categoryInfo = data.getJSONObject(channelLeft.getParentCategoryId());
            if (channelLeft.getName().equals(categoryInfo.getString(AUTO_VC_NAME)) && channelLeft.getMembers().size() == 0)
                try {
                    channelLeft.delete().complete();
                } catch (Exception ignored) {
                }
        }

        if (channelJoined.getParentCategory() == null)
            return;

        if (data.has(channelJoined.getParentCategoryId())) {
            categoryInfo = data.getJSONObject(channelJoined.getParentCategoryId());
            if (channelJoined.getName().equals(categoryInfo.getString(AUTO_VC_NAME)) && channelJoined.getMembers().size() == 1)
                event.getGuild().createCopyOfChannel(channelJoined).queue();
        }
    }

}