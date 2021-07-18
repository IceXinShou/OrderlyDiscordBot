package main.java.youtube;

import org.json.JSONArray;
import org.json.JSONObject;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.UnsupportedEncodingException;
import java.net.URL;
import java.net.URLDecoder;
import java.util.HashMap;
import java.util.Map;
import java.util.regex.Pattern;

public class GetYoutubeInfo {
    public GetYoutubeInfo() {
        String videoUrl = "https://www.youtube.com/watch?v=DtBoAqkIJzI";

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
        try {
            player_response = URLDecoder.decode(player_response, "UTF-8");
        } catch (UnsupportedEncodingException e) {
            e.printStackTrace();
        }

        //for decode signature
        YoutubeDecode decode = null;
        //get video format

        JSONObject playerResponse = new JSONObject(player_response);
        JSONArray videoFormats = playerResponse
                .getJSONObject("streamingData")
                .getJSONArray("adaptiveFormats");
        for (Object i : videoFormats) {
            JSONObject data = ((JSONObject) i);
            String mimeType = data.getString("mimeType");

            String url;
            if (data.has("url")) {
                url = unicodeDecode(data.getString("url"));
            } else {
                if (decode == null)
                    decode = new YoutubeDecode(videoID);
                String[] signatureWithUrl = unicodeDecode(data.getString("signatureCipher")).split("&");
                Map<String, String> linkData = new HashMap<>();
                for (String j : signatureWithUrl) {
                    String[] k = j.split("=");
                    linkData.put(k[0], k[1]);
                }
                String signature = decode.decode(urlDecode(linkData.get("s")));

                url = urlDecode(urlDecode(linkData.get("url"))) + '&' + linkData.get("sp") + '=' + signature;
            }

//            System.out.println(data.toStringBeauty());
            System.out.println("itag: " + data.getInt("itag"));
            if (data.has("qualityLabel"))
                System.out.println("qualityLabel: " + data.getString("qualityLabel"));
            else
                System.out.println("quality: " + data.getString("quality"));
            if (data.has("width"))
                System.out.println("size: " + data.getInt("width") + "x" + data.getInt("height"));
            if (data.has("fps"))
                System.out.println("fps: " + data.getInt("fps"));
            System.out.println("bitrate: " + data.getInt("bitrate"));
            System.out.println("mimeType: " + mimeType);
            System.out.println(url);

//            System.out.println(mimeType);
//            System.out.println();

//            if (!data.containsKey("url"))
//                continue;

//            if (mimeType.startsWith("audio/mp4")) {
//                System.out.println("Audio");
//                System.out.println(unicodeDecode(data.getString("url")));
//            }
//            if (mimeType.startsWith("video/mp4")) {
//                System.out.println("Video");
//                System.out.println(unicodeDecode(data.getString("url")));
//            }
        }
//        System.out.println(playerResponse.toStringBeauty());
    }

    public static String getUrl(String input) {
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
            return out.toString("UTF8");
        } catch (IOException e) {
            return null;
        }
    }

    private String urlDecode(String input) {
        try {
            return URLDecoder.decode(input, "UTF-8");
        } catch (UnsupportedEncodingException e) {
            return null;
        }
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

    public static void main(String[] args) {
        new GetYoutubeInfo();
    }
}
