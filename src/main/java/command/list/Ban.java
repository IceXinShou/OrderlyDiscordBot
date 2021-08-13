package main.java.command.list;

import net.dv8tion.jda.api.entities.Member;
import net.dv8tion.jda.api.entities.User;
import net.dv8tion.jda.api.events.interaction.SlashCommandEvent;
import net.dv8tion.jda.api.interactions.commands.OptionMapping;
import org.jetbrains.annotations.NotNull;

import static main.java.BotSetting.botOwnerID;
import static main.java.util.EmbedCreator.createEmbed;
import static main.java.util.PermissionERROR.hasPermission;
import static main.java.util.SlashCommandOption.DAYS;
import static main.java.util.SlashCommandOption.USER_TAG;
import static net.dv8tion.jda.api.Permission.BAN_MEMBERS;

public class Ban {
    public void onCommand(@NotNull SlashCommandEvent event) {
        User user = event.getOption(USER_TAG).getAsUser();
        Member member = event.getGuild().retrieveMemberById(user.getId()).complete();

        if (hasPermission(BAN_MEMBERS, event, true))
            return;

        Member selfMember = event.getGuild().getSelfMember();
        if (!selfMember.hasPermission(BAN_MEMBERS)) {
            event.getHook().editOriginalEmbeds(createEmbed("機器人並沒有權限封禁成員", 0xFF0000)).queue();
            return;
        }

        if (user != null && !selfMember.canInteract(member)) {
            event.getHook().editOriginalEmbeds(createEmbed("此成員的力量大到讓我無法執行此動作", 0xFF0000)).queue();
            return;
        }

        if (user != null && botOwnerID.contains(member.getId())) {
            event.getHook().editOriginalEmbeds(createEmbed("此成員為機器人的開發者", 0xFF0000)).queue();
            return;
        }

        int delDays = 0;
        OptionMapping option = event.getOption(DAYS);
        if (option != null)
            delDays = (int) Math.max(0, Math.min(7, option.getAsLong()));
        event.getGuild().ban(user, delDays)
                .flatMap(v -> event.getHook().editOriginalEmbeds(createEmbed("封禁成員 " + user.getAsTag(), 0xffb1b3)))
                .queue();
    }
}
