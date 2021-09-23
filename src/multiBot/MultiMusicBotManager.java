package multiBot;

import main.java.Main;
import multiBot.music.GuildMusicManager;
import multiBot.music.TrackScheduler;
import net.dv8tion.jda.api.JDA;
import net.dv8tion.jda.api.JDABuilder;
import net.dv8tion.jda.api.OnlineStatus;
import net.dv8tion.jda.api.entities.*;
import net.dv8tion.jda.api.events.guild.voice.GuildVoiceLeaveEvent;
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
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.regex.Pattern;

import static java.nio.charset.StandardCharsets.UTF_8;
import static main.java.BotSetting.YT_APIKEY;
import static main.java.BotSetting.multiMusicBotTokens;
import static main.java.Main.emoji;
import static main.java.command.list.Invite.authChannelID;
import static main.java.lang.LangKey.*;
import static main.java.util.EmbedCreator.createEmbed;
import static main.java.util.SlashCommandOption.COUNT;
import static main.java.util.SlashCommandOption.NAME;
import static main.java.util.UrlDataGetter.getData;


public class MultiMusicBotManager {
    private static final String TAG = "[MultiMusicBot]";
    private final MusicBotEvent musicEvent = new MusicBotEvent(this);

    //                GuildID ChannelID   MusicBot
    private final Map<String, Map<String, MusicBot>> channelBot = new HashMap<>();

    //                botID, MusicBot
    private final Map<String, MusicBot> bots = new LinkedHashMap<>();

    public void setupAllBot() {
        for (String token : multiMusicBotTokens)
            newBot(token);
//        startChangeActivity();
        System.out.println(TAG + " MultiMusicBot loaded!");
    }

    private int commandState = 0;

