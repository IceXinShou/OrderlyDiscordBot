package multiBot;

import multiBot.music.GuildMusicManager;
import multiBot.music.TrackScheduler;
import net.dv8tion.jda.api.JDA;
import net.dv8tion.jda.api.JDABuilder;
import net.dv8tion.jda.api.entities.ChannelType;
import net.dv8tion.jda.api.entities.MessageEmbed;
import net.dv8tion.jda.api.events.interaction.ButtonClickEvent;
import net.dv8tion.jda.api.events.interaction.GenericInteractionCreateEvent;
import net.dv8tion.jda.api.events.interaction.SelectionMenuEvent;
import net.dv8tion.jda.api.events.interaction.SlashCommandEvent;
import net.dv8tion.jda.api.interactions.commands.OptionMapping;
import net.dv8tion.jda.api.interactions.components.selections.SelectionMenu;
import net.dv8tion.jda.api.requests.GatewayIntent;
import net.dv8tion.jda.api.utils.Compression;
import net.dv8tion.jda.api.utils.cache.CacheFlag;
import org.jetbrains.annotations.NotNull;
import org.json.JSONArray;
import org.json.JSONObject;

import javax.security.auth.login.LoginException;
import java.net.URLEncoder;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;
import java.util.regex.Pattern;

import static java.nio.charset.StandardCharsets.UTF_8;
import static main.java.BotSetting.*;
import static main.java.SlashCommandOption.COUNT;
import static main.java.SlashCommandOption.NAME;
import static main.java.command.Invite.authChannelID;
import static main.java.util.Funtions.createEmbed;
import static main.java.util.Funtions.toUnicode;
import static multiBot.music.TrackScheduler.getUrlData;


public class MultiMusicBotManager {
    private static final String TAG = "[MultiMusicBot]";
    private ScheduledExecutorService threadPool;
    private int currentIndex = 0;
    private final MusicBotEvent musicEvent = new MusicBotEvent(this);

    //          GuildID ChannelID   MusicBot
    private final Map<String, Map<String, MusicBot>> channelBot = new HashMap<>();

    //                botID, MusicBot
    private final Map<String, MusicBot> bots = new LinkedHashMap<>();

    public MultiMusicBotManager() {
        for (String token : multiMusicBotTokens)
            newBot(token);
        startChangeActivity();
        System.out.println(TAG + " MultiMusicBot loaded!");
    }

    private int commandState = 0;

