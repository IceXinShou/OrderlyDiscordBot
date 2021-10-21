package main.java.event;

import main.java.Main;
import main.java.util.EmojiUtil;
import main.java.util.GuildUtil;
import main.java.util.QuestionStep;
import main.java.util.file.JsonFileManager;
import net.dv8tion.jda.api.entities.*;
import net.dv8tion.jda.api.events.guild.member.GuildMemberJoinEvent;
import net.dv8tion.jda.api.events.interaction.SlashCommandEvent;
import net.dv8tion.jda.api.events.message.priv.PrivateMessageReceivedEvent;
import net.dv8tion.jda.api.events.message.priv.react.PrivateMessageReactionAddEvent;
import org.json.JSONObject;

import java.time.OffsetDateTime;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.regex.Pattern;

import static main.java.BotSetting.configFolder;
import static main.java.BotSetting.joinRoleID;
import static main.java.Main.emoji;
import static main.java.util.EmbedCreator.createEmbed;
import static main.java.util.GuildUtil.guild;
import static main.java.util.GuildUtil.guildID;
import static main.java.util.JsonKeys.*;
import static main.java.util.UrlDataGetter.getData;

public class OwnJoin {
    public static JSONObject memberData;
    private final JsonFileManager memberFile;
    Map<String, QuestionStep> userProgress = new HashMap<>();

    public OwnJoin() {
        memberFile = new JsonFileManager(configFolder + "/allMember.json");
        memberData = memberFile.data;
    }

    /**
     * 加入事件
     */
    public void onGuildMemberJoin(GuildMemberJoinEvent event) {
        if (!event.getGuild().getId().equals(guildID)) return;
        String userID = event.getUser().getId();

        for (Role i : joinRoleID) {
            try {
                guild.addRoleToMember(userID, i).queue();
            } catch (Exception ignored) {
            }
        }

        // 打開私聊
        try {
            event.getUser().openPrivateChannel().queue(channel -> {
                if (memberData.has(userID)) {
                    channel.sendMessageEmbeds(createEmbed("您已完成過暱稱設定\n" +
                            "Your nickname had been set", 0xe5b849)).queue();
                    initMemberOnJoin(userID, channel, true);
                    FollowRoles(userID, channel);
                } else {
                    initMemberOnJoin(userID, channel, false);
                }
            });
        } catch (Exception e) {
            System.out.println(e.getMessage());
        }
    }


    private void changeNickName(Member member, String chinese, String english) {
//        String nameSpace = "%name% - %MinecraftID%";
        member.modifyNickname(chinese + " - " + english).queue();
    }

    private void showUserInfo(MessageChannel privateChannel, User user, QuestionStep progress) {
        if (progress.infoMessageID != null)
            privateChannel.deleteMessageById(progress.infoMessageID).queue();

        String nickname;
        List<MessageEmbed.Field> fields = new ArrayList<>();
        boolean settingDone = true;
        if (progress.chineseNick != null) {
            fields.add(new MessageEmbed.Field(EmojiUtil.dotEmojis[7].getAsMention() + " 中文暱稱: ", progress.chineseNick, false));
            nickname = progress.chineseNick + " - ";
        } else {
            fields.add(new MessageEmbed.Field(EmojiUtil.dotEmojis[7].getAsMention() + " 中文暱稱: ", "未設置", false));
            nickname = "未設置 - ";
            settingDone = false;
        }

        if (progress.playMinecraft) {
            if (progress.minecraftID != null) {
                fields.add(new MessageEmbed.Field(EmojiUtil.dotEmojis[4].getAsMention() + " Minecraft ID: ", progress.minecraftID, false));
                nickname += progress.minecraftID;
            } else {
                fields.add(new MessageEmbed.Field(EmojiUtil.dotEmojis[4].getAsMention() + " Minecraft ID: ", "未設置", false));
                nickname += "未設置";
                settingDone = false;
            }
        } else if (progress.englishNick != null) {
            fields.add(new MessageEmbed.Field(EmojiUtil.dotEmojis[4].getAsMention() + " 英文暱稱: ", progress.englishNick, false));
            nickname += progress.englishNick;
        } else {
            fields.add(new MessageEmbed.Field(EmojiUtil.dotEmojis[4].getAsMention() + " 英文暱稱: ", "未設置", false));
            nickname += "未設置";
            settingDone = false;
        }

        privateChannel.sendMessageEmbeds(createEmbed(
                "原之序 | ORDERLY SERVER", "伺服器暱稱" + (settingDone ? "已完成設定" : "設定中"),
                "原序之徒 | ORDERLY DISCIPLE",
                nickname, user.getAvatarUrl(),
                fields, OffsetDateTime.now(), 0xCFD3FF
        )).queue(msg -> progress.setInfoMessageID(msg.getId()));

    }

