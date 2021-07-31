package main.java.command;

import net.dv8tion.jda.api.Permission;
import net.dv8tion.jda.api.entities.Member;
import net.dv8tion.jda.api.events.interaction.SlashCommandEvent;
import net.dv8tion.jda.api.exceptions.PermissionException;
import org.jetbrains.annotations.NotNull;

import static main.java.BotSetting.noPermissionERROR;
import static main.java.SlashCommandOption.USER_TAG;
import static main.java.util.Funtions.createEmbed;
import static main.java.util.Funtions.isBotOwner;

public class Kick {

    public void onCommand(@NotNull SlashCommandEvent event) {
        Member selfMember = event.getGuild().getSelfMember();

        if (!selfMember.hasPermission(Permission.KICK_MEMBERS)) {
            event.getHook().editOriginalEmbeds(createEmbed("我必須要有踢出權限才可以踢出成員", 0xFF0000)).queue();
            return;
        } else if (!event.getMember().hasPermission(Permission.KICK_MEMBERS)) {
            event.getHook().editOriginalEmbeds(createEmbed(noPermissionERROR + "`(KICK_MEMBERS)`", 0xFF0000)).queue();
            return;
        }
        Member member = event.getOption(USER_TAG).getAsMember();
        if (member != null && !selfMember.canInteract(member)) {
            event.getHook().editOriginalEmbeds(createEmbed("無法踢出此成員, 他的權限太高了", 0xFF0000)).queue();
            return;
        }

        if (member != null && isBotOwner(event)) {
            event.getHook().editOriginalEmbeds(createEmbed("此成員為機器人的開發者", 0xFF0000)).queue();
            return;
        }


        event.getGuild().kick(member).queue(
                success -> event.getHook().editOriginalEmbeds(createEmbed("已踢出", 0xffd2c5)).queue(),
                error -> {
                    if (error instanceof PermissionException) {
                        event.getHook().editOriginalEmbeds(
                                createEmbed("權限錯誤" + member.getEffectiveName() + ": " + error.getMessage(), 0xFF0000)).queue();
                    } else {
                        event.getHook().editOriginalEmbeds(
                                createEmbed("未知的錯誤" + member.getEffectiveName() + ": <" + error.getClass().getSimpleName() + ">: " + error.getMessage(), 0xFF0000)).queue();
                    }
                });
    }
}
