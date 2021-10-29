package main.command.list;

import main.Main;
import net.dv8tion.jda.api.entities.Member;
import net.dv8tion.jda.api.entities.User;
import net.dv8tion.jda.api.events.interaction.SlashCommandEvent;
import net.dv8tion.jda.api.interactions.commands.OptionMapping;

import java.util.List;

import static main.BotSetting.botOwnerID;
import static main.lang.LangKey.*;
import static main.util.EmbedCreator.createEmbed;
import static main.util.PermissionERROR.hasPermission;
import static main.util.SlashCommandOption.DAYS;
import static main.util.SlashCommandOption.USER_TAG;
import static net.dv8tion.jda.api.Permission.BAN_MEMBERS;

public class Ban {
    public void onCommand(SlashCommandEvent event) {
        if (!hasPermission(BAN_MEMBERS, event, true))
            return;
        List<String> lang = Main.language.getGuildLang(event.getGuild().getId());
        User user = event.getOption(USER_TAG).getAsUser();
//        Member member = event.getGuild().retrieveMemberById(user.getId()).complete();

        Member selfMember = event.getGuild().getSelfMember();
        if (!selfMember.hasPermission(BAN_MEMBERS)) {
            event.getHook().editOriginalEmbeds(createEmbed(lang.get(BAN_NO_PERMISSION), 0xFF0000)).queue();
            return;
        }

        if (botOwnerID.contains(user.getId())) {
            event.getHook().editOriginalEmbeds(createEmbed(lang.get(BAN_DEVELOPER), 0xFF0000)).queue();
            return;
        }

        int delDays = 0;
        OptionMapping option = event.getOption(DAYS);
        if (option != null)
            delDays = (int) Math.max(0, Math.min(7, option.getAsLong()));
        event.getGuild().ban(user, delDays)
                .flatMap(v -> event.getHook().editOriginalEmbeds(createEmbed(lang.get(BAN_SUCCESS) + ' ' + user.getAsTag(), 0xffb1b3)))
                .queue();
    }
}
