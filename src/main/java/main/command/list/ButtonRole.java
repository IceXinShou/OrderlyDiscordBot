package main.command.list;

import net.dv8tion.jda.api.Permission;
import net.dv8tion.jda.api.entities.Emoji;
import net.dv8tion.jda.api.entities.Message;
import net.dv8tion.jda.api.entities.Role;
import net.dv8tion.jda.api.entities.TextChannel;
import net.dv8tion.jda.api.events.interaction.ButtonClickEvent;
import net.dv8tion.jda.api.events.interaction.SlashCommandEvent;
import net.dv8tion.jda.api.interactions.components.ActionRow;
import net.dv8tion.jda.api.interactions.components.Button;
import net.dv8tion.jda.api.interactions.components.ButtonStyle;

import java.util.List;

import static main.util.EmbedCreator.createEmbed;
import static main.util.GetEmoji.toEmoji;
import static main.util.PermissionERROR.hasPermission;

public class ButtonRole {
    public void onCommand(SlashCommandEvent event) {
        if (!hasPermission(Permission.ADMINISTRATOR, event, true))
            return;

        TextChannel channel = (TextChannel) event.getOption("channel").getAsGuildChannel();

        String buttonName = event.getOption("buttonname") == null ? null : event.getOption("buttonname").getAsString();
        String buttonEmojiName = event.getOption("buttonemoji") == null ? null : event.getOption("buttonemoji").getAsString();
        Emoji buttonEmoji = (buttonEmojiName == null ? null : toEmoji(buttonEmojiName, event.getGuild()));
        Role role;
        Message message;
        try {
            message = channel.retrieveMessageById(event.getOption("messageid").getAsString()).complete();
        } catch (Exception e) {
            event.getHook().editOriginalEmbeds(createEmbed("無法取得訊息", 0xFF0000)).queue();
            return;
        }
        try {
            role = event.getOption("role").getAsRole();
        } catch (Exception e) {
            event.getHook().editOriginalEmbeds(createEmbed("無法取得身分組", 0xFF0000)).queue();
            return;
        }

        ButtonStyle buttonStyle = ButtonStyle.SUCCESS;

        if (event.getOption("buttonstyle") != null)
            switch (event.getOption("buttonstyle").getAsString().toUpperCase()) {
                case "RED" -> buttonStyle = ButtonStyle.DANGER;
                case "GREEN" -> buttonStyle = ButtonStyle.SUCCESS;
                case "BLUE" -> buttonStyle = ButtonStyle.PRIMARY;
                case "GRAY" -> buttonStyle = ButtonStyle.SECONDARY;
                default -> event.getHook().editOriginalEmbeds(createEmbed("顏色輸入錯誤", 0xFF0000)).queue();
            }

        Button button = null;
        if (buttonName == null && buttonEmoji != null)
            button = Button.of(buttonStyle, "BR:roleToggle::" + role.getId(), null, buttonEmoji);
        else if (buttonName != null && buttonEmoji != null)
            button = Button.of(buttonStyle, "BR:roleToggle::" + role.getId(), buttonName, buttonEmoji);
        else if (buttonName != null && buttonEmoji == null)
            button = Button.of(buttonStyle, "BR:roleToggle::" + role.getId(), buttonName);
        else if (buttonName == null && buttonEmoji == null) {
            event.getHook().editOriginalEmbeds(createEmbed("按鈕名稱與按鈕符號至少要填一種", 0xFF0000)).queue();
            return;
        }


        boolean done = false;

        if (message.getActionRows().size() > 0) { // 新增
            List<ActionRow> actionRows = message.getActionRows();
            for (ActionRow i : actionRows) {
                if (i.getButtons().size() > 4)
                    continue;

                i.getButtons().add(button);
                done = true;
                break;
            }

            if (!done)
                if (actionRows.size() == 5)
                    event.getHook().editOriginalEmbeds(createEmbed("按鈕她媽太多了", 0xFF0000)).queue();
                else {
                    actionRows.add(ActionRow.of(button));
                    done = true;
                }
            if (done)
                message.editMessageComponents(actionRows).queue();

        } else { // 創建
            if (message.getEmbeds().size() > 0)
                event.getChannel().sendMessage(message).setActionRows(ActionRow.of(button)).queue();
            else
                event.getChannel().sendMessageEmbeds(createEmbed(message.getContentRaw(), 0x00FFFF)).setActionRows(ActionRow.of(button)).queue();

            done = true;
        }

        if (done)
            event.getHook().editOriginalEmbeds(createEmbed("設置成功", 0x00FFFF)).queue();
    }

    public void onButtonClick(ButtonClickEvent event, String[] args) {
        if (!args[0].equals("BR"))
            return;

        Role role = event.getGuild().getRoleById(args[3]);

        if (role == null || event.getMember() == null) {
            event.deferEdit().queue();
            return;
        }

        if (!event.getGuild().getSelfMember().canInteract(role)) {
            event.replyEmbeds(createEmbed("無法編輯比自己權限更高的成員。修復此問題，需將機器人權限組調高!", 0xFF0000)).setEphemeral(true).queue();
            return;
        }
        if (!event.getGuild().getSelfMember().hasPermission(Permission.MANAGE_ROLES)) {
            event.replyEmbeds(createEmbed("機器人無法編輯身分組，請給予編輯權限!", 0xFF0000)).setEphemeral(true).queue();
            return;
        }


        if (event.getMember().getRoles().contains(role))
            event.getGuild().removeRoleFromMember(event.getMember(), role).queue();
        else
            event.getGuild().addRoleToMember(event.getMember(), role).queue();

        event.deferEdit().queue();
    }
}
