package main.java.command;

import main.java.Main;
import main.java.event.Log;
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

import static main.java.BotSetting.botOwnerID;
import static main.java.BotSetting.noPermissionERROR;
import static main.java.Main.emoji;
import static main.java.SlashCommandOption.COUNT;
import static main.java.event.Log.ignoreMessageID;
import static main.java.event.Log.logChannel;
import static main.java.util.EmbedUtil.createEmbed;

public class ClearCommand {


    /**
     * clear 指令
     */

    public void onCommand(SlashCommandEvent event) {
        if (!event.getMember().hasPermission(Permission.MESSAGE_MANAGE) && !botOwnerID.contains(event.getMember().getId())) {
            event.replyEmbeds(createEmbed(noPermissionERROR, 0xFF0000)).setEphemeral(true).queue();
            return;
        }
        OptionMapping amountOption = event.getOption(COUNT);
        int amount = amountOption == null
                ? 100 // default 100
                : (int) Math.min(200, Math.max(2, amountOption.getAsLong())); // set number to 2~200
        //arg
        String userId = event.getUser().getId();
        event.replyEmbeds(createEmbed("確定刪除 " + amount + " 則訊息?", 0xd0effe))
                .addActionRow(//add component
                        Button.danger(userId + ":delete:" + amount, "Yes!"))
                .setEphemeral(true).queue();
    }


    public void onButton(ButtonClickEvent event, String[] args) {
        if (args[1].equals("delete")) {
            int amount = Integer.parseInt(args[2]);
            try {
                List<Message> deleteMessage = event.getChannel().getIterableHistory()
                        .skipTo(event.getMessageIdLong())
                        .takeAsync(amount)
                        .get();
                deleteMessage.forEach(i -> ignoreMessageID.add(i.getId())); // 取得訊息
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
                    if (thisMessage.getMember().getNickname() == null)
                        memberNickName = "名字找無";
                    else
                        memberNickName = thisMessage.getMember().getNickname();

                    if (messageContent.length() == 0)
                        messageContent = "找無";
                    int thisMessageLength = messageContent.length() + memberNickName.length();

                    if (textLength + thisMessageLength > 5000 || i == deleteMessage.size() - 1) {
                        if (i == deleteMessage.size() - 1) {
                            if (messageContent.length() > 1024)
                                messageContent = messageContent.substring(0, 1021) + "...";
                            fields.add(new MessageEmbed.Field(memberNickName, messageContent, false)); // 加入最後一個訊息
                        }
                        logChannel.sendMessage(createEmbed(
                                "刪除: ", null,
                                "刪除訊息",
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

                event.getChannel().sendMessage(
                        createEmbed(emoji.yesEmoji.getAsMention() + ' ' + amount + " 則文字已刪除！", 0xe3c6d6)
                ).queue(m -> Log.deleteNoLog(m, 2));
            } catch (InterruptedException | ExecutionException e) {
                logChannel.sendMessage(createEmbed(
                        "錯誤: ", null,
                        e.getMessage(),
                        Main.botNickname, Main.botAvatarUrl,
                        OffsetDateTime.now(), 0x00FFFF)).queue();
            }
        }
    }
}
