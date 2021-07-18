package main.java.YoutubeDownloader.util;

import java.util.ArrayList;
import java.util.List;

public enum ITag {
    // Muxed
    i5(Container.Flv, AudioEncoding.Mp3, VideoEncoding.H263, VideoQuality.q144),
    i6(Container.Flv, AudioEncoding.Mp3, VideoEncoding.H263, VideoQuality.q240),
    i13(Container.Tgpp, AudioEncoding.Aac, VideoEncoding.Mp4V, VideoQuality.q144),
    i17(Container.Tgpp, AudioEncoding.Aac, VideoEncoding.Mp4V, VideoQuality.q144),
    i18(Container.Mp4, AudioEncoding.Aac, VideoEncoding.H264, VideoQuality.q360),
    i22(Container.Mp4, AudioEncoding.Aac, VideoEncoding.H264, VideoQuality.q720),
    i34(Container.Flv, AudioEncoding.Aac, VideoEncoding.H264, VideoQuality.q360),
    i35(Container.Flv, AudioEncoding.Aac, VideoEncoding.H264, VideoQuality.q480),
    i36(Container.Tgpp, AudioEncoding.Aac, VideoEncoding.Mp4V, VideoQuality.q240),
    i37(Container.Mp4, AudioEncoding.Aac, VideoEncoding.H264, VideoQuality.q1080),
    i38(Container.Mp4, AudioEncoding.Aac, VideoEncoding.H264, VideoQuality.q3072),
    i43(Container.WebM, AudioEncoding.Vorbis, VideoEncoding.Vp8, VideoQuality.q360),
    i44(Container.WebM, AudioEncoding.Vorbis, VideoEncoding.Vp8, VideoQuality.q480),
    i45(Container.WebM, AudioEncoding.Vorbis, VideoEncoding.Vp8, VideoQuality.q720),
    i46(Container.WebM, AudioEncoding.Vorbis, VideoEncoding.Vp8, VideoQuality.q1080),
    i59(Container.Mp4, AudioEncoding.Aac, VideoEncoding.H264, VideoQuality.q480),
    i78(Container.Mp4, AudioEncoding.Aac, VideoEncoding.H264, VideoQuality.q480),
    i82(Container.Mp4, AudioEncoding.Aac, VideoEncoding.H264, VideoQuality.q360),
    i83(Container.Mp4, AudioEncoding.Aac, VideoEncoding.H264, VideoQuality.q480),
    i84(Container.Mp4, AudioEncoding.Aac, VideoEncoding.H264, VideoQuality.q720),
    i85(Container.Mp4, AudioEncoding.Aac, VideoEncoding.H264, VideoQuality.q1080),
    i91(Container.Mp4, AudioEncoding.Aac, VideoEncoding.H264, VideoQuality.q144),
    i92(Container.Mp4, AudioEncoding.Aac, VideoEncoding.H264, VideoQuality.q240),
    i93(Container.Mp4, AudioEncoding.Aac, VideoEncoding.H264, VideoQuality.q360),
    i94(Container.Mp4, AudioEncoding.Aac, VideoEncoding.H264, VideoQuality.q480),
    i95(Container.Mp4, AudioEncoding.Aac, VideoEncoding.H264, VideoQuality.q720),
    i96(Container.Mp4, AudioEncoding.Aac, VideoEncoding.H264, VideoQuality.q1080),
    i100(Container.WebM, AudioEncoding.Vorbis, VideoEncoding.Vp8, VideoQuality.q360),
    i101(Container.WebM, AudioEncoding.Vorbis, VideoEncoding.Vp8, VideoQuality.q480),
    i102(Container.WebM, AudioEncoding.Vorbis, VideoEncoding.Vp8, VideoQuality.q720),
    i132(Container.Mp4, AudioEncoding.Aac, VideoEncoding.H264, VideoQuality.q240),
    i151(Container.Mp4, AudioEncoding.Aac, VideoEncoding.H264, VideoQuality.q144),

    // Video-only (mp4)
    i133(Container.Mp4, null, VideoEncoding.H264, VideoQuality.q240),
    i134(Container.Mp4, null, VideoEncoding.H264, VideoQuality.q360),
    i135(Container.Mp4, null, VideoEncoding.H264, VideoQuality.q480),
    i136(Container.Mp4, null, VideoEncoding.H264, VideoQuality.q720),
    i137(Container.Mp4, null, VideoEncoding.H264, VideoQuality.q1080),
    i138(Container.Mp4, null, VideoEncoding.H264, VideoQuality.q4320),
    i160(Container.Mp4, null, VideoEncoding.H264, VideoQuality.q144),
    i212(Container.Mp4, null, VideoEncoding.H264, VideoQuality.q480),
    i213(Container.Mp4, null, VideoEncoding.H264, VideoQuality.q480),
    i214(Container.Mp4, null, VideoEncoding.H264, VideoQuality.q720),
    i215(Container.Mp4, null, VideoEncoding.H264, VideoQuality.q720),
    i216(Container.Mp4, null, VideoEncoding.H264, VideoQuality.q1080),
    i217(Container.Mp4, null, VideoEncoding.H264, VideoQuality.q1080),
    i264(Container.Mp4, null, VideoEncoding.H264, VideoQuality.q1440),
    i266(Container.Mp4, null, VideoEncoding.H264, VideoQuality.q2160),
    i298(Container.Mp4, null, VideoEncoding.H264, VideoQuality.q720),
    i299(Container.Mp4, null, VideoEncoding.H264, VideoQuality.q1080),

