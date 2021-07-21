package main.java.event;

import main.java.Main;
import main.java.funtion.JsonFileManager;
import main.java.util.GuildUtil;
import main.java.util.QuestionStep;
import net.dv8tion.jda.api.entities.*;
import net.dv8tion.jda.api.events.guild.member.GuildMemberJoinEvent;
import net.dv8tion.jda.api.events.message.priv.PrivateMessageReceivedEvent;
import net.dv8tion.jda.api.events.message.priv.react.PrivateMessageReactionAddEvent;
import net.dv8tion.jda.api.hooks.ListenerAdapter;
import org.jetbrains.annotations.NotNull;
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
import static main.java.util.EmbedUtil.createEmbed;
import static main.java.util.GuildUtil.guild;
import static main.java.util.GuildUtil.guildID;
import static main.java.util.JsonKeys.*;

public class Join extends ListenerAdapter {
    public static JSONObject memberData;
    private final JsonFileManager memberFile;
    Map<String, QuestionStep> userProgress = new HashMap<>();

    public Join() {
        memberFile = new JsonFileManager(configFolder + "/allMember.json");
        memberData = memberFile.data;
    }

    /**
     * 加入事件
     */

    @Override
    public void onGuildMemberJoin(@NotNull GuildMemberJoinEvent event) {
        if (!event.getGuild().getId().equals(guildID)) return;
        //打開私聊
        event.getUser().openPrivateChannel().queue((channel) -> {
            if (memberData.has(event.getUser().getId())) {
                channel.sendMessage(createEmbed("您已完成過暱稱設定 (Your NickName had been Set)", 0xe5b849)).queue();
                initMemberOnJoin(channel, event.getUser().getId());
                FollowRoles(event.getUser().getId(), channel);
            } else {
                initMemberOnJoin(channel, event.getUser().getId());
            }
        });

    }

    /**
     * 回覆導向 (反應)
     */
    @Override
    public void onPrivateMessageReactionAdd(@NotNull PrivateMessageReactionAddEvent event) {
        super.onPrivateMessageReactionAdd(event);
        String userID = event.getUserId();
        if (userID.equals(Main.botID)) return;

        QuestionStep progress = userProgress.getOrDefault(userID, new QuestionStep(0, null));
        String messageID = event.getMessageId();
        String reaction = event.getReactionEmote().getEmote().getAsMention();

        if (messageID.equals(progress.getMessageID())) {
            switch (progress.getStep()) {
                case 0:
                    event.getChannel().sendMessage(createEmbed("您發現了錯誤, 請回報給工作人員 (You Found A Bug, Please Report To Developers) (ERROR: 001)", 0xFF0000)).queue();
                    break;
                case 2:
                    //yes
                    if (reaction.equals(emoji.yesEmoji.getAsMention())) {
                        progress.playMinecraft = true;
                        showUserInfo(event.getChannel(), event.getUser(), progress);
                        getMinecraftID(userID, event.getChannel());
                        break;
                    }
                    //no
                    else if (reaction.equals(emoji.noEmoji.getAsMention())) {
                        progress.playMinecraft = false;
                        showUserInfo(event.getChannel(), event.getUser(), progress);
                        getEnglishNickName(userID, event.getChannel());
                        break;
                    }
                    break;
                case 7:
                    //yes
                    if (reaction.equals(emoji.yesEmoji.getAsMention())) {
                        //更改nickname和處存資料到檔案
                        Member member = GuildUtil.guild.retrieveMemberById(userID).complete();
                        if (progress.playMinecraft)
                            changeNickName(member, progress.chineseNick, progress.minecraftID);
                        else
                            changeNickName(member, progress.chineseNick, progress.englishNick);
                        //儲存user至檔案
                        saveMember(member);
                        userProgress.remove(userID);
                        event.getChannel().sendMessage(createEmbed("歡迎加入我們~~ (Welcome To Join Us!)", 0x9740b9)).queue();
                        break;
                    }
                    //no
                    else if (reaction.equals(emoji.noEmoji.getAsMention())) {
                        event.getChannel().sendMessage(createEmbed("由於您的抉擇，我們無法讓您加入伺服器 (Because Of Your Choice, We Can Not Let You In!)", 0xFF0000)).queue();
                        userProgress.remove(userID);
                        break;
                    }
            }
        }
    }


