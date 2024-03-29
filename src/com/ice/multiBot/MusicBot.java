package com.ice.multiBot;

import com.sedmelluq.discord.lavaplayer.player.AudioLoadResultHandler;
import com.sedmelluq.discord.lavaplayer.player.AudioPlayerManager;
import com.sedmelluq.discord.lavaplayer.player.DefaultAudioPlayerManager;
import com.sedmelluq.discord.lavaplayer.source.AudioSourceManagers;
import com.sedmelluq.discord.lavaplayer.tools.FriendlyException;
import com.sedmelluq.discord.lavaplayer.track.AudioPlaylist;
import com.sedmelluq.discord.lavaplayer.track.AudioTrack;
import com.ice.main.BotSetting;
import com.ice.main.Main;
import com.ice.multiBot.music.GuildMusicManager;
import com.ice.multiBot.music.MusicInfoData;
import com.ice.multiBot.music.TrackScheduler;
import net.dv8tion.jda.api.JDA;
import net.dv8tion.jda.api.OnlineStatus;
import net.dv8tion.jda.api.Permission;
import net.dv8tion.jda.api.audio.hooks.ConnectionListener;
import net.dv8tion.jda.api.audio.hooks.ConnectionStatus;
import net.dv8tion.jda.api.entities.*;
import net.dv8tion.jda.api.events.interaction.GenericInteractionCreateEvent;
import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent;
import net.dv8tion.jda.api.events.interaction.component.SelectMenuInteractionEvent;
import net.dv8tion.jda.api.interactions.components.ActionRow;
import net.dv8tion.jda.api.interactions.components.buttons.Button;
import net.dv8tion.jda.api.interactions.components.buttons.ButtonStyle;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.function.Consumer;

