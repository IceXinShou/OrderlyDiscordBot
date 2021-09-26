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
import org.json.JSONObject;

import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

import static main.java.util.JsonKeys.*;

public class StatusListener {
    private final String TAG = "[StatusListener] ";
    private final GuildSettingHelper settingHelper;
    //                guildID    typeName  value
    private final Map<String, Map<String, Integer>> guildsMemberStatus = new HashMap<>();
    //              channelID lastTime
    private final Map<String, Long> guildChannelTimer = new HashMap<>();
    private final Map<String, Integer> guildChannelChange = new HashMap<>();

    public StatusListener(GuildSettingHelper settingHelper) {
        this.settingHelper = settingHelper;
    }

    private ScheduledExecutorService threadPool = Executors.newSingleThreadScheduledExecutor();

    public void startListen(JDA jda) {
        if (threadPool != null && !threadPool.isShutdown())
            threadPool.shutdown();
        threadPool = Executors.newScheduledThreadPool(1);

        // run thread
        threadPool.scheduleWithFixedDelay(() -> {
            try {
                for (String guildID : guildsMemberStatus.keySet()) {
                    updateGuild(jda.getGuildById(guildID));
                    Thread.sleep(1000);
                }
            } catch (InterruptedException e) {
                System.err.println(TAG + " " + e.getMessage());
            }
        }, 0, 3, TimeUnit.MINUTES);
    }

    public void onGuildReady(Guild guild) {
        if (settingHelper.getSettingData(guild, CS_SETTING) != null)
            updateGuild(guild);
    }

    private final String[] keys = new String[]{
            "member_noBot",
            "member",
            "online_noBot",
            "online",
            "offline_noBot",
            "offline",
            "idle_noBot",
            "idle",
            "dnd_noBot",
            "dnd",
            "inVoiceChannel_noBot",
            "inVoiceChannel",
            "notInVoiceChannel_noBot",
            "notInVoiceChannel",
            "stream",
            "camera",
            "playMinecraft",
    };

    public void updateGuild(Guild guild) {
        updateGuild(guild, false);
    }

