package main.java.command;

import net.dv8tion.jda.api.Permission;
import net.dv8tion.jda.api.entities.Member;
import net.dv8tion.jda.api.events.interaction.SlashCommandEvent;
import net.dv8tion.jda.api.exceptions.PermissionException;

import static main.java.BotSetting.botOwnerID;
import static main.java.BotSetting.noPermissionERROR;
import static main.java.SlashCommandOption.USER_TAG;
import static main.java.util.EmbedUtil.createEmbed;
import static main.java.util.GuildUtil.guild;

public class KickCommand {

    public void onCommand(SlashCommandEvent event) {
        Member selfMember = event.getGuild().getSelfMember();

        if (!selfMember.hasPermission(Permission.KICK_MEMBERS)) {
            event.replyEmbeds(createEmbed("我必須要有踢出權限才可以踢出成員", 0xFF0000)).setEphemeral(true).queue();
            return;
        } else if (!event.getMember().hasPermission(Permission.KICK_MEMBERS)) {
            event.replyEmbeds(createEmbed(noPermissionERROR, 0xFF0000)).setEphemeral(true).queue();
            return;
        }
        Member member = event.getOption(USER_TAG).getAsMember();
        if (member != null && !selfMember.canInteract(member)) {
            event.replyEmbeds(createEmbed("無法踢出此成員, 他的權限太高了", 0xFF0000)).setEphemeral(true).queue();
            return;
        }

        if (member != null && botOwnerID.contains(member.getId())) {
            event.replyEmbeds(createEmbed("此成員為機器人的開發者", 0xFF0000)).setEphemeral(true).queue();
            return;
        }


        event.getGuild().kick(member).queue(
                success -> event.replyEmbeds(createEmbed("已踢出", 0xffd2c5)).setEphemeral(true).queue(),
                error -> {
                    if (error instanceof PermissionException) {
                        event.replyEmbeds(
                                createEmbed("權限錯誤" + member.getEffectiveName() + ": " + error.getMessage(), 0xFF0000)).setEphemeral(true).queue();
                    } else {
                        event.replyEmbeds(
                                createEmbed("未知的錯誤" + member.getEffectiveName() + ": <" + error.getClass().getSimpleName() + ">: " + error.getMessage(), 0xFF0000)).setEphemeral(true).queue();
                    }
                });
    }
}
