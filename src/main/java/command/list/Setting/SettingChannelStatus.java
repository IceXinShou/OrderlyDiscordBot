package main.java.command.list.Setting;

import main.java.Main;
import main.java.event.StatusListener;
import main.java.util.StringCalculate;
import main.java.util.file.GuildSettingHelper;
import net.dv8tion.jda.api.events.interaction.SlashCommandEvent;
import org.jetbrains.annotations.NotNull;
import org.json.JSONObject;

import java.util.List;
import java.util.Objects;

import static main.java.lang.LangKey.*;
import static main.java.util.EmbedCreator.createEmbed;
import static main.java.util.JsonKeys.*;

public record SettingChannelStatus(GuildSettingHelper settingHelper) {

    public void newCS(@NotNull SlashCommandEvent event, @NotNull StatusListener listener) {
        List<String> lang = Main.language.getGuildLang(event.getGuild().getId());
        String name = Objects.requireNonNull(event.getOption("channelname")).getAsString();
        String format = String.valueOf(Objects.requireNonNull(event.getOption("format")).getAsLong());
        if (Integer.parseInt(format) < 0 || Integer.parseInt(format) > 10)
            event.getHook().editOriginalEmbeds(createEmbed(lang.get(SETTINGCHANNELSTATUS_FAIL), 0xFF0000)).queue();
        else {
            format = "%." + format + "f";
            StringCalculate check = new StringCalculate();
            check.processes(name, format);
            if (check.haveError())
                event.getHook().editOriginalEmbeds(createEmbed(check.getError(), 0xFF0000)).queue();
            else {
                settingHelper.getSettingData(Objects.requireNonNull(event.getGuild()), CS_SETTING).put(Objects.requireNonNull(event.getOption("channel")).getAsGuildChannel().getId(), new JSONObject().put(CS_NAME, name).put(CS_FORMAT, format));
                settingHelper.getGuildSettingManager(event.getGuild().getId()).saveFile();
                listener.updateGuild(event.getGuild());
                event.getHook().editOriginalEmbeds(createEmbed(lang.get(SETTINGCHANNELSTATUS_SETTING_SUCCESS), 0x00FFFF)).queue();
            }
        }
    }

    public void removeCS(@NotNull SlashCommandEvent event) {
        List<String> lang = Main.language.getGuildLang(event.getGuild().getId());
        JSONObject data = settingHelper.getSettingData(Objects.requireNonNull(event.getGuild()), CS_SETTING);
        String channelID = Objects.requireNonNull(event.getOption("channel")).getAsGuildChannel().getId();

        data.remove(channelID);
        Objects.requireNonNull(event.getOption("channel")).getAsGuildChannel().delete().queue();
        event.getHook().editOriginalEmbeds(createEmbed(lang.get(SETTINGCHANNELSTATUS_REMOVE_SUCCESS), 0x00FFFF)).queue();
        settingHelper.getGuildSettingManager(event.getGuild().getId()).saveFile();

    }

}
