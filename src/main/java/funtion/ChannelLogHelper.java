package main.java.funtion;

import java.io.File;
import java.util.HashMap;
import java.util.Map;

import static main.java.BotSetting.channelLogFolder;
import static main.java.BotSetting.levelFolder;

public class ChannelLogHelper {
    Map<String, JsonFileManager> channelLogs = new HashMap<>();
    Map<String, JsonFileManager> levelFolders = new HashMap<>();

    public JsonFileManager getChannelFileManager(String channelID) {
        if (channelLogs.containsKey(channelID))
            return channelLogs.get(channelID);

        JsonFileManager channelFileManager = new JsonFileManager(channelLogFolder.getPath() + '/' + channelID + ".json");
        channelLogs.put(channelID, channelFileManager);
        return channelFileManager;
    }

    public JsonFileManager getLevelFileManager(String guildID, String type) {
        File filepath = new File(levelFolder.getPath() + '/' + guildID);
        if (!filepath.exists())
            filepath.mkdir();

        if (levelFolders.containsKey(guildID))
            return levelFolders.get(guildID);

        JsonFileManager levelFileManager = new JsonFileManager(filepath.getPath() + '/' + type + ".json");
        levelFolders.put(guildID, levelFileManager);
        return levelFileManager;
    }

    public void saveALL() {
        channelLogs.forEach((key, fileManager) -> fileManager.saveFile());
        levelFolders.forEach((key, fileManager) -> fileManager.saveFile());
    }
}