    // 儲存使用者到資料
    private void saveMember(Member member) {
        QuestionStep progress = userProgress.get(member.getId());

        JSONObject memberInfo = new JSONObject();
        memberInfo.put(PLAY_MINECRAFT, progress.playMinecraft);
        // 後面的名字
        if (progress.playMinecraft)
            memberInfo.put(MINECRAFT_ID, progress.minecraftID);
        else
            memberInfo.put(ENGLISH_NICK, progress.englishNick);
        memberInfo.put(CHINESE_NICK, progress.chineseNick);


        System.out.printf("[Join] %s (%s) : %s", member.getUser().getAsTag(), member.getId(), memberInfo);
        memberData.put(member.getId(), memberInfo);
        memberFile.saveFile();
    }

    public void onCommand(SlashCommandEvent event) {
        /*
        String userID = event.getUser().getId();
        if (userID.equals(Main.botID)) return;
        MessageChannel channel = event.getChannel();


        if (event.getName().equals("join")) {
            if (memberData.has(userID)) {
                event.getHook().editOriginalEmbeds(createEmbed("您已完成過暱稱設定, 開始重新設定\n" +
                        "You Have Setting Before, Let's Reset!", 0xe5b849)).queue();
                initMemberOnJoin(channel, userID);
                // 傳送設定檔
                askForMinecraftID(userID, channel, false);
            } else {
                initMemberOnJoin(channel, userID);
            }
            return;
        }
*/

        String userID = event.getUser().getId();
        if (userID.equals(Main.botID)) return;
        MessageChannel channel = event.getChannel();

        // 手動設定
        if (event.getName().equals("join")) {
            if (memberData.has(userID)) {
                event.getHook().editOriginalEmbeds(createEmbed("您已完成過暱稱設定,開始重新設定\n" +
                        "you had set nickname before, let's reset!", 0xe5b849)).queue();
                initMemberOnJoin(userID, channel, true);
                // 傳送設定檔
                askPlayingMinecraft(userID, channel, false);
            }
        }


    }


    /**
     * 回覆導向 (反應)
     */
    public void onPrivateMessageReactionAdd(PrivateMessageReactionAddEvent event) {
        String userID = event.getUserId();
        if (userID.equals(Main.botID)) return;

        QuestionStep progress = userProgress.getOrDefault(userID, new QuestionStep(0, null));
        String messageID = event.getMessageId();
        String reaction = event.getReactionEmote().getEmote().getAsMention();

        if (messageID.equals(progress.getMessageID())) {
            switch (progress.getStep()) {
                case 0:
                    event.getChannel().sendMessageEmbeds(createEmbed("您發現了錯誤, 請回報給工作人員\n" +
                            "You found a bug, please report to developers", 0xFF0000)).queue();
                    break;
                case 2:
                    // yes
                    if (reaction.equals(emoji.yesEmoji.getAsMention())) {
                        progress.playMinecraft = true;
                        showUserInfo(event.getChannel(), event.getUser(), progress);
                        getMinecraftID(userID, event.getChannel());
                        break;
                    }
                    // no
                    else if (reaction.equals(emoji.noEmoji.getAsMention())) {
                        progress.playMinecraft = false;
                        showUserInfo(event.getChannel(), event.getUser(), progress);
                        getEnglishNickName(userID, event.getChannel());
                        break;
                    }
                    break;
                case 7:
                    // yes
                    if (reaction.equals(emoji.yesEmoji.getAsMention())) {
                        // 更改nickname和處存資料到檔案
                        Member member = GuildUtil.guild.retrieveMemberById(userID).complete();
                        if (progress.playMinecraft)
                            changeNickName(member, progress.chineseNick, progress.minecraftID);
                        else
                            changeNickName(member, progress.chineseNick, progress.englishNick);
                        // 儲存user至檔案
                        saveMember(member);
                        userProgress.remove(userID);
                        event.getChannel().sendMessageEmbeds(createEmbed("請邀請者在認證頻道把你拉進來喔~\n" +
                                "Welcome to join us!", 0x9740b9)).queue();
                        break;
                    }
                    // no
                    else if (reaction.equals(emoji.noEmoji.getAsMention())) {
                        event.getChannel().sendMessageEmbeds(createEmbed("由於您的抉擇，我們無法讓您加入伺服器\n" +
                                "Because of your choice, we can not let you join!", 0xFF0000)).queue();
                        userProgress.remove(userID);
                        break;
                    }
            }
        }
    }


