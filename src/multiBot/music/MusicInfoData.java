package multiBot.music;

import com.sedmelluq.discord.lavaplayer.track.AudioTrack;
import org.json.JSONArray;
import org.json.JSONObject;

import static main.java.BotSetting.apiKEY;
import static main.java.util.UrlDataGetter.getData;
import static main.java.util.UrlDataGetter.postData;

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

        // 影片資料
        String url = "https://youtubei.googleapis.com/youtubei/v1/player?key=AIzaSyAO_FJ2SlqU8Q4STEHLGCilw_Y9_11qcW8";
        String payload = "{\"videoId\":\"" + videoID + "\",\"context\":{\"client\":{\"hl\":\"zh\",\"gl\":\"TW\",\"clientName\":\"MWEB\",\"clientVersion\":\"2.20210330.08.00\"}}}";
        String result = postData(url, payload);
        if (result == null) {
            return;
        }

        // 影片資訊
        JSONObject resultJson = new JSONObject(result);

        if (resultJson.has("playerConfig")) {
            if (resultJson.getJSONObject("playerConfig").getJSONObject("audioConfig").has("loudnessDb"))
                loudness = resultJson.getJSONObject("playerConfig").getJSONObject("audioConfig").getFloat("loudnessDb");
            else
                loudness = 0f;
        }
        JSONObject videoDetails = resultJson.getJSONObject("videoDetails");
        title = videoDetails.getString("title");
        description = videoDetails.getString("shortDescription");
        JSONObject playerMicroformat = resultJson.getJSONObject("microformat").getJSONObject("playerMicroformatRenderer");
        JSONArray thumbnails = playerMicroformat.getJSONObject("thumbnail").getJSONArray("thumbnails");
        thumbnailUrl = getMaximum(thumbnails);
        if (thumbnailUrl == null)
            thumbnailUrl = getMaximum(videoDetails.getJSONObject("thumbnail").getJSONArray("thumbnails"));
        publishDate = playerMicroformat.getString("publishDate");

        // 頻道
        channelID = videoDetails.getString("channelId");
        result = getData("https://www.googleapis.com/youtube/v3/channels?part=snippet&id=" + channelID + "&key=" + apiKEY);
        channelURL = ("https://www.youtube.com/channel/" + channelID);
        if (result == null) return;
//        System.out.println(result);
        JSONObject channelInfo = new JSONObject(result).getJSONArray("items").getJSONObject(0).getJSONObject("snippet");
        channelThumbnailUrl = channelInfo.getJSONObject("thumbnails").getJSONObject("default").getString("url");
        channelName = channelInfo.getString("title");

        // 影片統計
        result = getData("https://www.googleapis.com/youtube/v3/videos?part=statistics&id=" + videoID + "&key=" + apiKEY);
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

