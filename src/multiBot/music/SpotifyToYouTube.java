package multiBot.music;

import org.json.JSONArray;
import org.json.JSONObject;

import java.net.URLEncoder;

import static java.nio.charset.StandardCharsets.UTF_8;
import static main.java.BotSetting.*;
import static main.java.util.UrlDataGetter.*;

public class SpotifyToYouTube {

    static public String[] Translate(String oldURL) {
        String url = "https://api.spotify.com/v1/playlists/" + oldURL.split("playlist/")[1] + "/tracks?fields=items(track(name%2Cartists(name)))";
        String token = "Bearer AQC6T36odeCn9XKFj0TWa7dmo5IOOTlysGxfVpLydPw28aMOQ0ugzTSemhNbqOgsYzdWaH2FXJ-JxKea5Ns1DTIUSSytstbx-FbMEei2EcOxqtnLgi4AAiNPn6kTI01YlOL9XixTrbrxg3qYPVqHz2U5rdNb9xUdKngnQaYvLA";
        JSONObject result = new JSONObject(getDataAuthorization(url, token));

        if (result.has("error"))
            return new String[]{"error"};

        JSONArray data = result.getJSONArray("items");
        String[] output = new String[data.length()];
        for (int i = 0; i < output.length; i++) {
            JSONObject track = data.getJSONObject(i).getJSONObject("track");
            StringBuilder builder = new StringBuilder();
            for (Object artist : track.getJSONArray("artists"))
                builder.append(' ').append(((JSONObject) artist).getString("name"));

            String videoData = getData("https://youtube.googleapis.com/youtube/v3/search?part=snippet&maxResults=1&type=video&q=" +
                    URLEncoder.encode(track.getString("name"), UTF_8) + URLEncoder.encode(builder.toString(), UTF_8) + "&key=" + YT_APIKEY);

            if (videoData == null) continue;
            JSONObject jd;
            if ((jd = new JSONObject(videoData)).has("items"))
                output[i] = jd.getJSONArray("items").getJSONObject(0).getJSONObject("id").getString("videoId");
        }

        return output;
    }
}
