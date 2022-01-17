package com.ice.main.command.list;

import net.dv8tion.jda.api.Permission;
import net.dv8tion.jda.api.events.interaction.SlashCommandEvent;
import net.dv8tion.jda.api.events.message.react.MessageReactionAddEvent;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static com.ice.main.util.EmbedCreator.createEmbed;
import static com.ice.main.util.PermissionERROR.hasPermission;

public class NoneReaction {

    Map<String, Map<String, List<String>>> info = new HashMap<>();
    // GuildID, ChannelID, MessageID

    public void onCommand(SlashCommandEvent event) {
        if (!hasPermission(Permission.ADMINISTRATOR, event, true))
            return;
        try {
            Map<String, String> messageInfo = new HashMap<>();
            messageInfo.put(event.getTextChannel().getId(), event.getOption("messageid").getAsString());

            if (info.containsKey(event.getGuild().getId())) {
                Map<String, List<String>> data = info.get(event.getGuild().getId());
                if (data.containsKey(event.getTextChannel().getId()))
                    data.get(event.getTextChannel().getId()).add(event.getOption("messageid").getAsString());
                else {
                    List<String> newData = new ArrayList<>();
                    newData.add(event.getOption("messageid").getAsString());
                    data.put(event.getTextChannel().getId(), newData);
                }

                info.put(event.getGuild().getId(), data);

            } else {
                Map<String, List<String>> data = new HashMap<>();
                List<String> newData = new ArrayList<>();

                newData.add(event.getOption("messageid").getAsString());
                data.put(event.getTextChannel().getId(), newData);

                event.getTextChannel().retrieveMessageById(event.getOption("messageid").getAsString()).complete().clearReactions().queue();

                info.put(event.getGuild().getId(), data);
            }

            event.getHook().editOriginalEmbeds(createEmbed("設定成功", 0x00FFFF)).queue();

        } catch (Exception e) {
            System.err.println(e.getMessage());
            event.getHook().editOriginalEmbeds(createEmbed("設定失敗", 0xFF0000)).queue();
        }
    }


    public void onReaction(MessageReactionAddEvent event) {
        try {
            if (!info.containsKey(event.getGuild().getId())
                    || !info.get(event.getGuild().getId()).containsKey(event.getChannel().getId())
                    || !info.get(event.getGuild().getId()).get(event.getChannel().getId()).contains(event.getMessageId()))
                return;

            event.getReaction().clearReactions().queue();
        } catch (Exception e) {
            System.err.println(e.getMessage());
        }

    }
}
