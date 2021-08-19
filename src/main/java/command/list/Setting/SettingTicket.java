package main.java.command.list.Setting;

import main.java.command.list.Ticket;
import main.java.util.file.GuildSettingHelper;
import main.java.util.file.JsonFileManager;
import net.dv8tion.jda.api.MessageBuilder;
import net.dv8tion.jda.api.entities.*;
import net.dv8tion.jda.api.events.interaction.SlashCommandEvent;
import net.dv8tion.jda.api.interactions.components.ActionRow;
import net.dv8tion.jda.api.interactions.components.Button;
import net.dv8tion.jda.api.interactions.components.ButtonStyle;
import net.dv8tion.jda.api.interactions.components.Component;
import org.jetbrains.annotations.Contract;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.json.JSONArray;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

import static main.java.BotSetting.defaultTicketChannelName;
import static main.java.Main.botID;
import static main.java.util.EmbedCreator.createEmbed;
import static main.java.util.JsonKeys.*;
import static main.java.util.Tag.tagChannel;
import static main.java.util.Tag.tagChannelID;

public record SettingTicket(GuildSettingHelper settingHelper) {

    @SuppressWarnings("ConstantConditions")
    public void newTicket(@NotNull SlashCommandEvent event, boolean newTicket) {
        Guild guild = event.getGuild();
        List<MessageEmbed.Field> fields = new ArrayList<>();
        TextChannel eventTextChannel = event.getTextChannel();

        // 新增按鈕要輸入Ticket訊息，新增訊息要輸入要傳送的訊息 (必填)
        String messageID = event.getOption("messageid").getAsString();
        // 新增按鈕要輸入Ticket訊息頻道，新增訊息要輸入要傳送的訊息的頻道 (必填)
        TextChannel sendTicketMessageChannel = null;
        if (event.getOption("messagechannel").getAsGuildChannel().getType() != ChannelType.TEXT) {
            fields.add(new MessageEmbed.Field("訊息頻道錯誤", "", false));
        } else
            sendTicketMessageChannel = (TextChannel) event.getOption("messagechannel").getAsGuildChannel();
        /*
          按鈕
         */
        // 顏色
        ButtonStyle buttonStyle = null;
        int buttonColorInt = 0x4F545C;
        String buttonColor = null;
        if (event.getOption("buttoncolor") == null)
            buttonStyle = ButtonStyle.PRIMARY;
        else
            switch (buttonColor = event.getOption("buttoncolor").getAsString().toUpperCase()) {
                case "RED" -> {
                    buttonStyle = ButtonStyle.DANGER;
                    buttonColorInt = 0xF04747;
                }
                case "GREEN" -> {
                    buttonStyle = ButtonStyle.SUCCESS;
                    buttonColorInt = 0x43B581;
                }
                case "BLUE" -> {
                    buttonStyle = ButtonStyle.PRIMARY;
                    buttonColorInt = 0x5865F2;
                }
                case "GRAY" -> {
                    buttonStyle = ButtonStyle.SECONDARY;
                    buttonColorInt = 0x4F545C;
                }
                default -> fields.add(new MessageEmbed.Field("按鈕顏色錯誤", "", false));
            }
        // 表情符號
        String buttonEmojiName = event.getOption("buttonemoji") == null ? null : event.getOption("buttonemoji").getAsString();
        Emoji buttonEmoji = (buttonEmojiName == null ? null : toEmoji(buttonEmojiName, guild));
        // 內文
        String buttonName = event.getOption("buttonname") == null ? null : event.getOption("buttonname").getAsString();
        /*
          Ticket
         */
        // 預設輸入訊息
        String enteredMessage = event.getOption("enteredmessage") == null ? null : event.getOption("enteredmessage").getAsString();
        // 允許檢視頻道的權限組
        Role allowRole = event.getOption("allowrole") == null ? null : event.getOption("allowrole").getAsRole();
        // tag 權限組 (預設true)
        boolean tagRole = event.getOption("allowtagrole") == null || event.getOption("allowtagrole").getAsBoolean();
        // 觸發權限
        boolean onlyone = event.getOption("onlyone") == null || event.getOption("onlyone").getAsBoolean();
        /*
          文字頻道
         */
        // 頻道名稱 (預設: Ticket %num%)
        String textName = event.getOption("textname") == null ? null : event.getOption("textname").getAsString();
        // 頻道類別
        String textCategoryID = event.getOption("textcategory") == null ? null : event.getOption("textcategory").getAsString();
        /*
          語音頻道
         */
        // 頻道名稱 (預設: Ticket %num%)
        String voiceName = event.getOption("voicename") == null ? null : event.getOption("voicename").getAsString();
        // 頻道類別
        String voiceCategoryID = event.getOption("voicecategory") == null ? null : event.getOption("voicecategory").getAsString();
        boolean hasVoiceChannel;
        if (event.getOption("hasvoicechannel") != null)
            hasVoiceChannel = event.getOption("hasvoicechannel").getAsBoolean();
        else
            hasVoiceChannel = voiceName != null || voiceCategoryID != null;

        // 判斷error
        if (buttonEmojiName == null && buttonName == null)
            fields.add(new MessageEmbed.Field("按鈕名稱與表情符號請至少輸入一項", "", false));
        if (buttonEmojiName != null && buttonEmoji == null)
            fields.add(new MessageEmbed.Field("找不到此表情符號", "", false));
        if (allowRole == null)
            fields.add(new MessageEmbed.Field("需輸入處理客服的身分組", "", false));

        // 取得要傳送的訊息，或是要修改的訊息
        Message message = null;
        if (sendTicketMessageChannel != null) {
            try {
                message = sendTicketMessageChannel.retrieveMessageById(messageID).complete();
            } catch (Exception e) {
                fields.add(new MessageEmbed.Field("在 " + tagChannel(sendTicketMessageChannel) + "找不到此 ID 的訊息 (`" + messageID + "`)", "", false));
            }
        }

        //如果是要新增訊息，把新增位置改到打指令的頻道
        if (newTicket)
            sendTicketMessageChannel = eventTextChannel;

        //取得自動設定的訊息參數
        if (textName == null)
            textName = defaultTicketChannelName;
        if (textCategoryID == null)
            textCategoryID = sendTicketMessageChannel.getParent().getId();

        if (hasVoiceChannel) {
            if (voiceName == null)
                voiceName = defaultTicketChannelName;
            if (voiceCategoryID == null)
                voiceCategoryID = sendTicketMessageChannel.getParent().getId();
        }


        JSONObject ticketSetting = getSettingData(event.getGuild(), settingHelper);
        JSONObject channelKey = getOrDefault(ticketSetting, sendTicketMessageChannel.getId());
        JSONArray buttonSettings = null;
        int buttonLength = 0;
        // 新增訊息
        MessageBuilder builder = new MessageBuilder();
        if (newTicket) {
            if (message != null) {
                if (message.getEmbeds().size() > 0) {
                    builder.setEmbeds(message.getEmbeds());
                }
                String messageRaw = message.getContentRaw();
                if (message.getEmbeds().size() == 0 && messageRaw != null) {
                    int newLine = messageRaw.indexOf("\n");
                    String title;
                    if (newLine != -1 && (title = messageRaw.substring(0, newLine)).length() <= 256)
                        builder.setEmbeds(createEmbed(title, messageRaw.substring(newLine), buttonColorInt));
                    else {
                        if (messageRaw.length() <= 256)
                            builder.setEmbeds(createEmbed(buttonColorInt, messageRaw));
                        else
                            builder.setEmbeds(createEmbed(messageRaw, buttonColorInt));
                    }
                } else {
                    builder.setContent(messageRaw);
                }
            }
        }
        // 新增按鈕
        else {
            // 取得本來有多少按鈕
            buttonSettings = getOrDefaultArray(channelKey, messageID);
            buttonLength = buttonSettings.length();
            // 找不到設定
            if (!ticketSetting.has(sendTicketMessageChannel.getId()) ||
                    !ticketSetting.getJSONObject(sendTicketMessageChannel.getId()).has(messageID) ||
                    message.getActionRows().size() == 0 ||
                    buttonLength == 0)
                fields.add(new MessageEmbed.Field("此訊息尚未註冊, 請使用 `/newticket` 新增客服按鈕", "", false));
            if (buttonLength == 5)
                fields.add(new MessageEmbed.Field("此訊息的按鈕量過多", "", false));
        }

        // 設定按鈕
        Button button;
        if (buttonEmoji != null)
            button = Button.of(buttonStyle, "Ticket:newTicket::" + buttonLength, buttonName, buttonEmoji);
        else
            button = Button.of(buttonStyle, "Ticket:newTicket::" + buttonLength, buttonName);

        if (fields.size() > 0) {
            event.getHook().editOriginalEmbeds(createEmbed("錯誤回報", fields, 0xFF0000)).queue();
            return;
        }

        if (newTicket) {
            messageID = sendTicketMessageChannel
                    .sendMessage(builder.build())
                    .setActionRows(ActionRow.of(button))
                    .complete().getId();
            //使用新的messageID
            buttonSettings = getOrDefaultArray(channelKey, messageID);
        } else {
            //要修改的訊息有足夠大小的ActionRows
            if (message.getActionRows().size() > 0) {
                List<Component> buttons = message.getActionRows().get(0).getComponents();
                buttons.add(button);
                message.editMessageComponents(message.getActionRows()).queue();
            }
        }

        // 儲存
        JSONObject buttonSetting = new JSONObject();
        buttonSetting.put(TICKET_ALLOW_ROLE_ID, allowRole.getId());
        buttonSetting.put(TICKET_ALLOW_TAG, tagRole);
        buttonSetting.put(TICKET_BUTTON_TYPE, buttonStyle.getKey());
        if (buttonEmoji != null)
            buttonSetting.put(TICKET_BUTTON_EMOJI_NAME, buttonEmoji.getAsMention());
        if (buttonName != null)
            buttonSetting.put(TICKET_BUTTON_NAME, buttonName);
        buttonSetting.put(TICKET_TEXT_CATEGORY_ID, textCategoryID);
        buttonSetting.put(TICKET_TEXT_NAME, textName);
        if (hasVoiceChannel) {
            buttonSetting.put(TICKET_VOICE_CATEGORY_ID, voiceCategoryID);
            buttonSetting.put(TICKET_VOICE_NAME, voiceName);
        }
        buttonSetting.put(TICKET_ENTERED_MESSAGE, enteredMessage);
        buttonSetting.put(TICKET_ONLY_ONE, onlyone);
        buttonSettings.put(buttonSetting);
        channelKey.put(messageID, buttonSettings);

        fields.add(new MessageEmbed.Field("新增按鈕的訊息 ID", messageID, false));
        fields.add(new MessageEmbed.Field("此訊息的頻道", tagChannel(sendTicketMessageChannel) + "\n`(" + sendTicketMessageChannel.getId() + ")`", false));
        fields.add(new MessageEmbed.Field("預設輸入訊息", "\n`(" + message.getId() + ")`", false));
        fields.add(new MessageEmbed.Field("客服文字頻道名稱", textName, false));
        fields.add(new MessageEmbed.Field("客服文字頻道目錄", tagChannelID(textCategoryID) + "\n`(" + textCategoryID + ")`", false));
        fields.add(new MessageEmbed.Field("語音頻道", hasVoiceChannel ? "有" : "無", false));
        if (hasVoiceChannel) {
            fields.add(new MessageEmbed.Field("客服語音頻道名稱", voiceName, false));
            fields.add(new MessageEmbed.Field("客服語音頻道目錄", tagChannelID(voiceCategoryID) + "\n`(" + voiceCategoryID + ")`", false));
        }
        if (buttonName != null)
            fields.add(new MessageEmbed.Field("按鈕名稱", buttonName, false));
        if (buttonEmoji != null)
            fields.add(new MessageEmbed.Field("按鈕表情符號", buttonEmoji.getAsMention(), false));
        fields.add(new MessageEmbed.Field("按鈕顏色", buttonColor + "\n`(0x" + Integer.toHexString(buttonColorInt) + ")`", false));
        fields.add(new MessageEmbed.Field("允許身分組", allowRole.getAsMention() + "\n`(" + allowRole.getId() + ")`", false));
        fields.add(new MessageEmbed.Field("是否 Tag 允許身分組", "`" + (tagRole ? "是" : "否") + "`", false));
        fields.add(new MessageEmbed.Field("同一人一次只能使用一次", "`" + (onlyone ? "是" : "否") + "`", false));


        settingHelper.getGuildSettingManager(event.getGuild().getId()).saveFile();
        event.getHook().editOriginalEmbeds(createEmbed("設定成功", fields, 0x11FF99)).queue();
    }

