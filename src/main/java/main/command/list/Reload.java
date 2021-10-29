package main.command.list;

import main.Main;
import net.dv8tion.jda.api.events.interaction.SlashCommandEvent;

import java.util.List;

import static main.Main.setting;
import static main.lang.LangKey.RELOAD_SUCCESS;
import static main.util.EmbedCreator.createEmbed;
import static main.util.PermissionERROR.hasPermission;
import static net.dv8tion.jda.api.Permission.ADMINISTRATOR;

public class Reload {

    public boolean onCommand(SlashCommandEvent event) {
        List<String> lang = Main.language.getGuildLang(event.getGuild().getId());
        if (!hasPermission(ADMINISTRATOR, event, true))
            return false;
        else {
            setting.reloadConfig();
            event.getHook().editOriginalEmbeds(createEmbed(lang.get(RELOAD_SUCCESS), 0x00FFFF)).queue();
            return true;
        }
    }
}