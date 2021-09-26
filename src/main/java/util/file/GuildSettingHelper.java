package main.java.util.file;

import net.dv8tion.jda.api.entities.Guild;
import org.json.JSONObject;

import java.io.File;
import java.util.HashMap;
import java.util.Map;

import static main.java.BotSetting.guildSettingFolder;

public class GuildSettingHelper {
    // GuildID    JsonFileManager
    private final Map<String, JsonFileManager> guildSetting = new HashMap<>();

    public JsonFileManager getGuildSettingManager(String guildID) {
        JsonFileManager fileManager = guildSetting.get(guildID);
        if (fileManager != null)
            return fileManager;

        File filepath = new File(guildSettingFolder.getPath() + '/' + guildID);
        if (!filepath.exists())
            filepath.mkdirs();

        JsonFileManager levelFileManager = new JsonFileManager(filepath.getPath() + "/guildSetting.json");
        guildSetting.put(guildID, levelFileManager);
        return levelFileManager;
    }

    public JSONObject getSettingData(Guild guild, String key) {
        JsonFileManager fileManager = getGuildSettingManager(guild.getId());
        Object data = fileManager.data.get(key);
        if (data != null)
            return (JSONObject) data;
        JSONObject newJson = new JSONObject();
        fileManager.data.put(key, data);
        fileManager.saveFile();
        return newJson;
    }
}
