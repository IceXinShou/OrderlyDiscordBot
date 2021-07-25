package main.java.command;

import net.dv8tion.jda.api.Permission;
import net.dv8tion.jda.api.entities.Member;
import net.dv8tion.jda.api.entities.User;
import net.dv8tion.jda.api.events.interaction.SlashCommandEvent;
import net.dv8tion.jda.api.interactions.commands.OptionMapping;

import static main.java.BotSetting.noPermissionERROR;
import static main.java.SlashCommandOption.DAYS;
import static main.java.SlashCommandOption.USER_TAG;
import static main.java.util.Funtions.createEmbed;
import static main.java.util.Funtions.isBotOwner;

public class Ban {
    public void onCommand(SlashCommandEvent event) {
        User user = event.getOption(USER_TAG).getAsUser();
        Member member = event.getGuild().retrieveMemberById(user.getId()).complete();

        if (!event.getMember().hasPermission(Permission.BAN_MEMBERS)) {
            event.replyEmbeds(createEmbed(noPermissionERROR, 0xFF0000)).setEphemeral(true).queue();
            return;
        }

        Member selfMember = event.getGuild().getSelfMember();
        if (!selfMember.hasPermission(Permission.BAN_MEMBERS)) {
            event.replyEmbeds(createEmbed("機器人並沒有權限封禁成員", 0xFF0000)).setEphemeral(true).queue();
            return;
        }

        if (user != null && !selfMember.canInteract(member)) {
            event.replyEmbeds(createEmbed("此成員的力量大到讓我無法執行此動作", 0xFF0000)).setEphemeral(true).queue();
            return;
        }

        if (user != null && isBotOwner(event)) {
            event.replyEmbeds(createEmbed("此成員為機器人的開發者", 0xFF0000)).setEphemeral(true).queue();
            return;
        }

        int delDays = 0;
        OptionMapping option = event.getOption(DAYS);
        if (option != null)
            delDays = (int) Math.max(0, Math.min(7, option.getAsLong()));
        event.getGuild().ban(user, delDays)
                .flatMap(v -> event.replyEmbeds(createEmbed("封禁成員 " + user.getAsTag(), 0xffb1b3)).setEphemeral(true))
                .queue();
    }
}
