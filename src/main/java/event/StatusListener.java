package main.java.event;

import main.java.util.StringCalculate;
import main.java.util.file.GuildSettingHelper;
import net.dv8tion.jda.api.JDA;
import net.dv8tion.jda.api.OnlineStatus;
import net.dv8tion.jda.api.entities.Activity;
import net.dv8tion.jda.api.entities.Guild;
import net.dv8tion.jda.api.entities.GuildChannel;
import net.dv8tion.jda.api.entities.Member;
import net.dv8tion.jda.api.events.guild.member.GuildMemberRemoveEvent;
import net.dv8tion.jda.api.events.user.update.UserUpdateOnlineStatusEvent;
import org.jetbrains.annotations.NotNull;
import org.json.JSONObject;

import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

import static main.java.util.JsonKeys.*;

public class StatusListener {
    private final GuildSettingHelper settingHelper;
    //                guildID    typeName  value
    private final Map<String, Map<String, Integer>> guildsMemberStatus = new HashMap<>();

    public StatusListener(GuildSettingHelper settingHelper) {
        this.settingHelper = settingHelper;
    }


    public void updateGuild(@NotNull Guild guild) {
        if (settingHelper.getSettingData(guild, CS_SETTING) == null)
            return;
        guild.loadMembers()
                .onError(error -> System.err.println(error.getMessage()))
                .onSuccess(members -> {
                    Map<String, Integer> memberStatus = new HashMap<>();
                    memberStatus.put("member", 0);
                    memberStatus.put("member_bot", members.size());
                    memberStatus.put("online", 0);
                    memberStatus.put("online_bot", 0);
                    memberStatus.put("offline", 0);
                    memberStatus.put("offline_bot", 0);
                    memberStatus.put("idle", 0);
                    memberStatus.put("idle_bot", 0);
                    memberStatus.put("dnd", 0);
                    memberStatus.put("dnd_bot", 0);
                    memberStatus.put("inVoiceChannel", 0);
                    memberStatus.put("inVoiceChannel_bot", 0);
                    memberStatus.put("stream", 0);
                    memberStatus.put("camera", 0);
                    memberStatus.put("play_minecraft", 0);
                    members.forEach(member -> {
                        if (member.getUser().isBot()) { // 包含機器人
                            // 線上狀態
                            memberStatus.put(member.getOnlineStatus().getKey() + "_bot", memberStatus.get(member.getOnlineStatus().getKey() + "_bot") + 1);

                            // 通話狀態
                            if (member.getVoiceState().inVoiceChannel())
                                memberStatus.put("inVoiceChannel_bot", memberStatus.get("inVoiceChannel_bot") + 1);

                        } else { // 不包含機器人
                            memberStatus.put("member", memberStatus.get("member") + 1);

                            // 線上狀態
                            memberStatus.put(member.getOnlineStatus().getKey(), memberStatus.get(member.getOnlineStatus().getKey()) + 1);
                            memberStatus.put(member.getOnlineStatus().getKey() + "_bot", memberStatus.get(member.getOnlineStatus().getKey() + "_bot") + 1);

                            // 通話狀態
                            if (member.getVoiceState().inVoiceChannel()) {
                                memberStatus.put("inVoiceChannel", memberStatus.get("inVoiceChannel") + 1);
                            }

                            if (member.getVoiceState().isStream())
                                memberStatus.put("stream", memberStatus.get("stream") + 1);

                            if (member.getVoiceState().isSendingVideo())
                                memberStatus.put("camera", memberStatus.get("camera") + 1);

                            // Play Minecraft
                            for (Activity activity : member.getActivities()) {
                                if (activity.getName().equals("Minecraft"))
                                    memberStatus.put("play_minecraft", memberStatus.get("play_minecraft") + 1);
                                else if (activity.getName().equals("LabyMod"))
                                    memberStatus.put("play_minecraft", memberStatus.get("play_minecraft") + 1);
                                else if (activity.getName().equals("Lunar Client"))
                                    memberStatus.put("play_minecraft", memberStatus.get("play_minecraft") + 1);
                                else if (activity.getName().equals("Badlion Client"))
                                    memberStatus.put("play_minecraft", memberStatus.get("play_minecraft") + 1);
                                else if (activity.getName().equals("Impact"))
                                    memberStatus.put("play_minecraft", memberStatus.get("play_minecraft") + 1);
                                else if (activity.getName().equals("Aristois"))
                                    memberStatus.put("play_minecraft", memberStatus.get("play_minecraft") + 1);
                                else if (activity.getName().equals("Skyblock"))
                                    memberStatus.put("play_minecraft", memberStatus.get("play_minecraft") + 1);
                            }
                        }
                    });
                    Map<String, Integer> lastMemberStatus;
                    int change = 0;
                    if ((lastMemberStatus = guildsMemberStatus.get(guild.getId())) != null) {
                        if ((int) lastMemberStatus.get("member") != memberStatus.get("member"))
                            change += 1;
                        if ((int) lastMemberStatus.get("member_bot") != memberStatus.get("member_bot"))
                            change += 1 << 1;
                        if ((int) lastMemberStatus.get("online") != memberStatus.get("online"))
                            change += 1 << 2;
                        if ((int) lastMemberStatus.get("online_bot") != memberStatus.get("online_bot"))
                            change += 1 << 3;
                        if ((int) lastMemberStatus.get("offline") != memberStatus.get("offline"))
                            change += 1 << 4;
                        if ((int) lastMemberStatus.get("offline_bot") != memberStatus.get("offline_bot"))
                            change += 1 << 5;
                        if ((int) lastMemberStatus.get("idle") != memberStatus.get("idle"))
                            change += 1 << 6;
                        if ((int) lastMemberStatus.get("idle_bot") != memberStatus.get("idle_bot"))
                            change += 1 << 7;
                        if ((int) lastMemberStatus.get("dnd") != memberStatus.get("dnd"))
                            change += 1 << 8;
                        if ((int) lastMemberStatus.get("dnd_bot") != memberStatus.get("dnd_bot"))
                            change += 1 << 9;
                        if ((int) lastMemberStatus.get("inVoiceChannel") != memberStatus.get("inVoiceChannel"))
                            change += 1 << 10;
                        if ((int) lastMemberStatus.get("inVoiceChannel_bot") != memberStatus.get("inVoiceChannel_bot"))
                            change += 1 << 11;
                        if ((int) lastMemberStatus.get("member") - lastMemberStatus.get("inVoiceChannel") != memberStatus.get("member") - memberStatus.get("inVoiceChannel"))
                            change += 1 << 12;
                        if ((int) lastMemberStatus.get("member_bot") - lastMemberStatus.get("inVoiceChannel_bot") != memberStatus.get("member_bot") - memberStatus.get("inVoiceChannel_bot"))
                            change += 1 << 13;
                        if ((int) lastMemberStatus.get("stream") != memberStatus.get("stream"))
                            change += 1 << 14;
                        if ((int) lastMemberStatus.get("camera") != memberStatus.get("camera"))
                            change += 1 << 15;
                        if ((int) lastMemberStatus.get("play_minecraft") != memberStatus.get("play_minecraft"))
                            change += 1 << 16;
                    } else {
                        change = 65535;
                    }
                    guildsMemberStatus.put(guild.getId(), memberStatus);
                    updateChannelStatus(guild, change, memberStatus);
                });
    }