    /**
     * 回覆導向 (對話)
     */
    public void onPrivateMessageReceived(PrivateMessageReceivedEvent event) {
        String userID = event.getAuthor().getId();
        if (userID.equals(Main.botID)) return;
        String message = event.getMessage().getContentRaw();
        PrivateChannel channel = event.getChannel();

        // 手動設定
        if (message.equalsIgnoreCase("j")) {
            if (memberData.has(userID)) {
                channel.sendMessageEmbeds(createEmbed("您已完成過暱稱設定,開始重新設定\n" +
                        "you had set nickname before, let's reset!", 0xe5b849)).complete();
                initMemberOnJoin(userID, channel, true);
                // 傳送設定檔
                askPlayingMinecraft(userID, channel, false);
            } else {
                initMemberOnJoin(userID, channel, false);
            }
            return;
        }

        // 問題回答
        QuestionStep progress = userProgress.getOrDefault(userID, new QuestionStep(0, ""));
        List<Message> lastMessage = channel.getHistory().retrievePast(2).complete();
        if (lastMessage.get(1) == null) return;
        String questionMessageID = lastMessage.get(1).getId();
        if (questionMessageID.equals(progress.getMessageID())) {
            switch (progress.getStep()) {
                case 0:
                    channel.sendMessageEmbeds(createEmbed("您發現了錯誤, 請回報給工作人員\n" +
                            "You found a bug, please report to developers", 0xFF0000)).queue();
                    break;
                case 1:
                    askPlayingMinecraft(userID, channel, true);
                    break;
                case 4:
                    if (message.equals("返回") || message.equalsIgnoreCase("cancel")) {
                        askPlayingMinecraft(userID, channel, true);
                        break;
                    }
                    String result = getData("https://api.mojang.com/users/profiles/minecraft/" + message);
                    if (result == null || result.length() == 0 || new JSONObject(result).has("error")) {
                        channel.sendMessageEmbeds(createEmbed("查無此玩家！\n" +
                                "Can not find player!", 0xFF0000)).queue();
                        progress.minecraftID = null;
                        getMinecraftID(userID, channel);

                    } else {
                        progress.englishNick = null;
                        progress.minecraftID = message;
                        showUserInfo(channel, event.getAuthor(), progress);
                        getChineseNickName(userID, channel);
                    }
                    break;
                case 5:
                    progress.englishNick = message + '*';
                    progress.minecraftID = null;
                    showUserInfo(channel, event.getAuthor(), progress);
                    getChineseNickName(userID, channel);
                    break;
                case 6:
                    if (Pattern.matches("^[\u4E00-\u9fa5]+$", message) && message.length() == 2) {
                        progress.chineseNick = message;
                        showUserInfo(channel, event.getAuthor(), progress);
                        FollowRoles(userID, channel);
                    } else {
                        channel.sendMessageEmbeds(createEmbed("輸入錯誤！\n" +
                                "Wrong Type", 0xFF0000)).queue();
                        progress.chineseNick = null;
                        getChineseNickName(userID, channel);
                    }
                    break;
            }
        }
    }


