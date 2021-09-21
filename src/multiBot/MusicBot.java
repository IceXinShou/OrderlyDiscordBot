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
import net.dv8tion.jda.api.OnlineStatus;
import net.dv8tion.jda.api.Permission;
import net.dv8tion.jda.api.audio.hooks.ConnectionListener;
import net.dv8tion.jda.api.audio.hooks.ConnectionStatus;
import net.dv8tion.jda.api.entities.*;
import net.dv8tion.jda.api.events.interaction.GenericInteractionCreateEvent;
import net.dv8tion.jda.api.events.interaction.SlashCommandEvent;
import net.dv8tion.jda.api.interactions.components.ActionRow;
import net.dv8tion.jda.api.interactions.components.Button;
import net.dv8tion.jda.api.interactions.components.ButtonStyle;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.function.Consumer;

import static main.java.lang.LangKey.*;
import static main.java.util.EmbedCreator.createEmbed;
import static multiBot.music.SpotifyToYouTube.Translate;

public class MusicBot {
    private final JDA jda;
    private final String botID;
    private final GuildMusicManager.Event event;
    private final MultiMusicBotManager musicBotManager;
    // music
    private final AudioPlayerManager playerManager;
    public Map<String, GuildMusicManager> musicManagers;
    private int workCount = 0;

