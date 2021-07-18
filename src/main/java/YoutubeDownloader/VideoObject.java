package main.java.YoutubeDownloader;

import main.java.YoutubeDownloader.util.ITag;
import org.json.JSONObject;

public class VideoObject {
    private final ITag itag;
    private final String url;
    private final String mimeType;
    private final String quality;
    private final int bitrate;
    private final int averageBitrate;
    private final int contentLength;
    private final long lastModified;
    private final String projectionType;
    private final String approxDurationMs;
    //video
    private int width;
    private int height;
    private int fps;
    private String qualityLabel;
    //audio
    private String audioQuality;
    private int audioSampleRate;
    private int audioChannels;
    private float loudnessDb;

    public VideoObject(JSONObject data) {
        itag = ITag.valueOf(data.getInt("itag"));
        url = data.getString("url");
        mimeType = data.getString("mimeType");
        quality = data.getString("quality");
        bitrate = data.getInt("bitrate");
        averageBitrate = data.getInt("averageBitrate");
        contentLength = data.getInt("contentLength");
        lastModified = data.getLong("lastModified");
        projectionType = data.getString("projectionType");
        approxDurationMs = data.getString("approxDurationMs");

        if (itag.isVideo()) {
            width = data.getInt("width");
            height = data.getInt("height");
            fps = data.getInt("fps");
            qualityLabel = data.getString("qualityLabel");
        }

        if (itag.isAudio()) {
            audioQuality = data.getString("audioQuality");
            audioSampleRate = data.getInt("audioSampleRate");
            audioChannels = data.getInt("audioChannels");
            loudnessDb = data.getFloat("loudnessDb");
        }
    }

    public ITag getItag() {
        return itag;
    }

    public String getUrl() {
        return url;
    }

    public String getMimeType() {
        return mimeType;
    }

    public String getQuality() {
        return quality;
    }

    public int getBitrate() {
        return bitrate;
    }

    public int getAverageBitrate() {
        return averageBitrate;
    }

    public int getContentLength() {
        return contentLength;
    }

    public long getLastModified() {
        return lastModified;
    }

    public String getProjectionType() {
        return projectionType;
    }

    public String getApproxDurationMs() {
        return approxDurationMs;
    }

    public int getWidth() {
        return width;
    }

    public int getHeight() {
        return height;
    }

    public int getFps() {
        return fps;
    }

    public String getQualityLabel() {
        return qualityLabel;
    }

    public String getAudioQuality() {
        return audioQuality;
    }

    public int getAudioSampleRate() {
        return audioSampleRate;
    }

    public int getAudioChannels() {
        return audioChannels;
    }

    public float getLoudnessDb() {
        return loudnessDb;
    }
}
