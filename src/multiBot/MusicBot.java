package multiBot;

import com.sedmelluq.discord.lavaplayer.player.AudioLoadResultHandler;
import com.sedmelluq.discord.lavaplayer.player.AudioPlayerManager;
import com.sedmelluq.discord.lavaplayer.player.DefaultAudioPlayerManager;
import com.sedmelluq.discord.lavaplayer.source.AudioSourceManagers;
import com.sedmelluq.discord.lavaplayer.tools.FriendlyException;
import com.sedmelluq.discord.lavaplayer.track.AudioPlaylist;
import com.sedmelluq.discord.lavaplayer.track.AudioTrack;
import com.sedmelluq.discord.lavaplayer.track.AudioTrackInfo;
import multiBot.music.GuildMusicManager;
import multiBot.music.TrackScheduler;
import net.dv8tion.jda.api.JDA;
import net.dv8tion.jda.api.audio.hooks.ConnectionListener;
import net.dv8tion.jda.api.audio.hooks.ConnectionStatus;
import net.dv8tion.jda.api.entities.*;
import net.dv8tion.jda.api.events.interaction.GenericInteractionCreateEvent;
import net.dv8tion.jda.api.events.interaction.SlashCommandEvent;
import net.dv8tion.jda.api.interactions.components.ActionRow;
import net.dv8tion.jda.api.interactions.components.Button;
import net.dv8tion.jda.api.interactions.components.ButtonStyle;
import org.jetbrains.annotations.NotNull;
import org.json.JSONObject;

import java.time.OffsetDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;

import static main.java.BotSetting.apiKEY;
import static main.java.util.Funtions.createEmbed;
import static multiBot.music.TrackScheduler.getUrlData;

public class MusicBot {
    private final String TAG;
    private final JDA jda;
    private final String botID;
    private final GuildMusicManager.Event event;
    private final MultiMusicBotManager musicBotManager;


    // music
    private final AudioPlayerManager playerManager;
    public Map<String, GuildMusicManager> musicManagers;

    public MusicBot(@NotNull JDA jda, MultiMusicBotManager musicBotManager, GuildMusicManager.Event event) {
        this.TAG = "[" + jda.getSelfUser().getName() + "]";
        this.jda = jda;
        this.botID = jda.getSelfUser().getId();
        this.event = event;
        this.musicBotManager = musicBotManager;

        this.musicManagers = new HashMap<>();
        this.playerManager = new DefaultAudioPlayerManager();
        AudioSourceManagers.registerRemoteSources(playerManager);
        AudioSourceManagers.registerLocalSource(playerManager);
    }

    /**
     * command player control
     */
    private void play(AudioPlaylist playlist, VoiceChannel vc, @NotNull GuildMusicManager manager, GenericInteractionCreateEvent event, boolean playNow) {
        connectVC(manager.guild, vc, event);
        if (playNow) {
            event.replyEmbeds(createEmbed("目前未支援立即播放歌單功能", 0xFF0000)).queue();
            return;
        }
        manager.scheduler.addPlayListToQueue(playlist, event, this);
    }

    private void play(AudioTrack track, VoiceChannel vc, @NotNull GuildMusicManager manager, GenericInteractionCreateEvent event, boolean search, boolean playNow) {
        connectVC(manager.guild, vc, event);
        manager.scheduler.queue(track, event, this, -1, search, playNow);
    }

    public void changeVolume(int volume, Guild guild, SlashCommandEvent event) {
        getMusicManager(guild).scheduler.setVolume(volume, event);
    }

    public void nextTrack(@NotNull SlashCommandEvent event, boolean search) {
        getMusicManager(event.getGuild()).scheduler.nextTrack(event, this, search);

    }

//    public void playPrevious(@NotNull SlashCommandEvent event) {
//        Guild guild = jda.getGuildById(event.getGuild().getId());
//        connectVC(guild, event.getMember().getVoiceState().getChannel());
//        getMusicManager(event.getGuild()).scheduler.previousTrack(event);
//    }