    public void updateGuild(Guild guild, boolean useSetting) {
        guild.loadMembers()
                .onError(error -> System.err.println(TAG + " " + error.getMessage()))
                .onSuccess(members -> {
                    int member_noBot = 0;
                    int member = members.size();
                    int online_noBot = 0;
                    int online = 0;
                    int offline_noBot = 0;
                    int offline = 0;
                    int idle_noBot = 0;
                    int idle = 0;
                    int dnd_noBot = 0;
                    int dnd = 0;
                    int inVoiceChannel_noBot = 0;
                    int inVoiceChannel = 0;
                    int notInVoiceChannel_noBot = 0;
                    int notInVoiceChannel = 0;
                    int stream = 0;
                    int camera = 0;
                    int playMinecraft = 0;

                    for (Member gMember : members) {
                        if (!gMember.getUser().isBot()) { // 不包含機器人
                            member_noBot++;

                            // 線上狀態
                            switch (gMember.getOnlineStatus()) {
                                case ONLINE -> online_noBot++;
                                case IDLE -> idle_noBot++;
                                case OFFLINE -> offline_noBot++;
                                case DO_NOT_DISTURB -> dnd_noBot++;
                            }

                            // 通話狀態
                            if (gMember.getVoiceState().inVoiceChannel())
                                inVoiceChannel_noBot++;
                        }

                        if (gMember.getVoiceState().isStream())
                            stream++;

                        if (gMember.getVoiceState().isSendingVideo())
                            camera++;

                        // Play Minecraft
                        for (Activity activity : gMember.getActivities()) {
                            String activityName = activity.getName();
                            if (activityName.equals("Minecraft") ||
                                    activityName.equals("LabyMod") ||
                                    activityName.equals("Lunar Client") ||
                                    activityName.equals("Badlion Client") ||
                                    activityName.equals("Impact") ||
                                    activityName.equals("Aristois") ||
                                    activityName.equals("SkyClient") ||
                                    activityName.contains("FTB") ||
                                    activityName.equals("Skyblock")
                            )
                                playMinecraft++;
                        }

                        // 包含機器人
                        // 線上狀態
                        switch (gMember.getOnlineStatus()) {
                            case ONLINE -> online++;
                            case IDLE -> idle++;
                            case OFFLINE -> offline++;
                            case DO_NOT_DISTURB -> dnd++;
                        }
                        // 通話狀態
                        if (gMember.getVoiceState().inVoiceChannel())
                            inVoiceChannel++;
                    }
                    Map<String, Integer> memberStatus;
                    int change = 0;
                    if ((memberStatus = guildsMemberStatus.get(guild.getId())) != null) {
                        // User Status
                        if (memberStatus.get("member_noBot") != member_noBot) {
                            change += 1;
                            memberStatus.put("member_noBots", member_noBot);
                        }
                        if (memberStatus.get("member") != member) {
                            change += 1 << 1;
                            memberStatus.put("member", member);
                        }
                        if (memberStatus.get("online_noBot") != online_noBot) {
                            change += 1 << 2;
                            memberStatus.put("online_noBot", online_noBot);
                        }
                        if (memberStatus.get("online") != online) {
                            change += 1 << 3;
                            memberStatus.put("online", online);
                        }
                        if (memberStatus.get("offline_noBot") != offline_noBot) {
                            change += 1 << 4;
                            memberStatus.put("offline_noBot", offline_noBot);
                        }
                        if (memberStatus.get("offline") != offline) {
                            change += 1 << 5;
                            memberStatus.put("offline", offline);
                        }
                        if (memberStatus.get("idle_noBot") != idle_noBot) {
                            change += 1 << 6;
                            memberStatus.put("idle_noBot", idle_noBot);
                        }
                        if (memberStatus.get("idle") != idle) {
                            change += 1 << 7;
                            memberStatus.put("idle", idle);
                        }
                        if (memberStatus.get("dnd_noBot") != dnd_noBot) {
                            change += 1 << 8;
                            memberStatus.put("dnd_noBot", dnd_noBot);
                        }
                        if (memberStatus.get("dnd") != dnd) {
                            change += 1 << 9;
                            memberStatus.put("dnd", dnd);
                        }

                        // Voice Status
                        if (memberStatus.get("inVoiceChannel_noBot") != inVoiceChannel_noBot) {
                            change += 1 << 10;
                            memberStatus.put("inVoiceChannel_noBot", inVoiceChannel_noBot);
                        }
                        if (memberStatus.get("inVoiceChannel") != inVoiceChannel) {
                            change += 1 << 11;
                            memberStatus.put("inVoiceChannel", inVoiceChannel);
                        }
                        if (memberStatus.get("notInVoiceChannel_noBot") != member_noBot - inVoiceChannel_noBot) {
                            change += 1 << 12;
                            memberStatus.put("notInVoiceChannel_noBot", notInVoiceChannel_noBot);
                        }
                        if (memberStatus.get("notInVoiceChannel") != member - inVoiceChannel) {
                            change += 1 << 13;
                            memberStatus.put("notInVoiceChannel", notInVoiceChannel);
                        }
                        if (memberStatus.get("stream") != stream) {
                            change += 1 << 14;
                            memberStatus.put("stream", stream);
                        }
                        if (memberStatus.get("camera") != camera) {
                            change += 1 << 15;
                            memberStatus.put("camera", camera);
                        }

                        // Playing Status
                        if (memberStatus.get("playMinecraft") != playMinecraft) {
                            change += 1 << 16;
                            memberStatus.put("playMinecraft", playMinecraft);
                        }
                    } else {
                        change = 65535;
                        memberStatus = new HashMap<>();
                        memberStatus.put("member_noBot", member_noBot);
                        memberStatus.put("member", member);
                        memberStatus.put("online_noBot", online_noBot);
                        memberStatus.put("online", online);
                        memberStatus.put("offline_noBot", offline_noBot);
                        memberStatus.put("offline", offline);
                        memberStatus.put("idle_noBot", idle_noBot);
                        memberStatus.put("idle", idle);
                        memberStatus.put("dnd_noBot", dnd_noBot);
                        memberStatus.put("dnd", dnd);
                        memberStatus.put("inVoiceChannel_noBot", inVoiceChannel_noBot);
                        memberStatus.put("inVoiceChannel", inVoiceChannel);
                        memberStatus.put("notInVoiceChannel_noBot", notInVoiceChannel_noBot);
                        memberStatus.put("notInVoiceChannel", notInVoiceChannel);
                        memberStatus.put("stream", stream);
                        memberStatus.put("camera", camera);
                        memberStatus.put("playMinecraft", playMinecraft);
                        guildsMemberStatus.put(guild.getId(), memberStatus);
                    }
                    updateChannelStatus(guild, useSetting ? 25535 : change, memberStatus);
                });
    }

