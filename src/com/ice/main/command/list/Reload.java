package com.ice.main.command.list;

import com.ice.main.Main;
import net.dv8tion.jda.api.events.interaction.SlashCommandEvent;

import java.util.List;

import static com.ice.main.Main.setting;
import static com.ice.main.lang.LangKey.RELOAD_SUCCESS;
import static com.ice.main.util.EmbedCreator.createEmbed;
import static com.ice.main.util.PermissionERROR.permissionCheck;
import static net.dv8tion.jda.api.Permission.ADMINISTRATOR;

public class Reload {

    public boolean onCommand(SlashCommandEvent event) {
        List<String> lang = Main.language.getGuildLang(event.getGuild().getId());
        if (!permissionCheck(ADMINISTRATOR, event, true))
            return false;
        else {
            setting.reloadConfig();
            event.getHook().editOriginalEmbeds(createEmbed(lang.get(RELOAD_SUCCESS), 0x00FFFF)).queue();
            return true;
        }
    }
}