    public int onCommand(@NotNull SlashCommandEvent event) {
        Map<String, MusicBot> botInGuild = channelBot.get(event.getGuild().getId());
        MusicBot bot;
        if (event.getTextChannel().getId().equals(authChannelID))
            return 0;
        if (botInGuild == null)
            bot = null;
        else if (event.getMember().getVoiceState().inVoiceChannel() && event.getMember().getVoiceState().getChannel().getType().equals(ChannelType.VOICE))
            bot = botInGuild.get(event.getMember().getVoiceState().getChannel().getId());
        else
            bot = null;

        commandState = 1;

        switch (event.getName()) {
            // 全域頻道
            case "p":
            case "play":
                if (event.getOptions().size() == 0) {
                    if (checkVcState(event, bot)) {
                        play(event, bot);
                    }
                } else
                    play(event, bot);
                break;
            case "s":
            case "skip":
                if (checkVcState(event, bot))
                    bot.nextTrack(event);
                break;
            case "remove":
                if (checkVcState(event, bot))
                    bot.remove(event);
                break;
//            case "previous":
//                if (checkVcState(event, bot))
//                    bot.playPrevious(event);
//                break;
            case "repeat":
                if (checkVcState(event, bot))
                    bot.toggleRepeat(event);
                break;
            case "pause":
                if (checkVcState(event, bot))
                    bot.pause(event, event.getGuild());
                break;
            case "stop":
            case "leave":
            case "disconnect":
                if (checkVcState(event, bot))
                    bot.disconnect(event);
                break;

            case "loop":
                if (checkVcState(event, bot))
                    bot.toggleLoop(event);
                break;
            case "q":
            case "queue":
            case "playing":
                if (checkVcState(event, bot))
                    bot.displayQueue(event, false);
                break;
            case "volume":
                if (checkVcState(event, bot)) {
                    Integer volume = 50;
                    if (event.getOption(COUNT) != null)
                        if (event.getOption(COUNT).getAsLong() <= 100)
                            volume = (int) event.getOption(COUNT).getAsLong();
                        else {
                            event.getHook().editOriginalEmbeds(createEmbed("未知的數值", 0xFF0000)).queue();
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

    private void play(@NotNull SlashCommandEvent event, MusicBot bot) {
        if (event.getOptions().size() > 0) {
            if (checkVcState(event)) {
                if (bot == null)
                    // 取得機器人
                    for (Object i : bots.values().toArray()) {
                        MusicBot thisBot = (MusicBot) i;
                        GuildMusicManager manager = thisBot.getMusicManager(event.getGuild().getId());
                        if (manager != null && manager.scheduler.playingTrack == null) {
                            bot = (MusicBot) i;
                            commandState = 1;
                            break;
                        }
                    }

                if (bot == null) {
                    event.getHook().editOriginalEmbeds(createEmbed("目前所有機器人都已被占用", 0xFF0000)).queue();
                    commandState = -1;
                    return;
                }

                // 開始撥放
                OptionMapping url = event.getOption(NAME);
                if (Pattern.matches(".*\\.?youtu\\.?be(\\.com)?/+.*", url.getAsString())) {
                    bot.loadAndPlay(event, url.getAsString(), false);
                } else {

                    String keyWord = URLEncoder.encode(event.getOption(NAME).getAsString(), UTF_8);

                    SelectionMenu.Builder builder = SelectionMenu.create(event.getUser().getId() + ":searchResult:" + bot.getID());

                    JSONObject result = new JSONObject(
                            getUrlData("https://youtube.googleapis.com/youtube/v3/search?part=snippet&maxResults=25&type=video&q=" +
                                    keyWord + "&key=" + apiKEY));

                    JSONArray videoInfo = result.getJSONArray("items");

                    for (Object vinfo : videoInfo) {
                        JSONObject snippet = ((JSONObject) vinfo).getJSONObject("snippet");
                        String title = toUnicode(snippet.getString("title"));
                        if (title.length() > 25)
                            title = title.substring(0, 24) + "…";
                        builder.addOption(title, ((JSONObject) vinfo).getJSONObject("id").getString("videoId"));
                    }
                    event.getHook().editOriginalComponents().setEmbeds(createEmbed("搜尋結果", 0xa3d7fe)).setActionRow(builder.build()).queue();

//                event.getTextChannel().sendMessageEmbeds(createEmbed("搜尋結果", 0xa3d7fe)).setActionRow(builder.build()).queue();
                }
            }
        } else {
            if (bot.getMusicManager(event.getGuild().getId()).scheduler.musicPause) {
                bot.play(event, event.getGuild());
                event.getHook().editOriginalEmbeds(createEmbed("已開始播放", 0xbde3ae)).queue();
            } else
                event.getHook().editOriginalEmbeds(createEmbed("已經在播放中了", 0xFF0000)).queue();
        }
    }

    public void onButton(ButtonClickEvent event, String @NotNull [] args) {
        if (args[1].startsWith("music")) {
            if (!checkVcState(event))
                return;
            if (!event.getMember().getVoiceState().getChannel().getId().equals(args[3])) {
                event.deferEdit().setEmbeds(createEmbed("未知的頻道按鈕", 0xFF0000)).setActionRows().queue();
                return;
            }


            MusicBot bot = bots.get(args[2]);
            GuildMusicManager manager = bot.getMusicManager(event.getGuild().getId());
            TrackScheduler scheduler = manager.scheduler;
            int volume;
            MessageEmbed[] embed = null;
            switch (args[1]) {
                case "musicLoopChange": {
                    switch (scheduler.loopStatus = (scheduler.loopStatus + 1) % 3) {
                        case 0:
                            scheduler.repeat = false;
                            scheduler.loop = false;
                            break;
                        case 1:
                            scheduler.repeat = false;
                            scheduler.loop = true;
                            break;
                        case 2:
                            scheduler.repeat = true;
                            scheduler.loop = false;
                            break;
                        default:
                            return;
                    }
                    break;
                }
                case "musicPause":
                    scheduler.switchPause();
                    break;
                case "musicNext":
                    scheduler.nextTrack(null, null);
                    embed = bot.playStatus(event.getMember(), scheduler, true);
                    break;
                case "musicVolumeUp":
                    volume = scheduler.getVolume() + 5;
                    scheduler.setVolume(Math.min(volume, 100), null);
                    break;
                case "musicVolumeDown":
                    volume = scheduler.getVolume() - 5;
                    scheduler.setVolume(Math.max(volume, 0), null);
                    break;
                default:
            }

            // 如果是等待的話要加時間
            scheduler.calculatePauseTime();
            if (bot.getMusicManager(event.getGuild().getId()).guild.getSelfMember().getVoiceState().getChannel() == null) {
                event.getHook().editOriginalEmbeds(createEmbed(0xFF0000, "沒有機器人在語音頻道餒...")).queue();
                commandState = -1;
            } else {
                if (embed == null)
                    embed = bot.playStatus(event.getMember(), scheduler, false);
                try {
                    event.deferEdit().setEmbeds(embed[0], embed[1]).setActionRows(bot.controlButtons(args[0], scheduler.musicPause, scheduler.loopStatus,
                            bot.getMusicManager(event.getGuild().getId()).guild.getSelfMember().getVoiceState().getChannel().getId())).queue();
                } catch (Exception e) {
                    System.err.println(e.getMessage());
                }
            }
        }
    }

    public void onSelectMenu(@NotNull SelectionMenuEvent event, String @NotNull [] args) {
        MusicBot bot = bots.get(args[2]);
        if (event.getMember().getVoiceState().inVoiceChannel() && !event.getMember().getVoiceState().getChannel().getType().equals(ChannelType.VOICE))
            event.replyEmbeds(createEmbed("請在語音頻道使用此指令", 0xFF0000)).setEphemeral(true).queue();
        else if (!args[0].equals(event.getUser().getId()))
            event.replyEmbeds(createEmbed("此為其他成員的觸發項目", 0xFF0000)).setEphemeral(true).queue();
        else {
            bot.loadAndPlay(event, "https://youtu.be/" + event.getValues().get(0), true);
        }
    }

    private boolean checkVcState(@NotNull GenericInteractionCreateEvent event, MusicBot botsInChannel) {
        if (!event.getMember().getVoiceState().inVoiceChannel()) {
            event.getHook().editOriginalEmbeds(createEmbed("請在語音頻道內使用此指令", 0xFF0000)).queue();
            commandState = -1;
            return false;
        } else if (!event.getMember().getVoiceState().getChannel().getType().equals(ChannelType.VOICE)) {
            event.getHook().editOriginalEmbeds(createEmbed("請在語音頻道內使用此指令", 0xFF0000)).queue();
            commandState = -1;
            return false;
        } else if (botsInChannel == null) {
            event.getHook().editOriginalEmbeds(createEmbed(0xFF0000, "沒有機器人在語音頻道餒...")).queue();
            commandState = -1;
            return false;
        }
        return true;
    }

    private boolean checkVcState(@NotNull GenericInteractionCreateEvent event) {
        if (!event.getMember().getVoiceState().inVoiceChannel()) {
            event.getHook().editOriginalEmbeds(createEmbed("請在語音頻道內使用此指令", 0xFF0000)).queue();
            commandState = -1;
            return false;
        } else if (!event.getMember().getVoiceState().getChannel().getType().equals(ChannelType.VOICE)) {
            event.getHook().editOriginalEmbeds(createEmbed("請在語音頻道內使用此指令", 0xFF0000)).queue();
            commandState = -1;
            return false;
        }
        return true;
    }

    public void setBotToChannel(String guildID, String ChannelID, MusicBot musicBot) {
        Map<String, MusicBot> botInGuild = channelBot.get(guildID);
        if (botInGuild == null) {
            HashMap<String, MusicBot> map = new HashMap<>();
            map.put(ChannelID, musicBot);
            channelBot.put(guildID, map);
        } else {
            botInGuild.put(ChannelID, musicBot);
        }
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
            bots.put(jda.getSelfUser().getId(), new MusicBot(jda, this, musicEvent));

            System.out.println(TAG + " " + jda.getSelfUser().getAsTag() + " Enabled");
        } catch (LoginException e) {
            System.err.println(e.getMessage());
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