    private void changeNickName(Member member, String name, String minecraftID) {
//        String nameSpace = "%name% - %MinecraftID%";
//        member.modifyNickname(progress.chineseNick + " - " + (progress.playMinecraft ? progress.minecraftID : progress.englishNick)).complete();
        member.modifyNickname(name + " - " + minecraftID).queue();
    }


    private void showUserInfo(PrivateChannel privateChannel, User user, QuestionStep progress) {
        if (progress.infoMessageID != null)
            privateChannel.deleteMessageById(progress.infoMessageID).queue();

        String nickname;
        List<MessageEmbed.Field> fields = new ArrayList<>();
        boolean settingDone = true;
        if (progress.chineseNick != null) {
            fields.add(new MessageEmbed.Field(emoji.dot7.getAsMention() + " 中文暱稱: ", progress.chineseNick, false));
            nickname = progress.chineseNick + " - ";
        } else {
            fields.add(new MessageEmbed.Field(emoji.dot7.getAsMention() + " 中文暱稱: ", "未設置", false));
            nickname = "未設置 - ";
            settingDone = false;
        }

        if (progress.playMinecraft) {
            if (progress.minecraftID != null) {
                fields.add(new MessageEmbed.Field(emoji.dot4.getAsMention() + " Minecraft ID: ", progress.minecraftID, false));
                nickname += progress.minecraftID;
            } else {
                fields.add(new MessageEmbed.Field(emoji.dot4.getAsMention() + " Minecraft ID: ", "未設置", false));
                nickname += "未設置";
                settingDone = false;
            }
        } else {
            if (progress.englishNick != null) {
                fields.add(new MessageEmbed.Field(emoji.dot4.getAsMention() + " 英文暱稱: ", progress.englishNick, false));
                nickname += progress.englishNick;
            } else {
                fields.add(new MessageEmbed.Field(emoji.dot4.getAsMention() + " 英文暱稱: ", "未設置", false));
                nickname += "未設置";
                settingDone = false;
            }
        }

        privateChannel.sendMessage(createEmbed(
                "原之序 | ORDERLY SERVER", "伺服器暱稱" + (settingDone ? "已完成設定" : "設定中"),
                "原序之徒 | ORDERLY DISCIPLE",
                nickname, user.getAvatarUrl(),
                fields, OffsetDateTime.now(), 0xCFD3FF
        )).queue(msg -> {
            progress.setInfoMessageID(msg.getId());
        });

    }

    //儲存使用者到資料
    private void saveMember(Member member) {
        QuestionStep progress = userProgress.get(member.getId());

        JSONObject memberInfo = new JSONObject();
        memberInfo.put(PLAY_MINECRAFT, progress.playMinecraft);
        //後面的名字
        if (progress.playMinecraft)
            memberInfo.put(MINECRAFT_ID, progress.minecraftID);
        else
            memberInfo.put(ENGLISH_NICK, progress.englishNick);
        memberInfo.put(CHINESE_NICK, progress.chineseNick);


        System.out.println(memberInfo);
        memberData.put(member.getId(), memberInfo);
        memberFile.saveFile();
    }

