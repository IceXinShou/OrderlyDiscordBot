package main.java.YoutubeDownloader.util;

public enum VideoQuality {
    q144,
    q240,
    q360,
    q480,
    q720,
    q1080,
    q1440,
    q2160,
    q3072,
    q4320,

    UNKNOWN;

    public static VideoQuality getQuality(String qualityWithP) {
        for (VideoQuality quality : values()) {
            if (quality.name().endsWith(qualityWithP))
                return quality;
        }
        return UNKNOWN;
    }
}
