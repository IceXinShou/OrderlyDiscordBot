package main.command.list;

import main.Main;
import net.dv8tion.jda.api.Permission;
import net.dv8tion.jda.api.entities.Member;
import net.dv8tion.jda.api.events.interaction.SlashCommandEvent;
import net.dv8tion.jda.api.exceptions.PermissionException;

import java.util.List;

import static main.lang.LangKey.*;
import static main.util.EmbedCreator.createEmbed;
import static main.util.PermissionERROR.hasPermission;
import static main.util.SlashCommandOption.USER_ID;

public class UnBan {

    public void onCommand(SlashCommandEvent event) {
        List<String> lang = Main.language.getGuildLang(event.getGuild().getId());
        try {
            Member selfMember = event.getGuild().getSelfMember();

            if (!selfMember.hasPermission(Permission.BAN_MEMBERS))
                event.getHook().editOriginalEmbeds(createEmbed(lang.get(UNBAN_NO_PERMISSION), 0xFF0000)).queue();
            else if (!hasPermission(Permission.BAN_MEMBERS, event, true))
                return;
            try {
                event.getGuild().unban(event.getOption(USER_ID).getAsString()).queue();
                event.getHook().editOriginalEmbeds(createEmbed(lang.get(UNBAN_SUCCESS), 0xc5ffd2)).queue();
            } catch (Exception ex) {
                if (ex instanceof PermissionException)
                    event.getHook().editOriginalEmbeds(createEmbed(lang.get(UNBAN_PERMISSION_ERROR) + ex.getMessage(), 0xFF0000)).queue();
                else
                    event.getHook().editOriginalEmbeds(createEmbed(lang.get(UNBAN_UNKNOWN_ERROR) + ex.getClass().getSimpleName() + ">: " + ex.getMessage(), 0xFF0000)).queue();

            }
        } catch (Exception ex) {
            event.getHook().editOriginalEmbeds(createEmbed(lang.get(UNBAN_CAN_NOTE_FOUND_MEMBER), 0xFF0000)).queue();
        }
    }
}
