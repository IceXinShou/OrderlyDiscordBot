package main.java;

import main.java.util.file.GuildSettingHelper;
import net.dv8tion.jda.api.events.interaction.ButtonClickEvent;
import org.json.JSONObject;

import java.util.List;

import static main.java.util.JsonKeys.*;

record Economy(GuildSettingHelper settingHelper) {

    void onDailyCheck(ButtonClickEvent event, String[] args) {
        List<String> lang = Main.language.getGuildLang(event.getGuild().getId());
        if (!args[0].equals("Economy"))
            return;
        JSONObject data = settingHelper.getSettingData(event.getGuild(), ECONOMY_SETTING);
        if (!data.getBoolean(ECONOMY_ENABLE))
            return;

        JSONObject object = new JSONObject();
        object.put(event.getUser().getId(), data.getJSONObject(ECONOMY_BAL).getLong(ECONOMY_BAL) + 1);
        data.put(ECONOMY_BAL, object);
    }
}

