package com.ice.multiBot.music;

import org.json.JSONArray;
import org.json.JSONObject;

import java.net.URLEncoder;
import java.util.Base64;

import static com.ice.main.BotSetting.*;
import static com.ice.main.util.UrlDataGetter.*;
import static java.nio.charset.StandardCharsets.UTF_8;

public class SpotifyToYouTube {
    /**
     * spotify_id
     * spotify_secret
     * spotify_refresh
     */

    private final String SpotifyTokenApiUrl = "https://accounts.spotify.com/api/token";
    private String bearerToken;
    private final String basicAuthorization;

    public SpotifyToYouTube() {
        basicAuthorization = Base64.getEncoder().encodeToString((spotify_id + ':' + spotify_secret).getBytes());
        refreshToken();
    }

    public String[] translate(String oldURL) {
        String url = "https://api.spotify.com/v1/playlists/" + oldURL.split("playlist/")[1] + "/tracks?fields=items(track(name%2Cartists(name)))";
        String token = "Bearer " + bearerToken;
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

    public void refreshToken() {
        String result = postDataAuthorization(
                SpotifyTokenApiUrl,
                "application/x-www-form-urlencoded", "grant_type=refresh_token&refresh_token=" + spotify_refresh,
                "Basic " + basicAuthorization);
        JSONObject data = new JSONObject(result);
        bearerToken = data.getString("access_token");
    }
}