    private void updateChannelStatus(Guild guild, int change, Map<String, Integer> memberStatus) {
        JSONObject guildData = settingHelper.getSettingData(guild, CS_SETTING);
        for (String channelID : guildData.keySet()) {
            Long lastTime;
            int minChangeDelay = (int) (5.5f * 60 * 1000);
            if ((lastTime = guildChannelTimer.get(channelID)) == null || System.currentTimeMillis() - lastTime > minChangeDelay) {
                change |= guildChannelChange.getOrDefault(channelID, 0);
                guildChannelChange.put(channelID, 0);

                guildChannelTimer.put(channelID, System.currentTimeMillis());
                GuildChannel channel;
                if ((channel = guild.getGuildChannelById(channelID)) == null) {
                    guildData.remove(channelID);
                    settingHelper.getGuildSettingManager(guild.getId()).saveFile();
                } else {
                    JSONObject data = guildData.getJSONObject(channelID);
                    String newName = data.getString(CS_NAME);
                    newName = guildStatusReplace(newName, data.getString(CS_FORMAT), change, memberStatus);
                    if (!channel.getName().equals(newName))
                        channel.getManager().setName(newName).queue();
                }
            } else
                guildChannelChange.merge(channelID, change, (n1, n2) -> n1 | n2);
        }
    }

    private String guildStatusReplace(String input, String format, int change, Map<String, Integer> memberStatus) {
        String result = input;
        for (int i = 0; i < keys.length; i++) {
            if ((change & 1 << i) > 0 && result.contains('%' + keys[i] + '%'))
                result = result.replace('%' + keys[i] + '%', memberStatus.get(keys[i]).toString());
        }

        if (input.contains("${")) {
            StringCalculate calculate = new StringCalculate();
            input = calculate.processes(input, format);
            if (calculate.haveError())
                return input;
        }
        return result;
    }

    public String replace(String guildID, String input) {
        Map<String, Integer> memberStatus = guildsMemberStatus.get(guildID);
        for (int i = 0; i < keys.length; i++) {
            if (input.contains('%' + keys[i] + '%'))
                input = input.replace('%' + keys[i] + '%', memberStatus.get(keys[i]).toString());
        }
        return input;
    }

    public void memberVoiceLeave(Member member) {
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

    public void memberVoiceJoin(Member member) {
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

    public void memberVoiceStream(Member member) {
        if (settingHelper.getSettingData(member.getGuild(), CS_SETTING) == null)
            return;
        Map<String, Integer> memberStatus = guildsMemberStatus.get(member.getGuild().getId());
        byte n = 1;
        if (!member.getVoiceState().isStream())
            n = -1;

        memberStatus.merge("stream", (int) n, Integer::sum); // 14

        updateChannelStatus(member.getGuild(), 16384, memberStatus);
    }

    public void memberVoiceVideo(Member member) {
        if (settingHelper.getSettingData(member.getGuild(), CS_SETTING) == null)
            return;
        Map<String, Integer> memberStatus = guildsMemberStatus.get(member.getGuild().getId());
        byte n = 1;
        if (!member.getVoiceState().isSendingVideo())
            n = -1;

        memberStatus.merge("camera", (int) n, Integer::sum); // 11

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

    public void memberLeave(GuildMemberRemoveEvent event) {
        if (settingHelper.getSettingData(event.getGuild(), CS_SETTING) == null)
            return;
        updateGuild(event.getGuild());
    }

    public void statusChange(UserUpdateOnlineStatusEvent evnet) {
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
        if (memberStatus == null)
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

    /*
      ONLINE("online"),
      IDLE("idle"),
      DO_NOT_DISTURB("dnd"),
      INVISIBLE("invisible"),
      OFFLINE("offline"),
      UNKNOWN("");
     */
}