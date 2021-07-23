package multiBot;

import net.dv8tion.jda.api.JDA;
import net.dv8tion.jda.api.JDABuilder;
import net.dv8tion.jda.api.events.interaction.ButtonClickEvent;
import net.dv8tion.jda.api.events.interaction.GenericInteractionCreateEvent;
import net.dv8tion.jda.api.events.interaction.SelectionMenuEvent;
import net.dv8tion.jda.api.events.interaction.SlashCommandEvent;
import net.dv8tion.jda.api.interactions.commands.OptionMapping;
import net.dv8tion.jda.api.interactions.components.selections.SelectionMenu;
import net.dv8tion.jda.api.requests.GatewayIntent;
import net.dv8tion.jda.api.utils.Compression;
import net.dv8tion.jda.api.utils.cache.CacheFlag;
import org.json.JSONArray;
import org.json.JSONObject;

import javax.security.auth.login.LoginException;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;
import java.util.regex.Pattern;

import static main.java.BotSetting.*;
import static main.java.SlashCommandOption.COUNT;
import static main.java.SlashCommandOption.URL;
import static main.java.util.Funtions.createEmbed;
import static multiBot.music.TrackScheduler.getUrlData;


public class MultiMusicBotManager {
    private static final String TAG = "[MultiMusicBot]";
    private ScheduledExecutorService threadPool;
    private int currentIndex = 0;
    private final MusicBotEvent event = new MusicBotEvent();

    // botID, jda
    private final Map<String, MusicBot> bots = new LinkedHashMap<>();

    public MultiMusicBotManager() {
        for (String token : multiMusicBotTokens)
            newBot(token);
        startChangeActivity();
        System.out.println(TAG + " MultiMusicBot loaded!");
    }

    private int commandState;

    public int onCommand(SlashCommandEvent event) {
        MusicBot bot = (MusicBot) bots.values().toArray()[0];
        commandState = 1;

        switch (event.getName()) {
            // 全域頻道
            case "play":
                if (checkVcState(event)) {
                    OptionMapping url;
                    // 開始撥放
                    if ((url = event.getOption(URL)) == null) {
                        bot.pause(event, event.getGuild(), true);
                        event.replyEmbeds(createEmbed("已開始播放", 0xbde3ae)).setEphemeral(true).queue();
                    } else if (Pattern.matches(".*\\.?youtu\\.?be(\\.com)?/+.*", url.getAsString())) {
                        bot.loadAndPlay(event, url.getAsString());
                    } else {

                        String keyWord = URLEncoder.encode(event.getOption(URL).getAsString(), StandardCharsets.UTF_8);

                        SelectionMenu.Builder builder = SelectionMenu.create(event.getUser().getId() + ":searchResult");

                        JSONObject result = new JSONObject(
                                getUrlData("https://youtube.googleapis.com/youtube/v3/search?part=snippet&maxResults=25&q=" +
                                        keyWord + "&key=" + apiKEY));

                        JSONArray videoInfo = result.getJSONArray("items");

                        for (Object vinfo : videoInfo) {
                            JSONObject snippet = ((JSONObject) vinfo).getJSONObject("snippet");
                            String title = snippet.getString("title");
                            if (title.length() > 25)
                                title = title.substring(0, 24) + "…";

                            builder.addOption(title, ((JSONObject) vinfo).getJSONObject("id").getString("videoId"));
                        }
                        event.reply("搜尋結果").addActionRow(builder.build()).setEphemeral(true).queue();
                    }
                }
                break;
            case "skip":
                if (checkVcState(event))
                    bot.nextTrack(event);
                break;
            case "previous":
                if (checkVcState(event))
                    bot.playPrevious(event);
                break;
            case "repeat":
                if (checkVcState(event))
                    bot.toggleRepeat(event);
                break;
            case "pause":
                if (checkVcState(event))
                    bot.pause(event, event.getGuild(), false);
                break;
            case "queue":
                if (checkVcState(event))
                    bot.queue(event);
                break;
            case "volume":
                if (checkVcState(event)) {
                    Integer volume = null;
                    if (event.getOption(COUNT) != null)
                        if (event.getOption(COUNT).getAsLong() <= 100)
                            volume = (int) event.getOption(COUNT).getAsLong();
                        else {
                            event.replyEmbeds(createEmbed("未知的數值", 0xFF0000)).setEphemeral(true).queue();
                            break;
                        }
                    bot.changeVolume(volume, event.getGuild(), event);
                }
                break;
            default:
                commandState = 0;
        }
        return commandState;
    }

