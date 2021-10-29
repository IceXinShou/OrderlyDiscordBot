package main.command.list.setting;

import main.util.file.JsonFileManager;
import net.dv8tion.jda.api.entities.MessageEmbed;
import net.dv8tion.jda.api.events.interaction.SelectionMenuEvent;
import net.dv8tion.jda.api.events.interaction.SlashCommandEvent;
import net.dv8tion.jda.api.interactions.components.selections.SelectionMenu;
import org.json.JSONObject;

import java.io.IOException;
import java.net.*;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;

import static main.util.EmbedCreator.createEmbed;
import static main.util.UrlDataGetter.readResponse;

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
        if (name == null)
            event.getHook().editOriginalEmbeds(createEmbed("帳號或密碼錯誤! 請使用 `/school login`", 0xFF0000));
        else
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

    public void onSlashInfo(SlashCommandEvent event) {
        if ((checkAccount(event.getUser().getId())) == null) {
            event.getHook().editOriginalEmbeds(createEmbed("請先使用 `/school login` 登入", 0x00FFFF)).queue();
            return;
        }

        SelectionMenu.Builder builder = SelectionMenu.
                create("SS:onSI:" + event.getUser().getId() + ':');

        builder.addOption("完整記錄", "full");
        builder.addOption("獎懲紀錄", "reward");
        builder.addOption("學期成績", "score");
        builder.addOption("缺曠紀錄", "class");

        event.getHook().editOriginalComponents().setEmbeds(createEmbed("請選擇資訊", 0x00FFFF)).setActionRow(builder.build()).queue();
    }

    public void onSelectInfo(SelectionMenuEvent event, String[] args) {
        if (!args[0].equals("SS") || !args[1].equals("onSI"))
            return;
        String userName;
        if ((userName = checkAccount(event.getUser().getId())) == null) {
            event.getHook().editOriginalEmbeds(createEmbed("請先使用 `/school login` 登入", 0x00FFFF)).queue();
            return;
        }

        switch (event.getValues().get(0)) {
            case "full" -> {
                event.replyEmbeds(getReward(userName)).setEphemeral(true).queue();
                break;
            }
            case "reward" -> {
                event.replyEmbeds(getReward(userName)).setEphemeral(true).queue();
                break;
            }
//            case "score" -> {
//                break;
//            }
//            case "class" -> {
//                break;
//            }
            default -> event.replyEmbeds(createEmbed("未知的錯誤", 0xFF0000)).setEphemeral(true).queue();
        }
    }

    private String checkAccount(String userID) {
        if (schoolFileData.has(userID)) {
            String name;
            if ((name = onLogin(schoolFileData.getJSONObject(userID).getString("id"),
                    schoolFileData.getJSONObject(userID).getString("password"), userID)) != null)
                return name;
        }
        return null;
    }

    private MessageEmbed getReward(String username) {

        int[] reward = new int[8];
        String rewardData = getPageData("stu_result6.asp");

        int i = 0;
        for (String datum : rewardData.substring(rewardData.indexOf("<b>總計</b>") + 55, rewardData.indexOf("歷年來") - 190).split("</td>")) {
            if (datum.contains("<b>"))
                reward[i] = Integer.parseInt(datum.substring(datum.indexOf("<b>") + 3, datum.indexOf("</b>")));
            else
                reward[i] = Integer.parseInt(datum.substring(datum.indexOf("5\">") + 3, datum.indexOf("</f")));
            i++;
        }

        List<MessageEmbed.Field> fields = new ArrayList<>();

        fields.add(new MessageEmbed.Field("大功", '`' + String.valueOf(reward[0]) + '`', true));
        fields.add(new MessageEmbed.Field("小功", '`' + String.valueOf(reward[1]) + '`', true));
        fields.add(new MessageEmbed.Field("嘉獎", '`' + String.valueOf(reward[2]) + '`', true));
        fields.add(new MessageEmbed.Field("大過", '`' + String.valueOf(reward[4]) + '`', true));
        fields.add(new MessageEmbed.Field("小過", '`' + String.valueOf(reward[5]) + '`', true));
        fields.add(new MessageEmbed.Field("警告", '`' + String.valueOf(reward[6]) + '`', true));

        return createEmbed("", "", "獎懲紀錄 | " + username, fields, null, 0x00FFFF);
    }
}