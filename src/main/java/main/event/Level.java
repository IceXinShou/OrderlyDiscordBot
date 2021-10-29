package main.event;

import main.util.file.JsonFileManager;
import main.util.file.LevelLogHelper;
import net.dv8tion.jda.api.events.message.guild.GuildMessageReceivedEvent;
import org.json.JSONObject;

import static main.util.JsonKeys.COUNT;

public class Level {

    LevelLogHelper llh = new LevelLogHelper();

    public void onGuildMessageReceived(GuildMessageReceivedEvent event) {
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