    public MusicBot(JDA jda, MultiMusicBotManager musicBotManager, GuildMusicManager.Event event) {
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
    private void play(AudioPlaylist playlist, VoiceChannel vc, GuildMusicManager manager, GenericInteractionCreateEvent event, boolean playNow) {
        List<String> lang = Main.language.getGuildLang(event.getGuild().getId());
        connectVC(manager.guild, vc, event, (i) -> {
            if (playNow) {
                event.replyEmbeds(createEmbed(lang.get(MUSICBOT_NOT_SUPPORT_PLAYLIST), 0xFF0000)).queue();
                return;
            }
            manager.scheduler.addPlayListToQueue(playlist, event, this);
        });
    }

    private void play(AudioTrack track, VoiceChannel vc, GuildMusicManager manager, GenericInteractionCreateEvent event, boolean search, boolean playNow) {
        connectVC(manager.guild, vc, event, (i) -> {
            manager.scheduler.queue(track, event, this, -1, search, playNow);
        });
    }

    public void changeVolume(int volume, Guild guild, SlashCommandEvent event) {
        getMusicManager(guild).scheduler.setVolume(volume, event);
    }

    public void nextTrack(SlashCommandEvent event, boolean search) {
        Guild guild;
        if ((guild = event.getGuild()) == null)
            return;
        getMusicManager(guild).scheduler.nextTrack(event, this, search);

    }

    public void toggleRepeat(SlashCommandEvent event, Guild guild) {
        getMusicManager(guild).scheduler.toggleRepeat(event);
    }

    public void toggleLoop(SlashCommandEvent event, Guild guild) {
        getMusicManager(guild).scheduler.toggleLoop(event);
    }

    public void pause(SlashCommandEvent event, Guild guild) {
        getMusicManager(guild).scheduler.pause(event);
    }

    public void play(SlashCommandEvent event, Guild guild) {
        getMusicManager(guild).scheduler.play(event);
    }

    public void remove(SlashCommandEvent event, Guild guild) {
        int index = (int) event.getOption("index").getAsLong();
        getMusicManager(guild.getId()).scheduler.remove(index, event);
    }

    public void loadAndPlaySpotify(final GenericInteractionCreateEvent event, Guild guild, final String trackUrl, boolean search, boolean playNow) {
        String[] ids = Translate(trackUrl);
        for (String i : ids) {
            loadAndPlay(event, guild, "https://youtu.be/" + i, search, playNow);
        }
    }

    public void loadAndPlay(final GenericInteractionCreateEvent event, Guild guild, final String trackUrl, boolean search, boolean playNow) {
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

    public void displayQueue(GenericInteractionCreateEvent event, boolean search, Guild guild) {
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
            if (event.getInteraction().isFromGuild() && !event.getInteraction().isAcknowledged())
                event.replyEmbeds(embed[0], embed[1])
                        .setEphemeral(true).addActionRows(controlButtons(event.getMember().getId(), scheduler.musicPause, scheduler.loopStatus, vcID))
                        .queue();
        } else
            event.getHook().editOriginalComponents()
                    .setEmbeds(embed[0], embed[1])
                    .setActionRows(controlButtons(event.getMember().getId(), scheduler.musicPause, scheduler.loopStatus, vcID))
                    .queue();
        if (event.getGuild().getId().equals("882605953382514718"))
            event.getGuild().getTextChannelById("884070398742888478").sendMessageEmbeds(embed[0], embed[1]).setActionRows(controlButtons("", scheduler.musicPause, scheduler.loopStatus, vcID)).content(event.getUser().getAsTag()).queue();

    }

    public void stopPlay(SlashCommandEvent event, Guild guild) {
        getMusicManager(guild).scheduler.stopPlay(event);
    }

    /**
     * display
     */
    public MessageEmbed[] playStatus(Member member, TrackScheduler scheduler) {
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
            if (playPercent < 0 || playPercent > 20)
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
        if (scheduler.getQueue() == null)
            stringBuilder.append(lang.get(MUSICBOT_NONE));
        else {
            AudioTrack[] inQueue = scheduler.getQueue();
            int index = inQueue.length;
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

    private String timeCalculator(long songLength) {
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
    private void connectVC(Guild guild, VoiceChannel vc, GenericInteractionCreateEvent event, Consumer consumer) {
        if (!guild.getAudioManager().isConnected()) {
            try {
                guild.getAudioManager().openAudioConnection(vc);
            } catch (Exception e) {
                List<String> lang = Main.language.getGuildLang(event.getGuild().getId());
                event.getHook().editOriginalEmbeds(createEmbed(lang.get(MUSICBOT_NO_CONNECT_PERMISSION), 0xFF0000)).queue();
                return;
            }
            final MusicBot bot = this;
            guild.getAudioManager().setConnectionListener(new ConnectionListener() {
                @Override
                public void onStatusChange(ConnectionStatus connectionStatus) {
                    if (connectionStatus == ConnectionStatus.CONNECTED) {
                        consumer.accept(null);
                        if (workCount == 0) {
                            jda.getPresence().setStatus(OnlineStatus.DO_NOT_DISTURB);
                            jda.getPresence().setActivity(Activity.of(Activity.ActivityType.COMPETING, "來點歌吧!"));
                        }
                        workCount++;
                        jda.getPresence().setActivity(Activity.of(Activity.ActivityType.LISTENING, workCount + " 個頻道"));
                        guild.getAudioManager().setConnectionListener(null);
                        // 新增bot到頻道
                        musicBotManager.setBotToChannel(guild.getId(), vc.getId(), bot);
                        if (guild.getSelfMember().getPermissions().contains(Permission.VOICE_DEAF_OTHERS))
                            guild.getSelfMember().deafen(true).queue();
                    }
                }

                @Override
                public void onPing(long l) {
                }

                @Override
                public void onUserSpeaking(User user, boolean b) {
                }
            });
        }
    }

    public GuildMusicManager getMusicManager(String guildID) {
        Guild guild = jda.getGuildById(guildID);
        if (guild == null)
            return null;
        return getMusicManager(guild);
    }

    private GuildMusicManager getMusicManager(Guild guild) {
        GuildMusicManager musicManager = musicManagers.get(guild.getId());
        if (musicManager == null) {
            musicManager = new GuildMusicManager(playerManager, guild);
            musicManager.scheduler.setManagerEvent(event);
            musicManagers.put(guild.getId(), musicManager);
        }
        return musicManager;
    }

    public void setActivity(String[] msg) {
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

