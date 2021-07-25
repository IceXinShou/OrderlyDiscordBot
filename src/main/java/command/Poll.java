package main.java.command;

import net.dv8tion.jda.api.Permission;
import net.dv8tion.jda.api.entities.MessageEmbed;
import net.dv8tion.jda.api.events.interaction.SlashCommandEvent;

import java.time.OffsetDateTime;
import java.util.ArrayList;
import java.util.List;

import static main.java.BotSetting.noPermissionERROR;
import static main.java.SlashCommandOption.QUESTION;
import static main.java.util.EmojiUtil.dotEmojis;
import static main.java.util.Funtions.createEmbed;

public class Poll {

    public void onCommand(SlashCommandEvent event) {

        if (!event.getMember().hasPermission(Permission.MESSAGE_MANAGE)) {
            event.getHook().editOriginalEmbeds(createEmbed(noPermissionERROR, 0xFF0000)).queue();
            return;
        }

        List<MessageEmbed.Field> fields = new ArrayList<>();
        for (int i = 1; i < event.getOptions().size(); i++) {
            fields.add(new MessageEmbed.Field(dotEmojis[i - 1].getAsMention() + event.getOptions().get(i).getAsString(), "", false));
        }
        event.getChannel().sendMessageEmbeds(createEmbed(
                event.getOption(QUESTION).getAsString(), null,
                "成員投票",
                event.getMember().getNickname() == null ? event.getUser().getAsTag() : event.getMember().getNickname(), event.getUser().getAvatarUrl(),
                fields,
                OffsetDateTime.now(), 0x87E5CF
        )).queue(m -> {
            for (int i = 0; i < event.getOptions().size() - 1; i++) {
                m.addReaction(dotEmojis[i]).queue();
            }
        });

        event.getHook().editOriginalEmbeds(createEmbed("創建成功", 0x9740b9)).queue();
    }
}