    public void removeTicket(@NotNull SlashCommandEvent event, Ticket ticket) {
        JSONObject data = getSettingData(Objects.requireNonNull(event.getGuild()), settingHelper);
        String channelID = Objects.requireNonNull(event.getOption("messagechannel")).getAsGuildChannel().getId();
        if (Objects.requireNonNull(data).has(channelID)) {
            TextChannel channel;
            if ((channel = event.getGuild().getTextChannelById(channelID)) == null) {
                event.getHook().editOriginalEmbeds(createEmbed("移除失敗 (找不到此頻道)", 0xFF0000)).queue();
                data.remove(channelID);
                settingHelper.getGuildSettingManager(event.getGuild().getId()).saveFile();
                return;
            }
            JSONObject messageChannel = data.getJSONObject(channelID);
            String messageID = Objects.requireNonNull(event.getOption("messageid")).getAsString();
            if (messageChannel.has(messageID)) {
                Message message;
                if ((message = channel.retrieveMessageById(messageID).complete()) == null || !message.getAuthor().getId().equals(botID)) {
                    event.getHook().editOriginalEmbeds(createEmbed("移除失敗 (找不到此訊息)", 0xFF0000)).queue();
                    messageChannel.remove(channelID);
                    settingHelper.getGuildSettingManager(event.getGuild().getId()).saveFile();
                    return;
                }
                JSONArray messageButtons = messageChannel.getJSONArray(messageID);
                // {messageChannel:{messageID:[{},{},{},{},{}]}}
                Byte removePos = (Byte) (byte) Math.max(1, Math.min(5, (Objects.requireNonNull(event.getOption("position")).getAsLong() - 1)));
                if (ticket.isButtonUsed(channelID, messageID, removePos)) {
                    event.getHook().editOriginalEmbeds(createEmbed("此按鈕目前正在被使用，請先關閉所有關於此按鈕的客服再執行此指令", 0xFF0000)).queue();
                    return;
                }

                if (messageButtons.length() > removePos) {
                    messageButtons.remove(removePos);
                    List<Button> buttons = new ArrayList<>();
                    if (messageButtons.length() > 0) {
                        for (int i = 0; i < messageButtons.length(); i++) {
                            JSONObject buttonObject = messageButtons.getJSONObject(i);
                            Button button;
                            String buttonName = buttonObject.has(TICKET_BUTTON_NAME) ? buttonObject.getString(TICKET_BUTTON_NAME) : "";
                            if (buttonObject.has(TICKET_BUTTON_EMOJI_NAME))
                                button = Button.of(
                                        ButtonStyle.fromKey(buttonObject.getInt(TICKET_BUTTON_TYPE)),
                                        "Ticket:newTicket::" + i,
                                        buttonName,
                                        toEmoji(buttonObject.getString(TICKET_BUTTON_EMOJI_NAME), event.getGuild()));
                            else
                                button = Button.of(
                                        ButtonStyle.fromKey(buttonObject.getInt(TICKET_BUTTON_TYPE)),
                                        "Ticket:newTicket::" + i,
                                        buttonName);
                            buttons.add(button);
                        }
                        ActionRow actionRow = ActionRow.of(buttons);
                        message.editMessageComponents(actionRow).queue();
                    } else {
                        messageChannel.remove(messageID);
                        if (messageChannel.length() == 0)
                            data.remove(channelID);
                        message.delete().queue();
                    }
                    settingHelper.getGuildSettingManager(event.getGuild().getId()).saveFile();
                } else
                    event.getHook().editOriginalEmbeds(createEmbed("無法刪除指定的按鈕位置", 0xFF0000)).queue();
            } else
                event.getHook().editOriginalEmbeds(createEmbed("此訊息無被設定紀錄", 0xFF0000)).queue();
        } else
            event.getHook().editOriginalEmbeds(createEmbed("此頻道無被設定紀錄", 0xFF0000)).queue();
    }

