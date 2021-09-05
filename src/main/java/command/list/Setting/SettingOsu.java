package main.java.command.list.Setting;

import main.java.Main;
import main.java.util.file.JsonFileManager;
import net.dv8tion.jda.api.events.interaction.SlashCommandEvent;
import org.jetbrains.annotations.NotNull;
import org.json.JSONArray;
import org.json.JSONObject;

import java.util.List;

import static main.java.Main.emoji;
import static main.java.util.EmbedCreator.createEmbed;
import static main.java.util.GuildUtil.guildID;
import static main.java.util.UrlDataGetter.getData;

public class SettingOsu {
    private final String TAG = "SettingOsu";
    public JSONObject osuFileData;
    private JsonFileManager jsonFileManager;

    public SettingOsu() {
        jsonFileManager = new JsonFileManager(System.getProperty("user.dir") + "/osu.json");
        osuFileData = jsonFileManager.data;
    }


    public void onRegister(@NotNull SlashCommandEvent event) {
        // https://osu.ppy.sh/api/get_user_best?k=b79a9a88c8aa44d689c44b6af2fe3a356e301f48&u=
        JSONArray dataArray = new JSONArray(getData("https://osu.ppy.sh/api/get_user?k=b79a9a88c8aa44d689c44b6af2fe3a356e301f48&u=" + event.getOption("name").getAsString().replace(' ', '_')));

        if (dataArray.length() == 0) {
            event.getHook().editOriginalEmbeds(createEmbed("名字錯誤", 0xFF0000)).queue();
            return;
        }

        List<String> lang = Main.language.getGuildLang(event.getGuild().getId());
        osuFileData.put(event.getUser().getId(), dataArray.getJSONObject(0).getString("user_id"));
        jsonFileManager.saveFile();

        event.getHook().editOriginalEmbeds(createEmbed("設定完成", 0xFF0000)).queue();
    }

    public void onTop(@NotNull SlashCommandEvent event) {
        String osuID;
        if ((osuID = getOsuID(event)) == null)
            return;

        JSONObject userData = new JSONArray(getData("https://osu.ppy.sh/api/get_user?k=b79a9a88c8aa44d689c44b6af2fe3a356e301f48&type=id&u=" + osuID)).getJSONObject(0);
        StringBuilder description = new StringBuilder();
        for (Object o : new JSONArray(getData("https://osu.ppy.sh/api/get_user_best?k=b79a9a88c8aa44d689c44b6af2fe3a356e301f48&type=id&u=" + osuID))) {
            JSONObject data = (JSONObject) o;
            JSONObject mapData = new JSONArray(getData("https://osu.ppy.sh/api/get_beatmaps?k=b79a9a88c8aa44d689c44b6af2fe3a356e301f48&b=" + data.getString("beatmap_id"))).getJSONObject(0);
            String rankEmoji = "";
            switch (data.getString("rank")) {
                case "SSH" -> rankEmoji = emoji.osu_ssh.getAsMention();
                case "SH" -> rankEmoji = emoji.osu_sh.getAsMention();
                case "SS" -> rankEmoji = emoji.osu_ss.getAsMention();
                case "S" -> rankEmoji = emoji.osu_s.getAsMention();
                case "A" -> rankEmoji = emoji.osu_a.getAsMention();
                case "B" -> rankEmoji = emoji.osu_b.getAsMention();
                case "C" -> rankEmoji = emoji.osu_c.getAsMention();
                case "F" -> rankEmoji = emoji.osu_f.getAsMention();
            }
            description.append(rankEmoji)
                    .append(" **[").append(mapData.getString("title")).append(" [").append(mapData.getString("version")).append(']').append("](https://osu.ppy.sh/beatmapsets/").append(mapData.getString("beatmapset_id")).append("/)**\n")
                    .append("▸ **連擊:** ").append(data.getString("maxcombo")).append('/').append(mapData.getString("max_combo")).append(' ')
                    .append("▸ **PP:** ").append(String.format("%.2f", Float.parseFloat(data.getString("pp")))).append(' ').append('\n')
                    .append("▸ **分數:** ").append(String.format("%,d", Integer.parseInt(data.getString("score")))).append("\n\n");
        }
        if (event.getGuild().getId().equals(guildID))
            event.getHook().editOriginalEmbeds(createEmbed(
                    "",
                    "https://a.ppy.sh/" + osuID + "?476",
                    "https://osu.ppy.sh/users/" + osuID,
                    description.toString(),
                    "",
                    userData.getString("username"),
                    "https://www.countryflags.io/" + userData.getString("country") + "/flat/64.png",
                    0x00FFFF
            )).queue();
        else {
            event.getTextChannel().sendMessageEmbeds(createEmbed(
                    "",
                    "https://a.ppy.sh/" + osuID + "?476",
                    "https://osu.ppy.sh/users/" + osuID,
                    description.toString(),
                    "",
                    userData.getString("username"),
                    "https://www.countryflags.io/" + userData.getString("country") + "/flat/64.png",
                    0x00FFFF
            )).queue();
            event.getHook().editOriginalEmbeds(createEmbed("完成", 0x00ffff)).queue();
        }
    }

