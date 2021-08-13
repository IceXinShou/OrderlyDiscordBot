package multiBot.music;

import com.sedmelluq.discord.lavaplayer.player.AudioPlayer;
import com.sedmelluq.discord.lavaplayer.player.event.AudioEventAdapter;
import com.sedmelluq.discord.lavaplayer.track.AudioPlaylist;
import com.sedmelluq.discord.lavaplayer.track.AudioTrack;
import com.sedmelluq.discord.lavaplayer.track.AudioTrackEndReason;
import multiBot.MusicBot;
import net.dv8tion.jda.api.entities.Guild;
import net.dv8tion.jda.api.events.interaction.GenericInteractionCreateEvent;
import net.dv8tion.jda.api.events.interaction.SlashCommandEvent;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.List;

/**
 * This class schedules tracks for the audio player. It contains the queue of tracks.
 */
public class TrackScheduler extends AudioEventAdapter {
    private final Guild guild;
    private final AudioPlayer player;
    private final List<AudioTrack> queue;
    private int lastIndex = 0;
    private int index = 0;

    private GuildMusicManager.Event event;
    // now playing
    private AudioTrack playingTrack;
    public MusicInfoData musicInfo;
    public long startPlayTime;

    // setting
    public boolean repeat = false;
    public boolean loop = false;
    private final int defaultVolume = 40;

    public int loopStatus = 0;

    /**
     * @param player The audio player this scheduler uses
     */
    public TrackScheduler(AudioPlayer player, Guild guild) {
        this.guild = guild;
        this.player = player;
        this.queue = new ArrayList<>();
    }

    public void addPlayListToQueue(@NotNull AudioPlaylist playlist, GenericInteractionCreateEvent event, MusicBot musicBot) {
        List<AudioTrack> trackList = playlist.getTracks();
        if (trackList.size() == 0)
            return;
        // 加入序列
        for (int i = 1; i < trackList.size(); i++) {
            queue.add(trackList.get(i));
        }
        // 嘗試播放
        queue(trackList.get(0), event, musicBot, 0, false, false);

        this.event.addPlayerListToQueue(playlist, event);
    }

    /**
     * Add the next track to queue or play right away if nothing is in the queue.
     *
     * @param track The track to play or add to queue.
     */
    public void queue(AudioTrack track, GenericInteractionCreateEvent event, MusicBot musicBot, int position, boolean search, boolean playNow) {
        // Calling startTrack with the noInterrupt set to true will start the track only if nothing is currently playing. If
        // something is playing, it returns false and does nothing. In that case the player was already playing so this
        // track goes to the queue instead.
        if (position != -1)
            queue.add(position, track);
        else if (playNow && queue.size() > 0) {
            queue.add(index + 1, track);
            if (repeat) {
                index++;
                playingTrack = track;
            }
            nextTrack(event, musicBot, search);
            return;
        } else
            queue.add(track);
        if (player.startTrack(track, true)) {
            // 開始撥放
            playingTrack = track;
            startPlayTime = System.currentTimeMillis();
            musicInfo = new MusicInfoData(track);
            calculateNormalized(musicInfo.getLoudness());
            this.event.trackStart(track, event, guild, musicBot, search);
        } else {
            // 加入序列
            this.event.addToQueue(track, event, search, playNow);
        }
    }

    public void remove(int index, SlashCommandEvent event) {
        if (index > 0x7FFFFFFE)
            index = 0x7FFFFFFE;
        if (index < 1 || this.index + index + 1 > queue.size()) {
            this.event.remove(null, event);
            return;
        }
        this.event.remove(queue.remove(this.index + index), event);
    }

    private boolean playTrack() {
        AudioTrack track;
        if (repeat) {
            index = lastIndex;
            track = playingTrack.makeClone();
        } else {
            if (index < 0)
                index = lastIndex;
            if (index >= queue.size()) {
                if (loop) {
                    index = 0;
                } else
                    return false;
            }

            // 取得歌曲
            track = queue.get(index);
            if (index < lastIndex)
                track = track.makeClone();
            if (loop)
                track = track.makeClone();
        }
        if (player.startTrack(track, false)) {
            playingTrack = track;
            startPlayTime = System.currentTimeMillis();
            musicInfo = new MusicInfoData(track);
            calculateNormalized(musicInfo.getLoudness());

            return true;
        }

        return false;
    }

