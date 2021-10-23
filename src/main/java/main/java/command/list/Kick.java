package main.java.command.list;

import main.java.Main;
import net.dv8tion.jda.api.Permission;
import net.dv8tion.jda.api.entities.Member;
import net.dv8tion.jda.api.events.interaction.SlashCommandEvent;
import net.dv8tion.jda.api.exceptions.PermissionException;

import java.util.List;

import static main.java.BotSetting.botOwnerID;
import static main.java.lang.LangKey.*;
import static main.java.util.EmbedCreator.createEmbed;
import static main.java.util.PermissionERROR.hasPermission;
import static main.java.util.SlashCommandOption.USER_TAG;
import static main.java.util.Tag.getMemberName;

public class Kick {
    @SuppressWarnings("ALL")
    public void onCommand(SlashCommandEvent event) {
        List<String> lang = Main.language.getGuildLang(event.getGuild().getId());
        Member selfMember = event.getGuild().getSelfMember();

        if (!selfMember.hasPermission(Permission.KICK_MEMBERS)) {
            event.getHook().editOriginalEmbeds(createEmbed(lang.get(KICK_NO_PERMISSION), 0xFF0000)).queue();
            return;
        } else if (!hasPermission(Permission.KICK_MEMBERS, event, true))
            return;

        Member member = event.getOption(USER_TAG).getAsMember();
        if (member != null && !selfMember.canInteract(member)) {
            event.getHook().editOriginalEmbeds(createEmbed(lang.get(KICK_PERMISSION_DENIED), 0xFF0000)).queue();
            return;
        }

        if (member != null && botOwnerID.contains(member.getId())) {
            event.getHook().editOriginalEmbeds(createEmbed(lang.get(KICK_DEVELOPER), 0xFF0000)).queue();
            return;
        }

        String nickname = getMemberName(event);
        event.getGuild().kick(member).queue(
                success -> event.getHook().editOriginalEmbeds(createEmbed(lang.get(KICK_SUCCESS) + ' ' + nickname, 0xffd2c5)).queue(),
                error -> {
                    if (error instanceof PermissionException) {
                        event.getHook().editOriginalEmbeds(
                                createEmbed(String.format("%s %s: %s", lang.get(KICK_PERMISSION_ERROR),
                                        member.getEffectiveName(), error.getMessage()), 0xFF0000)).queue();
                    } else {
                        event.getHook().editOriginalEmbeds(createEmbed(
                                String.format("%s %s: <%s>: %s",
                                        lang.get(KICK_UNKNOWN_ERROR),
                                        member.getEffectiveName(),
                                        error.getClass().getSimpleName(),
                                        error.getMessage()), 0xFF0000)).queue();
                    }
                });
    }
}