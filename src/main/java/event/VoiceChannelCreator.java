package main.java.event;

import main.java.util.file.GuildSettingHelper;
import net.dv8tion.jda.api.entities.Guild;
import net.dv8tion.jda.api.entities.VoiceChannel;
import net.dv8tion.jda.api.events.guild.GuildReadyEvent;
import net.dv8tion.jda.api.events.guild.voice.GuildVoiceJoinEvent;
import net.dv8tion.jda.api.events.guild.voice.GuildVoiceLeaveEvent;
import net.dv8tion.jda.api.events.guild.voice.GuildVoiceMoveEvent;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.json.JSONObject;

import static main.java.util.JsonKeys.AUTO_VC_NAME;
import static main.java.util.JsonKeys.AUTO_VC_SETTING;

public class VoiceChannelCreator {

    public void onGuildReady(@NotNull GuildReadyEvent event, GuildSettingHelper settingHelper) {
        JSONObject data;
        if ((data = getSettingData(event.getGuild(), settingHelper)) == null)
            return;
        boolean has = false;
        for (String key : data.keySet()) {
            if (event.getGuild().getCategoryById(key) == null) {
                data.remove(key);
                settingHelper.getGuildSettingManager(event.getGuild().getId()).saveFile();
                continue;
            }
            for (VoiceChannel channel : event.getGuild().getCategoryById(key).getVoiceChannels()) { // {[autoVC:{"1465416512":{n:"語音頻道"},c2:{n:"語音頻道"}}]}
                if (channel.getMembers().size() == 0 && channel.getName().equals(data.getJSONObject(key).getString(AUTO_VC_NAME)))
                    if (has)
                        channel.delete().queue();
                    else
                        has = true;
            }
        }
    }

    public void onGuildVoiceJoin(@NotNull GuildVoiceJoinEvent event, @NotNull GuildSettingHelper settingHelper) {
        if (event.getChannelJoined() == null || event.getChannelJoined().getParent() == null)
            return;
        JSONObject data;
        if ((data = getSettingData(event.getGuild(), settingHelper)) == null)
            return;
        JSONObject categoryInfo;
        VoiceChannel channelJoined = event.getChannelJoined();
        if (data.has(channelJoined.getParent().getId())) {
            categoryInfo = data.getJSONObject(channelJoined.getParent().getId());
            if (channelJoined.getName().equals(categoryInfo.getString(AUTO_VC_NAME)) && channelJoined.getMembers().size() == 1) {
                event.getGuild().createCopyOfChannel(channelJoined).queue();
            }
        }
    }

    public void onGuildVoiceLeave(@NotNull GuildVoiceLeaveEvent event, GuildSettingHelper settingHelper) {
        if (event.getChannelLeft() == null || event.getChannelLeft().getParent() == null)
            return;
        JSONObject data;
        if ((data = getSettingData(event.getGuild(), settingHelper)) == null)
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

    public void onGuildVoiceMove(@NotNull GuildVoiceMoveEvent event, GuildSettingHelper settingHelper) {
        if (event.getChannelLeft() == null || event.getChannelLeft().getParent() == null)
            return;

        JSONObject data;
        if ((data = getSettingData(event.getGuild(), settingHelper)) == null)
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

        if (event.getChannelJoined() == null || event.getChannelJoined().getParent() == null)
            return;

        if (data.has(channelJoined.getParent().getId())) {
            categoryInfo = data.getJSONObject(event.getChannelJoined().getParent().getId());
            if (channelJoined.getName().equals(categoryInfo.getString(AUTO_VC_NAME)) && channelJoined.getMembers().size() == 1)
                event.getGuild().createCopyOfChannel(channelJoined).queue();
        }
    }

    private @Nullable JSONObject getSettingData(@NotNull Guild guild, @NotNull GuildSettingHelper settingHelper) {
        if (settingHelper.getGuildSettingManager(guild.getId()).data.has(AUTO_VC_SETTING))
            return settingHelper.getGuildSettingManager(guild.getId()).data.getJSONObject(AUTO_VC_SETTING);
        else {
            return null;
        }
    }
}