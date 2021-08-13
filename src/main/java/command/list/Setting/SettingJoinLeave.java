package main.java.command.list.Setting;

import main.java.util.file.GuildSettingHelper;
import main.java.util.file.JsonFileManager;
import net.dv8tion.jda.api.entities.Guild;
import net.dv8tion.jda.api.events.interaction.SlashCommandEvent;
import org.jetbrains.annotations.NotNull;
import org.json.JSONObject;

import static main.java.util.EmbedCreator.createEmbed;
import static main.java.util.JsonKeys.*;

public class SettingJoinLeave {

    // {channelID: {jm:"",lm:""}}
    public void newJoin(@NotNull SlashCommandEvent event, GuildSettingHelper settingHelper) {
        JSONObject setting = getOrDefault(getSettingData(event.getGuild(), settingHelper), event.getOption("channel").getAsString());
        setting.put(JL_JOIN_MESSAGE, event.getOption("message").getAsString());
        settingHelper.getGuildSettingManager(event.getGuild().getId()).saveFile();
        event.getHook().editOriginalEmbeds(createEmbed("設定成功", 0x00FFFF)).queue();
    }

    public void newLeave(@NotNull SlashCommandEvent event, GuildSettingHelper settingHelper) {
        JSONObject setting = getOrDefault(getSettingData(event.getGuild(), settingHelper), event.getOption("channel").getAsString());
        setting.put(JL_LEAVE_MESSAGE, event.getOption("message").getAsString());
        settingHelper.getGuildSettingManager(event.getGuild().getId()).saveFile();
        event.getHook().editOriginalEmbeds(createEmbed("設定成功", 0x00FFFF)).queue();
    }

    public void removeJoin(@NotNull SlashCommandEvent event, GuildSettingHelper settingHelper) {

        if (!getSettingData(event.getGuild(), settingHelper).has(event.getOption("channel").getAsGuildChannel().getId())) {
            event.getHook().editOriginalEmbeds(createEmbed("此頻道無被設定紀錄", 0xFF0000)).queue();
            return;
        }
        if (!getSettingData(event.getGuild(), settingHelper).getJSONObject(event.getOption("channel").getAsGuildChannel().getId()).has(JL_JOIN_MESSAGE)) {
            event.getHook().editOriginalEmbeds(createEmbed("此頻道無被設定紀錄", 0xFF0000)).queue();
            return;
        }
        getSettingData(event.getGuild(), settingHelper).getJSONObject(event.getOption("channel").getAsGuildChannel().getId()).remove(JL_JOIN_MESSAGE);
        settingHelper.getGuildSettingManager(event.getGuild().getId()).saveFile();
        event.getHook().editOriginalEmbeds(createEmbed("移除成功", 0x00FFFF)).queue();
    }

    public void removeLeave(@NotNull SlashCommandEvent event, GuildSettingHelper settingHelper) {
        if (!getSettingData(event.getGuild(), settingHelper).has(event.getOption("channel").getAsGuildChannel().getId())) {
            event.getHook().editOriginalEmbeds(createEmbed("此頻道無被設定紀錄", 0xFF0000)).queue();
            return;
        }

        if (!getSettingData(event.getGuild(), settingHelper).getJSONObject(event.getOption("channel").getAsGuildChannel().getId()).has(JL_LEAVE_MESSAGE)) {
            event.getHook().editOriginalEmbeds(createEmbed("此頻道無被設定紀錄", 0xFF0000)).queue();
            return;
        }

        getSettingData(event.getGuild(), settingHelper).getJSONObject(event.getOption("channel").getAsGuildChannel().getId()).remove(JL_LEAVE_MESSAGE);
        settingHelper.getGuildSettingManager(event.getGuild().getId()).saveFile();
        event.getHook().editOriginalEmbeds(createEmbed("移除成功", 0x00FFFF)).queue();
    }

    private JSONObject getSettingData(@NotNull Guild guild, @NotNull GuildSettingHelper settingHelper) {
        JsonFileManager fileManager = settingHelper.getGuildSettingManager(guild.getId());
        if (fileManager.data.has(JL_SETTING))
            return fileManager.data.getJSONObject(JL_SETTING);
        else {
            JSONObject data = new JSONObject();
            settingHelper.getGuildSettingManager(guild.getId()).data.put(JL_SETTING, data);
            return data;
        }
    }

    private JSONObject getOrDefault(@NotNull JSONObject input, String key) {
        if (input.has(key))
            return input.getJSONObject(key);
        else {
            JSONObject data = new JSONObject();
            input.put(key, data);
            return data;
        }
    }
}
