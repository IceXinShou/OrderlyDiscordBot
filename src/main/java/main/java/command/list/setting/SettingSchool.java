package main.java.command.list.setting;

import main.java.util.file.JsonFileManager;
import net.dv8tion.jda.api.events.interaction.SlashCommandEvent;
import org.json.JSONObject;

import java.io.IOException;
import java.net.*;
import java.nio.charset.StandardCharsets;

import static main.java.util.EmbedCreator.createEmbed;
import static main.java.util.UrlDataGetter.readResponse;

public class SettingSchool {
    private final String TAG = "[SettingSchool]";
    public JSONObject schoolFileData;
    private final JsonFileManager jsonFileManager;

    public SettingSchool() {
        jsonFileManager = new JsonFileManager(System.getProperty("user.dir") + "/school.json");
        schoolFileData = jsonFileManager.data;
    }

    private String onLogin(String id, String password, String userID) {
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
            conn.getOutputStream().write(("txtid=" + id + "&txtpwd=" + URLEncoder.encode(password, "utf-8") + "&check=confirm").getBytes(StandardCharsets.UTF_8));
            conn.getOutputStream().close();
            readResponse(conn.getInputStream());
            conn.disconnect();

            if (conn.getResponseCode() != 302)
                return null;

            conn = (HttpURLConnection) new URL("https://sctnank.ptivs.tn.edu.tw/skyweb/f_left.asp").openConnection();
            conn.setRequestProperty("User-Agent", "Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/95.0.4638.54 Safari/537.36");
            conn.setRequestMethod("GET");
            readResponse(conn.getInputStream());
            conn.disconnect();

            JSONObject object = new JSONObject();
            object.put("id", id).put("password", password);

            schoolFileData.put(userID, object);
            jsonFileManager.saveFile();

            String data = getPageData("stu_result3.asp");

            return data.substring(data.indexOf(" ： ") + 3, data.indexOf("&nb"));

        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }

    public void onSchoolLogin(SlashCommandEvent event) {
        String name = onLogin(event.getOption("id").getAsString(), event.getOption("password").getAsString(), event.getUser().getId());
        if (name == null) {
            event.getHook().editOriginalEmbeds(createEmbed("帳號或密碼錯誤! 請使用 `/school login`", 0xFF0000));
            return;
        } else
            event.getHook().editOriginalEmbeds(createEmbed("設定完成，登入身分為：" + name, 0x00FFFF)).queue();
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

    public void onInfo(SlashCommandEvent event) {
        if (!checkAccount(event))
            return;

        int[] reward = new int[6];
        String rewardData = getPageData("stu_result6.asp");
        rewardData.substring(rewardData.indexOf("總計") + 10, rewardData.indexOf("歷年來") - 10);
        System.out.println(rewardData);
//        reward[0] =
    }

    private boolean checkAccount(SlashCommandEvent event) {
        if (schoolFileData.has(event.getUser().getId()) &&
                onLogin(schoolFileData.getJSONObject(event.getUser().getId()).getString("id"),
                        schoolFileData.getJSONObject(event.getUser().getId()).getString("password"), event.getUser().getId()) != null)
            return true;
        else
            event.getHook().editOriginalEmbeds(createEmbed("請先使用 `/school login` 登入", 0x00FFFF)).queue();
        return false;
    }
}