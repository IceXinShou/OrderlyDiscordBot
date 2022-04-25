package com.ice.main.command.list;

import com.ice.main.Main;
import net.dv8tion.jda.api.Permission;
import net.dv8tion.jda.api.entities.MessageEmbed;
import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent;

import java.time.OffsetDateTime;
import java.util.ArrayList;
import java.util.List;

import static com.ice.main.lang.LangKey.POLL_MEMBER_POLL;
import static com.ice.main.lang.LangKey.POLL_SUCCESS;
import static com.ice.main.util.EmbedCreator.createEmbed;
import static com.ice.main.util.EmojiUtil.dotEmojis;
import static com.ice.main.util.PermissionERROR.permissionCheck;
import static com.ice.main.util.SlashCommandOption.QUESTION;
import static com.ice.main.util.Tag.getMemberName;
import static com.ice.main.util.Tag.getMemberNick;

public class Poll {

    public void onCommand(SlashCommandInteractionEvent event) {
        List<String> lang = Main.language.getGuildLang(event.getGuild().getId());
        if (!permissionCheck(Permission.MANAGE_CHANNEL, event, true))
            return;

        List<MessageEmbed.Field> fields = new ArrayList<>();
        for (int i = 1; i < event.getOptions().size(); i++)
            fields.add(new MessageEmbed.Field(dotEmojis[i - 1].getAsMention() + event.getOptions().get(i).getAsString(), "", false));

        event.getChannel().sendMessageEmbeds(createEmbed(
                event.getOption(QUESTION).getAsString(), null,
                lang.get(POLL_MEMBER_POLL),
                getMemberNick(event), event.getUser().getAvatarUrl(),
                fields,
                OffsetDateTime.now(), 0x87E5CF
        )).queue(m -> {
            for (int i = 0; i < event.getOptions().size() - 1; i++)
                m.addReaction(dotEmojis[i]).queue();
        });

        event.getHook().editOriginalEmbeds(createEmbed(lang.get(POLL_SUCCESS), 0x9740b9)).queue();
    }
}