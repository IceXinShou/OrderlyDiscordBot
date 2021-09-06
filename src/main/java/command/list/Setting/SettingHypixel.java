package main.java.command.list.Setting;

import main.java.Main;
import main.java.util.file.JsonFileManager;
import net.dv8tion.jda.api.events.interaction.SlashCommandEvent;
import org.jetbrains.annotations.NotNull;
import org.json.JSONObject;

import java.time.Instant;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.List;

import static main.java.util.EmbedCreator.createEmbed;
import static main.java.util.GuildUtil.guildID;
import static main.java.util.UrlDataGetter.getData;

public class SettingHypixel {
    private final String TAG = "SettingOsu";
    public JSONObject hypixelFileData;
    private JsonFileManager jsonFileManager;

    public SettingHypixel() {
        jsonFileManager = new JsonFileManager(System.getProperty("user.dir") + "/hypixel.json");
        hypixelFileData = jsonFileManager.data;
    }


    public void onRegister(@NotNull SlashCommandEvent event) {
        String result = getData("https://api.mojang.com/users/profiles/minecraft/" + event.getOption("name").getAsString());
        if (result == null || result.length() == 0 || new JSONObject(result).has("error")) {
            event.getHook().editOriginalEmbeds(createEmbed("名字錯誤", 0xFF0000)).queue();
            return;
        }

        List<String> lang = Main.language.getGuildLang(event.getGuild().getId());
        hypixelFileData.put(event.getUser().getId(), (new JSONObject(result)).getString("id"));
        jsonFileManager.saveFile();

        event.getHook().editOriginalEmbeds(createEmbed("設定完成", 0xFF0000)).queue();
    }

    public void info(@NotNull SlashCommandEvent event) {
        String uuid;
        if ((uuid = getUUID(event)) == null)
            return;
        boolean onlineStats;
        String gameType = "";
        String mode = "";
        long firstTimeLogin;
        long lastTimeLogin;
        long lastTimeLogout;
        String displayName;
        String userLanguage;
        String rank = "";
        int color = 0xFFFFFF;
        int achievementPoints;
        long general_coins;
        long karma;
        JSONObject statusData;
        JSONObject playerData;
        try {
            statusData = new JSONObject(getData("https://api.hypixel.net/status?key=a661ea37-fffe-4c1b-b3dd-53af331e4aeb&uuid=" + uuid)).getJSONObject("session");
            playerData = new JSONObject(getData("https://api.hypixel.net/player?key=a661ea37-fffe-4c1b-b3dd-53af331e4aeb&uuid=" + uuid)).getJSONObject("player");
        } catch (Exception e) {
            System.err.println(e.getMessage());
            event.getHook().editOriginalEmbeds(createEmbed("錯誤，請重試！",0xFF0000)).queue();
            return;
        }

        onlineStats = statusData.getBoolean("online");
        if (onlineStats) {
            gameType = statusData.getString("gameType");
            mode = statusData.getString("mode");
        }
        firstTimeLogin = playerData.getLong("firstLogin");
        lastTimeLogin = playerData.getLong("lastLogin");
        lastTimeLogout = playerData.getLong("lastLogout");

        DateTimeFormatter format = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");
        ZoneId zoneId = ZoneId.of("Asia/Taipei");
        String firstTime = Instant.ofEpochMilli(firstTimeLogin).atZone(zoneId).toLocalDateTime().format(format);
        String lastTime = Instant.ofEpochMilli(lastTimeLogin).atZone(zoneId).toLocalDateTime().format(format);
        String lastoutTime = Instant.ofEpochMilli(lastTimeLogout).atZone(zoneId).toLocalDateTime().format(format);

        displayName = playerData.getString("displayname");
        userLanguage = playerData.getString("userLanguage");
        achievementPoints = playerData.getInt("achievementPoints");
        general_coins = playerData.getJSONObject("stats").getJSONObject("Arcade").getLong("coins");
        karma = playerData.getLong("karma");
        if (playerData.has("newPackageRank"))
            if (playerData.has("monthlyPackageRank")) {
                if (playerData.getString("monthlyPackageRank").equals("SUPERSTAR")) {
                    rank = "[MVP++] ";
                    color = 0xFFAA00;
                }
            } else
                switch (playerData.getString("newPackageRank")) {
                    case "MVP_PLUS" -> {
                        rank = "[MVP+] ";
                        color = 0x55FFFF;
                    }
                    case "MVP" -> {
                        rank = "[MVP] ";
                        color = 0x55FFFF;
                    }
                    case "VIP_PLUS" -> {
                        rank = "[VIP+] ";
                        color = 0x55FF55;
                    }
                    case "VIP" -> {
                        rank = "[VIP] ";
                        color = 0x55FF55;
                    }
                }


        StringBuilder description = new StringBuilder();
        description
                .append("▸ **狀態:** ").append(onlineStats ? "線上 (" + gameType + " - " + mode + ')' : "離線").append("\n\n")
                .append("▸ **第一次加入時間:** ").append(firstTime).append('\n')
                .append("▸ **使用語言:** ").append(userLanguage).append('\n')
                .append("▸ **人品值:** ").append(String.format("%,d", karma)).append('\n')
                .append("▸ **金錢:** ").append(String.format("%,d", general_coins)).append('\n')
                .append("▸ **成就點數:** ").append(String.format("%,d", achievementPoints)).append("\n\n")
                .append("▸ **最後一次上線:** ").append(lastTime).append('\n')
                .append("▸ **最後一次離線:** ").append(lastoutTime).append('\n');
        if (event.getGuild().getId().equals(guildID))
            event.getHook().editOriginalEmbeds(createEmbed(
                    "",
                    "https://plancke.io/hypixel/player/stats/" + uuid,
                    description.toString(),
                    "",
                    rank + displayName,
                    "https://crafatar.com/avatars/" + uuid,
                    color
            )).queue();
        else {
            event.getTextChannel().sendMessageEmbeds(createEmbed(
                    "",
                    "https://plancke.io/hypixel/player/stats/" + uuid,
                    description.toString(),
                    "",
                    rank + displayName,
                    "https://crafatar.com/avatars/" + uuid,
                    color
            )).queue();
            event.getHook().editOriginalEmbeds(createEmbed("完成", 0x00ffff)).queue();
        }
    }

    private String getUUID(@NotNull SlashCommandEvent event) {
        String uuid;
        if (event.getOption("name") != null) {
            String result = getData("https://api.mojang.com/users/profiles/minecraft/" + event.getOption("name").getAsString());
            if (result == null || result.length() == 0 || new JSONObject(result).has("error")) {
                event.getHook().editOriginalEmbeds(createEmbed("名字錯誤", 0xFF0000)).queue();
                return null;
            }
            uuid = (new JSONObject(result)).getString("id");
        } else if (!hypixelFileData.has(event.getUser().getId())) {
            event.getHook().editOriginalEmbeds(createEmbed("請先使用 `/hy setuser <name>` 綁定帳號", 0xFF0000)).queue();
            return null;
        } else {
            uuid = hypixelFileData.getString(event.getUser().getId());
        }
        return uuid;
    }
}