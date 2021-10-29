package main.command.list;

import main.Main;
import main.event.Log;
import net.dv8tion.jda.api.Permission;
import net.dv8tion.jda.api.entities.Message;
import net.dv8tion.jda.api.entities.MessageEmbed;
import net.dv8tion.jda.api.events.interaction.ButtonClickEvent;
import net.dv8tion.jda.api.events.interaction.SlashCommandEvent;
import net.dv8tion.jda.api.interactions.commands.OptionMapping;
import net.dv8tion.jda.api.interactions.components.Button;

import java.time.OffsetDateTime;
import java.util.LinkedList;
import java.util.List;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;

import static main.Main.emoji;
import static main.event.Log.ignoreMessageID;
import static main.event.Log.logChannel;
import static main.lang.LangKey.*;
import static main.util.EmbedCreator.createEmbed;
import static main.util.PermissionERROR.hasPermission;
import static main.util.SlashCommandOption.COUNT;

public class Clear {


    /**
     * clear 指令
     */

    public void onCommand(SlashCommandEvent event) {
        List<String> lang = Main.language.getGuildLang(event.getGuild().getId());
        if (!hasPermission(Permission.MESSAGE_MANAGE, event, true))
            return;
        OptionMapping amountOption = event.getOption(COUNT);
        int amount = amountOption == null
                ? 100 // default 100
                : (int) Math.min(200, Math.max(2, amountOption.getAsLong())); // set number to 2~200
        // arg
        String userId = event.getUser().getId();
        event.getHook().editOriginalEmbeds(createEmbed(lang.get(CLEAR_CONFIRM_DELETE).formatted(amount), 0xd0effe))
                .setActionRow(Button.danger("Clear:delete:" + userId + ':' + amount, "Yes!"))
                .queue();
    }

    public void onButton(ButtonClickEvent event, String[] args) {
        List<String> lang = Main.language.getGuildLang(event.getGuild().getId());
        if (!args[0].equals("Clear"))
            return;
        if (args[1].equals("delete")) {
            event.deferEdit().setEmbeds(createEmbed(lang.get(CLEAR_DELETED), 0x9740b9)).setActionRows().queue();
            int amount = Integer.parseInt(args[3]);
            try {
                List<Message> deleteMessage = event.getChannel().getIterableHistory()
                        .skipTo(event.getMessageIdLong())
                        .takeAsync(amount)
                        .get();
                for (Message i : deleteMessage)
                    ignoreMessageID.add(i.getId());

                event.getChannel().purgeMessages(deleteMessage);

                /**
                 * 紀錄刪除內容
                 */
                LinkedList<MessageEmbed.Field> fields = new LinkedList<>();
                int textLength = 0;
                /**
                 * 存放 i 至最大限制容量
                 */
                for (int i = 0; i < deleteMessage.size(); i++) {
                    Message thisMessage = deleteMessage.get(i);
                    String messageContent = thisMessage.getContentRaw();
                    String memberNickName;
                    if (thisMessage.getMember() == null)
                        memberNickName = lang.get(CLEAR_MEMBER_NOT_FOUND);
                    else if (thisMessage.getMember().getNickname() == null)
                        memberNickName = lang.get(CLEAR_MEMBER_NAME_NOT_FOUND);
                    else
                        memberNickName = lang.get(CLEAR_MEMBER_NONE);

                    if (messageContent.length() == 0)
                        messageContent = lang.get(CLEAR_MESSAGE_NOT_FOUND);
                    int thisMessageLength = messageContent.length() + memberNickName.length();

                    if (textLength + thisMessageLength > 5000 || i == deleteMessage.size() - 1) {
                        if (i == deleteMessage.size() - 1) {
                            if (messageContent.length() > 1024)
                                messageContent = messageContent.substring(0, 1021) + "...";
                            fields.add(new MessageEmbed.Field(memberNickName, messageContent, false)); // 加入最後一個訊息
                        }
                        logChannel.sendMessageEmbeds(createEmbed(
                                lang.get(CLEAR_MESSAGE_DELETE_EVENT), null,
                                lang.get(CLEAR_MESSAGE_DELETE),
                                Main.botNickname, Main.botAvatarUrl,
                                fields,
                                OffsetDateTime.now(), 0x1ABC9C)
                        ).completeAfter(50, TimeUnit.MILLISECONDS);
                        fields.clear();
                        textLength = 0;
                    } else {
                        if (messageContent.length() > 1024)
                            messageContent = messageContent.substring(0, 1021) + "...";
                        fields.add(new MessageEmbed.Field(memberNickName, messageContent, false));
                        textLength += thisMessageLength;
                    }
                }

                event.getChannel().sendMessageEmbeds(
                        createEmbed(emoji.yesEmoji.getAsMention() + ' ' + amount + ' ' + lang.get(CLEAR_MESSAGE_COUNT).formatted(amount), 0xe3c6d6)
                ).queue(m -> Log.deleteNoLog(m, 2));
            } catch (InterruptedException | ExecutionException e) {
                logChannel.sendMessageEmbeds(createEmbed(
                        lang.get(CLEAR_MESSAGE_ERROR_EVENT), null,
                        e.getMessage(),
                        Main.botNickname, Main.botAvatarUrl,
                        OffsetDateTime.now(), 0x00FFFF)).queue();
            }
        }
    }
}
