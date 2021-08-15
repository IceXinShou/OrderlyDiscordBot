package main.java.command.list.Setting;

import main.java.event.StatusListener;
import main.java.util.StringCalculate;
import main.java.util.file.GuildSettingHelper;
import main.java.util.file.JsonFileManager;
import net.dv8tion.jda.api.entities.Guild;
import net.dv8tion.jda.api.events.interaction.SlashCommandEvent;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.json.JSONObject;

import static main.java.util.EmbedCreator.createEmbed;
import static main.java.util.JsonKeys.*;

public class SettingChannelStatus {

    public void newCS(@NotNull SlashCommandEvent event, GuildSettingHelper settingHelper, @NotNull StatusListener listener) {
        String name = event.getOption("channelname").getAsString();
        String format = String.valueOf(event.getOption("format").getAsLong());
        if (Integer.parseInt(format) < 0 || Integer.parseInt(format) > 10)
            event.getHook().editOriginalEmbeds(createEmbed("格式化位數錯誤", 0xFF0000)).queue();
        else {
            format = "%." + format + "f";
            StringCalculate check = new StringCalculate();
            check.processes(name, format);
            if (check.haveError())
                event.getHook().editOriginalEmbeds(createEmbed(check.getError(), 0xFF0000)).queue();
            else {
                getSettingData(event.getGuild(), settingHelper).put(event.getOption("channel").getAsGuildChannel().getId(), new JSONObject().put(CS_NAME, name).put(CS_FORMAT, format));
                settingHelper.getGuildSettingManager(event.getGuild().getId()).saveFile();
                listener.updateGuild(event.getGuild());
                event.getHook().editOriginalEmbeds(createEmbed("設定成功", 0x00FFFF)).queue();
            }
        }
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
