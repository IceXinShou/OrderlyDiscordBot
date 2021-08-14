package main.java.command;

import main.java.Main;
import main.java.util.GuildUtil;
import net.dv8tion.jda.api.JDA;
import net.dv8tion.jda.api.entities.Guild;
import net.dv8tion.jda.api.events.guild.GuildReadyEvent;
import net.dv8tion.jda.api.interactions.commands.build.CommandData;
import net.dv8tion.jda.api.interactions.commands.build.OptionData;
import net.dv8tion.jda.api.interactions.commands.build.SubcommandData;
import org.jetbrains.annotations.NotNull;

import java.util.List;

import static main.java.BotSetting.*;
import static main.java.command.list.Invite.authChannel;
import static main.java.command.list.Invite.authChannelID;
import static main.java.event.InformationReaction.internalRole;
import static main.java.event.InformationReaction.logRole;
import static main.java.event.Log.*;
import static main.java.util.SlashCommandOption.*;
import static net.dv8tion.jda.api.interactions.commands.OptionType.*;

public class CommandRegister {

    public void addOwnSlashCommand(@NotNull Guild guild) {
        guild.updateCommands().addCommands(publicCommands()).addCommands(ownCommands()).queue();
    }

    public void addPublicSlashCommand(@NotNull Guild guild) {
        guild.updateCommands().addCommands(publicCommands()).queue();
    }

    public void addCommandEveryWhere(@NotNull JDA jda) {
        jda.updateCommands().addCommands(everywhereCommands()).queue();
    }

    public void onGuildReady(@NotNull GuildReadyEvent event) {
        if (event.getGuild().getId().equals(GuildUtil.guildID))
            getMainGuildVariable(event.getGuild());
        else {
            try {
                addPublicSlashCommand(event.getGuild());
                System.out.println("[" + event.getGuild().getName() + "] Command Updated! (Owner: " + event.getGuild().retrieveOwner().complete().getUser().getAsTag() + ")");
            } catch (Exception e) {
                System.err.println("[" + event.getGuild().getName() + "] Command Update Failed!");
                System.err.println(e.getMessage());
            }
        }
    }

    public void getMainGuildVariable(@NotNull Guild guild) {
        // 註冊全域指令
        addCommandEveryWhere(guild.getJDA());
        // 註冊主要公會指令
        addOwnSlashCommand(guild);

        Main.emoji.loadEmoji(guild);
        // 偵測管理員 ID
//        Role role = guild.getRoleById(adminPermissionID);
//        if (role == null)
//            System.err.println(TAG + " 無法取得管理員權限");
//        else
//            adminPermissionPos = role.getPositionRaw();

        // get guild
        GuildUtil.guild = guild;

        boostedRole = guild.getRoleById(boostedRoleID);

        // get bot nickname in main guild
        Main.botNickname = guild.getSelfMember().getNickname();


        // get LogChannel
        logChannel = guild.getTextChannelById(logChannelID);
        if (logChannel == null)
            System.err.println("無法取得記錄頻道 ID: " + logChannelID);

        // get ConsoleChannel
        consoleChannel = guild.getTextChannelById(consoleChannelID);
        if (consoleChannel == null)
            System.err.println("無法取得控制台頻道 ID: " + consoleChannelID);

        // get AuthenticateChannel
        authChannel = guild.getTextChannelById(authChannelID);
        if (authChannel == null)
            System.err.println("無法取得認證頻道 ID: " + authChannelID);

        // Role
        if (joinRoleID.size() > 0) joinRoleID.clear();
        for (String roleID : (List<String>) IDSettings.get("joinRoleID"))
            joinRoleID.add(guild.getRoleById(roleID));

        if (confirmRoleID.size() > 0) confirmRoleID.clear();
        for (String roleID : (List<String>) IDSettings.get("confirmRoleID"))
            confirmRoleID.add(guild.getRoleById(roleID));

        memberRole = guild.getRoleById(memberRoleID);
        logRole = guild.getRoleById(logRoleID);
        internalRole = guild.getRoleById(internalRoleID);

        System.out.println("Command Variable loaded");
    }


