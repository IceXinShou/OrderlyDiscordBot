package multiBot;

import multiBot.music.GuildMusicManager;
import multiBot.music.TrackScheduler;
import net.dv8tion.jda.api.JDA;
import net.dv8tion.jda.api.JDABuilder;
import net.dv8tion.jda.api.entities.ChannelType;
import net.dv8tion.jda.api.entities.Emoji;
import net.dv8tion.jda.api.entities.MessageEmbed;
import net.dv8tion.jda.api.entities.VoiceChannel;
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
import org.jetbrains.annotations.NotNull;
import org.json.JSONArray;
import org.json.JSONObject;

import javax.security.auth.login.LoginException;
import java.net.URLEncoder;
import java.util.*;
import java.util.regex.Pattern;

import static java.nio.charset.StandardCharsets.UTF_8;
import static main.java.BotSetting.apiKEY;
import static main.java.BotSetting.multiMusicBotTokens;
import static main.java.Main.emoji;
import static main.java.command.list.Invite.authChannelID;
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

    public int onCommand(@NotNull SlashCommandEvent event) {
        Map<String, MusicBot> botInGuild = channelBot.get(Objects.requireNonNull(event.getGuild()).getId());
        MusicBot bot;
        if (event.getTextChannel().getId().equals(authChannelID))
            return 0;
        if (botInGuild == null)
            bot = null;
        else if (Objects.requireNonNull(Objects.requireNonNull(event.getMember()).getVoiceState()).inVoiceChannel() && Objects.requireNonNull(Objects.requireNonNull(event.getMember().getVoiceState()).getChannel()).getType().equals(ChannelType.VOICE))
            bot = botInGuild.get(Objects.requireNonNull(event.getMember().getVoiceState().getChannel()).getId());
        else
            bot = null;

        commandState = 1;

        switch (event.getName()) {
            // 全域頻道
            case "p":
            case "play":
                if (event.getOptions().size() == 0) {
                    if (checkVcState(event, bot)) {
                        play(event, bot, false);
                    }
                } else
                    play(event, bot, false);
                break;
            case "pn":
            case "playnow":
                play(event, bot, true);
                break;
            case "s":
            case "skip":
                if (checkVcState(event, bot)) {
                    Objects.requireNonNull(bot).nextTrack(event, false);
                }
                break;
            case "remove":
                if (checkVcState(event, bot))
                    Objects.requireNonNull(bot).remove(event, event.getGuild());
                break;
            case "repeat":
                if (checkVcState(event, bot))
                    Objects.requireNonNull(bot).toggleRepeat(event, event.getGuild());
                break;
            case "pause":
                if (checkVcState(event, bot))
                    Objects.requireNonNull(bot).pause(event, event.getGuild());
                break;
            case "stop":
            case "leave":
            case "disconnect":
                if (checkVcState(event, bot))
                    Objects.requireNonNull(bot).disconnect(event, event.getGuild());
                break;

            case "loop":
                if (checkVcState(event, bot))
                    Objects.requireNonNull(bot).toggleLoop(event, event.getGuild());
                break;
            case "q":
            case "queue":
            case "playing":
                if (checkVcState(event, bot))
                    Objects.requireNonNull(bot).displayQueue(event, false, event.getGuild());
                break;
            case "volume":
                if (checkVcState(event, bot)) {
                    int volume = 50;
                    if (event.getOption(COUNT) != null)
                        if ((volume = (int) Objects.requireNonNull(event.getOption(COUNT)).getAsLong()) > 100) {
                            event.getHook().editOriginalEmbeds(createEmbed("未知的數值", 0xFF0000)).queue();
                            break;
                        }
                    Objects.requireNonNull(bot).changeVolume(volume, event.getGuild(), event);
                }
                break;
            default:
                commandState = 0;
        }
        return commandState;
    }

    private void play(@NotNull SlashCommandEvent event, MusicBot bot, boolean playNow) {
        if (event.getOptions().size() > 0) {
            if (checkVcState(event)) {
                if (bot == null)
                    // 取得機器人
                    for (MusicBot thisBot : bots.values()) {
                        GuildMusicManager manager = thisBot.getMusicManager(Objects.requireNonNull(event.getGuild()).getId());
                        if (manager != null && manager.scheduler.musicInfo == null) {
                            bot = thisBot;
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
                assert url != null;
                if (Pattern.matches(".*\\.?youtu\\.?be(\\.com)?/+.*", url.getAsString())) {
                    bot.loadAndPlay(event, Objects.requireNonNull(event.getGuild()), url.getAsString(), false, playNow);
                } else {
                    String keyWord = URLEncoder.encode(Objects.requireNonNull(event.getOption(NAME)).getAsString(), UTF_8);
                    SelectionMenu.Builder builder = SelectionMenu.create("MultiMusicBotManager:searchResult:" + event.getUser().getId() + ':' + bot.getID() + ':' + playNow);

                    String result = getData(
                            "https://youtube.googleapis.com/youtube/v3/search?part=snippet&maxResults=25&type=video&q=" +
                                    keyWord + "&key=" + apiKEY);
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
                        event.getHook().editOriginalEmbeds(createEmbed("查無搜尋結果!", 0xFF0000)).queue();
                    } else
                        event.getHook().editOriginalComponents().setEmbeds(createEmbed("搜尋結果", 0xa3d7fe)).setActionRow(builder.build()).queue();

                }
            }
        } else {
            if (bot.getMusicManager(Objects.requireNonNull(event.getGuild()).getId()).scheduler.musicPause) {
                bot.play(event, event.getGuild());
                event.getHook().editOriginalEmbeds(createEmbed("已開始播放", 0xbde3ae)).queue();
            } else
                event.getHook().editOriginalEmbeds(createEmbed("已經在播放中了", 0xFF0000)).queue();
        }
    }

    public void onButton(ButtonClickEvent event, String @NotNull [] args) {
        if (!args[0].equals("MusicBot"))
            return;
        if (!checkVcState(event))
            return;
        if (!Objects.requireNonNull(Objects.requireNonNull(Objects.requireNonNull(event.getMember()).getVoiceState()).getChannel()).getId().equals(args[4])) {
            event.deferEdit().setEmbeds(createEmbed("未知的頻道按鈕", 0xFF0000)).setActionRows().queue();
            return;
        }


        MusicBot bot = bots.get(args[3]);
        GuildMusicManager manager = bot.getMusicManager(Objects.requireNonNull(event.getGuild()).getId());
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
                scheduler.nextTrack(null, null, false);
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
        VoiceChannel vc = Objects.requireNonNull(musicManager.guild.getSelfMember().getVoiceState()).getChannel();
        if (vc == null) {
            event.editMessageEmbeds(createEmbed(0xFF0000, "沒有機器人在語音頻道餒...")).setActionRows().queue();
            commandState = -1;
        } else {
            MessageEmbed[] embed = bot.playStatus(event.getMember(), scheduler);
            event.editMessageEmbeds(embed[0], embed[1])
                    .setActionRows(bot.controlButtons(args[2], scheduler.musicPause, scheduler.loopStatus, vc.getId())
                    ).queue();
        }

    }

    public void onSelectMenu(@NotNull SelectionMenuEvent event, String @NotNull [] args) {
        if (!args[0].equals("MultiMusicBotManager"))
            return;
        if (args[1].equals("searchResult")) {
            MusicBot bot = bots.get(args[3]);
            if (Objects.requireNonNull(Objects.requireNonNull(event.getMember()).getVoiceState()).inVoiceChannel() && !Objects.requireNonNull(Objects.requireNonNull(event.getMember().getVoiceState()).getChannel()).getType().equals(ChannelType.VOICE))
                event.replyEmbeds(createEmbed("請在語音頻道使用此指令", 0xFF0000)).setEphemeral(true).queue();
            else {
                bot.loadAndPlay(event, Objects.requireNonNull(event.getGuild()), "https://youtu.be/" + event.getValues().get(0), true, Boolean.parseBoolean(args[4]));
            }
        }
    }

    private boolean checkVcState(@NotNull GenericInteractionCreateEvent event, MusicBot botsInChannel) {
        if (!Objects.requireNonNull(Objects.requireNonNull(event.getMember()).getVoiceState()).inVoiceChannel()) {
            event.getHook().editOriginalEmbeds(createEmbed("請在語音頻道內使用此指令", 0xFF0000)).queue();
            commandState = -1;
            return false;
        } else if (!Objects.requireNonNull(Objects.requireNonNull(event.getMember().getVoiceState()).getChannel()).getType().equals(ChannelType.VOICE)) {
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
        if (!Objects.requireNonNull(Objects.requireNonNull(event.getMember()).getVoiceState()).inVoiceChannel()) {
            event.getHook().editOriginalEmbeds(createEmbed("請在語音頻道內使用此指令", 0xFF0000)).queue();
            commandState = -1;
            return false;
        } else if (!Objects.requireNonNull(Objects.requireNonNull(event.getMember().getVoiceState()).getChannel()).getType().equals(ChannelType.VOICE)) {
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
                .setBulkDeleteSplittingEnabled(false) // Enable the bulk delete event
                .setCompression(Compression.ZLIB); // Disable compression (not recommended)
        try {
            JDA jda = builder.build();
            bots.put(jda.getSelfUser().getId(), new MusicBot(jda, this, musicEvent));
            System.out.println(TAG + " " + jda.getSelfUser().getAsTag() + " Enabled");
        } catch (LoginException e) {
            System.err.println(e.getMessage());
        }
    }

    public void onVoiceLeave(@NotNull GuildVoiceLeaveEvent event) {
        if (event.getMember().getUser().isBot() && bots.containsKey(event.getMember().getId())) {
            try {
                MusicBot bot = channelBot.get(event.getGuild().getId()).put(event.getChannelLeft().getId(), null);
                if (bot == null) return;
                bot.stopPlay(event.getGuild());
            } catch (Exception ignored) {
            }
        }
    }
}