    private void updateChannelStatus(Guild guild, int change, Map<String, Integer> memberStatus) {
        JSONObject data = settingHelper.getSettingData(guild, CS_SETTING);
        for (String channelID : data.keySet()) {
            Long lastTime;
            if ((lastTime = guildChannelTimer.get(channelID)) == null || System.currentTimeMillis() - lastTime > minChangeDelay) {
                if (guildChannelChange.containsKey(channelID)) {
                    change = guildChannelChange.get(channelID);
                    guildChannelChange.put(channelID, 0);
                }
                guildChannelTimer.put(channelID, System.currentTimeMillis());
                if (guild.getGuildChannelById(channelID) == null)
                    data.remove(channelID);
                else {
                    String newName = data.getJSONObject(channelID).getString(CS_NAME);
                    newName = guildStatusReplace(newName,
                            data.getJSONObject(channelID).getString(CS_FORMAT),
                            change, memberStatus);
                    GuildChannel channel = guild.getGuildChannelById(channelID);
                    if (!channel.getName().equals(newName))
                        channel.getManager().setName(newName).queue();
                }
            } else
                guildChannelChange.merge(channelID, change, (n1, n2) -> n1 | n2);
        }
    }

    public void memberVoiceLeave(@NotNull Member member) {
        if (settingHelper.getSettingData(member.getGuild(), CS_SETTING) == null)
            return;
        Map<String, Integer> memberStatus = guildsMemberStatus.get(member.getGuild().getId());
        // 11 13
        int change = 10240;
        memberStatus.merge("inVoiceChannel", -1, Integer::sum); // 11
        memberStatus.merge("notInVoiceChannel", -1, Integer::sum); // 13
        if (!member.getUser().isBot()) {
            memberStatus.merge("inVoiceChannel_noBot", -1, Integer::sum); // 10
            memberStatus.merge("notInVoiceChannel_noBot", -1, Integer::sum); // 12
            // 10 12
            change += 5120;
        }
        updateChannelStatus(member.getGuild(), change, memberStatus);
    }

