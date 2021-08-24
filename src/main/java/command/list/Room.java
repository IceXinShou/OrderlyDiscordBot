package main.java.command.list;

import main.java.util.file.GuildSettingHelper;
import net.dv8tion.jda.api.Permission;
import net.dv8tion.jda.api.entities.Category;
import net.dv8tion.jda.api.entities.GuildChannel;
import net.dv8tion.jda.api.events.guild.voice.GenericGuildVoiceUpdateEvent;
import net.dv8tion.jda.api.events.guild.voice.GuildVoiceJoinEvent;
import net.dv8tion.jda.api.events.guild.voice.GuildVoiceLeaveEvent;
import net.dv8tion.jda.api.events.guild.voice.GuildVoiceMoveEvent;
import org.jetbrains.annotations.NotNull;
import org.json.JSONObject;

import java.util.*;

import static main.java.util.JsonKeys.*;
import static main.java.util.PlaceholderReplacer.placeholderReplacer;

public record Room(GuildSettingHelper settingHelper) {

    //             GuildID     MemberID   ChannelIDs(Voice, Text)
    public static Map<String, Map<String, List<String>>> voiceState = new HashMap<>();


    public void onGuildVoiceJoin(@NotNull GuildVoiceJoinEvent event) {
        JSONObject data;
        if ((data = settingHelper.getSettingData(event.getGuild(), ROOM_SETTING)) == null)
            return;
        if (data.has(event.getChannelJoined().getId()))
            newChannel(event, data);
    }

    public void onGuildVoiceLeave(@NotNull GuildVoiceLeaveEvent event) {
        Map<String, List<String>> membersID = voiceState.get(event.getGuild().getId());
        if (membersID != null) {
            String memberID = event.getMember().getId();
            if (membersID.containsKey(memberID)) {
                if (membersID.get(memberID).get(0).equals(event.getChannelLeft().getId())) {
                    event.getChannelLeft().delete().queue();
                    if (membersID.get(memberID).size() > 1) {
                        GuildChannel removedChannel;
                        if ((removedChannel = event.getGuild().getTextChannelById(membersID.get(memberID).get(1))) != null)
                            removedChannel.delete().queue();
                    }
                    membersID.remove(memberID);
                }
            }
        }
    }

    public void onGuildVoiceMove(@NotNull GuildVoiceMoveEvent event) {
        JSONObject data;
        if ((data = settingHelper.getSettingData(event.getGuild(), ROOM_SETTING)) == null)
            return;
        Map<String, List<String>> membersID = voiceState.get(event.getGuild().getId());
        if (membersID != null) {
            String memberID = event.getMember().getId();
            if (membersID.containsKey(memberID))
                if (membersID.get(memberID).get(0).equals(event.getChannelLeft().getId())) {
                    event.getChannelLeft().delete().queue();
                    if (membersID.get(memberID).size() > 1) {
                        GuildChannel removedChannel;
                        if ((removedChannel = event.getGuild().getTextChannelById(membersID.get(memberID).get(1))) != null)
                            removedChannel.delete().queue();
                    }
                    membersID.remove(memberID);
                }
        }
        if (data.has(event.getChannelJoined().getId()))
            newChannel(event, data);
    }

    /**
     * Funtion
     */

    private void newChannel(@NotNull GenericGuildVoiceUpdateEvent event, @NotNull JSONObject inputData) {
        Collection<Permission> allow = new ArrayList<>();
        allow.add(Permission.VIEW_CHANNEL);
        allow.add(Permission.MANAGE_CHANNEL);

        JSONObject data = inputData.getJSONObject(event.getChannelJoined().getId());
        final Category textCategory;
        Category voiceCategory = event.getGuild().getCategoryById(data.getString(ROOM_VOICE_CATEGORY_ID));
        boolean hasTextChannel;
        if (data.has(ROOM_TEXT_CATEGORY_ID)) {
            hasTextChannel = true;
            textCategory = event.getGuild().getCategoryById(data.getString(ROOM_TEXT_CATEGORY_ID));
        } else {
            hasTextChannel = false;
            textCategory = null;
        }

        if (voiceCategory == null || (hasTextChannel && textCategory == null)) {
            inputData.remove(event.getChannelJoined().getId());
            return;
        }

        event.getGuild().createVoiceChannel(
                placeholderReplacer(data.getString(ROOM_VOICE_NAME), event.getMember()),
                voiceCategory
        ).setBitrate(
                data.getInt(ROOM_VOICE_BITRATE) * 1000
        ).queue(nvc -> {
            if (data.has(ROOM_VOICE_MEMBER_LIMIT))
                nvc.getManager().setUserLimit(data.getInt(ROOM_VOICE_MEMBER_LIMIT)).queue();
            nvc.createPermissionOverride(event.getMember()).setAllow(allow).queue();
            event.getGuild().moveVoiceMember(event.getMember(), nvc).queue();
            Map<String, List<String>> map = voiceState.get(event.getGuild().getId());
            List<String> channels = new ArrayList<>(List.of(nvc.getId()));
            if (map == null)
                voiceState.put(event.getGuild().getId(), new HashMap<>() {{
                    put(event.getMember().getId(), channels);
                }});
            else
                map.put(event.getMember().getId(), channels);

            if (hasTextChannel)
                // 創建專屬文字頻道
                event.getGuild().createTextChannel(
                        placeholderReplacer(data.getString(ROOM_TEXT_NAME), event.getMember()),
                        textCategory
                ).queue(ntc -> {
                    ntc.createPermissionOverride(event.getMember()).setAllow(allow).queue();
                    channels.add(ntc.getId());
                });
        });
    }
}