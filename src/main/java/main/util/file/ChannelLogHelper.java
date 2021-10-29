package main.util.file;

import java.io.File;
import java.util.HashMap;
import java.util.Map;

import static main.BotSetting.channelLogFolder;
import static main.BotSetting.guildSettingFolder;

public class ChannelLogHelper {
    private final Map<String, JsonFileManager> channelLogs = new HashMap<>();

    @SuppressWarnings("ALL")
    public JsonFileManager getChannelFileManager(String guildID, String channelID) {
        if (channelLogs.containsKey(channelID))
            return channelLogs.get(channelID);

        JsonFileManager fileManager = channelLogs.get(channelID);
        if (fileManager != null)
            return fileManager;

        File filepath = new File(guildSettingFolder.getPath() + '/' + guildID + '/' + channelLogFolder.getPath());
        if (!filepath.exists())
            filepath.mkdirs();

        JsonFileManager channelFileManager = new JsonFileManager(filepath.getPath() + '/' + channelID + ".json");
        channelLogs.put(channelID, channelFileManager);
        return channelFileManager;
    }

}
