package com.ice.main.command.list.setting;

import com.ice.main.Main;
import com.ice.main.event.StatusListener;
import com.ice.main.util.StringCalculate;
import com.ice.main.util.file.GuildSettingHelper;
import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent;
import org.json.JSONObject;

import java.util.List;

import static com.ice.main.lang.LangKey.*;
import static com.ice.main.util.EmbedCreator.createEmbed;
import static com.ice.main.util.JsonKeys.*;

public record SettingChannelStatus(GuildSettingHelper settingHelper) {

    public void newCS(SlashCommandInteractionEvent event, StatusListener listener) {
        List<String> lang = Main.language.getGuildLang(event.getGuild().getId());
        String name = event.getOption("channelname").getAsString();
        int formatInt = Integer.parseInt(String.valueOf(event.getOption("format").getAsLong()));
        if (formatInt < 0 || formatInt > 10)
            event.getHook().editOriginalEmbeds(createEmbed(lang.get(SETTINGCHANNELSTATUS_FAIL), 0xFF0000)).queue();
        else {
            String format = "%." + formatInt + "f";
            StringCalculate check = listener.testInput(event.getGuild(), event.getOption("channel").getAsGuildChannel().getId(), name, format);

            if (check.haveError())
                event.getHook().editOriginalEmbeds(createEmbed(check.getError(), 0xFF0000)).queue();
            else {
                settingHelper.getSettingData(event.getGuild(), CS_SETTING)
                        .put(event.getOption("channel").getAsGuildChannel().getId(), new JSONObject()
                                .put(CS_NAME, name)
                                .put(CS_FORMAT, format));

                listener.updateGuild(event.getGuild());
                settingHelper.getGuildSettingManager(event.getGuild().getId()).saveFile();
                event.getHook().editOriginalEmbeds(createEmbed(lang.get(SETTINGCHANNELSTATUS_SETTING_SUCCESS), 0x00FFFF)).queue();
            }
        }
    }

    public void removeCS(SlashCommandInteractionEvent event) {
        List<String> lang = Main.language.getGuildLang(event.getGuild().getId());
        JSONObject data = settingHelper.getSettingData(event.getGuild(), CS_SETTING);
        String channelID = event.getOption("channel").getAsGuildChannel().getId();

        data.remove(channelID);
        event.getOption("channel").getAsGuildChannel().delete().queue();
        event.getHook().editOriginalEmbeds(createEmbed(lang.get(SETTINGCHANNELSTATUS_REMOVE_SUCCESS), 0x00FFFF)).queue();
        settingHelper.getGuildSettingManager(event.getGuild().getId()).saveFile();
    }
}