    private @Nullable
    JSONObject getSettingData(@NotNull Guild guild, @NotNull GuildSettingHelper settingHelper) {
        JsonFileManager fileManager = settingHelper.getGuildSettingManager(guild.getId());
        if (fileManager.data.has(TICKET_SETTING))
            return fileManager.data.getJSONObject(TICKET_SETTING);
        else {
            JSONObject data = new JSONObject();
            settingHelper.getGuildSettingManager(guild.getId()).data.put(TICKET_SETTING, data);
            return data;
        }
    }

    private @Nullable
    Emoji toEmoji(@NotNull String emojiName, Guild guild) {
        int startIndex;
        if ((startIndex = emojiName.indexOf("<")) != -1) {
            int endIndex = emojiName.indexOf(">", startIndex);
            String[] ids = emojiName.substring(startIndex + 1, endIndex).split(":");
            Emote emoji;
            if (ids.length == 3 && (emoji = guild.getJDA().getEmoteById(ids[2])) != null)
                return Emoji.fromEmote(emoji);
            if (ids.length == 3)
                return null;
        }
        if ((emojiName.startsWith("U+") || emojiName.startsWith("u+")))
            return Emoji.fromUnicode(emojiName);
        Emote emote;
        if (isDigit(emojiName) && (emote = guild.getJDA().getEmoteById(emojiName)) != null)
            return Emoji.fromEmote(emote);

        List<Emote> emotes = guild.getEmotesByName(emojiName, false);
        if (emotes.size() > 0)
            return Emoji.fromEmote(emotes.get(0));
        return null;
    }