import static com.ice.main.lang.LangKey.*;
import static com.ice.main.util.EmbedCreator.createEmbed;

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
                if (event instanceof SelectMenuInteractionEvent)
                    ((SelectMenuInteractionEvent) event).replyEmbeds(createEmbed(lang.get(MUSICBOT_NOT_SUPPORT_PLAYLIST), 0xFF0000)).queue();
                else if (event instanceof SlashCommandInteractionEvent)
                    ((SlashCommandInteractionEvent) event).replyEmbeds(createEmbed(lang.get(MUSICBOT_NOT_SUPPORT_PLAYLIST), 0xFF0000)).queue();
                return;
            }
            manager.scheduler.addPlayListToQueue(playlist, event);
        });
    }

    private void play(AudioTrack track, VoiceChannel vc, GuildMusicManager manager,
                      GenericInteractionCreateEvent event, boolean search, boolean playNow, SelectMenuInteractionEvent SelectMenuInteractionEvent) {
        connectVC(manager.guild, vc, event, (i) -> manager.scheduler.queue(track, event, -1, search, playNow, SelectMenuInteractionEvent));
    }

    public void changeVolume(int volume, Guild guild, SlashCommandInteractionEvent event) {
        getMusicManager(guild).scheduler.setVolume(volume, event);
    }

    public void nextTrack(SlashCommandInteractionEvent event, boolean search) {
        Guild guild;
        if ((guild = event.getGuild()) == null)
            return;
        getMusicManager(guild).scheduler.nextTrack(event, search);
    }

    public void toggleRepeat(SlashCommandInteractionEvent event, Guild guild) {
        getMusicManager(guild).scheduler.toggleRepeat(event);
    }

    public void toggleLoop(SlashCommandInteractionEvent event, Guild guild) {
        getMusicManager(guild).scheduler.toggleLoop(event);
    }

    public void pause(SlashCommandInteractionEvent event, Guild guild) {
        getMusicManager(guild).scheduler.pause(event);
    }

    public void play(SlashCommandInteractionEvent event, Guild guild) {
        getMusicManager(guild).scheduler.play(event);
    }

    public void remove(SlashCommandInteractionEvent event, Guild guild) {
        int index = (int) event.getOption("index").getAsLong();
        getMusicManager(guild.getId()).scheduler.remove(index, event);
    }

    public void loadAndPlaySpotify(final GenericInteractionCreateEvent event, Guild guild,
                                   final String trackUrl, boolean search, boolean playNow, SelectMenuInteractionEvent SelectMenuInteractionEvent) {
        String[] ids = BotSetting.spotifyToYouTube.translate(trackUrl);
        for (String i : ids) {
            loadAndPlay(event, guild, "https://youtu.be/" + i, search, playNow, SelectMenuInteractionEvent);
        }
    }

    public void loadAndPlay(final GenericInteractionCreateEvent event, Guild guild,
                            final String trackUrl, boolean search, boolean playNow, SelectMenuInteractionEvent SelectMenuInteractionEvent) {
        List<String> lang = Main.language.getGuildLang(guild.getId());
        VoiceChannel vc = (VoiceChannel) event.getMember().getVoiceState().getChannel();
        GuildMusicManager manager = getMusicManager(jda.getGuildById(guild.getId()));
        // 取得音樂
        playerManager.loadItemOrdered(musicManagers, trackUrl, new AudioLoadResultHandler() {
            @Override
            public void trackLoaded(AudioTrack track) {
                play(track, vc, manager, event, search, playNow, SelectMenuInteractionEvent);
            }

            @Override
            public void playlistLoaded(AudioPlaylist playlist) {
                play(playlist, vc, manager, event, playNow);
            }

            @Override
            public void noMatches() {
                try {
                    if (event instanceof SelectMenuInteractionEvent)
                        ((SelectMenuInteractionEvent) event).getHook().editOriginalEmbeds(createEmbed(lang.get(MUSICBOT_URL_NOT_FOUND) + ": " + trackUrl, 0xFF0000)).queue();
                    else if (event instanceof SlashCommandInteractionEvent)
                        ((SlashCommandInteractionEvent) event).getHook().editOriginalEmbeds(createEmbed(lang.get(MUSICBOT_URL_NOT_FOUND) + ": " + trackUrl, 0xFF0000)).queue();
                } catch (Exception ignored) {
                }
            }

            @Override
            public void loadFailed(FriendlyException exception) {
                try {
                    if (event instanceof SelectMenuInteractionEvent)
                        ((SelectMenuInteractionEvent) event).getHook().editOriginalEmbeds(createEmbed(lang.get(MUSICBOT_CANT_PLAY_URL) + ": " + exception.getMessage(), 0xFF0000)).queue();
                    else if (event instanceof SlashCommandInteractionEvent)
                        ((SlashCommandInteractionEvent) event).getHook().editOriginalEmbeds(createEmbed(lang.get(MUSICBOT_CANT_PLAY_URL) + ": " + exception.getMessage(), 0xFF0000)).queue();
                } catch (Exception ignored) {
                }
            }
        });

    }

    public void displayQueue(GenericInteractionCreateEvent event, boolean search, Guild guild, SelectMenuInteractionEvent SelectMenuInteractionEvent) {
        List<String> lang = Main.language.getGuildLang(guild.getId());
        GuildMusicManager musicManager = getMusicManager(guild);
        TrackScheduler scheduler = musicManager.scheduler;
        if (scheduler.musicInfo == null) {
            try {
                if (event instanceof SelectMenuInteractionEvent)
                    ((SelectMenuInteractionEvent) event).getHook().editOriginalEmbeds(createEmbed(lang.get(MUSICBOT_NO_MUSIC), 0xFF0000)).queue();
            } catch (Exception ignored) {
            }
            return;
        }

        scheduler.calculatePauseTime();
        MessageEmbed[] embed = playStatus(event.getMember(), scheduler);
        if (musicManager.guild.getSelfMember().getVoiceState().getChannel() == null) {
            if (search)
                SelectMenuInteractionEvent.replyEmbeds(createEmbed(lang.get(MUSICBOT_NO_CONNECT_PERMISSION), 0xFF0000)).setEphemeral(true).queue();
            else {
                if (event instanceof SelectMenuInteractionEvent)
                    ((SelectMenuInteractionEvent) event).getHook().editOriginalEmbeds(createEmbed(lang.get(MUSICBOT_NO_CONNECT_PERMISSION), 0xFF0000)).queue();
                else if (event instanceof SlashCommandInteractionEvent)
                    ((SlashCommandInteractionEvent) event).getHook().editOriginalEmbeds(createEmbed(lang.get(MUSICBOT_NO_CONNECT_PERMISSION), 0xFF0000)).queue();
            }
            return;
        }
        String vcID = musicManager.guild.getSelfMember().getVoiceState().getChannel().getId();
        if (search) {
            SelectMenuInteractionEvent.replyEmbeds(embed[0], embed[1]).setEphemeral(true)
                    .addActionRows(controlButtons(event.getMember().getId(), scheduler.musicPause, scheduler.loopStatus, vcID))
                    .queue();
        } else {
            if (event instanceof SelectMenuInteractionEvent)
                ((SelectMenuInteractionEvent) event).getHook().editOriginalComponents()
                        .setEmbeds(embed[0], embed[1])
                        .setActionRows(controlButtons(event.getMember().getId(), scheduler.musicPause, scheduler.loopStatus, vcID))
                        .queue();
            else if (event instanceof SlashCommandInteractionEvent)
                ((SlashCommandInteractionEvent) event).getHook().editOriginalComponents()
                        .setEmbeds(embed[0], embed[1])
                        .setActionRows(controlButtons(event.getMember().getId(), scheduler.musicPause, scheduler.loopStatus, vcID))
                        .queue();
        }
        if (event.getGuild().getId().equals("882605953382514718"))
            event.getGuild().getTextChannelById("884070398742888478").sendMessageEmbeds(embed[0], embed[1])
                    .setActionRows(controlButtons("", scheduler.musicPause, scheduler.loopStatus, vcID)).content(event.getUser().getAsTag()).queue();

    }

    public void stopPlay(SlashCommandInteractionEvent event, Guild guild) {
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
                    .append(scheduler.loopStatus == 0 ? " <" + lang.get(MUSICBOT_NORMAL_PLAY) + ">\n" :
                            (scheduler.loopStatus == 1 ? " <" + lang.get(MUSICBOT_LOOP_PLAY) + ">\n" : " <" + lang.get(MUSICBOT_REPEAT_PLAY) + ">\n"));

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
                stringBuilder.append("[").append(index--).append("] [")
                        .append(track.getInfo().title).append("](").append(track.getInfo().uri);
                if (track.getInfo().isStream)
                    stringBuilder.append(") **[LIVE]**\n");
                else
                    stringBuilder.append(")\n");
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

                if (event instanceof SelectMenuInteractionEvent)
                    ((SelectMenuInteractionEvent) event).getHook().editOriginalEmbeds(createEmbed(lang.get(MUSICBOT_NO_CONNECT_PERMISSION), 0xFF0000)).queue();
                else if (event instanceof SlashCommandInteractionEvent)
                    ((SlashCommandInteractionEvent) event).getHook().editOriginalEmbeds(createEmbed(lang.get(MUSICBOT_NO_CONNECT_PERMISSION), 0xFF0000)).queue();
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
        } else
            consumer.accept(null);
    }

    public void disconnect(Guild guild) {
        workCount--;
        if (workCount < 0)
            workCount = 0;
        musicManagers.remove(guild.getId());
        if (workCount == 0) {
            jda.getPresence().setStatus(OnlineStatus.IDLE);
            jda.getPresence().setActivity(Activity.of(Activity.ActivityType.COMPETING, "來點歌吧!"));
        } else {
            jda.getPresence().setActivity(Activity.of(Activity.ActivityType.LISTENING, workCount + " 個頻道"));
        }
        if (guild.getAudioManager().isConnected()) {
            // 從頻道移除bot
            musicBotManager.setBotToChannel(guild.getId(), guild.getAudioManager().getConnectedChannel().getId(), null);
            guild.getAudioManager().closeAudioConnection();
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
            musicManager = new GuildMusicManager(playerManager, guild, this);
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