    public void onPrevious(@NotNull SlashCommandEvent event) {
        String osuID;
        if ((osuID = getOsuID(event)) == null)
            return;
        JSONObject userData = new JSONArray(getData("https://osu.ppy.sh/api/get_user?k=b79a9a88c8aa44d689c44b6af2fe3a356e301f48&type=id&u=" + osuID)).getJSONObject(0);
        JSONArray mapArray = new JSONArray(getData("https://osu.ppy.sh/api/get_user_recent?k=b79a9a88c8aa44d689c44b6af2fe3a356e301f48&type=id&limit=1&u=" + osuID));
        if (mapArray.length() == 0) {
            event.getHook().editOriginalEmbeds(createEmbed("你沒有在24小時內玩過遊戲", 0xFF0000)).queue();
            return;
        }
        JSONObject data = new JSONArray(getData("https://osu.ppy.sh/api/get_user_recent?k=b79a9a88c8aa44d689c44b6af2fe3a356e301f48&type=id&limit=1&u=" + osuID)).getJSONObject(0);
        JSONObject mapData = new JSONArray(getData("https://osu.ppy.sh/api/get_beatmaps?k=b79a9a88c8aa44d689c44b6af2fe3a356e301f48&b=" + data.getString("beatmap_id"))).getJSONObject(0);
        String rankEmoji = "";
        switch (data.getString("rank")) {
            case "SSH" -> rankEmoji = emoji.osu_ssh.getAsMention();
            case "SH" -> rankEmoji = emoji.osu_sh.getAsMention();
            case "SS" -> rankEmoji = emoji.osu_ss.getAsMention();
            case "S" -> rankEmoji = emoji.osu_s.getAsMention();
            case "A" -> rankEmoji = emoji.osu_a.getAsMention();
            case "B" -> rankEmoji = emoji.osu_b.getAsMention();
            case "C" -> rankEmoji = emoji.osu_c.getAsMention();
            case "F" -> rankEmoji = emoji.osu_f.getAsMention();
        }
        if (event.getGuild().getId().equals(guildID)) {
            event.getHook().editOriginalEmbeds(createEmbed(
                    rankEmoji + ' ' + mapData.getString("title") + " [" + mapData.getString("version") + ']',
                    "https://assets.ppy.sh/beatmaps/" + mapData.getString("beatmapset_id") + "/covers/cover.jpg\n",
                    "https://osu.ppy.sh/beatmaps/" + data.getString("beatmap_id"),
                    "https://osu.ppy.sh/users/" + osuID,
                    "\n▸ **連擊:** " + data.getString("maxcombo") + '/' + mapData.getString("max_combo") + "\n▸ **PP:** " + String.format("%.2f", Float.parseFloat(data.getString("pp"))) + "\n▸ **分數:** " + String.format("%,d", Integer.parseInt(data.getString("score"))),
                    "",
                    0x00FFFF,
                    userData.getString("username"),
                    "https://a.ppy.sh/" + osuID + "?476"
            )).queue();
        } else {
            event.getTextChannel().sendMessageEmbeds(createEmbed(
                    rankEmoji + ' ' + mapData.getString("title") + " [" + mapData.getString("version") + ']',
                    "https://assets.ppy.sh/beatmaps/" + mapData.getString("beatmapset_id") + "/covers/cover.jpg\n",
                    "https://osu.ppy.sh/beatmaps/" + data.getString("beatmap_id"),
                    "https://osu.ppy.sh/users/" + osuID,
                    "\n▸ **連擊:** " + data.getString("maxcombo") + '/' + mapData.getString("max_combo") + /*"\n▸ **PP:** " + String.format("%.2f", Float.parseFloat(data.getString("pp"))) +*/ "\n▸ **分數:** " + String.format("%,d", Integer.parseInt(data.getString("score"))),
                    "",
                    0x00FFFF,
                    userData.getString("username"),
                    "https://a.ppy.sh/" + osuID + "?476"
            )).queue();
            event.getHook().editOriginalEmbeds(createEmbed("完成", 0x00FFFF)).queue();
        }
    }

