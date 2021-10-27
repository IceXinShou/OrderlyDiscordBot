package main.java.command.list.setting;

import main.java.Main;
import main.java.util.file.JsonFileManager;
import net.dv8tion.jda.api.events.interaction.SlashCommandEvent;
import org.json.JSONObject;

import java.time.Instant;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.List;

import static main.java.lang.LangKey.*;
import static main.java.util.EmbedCreator.createEmbed;
import static main.java.util.GuildUtil.guildID;
import static main.java.util.UrlDataGetter.getData;

public class SettingHypixel {
    private final String TAG = "SettingOsu";
    public JSONObject hypixelFileData;
    private final JsonFileManager jsonFileManager;

    public SettingHypixel() {
        jsonFileManager = new JsonFileManager(System.getProperty("user.dir") + "/hypixel.json");
        hypixelFileData = jsonFileManager.data;
    }


    public void onRegister(SlashCommandEvent event) {
        List<String> lang = Main.language.getGuildLang(event.getGuild().getId());
        String result = getData("https://api.mojang.com/users/profiles/minecraft/" + event.getOption("name").getAsString());
        if (result == null || result.length() == 0 || new JSONObject(result).has("error")) {
            event.getHook().editOriginalEmbeds(createEmbed(lang.get(HYPIXEL_NAME_ERROR), 0xFF0000)).queue();
            return;
        }

        hypixelFileData.put(event.getUser().getId(), (new JSONObject(result)).getString("id"));
        jsonFileManager.saveFile();

        event.getHook().editOriginalEmbeds(createEmbed(lang.get(HYPIXEL_SETTING_SUCCESSFULLY), 0xFF0000)).queue();
    }

    public void info(SlashCommandEvent event) {
        List<String> lang = Main.language.getGuildLang(event.getGuild().getId());
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
        long karma;
        JSONObject statusData;
        JSONObject playerData;

        try {
            statusData = new JSONObject(getData("https://api.hypixel.net/status?key=e3a78fc6-2d53-438f-b905-0e13bed60224&uuid=" + uuid)).getJSONObject("session");
            playerData = new JSONObject(getData("https://api.hypixel.net/player?key=e3a78fc6-2d53-438f-b905-0e13bed60224&uuid=" + uuid)).getJSONObject("player");
        } catch (Exception e) {
            System.err.println(e.getMessage());
            event.getHook().editOriginalEmbeds(createEmbed(lang.get(HYPIXEL_ERROR_PLEASE_TRY_AGAIN), 0xFF0000)).queue();
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
        karma = playerData.getLong("karma");
        if (playerData.has("newPackageRank"))
            if (playerData.has("monthlyPackageRank") && playerData.getString("monthlyPackageRank").equals("SUPERSTAR")) {
                rank = "[MVP++] ";
                color = 0xFFAA00;
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
                .append("▸ **" + lang.get(HYPIXEL_STATUS) + ":** ").append(
                        onlineStats ? (lang.get(HYPIXEL_STATUS_ONLINE) + " (" + gameType + " - " + mode + ')') : lang.get(HYPIXEL_STATUS_OFFLINE)).append("\n\n")
                .append("▸ **" + lang.get(HYPIXEL_FIRST_JOIN_TIME) + ":** ").append(firstTime).append('\n')
                .append("▸ **" + lang.get(HYPIXEL_USING_LANGUAGE) + ":** ").append(userLanguage).append('\n')
                .append("▸ **" + lang.get(HYPIXEL_KARMA) + ":** ").append(String.format("%,d", karma)).append('\n')
                .append("▸ **" + lang.get(HYPIXEL_ACHIEVEMENT_POINT) + ":** ").append(String.format("%,d", achievementPoints)).append("\n\n")
                .append("▸ **" + lang.get(HYPIXEL_JOIN_TIME) + ":** ").append(lastTime).append('\n')
                .append("▸ **" + lang.get(HYPIXEL_LEFT_TIME) + ":** ").append(lastoutTime).append('\n');
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
            event.getHook().editOriginalEmbeds(createEmbed(lang.get(HYPIXEL_SETTING_SUCCESSFULLY), 0x00ffff)).queue();
        }
    }

    public void bedwars(SlashCommandEvent event) {
        List<String> lang = Main.language.getGuildLang(event.getGuild().getId());
        String uuid;
        if ((uuid = getUUID(event)) == null)
            return;
        String displayName;
        String rank = "";
        int color = 0xFFFFFF;
        long bedwars_coins;
        JSONObject playerData;
        JSONObject playerStats;
        try {
            playerData = new JSONObject(getData("https://api.hypixel.net/player?key=e3a78fc6-2d53-438f-b905-0e13bed60224&uuid=" + uuid)).getJSONObject("player");
            playerStats = playerData.getJSONObject("stats").getJSONObject("Bedwars");
        } catch (Exception e) {
            System.err.println(e.getMessage());
            event.getHook().editOriginalEmbeds(createEmbed(lang.get(HYPIXEL_ERROR_PLEASE_TRY_AGAIN), 0xFF0000)).queue();
            return;
        }

        displayName = playerData.getString("displayname");
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

        bedwars_coins = playerStats.getLong("coins");

        StringBuilder description = new StringBuilder();
//        description
//                .append("▸ **狀態:** ").append(onlineStats ? "線上 (" + gameType + " - " + mode + ')' : "離線").append("\n\n")
//                .append("▸ **第一次加入時間:** ").append(firstTime).append('\n')
//                .append("▸ **使用語言:** ").append(userLanguage).append('\n')
//                .append("▸ **人品值:** ").append(String.format("%,d", karma)).append('\n')
//                .append("▸ **金錢:** ").append(String.format("%,d", general_coins)).append('\n')
//                .append("▸ **成就點數:** ").append(String.format("%,d", achievementPoints)).append("\n\n")
//                .append("▸ **最後一次上線:** ").append(lastTime).append('\n')
//                .append("▸ **最後一次離線:** ").append(lastoutTime).append('\n');
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
            event.getHook().editOriginalEmbeds(createEmbed(lang.get(HYPIXEL_SETTING_SUCCESSFULLY), 0x00ffff)).queue();
        }
    }

    private String getUUID(SlashCommandEvent event) {
        List<String> lang = Main.language.getGuildLang(event.getGuild().getId());
        String uuid;
        if (event.getOption("name") != null) {
            String result = getData("https://api.mojang.com/users/profiles/minecraft/" + event.getOption("name").getAsString());
            if (result == null || result.length() == 0 || new JSONObject(result).has("error")) {
                event.getHook().editOriginalEmbeds(createEmbed(lang.get(HYPIXEL_NAME_ERROR), 0xFF0000)).queue();
                return null;
            }
            uuid = (new JSONObject(result)).getString("id");
        } else if (!hypixelFileData.has(event.getUser().getId())) {
            event.getHook().editOriginalEmbeds(createEmbed(lang.get(HYPIXEL_REGISTER_FIRST), 0xFF0000)).queue();
            return null;
        } else
            uuid = hypixelFileData.getString(event.getUser().getId());

        return uuid;
    }
}