    public void toggleRepeat(@NotNull SlashCommandEvent event) {
        getMusicManager(event.getGuild()).scheduler.toggleRepeat(event);
    }

    public void toggleLoop(@NotNull SlashCommandEvent event) {
        getMusicManager(event.getGuild()).scheduler.toggleLoop(event);
    }

    public void pause(SlashCommandEvent event, Guild guild) {
        getMusicManager(guild).scheduler.pause(event);
    }

    public void play(SlashCommandEvent event, Guild guild) {
        getMusicManager(guild).scheduler.play(event);
    }

    public void remove(@NotNull SlashCommandEvent event) {
        int index = (int) event.getOption("index").getAsLong();
        getMusicManager(event.getGuild().getId()).scheduler.remove(index, event);
    }

    public void loadAndPlay(final @NotNull GenericInteractionCreateEvent event, final String trackUrl, boolean search, boolean playNow) {
        VoiceChannel vc = event.getMember().getVoiceState().getChannel();
        GuildMusicManager manager = getMusicManager(jda.getGuildById(event.getGuild().getId()));
        // 取得音樂
        playerManager.loadItemOrdered(musicManagers, trackUrl, new AudioLoadResultHandler() {
            @Override
            public void trackLoaded(AudioTrack track) {
                play(track, vc, manager, event, search, playNow);
            }

            @Override
            public void playlistLoaded(AudioPlaylist playlist) {
                play(playlist, vc, manager, event, playNow);
            }

            @Override
            public void noMatches() {
                try {
                    event.getHook().editOriginalEmbeds(createEmbed("查無此網址: " + trackUrl, 0xFF0000)).queue();
                } catch (Exception ignored) {
                }
            }

            @Override
            public void loadFailed(FriendlyException exception) {
                try {
                    event.getHook().editOriginalEmbeds(createEmbed("無法播放此網址: " + exception.getMessage(), 0xFF0000)).queue();
                } catch (Exception ignored) {
                }
            }
        });

    }

    public void displayQueue(@NotNull GenericInteractionCreateEvent event, boolean searchAble) {
        GuildMusicManager musicManager = getMusicManager(event.getGuild());
        TrackScheduler scheduler = musicManager.scheduler;
        if (scheduler.playingTrack == null) {
            event.getHook().editOriginalEmbeds(createEmbed("目前無音樂播放", 0xFF0000)).queue();
            return;
        }

        scheduler.calculatePauseTime();
        MessageEmbed[] embed = playStatus(event.getMember(), scheduler, false);
        String vcID = musicManager.guild.getSelfMember().getVoiceState().getChannel().getId();
        if (search) {
            try {
                event.deferReply(true).addEmbeds(embed[0], embed[1])
                        .addActionRows(controlButtons(event.getMember().getId(), scheduler.musicPause, scheduler.loopStatus, vcID))
                        .queue();
            } catch (Exception e) {
                System.err.println(e.getMessage());
            }
        } else
            event.getHook().editOriginalComponents()
                    .setEmbeds(embed[0], embed[1])
                    .setActionRows(controlButtons(event.getMember().getId(), scheduler.musicPause, scheduler.loopStatus, vcID))
                    .queue();
    }

    public void disconnect(@NotNull SlashCommandEvent event) {
        getMusicManager(event.getGuild()).scheduler.stopPlay(event);
    }

    /**
     * display
     */
    private JSONObject videoInfo;


    public void updateVideoInfo(Guild guild) {
        videoInfo = new JSONObject(getUrlData("https://www.googleapis.com/youtube/v3/videos?id=" +
                getMusicManager(guild).scheduler.playingTrack.getInfo().identifier +
                "&key=" + apiKEY + "&part=statistics,snippet")).getJSONArray("items").getJSONObject(0);
    }