    // Video-only (webm)
    i167(Container.WebM, null, VideoEncoding.Vp8, VideoQuality.q360),
    i168(Container.WebM, null, VideoEncoding.Vp8, VideoQuality.q480),
    i169(Container.WebM, null, VideoEncoding.Vp8, VideoQuality.q720),
    i170(Container.WebM, null, VideoEncoding.Vp8, VideoQuality.q1080),
    i218(Container.WebM, null, VideoEncoding.Vp8, VideoQuality.q480),
    i219(Container.WebM, null, VideoEncoding.Vp8, VideoQuality.q480),
    i242(Container.WebM, null, VideoEncoding.Vp9, VideoQuality.q240),
    i243(Container.WebM, null, VideoEncoding.Vp9, VideoQuality.q360),
    i244(Container.WebM, null, VideoEncoding.Vp9, VideoQuality.q480),
    i245(Container.WebM, null, VideoEncoding.Vp9, VideoQuality.q480),
    i246(Container.WebM, null, VideoEncoding.Vp9, VideoQuality.q480),
    i247(Container.WebM, null, VideoEncoding.Vp9, VideoQuality.q720),
    i248(Container.WebM, null, VideoEncoding.Vp9, VideoQuality.q1080),
    i271(Container.WebM, null, VideoEncoding.Vp9, VideoQuality.q1440),
    i272(Container.WebM, null, VideoEncoding.Vp9, VideoQuality.q2160),
    i278(Container.WebM, null, VideoEncoding.Vp9, VideoQuality.q144),
    i302(Container.WebM, null, VideoEncoding.Vp9, VideoQuality.q720),
    i303(Container.WebM, null, VideoEncoding.Vp9, VideoQuality.q1080),
    i308(Container.WebM, null, VideoEncoding.Vp9, VideoQuality.q1440),
    i313(Container.WebM, null, VideoEncoding.Vp9, VideoQuality.q2160),
    i315(Container.WebM, null, VideoEncoding.Vp9, VideoQuality.q2160),
    i330(Container.WebM, null, VideoEncoding.Vp9, VideoQuality.q144),
    i331(Container.WebM, null, VideoEncoding.Vp9, VideoQuality.q240),
    i332(Container.WebM, null, VideoEncoding.Vp9, VideoQuality.q360),
    i333(Container.WebM, null, VideoEncoding.Vp9, VideoQuality.q480),
    i334(Container.WebM, null, VideoEncoding.Vp9, VideoQuality.q720),
    i335(Container.WebM, null, VideoEncoding.Vp9, VideoQuality.q1080),
    i336(Container.WebM, null, VideoEncoding.Vp9, VideoQuality.q1440),
    i337(Container.WebM, null, VideoEncoding.Vp9, VideoQuality.q2160),

    // Audio-only (mp4)
    i139(Container.M4A, AudioEncoding.Aac, null, null),
    i140(Container.M4A, AudioEncoding.Aac, null, null),
    i141(Container.M4A, AudioEncoding.Aac, null, null),
    i256(Container.M4A, AudioEncoding.Aac, null, null),
    i258(Container.M4A, AudioEncoding.Aac, null, null),
    i325(Container.M4A, AudioEncoding.Aac, null, null),
    i328(Container.M4A, AudioEncoding.Aac, null, null),

    // Audio-only (webm)
    i171(Container.WebM, AudioEncoding.Vorbis, null, null),
    i172(Container.WebM, AudioEncoding.Vorbis, null, null),
    i249(Container.WebM, AudioEncoding.Opus, null, null),
    i250(Container.WebM, AudioEncoding.Opus, null, null),
    i251(Container.WebM, AudioEncoding.Opus, null, null),

    //new
    i571(Container.Mp4, null, VideoEncoding.UNKNOWN, VideoQuality.q4320),
    i401(Container.Mp4, null, VideoEncoding.UNKNOWN, VideoQuality.q2160),
    i400(Container.Mp4, null, VideoEncoding.UNKNOWN, VideoQuality.q1440),
    i399(Container.Mp4, null, VideoEncoding.UNKNOWN, VideoQuality.q1080),
    i398(Container.Mp4, null, VideoEncoding.UNKNOWN, VideoQuality.q720),
    i397(Container.Mp4, null, VideoEncoding.UNKNOWN, VideoQuality.q480),
    i396(Container.Mp4, null, VideoEncoding.UNKNOWN, VideoQuality.q360),
    i395(Container.Mp4, null, VideoEncoding.UNKNOWN, VideoQuality.q240),
    i394(Container.Mp4, null, VideoEncoding.UNKNOWN, VideoQuality.q144),

    UNKNOWN(null, null, null, null);

    public Container container;
    private AudioEncoding audioEncoding;
    private VideoEncoding videoEncoding;
    private VideoQuality videoQuality;

    ITag(Container container, AudioEncoding audioEncoding, VideoEncoding videoEncoding, VideoQuality videoQuality) {
        this.container = container;
        this.audioEncoding = audioEncoding;
        this.videoEncoding = videoEncoding;
        this.videoQuality = videoQuality;
    }

    public static List<ITag> getITagByVideoQuality(VideoQuality quality) {
        List<ITag> allMatch = new ArrayList<>();
        for (ITag iTag : values()) {
            if (iTag.videoQuality == quality)
                allMatch.add(iTag);
        }
        return allMatch;
    }

    public static ITag valueOf(int iTagValue) {
        for (ITag iTag : values()) {
            if (iTag.name().equals("i" + iTagValue))
                return iTag;
        }
        return UNKNOWN;
    }

    public boolean isAudio() {
        return audioEncoding != null;
    }

    public boolean isVideo() {
        return videoEncoding != null;
    }
}
