package main.java.command.list.Setting;

import main.java.event.StatusListener;
import main.java.util.file.GuildSettingHelper;
import main.java.util.file.JsonFileManager;
import net.dv8tion.jda.api.entities.Guild;
import net.dv8tion.jda.api.events.interaction.SlashCommandEvent;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.json.JSONObject;

import static main.java.util.EmbedCreator.createEmbed;
import static main.java.util.JsonKeys.CS_SETTING;

public class SettingChannelStatus {

    public void newCS(@NotNull SlashCommandEvent event, GuildSettingHelper settingHelper, @NotNull StatusListener listener) {
        getSettingData(event.getGuild(), settingHelper).put(event.getOption("channel").getAsGuildChannel().getId(), event.getOption("channelname").getAsString());
        settingHelper.getGuildSettingManager(event.getGuild().getId()).saveFile();
        listener.updateGuild(event.getGuild());
        event.getHook().editOriginalEmbeds(createEmbed("設定成功", 0x00FFFF)).queue();
    }

    public void removeCS(@NotNull SlashCommandEvent event, GuildSettingHelper settingHelper) {
        JSONObject data = getSettingData(event.getGuild(), settingHelper);
        String channelID = event.getOption("channel").getAsGuildChannel().getId();

        data.remove(channelID);
        event.getOption("channel").getAsGuildChannel().delete().queue();
        event.getHook().editOriginalEmbeds(createEmbed("移除成功", 0x00FFFF)).queue();
        settingHelper.getGuildSettingManager(event.getGuild().getId()).saveFile();

    }

    private @Nullable JSONObject getSettingData(@NotNull Guild guild, @NotNull GuildSettingHelper settingHelper) {
        JsonFileManager fileManager = settingHelper.getGuildSettingManager(guild.getId());
        if (fileManager.data.has(CS_SETTING))
            return fileManager.data.getJSONObject(CS_SETTING);
        else {
            JSONObject data = new JSONObject();
            settingHelper.getGuildSettingManager(guild.getId()).data.put(CS_SETTING, data);
            return data;
        }
    }
}