    public MessageEmbed[] playStatus(Member member, @NotNull TrackScheduler scheduler, boolean update) {
        // 現在播放資料
        StringBuilder progress = new StringBuilder();
        MessageEmbed nowPlaying;
        // 有歌曲正在播放
        if (scheduler.playingTrack != null) {
//            if (update) {
            updateVideoInfo(member.getGuild());
//            }
            // 進度顯示
            AudioTrackInfo trackInfo = scheduler.playingTrack.getInfo();
            int nowPlayingTime = (int) ((System.currentTimeMillis() - scheduler.startPlayTime) / 1000);
            int playPercent = (int) ((nowPlayingTime / (float) (trackInfo.length / 1000)) * 20);

            JSONObject statistics = videoInfo.getJSONObject("statistics");
            JSONObject snippet = videoInfo.getJSONObject("snippet");
            JSONObject thumbnails = snippet.getJSONObject("thumbnails");

            progress.append("\n\n**[")
                    .append(timeCalculator(nowPlayingTime))
                    .append("] **")
                    .append("━".repeat(playPercent))
                    .append("❚")
                    .append("─".repeat(20 - playPercent))
                    .append("** [").append(trackInfo.isStream ? "LIVE" : (timeCalculator((int) (trackInfo.length / 1000))))
                    .append("]**\n");

            // 音量顯示
            int volumePercent = (getMusicManager(member.getGuild()).scheduler.getVolume() / 5);
            progress.append("\n")
                    .append("**音量: **")
                    .append("◆".repeat(volumePercent))
                    .append("◇".repeat(20 - volumePercent))
                    .append(scheduler.loopStatus == 0 ? " <順序播放>\n" : (scheduler.loopStatus == 1 ? " <循環播放>\n" : " <單曲循環>\n"));

            // 組裝
            nowPlaying = createEmbed("**" + trackInfo.title + "**", trackInfo.uri,
                    progress.toString(),
                    new StringBuilder()
                            .append(" \uD83D\uDC40 ").append(String.format("%,d", Long.parseLong(statistics.getString("viewCount"))))
                            .append(" | \uD83D\uDC4D ").append(String.format("%,d", Long.parseLong(statistics.getString("likeCount"))))
                            .append(" | \uD83D\uDC4E ").append(String.format("%,d", Long.parseLong(statistics.getString("dislikeCount"))))
                            .append(" | \uD83D\uDCAC ").append(String.format("%,d", Long.parseLong(statistics.getString("commentCount"))))
                            .append(" | \uD83D\uDD0A ").append(String.format("%.2f db", scheduler.loudness))
                            .append(" | \uD83D\uDCC5 ").append(OffsetDateTime.parse(snippet.getString("publishedAt")).format(DateTimeFormatter.ofPattern("yyyy.MM.dd")))

                            .toString()
                    , trackInfo.author, channelImageUrl, thumbnails.getJSONObject(quality).getString("url"),
                    0xe5b849);
        } else {
            nowPlaying = createEmbed(0xFF0000, "**[沒有歌曲正在被播放]**");
        }
        // 歌曲列表
        List<MessageEmbed.Field> fields = new ArrayList<>();
        if (scheduler.getQueue().size() == 0)
            fields.add(new MessageEmbed.Field("無", "", false));
        else {
            List<AudioTrack> inQueue = scheduler.getQueue();
            int index = inQueue.size();
            for (AudioTrack track : inQueue) {
                fields.add(new MessageEmbed.Field("[" + index-- + "] " + track.getInfo().title, track.getInfo().isStream ? "**[LIVE]**" : ""/*"**[" + (timeCalculator(songLength)) + "]**"*/, false));
            }
        }

        return new MessageEmbed[]{createEmbed("歌曲列表", "",
                "",
                fields,
                null,
                0x7fc89a),
                nowPlaying};
    }

