package main.java.command.list.setting;

import main.java.Main;
import main.java.util.file.JsonFileManager;
import net.dv8tion.jda.api.events.interaction.SlashCommandEvent;
import org.json.JSONArray;
import org.json.JSONObject;

import java.io.IOException;
import java.net.*;
import java.nio.charset.StandardCharsets;
import java.util.List;

import static main.java.Main.emoji;
import static main.java.lang.LangKey.*;
import static main.java.util.EmbedCreator.createEmbed;
import static main.java.util.GuildUtil.guildID;
import static main.java.util.UrlDataGetter.getData;
import static main.java.util.UrlDataGetter.readResponse;

public class SettingSchool {
    private final String TAG = "[SettingSchool]";
    public JSONObject schoolFileData;
    private final JsonFileManager jsonFileManager;

    public SettingSchool() {
        jsonFileManager = new JsonFileManager(System.getProperty("user.dir") + "/school.json");
        schoolFileData = jsonFileManager.data;
    }

    public void onSchoolLogin(SlashCommandEvent event) {
        try {
            CookieManager cookieManager = new CookieManager(null, CookiePolicy.ACCEPT_ALL);
            CookieHandler.setDefault(cookieManager);

            //get Cookie
            HttpURLConnection conn = (HttpURLConnection) new URL("https://sctnank.ptivs.tn.edu.tw/skyweb/main.asp").openConnection();
            conn.setRequestProperty("User-Agent", "Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/95.0.4638.54 Safari/537.36");
            conn.setRequestProperty("Content-Type", "application/x-www-form-urlencoded");
            conn.setRequestMethod("POST");
            conn.setDoOutput(true);
            conn.setInstanceFollowRedirects(false);
            conn.getOutputStream().write(("txtid=" + event.getOption("id").getAsString() + "&txtpwd=" + URLEncoder.encode(event.getOption("password").getAsString(), "utf-8") + "&check=confirm").getBytes(StandardCharsets.UTF_8));
            conn.getOutputStream().close();
            readResponse(conn.getInputStream());
            conn.disconnect();
            if (conn.getResponseCode() != 302) {
                event.getHook().editOriginalEmbeds(createEmbed("帳號或密碼輸入錯誤!", 0xFF0000)).queue();
                return;
            }

            conn = (HttpURLConnection) new URL("https://sctnank.ptivs.tn.edu.tw/skyweb/f_left.asp").openConnection();
            conn.setRequestProperty("User-Agent", "Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/95.0.4638.54 Safari/537.36");
            conn.setRequestMethod("GET");
            readResponse(conn.getInputStream());
            conn.disconnect();

            schoolFileData.put(event.getUser().getId(), new String[]{event.getOption("id").getAsString(), event.getOption("password").getAsString()});
            jsonFileManager.saveFile();

            String data = getPageData("stu_result3.asp");

            String name = data.substring(data.indexOf(" ： "), data.indexOf("&nb" + 1));

            event.getHook().editOriginalEmbeds(createEmbed("設定完成，登入身分為：" + name, 0x00FFFF)).queue();

        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public void onAbsent(SlashCommandEvent event) {
    }

    private String getPageData(String url) {
        try {
            HttpURLConnection conn = (HttpURLConnection) new URL("https://sctnank.ptivs.tn.edu.tw/skyweb/stu/" + url).openConnection();
            conn.setRequestProperty("User-Agent", "Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/95.0.4638.54 Safari/537.36");
            conn.setRequestMethod("GET");
            String response = readResponse(conn.getInputStream());
            conn.disconnect();
            return response;
        } catch (IOException e) {
            e.printStackTrace();
        }
        return null;
    }
}