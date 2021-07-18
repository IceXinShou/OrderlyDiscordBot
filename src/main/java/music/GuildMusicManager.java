package main.java.music;

import com.sedmelluq.discord.lavaplayer.player.AudioPlayer;
import com.sedmelluq.discord.lavaplayer.player.AudioPlayerManager;
import com.sedmelluq.discord.lavaplayer.track.AudioPlaylist;
import com.sedmelluq.discord.lavaplayer.track.AudioTrack;
import net.dv8tion.jda.api.events.interaction.SlashCommandEvent;

/**
 * Holder for both the player and a track scheduler for one guild.
 */
public class GuildMusicManager {
    /**
     * Audio player for the guild.
     */
    public final AudioPlayer player;
    /**
     * Track scheduler for the player.
     */
    public final TrackScheduler scheduler;

    /**
     * Creates a player and a track scheduler.
     *
     * @param manager Audio player manager to use for creating the player.
     */
    public GuildMusicManager(AudioPlayerManager manager) {
        player = manager.createPlayer();
        scheduler = new TrackScheduler(player);
        player.addListener(scheduler);
    }

    public interface Event {
        void playStart(AudioTrack track, SlashCommandEvent event);

        void addToQueue(AudioTrack track, SlashCommandEvent event);

        void addPlayerListToQueue(AudioPlaylist track, SlashCommandEvent event);

        void noMoreTrack(SlashCommandEvent event);

        void skip(AudioTrack lastTrack, SlashCommandEvent event);

        void repeat(AudioTrack track, boolean repeatState, SlashCommandEvent event);

        void pause(boolean pause, SlashCommandEvent event);

        void volumeChange(int volume, SlashCommandEvent event);
    }

    /**
     * @return Wrapper around AudioPlayer to use it as an AudioSendHandler.
     */
    public AudioPlayerSendHandler getSendHandler() {
        return new AudioPlayerSendHandler(player);
    }
}
