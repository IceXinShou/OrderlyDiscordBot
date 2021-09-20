package multiBot.music;

import org.json.JSONArray;
import org.json.JSONObject;

import java.net.URLEncoder;

import static java.nio.charset.StandardCharsets.UTF_8;
import static main.java.BotSetting.YT_APIKEY;
import static main.java.util.SlashCommandOption.NAME;
import static main.java.util.UrlDataGetter.getData;
import static main.java.util.UrlDataGetter.getDataAuthorization;

public class SpotifyToYouTube {

    static public String[] Translate(String oldURL) {
        String url = "https://api.spotify.com/v1/playlists/" + oldURL.split("playlist/")[1] + "/tracks?fields=items(track(name%2Cartists(name)))";
        String token = "Bearer BQDhOZAYZAmS2EazF1mecibs428x8kZQg9WYhWefiiCusl-rNkJee07rA66mXsSQE-f6ycIbWVF9_411QXPxDqNpuXMCn3IVvjBqKHEZTBvT8U9jjUr_ndDfPEuIbZmX_tmcWFzAE2hzFy20tWdr8cSBY7rSO88";
        JSONObject result = new JSONObject(getDataAuthorization(url, token));

        System.out.println(url);
        if (result.has("error"))
            return new String[]{"error"};

        JSONArray data = result.getJSONArray("items");
        String[] output = new String[data.length()];
        int count = 0;
        for (Object i : data) {
            JSONObject track = ((JSONObject) i).getJSONObject("track");
            StringBuilder builder = new StringBuilder();
            for (Object artist : track.getJSONArray("artists"))
                builder.append(' ').append(((JSONObject) artist).getString("name"));

            String videoData = getData("https://youtube.googleapis.com/youtube/v3/search?part=snippet&maxResults=1&type=video&q=" +
                    URLEncoder.encode(track.getString("name"), UTF_8) + URLEncoder.encode(builder.toString(), UTF_8) + "&key=" + YT_APIKEY);

            if (videoData == null) return new String[]{"error"};
            System.out.println("https://youtube.googleapis.com/youtube/v3/search?part=snippet&maxResults=1&type=video&q=" +
                    URLEncoder.encode(track.getString("name"), UTF_8) + URLEncoder.encode(builder.toString(), UTF_8) + "&key=" + YT_APIKEY + "\n\n" + videoData);
            output[count] = (new JSONObject(videoData)).getJSONArray("items").getJSONObject(0).getJSONObject("id").getString("videoId");

        }

        return output;
    }
}
