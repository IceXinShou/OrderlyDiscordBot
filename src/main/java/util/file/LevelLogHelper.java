package main.java.util.file;

import java.io.File;
import java.util.HashMap;
import java.util.Map;

import static main.java.BotSetting.guildSettingFolder;


public class LevelLogHelper {
    Map<String, JsonFileManager> levelFolders = new HashMap<>();

    public JsonFileManager getLevelFileManager(String guildID) {
        if (levelFolders.containsKey(guildID))
            return levelFolders.get(guildID);

        File filepath = new File(guildSettingFolder.getPath() + '/' + guildID);
        if (!filepath.exists())
            filepath.mkdirs();

        JsonFileManager levelFileManager = new JsonFileManager(filepath.getPath() + "/memberLevel.json");
        levelFolders.put(guildID, levelFileManager);
        return levelFileManager;
    }
}