    public void nextTrack(GenericInteractionCreateEvent event, MusicBot musicBot, boolean search) {
        lastIndex = index;
        index++;
        if (playTrack()) {
//            this.event.skip(playingTrack, event, guild);
            this.event.trackStart(playingTrack, event, guild, musicBot, search);
        } else {
            stopPlay(event);
        }
    }

//    public void previousTrack(SlashCommandEvent event) {
//        lastIndex = index;
//        index--;
//        if (playTrack())
//            this.event.trackStart(playingTrack, event, guild, null, false);
//        else
//            stopPlay(event);
//    }

    @Override
    public void onTrackEnd(AudioPlayer player, AudioTrack track, @NotNull AudioTrackEndReason endReason) {
        // Only start the next track if the end reason is suitable for it (FINISHED or LOAD_FAILED)
        if (endReason.mayStartNext) {
            lastIndex = index;
            index++;

            if (!playTrack())
                stopPlay(null);
        }
    }

    public void stopPlay(GenericInteractionCreateEvent event) {
        percent = 0d;
        queue.clear();
        index = 0;
        playingTrack = null;
        musicInfo = null;
        musicPause = false;
        player.setPaused(false);
        player.stopTrack();
        this.event.noMoreTrack(event, guild);
    }

    public List<AudioTrack> getQueue() {
        if (index + 1 >= queue.size())
            return new ArrayList<>();
        List<AudioTrack> out = new ArrayList<>();
        for (int i = queue.size() - 1; i > (loop ? -1 : index); i--) {
            out.add(queue.get(i));
        }
        return out;
    }

    public void toggleRepeat(SlashCommandEvent slashCommandEvent) {
        loop = false;
        repeat = !repeat;
        loopStatus = repeat ? 2 : 0;
        event.repeat(playingTrack, repeat, slashCommandEvent);
    }

    public void toggleLoop(SlashCommandEvent slashCommandEvent) {
        repeat = false;
        loop = !loop;
        loopStatus = loop ? 1 : 0;
        event.loop(loop, slashCommandEvent);
    }

    /**
     * pause
     */
    public boolean musicPause = false;
    private long pauseStart;

    public void pause(SlashCommandEvent event) {
        if (!musicPause) {
            pauseStart = System.currentTimeMillis();
            player.setPaused(true);
            musicPause = true;
        } else {
            calculatePauseTime();
            player.setPaused(false);
            musicPause = false;
        }
        this.event.pauseStateChange(musicPause, event, guild);
    }

    public void play(SlashCommandEvent event) {
        if (musicPause) {
            startPlayTime += System.currentTimeMillis() - pauseStart;
            player.setPaused(false);
            musicPause = false;
        }
        this.event.pauseStateChange(false, event, guild);
    }

    public void switchPause() {
        player.setPaused(musicPause = !musicPause);
        if (musicPause)
            pauseStart = System.currentTimeMillis();
        else
            startPlayTime += System.currentTimeMillis() - pauseStart;

        this.event.pauseStateChange(musicPause, null, guild);
    }

    public void calculatePauseTime() {
        if (playingTrack != null && musicPause) {
            startPlayTime += System.currentTimeMillis() - pauseStart;
            pauseStart = System.currentTimeMillis();
        }
    }

    /**
     * volume control
     */
    private final int range = 2;
    private double percent = -1;
    private int volume;

    public void setVolume(Integer targetVolume, SlashCommandEvent event) {
        if (targetVolume == null)
            targetVolume = defaultVolume;

        volume = targetVolume;

        if (percent > -1)
            player.setVolume((int) (Math.round(percent * volume) / range));
        else
            player.setVolume(volume / range);

        this.event.volumeChange(volume, event);
    }

    public int getVolume() {
        return volume;
    }

    // https://gitlab.tu-clausthal.de/sfri16/discordmusicbotnetwork/-/blob/020d0e13068c9e1740c7692c9c3fb7f6aed78dfd/src/main/java/music/NormalizedAudioTrack.java
    private void calculateNormalized(Float loudness) {
        int normalizedVolume;
        volume = defaultVolume;
        if (loudness == null) {
            normalizedVolume = volume;
            percent = -1;
        } else {
            percent = ((95 + -7.22 * loudness) / 100);
            normalizedVolume = (int) Math.round(percent * volume) / range;
        }
        player.setVolume(normalizedVolume);
    }

    public void setManagerEvent(GuildMusicManager.Event event) {
        this.event = event;
    }
}
