package main.java.command;

import net.dv8tion.jda.api.Permission;
import net.dv8tion.jda.api.entities.Member;
import net.dv8tion.jda.api.events.interaction.SlashCommandEvent;
import net.dv8tion.jda.api.exceptions.PermissionException;

import static main.java.BotSetting.noPermissionERROR;
import static main.java.SlashCommandOption.USER_ID;
import static main.java.util.GuildUtil.guild;

public class UnBanCommand {

    public void onCommand(SlashCommandEvent event) {

        try {

            Member selfMember = guild.getSelfMember();

            if (!selfMember.hasPermission(Permission.BAN_MEMBERS)) {
                event.reply("我必須要有封禁權限才可以解除封鎖").setEphemeral(true).queue();
            } else if (!event.getMember().hasPermission(Permission.BAN_MEMBERS)) {
                event.reply(noPermissionERROR).setEphemeral(true).queue();
                return;
            }
            try {
                guild.unban(event.getOption(USER_ID).getAsString()).queue();
                event.reply("已成功解除封鎖").setEphemeral(true).queue();
            } catch (Exception ex) {
                if (ex instanceof PermissionException) {
                    event.reply("權限錯誤: " + ex.getMessage()).setEphemeral(true).queue();
                } else {
                    event.reply("未知的錯誤: " + ex.getClass().getSimpleName() + ">: " + ex.getMessage()).setEphemeral(true).queue();
                }
            }
        } catch (Exception ex) {
            event.reply("無法找到此成員").setEphemeral(true).queue();
        }
    }
}
