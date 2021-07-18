package main.java.funtion;

import java.util.HashMap;
import java.util.Map;

import static main.java.BotSetting.channelLogFolder;

public class ChannelLogHelper {
    Map<String, JsonFileManager> channelLogs = new HashMap<>();

    public JsonFileManager getChannelFileManager(String channelID) {
        if (channelLogs.containsKey(channelID))
            return channelLogs.get(channelID);

        JsonFileManager channelFileManager = new JsonFileManager(channelLogFolder.getPath() + '/' + channelID + ".json");
        channelLogs.put(channelID, channelFileManager);
        return channelFileManager;
    }

    public void saveALL() {
        channelLogs.forEach((key, fileManager) -> fileManager.saveFile());
    }
}
