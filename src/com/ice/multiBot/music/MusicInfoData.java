package com.ice.multiBot.music;

import com.sedmelluq.discord.lavaplayer.track.AudioTrack;
import org.json.JSONArray;
import org.json.JSONObject;

import static com.ice.main.BotSetting.YT_APIKEY;
import static com.ice.main.util.UrlDataGetter.getData;

@SuppressWarnings("ALL")
public class MusicInfoData {
    // 影片
    private String title;
    private final String videoID;
    private String description;
    private String thumbnailUrl;
    private String publishDate;
    // 頻道
    private String channelName;
    private String channelID;
    private String channelURL;
    private String channelThumbnailUrl;
    // 統計
    private long viewCount;
    private long likeCount;
    private long dislikeCount;
    private long commentCount;

    // audio
    private Float loudness;
    private long length;
    private boolean stream;

    public MusicInfoData(AudioTrack track) {
        videoID = track.getInfo().identifier;

        String url = "https://youtu.be/" + videoID;
        String result = getData(url);
        if (result == null) {
            return;
        }

        //get video data
        int startIndex = result.indexOf("ytInitialPlayerResponse");
        if (startIndex == -1) return;
        startIndex = result.indexOf('{', startIndex);
        if (startIndex == -1) return;
        //get video format
        JSONObject rawData = new JSONObject(result.substring(startIndex));
        JSONArray videoFormats = rawData
                .getJSONObject("streamingData")
                .getJSONArray("adaptiveFormats");
        //get details
        JSONObject videoDetails = rawData.getJSONObject("videoDetails");

        // 影片資訊
        if (rawData.has("playerConfig")) {
            if (rawData.getJSONObject("playerConfig").getJSONObject("audioConfig").has("loudnessDb"))
                loudness = rawData.getJSONObject("playerConfig").getJSONObject("audioConfig").getFloat("loudnessDb");
            else
                loudness = 0f;
        }
        title = videoDetails.getString("title");
        description = videoDetails.getString("shortDescription");
        JSONObject playerMicroformat = rawData.getJSONObject("microformat").getJSONObject("playerMicroformatRenderer");
        JSONArray thumbnails = playerMicroformat.getJSONObject("thumbnail").getJSONArray("thumbnails");
        thumbnailUrl = getMaximum(thumbnails);
        if (thumbnailUrl == null)
            thumbnailUrl = getMaximum(videoDetails.getJSONObject("thumbnail").getJSONArray("thumbnails"));
        publishDate = playerMicroformat.getString("publishDate");

        //頻道資料
        startIndex = result.indexOf("videoOwnerRenderer");
        if (startIndex == -1) return;
        startIndex = result.indexOf('{', startIndex);
        if (startIndex == -1) return;
        JSONObject ownerThumbnail = new JSONObject(result.substring(startIndex)).getJSONObject("thumbnail");
        channelID = videoDetails.getString("channelId");
        channelURL = ("https://www.youtube.com/channel/" + channelID);
        channelThumbnailUrl = getMaximum(ownerThumbnail.getJSONArray("thumbnails"));
        channelName = videoDetails.getString("author");

        // 影片統計
        result = getData("https://www.googleapis.com/youtube/v3/videos?part=statistics&id=" + videoID + "&key=" + YT_APIKEY);
        if (result == null) return;
        JSONObject statistics = new JSONObject(result).getJSONArray("items").getJSONObject(0).getJSONObject("statistics");
        viewCount = Long.parseLong(statistics.getString("viewCount"));
        if (statistics.has("likeCount"))
            likeCount = Long.parseLong(statistics.getString("likeCount"));
        else
            likeCount = 0L;
        if (statistics.has("dislikeCount"))
            dislikeCount = Long.parseLong(statistics.getString("dislikeCount"));
        else
            dislikeCount = 0L;
        if (statistics.has("commentCount"))
            commentCount = Long.parseLong(statistics.getString("commentCount"));
        else
            commentCount = 0L;

        length = track.getInfo().length;
        stream = track.getInfo().isStream;
    }

    public String getTitle() {
        return title;
    }

    public String getVideoID() {
        return videoID;
    }

    public String getDescription() {
        return description;
    }

    public String getThumbnailUrl() {
        return thumbnailUrl;
    }

    public String getPublishDate() {
        return publishDate;
    }

    public String getChannelName() {
        return channelName;
    }

    public String getChannelID() {
        return channelID;
    }

    public String getChannelURL() {
        return channelURL;
    }

    public String getChannelThumbnailUrl() {
        return channelThumbnailUrl;
    }

    public long getViewCount() {
        return viewCount;
    }

    public long getLikeCount() {
        return likeCount;
    }

    public long getDislikeCount() {
        return dislikeCount;
    }

    public long getCommentCount() {
        return commentCount;
    }

    public Float getLoudness() {
        return loudness;
    }

    public long getLength() {
        return length;
    }

    public boolean isStream() {
        return stream;
    }

    private String getMaximum(JSONArray data) {
        int maxWidth = -1;
        String url = null;
        for (Object i : data) {
            JSONObject thumbnailData = (JSONObject) i;
            int width = thumbnailData.getInt("width");
            if (width > maxWidth) {
                url = thumbnailData.getString("url");
            }
        }
        return url;
    }
}
