package main.java.music;

import com.sedmelluq.discord.lavaplayer.player.AudioLoadResultHandler;
import com.sedmelluq.discord.lavaplayer.player.AudioPlayerManager;
import com.sedmelluq.discord.lavaplayer.player.DefaultAudioPlayerManager;
import com.sedmelluq.discord.lavaplayer.source.AudioSourceManagers;
import com.sedmelluq.discord.lavaplayer.tools.FriendlyException;
import com.sedmelluq.discord.lavaplayer.track.AudioPlaylist;
import com.sedmelluq.discord.lavaplayer.track.AudioTrack;
import net.dv8tion.jda.api.entities.Guild;
import net.dv8tion.jda.api.entities.VoiceChannel;
import net.dv8tion.jda.api.events.interaction.SlashCommandEvent;

import java.util.HashMap;
import java.util.Map;
import java.util.Objects;

import static main.java.SlashCommandOption.COUNT;
import static main.java.SlashCommandOption.URL;
import static main.java.event.Log.logChannel;
import static main.java.util.EmbedUtil.createEmbed;
import static main.java.util.GuildUtil.guildID;

public class MusicCommands implements GuildMusicManager.Event {
    private final AudioPlayerManager playerManager;
    public Map<Long, GuildMusicManager> musicManagers;

    private int commandState;

    public MusicCommands() {
        this.musicManagers = new HashMap<>();

        this.playerManager = new DefaultAudioPlayerManager();
        AudioSourceManagers.registerRemoteSources(playerManager);
        AudioSourceManagers.registerLocalSource(playerManager);
    }

    public int onCommand(SlashCommandEvent event) {
        commandState = 1;

        switch (event.getName()) {
            // 全域頻道
            case "play": //
                checkVcState(event);
                String url = Objects.requireNonNull(event.getOption(URL)).getAsString();
                loadAndPlay(event, getGuildAudioPlayer(event.getGuild()), url);
                break;
            case "skip":
                checkVcState(event);
                getGuildAudioPlayer(event.getGuild()).scheduler.nextTrack(event);
                break;
            case "repeat":
                checkVcState(event);
                getGuildAudioPlayer(event.getGuild()).scheduler.toggleRepeat(event);
                break;
            case "pause":
                checkVcState(event);
                getGuildAudioPlayer(event.getGuild()).scheduler.pause(event);
                break;
            case "volume":
                checkVcState(event);
                Integer volume = null;
                if (event.getOption(COUNT) != null)
                    volume = (int) event.getOption(COUNT).getAsLong();
                getGuildAudioPlayer(event.getGuild()).scheduler.changeVolume(volume, event);
                break;
            default:
                commandState = 0;
        }
        return commandState;
    }

    private void checkVcState(SlashCommandEvent event) {
        if (!event.getMember().getVoiceState().inVoiceChannel()) {
            event.replyEmbeds(createEmbed("沒有在語音頻道", 0xFF0000)).setEphemeral(true).queue();
            return;
        } else if (event.getGuild().getSelfMember().getVoiceState().inVoiceChannel() && event.getMember().getVoiceState().getChannel().equals(event.getGuild().getSelfMember().getVoiceState().getChannel())) {
            event.replyEmbeds(createEmbed("機器人已被其他頻道所使用", 0xFF0000)).setEphemeral(true).queue();
            return;
        }
    }

    public void loadAndPlay(final SlashCommandEvent event, final GuildMusicManager manager, final String trackUrl) {
        VoiceChannel vc = event.getMember().getVoiceState().getChannel();

        //取得音樂
        playerManager.loadItemOrdered(musicManagers, trackUrl, new AudioLoadResultHandler() {
            @Override
            public void trackLoaded(AudioTrack track) {
                play(track, vc, manager, event);
            }

            @Override
            public void playlistLoaded(AudioPlaylist playlist) {
                play(playlist, vc, manager, event);
            }

            @Override
            public void noMatches() {
                event.replyEmbeds(createEmbed("查無此網址: " + trackUrl, 0xFF0000)).setEphemeral(true).queue();
            }

            @Override
            public void loadFailed(FriendlyException exception) {
                event.replyEmbeds(createEmbed("無法撥放此網址: " + exception.getMessage(), 0xFF0000)).setEphemeral(true).queue();
            }
        });

    }


