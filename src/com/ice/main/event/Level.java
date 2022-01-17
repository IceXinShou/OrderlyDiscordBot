package com.ice.main.event;

import com.ice.main.util.file.JsonFileManager;
import com.ice.main.util.file.LevelLogHelper;
import net.dv8tion.jda.api.events.message.MessageReceivedEvent;
import org.json.JSONObject;

import static com.ice.main.util.JsonKeys.COUNT;

public class Level {

    LevelLogHelper llh = new LevelLogHelper();

    public void onGuildMessageReceived(MessageReceivedEvent event) {
        if (event.getMember() != null) {
            JsonFileManager channelFileManager = llh.getLevelFileManager(event.getGuild().getId());
            JSONObject data = channelFileManager.data;
            Long count = 0L;
            // 有訊息資料

            if (data.has(event.getMember().getId())) {
                // 取得訊息資料
                JSONObject levelLog = data.getJSONObject(event.getMember().getId());
                count = levelLog.getLong(COUNT);
                count++;
            }
            data.put(event.getMember().getId(), new JSONObject().put(COUNT, count));
            channelFileManager.saveFile();
        }
    }
}