    public int onCommand(SlashCommandEvent event) {
        List<String> lang = Main.language.getGuildLang(event.getGuild().getId());
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
                play(event, bot, false);
                break;
            case "pn":
            case "playnow":
                play(event, bot, true);
                break;
            case "s":
            case "skip":
                if (checkVcState(event, bot)) {
                    bot.nextTrack(event, false);
                }
                break;
            case "remove":
                if (checkVcState(event, bot))
                    bot.remove(event, event.getGuild());
                break;
            case "repeat":
                if (checkVcState(event, bot))
                    bot.toggleRepeat(event, event.getGuild());
                break;
            case "pause":
                if (checkVcState(event, bot))
                    bot.pause(event, event.getGuild());
                break;
            case "stop":
            case "leave":
            case "disconnect":
                if (checkVcState(event, bot))
                    bot.stopPlay(event, event.getGuild());
                break;

            case "loop":
                if (checkVcState(event, bot))
                    bot.toggleLoop(event, event.getGuild());
                break;
            case "q":
            case "queue":
            case "playing":
                if (checkVcState(event, bot))
                    bot.displayQueue(event, false, event.getGuild(), null);
                break;
            case "volume":
                if (checkVcState(event, bot)) {
                    int volume = 50;
                    if (event.getOption(COUNT) != null)
                        if ((volume = (int) event.getOption(COUNT).getAsLong()) > 100) {
                            event.getHook().editOriginalEmbeds(createEmbed(lang.get(MUSICBOT_MG_UNKNOWN_VALUE), 0xFF0000)).queue();
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

    private void play(SlashCommandEvent event, MusicBot bot, boolean playNow) {
        List<String> lang = Main.language.getGuildLang(event.getGuild().getId());
        if (event.getOptions().size() > 0) {
            if (checkVcState(event)) {
                if (bot == null)
                    // 取得機器人
                    for (MusicBot thisBot : bots.values()) {
                        GuildMusicManager manager = thisBot.getMusicManager(event.getGuild().getId());
                        if (manager != null && manager.scheduler.musicInfo == null) {
                            bot = thisBot;
                            commandState = 1;
                            break;
                        }
                    }
                if (bot == null) {
                    event.getHook().editOriginalEmbeds(createEmbed(lang.get(MUSICBOT_MG_ALL_BOT_USED), 0xFF0000)).queue();
                    commandState = -1;
                    return;
                }

                // 開始撥放
                OptionMapping url = event.getOption(NAME);
                if (Pattern.matches(".*\\.?youtu\\.?be(\\.com)?/+.*", url.getAsString())) {
                    bot.loadAndPlay(event, event.getGuild(), url.getAsString(), false, playNow, null);
                } else if (Pattern.matches(".*open.spotify.com/playlist/\\w*", url.getAsString())) {
                    bot.loadAndPlaySpotify(event, event.getGuild(), url.getAsString(), false, playNow, null);
                } else {
//                    if (true) {
//                        event.getHook().editOriginalEmbeds(createEmbed("目前 YouTube API 無法使用搜尋功能！正在等待修復", 0xFF0000)).queue();
//                        return;
//                    }
                    String keyWord = URLEncoder.encode(event.getOption(NAME).getAsString(), UTF_8);
                    SelectionMenu.Builder builder = SelectionMenu.create("MultiMusicBotManager:searchResult:" + event.getUser().getId() + ':' + bot.getID() + ':' + playNow);

                    String result = getData(
                            "https://youtube.googleapis.com/youtube/v3/search?part=snippet&maxResults=25&type=video&q=" +
                                    keyWord + "&key=" + YT_APIKEY);
                    if (result == null) return;
                    JSONArray videoInfo = new JSONObject(result).getJSONArray("items");

                    for (Object vinfo : videoInfo) {
                        JSONObject snippet = ((JSONObject) vinfo).getJSONObject("snippet");
                        String title;
                        String channelOwner;
                        if ((channelOwner = snippet.getString("channelTitle")).length() > 25)
                            channelOwner = channelOwner.substring(0, 24) + "…";

                        if ((title = snippet.getString("title")).length() > 50)
                            title = title.substring(0, 49) + "…";

                        builder.addOption(channelOwner, ((JSONObject) vinfo).getJSONObject("id").getString("videoId"), title, Emoji.fromEmote(emoji.youtubeIcon));
                    }
                    if (builder.getOptions().size() == 0) {
                        event.getHook().editOriginalEmbeds(createEmbed(lang.get(MUSICBOT_MG_SEARCH_NO_RESULT), 0xFF0000)).queue();
                    } else
                        event.getHook().editOriginalComponents().setEmbeds(createEmbed(lang.get(MUSICBOT_MG_SEARCH), 0xa3d7fe)).setActionRow(builder.build()).queue();

                }
            }
        } else {
            if (bot.getMusicManager(event.getGuild().getId()).scheduler.musicPause) {
                bot.play(event, event.getGuild());
                event.getHook().editOriginalEmbeds(createEmbed(lang.get(MUSICBOT_MG_START_PLAY), 0xbde3ae)).queue();
            } else
                event.getHook().editOriginalEmbeds(createEmbed(lang.get(MUSICBOT_MG_START_PLAY_ALREADY), 0xFF0000)).queue();
        }
    }

    public void onButton(ButtonClickEvent event, String[] args) {
        List<String> lang = Main.language.getGuildLang(event.getGuild().getId());
        if (!args[0].equals("MusicBot"))
            return;
        if (!checkVcState(event))
            return;
        if (!event.getMember().getVoiceState().getChannel().getId().equals(args[4])) {
            event.deferEdit().setEmbeds(createEmbed(lang.get(MUSICBOT_MG_UNKNOWN_CHANNEL_BUTTON), 0xFF0000)).setActionRows().queue();
            return;
        }


        MusicBot bot = bots.get(args[3]);
        GuildMusicManager manager = bot.getMusicManager(event.getGuild().getId());
        TrackScheduler scheduler = manager.scheduler;
        int volume;
        switch (args[1]) {
            case "loopChange": {
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
            case "pauseToggle":
                scheduler.switchPause();
                break;
            case "next":
                scheduler.nextTrack(null, false);
                break;
            case "volumeUp":
                volume = scheduler.getVolume() + 5;
                scheduler.setVolume(Math.min(volume, 100), null);
                break;
            case "volumeDown":
                volume = scheduler.getVolume() - 5;
                scheduler.setVolume(Math.max(volume, 0), null);
                break;
            default:
        }


        // 如果是等待的話要加時間
        scheduler.calculatePauseTime();
        GuildMusicManager musicManager = bot.getMusicManager(event.getGuild().getId());
        VoiceChannel vc = musicManager.guild.getSelfMember().getVoiceState().getChannel();
        if (vc == null) {
            event.editMessageEmbeds(createEmbed(0xFF0000, lang.get(MUSICBOT_MG_NO_BOT_IN_CHANNEL))).setActionRows().queue();
            commandState = -1;
        } else {
            MessageEmbed[] embed = bot.playStatus(event.getMember(), scheduler);
            event.editMessageEmbeds(embed[0], embed[1])
                    .setActionRows(bot.controlButtons(args[2], scheduler.musicPause, scheduler.loopStatus, vc.getId())
                    ).queue();
        }

    }

    public void onSelectMenu(SelectionMenuEvent event, String[] args) {
        List<String> lang = Main.language.getGuildLang(event.getGuild().getId());
        if (!args[0].equals("MultiMusicBotManager"))
            return;
        if (args[1].equals("searchResult")) {
            MusicBot bot = bots.get(args[3]);
            if (event.getMember().getVoiceState().inVoiceChannel() && !event.getMember().getVoiceState().getChannel().getType().equals(ChannelType.VOICE))
                event.replyEmbeds(createEmbed(lang.get(MUSICBOT_MG_NEED_USE_IN_CHANNEL), 0xFF0000)).setEphemeral(true).queue();
            else {
                bot.loadAndPlay(event, event.getGuild(), "https://youtu.be/" + event.getValues().get(0), true, Boolean.parseBoolean(args[4]), event);
            }
        }
    }

    private boolean checkVcState(GenericInteractionCreateEvent event, MusicBot botsInChannel) {
        List<String> lang = Main.language.getGuildLang(event.getGuild().getId());
        if (!event.getMember().getVoiceState().inVoiceChannel()) {
            event.getHook().editOriginalEmbeds(createEmbed(lang.get(MUSICBOT_MG_NEED_USE_IN_CHANNEL), 0xFF0000)).queue();
            commandState = -1;
            return false;
        } else if (!event.getMember().getVoiceState().getChannel().getType().equals(ChannelType.VOICE)) {
            event.getHook().editOriginalEmbeds(createEmbed(lang.get(MUSICBOT_MG_NEED_USE_IN_CHANNEL), 0xFF0000)).queue();
            commandState = -1;
            return false;
        } else if (botsInChannel == null) {
            event.getHook().editOriginalEmbeds(createEmbed(0xFF0000, lang.get(MUSICBOT_MG_NO_BOT_IN_CHANNEL))).queue();
            commandState = -1;
            return false;
        }
        return true;
    }

    private boolean checkVcState(GenericInteractionCreateEvent event) {
        List<String> lang = Main.language.getGuildLang(event.getGuild().getId());
        if (!event.getMember().getVoiceState().inVoiceChannel()) {
            event.getHook().editOriginalEmbeds(createEmbed(lang.get(MUSICBOT_MG_NEED_USE_IN_CHANNEL), 0xFF0000)).queue();
            commandState = -1;
            return false;
        } else if (!event.getMember().getVoiceState().getChannel().getType().equals(ChannelType.VOICE)) {
            event.getHook().editOriginalEmbeds(createEmbed(lang.get(MUSICBOT_MG_NEED_USE_IN_CHANNEL), 0xFF0000)).queue();
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
        JDABuilder builder = JDABuilder.createLight(botToken)
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
                .setBulkDeleteSplittingEnabled(false)
                .setCompression(Compression.ZLIB)
                .setStatus(OnlineStatus.IDLE)
                .setActivity(Activity.of(Activity.ActivityType.COMPETING, "來點歌吧!"));
        try {
            JDA jda = builder.build();
            bots.put(jda.getSelfUser().getId(), new MusicBot(jda, this, musicEvent));
            System.out.println(TAG + " " + jda.getSelfUser().getAsTag() + " Enabled");
        } catch (LoginException e) {
            System.err.println(TAG + " " + e.getMessage());
        }
    }

    public void onVoiceLeave(GuildVoiceLeaveEvent event) {
        if (event.getMember().getUser().isBot() && bots.containsKey(event.getMember().getId())) {
            try {
                MusicBot bot = channelBot.get(event.getGuild().getId()).put(event.getChannelLeft().getId(), null);
                if (bot == null) return;
                bot.stopPlay(null, event.getGuild());
            } catch (Exception ignored) {
            }
        }
    }
}