    public ActionRow controlButtons(String senderID, boolean pauseStatus, int loopStatus, String channelID) {
        return ActionRow.of(
                Button.of(ButtonStyle.SECONDARY, "MusicBot:loopChange:" + senderID + ':' + botID + ':' + channelID, "",
                        loopStatus == 0 ? Emoji.fromUnicode("➡️") : (loopStatus == 1 ? Emoji.fromUnicode("\uD83D\uDD01") : Emoji.fromUnicode("\uD83D\uDD02"))),
                Button.of(ButtonStyle.SECONDARY, "MusicBot:pauseToggle:" + senderID + ':' + botID + ':' + channelID, "",
                        pauseStatus ? Emoji.fromUnicode("▶️") : Emoji.fromUnicode("⏸️")),
                Button.of(ButtonStyle.SECONDARY, "MusicBot:next:" + senderID + ':' + botID + ':' + channelID, "", Emoji.fromUnicode("⏭️")),
                Button.of(ButtonStyle.SECONDARY, "MusicBot:volumeDown:" + senderID + ':' + botID + ':' + channelID, "", Emoji.fromUnicode("\uD83D\uDD09")),
                Button.of(ButtonStyle.SECONDARY, "MusicBot:volumeUp:" + senderID + ':' + botID + ':' + channelID, "", Emoji.fromUnicode("\uD83D\uDD0A")));
    }

    /**
     * functions
     */

    private @NotNull String timeCalculator(long songLength) {
        StringBuilder builder = new StringBuilder();

        int hr = (int) songLength / 3600;
        int min = (int) ((songLength / 60) % 60);
        int sec = (int) songLength % 60;

        if (hr > 0)
            builder.append(hr < 10 ? "0" + hr : hr).append(':');
        builder.append(min < 10 ? "0" + min : min).append(':');
        builder.append(sec < 10 ? "0" + sec : sec);

        return builder.toString();
    }

    private void connectVC(@NotNull Guild guild, VoiceChannel vc, GenericInteractionCreateEvent event) {
        if (!guild.getAudioManager().isConnected()) {
            try {
                guild.getAudioManager().openAudioConnection(vc);
            } catch (Exception e) {
                event.getHook().editOriginalEmbeds(createEmbed("未取得連線至該頻道的權限", 0xFF0000)).queue();
                System.out.println(e.getMessage());
                return;
            }
            CountDownLatch countDownLatch = new CountDownLatch(1);
            guild.getAudioManager().setConnectionListener(new ConnectionListener() {
                @Override
                public void onPing(long l) {

                }

                @Override
                public void onStatusChange(@NotNull ConnectionStatus connectionStatus) {
                    if (connectionStatus == ConnectionStatus.CONNECTED)
                        countDownLatch.countDown();
                }

                @Override
                public void onUserSpeaking(@NotNull User user, boolean b) {

                }
            });
            try {
                countDownLatch.await(2000, TimeUnit.MILLISECONDS);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
            guild.getAudioManager().setConnectionListener(null);
            // 新增bot到頻道
            musicBotManager.setBotToChannel(guild.getId(), vc.getId(), this);
            guild.getSelfMember().deafen(true).queue();
        }
    }

    public GuildMusicManager getMusicManager(String guildID) {
        Guild guild = jda.getGuildById(guildID);
        if (guild == null)
            return null;
        return getMusicManager(guild);
    }

    private @NotNull GuildMusicManager getMusicManager(@NotNull Guild guild) {
        GuildMusicManager musicManager = musicManagers.get(guild.getId());
        if (musicManager == null) {
            musicManager = new GuildMusicManager(playerManager, guild);
            musicManager.scheduler.setManagerEvent(event);
            musicManagers.put(guild.getId(), musicManager);
        }
        return musicManager;
    }

    public void setActivity(String @NotNull [] msg) {
        if (msg[0].equals("STREAMING")) {
            // name, url
            jda.getPresence().setActivity(Activity.of(Activity.ActivityType.STREAMING, msg[1], msg[2]));
        } else {
            Activity.ActivityType type = Activity.ActivityType.valueOf(msg[0]);
            jda.getPresence().setActivity(Activity.of(type, msg[1]));
        }
    }

    public String getID() {
        return botID;
    }
}