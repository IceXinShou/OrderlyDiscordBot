package main.java.command.list.Setting;

import main.java.Main;
import main.java.event.StatusListener;
import main.java.util.StringCalculate;
import main.java.util.file.GuildSettingHelper;
import net.dv8tion.jda.api.events.interaction.SlashCommandEvent;
import org.json.JSONObject;

import java.util.List;

import static main.java.lang.LangKey.*;
import static main.java.util.EmbedCreator.createEmbed;
import static main.java.util.JsonKeys.*;

public record SettingChannelStatus(GuildSettingHelper settingHelper) {

    public void newCS(SlashCommandEvent event, StatusListener listener) {
        List<String> lang = Main.language.getGuildLang(event.getGuild().getId());
        String name = event.getOption("channelname").getAsString();
        int formatInt = Integer.parseInt(String.valueOf(event.getOption("format").getAsLong()));
        if (formatInt < 0 || formatInt > 10)
            event.getHook().editOriginalEmbeds(createEmbed(lang.get(SETTINGCHANNELSTATUS_FAIL), 0xFF0000)).queue();
        else {
            String format = "%." + formatInt + "f";
            listener.updateGuild(event.getGuild());
            StringCalculate check = new StringCalculate();
            check.processes(listener.replace(event.getGuild().getId(), name), format);
            if (check.haveError())
                event.getHook().editOriginalEmbeds(createEmbed(check.getError(), 0xFF0000)).queue();
            else {
                settingHelper.getSettingData(event.getGuild(), CS_SETTING).put(event.getOption("channel").getAsGuildChannel().getId(), new JSONObject().put(CS_NAME, name).put(CS_FORMAT, format));
                settingHelper.getGuildSettingManager(event.getGuild().getId()).saveFile();
                listener.updateGuild(event.getGuild(), true);
                event.getHook().editOriginalEmbeds(createEmbed(lang.get(SETTINGCHANNELSTATUS_SETTING_SUCCESS), 0x00FFFF)).queue();
            }
        }
    }

    public void removeCS(SlashCommandEvent event) {
        List<String> lang = Main.language.getGuildLang(event.getGuild().getId());
        JSONObject data = settingHelper.getSettingData(event.getGuild(), CS_SETTING);
        String channelID = event.getOption("channel").getAsGuildChannel().getId();

        data.remove(channelID);
        event.getOption("channel").getAsGuildChannel().delete().queue();
        event.getHook().editOriginalEmbeds(createEmbed(lang.get(SETTINGCHANNELSTATUS_REMOVE_SUCCESS), 0x00FFFF)).queue();
        settingHelper.getGuildSettingManager(event.getGuild().getId()).saveFile();

    }

}
