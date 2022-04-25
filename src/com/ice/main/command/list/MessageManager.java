package com.ice.main.command.list;

import net.dv8tion.jda.api.Permission;
import net.dv8tion.jda.api.entities.Message;
import net.dv8tion.jda.api.entities.TextChannel;
import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent;

import static com.ice.main.util.EmbedCreator.createEmbed;
import static com.ice.main.util.PermissionERROR.permissionCheck;

public class MessageManager {
    public void onDeleteCommand(SlashCommandInteractionEvent event) {
        if (!permissionCheck(Permission.ADMINISTRATOR, event, true))
            return;

        TextChannel channel = (TextChannel) event.getOption("channel").getAsGuildChannel();
        Message message;

        if ((message = getMessage(event, channel)) == null)
            return;

        try {
            message.delete().queue();
        } catch (Exception e) {
            System.err.println(e.getMessage());
            event.getHook().editOriginalEmbeds(createEmbed("刪除失敗", 0xFF0000)).queue();
            return;
        }
        event.getHook().editOriginalEmbeds(createEmbed("刪除成功", 0x0FFFF)).queue();
    }

    public void onRemoveReactionsCommand(SlashCommandInteractionEvent event) {
        if (!permissionCheck(Permission.ADMINISTRATOR, event, true))
            return;

        Message message;

        if ((message = getMessage(event, (TextChannel) event.getOption("channel").getAsGuildChannel())) == null)
            return;

        try {
            message.clearReactions().queue();
        } catch (Exception e) {
            event.getHook().editOriginalEmbeds(createEmbed("刪除失敗", 0xFF0000)).queue();
            return;
        }

        event.getHook().editOriginalEmbeds(createEmbed("刪除成功", 0x0FFFF)).queue();
    }

    public void onPinCommand(SlashCommandInteractionEvent event) {

        if (!permissionCheck(Permission.ADMINISTRATOR, event, true))
            return;

        Message message;

        if ((message = getMessage(event, (TextChannel) event.getOption("channel").getAsGuildChannel())) == null)
            return;

        try {
            message.pin().queue();
        } catch (Exception e) {
            event.getHook().editOriginalEmbeds(createEmbed("標記失敗", 0xFF0000)).queue();
            return;
        }

    }


    public void onUnPinCommand(SlashCommandInteractionEvent event) {

        if (!permissionCheck(Permission.ADMINISTRATOR, event, true))
            return;

        Message message;

        if ((message = getMessage(event, (TextChannel) event.getOption("channel").getAsGuildChannel())) == null)
            return;

        try {
            message.unpin().queue();
        } catch (Exception e) {
            event.getHook().editOriginalEmbeds(createEmbed("取消標記失敗", 0xFF0000)).queue();
            return;
        }

    }

    private Message getMessage(SlashCommandInteractionEvent event, TextChannel channel) {
        try {
            return channel.retrieveMessageById(event.getOption("messageid").getAsString()).complete();
        } catch (Exception e) {
            event.getHook().editOriginalEmbeds(createEmbed("無法取得訊息", 0xFF0000)).queue();
            return null;
        }
    }

}


