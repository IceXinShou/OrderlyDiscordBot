package main.java.util.file;

import java.io.File;
import java.util.HashMap;
import java.util.Map;

import static main.java.BotSetting.guildSettingFolder;


public class LevelLogHelper {
    private final Map<String, JsonFileManager> levelFolders = new HashMap<>();

    public JsonFileManager getLevelFileManager(String guildID) {
        JsonFileManager fileManager = levelFolders.get(guildID);
        if (fileManager != null)
            return fileManager;

        File filepath = new File(guildSettingFolder.getPath() + '/' + guildID);
        if (!filepath.exists())
            filepath.mkdirs();

        JsonFileManager levelFileManager = new JsonFileManager(filepath.getPath() + "/memberLevel.json");
        levelFolders.put(guildID, levelFileManager);
        return levelFileManager;
    }
}
