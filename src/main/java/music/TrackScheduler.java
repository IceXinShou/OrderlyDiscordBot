package main.java.music;

import com.sedmelluq.discord.lavaplayer.player.AudioPlayer;
import com.sedmelluq.discord.lavaplayer.player.event.AudioEventAdapter;
import com.sedmelluq.discord.lavaplayer.track.AudioPlaylist;
import com.sedmelluq.discord.lavaplayer.track.AudioTrack;
import com.sedmelluq.discord.lavaplayer.track.AudioTrackEndReason;
import net.dv8tion.jda.api.entities.Guild;
import net.dv8tion.jda.api.events.interaction.SlashCommandEvent;
import org.json.JSONObject;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLDecoder;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

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
    //now playing
    public AudioTrack playingTrack;
    public long startPlayTime;

    //setting
    public boolean repeat;
    public boolean loop;
    private final int defaultVolume = 20;

    public int loopStatus = 0;

    /**
     * @param player The audio player this scheduler uses
     */
    public TrackScheduler(AudioPlayer player, Guild guild) {
        this.guild = guild;
        this.player = player;
        this.queue = new ArrayList<>();
    }

    public void queue(AudioTrack track, SlashCommandEvent event) {
        queue(track, event, true);
    }

    public void addPlayListToQueue(AudioPlaylist playlist, SlashCommandEvent event) {
        List<AudioTrack> trackList = playlist.getTracks();
        if (trackList.size() == 0)
            return;
        //嘗試播放
        queue(trackList.get(0), event, false);

        for (int i = 1; i < trackList.size(); i++) {
            //加入序列
            queue.add(trackList.get(i));
        }

        this.event.addPlayerListToQueue(playlist, event);
    }

    /**
     * Add the next track to queue or play right away if nothing is in the queue.
     *
     * @param track The track to play or add to queue.
     */
    private void queue(AudioTrack track, SlashCommandEvent event, boolean showAdd) {
        // Calling startTrack with the noInterrupt set to true will start the track only if nothing is currently playing. If
        // something is playing, it returns false and does nothing. In that case the player was already playing so this
        // track goes to the queue instead.
        queue.add(track);
        if (!player.startTrack(track, true)) {
            //加入序列
            if (showAdd)
                this.event.addToQueue(track, event);
        } else {
            playingTrack = track;
            startPlayTime = System.currentTimeMillis();
            calculateNormalized(track, defaultVolume);
            if (showAdd)
                this.event.playStart(track, event, guild);
        }
    }

    private boolean playTrack() {
        AudioTrack track;
        if (repeat) {
            index = lastIndex;
            track = playingTrack.makeClone();
        } else {
            if (queue.size() == 0)
                return false;
            if (index < 0) {
                index = lastIndex;
                return false;
            }

            if (index == queue.size()) {
                if (loop)
                    index = 0;
                else
                    return false;
            }

            //取得歌曲
            track = queue.get(index);
            if (index < lastIndex)
                track = track.makeClone();
        }

        if (player.startTrack(track, false)) {
            playingTrack = track;
            startPlayTime = System.currentTimeMillis();
            calculateNormalized(track, defaultVolume);
            return true;
        }
        return false;
    }

    public void nextTrack(SlashCommandEvent event) {
        lastIndex = index;
        index++;
        if (playTrack()) {
            this.event.skip(playingTrack, event, guild);
            this.event.playStart(playingTrack, event, guild);
        } else
            stopPlay(event);
//        nextTrack(true, event);
    }

    public void previousTrack(SlashCommandEvent event) {
        lastIndex = index;
        index--;
        if (playTrack())
            this.event.playStart(playingTrack, event, guild);
        else
            stopPlay(event);
    }

    @Override
    public void onTrackEnd(AudioPlayer player, AudioTrack track, AudioTrackEndReason endReason) {
        // Only start the next track if the end reason is suitable for it (FINISHED or LOAD_FAILED)
        if (endReason.mayStartNext) {
            lastIndex = index;
            index++;
            if (playTrack()) {
                this.event.playStart(playingTrack, null, guild);
            } else
                stopPlay(null);

        }
    }

    public void stopPlay(SlashCommandEvent event) {
        playingTrack = null;
        musicPause = false;
        player.setPaused(false);
        player.stopTrack();
        this.event.noMoreTrack(event, guild);
    }

    public List<AudioTrack> getQueue() {
        return new ArrayList<>(queue).subList(index, queue.size());
    }

    public void toggleRepeat(SlashCommandEvent slashCommandEvent) {
        repeat = !repeat;
        event.repeat(playingTrack, repeat, slashCommandEvent);
    }

    /**
     * pause
     */
    public boolean musicPause = false;
    private long pauseStart;

    public void pause(SlashCommandEvent event, Boolean play) {
        if (play) {
            if (musicPause) {
                startPlayTime += System.currentTimeMillis() - pauseStart;
                player.setPaused(false);
                musicPause = false;
            }
        } else {

            player.setPaused(musicPause = !musicPause);
            if (musicPause)
                pauseStart = System.currentTimeMillis();
            else
                startPlayTime += System.currentTimeMillis() - pauseStart;

            this.event.pause(musicPause, event, guild);
        }
    }

    /**
     * volume control
     */
    double percent = -1;

    public void changeVolume(Integer targetVolume, SlashCommandEvent event) {

        if (targetVolume == null)
            targetVolume = defaultVolume;

        if (percent > -1) {
            player.setVolume((int) Math.round(percent * targetVolume));
        } else
            player.setVolume(targetVolume);

        this.event.volumeChange(targetVolume, event);
    }

    private void calculateNormalized(AudioTrack audioTrack, int defaultVolume) {
        percent = -1;
        String videoUrl = audioTrack.getInfo().uri;

        //get video ID
        String videoID;
        int videoIDIndex = videoUrl.indexOf("v=");
        int nextQueryIndex = videoUrl.indexOf("&", videoIDIndex);
        if (nextQueryIndex != -1)
            videoID = videoUrl.substring(videoIDIndex + 2, nextQueryIndex);
        else
            videoID = videoUrl.substring(videoIDIndex + 2);

        //get video info
        URL url;
        try {
            url = new URL("https://www.youtube.com/get_video_info?video_id=" + videoID +
                    "&eurl=https%3A%2F%2Fyoutube.googleapis.com%2Fv%2F" + videoID + "&html5=1&c=TVHTML5&cver=6.20180913");
        } catch (MalformedURLException e) {
            player.setVolume(defaultVolume);
            return;
        }
        //get result
        String result;
        try {
            InputStream in = url.openStream();
            ByteArrayOutputStream out = new ByteArrayOutputStream();
            byte[] buff = new byte[1024];
            int length;
            while ((length = in.read(buff)) > 0) {
                out.write(buff, 0, length);
            }
            result = out.toString(StandardCharsets.UTF_8);
        } catch (IOException e) {
            player.setVolume(defaultVolume);
            return;
        }
        Map<String, String> responseData = new HashMap<>();
        for (String i : result.split("&")) {
            int eqIndex = i.indexOf('=');
            responseData.put(i.substring(0, eqIndex), i.substring(eqIndex + 1));
        }

        //url percent decode
        String player_response = responseData.get("player_response");
        player_response = URLDecoder.decode(player_response, StandardCharsets.UTF_8);

        JSONObject playerResponse = new JSONObject(player_response);
        float loudness;
        if (playerResponse.getJSONObject("playerConfig").getJSONObject("audioConfig").has("loudnessDb"))
            loudness = playerResponse.getJSONObject("playerConfig").getJSONObject("audioConfig").getFloat("loudnessDb");
        else
            loudness = 0f;
        percent = ((95 + -7.22 * loudness) / 100);
        player.setVolume((int) Math.round(percent * defaultVolume) + 2);
    }

    public void setManagerEvent(GuildMusicManager.Event event) {
        this.event = event;
    }

}
