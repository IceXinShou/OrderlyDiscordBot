package main.java.command.list;

import main.java.util.file.GuildSettingHelper;
import net.dv8tion.jda.api.JDA;
import net.dv8tion.jda.api.Permission;
import net.dv8tion.jda.api.entities.Emoji;
import net.dv8tion.jda.api.entities.Guild;
import net.dv8tion.jda.api.entities.Message;
import net.dv8tion.jda.api.entities.MessageEmbed;
import net.dv8tion.jda.api.events.interaction.SlashCommandEvent;
import org.jetbrains.annotations.NotNull;
import org.json.JSONArray;
import org.json.JSONObject;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.*;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

import static main.java.lang.LangKey.*;
import static main.java.util.EmbedCreator.createEmbed;
import static main.java.util.GetEmoji.toEmoji;
import static main.java.util.JsonGetter.getOrDefaultArray;
import static main.java.util.JsonKeys.*;
import static main.java.util.TimeFormatter.millisToOffset;
import static main.java.util.TimeFormatter.timeFormat;

public record Giveaway(GuildSettingHelper settingHelper) {


    //         GuildID  EndTimeSec
    static Map<String, List<Long>> giveawayData = new HashMap<>();

    public void newGiveaway(@NotNull SlashCommandEvent event) {
        /**
         * Giveaway
         * /giveaway <name> [month] [week] [day] [hour] [minute] [second] [winnerCount] [emoji]
         */

        Guild guild;
        if ((guild = event.getGuild()) == null)
            return;

        if (!guild.getSelfMember().getPermissions().contains(Permission.ADMINISTRATOR)) {
            event.getHook().editOriginalEmbeds(createEmbed("請給予管理員權限才能夠執行此操作", 0xFF0000)).queue();
            return;
        }

        List<MessageEmbed.Field> fields = new ArrayList<>();

        // 名稱
        String giveawayName = event.getOption("name").getAsString();

        // 名稱
        int winnerCount = event.getOption("winnercount") == null ? 1 : (int) event.getOption("winnercount").getAsLong();

        // 表情符號
        String buttonEmojiName = event.getOption("buttonemoji") == null ? null : event.getOption("buttonemoji").getAsString();
        Emoji buttonEmoji = (buttonEmojiName == null ? Emoji.fromUnicode("\uD83C\uDF89") : toEmoji(buttonEmojiName, guild));
        long time = System.currentTimeMillis() / 1000;

        if (event.getOption("time") != null && (
                event.getOption("month") != null || event.getOption("week") != null ||
                        event.getOption("day") != null || event.getOption("hour") != null ||
                        event.getOption("minute") != null || event.getOption("second") != null))
            fields.add(new MessageEmbed.Field("只能填寫一種時間格式", "", false));
        else {

            // 絕對時間 yyyy.MM.dd.HH.mm.ss
            if (event.getOption("time") != null) {
                try {
                    time = new SimpleDateFormat("yyyy.MM.dd HH.mm.ss").parse(event.getOption("time").getAsString()).getTime();
                } catch (ParseException e) {
//                    System.err.println(e.getMessage());
                    fields.add(new MessageEmbed.Field("絕對時間格式錯誤", "", false));
                }
                if (time - (1970L * 365 * 24 * 60 * 60) <= 0)
                    fields.add(new MessageEmbed.Field("絕對時間錯誤", "", false));

            } else {
                // 相對時間
                if (event.getOption("month") != null)
                    if (event.getOption("month").

                            getAsLong() <= 12) time += event.getOption("month").

                            getAsLong() * 30 * 24 * 60 * 60;
                    else fields.add(new MessageEmbed.Field("月數太大", "", false));

                if (event.getOption("week") != null)
                    if (event.getOption("week").

                            getAsLong() <= 48) time += event.getOption("week").

                            getAsLong() * 7 * 24 * 60 * 60;
                    else fields.add(new MessageEmbed.Field("週數太大", "", false));

                if (event.getOption("day") != null)
                    if (event.getOption("day").

                            getAsLong() <= 365) time += event.getOption("day").

                            getAsLong() * 24 * 60 * 60;
                    else fields.add(new MessageEmbed.Field("天數太大", "", false));

                if (event.getOption("hour") != null)
                    if (event.getOption("hour").

                            getAsLong() <= 8760) time += event.getOption("hour").

                            getAsLong() * 60 * 60;
                    else fields.add(new MessageEmbed.Field("小時太大", "", false));

                if (event.getOption("minute") != null)
                    if (event.getOption("minute").

                            getAsLong() <= 625200) time += event.getOption("minute").

                            getAsLong() * 60;
                    else fields.add(new MessageEmbed.Field(lang.get(GIVEAWAY_MINUTE_OUT_OF_RANGE), "", false));

                if (event.getOption("second") != null)
                    if (event.getOption("month").

                            getAsLong() <= 31536000) time += event.getOption("second").

                            getAsLong();
                    else fields.add(new MessageEmbed.Field(lang.get(GIVEAWAY_SECOND_OUT_OF_RANGE), "", false));
            }
        }
        if (fields.size() > 0) {
            event.getHook().editOriginalEmbeds(createEmbed("設定失敗", fields, 0xFF0000)).queue();
            return;
        }


        fields.add(new MessageEmbed.Field("禮物名稱", giveawayName, false));
        fields.add(new MessageEmbed.Field("獲勝人數", String.valueOf(winnerCount), false));
        fields.add(new MessageEmbed.Field("結束時間", timeFormat(time), false));
        fields.add(new MessageEmbed.Field("表情符號", buttonEmoji.getAsMention(), false));


        long finalTime = time;
        event.getTextChannel().
                sendMessageEmbeds(createEmbed(
                        giveawayName, lang.get(GIVEAWAY_LK_WINNER_COUNT) + ": " + winnerCount + "\n",
                        lang.get(GIVEAWAY_TIME_LEFT), event.getMember().getNickname() == null ? event.getUser().getAsTag() : event.getMember().getNickname(),
                        event.getUser().getAvatarUrl(), millisToOffset(finalTime), 0x00FFFF)
                )
                .queue(message -> {
                    JSONObject data = new JSONObject();
                    data.put(GIVEAWAY_CHANNEL_ID, event.getTextChannel().getId());
                    data.put(GIVEAWAY_MESSAGE_ID, message.getId());
                    data.put(GIVEAWAY_NAME, giveawayName);
                    data.put(GIVEAWAY_WINNER_COUNT, winnerCount);
                    data.put(GIVEAWAY_TIME, finalTime);
                    data.put(GIVEAWAY_EMOJI, buttonEmoji);
                    JSONArray giveaway = getOrDefaultArray(settingHelper.getSettingData(guild, GIVEAWAY_SETTING), GIVEAWAY_ARRAY);
                    giveaway.put(data);
                    settingHelper.getGuildSettingManager(guild.getId()).saveFile();
                });
        event.getHook().
                editOriginalEmbeds(createEmbed(lang.get(GIVEAWAY_SUCCESS), fields, 0x00FFFF)).
                queue();
    }