    public void search(@NotNull SlashCommandEvent event) {
        List<String> lang = Main.language.getGuildLang(event.getGuild().getId());
        String result;
        if (event.getOption("name") == null) {
            String osuID;
            if ((osuID = getOsuID(event)) == null) {
                return;
            }
            result = getData("https://osu.ppy.sh/api/get_user?k=b79a9a88c8aa44d689c44b6af2fe3a356e301f48&u=" + osuID);
        } else {
            result = getData("https://osu.ppy.sh/api/get_user?k=b79a9a88c8aa44d689c44b6af2fe3a356e301f48&u=" + event.getOption("name").getAsString().replace(' ', '_'));
            if (new JSONArray(result).length() == 0) {
                event.getHook().editOriginalEmbeds(createEmbed("名字錯誤", 0xFF0000)).queue();
                return;
            }
        }
        JSONObject data = new JSONArray(result).getJSONObject(0);
        if (event.getGuild().getId().equals(guildID))
            event.getHook().editOriginalEmbeds(createEmbed(
                    "",
                    "https://a.ppy.sh/" + data.getString("user_id") + "?476",
                    "https://osu.ppy.sh/users/" + data.getString("user_id"),
                    "" +
                            "▸**排行: **" + '#' + String.format("%,d", Integer.parseInt(data.getString("pp_rank"))) + " (" + data.getString("country") + " #" + String.format("%,d", Integer.parseInt(data.getString("pp_country_rank"))) + ')' + '\n' +
                            "▸**等級: **" + ((int) (Float.parseFloat(data.getString("level")))) + '\n' +
                            "▸**PP: **" + String.format("%,.2f", Float.parseFloat(data.getString("pp_raw"))) + "  **Acc: **" + String.format("%.2f", Float.parseFloat(data.getString("accuracy"))) + "%\n" +
                            "▸**遊玩次數: **" + String.format("%,d", Integer.parseInt(data.getString("playcount"))) + " (" + String.format("%.2f", (Float.parseFloat(data.getString("total_seconds_played")) / 60 / 60)) + " 小時)\n" +
//                        "▸**等級: **" + String.format("%.0f", Float.parseFloat(data.getString("level"))) + '\n' +
                            "▸**成績: **" +
                            emoji.osu_ssh.getAsMention() + '`' + data.getString("count_rank_ssh") + '`' +
                            emoji.osu_sh.getAsMention() + '`' + data.getString("count_rank_sh") + '`' +
                            emoji.osu_ss.getAsMention() + '`' + data.getString("count_rank_ss") + '`' +
                            emoji.osu_s.getAsMention() + '`' + data.getString("count_rank_s") + '`' +
                            emoji.osu_a.getAsMention() + '`' + data.getString("count_rank_a") + '`',
                    "",
                    data.getString("username"),
                    "https://www.countryflags.io/" + data.getString("country") + "/flat/64.png",
                    0x00FFFF
            )).queue();
        else {
            event.getTextChannel().sendMessageEmbeds(createEmbed(
                    "",
                    "https://a.ppy.sh/" + data.getString("user_id") + "?476",
                    "https://osu.ppy.sh/users/" + data.getString("user_id"),
                    "" +
                            "▸**排行: **" + '#' + String.format("%,d", Integer.parseInt(data.getString("pp_rank"))) + " (" + data.getString("country") + " #" + String.format("%,d", Integer.parseInt(data.getString("pp_country_rank"))) + ')' + '\n' +
                            "▸**等級: **" + ((int) (Float.parseFloat(data.getString("level")))) + '\n' +
                            "▸**PP: **" + String.format("%,.2f", Float.parseFloat(data.getString("pp_raw"))) + "  **Acc: **" + String.format("%.2f", Float.parseFloat(data.getString("accuracy"))) + "%\n" +
                            "▸**遊玩次數: **" + String.format("%,d", Integer.parseInt(data.getString("playcount"))) + " (" + String.format("%.2f", (Float.parseFloat(data.getString("total_seconds_played")) / 60 / 60)) + " 小時)\n" +
//                        "▸**等級: **" + String.format("%.0f", Float.parseFloat(data.getString("level"))) + '\n' +
                            "▸**成績: **" +
                            emoji.osu_ssh.getAsMention() + '`' + data.getString("count_rank_ssh") + '`' +
                            emoji.osu_sh.getAsMention() + '`' + data.getString("count_rank_sh") + '`' +
                            emoji.osu_ss.getAsMention() + '`' + data.getString("count_rank_ss") + '`' +
                            emoji.osu_s.getAsMention() + '`' + data.getString("count_rank_s") + '`' +
                            emoji.osu_a.getAsMention() + '`' + data.getString("count_rank_a") + '`',
                    "",
                    data.getString("username"),
                    "https://www.countryflags.io/" + data.getString("country") + "/flat/64.png",
                    0x00FFFF
            )).queue();
            event.getHook().editOriginalEmbeds(createEmbed("完成", 0x00ffff)).queue();
        }

    }

    private String getOsuID(@NotNull SlashCommandEvent event) {
        String osuID;
        if (event.getOption("name") != null) {
            JSONArray dataArray = new JSONArray(getData("https://osu.ppy.sh/api/get_user?k=b79a9a88c8aa44d689c44b6af2fe3a356e301f48&u=" + event.getOption("name").getAsString().replace(' ', '_')));
            if (dataArray.length() == 0) {
                event.getHook().editOriginalEmbeds(createEmbed("名字錯誤", 0xFF0000)).queue();
                return null;
            }
            osuID = dataArray.getJSONObject(0).getString("user_id");
        } else if (!osuFileData.has(event.getUser().getId())) {
            event.getHook().editOriginalEmbeds(createEmbed("請先使用 `/osu setuser <name>` 綁定帳號", 0xFF0000)).queue();
            return null;
        } else {
            osuID = osuFileData.getString(event.getUser().getId());
        }
        return osuID;
    }
}