    /**
     * 回覆導向 (對話)
     */
    @Override
    public void onPrivateMessageReceived(@NotNull PrivateMessageReceivedEvent event) {
        super.onPrivateMessageReceived(event);
        String userID = event.getAuthor().getId();
        if (userID.equals(Main.botID)) return;
        String message = event.getMessage().getContentRaw();
        PrivateChannel channel = event.getChannel();

        //手動設定
        if (message.equalsIgnoreCase("j")) {
            if (memberData.has(userID)) {
                channel.sendMessage(createEmbed("您已完成過暱稱設定,開始重新設定 (You Have Setting Before, Let's Reset!)", 0xe5b849)).complete();
                initMemberOnJoin(channel, userID);
                //傳送設定檔
                askForMinecraftID(userID, channel, false);
            } else {
                initMemberOnJoin(channel, userID);
            }
            return;
        }

        //問題回答
        QuestionStep progress = userProgress.getOrDefault(userID, new QuestionStep(0, ""));
        List<Message> lastMessage = channel.getHistory().retrievePast(2).complete();
        if (lastMessage.get(1) == null) return;
        String questionMessageID = lastMessage.get(1).getId();
        if (questionMessageID.equals(progress.getMessageID())) {
            switch (progress.getStep()) {
                case 0:
                    channel.sendMessage(createEmbed("您發現了錯誤, 請回報給工作人員 (You Found A Bug, Please Report To Developers) (ERROR: 002)", 0xFF0000)).queue();
                    break;
                case 1:
                    askForMinecraftID(userID, channel, true);
                    break;
                case 3:
                    getEnglishNickName(userID, channel);
                    break;
                case 4:
                    progress.englishNick = null;
                    progress.minecraftID = message;
                    showUserInfo(channel, event.getAuthor(), progress);
                    getChineseNickName(userID, channel);
                    break;
                case 5:
                    progress.englishNick = message;
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
                        channel.sendMessage(createEmbed("輸入錯誤！(Wrong Type)", 0xFF0000)).queue();
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

    private void initMemberOnJoin(PrivateChannel channel, String userID) {
        //已經有user的資料
        if (memberData.has(userID)) {
            //加入以儲存的user資料
            QuestionStep progress = new QuestionStep(0, null);
            JSONObject userData = memberData.getJSONObject(userID);
            //有玩minecraft
            if (progress.playMinecraft = userData.getBoolean(PLAY_MINECRAFT))
                progress.minecraftID = userData.getString(MINECRAFT_ID);
            else
                progress.englishNick = userData.getString(ENGLISH_NICK);
            progress.chineseNick = userData.getString(CHINESE_NICK);
            //加入progress
            userProgress.put(userID, progress);
        } else {
            channel.sendMessage(createEmbed("歡迎您來到 <" + guild.getName() + "> , 在正式進入前還需要通過驗證！(Welcome To Server, Before Your Join, You Must Auth Your Account!)", 0x9740b9)).queue();
            for (Role i : joinRoleID) {
                guild.addRoleToMember(userID, i).queue();
            }
            askForMinecraftID(userID, channel, true);
        }
    }

    private void askForMinecraftID(String userID, PrivateChannel channel, boolean newAccount) {
        channel.sendMessage(createEmbed("請問您是否有遊玩 **Minecraft**？(Have You Ever Played Minecraft?)", 0xe5b849)).queue(messageContent -> {
            messageContent.addReaction(emoji.yesEmoji).queue();
            messageContent.addReaction(emoji.noEmoji).queue();
            //新加入的人
            if (newAccount) {
                QuestionStep step = new QuestionStep(2, channel.getId());
                step.setStep(2);
                step.setMessageID(messageContent.getId());
                userProgress.put(userID, step);
            }
            //修改資料
            else {
                QuestionStep step = userProgress.get(userID);
                step.setStep(2);
                step.setMessageID(messageContent.getId());
            }
        });
    }

    private void getMinecraftID(String userID, PrivateChannel channel) {
        channel.sendMessage(createEmbed("請輸入您的 **Minecraft ID**：(Please Type Your Minecraft ID)", 0xe5b849)).queue(messageContent -> {
            QuestionStep step = userProgress.get(userID);
            step.setStep(4);
            step.setMessageID(messageContent.getId());
            userProgress.put(userID, step);
        });
    }

    private void getEnglishNickName(String userID, PrivateChannel channel) {
        channel.sendMessage(createEmbed("請輸入您的**英文暱稱**：(Please Type Your English Nick)", 0xe5b849)).queue(messageContent -> {
            QuestionStep step = userProgress.get(userID);
            step.setStep(5);
            step.setMessageID(messageContent.getId());
            userProgress.put(userID, step);
        });
    }

    private void getChineseNickName(String userID, PrivateChannel channel) {
        channel.sendMessage(createEmbed("請輸入您的**兩字中文暱稱**：(Please Type Your Two Word Chinese Name)", 0xe5b849)).queue(messageContent -> {
            QuestionStep step = userProgress.get(userID);
            step.setStep(6);
            step.setMessageID(messageContent.getId());
            userProgress.put(userID, step);
        });
    }

    private void FollowRoles(String userID, PrivateChannel channel) {
        channel.sendMessage(createEmbed("請問您是否會**遵從管理員指示並服從**？(Will You Follow The Server Rules And Be Good?)", 0xe5b849)).queue(messageContent -> {
            messageContent.addReaction(emoji.yesEmoji).queue();
            messageContent.addReaction(emoji.noEmoji).queue();
            QuestionStep step = userProgress.get(userID);
            step.setStep(7);
            step.setMessageID(messageContent.getId());
            userProgress.put(userID, step);
        });
    }
}