    // giveaway:[{channelID:"channelID",messageID:"messageID",N:"GIVEAWAY_NAME",T:"GIVEAWAY_TIME",E:"GIVEAWAY_EMOJI",C:"GIVEAWAY_WINNER_COUNT"}]


    private void onReadyGiveaway(Guild guild) {
        JSONObject data;
        if ((data = settingHelper.getSettingData(guild, GIVEAWAY_SETTING)).length() == 0) {
            return;
        }
        List<Long> endTime;
        if (giveawayData.containsKey(guild.getId()))
            endTime = giveawayData.get(guild.getId());
        else {
            endTime = new ArrayList<>();
            giveawayData.put(guild.getId(), endTime);
        }
        for (Object i : data.getJSONArray(GIVEAWAY_ARRAY)) {
            endTime.add(((JSONObject) i).getLong(GIVEAWAY_TIME));
        }
    }

    private static ScheduledExecutorService minThreadPool = Executors.newSingleThreadScheduledExecutor();
    private static final ExecutorService secThreadPool = Executors.newCachedThreadPool();

    public void minDetectGiveaway(JDA jda) {
        if (minThreadPool != null && !minThreadPool.isShutdown())
            minThreadPool.shutdown();

        minThreadPool = Executors.newScheduledThreadPool(1);

        // run thread
        minThreadPool.scheduleWithFixedDelay(() -> {
            long time = System.currentTimeMillis() / 1000;
            giveawayData.forEach((key, giveaways) -> {
                for (int i = 0; i < giveaways.size(); i++) {
                    long timeLeft = giveaways.get(i) - time;
                    if (timeLeft <= 61) {
                        secDetectGiveaway(jda.getGuildById(key), i);
                    }
                }
            });
        }, 0, 1, TimeUnit.MINUTES);
    }

    public void secDetectGiveaway(Guild guild, int giveawayIndex) {
        JSONObject data = settingHelper.getSettingData(guild, GIVEAWAY_SETTING).getJSONArray(GIVEAWAY_ARRAY).getJSONObject(giveawayIndex);
        secThreadPool.submit(new Runnable() {
            final String channelID = data.getString(GIVEAWAY_CHANNEL_ID);
            final String messageID = data.getString(GIVEAWAY_MESSAGE_ID);
            final Message message = guild.getTextChannelById(channelID).retrieveMessageById(messageID).complete();

            @Override
            public void run() {
                for (long k = 60; k > 0; k -= 5) {
                    message.editMessage(message.getContentRaw()).queue();
                    try {
                        Thread.sleep(5000);
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }
                }
            }
        });
    }
}