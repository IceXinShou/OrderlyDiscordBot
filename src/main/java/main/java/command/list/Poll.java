package main.java.command.list;

import main.java.Main;
import net.dv8tion.jda.api.Permission;
import net.dv8tion.jda.api.entities.MessageEmbed;
import net.dv8tion.jda.api.events.interaction.SlashCommandEvent;

import java.time.OffsetDateTime;
import java.util.ArrayList;
import java.util.List;

import static main.java.lang.LangKey.POLL_MEMBER_POLL;
import static main.java.lang.LangKey.POLL_SUCCESS;
import static main.java.util.EmbedCreator.createEmbed;
import static main.java.util.EmojiUtil.dotEmojis;
import static main.java.util.PermissionERROR.hasPermission;
import static main.java.util.SlashCommandOption.QUESTION;
import static main.java.util.Tag.getMemberName;

public class Poll {

    public void onCommand(SlashCommandEvent event) {
        List<String> lang = Main.language.getGuildLang(event.getGuild().getId());
        if (!hasPermission(Permission.MANAGE_CHANNEL, event, true))
            return;

        List<MessageEmbed.Field> fields = new ArrayList<>();
        for (int i = 1; i < event.getOptions().size(); i++)
            fields.add(new MessageEmbed.Field(dotEmojis[i - 1].getAsMention() + event.getOptions().get(i).getAsString(), "", false));

        event.getChannel().sendMessageEmbeds(createEmbed(
                event.getOption(QUESTION).getAsString(), null,
                lang.get(POLL_MEMBER_POLL),
                getMemberName(event), event.getUser().getAvatarUrl(),
                fields,
                OffsetDateTime.now(), 0x87E5CF
        )).queue(m -> {
            for (int i = 0; i < event.getOptions().size() - 1; i++)
                m.addReaction(dotEmojis[i]).queue();
        });

        event.getHook().editOriginalEmbeds(createEmbed(lang.get(POLL_SUCCESS), 0x9740b9)).queue();
    }
}