package main.java.command;

import net.dv8tion.jda.api.Permission;
import net.dv8tion.jda.api.entities.Member;
import net.dv8tion.jda.api.events.interaction.SlashCommandEvent;
import net.dv8tion.jda.api.exceptions.PermissionException;

import static main.java.BotSetting.noPermissionERROR;
import static main.java.SlashCommandOption.USER_TAG;
import static main.java.util.GuildUtil.guild;

public class KickCommand {

    public void onCommand(SlashCommandEvent event) {
        Member selfMember = guild.getSelfMember();

        if (!selfMember.hasPermission(Permission.KICK_MEMBERS)) {
            event.reply("我必須要有踢出權限才可以踢出成員").setEphemeral(true).queue();
            return;
        } else if (!event.getMember().hasPermission(Permission.KICK_MEMBERS)) {
            event.reply(noPermissionERROR).setEphemeral(true).queue();
            return;
        }
        Member member = event.getOption(USER_TAG).getAsMember();
        if (!selfMember.canInteract(member)) {
            event.reply("無法踢出此成員, 他的權限太高了").setEphemeral(true).queue();
            return;
        }

        guild.kick(member).queue(
                success -> event.reply("已踢出").setEphemeral(true).queue(),
                error -> {
                    if (error instanceof PermissionException) {
                        event.reply(
                                "權限錯誤" + member.getEffectiveName() + ": " + error.getMessage()).setEphemeral(true).queue();
                    } else {
                        event.reply(
                                "未知的錯誤" + member.getEffectiveName() + ": <" + error.getClass().getSimpleName() + ">: " + error.getMessage()).setEphemeral(true).queue();
                    }
                });
    }
}
