package multiBot;

import com.sedmelluq.discord.lavaplayer.player.AudioLoadResultHandler;
import com.sedmelluq.discord.lavaplayer.player.AudioPlayerManager;
import com.sedmelluq.discord.lavaplayer.player.DefaultAudioPlayerManager;
import com.sedmelluq.discord.lavaplayer.source.AudioSourceManagers;
import com.sedmelluq.discord.lavaplayer.tools.FriendlyException;
import com.sedmelluq.discord.lavaplayer.track.AudioPlaylist;
import com.sedmelluq.discord.lavaplayer.track.AudioTrack;
import main.java.Main;
import multiBot.music.GuildMusicManager;
import multiBot.music.MusicInfoData;
import multiBot.music.TrackScheduler;
import net.dv8tion.jda.api.JDA;
import net.dv8tion.jda.api.Permission;
import net.dv8tion.jda.api.audio.hooks.ConnectionListener;
import net.dv8tion.jda.api.audio.hooks.ConnectionStatus;
import net.dv8tion.jda.api.entities.*;
import net.dv8tion.jda.api.events.interaction.GenericInteractionCreateEvent;
import net.dv8tion.jda.api.events.interaction.SlashCommandEvent;
import net.dv8tion.jda.api.interactions.components.ActionRow;
import net.dv8tion.jda.api.interactions.components.Button;
import net.dv8tion.jda.api.interactions.components.ButtonStyle;
import org.jetbrains.annotations.NotNull;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;

import static main.java.lang.LangKey.*;
import static main.java.util.EmbedCreator.createEmbed;

public class MusicBot {
    private final JDA jda;
    private final String botID;
    private final GuildMusicManager.Event event;
    private final MultiMusicBotManager musicBotManager;


    // music
    private final AudioPlayerManager playerManager;
    public Map<String, GuildMusicManager> musicManagers;

