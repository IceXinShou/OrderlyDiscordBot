package com.ice.multiBot.music;

import com.sedmelluq.discord.lavaplayer.player.AudioPlayer;
import com.sedmelluq.discord.lavaplayer.player.event.AudioEventAdapter;
import com.sedmelluq.discord.lavaplayer.track.AudioPlaylist;
import com.sedmelluq.discord.lavaplayer.track.AudioTrack;
import com.sedmelluq.discord.lavaplayer.track.AudioTrackEndReason;
import com.ice.multiBot.MusicBot;
import net.dv8tion.jda.api.entities.Guild;
import net.dv8tion.jda.api.events.interaction.GenericInteractionCreateEvent;
import net.dv8tion.jda.api.events.interaction.SelectionMenuEvent;
import net.dv8tion.jda.api.events.interaction.SlashCommandEvent;

import java.util.ArrayList;
import java.util.List;

public class TrackScheduler extends AudioEventAdapter {
    private final Guild guild;
    private final AudioPlayer player;
    private final MusicBot musicBot;
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

    public TrackScheduler(AudioPlayer player, Guild guild, MusicBot musicBot) {
        this.guild = guild;
        this.player = player;
        this.musicBot = musicBot;
        this.queue = new ArrayList<>();
    }

    public void addPlayListToQueue(AudioPlaylist playlist, GenericInteractionCreateEvent event) {
        List<AudioTrack> trackList = playlist.getTracks();
        if (trackList.size() == 0)
            return;
        // 加入序列
        for (int i = 1; i < trackList.size(); i++) {
            queue.add(trackList.get(i));
        }
        // 嘗試播放
        queue(trackList.get(0), event, 0, false, false, null);

        this.event.addPlayerListToQueue(playlist, event);
    }

    public void queue(AudioTrack track, GenericInteractionCreateEvent event, int position, boolean search, boolean playNow, SelectionMenuEvent selectionMenuEvent) {
        if (position != -1)
            queue.add(position, track);
        else if (playNow && queue.size() > 0) {
            queue.add(index + 1, track);
            if (repeat) {
                index++;
                playingTrack = track;
            }
            nextTrack(event, search);
            return;
        } else
            queue.add(track);
        if (player.startTrack(track, true)) {
            // 開始撥放
            playingTrack = track;
            startPlayTime = System.currentTimeMillis();
            musicInfo = new MusicInfoData(track);
            calculateNormalized(musicInfo.getLoudness());
            this.event.trackStart(track, event, guild, musicBot, search, selectionMenuEvent);
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
            track = playingTrack;
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
        }
        if (player.startTrack(track.makeClone(), false)) {
            playingTrack = track;
            startPlayTime = System.currentTimeMillis();
            musicInfo = new MusicInfoData(track);
            calculateNormalized(musicInfo.getLoudness());

            return true;
        }

        return false;
    }

    public void nextTrack(GenericInteractionCreateEvent event, boolean search) {
        lastIndex = index;
        index++;
        if (playTrack()) {
//            this.event.skip(playingTrack, event, guild);
            this.event.trackStart(playingTrack, event, guild, musicBot, search, null);
        } else {
            stopPlay(event);
        }
    }

    @Override
    public void onTrackEnd(AudioPlayer player, AudioTrack track, AudioTrackEndReason endReason) {
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
        this.event.noMoreTrack(event, guild, musicBot);
    }

    public AudioTrack[] getQueue() {
        if (index + 1 >= queue.size())
            return null;
        AudioTrack[] out = new AudioTrack[loop ? queue.size() : queue.size() - index - 1];
        int j = 0;
        for (int i = queue.size() - 1; i > (loop ? -1 : index); i--) {
            out[j++] = queue.get(i);
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