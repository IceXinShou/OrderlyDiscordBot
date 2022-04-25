package com.ice.main.command.list;

import com.ice.main.Main;
import com.ice.main.util.file.GuildSettingHelper;
import net.dv8tion.jda.api.JDA;
import net.dv8tion.jda.api.Permission;
import net.dv8tion.jda.api.entities.Emoji;
import net.dv8tion.jda.api.entities.Guild;
import net.dv8tion.jda.api.entities.Message;
import net.dv8tion.jda.api.entities.MessageEmbed;
import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent;
import org.json.JSONArray;
import org.json.JSONObject;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

import static com.ice.main.lang.LangKey.*;
import static com.ice.main.util.EmbedCreator.createEmbed;
import static com.ice.main.util.GetEmoji.toEmoji;
import static com.ice.main.util.JsonGetter.getOrDefaultArray;
import static com.ice.main.util.JsonKeys.*;
import static com.ice.main.util.Tag.getMemberName;
import static com.ice.main.util.TimeFormatter.millisToOffset;
import static com.ice.main.util.TimeFormatter.timeFormat;

public record Giveaway(GuildSettingHelper settingHelper) {


    //         GuildID  EndTimeSec
    static Map<String, List<Long>> giveawayData = new HashMap<>();

    public void newGiveaway(SlashCommandInteractionEvent event) {
        /**
         * Giveaway
         * /giveaway <name> [month] [week] [day] [hour] [minute] [second] [winnerCount] [emoji]
         */

        List<String> lang = Main.language.getGuildLang(event.getGuild().getId());

        Guild guild;
        if ((guild = event.getGuild()) == null)
            return;

        if (!guild.getSelfMember().getPermissions().contains(Permission.ADMINISTRATOR)) {
            event.getHook().editOriginalEmbeds(createEmbed(lang.get(GIVEAWAY_NEED_ADMIN_PERMISSION), 0xFF0000)).queue();
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
            fields.add(new MessageEmbed.Field(lang.get(GIVEAWAY_SINGLE_DATE_FORMAT), "", false));
        else {

            // 絕對時間 yyyy.MM.dd.HH.mm.ss
            if (event.getOption("time") != null) {
                try {
                    time = new SimpleDateFormat("yyyy.MM.dd HH.mm.ss").parse(event.getOption("time").getAsString()).getTime();
                } catch (ParseException e) {
//                    System.err.println(e.getMessage());
                    fields.add(new MessageEmbed.Field(lang.get(GIVEAWAY_ABSOLUTE_TIME_FORMAT_ERROR), "", false));
                }
                if (time - (1970L * 365 * 24 * 60 * 60) <= 0)
                    fields.add(new MessageEmbed.Field(lang.get(GIVEAWAY_ABSOLUTE_TIME_ERROR), "", false));

            } else {
                // 相對時間
                if (event.getOption("month") != null && event.getOption("month").getAsLong() <= 12)
                    time += event.getOption("month").getAsLong() * 30 * 24 * 60 * 60;
                else
                    fields.add(new MessageEmbed.Field(lang.get(GIVEAWAY_MONTH_OUT_OF_RANGE), "", false));

                if (event.getOption("week") != null && event.getOption("week").getAsLong() <= 48)
                    time += event.getOption("week").getAsLong() * 7 * 24 * 60 * 60;
                else
                    fields.add(new MessageEmbed.Field(lang.get(GIVEAWAY_WEEK_OUT_OF_RANGE), "", false));

                if (event.getOption("day") != null && event.getOption("day").getAsLong() <= 365)
                    time += event.getOption("day").getAsLong() * 24 * 60 * 60;
                else
                    fields.add(new MessageEmbed.Field(lang.get(GIVEAWAY_DAY_OUT_OF_RANGE), "", false));

                if (event.getOption("hour") != null && event.getOption("hour").getAsLong() <= 8760)
                    time += event.getOption("hour").getAsLong() * 60 * 60;
                else
                    fields.add(new MessageEmbed.Field(lang.get(GIVEAWAY_HOUR_OUT_OF_RANGE), "", false));

                if (event.getOption("minute") != null && event.getOption("minute").getAsLong() <= 625200)
                    time += event.getOption("minute").getAsLong() * 60;
                else
                    fields.add(new MessageEmbed.Field(lang.get(GIVEAWAY_MINUTE_OUT_OF_RANGE), "", false));

                if (event.getOption("second") != null && event.getOption("month").getAsLong() <= 31536000)
                    time += event.getOption("second").getAsLong();
                else
                    fields.add(new MessageEmbed.Field(lang.get(GIVEAWAY_SECOND_OUT_OF_RANGE), "", false));
            }
        }
        if (fields.size() > 0) {
            event.getHook().editOriginalEmbeds(createEmbed(lang.get(GIVEAWAY_SETTING_FAILED), fields, 0xFF0000)).queue();
            return;
        }


        fields.add(new MessageEmbed.Field(lang.get(GIVEAWAY_GIFT_NAME), giveawayName, false));
        fields.add(new MessageEmbed.Field(lang.get(GIVEAWAY_LK_WINNER_COUNT), String.valueOf(winnerCount), false));
        fields.add(new MessageEmbed.Field(lang.get(GIVEAWAY_END_TIME), timeFormat(time), false));
        fields.add(new MessageEmbed.Field(lang.get(GIVEAWAY_LK_EMOJI), buttonEmoji.getAsMention(), false));


        long finalTime = time;
        event.getTextChannel().
                sendMessageEmbeds(createEmbed(
                        giveawayName, lang.get(GIVEAWAY_LK_WINNER_COUNT) + ": " + winnerCount + "\n",
                        lang.get(GIVEAWAY_TIME_LEFT), getMemberName(event),
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
        if ((data = settingHelper.getSettingData(guild, GIVEAWAY_SETTING)).length() == 0)
            return;

        List<Long> endTime;
        if (giveawayData.containsKey(guild.getId()))
            endTime = giveawayData.get(guild.getId());
        else {
            endTime = new ArrayList<>();
            giveawayData.put(guild.getId(), endTime);
        }
        for (Object i : data.getJSONArray(GIVEAWAY_ARRAY))
            endTime.add(((JSONObject) i).getLong(GIVEAWAY_TIME));

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
            for (Map.Entry<String, List<Long>> data : giveawayData.entrySet())
                for (int i = 0; i < data.getValue().size(); i++) {
                    long timeLeft = data.getValue().get(i) - time;
                    if (timeLeft <= 61)
                        secDetectGiveaway(jda.getGuildById(data.getKey()), i);
                }
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
                        System.err.println(e.getMessage());
                    }
                }
            }
        });
    }
}