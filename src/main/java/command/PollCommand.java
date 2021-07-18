package main.java.command;

import net.dv8tion.jda.api.Permission;
import net.dv8tion.jda.api.entities.Emote;
import net.dv8tion.jda.api.entities.MessageEmbed;
import net.dv8tion.jda.api.events.interaction.SlashCommandEvent;

import java.lang.reflect.Field;
import java.time.OffsetDateTime;
import java.util.ArrayList;
import java.util.List;

import static main.java.BotSetting.noPermissionERROR;
import static main.java.Main.emoji;
import static main.java.SlashCommandOption.QUESTION;
import static main.java.util.EmbedUtil.createEmbed;

public class PollCommand {

    public void onCommand(SlashCommandEvent event) {

        List<Emote> list = new ArrayList();
        if (!event.getMember().hasPermission(Permission.MESSAGE_MANAGE)) {
            event.replyEmbeds(createEmbed(noPermissionERROR, 0xFF0000)).setEphemeral(true).queue();
            return;
        }

        List<MessageEmbed.Field> fields = new ArrayList<>();
        for (int i = 0; i < event.getOptions().size(); i++) {
            try {
                Field field = emoji.getClass().getField("dot" + i);
                list.add((Emote) field.get(emoji));
            } catch (NoSuchFieldException | IllegalAccessException e) {
                continue;
            }

            fields.add(new MessageEmbed.Field(list.get(i - 1).getAsMention() + "選項 " + i + " : ", event.getOptions().get(i).getAsString(), false));
        }
        event.getChannel().sendMessage(createEmbed(
                event.getOption(QUESTION).getAsString(), null,
                "成員投票",
                event.getMember().getNickname() == null ? event.getUser().getAsTag() : event.getMember().getNickname(), event.getUser().getAvatarUrl(),
                fields,
                OffsetDateTime.now(), 0x87E5CF
        )).queue(m -> {
            for (Emote emote : list) {
                m.addReaction(emote).queue();
            }
        });

        event.replyEmbeds(createEmbed("創建成功", 0x9740b9)).setEphemeral(true).queue();
    }
}