package main.java.util.file;

import net.dv8tion.jda.api.entities.Guild;
import org.json.JSONObject;

import java.io.File;
import java.util.HashMap;
import java.util.Map;

import static main.java.BotSetting.guildSettingFolder;

public class GuildSettingHelper {
    // GuildID    JsonFileManager
    Map<String, JsonFileManager> levelFolders = new HashMap<>();

    public JsonFileManager getGuildSettingManager(String guildID) {
        if (levelFolders.containsKey(guildID))
            return levelFolders.get(guildID);

        File filepath = new File(guildSettingFolder.getPath() + '/' + guildID);
        if (!filepath.exists())
            filepath.mkdirs();

        JsonFileManager levelFileManager = new JsonFileManager(filepath.getPath() + "/guildSetting.json");
        levelFolders.put(guildID, levelFileManager);
        return levelFileManager;
    }

    public JSONObject getSettingData(Guild guild, String key) {
        JsonFileManager fileManager = getGuildSettingManager(guild.getId());
        if (fileManager.data.has(key))
            return fileManager.data.getJSONObject(key);
        else {
            JSONObject data = new JSONObject();
            getGuildSettingManager(guild.getId()).data.put(key, data);
            return data;
        }
    }
}
