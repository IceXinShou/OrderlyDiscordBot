package multiBot;

import main.java.BotSetting;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.net.URLConnection;
import java.nio.charset.StandardCharsets;

public class Test {
    Test() {
        new BotSetting();
//        MultiMusicBotManager multiMusicBotManager = new MultiMusicBotManager();

// https://youtube.googleapis.com/youtube/v3/search?part=snippet&maxResults=25&order=searchSortUnspecified&q=Faded&key=

//        System.out.println(url);
//
//        System.out.println(getUrlData(url));
//
//        System.out.println(result.getJSONObject("pageInfo").getInt("totalResults"));
//        System.out.println(result.getJSONArray("items").getJSONObject(0).getJSONObject("snippet").getString("title"));
//        System.out.println(getUrlData(url));
    }

    private String getUrlData(String url) {
        try {
            URLConnection connection = new URL(url).openConnection();
            InputStream in = connection.getInputStream();
            ByteArrayOutputStream out = new ByteArrayOutputStream();

            byte[] buff = new byte[1024];
            int length;
            while ((length = in.read(buff)) > 0) {
                out.write(buff, 0, length);
            }
            return out.toString(StandardCharsets.UTF_8);
        } catch (IOException e) {
            System.err.println(e.getMessage());
            return null;
        }
    }

    public static void main(String[] args) {
        new Test();
    }
}
