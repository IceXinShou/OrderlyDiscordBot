package main.java.YoutubeDownloader;

import main.java.YoutubeDownloader.util.Container;
import main.java.YoutubeDownloader.util.ITag;
import main.java.YoutubeDownloader.util.VideoQuality;
import org.json.JSONArray;
import org.json.JSONObject;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.net.URLDecoder;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.regex.Pattern;

public class GetVideoInfo {
    private final Map<ITag, VideoObject> videoType = new HashMap<>();
    JSONObject videoDetails;

    public GetVideoInfo(String videoUrl) {
        //get video ID
        String videoID;
        int videoIDIndex = videoUrl.indexOf("v=");
        int nextQueryIndex = videoUrl.indexOf("&", videoIDIndex);
        if (nextQueryIndex != -1)
            videoID = videoUrl.substring(videoIDIndex + 2, nextQueryIndex);
        else
            videoID = videoUrl.substring(videoIDIndex + 2);

        //get video info
        String videoInfoURL = "https://www.youtube.com/get_video_info?video_id=" + videoID +
                "&eurl=https%3A%2F%2Fyoutube.googleapis.com%2Fv%2F" + videoID + "&html5=1&c=TVHTML5&cver=6.20180913";
        String result = getUrl(videoInfoURL);
        if (result == null) {
            System.err.println("can't get video info");
            return;
        }
        Map<String, String> responseData = new HashMap<>();
        for (String i : result.split("&")) {
            int eqIndex = i.indexOf('=');
            responseData.put(i.substring(0, eqIndex), i.substring(eqIndex + 1));
//            System.out.println(i);
        }
        //url percent decode
        String player_response = responseData.get("player_response");
        player_response = URLDecoder.decode(player_response, StandardCharsets.UTF_8);

        //for decode signature
        YoutubeDecode decode = null;
        //get video format
        JSONObject playerResponse = new JSONObject(player_response);
//        System.out.println(playerResponse.toStringBeauty());
        JSONArray videoFormats = playerResponse
                .getJSONObject("streamingData")
                .getJSONArray("adaptiveFormats");
        for (Object i : videoFormats) {
            JSONObject data = ((JSONObject) i);
            if (data.has("url")) {
                data.put("url", unicodeDecode(data.getString("url")));
            } else {
                if (decode == null)
                    decode = new YoutubeDecode(videoID, this);
                String[] signatureWithUrl = unicodeDecode(data.getString("signatureCipher")).split("&");
                Map<String, String> linkData = new HashMap<>();
                for (String j : signatureWithUrl) {
                    String[] k = j.split("=");
                    linkData.put(k[0], k[1]);
                }
                String signature = decode.decode(urlDecode(linkData.get("s")));

                data.put("url", urlDecode(urlDecode(linkData.get("url"))) + '&' + linkData.get("sp") + '=' + signature);
            }
            videoType.put(ITag.valueOf(data.getInt("itag")), new VideoObject(data));

            //unknow
            if (ITag.valueOf(data.getInt("itag")) == ITag.UNKNOWN) {
                System.out.println("unknown itag:" + data.getInt("itag"));
                System.out.println("mimeType:" + data.getString("mimeType"));
                System.out.println("size:" + data.getInt("width") + "x" + data.getInt("height"));
                System.out.println("qualityLabel:" + data.getString("qualityLabel"));
                System.out.println("have audio:" + data.has("mimeType"));
            }

//            videoType.put(ITag.valueOf(data.getInt("itag")), data);
//            System.out.println(data.toStringBeauty());
        }

        //get details
        videoDetails = playerResponse.getJSONObject("videoDetails");
    }

    public Map<ITag, VideoObject> getVideoData() {
        return videoType;
    }

    //getter
    public String getTitle() {
        return videoDetails.getString("title");
    }

    public String getDescription() {
        return videoDetails.getString("shortDescription");
    }

    public String getViewCount() {
        return videoDetails.getString("viewCount");
    }

    public String getChannelID() {
        return videoDetails.getString("channelId");
    }

    public String getVideoID() {
        return videoDetails.getString("videoId");
    }

    public int getLengthSec() {
        return Integer.parseInt(videoDetails.getString("lengthSeconds"));
    }

    public String getLargeThumbnailUrl() {
        int maxPix = 0;
        JSONObject result = null;
        for (Object i : videoDetails.getJSONArray("thumbnail")) {
            JSONObject data = ((JSONObject) i);
            int pix = data.getInt("width") * data.getInt("height");
            if (pix > maxPix) {
                result = data;
            }
        }
        if (result == null)
            return null;
        return result.getString("url");
    }

    public VideoObject getVideoByQualityFpsType(VideoQuality videoQuality, int fps, Container container) {
        for (ITag tag : ITag.getITagByVideoQuality(videoQuality)) {
            if (tag.container != container)
                continue;

            VideoObject videoObject = videoType.get(tag);
            if (videoObject != null && videoObject.getFps() == fps)
                return videoObject;
        }

        return null;
    }

    public List<VideoObject> getVideoByQuality(VideoQuality videoQuality) {
        List<VideoObject> list = new ArrayList<>();
        for (ITag tag : ITag.getITagByVideoQuality(videoQuality)) {
            VideoObject videoObject = videoType.get(tag);
            if (videoObject != null)
                list.add(videoObject);
        }
        return list;
    }

    public VideoObject getBestAudio() {
        VideoObject audioObject = null;
        int byteRate = 0;
        for (Map.Entry<ITag, VideoObject> tag : videoType.entrySet()) {
            if (!tag.getKey().isAudio()) continue;
            if (!(tag.getKey().container == Container.M4A)) continue;
            VideoObject thisAudio = tag.getValue();
            if (thisAudio.getBitrate() > byteRate) {
                byteRate = thisAudio.getBitrate();
                audioObject = thisAudio;
            }
        }
        return audioObject;
    }

    //data getter
    public String getUrl(String input) {
        URL url;
        try {
            url = new URL(input);
            //get result
            InputStream in = url.openStream();
            ByteArrayOutputStream out = new ByteArrayOutputStream();
            byte[] buff = new byte[1024];
            int length;
            while ((length = in.read(buff)) > 0) {
                out.write(buff, 0, length);
            }
            return out.toString(StandardCharsets.UTF_8);
        } catch (IOException e) {
            return null;
        }
    }

    private String urlDecode(String input) {
        return URLDecoder.decode(input, StandardCharsets.UTF_8);
    }

    private String unicodeDecode(String input) {
        String[] arr = input.split(Pattern.quote("\\u"));
        StringBuilder text = new StringBuilder();
        text.append(arr[0]);
        for (int i = 1; i < arr.length; i++) {
            int hexVal = Integer.parseInt(arr[i].substring(0, 4), 16);
            text.append((char) hexVal).append(arr[i].substring(4));
        }
        return text.toString();
    }

}
