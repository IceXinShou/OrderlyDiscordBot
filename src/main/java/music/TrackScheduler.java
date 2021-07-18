package main.java.music;

import com.sedmelluq.discord.lavaplayer.player.AudioPlayer;
import com.sedmelluq.discord.lavaplayer.player.event.AudioEventAdapter;
import com.sedmelluq.discord.lavaplayer.track.AudioPlaylist;
import com.sedmelluq.discord.lavaplayer.track.AudioTrack;
import com.sedmelluq.discord.lavaplayer.track.AudioTrackEndReason;
import net.dv8tion.jda.api.events.interaction.SlashCommandEvent;
import org.json.JSONObject;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.UnsupportedEncodingException;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLDecoder;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;

/**
 * This class schedules tracks for the audio player. It contains the queue of tracks.
 */
public class TrackScheduler extends AudioEventAdapter {
    private final AudioPlayer player;
    private final BlockingQueue<AudioTrack> queue;

    private GuildMusicManager.Event event;
    private AudioTrack nowPlaying;

    //setting
    private boolean repeat;
    private int defaultVolume = 20;

    /**
     * @param player The audio player this scheduler uses
     */
    public TrackScheduler(AudioPlayer player) {
        this.player = player;
        this.queue = new LinkedBlockingQueue<>();
    }

    /**
     * Add the next track to queue or play right away if nothing is in the queue.
     *
     * @param track The track to play or add to queue.
     * @param event
     */
    private void queue(AudioTrack track, SlashCommandEvent event, boolean showAdd) {
        // Calling startTrack with the noInterrupt set to true will start the track only if nothing is currently playing. If
        // something is playing, it returns false and does nothing. In that case the player was already playing so this
        // track goes to the queue instead.
        if (!player.startTrack(track, true)) {
            //加入序列
            queue.offer(track);
            if (showAdd)
                this.event.addToQueue(track, event);
        } else {
            nowPlaying = track;
            calculateNormalized(track, defaultVolume);
            if (showAdd)
                this.event.playStart(track, event);
        }
    }

    public void queue(AudioTrack track, SlashCommandEvent event) {
        queue(track, event, true);
    }

    public void addPlayListToQueue(AudioPlaylist playlist, SlashCommandEvent event) {
        List<AudioTrack> trackList = playlist.getTracks();
        if (trackList.size() == 0)
            return;
        //嘗試撥放
        queue(trackList.get(0), event, false);

        for (int i = 1; i < trackList.size(); i++) {
            //加入序列
            queue.offer(trackList.get(i));
        }

        this.event.addPlayerListToQueue(playlist, event);
    }

    /**
     * Start the next track, stopping the current one if it is playing.
     */
    private void nextTrack(boolean manualNext, SlashCommandEvent event) {
        // Start the next track, regardless of if something is already playing or not. In case queue was empty, we are
        // giving null to startTrack, which is a valid argument and will simply stop the player.
        AudioTrack track;
        if (repeat)
            track = nowPlaying.makeClone();
        else
            track = queue.poll();

        if (!player.startTrack(track, false)) {
            this.event.noMoreTrack(event);
        } else {
            if (manualNext)
                this.event.skip(nowPlaying, event);
            nowPlaying = track;
            calculateNormalized(track, defaultVolume);
            this.event.playStart(track, null);
        }
    }

    public void nextTrack(SlashCommandEvent event) {
        nextTrack(true, event);
    }

    @Override
    public void onTrackEnd(AudioPlayer player, AudioTrack track, AudioTrackEndReason endReason) {
        // Only start the next track if the end reason is suitable for it (FINISHED or LOAD_FAILED)
        if (endReason.mayStartNext) {
            nextTrack(false, null);
        }
    }
//
//    private synchronized void calculateNormalized(){
//        if(!audioTrack.getInfo().uri.contains("youtu")){
//            normalizedVolume = targetVolume;
//            return;
//        }
//
//        String id = audioTrack.getInfo().uri.split("=")[1];
//        String in = "";
//        URL url = null;
//        try {
//            url = new URL(infoUri + id);
//            Scanner scanner = new Scanner(url.openStream());
//
//            in = scanner.nextLine();
//            Pattern pat = Pattern.compile("relative_loudness=(.*?)&");
//            Matcher mat = pat.matcher(in);
//
//            if(!mat.find()) throw new Exception("No Loudness");
//            float loudness = Float.valueOf(mat.group(1));
//            double percent = ((95 + -7.22 * loudness) / 100);
//            normalizedVolume = (int) Math.round(percent * targetVolume);
//        } catch (Exception e) {
//            normalizedVolume = targetVolume;
//        }
//
//
//    }


    public void toggleRepeat(SlashCommandEvent slashCommandEvent) {
        repeat = !repeat;
        event.repeat(nowPlaying, repeat, slashCommandEvent);
    }

    private boolean pause = false;

    public void pause(SlashCommandEvent event) {
        player.setPaused(pause = !pause);
        this.event.pause(pause, event);
    }

    public void setManagerEvent(GuildMusicManager.Event event) {
        this.event = event;
    }

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
            result = out.toString("UTF8");
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
        try {
            player_response = URLDecoder.decode(player_response, "UTF-8");
        } catch (UnsupportedEncodingException e) {
            player.setVolume(defaultVolume);
            return;
        }

        JSONObject playerResponse = new JSONObject(player_response);
        float loudness = playerResponse.getJSONObject("playerConfig").getJSONObject("audioConfig").getFloat("loudnessDb");
        percent = ((95 + -7.22 * loudness) / 100);
        player.setVolume((int) Math.round(percent * defaultVolume) + 2);
    }

}