    public MusicBot(@NotNull JDA jda, MultiMusicBotManager musicBotManager, GuildMusicManager.Event event) {
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
    private void play(AudioPlaylist playlist, VoiceChannel vc, @NotNull GuildMusicManager manager, @NotNull GenericInteractionCreateEvent event, boolean playNow) {
        List<String> lang = Main.language.getGuildLang(event.getGuild().getId());
        connectVC(manager.guild, vc, event);
        if (playNow) {
            event.replyEmbeds(createEmbed(lang.get(MUSICBOT_NOT_SUPPORT_PLAYLIST), 0xFF0000)).queue();
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
        Guild guild;
        if ((guild = event.getGuild()) == null)
            return;
        getMusicManager(guild).scheduler.nextTrack(event, this, search);

    }

    public void toggleRepeat(@NotNull SlashCommandEvent event, Guild guild) {
        getMusicManager(guild).scheduler.toggleRepeat(event);
    }

    public void toggleLoop(@NotNull SlashCommandEvent event, Guild guild) {
        getMusicManager(guild).scheduler.toggleLoop(event);
    }

    public void pause(SlashCommandEvent event, Guild guild) {
        getMusicManager(guild).scheduler.pause(event);
    }

    public void play(SlashCommandEvent event, Guild guild) {
        getMusicManager(guild).scheduler.play(event);
    }

    public void remove(@NotNull SlashCommandEvent event, @NotNull Guild guild) {
        int index = (int) event.getOption("index").getAsLong();
        getMusicManager(guild.getId()).scheduler.remove(index, event);
    }

    public void loadAndPlay(final @NotNull GenericInteractionCreateEvent event, @NotNull Guild guild, final String trackUrl, boolean search, boolean playNow) {
        List<String> lang = Main.language.getGuildLang(guild.getId());
        VoiceChannel vc = event.getMember().getVoiceState().getChannel();
        GuildMusicManager manager = getMusicManager(jda.getGuildById(guild.getId()));
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
                    event.getHook().editOriginalEmbeds(createEmbed(lang.get(MUSICBOT_URL_NOT_FOUND) + ": " + trackUrl, 0xFF0000)).queue();
                } catch (Exception ignored) {
                }
            }

            @Override
            public void loadFailed(FriendlyException exception) {
                try {
                    event.getHook().editOriginalEmbeds(createEmbed(lang.get(MUSICBOT_CANT_PLAY_URL) + ": " + exception.getMessage(), 0xFF0000)).queue();
                } catch (Exception ignored) {
                }
            }
        });

    }

    public void displayQueue(@NotNull GenericInteractionCreateEvent event, boolean search, @NotNull Guild guild) {
        List<String> lang = Main.language.getGuildLang(guild.getId());
        GuildMusicManager musicManager = getMusicManager(guild);
        TrackScheduler scheduler = musicManager.scheduler;
        if (scheduler.musicInfo == null) {
            event.getHook().editOriginalEmbeds(createEmbed(lang.get(MUSICBOT_NO_MUSIC), 0xFF0000)).queue();
            return;
        }

        scheduler.calculatePauseTime();
        MessageEmbed[] embed = playStatus(event.getMember(), scheduler);
        if (musicManager.guild.getSelfMember().getVoiceState().getChannel() == null) {
            if (search)
                event.replyEmbeds(createEmbed(lang.get(MUSICBOT_NO_CONNECT_PERMISSION), 0xFF0000)).setEphemeral(true).queue();
            else
                event.getHook().editOriginalEmbeds(createEmbed(lang.get(MUSICBOT_NO_CONNECT_PERMISSION), 0xFF0000)).queue();
            return;
        }
        String vcID = musicManager.guild.getSelfMember().getVoiceState().getChannel().getId();
        if (search) {
            event.replyEmbeds(embed[0], embed[1]).setEphemeral(true)
                    .addActionRows(controlButtons(event.getMember().getId(), scheduler.musicPause, scheduler.loopStatus, vcID))
                    .queue();
        } else
            event.getHook().editOriginalComponents()
                    .setEmbeds(embed[0], embed[1])
                    .setActionRows(controlButtons(event.getMember().getId(), scheduler.musicPause, scheduler.loopStatus, vcID))
                    .queue();
    }

    public void disconnect(@NotNull SlashCommandEvent event, Guild guild) {
        getMusicManager(guild).scheduler.stopPlay(event);
    }

    public void stopPlay(Guild guild) {
        getMusicManager(guild).scheduler.stopPlay(null);
    }

    /**
     * display
     */
    public MessageEmbed[] playStatus(@NotNull Member member, @NotNull TrackScheduler scheduler) {
        List<String> lang = Main.language.getGuildLang(member.getGuild().getId());
        // 現在播放資料
        StringBuilder progress = new StringBuilder();
        MessageEmbed nowPlaying;
        // 有歌曲正在播放
        if (scheduler.musicInfo != null) {
            // 進度顯示
            MusicInfoData musicInfo = scheduler.musicInfo;
            int nowPlayingTime = (int) ((System.currentTimeMillis() - scheduler.startPlayTime) / 1000);
            int playPercent = (int) ((nowPlayingTime / (float) (musicInfo.getLength() / 1000)) * 20);
            if (playPercent < 0)
                playPercent = 0;
            progress.append("\n\n**[")
                    .append(timeCalculator(nowPlayingTime))
                    .append("] **")
                    .append("━".repeat(playPercent))
                    .append("❚")
                    .append("─".repeat(20 - playPercent))
                    .append("** [").append(musicInfo.isStream() ? "LIVE" : (timeCalculator((int) (musicInfo.getLength() / 1000))))
                    .append("]**\n");

            // 音量顯示
            int volumePercent = (getMusicManager(member.getGuild()).scheduler.getVolume() / 5);
            progress.append("\n").append("**")
                    .append(lang.get(MUSICBOT_VOLUME))
                    .append(": **")
                    .append("◆".repeat(volumePercent))
                    .append("◇".repeat(20 - volumePercent))
                    .append(scheduler.loopStatus == 0 ? " <" + lang.get(MUSICBOT_NORMAL_PLAY) + ">\n" : (scheduler.loopStatus == 1 ? " <" + lang.get(MUSICBOT_LOOP_PLAY) + ">\n" : " <" + lang.get(MUSICBOT_REPEAT_PLAY) + ">\n"));

            // 組裝
            nowPlaying = createEmbed("**" + musicInfo.getTitle() + "**", "https://www.youtube.com/watch?v=" + musicInfo.getVideoID(),
                    progress.toString(), "" +
                            " \uD83D\uDC40 " + String.format("%,d", musicInfo.getViewCount()) +
                            " | \uD83D\uDC4D " + String.format("%,d", musicInfo.getLikeCount()) +
                            " | \uD83D\uDC4E " + String.format("%,d", musicInfo.getDislikeCount()) +
                            " | \uD83D\uDCAC " + String.format("%,d", musicInfo.getCommentCount()) +
//                            .append(" | \uD83D\uDD0A ").append(String.format("%.2f db", musicInfo.getLoudness()))
                            " | \uD83D\uDCC5 " + musicInfo.getPublishDate().replace(',', '-')
                    , musicInfo.getChannelName(), musicInfo.getChannelURL(), musicInfo.getChannelThumbnailUrl(), musicInfo.getThumbnailUrl(),
                    0xe5b849);
        } else {
            nowPlaying = createEmbed(0xFF0000, "**[" + lang.get(MUSICBOT_NO_PLAYING) + "]**");
        }

        StringBuilder stringBuilder = new StringBuilder();
        if (scheduler.getQueue().size() == 0)
            stringBuilder.append(lang.get(MUSICBOT_NONE));
        else {
            List<AudioTrack> inQueue = scheduler.getQueue();
            int index = inQueue.size();
            for (AudioTrack track : inQueue) {
                stringBuilder.append("[").append(index--).append("] ")
                        .append(track.getInfo().title);
                if (track.getInfo().isStream)
                    stringBuilder.append(" **[LIVE]**");
                stringBuilder.append("\n");
            }
        }


        return new MessageEmbed[]{createEmbed(lang.get(MUSICBOT_WAITING_PLAYLIST), stringBuilder.toString(),
                "",
                null,
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

    @SuppressWarnings("ALL")
    private void connectVC(@NotNull Guild guild, VoiceChannel vc, GenericInteractionCreateEvent event) {
        if (!guild.getAudioManager().isConnected()) {
            try {
                guild.getAudioManager().openAudioConnection(vc);
            } catch (Exception e) {
                List<String> lang = Main.language.getGuildLang(event.getGuild().getId());
                event.getHook().editOriginalEmbeds(createEmbed(lang.get(MUSICBOT_NO_CONNECT_PERMISSION), 0xFF0000)).queue();
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
            if (guild.getSelfMember().getPermissions().contains(Permission.VOICE_DEAF_OTHERS))
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

