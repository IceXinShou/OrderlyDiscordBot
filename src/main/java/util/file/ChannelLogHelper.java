package main.java.util.file;

import java.io.File;
import java.util.HashMap;
import java.util.Map;

import static main.java.BotSetting.channelLogFolder;
import static main.java.BotSetting.guildSettingFolder;

public class ChannelLogHelper {
    Map<String, JsonFileManager> channelLogs = new HashMap<>();


    public JsonFileManager getChannelFileManager(String guildID, String channelID) {
        if (channelLogs.containsKey(channelID))
            return channelLogs.get(channelID);

        File filepath = new File(guildSettingFolder.getPath() + '/' + guildID + '/' + channelLogFolder.getPath());
        if (!filepath.exists())
            filepath.mkdirs();

        JsonFileManager channelFileManager = new JsonFileManager(filepath.getPath() + '/' + channelID + ".json");
        channelLogs.put(channelID, channelFileManager);
        return channelFileManager;
    }

}