    public void onButton(ButtonClickEvent event, String[] args) {
//        if (!checkVcState(event))
//            return;
//
//        AudioPlayer player = getGuildAudioPlayer(event.getGuild()).player;
//        TrackScheduler scheduler = getGuildAudioPlayer(event.getGuild()).scheduler;
//        int volume;
//        switch (args[1]) {
//            case "musicLoopChange": {
//                switch ((scheduler.loopStatus = (scheduler.loopStatus + 1) % 3)) {
//                    case 0:
//                        scheduler.repeat = false;
//                        scheduler.loop = false;
//                        break;
//                    case 1:
//                        scheduler.repeat = false;
//                        scheduler.loop = true;
//                        break;
//                    case 2:
//                        scheduler.repeat = true;
//                        scheduler.loop = false;
//                        break;
//                    default:
//                        return;
//                }
//                break;
//            }
//            case "musicPause":
//                scheduler.pause(null, false);
//                break;
//            case "nextToPlay":
//                scheduler.nextTrack(null);
//                break;
//            case "musicVolumeUp":
//                volume = player.getVolume() + 5;
//                player.setVolume(Math.min(volume, 100));
//                break;
//            case "musicVolumeDown":
//                volume = player.getVolume() - 5;
//                player.setVolume(Math.max(volume, 0));
//                break;
//            default:
//                return;
//        }
//
//        MessageEmbed[] embed = playStatus(event.getMember(), scheduler);
//        WebhookMessageUpdateAction<Message> message = event.getHook().editOriginalEmbeds(embed[0], embed[1]);
//        message.setActionRows(controlButtons(args[0], scheduler.musicPause, scheduler.loopStatus));
//        message.queue();

    }


    public void onSelectMenu(SelectionMenuEvent event, String[] args) {
//        if (!event.getMember().getVoiceState().inVoiceChannel())
//            event.replyEmbeds(createEmbed("請在語音頻道使用此指令", 0xFF0000));
//        else if (!args[0].equals(event.getUser().getId()))
//            event.replyEmbeds(createEmbed("此為其他成員的觸發項目", 0xFF0000));
//        else loadAndPlay(event, getGuildAudioPlayer(event.getGuild()), "https://youtu.be/" + event.getValues().get(0));
    }

    public boolean checkVcState(GenericInteractionCreateEvent event) {
        if (!event.getMember().getVoiceState().inVoiceChannel()) {
            event.replyEmbeds(createEmbed("請在語音頻道內使用此指令", 0xFF0000)).setEphemeral(true).queue();
            return false;
        } else if (event.getGuild().getSelfMember().getVoiceState().inVoiceChannel() && !event.getMember().getVoiceState().getChannel().equals(event.getGuild().getSelfMember().getVoiceState().getChannel())) {
            event.replyEmbeds(createEmbed("機器人已被其他頻道所使用", 0xFF0000)).setEphemeral(true).queue();
            return false;
        } else return true;
    }

    private void newBot(String botToken) {
        JDABuilder builder = JDABuilder.createDefault(botToken)
                .disableCache(
                        CacheFlag.ACTIVITY,
                        CacheFlag.EMOTE,
                        CacheFlag.CLIENT_STATUS,
                        CacheFlag.MEMBER_OVERRIDES,
                        CacheFlag.ROLE_TAGS,
                        CacheFlag.ONLINE_STATUS)  // Disable ALL of the cache
                .disableIntents(
                        GatewayIntent.GUILD_MEMBERS,
                        GatewayIntent.GUILD_BANS,
                        GatewayIntent.GUILD_EMOJIS,
                        GatewayIntent.GUILD_WEBHOOKS,
                        GatewayIntent.GUILD_INVITES,
                        GatewayIntent.GUILD_PRESENCES,
                        GatewayIntent.GUILD_MESSAGES,
                        GatewayIntent.GUILD_MESSAGE_REACTIONS,
                        GatewayIntent.GUILD_MESSAGE_TYPING,
                        GatewayIntent.DIRECT_MESSAGES,
                        GatewayIntent.DIRECT_MESSAGE_REACTIONS,
                        GatewayIntent.DIRECT_MESSAGE_TYPING)
                .enableIntents(GatewayIntent.GUILD_VOICE_STATES)
                .setBulkDeleteSplittingEnabled(false) // Enable the bulk delete event
                .setCompression(Compression.ZLIB); // Disable compression (not recommended)

        try {
            JDA jda = builder.build();

            bots.put(jda.getSelfUser().getId(), new MusicBot(jda, this, event));

            System.out.println(TAG + " " + jda.getSelfUser().getAsTag() + " Enabled");
        } catch (LoginException e) {
            e.printStackTrace();
            return;
        }
    }

    private void startChangeActivity() {
        if (threadPool != null && !threadPool.isShutdown())
            threadPool.shutdown();

        threadPool = Executors.newSingleThreadScheduledExecutor();
        // run thread
        threadPool.scheduleWithFixedDelay(() -> {
            for (MusicBot musicBot : bots.values()) {
                String[] msg = activityMessages.get(currentIndex);
                try {
                    musicBot.setActivity(msg);
                    Thread.sleep(500);
                } catch (IllegalArgumentException | InterruptedException e) {
                    System.err.println(TAG + " can not find type: " + msg[0]);
                    threadPool.shutdown();
                    return;
                }
                currentIndex = (currentIndex + 1) % activityMessages.size();
            }
            currentIndex -= bots.size() - 1;
            if (currentIndex < 0)
                currentIndex += activityMessages.size();
        }, 0, 5000, TimeUnit.MILLISECONDS);
    }
}
