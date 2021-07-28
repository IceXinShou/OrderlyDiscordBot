package main.java.automatic;

import net.dv8tion.jda.api.Permission;
import net.dv8tion.jda.api.entities.Category;
import net.dv8tion.jda.api.events.guild.voice.GenericGuildVoiceUpdateEvent;
import net.dv8tion.jda.api.events.guild.voice.GuildVoiceJoinEvent;
import net.dv8tion.jda.api.events.guild.voice.GuildVoiceLeaveEvent;
import net.dv8tion.jda.api.events.guild.voice.GuildVoiceMoveEvent;
import net.dv8tion.jda.api.hooks.ListenerAdapter;
import org.jetbrains.annotations.NotNull;

import java.util.*;

import static main.java.BotSetting.*;
import static main.java.event.Join.memberData;
import static main.java.util.GuildUtil.guild;
import static main.java.util.GuildUtil.guildID;
import static main.java.util.JsonKeys.CHINESE_NICK;

public class Room extends ListenerAdapter {
    Map<String, List<String>> voiceState = new HashMap<>();

    @Override
    public void onGuildVoiceJoin(@NotNull GuildVoiceJoinEvent event) {
        if (!event.getGuild().getId().equals(guildID))
            return;
        if (event.getChannelJoined().getId().equals("869925166522716211"))
            newChannel(event);
    }


    @Override
    public void onGuildVoiceLeave(@NotNull GuildVoiceLeaveEvent event) {
        if (!event.getGuild().getId().equals(guildID))
            return;
        if (voiceState.containsKey(event.getMember().getId()))
            if (voiceState.get(event.getMember().getId()).get(0).equals(event.getChannelLeft().getId())) {
                event.getChannelLeft().delete().queue();
                event.getGuild().getTextChannelById(voiceState.get(event.getMember().getId()).get(1)).delete().queue();
                voiceState.remove(event.getMember().getId());
            }
    }


    @Override
    public void onGuildVoiceMove(@NotNull GuildVoiceMoveEvent event) {
        if (!event.getGuild().getId().equals(guildID))
            return;
        if (event.getChannelJoined().getId().equals("869925166522716211"))
            newChannel(event);
        if (voiceState.containsKey(event.getMember().getId()))
            if (voiceState.get(event.getMember().getId()).get(0).equals(event.getChannelLeft().getId())) {
                event.getChannelLeft().delete().queue();
                event.getGuild().getTextChannelById(voiceState.get(event.getMember().getId()).get(1)).delete().queue();
                voiceState.remove(event.getMember().getId());
            }
    }

    /**
     * Funtion
     */

    private void newChannel(GenericGuildVoiceUpdateEvent event) {
        voiceState.put(event.getMember().getId(), null);
        Collection<Permission> allow = new ArrayList<>();
        allow.add(Permission.VIEW_CHANNEL);
        allow.add(Permission.MANAGE_CHANNEL);

        Category category = event.getChannelJoined().getParent();

        guild.createVoiceChannel(defaultRoomName.replace("%name%", memberData.getJSONObject(event.getMember().getId()).getString(CHINESE_NICK)), category).setBitrate(roomBitrate)
                .queue(nvc -> {
                    guild.moveVoiceMember(event.getMember(), nvc).queue();
                    // 創建專屬文字頻道
                    guild.createTextChannel(defaultRoomChatName.replace("%name%", memberData.getJSONObject(event.getMember().getId()).getString(CHINESE_NICK)), category)
                            .queue(ntc -> {
                                nvc.createPermissionOverride(event.getMember()).setAllow(allow).queue();
                                ntc.createPermissionOverride(event.getMember()).setAllow(allow).queue();
                                voiceState.put(event.getMember().getId(), List.of(nvc.getId(), ntc.getId()));
                            });
                });
    }
}