    /**
     * event
     */
    @Override
    public void playStart(AudioTrack track, SlashCommandEvent event) {
        if (event != null) {
            event.replyEmbeds(createEmbed("開始撥放 `" + track.getInfo().title + '`', 0xBCE9B6)).setEphemeral(true).queue();
            if (!event.getGuild().getId().equals(guildID))
                logChannel.sendMessage("開始撥放 `" + track.getInfo().title + "`").queue();
        }
    }

    @Override
    public void addToQueue(AudioTrack track, SlashCommandEvent event) {
        event.replyEmbeds(createEmbed("加入撥放清單: `" + track.getInfo().title + "`", 0xBCE9B6)).setEphemeral(true).queue();
        if (!event.getGuild().getId().equals(guildID))
            logChannel.sendMessage("加入撥放清單: `" + track.getInfo().title + "` ").queue();
    }

    @Override
    public void addPlayerListToQueue(AudioPlaylist playlist, SlashCommandEvent event) {
        event.replyEmbeds(createEmbed("加入 `" + playlist.getName() + "` ", 0xBCE9B6)).setEphemeral(true).queue();
        if (!event.getGuild().getId().equals(guildID))
            logChannel.sendMessage("加入 `" + playlist.getName() + "` ").queue();
    }

    @Override
    public void skip(AudioTrack lastTrack, SlashCommandEvent event) {
        event.replyEmbeds(createEmbed("已跳過", 0xD3DAFF)).setEphemeral(true).queue();
        if (!event.getGuild().getId().equals(guildID))
            logChannel.sendMessage("跳過 `" + lastTrack.getInfo().title + "`").queue();
    }

    @Override
    public void noMoreTrack(SlashCommandEvent event) {
        if (event == null) return;
        if (event.getGuild().getAudioManager().isConnected())
            event.getGuild().getAudioManager().closeAudioConnection();

        event.replyEmbeds(createEmbed("已停止撥放", 0xFF3B7D)).setEphemeral(true).queue();
        if (!event.getGuild().getId().equals(guildID))
            logChannel.sendMessage("停止撥放").queue();
    }

    @Override
    public void repeat(AudioTrack track, boolean repeatState, SlashCommandEvent event) {
        if (repeatState) {
            event.replyEmbeds(createEmbed("單曲循環", 0x7d95b9)).setEphemeral(true).queue();
        } else {
            event.replyEmbeds(createEmbed("正常撥放", 0xAFACCC)).setEphemeral(true).queue();
        }
    }

    @Override
    public void pause(boolean pause, SlashCommandEvent event) {
        event.replyEmbeds(pause ? createEmbed("已暫停撥放", 0xFF3B7D) : createEmbed("已開始撥放", 0x75C44C)).setEphemeral(true).queue();
    }

    @Override
    public void volumeChange(int volume, SlashCommandEvent event) {
        event.replyEmbeds(createEmbed("已將音量設定為: " + volume, 0xD9B99B)).setEphemeral(true).queue();
    }

    /**
     * method
     */
    private void play(AudioPlaylist playlist, VoiceChannel vc, GuildMusicManager manager, SlashCommandEvent event) {
        if (!event.getGuild().getAudioManager().isConnected())
            event.getGuild().getAudioManager().openAudioConnection(vc);

        manager.scheduler.addPlayListToQueue(playlist, event);
    }

    private void play(AudioTrack track, VoiceChannel vc, GuildMusicManager manager, SlashCommandEvent event) {
        if (!event.getGuild().getAudioManager().isConnected())
            event.getGuild().getAudioManager().openAudioConnection(vc);

        manager.scheduler.queue(track, event);
    }

    private synchronized GuildMusicManager getGuildAudioPlayer(Guild guild) {
        long guildId = Long.parseLong(guild.getId());
        GuildMusicManager musicManager = musicManagers.get(guildId);

        if (musicManager == null) {
            musicManager = new GuildMusicManager(playerManager);
            musicManagers.put(guildId, musicManager);
        }

        musicManager.scheduler.setManagerEvent(this);
        guild.getAudioManager().setSendingHandler(musicManager.getSendHandler());

        return musicManager;
    }


}