    /**
     * 問題列表
     */

    private void initMemberOnJoin(String userID, MessageChannel channel, boolean hasData) {
        // 已經有user的資料
        if (hasData) {
            // 加入已儲存的user資料
            QuestionStep progress = new QuestionStep(0, null);
            JSONObject userData = memberData.getJSONObject(userID);
            // 有玩minecraft
            if (progress.playMinecraft == userData.getBoolean(PLAY_MINECRAFT))
                progress.minecraftID = userData.getString(MINECRAFT_ID);
            else
                progress.englishNick = userData.getString(ENGLISH_NICK);
            progress.chineseNick = userData.getString(CHINESE_NICK);
            // 加入progress
            userProgress.put(userID, progress);
        } else {
            channel.sendMessageEmbeds(createEmbed(String.format("" +
                    "歡迎您來到 <%s> , 在正式進入前還需要通過驗證！\n Welcome to <%s> , before your join, you have to auth your account!", guild.getName(), guild.getName()), 0x9740b9)).queue();
            askPlayingMinecraft(userID, channel, true);
        }
    }

    private void askPlayingMinecraft(String userID, MessageChannel channel, boolean newAccount) {
        channel.sendMessageEmbeds(createEmbed("請問您是否有遊玩 **Minecraft**？\n" +
                "Have you ever played **Minecraft**?", 0xe5b849)).queue(messageContent -> {
            messageContent.addReaction(emoji.yesEmoji).queue();
            messageContent.addReaction(emoji.noEmoji).queue();
            // 新加入的人
            if (newAccount) {
                QuestionStep step = new QuestionStep(2, channel.getId());
                step.setStep(2);
                step.setMessageID(messageContent.getId());
                userProgress.put(userID, step);
            }
            // 修改資料
            else {
                QuestionStep step = userProgress.get(userID);
                step.setStep(2);
                step.setMessageID(messageContent.getId());
            }
        });
    }

    private void getMinecraftID(String userID, MessageChannel channel) {
        channel.sendMessageEmbeds(createEmbed("請輸入您的 **Minecraft ID** (輸入 `返回` 來返回)\n" +
                "Please type your **Minecraft ID** (type `cancel` to previous)", 0xe5b849)).queue(messageContent -> {
            QuestionStep step = userProgress.get(userID);
            step.setStep(4);
            step.setMessageID(messageContent.getId());
            userProgress.put(userID, step);
        });
    }

    private void getEnglishNickName(String userID, MessageChannel channel) {
        channel.sendMessageEmbeds(createEmbed("請輸入您的**英文暱稱**\n" +
                "Please type your English nickname", 0xe5b849)).queue(messageContent -> {
            QuestionStep step = userProgress.get(userID);
            step.setStep(5);
            step.setMessageID(messageContent.getId());
            userProgress.put(userID, step);
        });
    }

    private void getChineseNickName(String userID, MessageChannel channel) {
        channel.sendMessageEmbeds(createEmbed("請輸入您的**兩字中文暱稱**\n" +
                "Please type your Two Words Chinese nickname", 0xe5b849)).queue(messageContent -> {
            QuestionStep step = userProgress.get(userID);
            step.setStep(6);
            step.setMessageID(messageContent.getId());
            userProgress.put(userID, step);
        });
    }

    private void FollowRoles(String userID, MessageChannel channel) {
        channel.sendMessageEmbeds(createEmbed("請問您是否會**遵從管理員指示並服從**？\n" +
                "Will you follow the Server Rules and be good?", 0xe5b849)).queue(messageContent -> {
            messageContent.addReaction(emoji.yesEmoji).queue();
            messageContent.addReaction(emoji.noEmoji).queue();
            QuestionStep step = userProgress.get(userID);
            step.setStep(7);
            step.setMessageID(messageContent.getId());
            userProgress.put(userID, step);
        });
    }
}
