package main.java.automatic;

import main.java.funtion.ChannelLogHelper;
import main.java.funtion.JsonFileManager;
import net.dv8tion.jda.api.events.message.guild.GuildMessageReceivedEvent;
import net.dv8tion.jda.api.hooks.ListenerAdapter;
import org.jetbrains.annotations.NotNull;
import org.json.JSONObject;

import static main.java.util.JsonKeys.COUNT;

public class Level extends ListenerAdapter {

    ChannelLogHelper clh = new ChannelLogHelper();

    @Override
    public void onGuildMessageReceived(@NotNull GuildMessageReceivedEvent event) {
        JsonFileManager channelFileManager = clh.getLevelFileManager(event.getGuild().getId(), "message");
        JSONObject data = channelFileManager.data;
        Long count = 0L;
        // 有訊息資料
        if (data.has(event.getMember().getId())) {
            // 取得訊息資料
            JSONObject levelLog = data.getJSONObject(event.getMember().getId());
            count = levelLog.getLong(COUNT);
            if (count == null)
                count = 0L;
        }

        JSONObject messageContent = new JSONObject()
                .put(COUNT, count + 1L) // Message
                ;
        data.put(event.getMember().getId(), messageContent);
        channelFileManager.saveFile();

    }
}

