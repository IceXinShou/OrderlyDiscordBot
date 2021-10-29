package main.event;

import main.util.StringCalculate;
import main.util.file.GuildSettingHelper;
import net.dv8tion.jda.api.JDA;
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

import static main.util.JsonKeys.*;

public class StatusListener {
    private final String TAG = "[StatusListener] ";
    private final GuildSettingHelper settingHelper;
    //                guildID    typeName  value
    private final Map<String, Map<String, Integer>> guildsMemberStatus = new HashMap<>();
    //              channelID lastTime
    private final Map<String, Long> channelTimer = new HashMap<>();
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
                int wait = (40 * 1000 / guildsMemberStatus.size());
                for (String guildID : guildsMemberStatus.keySet()) {
                    updateGuild(jda.getGuildById(guildID));
                    Thread.sleep(wait);
                }
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }, 10, 60, TimeUnit.SECONDS);
    }

    public void onGuildReady(Guild guild) {
        if (settingHelper.getSettingData(guild, CS_SETTING).length() > 0)
            guildsMemberStatus.put(guild.getId(), null);
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
//        System.out.println("Update Guild " + guild.getName());
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
                    if ((memberStatus = guildsMemberStatus.get(guild.getId())) != null) {
                        // User Status
                        if (memberStatus.get("member_noBot") != member_noBot)
                            memberStatus.put("member_noBots", member_noBot);
                        if (memberStatus.get("member") != member)
                            memberStatus.put("member", member);
                        if (memberStatus.get("online_noBot") != online_noBot)
                            memberStatus.put("online_noBot", online_noBot);
                        if (memberStatus.get("online") != online)
                            memberStatus.put("online", online);
                        if (memberStatus.get("offline_noBot") != offline_noBot)
                            memberStatus.put("offline_noBot", offline_noBot);
                        if (memberStatus.get("offline") != offline)
                            memberStatus.put("offline", offline);
                        if (memberStatus.get("idle_noBot") != idle_noBot)
                            memberStatus.put("idle_noBot", idle_noBot);
                        if (memberStatus.get("idle") != idle)
                            memberStatus.put("idle", idle);
                        if (memberStatus.get("dnd_noBot") != dnd_noBot)
                            memberStatus.put("dnd_noBot", dnd_noBot);
                        if (memberStatus.get("dnd") != dnd)
                            memberStatus.put("dnd", dnd);

                        // Voice Status
                        if (memberStatus.get("inVoiceChannel_noBot") != inVoiceChannel_noBot)
                            memberStatus.put("inVoiceChannel_noBot", inVoiceChannel_noBot);
                        if (memberStatus.get("inVoiceChannel") != inVoiceChannel)
                            memberStatus.put("inVoiceChannel", inVoiceChannel);
                        if (memberStatus.get("notInVoiceChannel_noBot") != member_noBot - inVoiceChannel_noBot)
                            memberStatus.put("notInVoiceChannel_noBot", notInVoiceChannel_noBot);
                        if (memberStatus.get("notInVoiceChannel") != member - inVoiceChannel)
                            memberStatus.put("notInVoiceChannel", notInVoiceChannel);
                        if (memberStatus.get("stream") != stream)
                            memberStatus.put("stream", stream);
                        if (memberStatus.get("camera") != camera)
                            memberStatus.put("camera", camera);

                        // Playing Status
                        if (memberStatus.get("playMinecraft") != playMinecraft)
                            memberStatus.put("playMinecraft", playMinecraft);
                    } else {
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
                    updateGuildChannelStatus(guild, memberStatus);
                });
    }

    private void updateGuildChannelStatus(Guild guild, Map<String, Integer> memberStatus) {
        JSONObject guildData = settingHelper.getSettingData(guild, CS_SETTING);
        if (guildData.length() == 0)
            return;
//        System.out.println(guildData);
        for (String channelID : guildData.keySet()) {
            Long lastTime;
            int minChangeDelay = (int) (5.1f * 60 * 1000);
            if ((lastTime = channelTimer.get(channelID)) == null || System.currentTimeMillis() - lastTime > minChangeDelay) {
                GuildChannel channel;
                if ((channel = guild.getGuildChannelById(channelID)) == null) {
                    guildData.remove(channelID);
                    guildChannelChange.remove(channelID);
//                settingHelper.getGuildSettingManager(guild.getId()).saveFile();
                } else {
                    JSONObject data = guildData.getJSONObject(channelID);
                    String newName = data.getString(CS_NAME);
                    newName = guildStatusReplace(newName, data.getString(CS_FORMAT), guildChannelChange.getOrDefault(channelID, 65535), memberStatus);
                    if (!channel.getName().equals(newName)) {
                        channelTimer.put(channelID, System.currentTimeMillis());
                        channel.getManager().setName(newName).queue();
                    }
                }
            }
        }
    }

    //replace
    private String guildStatusReplace(String input, String format, int change, Map<String, Integer> memberStatus) {
        String result = input;
        for (int i = 0; i < keys.length; i++)
            if ((change & (1 << i)) > 0)
                result = result.replace('%' + keys[i] + '%', memberStatus.get(keys[i]).toString());

        StringCalculate calculate = new StringCalculate();
        result = calculate.processes(result, format);
        if (calculate.haveError())
            return input;
        return result;
    }

    public StringCalculate testInput(Guild guild, String channelID, String input, String format) {
        Map<String, Integer> memberStatus = guildsMemberStatus.get(guild.getId());
        if (memberStatus == null)
            updateGuild(guild);
        int needChange = 0;
        for (int i = 0; i < keys.length; i++) {
            if (input.contains('%' + keys[i] + '%')) {
                input = input.replace('%' + keys[i] + '%', memberStatus.get(keys[i]).toString());
                needChange += 1 << i;
            }
        }
        StringCalculate calculate = new StringCalculate();
        calculate.processes(input, format);
        if (!calculate.haveError())
            guildChannelChange.put(channelID, needChange);

        return calculate;
    }

    //event
    public void memberVoiceLeave(Member member) {
        Map<String, Integer> memberStatus = guildsMemberStatus.get(member.getGuild().getId());
        if (memberStatus == null)
            return;
        memberStatus.merge("inVoiceChannel", -1, Integer::sum);
        memberStatus.merge("notInVoiceChannel", -1, Integer::sum);
        if (!member.getUser().isBot()) {
            memberStatus.merge("inVoiceChannel_noBot", -1, Integer::sum);
            memberStatus.merge("notInVoiceChannel_noBot", -1, Integer::sum);
        }
        updateGuildChannelStatus(member.getGuild(), memberStatus);
    }

    public void memberVoiceJoin(Member member) {
        Map<String, Integer> memberStatus = guildsMemberStatus.get(member.getGuild().getId());
        if (memberStatus == null)
            return;
        memberStatus.merge("inVoiceChannel", 1, Integer::sum);
        memberStatus.merge("notInVoiceChannel", 1, Integer::sum);
        if (!member.getUser().isBot()) {
            memberStatus.merge("inVoiceChannel_noBot", 1, Integer::sum);
            memberStatus.merge("notInVoiceChannel_noBot", 1, Integer::sum);
        }
        updateGuildChannelStatus(member.getGuild(), memberStatus);
    }

    public void memberVoiceStream(Member member) {
        Map<String, Integer> memberStatus = guildsMemberStatus.get(member.getGuild().getId());
        if (memberStatus == null)
            return;
        byte n = 1;
        if (!member.getVoiceState().isStream())
            n = -1;

        memberStatus.merge("stream", (int) n, Integer::sum);

        updateGuildChannelStatus(member.getGuild(), memberStatus);
    }

    public void memberVoiceVideo(Member member) {
        Map<String, Integer> memberStatus = guildsMemberStatus.get(member.getGuild().getId());
        if (memberStatus == null)
            return;
        byte n = 1;
        if (!member.getVoiceState().isSendingVideo())
            n = -1;

        memberStatus.merge("camera", (int) n, Integer::sum);

        updateGuildChannelStatus(member.getGuild(), memberStatus);
    }

    public void memberJoin(Member member) {
        Map<String, Integer> memberStatus = guildsMemberStatus.get(member.getGuild().getId());
        if (memberStatus == null)
            return;
        memberStatus.merge("member", 1, Integer::sum);
        memberStatus.merge(member.getOnlineStatus().name(), 1, Integer::sum);

        if (!member.getUser().isBot())
            memberStatus.merge("member_noBot", 1, Integer::sum);


        updateGuildChannelStatus(member.getGuild(), memberStatus);
    }

    public void memberLeave(GuildMemberRemoveEvent event) {
        if (settingHelper.getSettingData(event.getGuild(), CS_SETTING) == null)
            return;
        updateGuild(event.getGuild());
    }

    public void statusChange(UserUpdateOnlineStatusEvent evnet) {
        Map<String, Integer> memberStatus = guildsMemberStatus.get(evnet.getGuild().getId());
        if (memberStatus == null)
            return;
        if (memberStatus == null)
            memberStatus.merge(evnet.getOldOnlineStatus().name(), -1, Integer::sum); // 2
        memberStatus.merge(evnet.getNewOnlineStatus().name(), 1, Integer::sum); // 2
        if (!evnet.getUser().isBot()) {
            memberStatus.merge(evnet.getOldOnlineStatus().name() + "_noBot", -1, Integer::sum);
            memberStatus.merge(evnet.getNewOnlineStatus().name() + "_noBot", 1, Integer::sum);
        }

        updateGuildChannelStatus(evnet.getGuild(), memberStatus);
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