    public void memberVoiceJoin(@NotNull Member member) {
        if (settingHelper.getSettingData(member.getGuild(), CS_SETTING) == null)
            return;
        Map<String, Integer> memberStatus = guildsMemberStatus.get(member.getGuild().getId());
        // 11 13
        int change = 10240;
        memberStatus.merge("inVoiceChannel", 1, Integer::sum); // 11
        memberStatus.merge("notInVoiceChannel", 1, Integer::sum); // 13
        if (!member.getUser().isBot()) {
            memberStatus.merge("inVoiceChannel_noBot", 1, Integer::sum); // 10
            memberStatus.merge("notInVoiceChannel_noBot", 1, Integer::sum); // 12
            // 10 12
            change += 5120;
        }
        updateChannelStatus(member.getGuild(), change, memberStatus);
    }

    public void memberVoiceStream(@NotNull Member member) {
        if (settingHelper.getSettingData(member.getGuild(), CS_SETTING) == null)
            return;
        Map<String, Integer> memberStatus = guildsMemberStatus.get(member.getGuild().getId());
        byte n = 1;
        if (!member.getVoiceState().isStream())
            n = -1;

        memberStatus.merge("stream", 1 * n, Integer::sum); // 14

        updateChannelStatus(member.getGuild(), 16384, memberStatus);
    }

    public void memberVoiceVideo(@NotNull Member member) {
        if (settingHelper.getSettingData(member.getGuild(), CS_SETTING) == null)
            return;
        Map<String, Integer> memberStatus = guildsMemberStatus.get(member.getGuild().getId());
        byte n = 1;
        if (!member.getVoiceState().isSendingVideo())
            n = -1;

        memberStatus.merge("camera", 1 * n, Integer::sum); // 11

        updateChannelStatus(member.getGuild(), 32768, memberStatus);
    }

    public void memberJoin(Member member) {
        if (settingHelper.getSettingData(member.getGuild(), CS_SETTING) == null)
            return;
        Map<String, Integer> memberStatus = guildsMemberStatus.get(member.getGuild().getId());
        // 2
        int change = 4;
        memberStatus.merge("member", 1, Integer::sum); // 2
        memberStatus.merge(member.getOnlineStatus().name(), 1, Integer::sum);
        switch (member.getOnlineStatus()) {
            case ONLINE -> change += 8;
            case IDLE -> change += 128;
            case OFFLINE -> change += 32;
            case DO_NOT_DISTURB -> change += 512;
        }

        if (!member.getUser().isBot()) {
            memberStatus.merge("member_noBot", 1, Integer::sum); // 1

            switch (member.getOnlineStatus()) {
                case ONLINE -> change += 4;
                case IDLE -> change += 64;
                case OFFLINE -> change += 16;
                case DO_NOT_DISTURB -> change += 256;
            }
            // 1
            change += 2;
        }

        updateChannelStatus(member.getGuild(), change, memberStatus);
    }

    public void memberLeave(@NotNull GuildMemberRemoveEvent event) {
        if (settingHelper.getSettingData(event.getGuild(), CS_SETTING) == null)
            return;
        updateGuild(event.getGuild());
    }

    public void statusChange(@NotNull UserUpdateOnlineStatusEvent evnet) {
        if (settingHelper.getSettingData(evnet.getGuild(), CS_SETTING) == null)
            return;
        Map<String, Integer> memberStatus = guildsMemberStatus.get(evnet.getGuild().getId());
        // 2
        int change = 0;
        OnlineStatus before = evnet.getOldOnlineStatus();
        OnlineStatus after = evnet.getNewOnlineStatus();
        switch (before) {
            case ONLINE -> change += 8;
            case IDLE -> change += 128;
            case OFFLINE -> change += 32;
            case DO_NOT_DISTURB -> change += 512;
        }
        switch (after) {
            case ONLINE -> change += 8;
            case IDLE -> change += 128;
            case OFFLINE -> change += 32;
            case DO_NOT_DISTURB -> change += 512;
        }

        memberStatus.merge(evnet.getOldOnlineStatus().name(), -1, Integer::sum); // 2
        memberStatus.merge(evnet.getNewOnlineStatus().name(), 1, Integer::sum); // 2
        if (!evnet.getUser().isBot()) {

            switch (before) {
                case ONLINE -> change += 4;
                case IDLE -> change += 64;
                case OFFLINE -> change += 16;
                case DO_NOT_DISTURB -> change += 256;
            }
            switch (after) {
                case ONLINE -> change += 4;
                case IDLE -> change += 64;
                case OFFLINE -> change += 16;
                case DO_NOT_DISTURB -> change += 256;
            }

            memberStatus.merge(evnet.getOldOnlineStatus().name() + "_noBot", -1, Integer::sum);
            memberStatus.merge(evnet.getNewOnlineStatus().name() + "_noBot", 1, Integer::sum);
        }

        updateChannelStatus(evnet.getGuild(), change, memberStatus);
    }

    /**
     * ONLINE("online"),
     * IDLE("idle"),
     * DO_NOT_DISTURB("dnd"),
     * INVISIBLE("invisible"),
     * OFFLINE("offline"),
     * UNKNOWN("");
     */
}