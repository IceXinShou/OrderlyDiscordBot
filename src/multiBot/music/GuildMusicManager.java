package multiBot.music;

import com.sedmelluq.discord.lavaplayer.player.AudioPlayer;
import com.sedmelluq.discord.lavaplayer.player.AudioPlayerManager;
import com.sedmelluq.discord.lavaplayer.track.AudioPlaylist;
import com.sedmelluq.discord.lavaplayer.track.AudioTrack;
import multiBot.MusicBot;
import net.dv8tion.jda.api.entities.Guild;
import net.dv8tion.jda.api.events.interaction.GenericInteractionCreateEvent;
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

    public final Guild guild;

    /**
     * Creates a player and a track scheduler.
     *
     * @param manager Audio player manager to use for creating the player.
     */
    public GuildMusicManager(AudioPlayerManager manager, Guild guild) {
        player = manager.createPlayer();
        scheduler = new TrackScheduler(player, guild);
        this.guild = guild;
        guild.getAudioManager().setSendingHandler(new AudioPlayerSendHandler(player));
        player.addListener(scheduler);
    }

    @SuppressWarnings("ALL")
    public interface Event {
        void trackStart(AudioTrack track, GenericInteractionCreateEvent event, Guild guild, MusicBot musicBot, boolean search);

        void addToQueue(AudioTrack track, GenericInteractionCreateEvent event, boolean search, boolean playNow);

        void addPlayerListToQueue(AudioPlaylist track, GenericInteractionCreateEvent event);

        void noMoreTrack(GenericInteractionCreateEvent event, Guild guild);

        void skip(AudioTrack lastTrack, SlashCommandEvent event, Guild guild);

        void remove(AudioTrack removedTrack, SlashCommandEvent event);

        void repeat(AudioTrack track, boolean repeatState, SlashCommandEvent event);

        void loop(boolean loopState, SlashCommandEvent event);

        void pauseStateChange(boolean pause, SlashCommandEvent event, Guild guild);

        void volumeChange(int volume, SlashCommandEvent event);

    }
}
