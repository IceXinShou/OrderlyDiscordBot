package main.java.command.list;

import net.dv8tion.jda.api.Permission;
import net.dv8tion.jda.api.entities.Member;
import net.dv8tion.jda.api.events.interaction.SlashCommandEvent;
import net.dv8tion.jda.api.exceptions.PermissionException;

import static main.java.util.EmbedCreator.createEmbed;
import static main.java.util.PermissionERROR.hasPermission;
import static main.java.util.SlashCommandOption.USER_ID;

public class UnBan {

    public void onCommand(SlashCommandEvent event) {

        try {

            Member selfMember = event.getGuild().getSelfMember();

            if (!selfMember.hasPermission(Permission.BAN_MEMBERS)) {
                event.getHook().editOriginalEmbeds(createEmbed("我必須要有封禁權限才可以解除封鎖", 0xFF0000)).queue();
            } else if (!hasPermission(Permission.BAN_MEMBERS, event, true))
                return;
            try {
                event.getGuild().unban(event.getOption(USER_ID).getAsString()).queue();
                event.getHook().editOriginalEmbeds(createEmbed("已成功解除封鎖", 0xc5ffd2)).queue();
            } catch (Exception ex) {
                if (ex instanceof PermissionException) {
                    event.getHook().editOriginalEmbeds(createEmbed("權限錯誤: " + ex.getMessage(), 0xFF0000)).queue();
                } else {
                    event.getHook().editOriginalEmbeds(createEmbed("未知的錯誤: " + ex.getClass().getSimpleName() + ">: " + ex.getMessage(), 0xFF0000)).queue();
                }
            }
        } catch (Exception ex) {
            event.getHook().editOriginalEmbeds(createEmbed("無法找到此成員", 0xFF0000)).queue();
        }
    }
}