    private CommandData @NotNull [] publicCommands() {
        return new CommandData[]{

                // General
                new CommandData("warn", "警告成員").addOptions(
                        new OptionData(USER, USER_TAG, "封禁你所選的成員", true),
                        new OptionData(STRING, REASON, "原因")
                ),
                new CommandData("remove_warn", "撤銷成員警告").addOption(USER, USER_TAG, "封禁你所選的成員", true),
                new CommandData("kick", "踢出成員").addOption(USER, USER_TAG, "踢出你所選的成員", true),
                new CommandData("ban", "封鎖成員").addOptions(
                        new OptionData(USER, USER_TAG, "封禁你所選的成員", true),
                        new OptionData(INTEGER, DAYS, "時間長度 (天)"),
                        new OptionData(STRING, REASON, "原因")
                ),
                new CommandData("unban", "解除封鎖成員").addOption(STRING, USER_ID, "解除封鎖你所選的成員", true),
                new CommandData("clear", "打掃垃圾").addOption(INTEGER, COUNT, "刪除介於 2 ~ 200 的訊息", true),
                new CommandData("poll", "發起投票").addOptions(
                        new OptionData(STRING, QUESTION, "票選問題", true),
                        new OptionData(STRING, CHOICE_A, "選項 A"),
                        new OptionData(STRING, CHOICE_B, "選項 B"),
                        new OptionData(STRING, CHOICE_C, "選項 C"),
                        new OptionData(STRING, CHOICE_D, "選項 D"),
                        new OptionData(STRING, CHOICE_E, "選項 E"),
                        new OptionData(STRING, CHOICE_F, "選項 F"),
                        new OptionData(STRING, CHOICE_G, "選項 G"),
                        new OptionData(STRING, CHOICE_H, "選項 H"),
                        new OptionData(STRING, CHOICE_I, "選項 I"),
                        new OptionData(STRING, CHOICE_J, "選項 J")),
                new CommandData("help", "顯示幫助列表"),
                new CommandData("helpannouncement", "將幫助列表以公告呈現"),

                // Music
                new CommandData("play", "加入播放音樂").addOption(STRING, NAME, "播放輸入的網址或歌曲名"), // 若未填則開始播放音樂
                new CommandData("pn", "強制播放音樂").addOption(STRING, NAME, "強制插入並播放輸入的網址或歌曲名 (歌單無法插播)", true),
                new CommandData("playnow", "強制播放音樂").addOption(STRING, NAME, "強制插入並播放輸入的網址或歌曲名 (歌單無法插播)", true),
                new CommandData("p", "加入播放音樂").addOption(STRING, NAME, "播放輸入的網址或歌曲名"), // 若未填則開始播放音樂
                new CommandData("queue", "顯示播放列表"),
                new CommandData("q", "顯示播放列表"),
                new CommandData("playing", "顯示播放列表"),
                new CommandData("skip", "切換至下一首"),
                new CommandData("s", "切換至下一首"),
                new CommandData("remove", "移除指定歌曲").addOption(INTEGER, INDEX, "歌曲編號", true),
                new CommandData("volume", "切換音量大小").addOption(INTEGER, COUNT, "未填則預設"), // 若未填則回覆預設
                new CommandData("loop", "循環模式"),
                new CommandData("repeat", "單曲循環模式"),
                new CommandData("pause", "暫停播放"),
                new CommandData("leave", "退出語音頻道"),
                new CommandData("disconnect", "退出語音頻道"),
                new CommandData("stop", "退出語音頻道"),

                // Tool

                new CommandData("surl", "創建短網址").addOption(STRING, URL, "網址", true), // 若未填則回覆預設

                // Advance
                new CommandData("setting", "設定").addSubcommands(
                        new SubcommandData("help", "顯示設定幫助列表"),

                        // -------------------
                        new SubcommandData("newroom", "創建自動化房間").addOptions(
                                new OptionData(CHANNEL, "detectchannel", "偵測頻道", true),
                                new OptionData(STRING, "voicename", "語音名稱(可包含空白鍵, %guild_name%, %user%, %user_name%, %user_tag%, 或 %nickname%)", true),
                                new OptionData(STRING, "textname", "文字名稱(不可包含空白鍵, %guild_name%, %user%, %user_name%, %user_tag%, 或 %nickname%)"),
                                new OptionData(CHANNEL, "voicecategory", "語音頻道目錄"),
                                new OptionData(CHANNEL, "textcategory", "文字頻道目錄"),
                                new OptionData(INTEGER, "voicebitrate", "語音位元率 (kbps)"),
                                new OptionData(INTEGER, "memberlimit", "語音人數限制 (1~99)")
                        ),

                        new SubcommandData("removeroom", "移除自動化房間").addOption(CHANNEL, "detectchannel", "偵測頻道", true),

                        // -------------------
                        new SubcommandData("newautovc", "創建彈性語音").addOptions(
                                new OptionData(CHANNEL, "detectcategory", "偵測目錄", true),
                                new OptionData(STRING, "voicename", "語音名稱", true)
                        ),

                        new SubcommandData("removeautovc", "移除彈性語音").addOption(CHANNEL, "detectcategory", "偵測目錄", true),

                        // -------------------
                        new SubcommandData("newticket", "創建客服按鈕").addOptions(
                                new OptionData(STRING, "messageid", "主要內容訊息ID", true),
                                new OptionData(CHANNEL, "messagechannel", "主要內容訊息頻道", true),
                                new OptionData(STRING, "enteredmessage", "頻道創建時發送的訊息 (%num%, %role%, %role_name%, %guild_name%, %user%, %user_name%, %user_tag%, 或 %nickname%)", true),
                                new OptionData(STRING, "textname", "新文字頻道名稱 (%num%, %guild_name%, %user%, %user_name%, %user_tag%, 或 %nickname%)"),
                                new OptionData(CHANNEL, "textcategory", "新文字頻道目錄"),
                                new OptionData(BOOLEAN, "hasvoicechannel", "是否有語音頻道"),
                                new OptionData(STRING, "voicename", "新語音頻道名稱 (%num%, %guild_name%, %user%, %user_name%, %user_tag%, 或 %nickname%)"),
                                new OptionData(CHANNEL, "voicecategory", "新語音頻道目錄"),
                                new OptionData(STRING, "buttonname", "按鈕名稱"),
                                new OptionData(STRING, "buttoncolor", "按鈕顏色 (red, orange, blue 或 gray)"),
                                new OptionData(STRING, "buttonemoji", "按鈕圖示"),
                                new OptionData(ROLE, "allowrole", "除了觸發者以外, 允許檢視此頻道的身分組"),
                                new OptionData(BOOLEAN, "allowtagrole", "觸發時是否 tag 此身分組")
                        ),

                        new SubcommandData("addticket", "添加客服按鈕").addOptions(
                                new OptionData(STRING, "messageid", "需添加的訊息ID", true),
                                new OptionData(CHANNEL, "messagechannel", "需添加的訊息頻道", true),
                                new OptionData(STRING, "enteredmessage", "頻道創建時發送的訊息 (%num%, %role%, %role_name%, %guild_name%, %user%, %user_name%, %user_tag%, 或 %nickname%)", true),
                                new OptionData(STRING, "textname", "新文字頻道名稱 (%num%, %guild_name%, %user%, %user_name%, %user_tag%, 或 %nickname%)"),
                                new OptionData(CHANNEL, "textcategory", "新文字頻道目錄"),
                                new OptionData(BOOLEAN, "hasvoicechannel", "是否有語音頻道"),
                                new OptionData(STRING, "voicename", "新語音頻道名稱 (%num%, %guild_name%, %user%, %user_name%, %user_tag%, 或 %nickname%)"),
                                new OptionData(CHANNEL, "voicecategory", "新語音頻道目錄"),
                                new OptionData(STRING, "buttonname", "按鈕名稱"),
                                new OptionData(STRING, "buttoncolor", "按鈕顏色 (red, orange, blue 或 gray)"),
                                new OptionData(STRING, "buttonemoji", "按鈕圖示"),
                                new OptionData(ROLE, "allowrole", "除了觸發者以外, 允許檢視此頻道的身分組"),
                                new OptionData(BOOLEAN, "allowtagrole", "觸發時是否 tag 此身分組")
                        ),

                        new SubcommandData("removeticket", "移除客服按鈕").addOptions(
                                new OptionData(STRING, "messageid", "主要內容訊息ID", true),
                                new OptionData(CHANNEL, "messagechannel", "主要內容訊息頻道", true),
                                new OptionData(STRING, "position", "按鈕位置 (1~5)", true)
                        ),

                        // -------------------
                        new SubcommandData("newchannelstatus", "新增頻道顯示數據").addOptions(
                                new OptionData(CHANNEL, "channel", "顯示資訊頻道", true),
                                new OptionData(STRING, "channelname", "頻道名稱 (請使用 /setting help 查看所有佔位符 (17))", true),
                                new OptionData(INTEGER, "format", "格式化設定, 最終結果將保留幾位小數點 (0~10)", true)
                        ),
                        new SubcommandData("removechannelstatus", "移除頻道顯示數據").addOption(CHANNEL, "channel", "顯示資訊的頻道", true),

                        // -------------------
                        new SubcommandData("newjoin", "新增加入訊息").addOptions(
                                new OptionData(CHANNEL, "channel", "顯示訊息頻道", true),
                                new OptionData(STRING, "message", "訊息內容 ()", true)
                        ),
                        new SubcommandData("removejoin", "移除加入訊息").addOption(CHANNEL, "channel", "顯示訊息頻道", true),

                        // -------------------
                        new SubcommandData("newleave", "新增退出訊息").addOptions(
                                new OptionData(CHANNEL, "channel", "顯示訊息頻道", true),
                                new OptionData(STRING, "message", "訊息內容 ()", true)
                        ),
                        new SubcommandData("removeleave", "移除退出訊息").addOption(CHANNEL, "channel", "顯示訊息頻道", true)

                        // -------------------
                ),
        };
    }

    private CommandData @NotNull [] ownCommands() {
        return new CommandData[]{
                new CommandData("reload", "重新載入機器人"),
                new CommandData("nick", "更改專屬伺服器暱稱"),
                new CommandData("invite", "邀請成員").addOption(USER, USER_TAG, "將你選的成員塞進伺服器或頻道", true),
        };
    }

    private CommandData @NotNull [] everywhereCommands() {
        return new CommandData[]{
                new CommandData("ping", "延遲測試"),
                new CommandData("support", "傳送問題回報").addOption(STRING, MESSAGE, "訊息內容", true),
                new CommandData("botinfo", "顯示機器人訊息"),
        };
    }
}
