package main.java.command.list;

import main.java.util.file.GuildSettingHelper;
import net.dv8tion.jda.api.JDA;
import net.dv8tion.jda.api.entities.Emoji;
import net.dv8tion.jda.api.entities.Guild;
import net.dv8tion.jda.api.entities.MessageEmbed;
import net.dv8tion.jda.api.events.interaction.SlashCommandEvent;
import org.jetbrains.annotations.NotNull;
import org.json.JSONObject;

import java.util.*;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

import static main.java.util.EmbedCreator.createEmbed;
import static main.java.util.GetEmoji.toEmoji;
import static main.java.util.JsonGetter.getOrDefault;
import static main.java.util.JsonKeys.*;
import static main.java.util.TimeFormatter.timeFormat;

public record Giveaway(GuildSettingHelper settingHelper) {


    //         GuildID ChannelID+MessageID EndTimeSec
    static Map<String, Map<String, Long>> giveawayData = new HashMap<>();

    public void newGiveaway(@NotNull SlashCommandEvent event) {
        /**
         * Giveaway
         * /giveaway <name> [month] [week] [day] [hour] [minute] [second] [winnerCount] [emoji]
         */

        Guild guild;
        if ((guild = event.getGuild()) == null)
            return;

        List<MessageEmbed.Field> fields = new ArrayList<>();

        // 名稱
        String giveawayName = Objects.requireNonNull(event.getOption("name")).getAsString();

        // 名稱
        int winnerCount = event.getOption("winnercount") == null ? 1 : (int) event.getOption("winnercount").getAsLong();

        // 表情符號
        String buttonEmojiName = event.getOption("buttonemoji") == null ? null : event.getOption("buttonemoji").getAsString();
        Emoji buttonEmoji = (buttonEmojiName == null ? Emoji.fromUnicode("\uD83C\uDF89") : toEmoji(buttonEmojiName, guild));

        // 時間
        long time = System.currentTimeMillis() / 1000;
        if (event.getOption("month") != null)
            if (event.getOption("month").getAsLong() <= 12) time += event.getOption("month").getAsLong() * 30 * 24 * 60 * 60;
            else fields.add(new MessageEmbed.Field("月份太大", "", false));

        if (event.getOption("week") != null)
            if (event.getOption("week").getAsLong() <= 48) time += event.getOption("week").getAsLong() * 7 * 24 * 60 * 60;
            else fields.add(new MessageEmbed.Field("週數太大", "", false));

        if (event.getOption("day") != null)
            if (event.getOption("day").getAsLong() <= 365) time += event.getOption("day").getAsLong() * 24 * 60 * 60;
            else fields.add(new MessageEmbed.Field("天數太大", "", false));

        if (event.getOption("hour") != null)
            if (event.getOption("hour").getAsLong() <= 8760) time += event.getOption("hour").getAsLong() * 60 * 60;
            else fields.add(new MessageEmbed.Field("小時太大", "", false));

        if (event.getOption("minute") != null)
            if (event.getOption("minute").getAsLong() <= 625200) time += event.getOption("minute").getAsLong() * 60;
            else fields.add(new MessageEmbed.Field("分鐘太大", "", false));

        if (event.getOption("second") != null)
            if (event.getOption("month").getAsLong() <= 31536000) time += event.getOption("second").getAsLong();
            else fields.add(new MessageEmbed.Field("秒數太大", "", false));

        if (fields.size() > 0) {
            event.getHook().editOriginalEmbeds(createEmbed("設定失敗", fields, 0xFF0000)).queue();
            return;
        }


        fields.add(new MessageEmbed.Field("名稱", giveawayName, false));
        fields.add(new MessageEmbed.Field("獲勝人數", String.valueOf(winnerCount), false));
        fields.add(new MessageEmbed.Field("結束時間", timeFormat(time), false));
        fields.add(new MessageEmbed.Field("表情符號", buttonEmoji.getAsMention(), false));


        long finalTime = time;
        event.getTextChannel().sendMessageEmbeds(createEmbed("", "", 0x00FFFF)).queue(i -> {
            JSONObject data = getOrDefault(settingHelper.getSettingData(event.getGuild(), J_SETTING), event.getTextChannel() + i.getId());
            data.put(GIVEAWAY_NAME, giveawayName);
            data.put(GIVEAWAY_WINNER_COUNT, winnerCount);
            data.put(GIVEAWAY_TIME, finalTime);
            data.put(GIVEAWAY_EMOJI, buttonEmoji);
            settingHelper.getGuildSettingManager(event.getGuild().getId()).saveFile();
        });
        event.getHook().editOriginalEmbeds(createEmbed("設定成功", fields, 0x00FFFF)).queue();
    }


    private void onReadyGiveaway(Guild guild) {
        JSONObject data;
        if ((data = settingHelper.getSettingData(guild, GIVEAWAY_SETTING)).length() == 0) {
            System.out.println("[" + guild.getName() + "]  did not have giveaway");
            return;
        }
        Map<String, Long> j;
        if (giveawayData.containsKey(guild.getId()))
            j = giveawayData.get(guild.getId());
        else {
            j = new HashMap<>();
            giveawayData.put(guild.getId(), j);
        }

        data.toMap().forEach((i, k) -> j.put(i, ((JSONObject) k).getLong(GIVEAWAY_TIME)));
    }

    private static ScheduledExecutorService threadPool = Executors.newSingleThreadScheduledExecutor();

    public void minDetectGiveaway(JDA jda) {
        if (threadPool != null && !threadPool.isShutdown())
            threadPool.shutdown();

        threadPool = Executors.newScheduledThreadPool(1);

        // run thread
        long time = System.currentTimeMillis() / 1000;
        threadPool.scheduleWithFixedDelay(() -> {
            giveawayData.keySet().forEach(i -> {
                giveawayData.get(i).keySet().forEach(j -> {
                    if (time - giveawayData.get(i).get(j) <= 61) {
//                        secDetectGiveaway(jda.getGuildById(i), j.substring(),j.substring());
                    }
                });
            });
        }, 0, 1, TimeUnit.MINUTES);
    }

    public void secDetectGiveaway(Guild guild, String channelID, String messageID) {
        if (threadPool != null && !threadPool.isShutdown())
            threadPool.shutdown();

        threadPool = Executors.newScheduledThreadPool(1);

        // run thread
        long time = System.currentTimeMillis() / 1000;
        threadPool.scheduleWithFixedDelay(() -> {
            giveawayData.keySet().forEach(i -> {
                giveawayData.get(i).keySet().forEach(j -> {

                });
            });
        }, 0, 1, TimeUnit.SECONDS);
    }
}