    @Contract(pure = true)
    private boolean isDigit(@NotNull String emojiName) {
        for (char i : emojiName.toCharArray()) {
            if (i < '0' || i > '9')
                return false;
        }
        return true;
    }

    private JSONObject getOrDefault(@NotNull JSONObject input, String key) {
        if (input.has(key))
            return input.getJSONObject(key);
        else {
            JSONObject data = new JSONObject();
            input.put(key, data);
            return data;
        }
    }

    private JSONArray getOrDefaultArray(@NotNull JSONObject input, String key) {
        if (input.has(key))
            return input.getJSONArray(key);
        else {
            JSONArray data = new JSONArray();
            input.put(key, data);
            return data;
        }
    }
}

/*
 * buttonType:
 * RED, GREEN, BLUE, GRAY
 * <p>
 * /setting newticket
 * {ticket:{channelID:{messageID:[{textCategory:"12345",allowRoleID:"12345",buttonName:"12345",emojiID:"8787",emojiName:"thinking",shouldTag:"true",enteredMessage:"",buttonColor:"RED"}]}}
 * <p>
 * /setting addticket
 * <p>
 * /setting removeticket num
 * <p>
 * <p>
 * new OptionData(STRING, "messageid", "添加按鈕的訊息 ID (需在同一個頻道)", true),
 * new OptionData(CHANNEL, "messagechannel", "訊息頻道", true),
 * new OptionData(CHANNEL, "textcategory", "新客服文字頻道目錄"),
 * new OptionData(STRING, "textname", "新客服文字頻道名稱 (%num%, %guild_name%, %user%, %user_name%, %user_tag%, or %nickname%)"),
 * new OptionData(BOOLEAN, "hasvoicechannel", "是否有語音頻道"),
 * new OptionData(CHANNEL, "voicecategory", "新客服語音頻道目錄"),
 * new OptionData(STRING, "voicename", "新客服語音頻道名稱 (%num%, %guild_name%, %user%, %user_name%, %user_tag%, or %nickname%)"),
 * new OptionData(STRING, "buttoncolor", "按鈕顏色"),
 * new OptionData(STRING, "buttonname", "按鈕名稱"),
 * new OptionData(STRING, "buttonemoji", "按鈕表情符號"),
 * new OptionData(ROLE, "allowrole", "允許檢視頻道的身分組"),
 * new OptionData(BOOLEAN, "allowtagrole", "觸發時是否 tag 身分組"),
 * new OptionData(STRING, "enteredmessage", "觸發時發送的訊息 (%num%, %role%, %rolename%, %guild_name%, %user%, %user_name%, %user_tag%, or %nickname%)")
 */