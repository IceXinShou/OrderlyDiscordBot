package main.java;

import main.java.command.*;
import main.java.util.GuildUtil;
import multiBot.MultiMusicBotManager;
import net.dv8tion.jda.api.JDA;
import net.dv8tion.jda.api.entities.Guild;
import net.dv8tion.jda.api.entities.Role;
import net.dv8tion.jda.api.events.guild.GuildJoinEvent;
import net.dv8tion.jda.api.events.guild.GuildReadyEvent;
import net.dv8tion.jda.api.events.guild.member.update.GuildMemberUpdateBoostTimeEvent;
import net.dv8tion.jda.api.events.interaction.ButtonClickEvent;
import net.dv8tion.jda.api.events.interaction.SelectionMenuEvent;
import net.dv8tion.jda.api.events.interaction.SlashCommandEvent;
import net.dv8tion.jda.api.hooks.ListenerAdapter;
import net.dv8tion.jda.api.interactions.commands.OptionMapping;
import net.dv8tion.jda.api.interactions.commands.build.CommandData;
import net.dv8tion.jda.api.interactions.commands.build.OptionData;
import net.dv8tion.jda.api.requests.restaction.CommandListUpdateAction;
import org.jetbrains.annotations.NotNull;

import java.time.OffsetDateTime;
import java.util.List;

import static main.java.BotSetting.*;
import static main.java.SlashCommandOption.*;
import static main.java.automatic.InformationReaction.internalRole;
import static main.java.automatic.InformationReaction.logRole;
import static main.java.command.Invite.authChannel;
import static main.java.command.Invite.authChannelID;
import static main.java.command.VoiceChannel.voiceChannelData;
import static main.java.event.Log.*;
import static main.java.util.Funtions.createEmbed;
import static main.java.util.Funtions.tagChannel;
import static main.java.util.GuildUtil.guild;
import static main.java.util.GuildUtil.guildID;
import static main.java.util.JsonKeys.TEXT_CHANNEL_ID;
import static net.dv8tion.jda.api.interactions.commands.OptionType.*;

/**
 * 這些指令最多需要 1 小時時間做更新
 */

public class SlashCommandManager extends ListenerAdapter {
    private final String TAG = "[SlashCommandManager]";

    Integer adminPermissionPos;
    Ban banCommand;
    Clear clearCommand;
    Invite createInviteCommand;
    VoiceChannel voiceChannelCommand;
    UnBan unBanCommand;
    Kick kickCommand;
    MultiMusicBotManager musicManager;
    Poll pollCommand;
    Help helpCommand;
    BotInfo botInfo;
    Support support;

    SlashCommandManager() {
        banCommand = new Ban();
        unBanCommand = new UnBan();
        clearCommand = new Clear();
        createInviteCommand = new Invite();
        voiceChannelCommand = new VoiceChannel();
        kickCommand = new Kick();
        pollCommand = new Poll();
        helpCommand = new Help();
        botInfo = new BotInfo();
        support = new Support();
        System.out.println(TAG + " Listener loaded!");
    }

    @Override
    public void onSlashCommand(SlashCommandEvent event) {
        event.deferReply(true).queue();


        // 如果找不到伺服器 ->

        if (event.getGuild() == null) {
            if (debugMode) {
                List<OptionMapping> options = event.getOptions();
                StringBuilder builder = new StringBuilder();
                for (OptionMapping option : options) {
                    builder.append(" ").append(option.getAsString());
                }

                System.out.println("[Private] " + event.getUser().getAsTag() + " issued command: /" + event.getName() + builder);
            }


            switch (event.getName()) {
                case "ping" -> {
                    event.getHook().editOriginalEmbeds(createEmbed("Ping: " + event.getJDA().getGatewayPing(), 0x00FFFF)).queue();
                    return;
                }
                case "support" -> {
                    support.onMemberCommand(event);
                    return;
                }
                case "botinfo" -> {
                    botInfo.onCommand(event);
                    return;
                }
            }
            event.getHook().editOriginalEmbeds(createEmbed("目前無法處理此命令", 0xFF0000)).queue();
        }

        if (debugMode) {
            List<OptionMapping> options = event.getOptions();
            StringBuilder builder = new StringBuilder();
            for (OptionMapping option : options) {
                builder.append(" ").append(option.getAsString());
            }

            System.out.println("[" + event.getGuild().getName() + "] " + event.getUser().getAsTag() + " issued command: /" + event.getName() + builder);
        }

        // 取得輸入指令的頻道
        String channelID = event.getChannel().getId();

        int type;
        // 語音指令
//        if (!channelID.equals(authChannelID)) {
//            type = voiceChannelCommand.onVoiceChannelCommand(event);
//            if (type == -1) { // 輸入的頻道錯誤
//                return;
//            } else if (type == 1) { // 已經執行完成並 return
//                return;
//            }
//        }

        type = musicManager.onCommand(event);
        if (type == -1) { // 輸入的頻道錯誤
            return;
        } else if (type == 1) { // 已經執行完成並 return
            return;
        }

        // 邀請
        if (event.getName().equals("invite")) {
            if (channelID.equals(authChannelID))
                createInviteCommand.onCommand(event);
            else {
                event.getHook().editOriginalEmbeds(createEmbed("請到指定位置使用此指令 (" +
                                tagChannel(authChannelID) + ", " +
                                tagChannel(voiceChannelData.getJSONObject(event.getId()).getString(TEXT_CHANNEL_ID)) + ")", 0xFF0000))
                        .queue();
            }
            return;
        }


        // 其他指令
        switch (event.getName()) {
            // 全域頻道
            case "ban" -> {
                banCommand.onCommand(event);
                return;
            }
            case "unban" -> {
                unBanCommand.onCommand(event);
                return;
            }
            case "kick" -> {
                kickCommand.onCommand(event);
                return;
            }
            case "clear" -> {
                clearCommand.onCommand(event);
                return;
            }
            case "poll" -> {
                pollCommand.onCommand(event);
                return;
            }
            case "help" -> {
                helpCommand.onMemberCommand(event);
                return;
            }
            case "ping" -> {
                event.getHook().editOriginalEmbeds(createEmbed("Pong!  \uD83C\uDFD3",
                        "⌛ : xx ms\n\n⏱️ :  ms", "", "", "", OffsetDateTime.now(), 0x00FFFF)).queue(
                        i -> event.getHook().editOriginalEmbeds(createEmbed("Pong!  \uD83C\uDFD3",
                                        "⌛ : " + (Integer.parseInt(String.valueOf(i.getTimeCreated().toInstant().toEpochMilli() -
                                                event.getInteraction().getTimeCreated().toInstant().toEpochMilli() -
                                                event.getJDA().getGatewayPing())) < 0 ? "1" : Integer.parseInt(String.valueOf(i.getTimeCreated().toInstant().toEpochMilli() -
                                                event.getInteraction().getTimeCreated().toInstant().toEpochMilli() -
                                                event.getJDA().getGatewayPing()))) +
                                                " ms\n\n⏱️ : " +
                                                event.getJDA().getGatewayPing() + " ms", "", "", "", OffsetDateTime.now(), 0x00FFFF))
                                .queue()
                );
                return;
            }
            case "botinfo" -> {
                botInfo.onCommand(event);
                return;
            }
            case "support" -> {
                support.onMemberCommand(event);
                return;
            }
            case "announcement" -> {
                helpCommand.onAnnouncementCommand(event);
                return;
            }
        }
        event.getHook().editOriginalEmbeds(createEmbed("目前無法處理此命令", 0xFF0000)).queue();
    }

    @Override
    public void onButtonClick(ButtonClickEvent event) {
        // button的id
        String[] args = event.getComponentId().split(":");
        String authorId = args[0];
        // 確認按按鈕的人是誰
        if (!authorId.equals(event.getUser().getId()))
            return;
//        if (!args[1].equals("nevermind")) {
        clearCommand.onButton(event, args);
        createInviteCommand.onButton(event, args);
        voiceChannelCommand.onButton(event, args);
        musicManager.onButton(event, args);
//        } else
//            event.deferEdit().queue(); // acknowledge the button was clicked, otherwise the interaction will fail
    }

    @Override
    public void onSelectionMenu(@NotNull SelectionMenuEvent event) {
        String[] args = event.getComponentId().split(":");
        if (args[1].equals("searchResult")) {
            musicManager.onSelectMenu(event, args);
        }
    }

    @Override
    public void onGuildReady(@NotNull GuildReadyEvent event) {
        if (event.getGuild().getId().equals(guildID)) {
            getGuildVariable(event.getGuild());
            musicManager = new MultiMusicBotManager();
        } else {
            System.out.println("[" + event.getGuild().getName() + "] Command Loading...");
            addPublicSlashCommand(event.getGuild());
            System.out.println("[" + event.getGuild().getName() + "] Command Updated!");
        }
    }

    @Override
    public void onGuildMemberUpdateBoostTime(@NotNull GuildMemberUpdateBoostTimeEvent event) {
        if (!event.getMember().getRoles().contains(boostedRole)) {
            guild.addRoleToMember(event.getMember(), boostedRole).queue();
        }
    }

    @Override
    public void onGuildJoin(@NotNull GuildJoinEvent event) {

        if (event.getGuild().getId().equals(guildID))
            addOwnSlashCommand(event.getGuild());
        else
            addPublicSlashCommand(event.getGuild());
        try {
            event.getGuild().getOwner().getUser().openPrivateChannel().queue(i ->
                    i.sendMessageEmbeds(createEmbed("您已邀請 <**" +
                            event.getGuild().getSelfMember().getUser().getAsTag() +
                            "**> 進入 <**" + event.getGuild().getName() + "**>\n" +
                            "You have invited <**" + event.getGuild().getSelfMember().getUser().getAsTag() +
                            "**> join <**" + event.getGuild().getName() +
                            "**> Discord Server", "", "", "", "", helpCommand.summonMemberFields(null, true), OffsetDateTime.now(), 0x00FFFF)).queue());
        } catch (Exception e) {
            System.err.println(e.getMessage());
        }
    }

    public void getGuildVariable(Guild guild) {
        // 註冊全域指令
        addCommandEveryWhere(guild.getJDA());

        // 註冊主公會指令
        addOwnSlashCommand(guild);
        Main.emoji.loadEmoji(guild);
        // 偵測管理員 ID
        Role role = guild.getRoleById(adminPermissionID);
        if (role == null)
            System.err.println(TAG + " 無法取得管理員權限");
        else
            adminPermissionPos = role.getPositionRaw();

        // get guild
        GuildUtil.guild = guild;

        boostedRole = guild.getRoleById(boostedRoleID);

        // get bot nickname in main guild
        Main.botNickname = guild.getSelfMember().getNickname();


        // get LogChannel
        logChannel = guild.getTextChannelById(logChannelID);
        if (logChannel == null)
            System.err.println(TAG + " 無法取得記錄頻道 ID: " + logChannelID);
        // get ConsoleChannel
        consoleChannel = guild.getTextChannelById(consoleChannelID);
        if (consoleChannel == null)
            System.err.println(TAG + " 無法取得控制台頻道 ID: " + consoleChannelID);

        // get AuthenticateChannel
        authChannel = guild.getTextChannelById(authChannelID);
        if (authChannel == null)
            System.err.println(TAG + " 無法取得認證頻道 ID: " + authChannelID);

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

        System.out.println(TAG + " Command Variable loaded");
    }

    private void addOwnSlashCommand(Guild guild) {

        CommandListUpdateAction command = guild.updateCommands();

//        command.addCommands(
//                new CommandData("promote", "提拔成員成為房間管理員")
//                        .addOptions(new OptionData(USER, USER_TAG, "拉起來吧")
//                                .setRequired(true))
//        );
//        command.addCommands(
//                new CommandData("unpromote", "移除房間管理員權限")
//                        .addOptions(new OptionData(USER, USER_TAG, "壓下去吧")
//                                .setRequired(true))
//        );
//        command.addCommands(
//                new CommandData("private", "將你所在的房間改為私人")
//        );
//        command.addCommands(
//                new CommandData("public", "將你所在的房間改為公開")
//        );
//        command.addCommands(
//                new CommandData("info", "列出房間數據")
//                        .addOptions(new OptionData(CHANNEL, CHANNEL_TAG, "頻道")
//                                .setRequired(false)) // 若未填則列出所在頻道數據
//        );
//        command.addCommands(
//                new CommandData("state", "列出房間數據")
//                        .addOptions(new OptionData(CHANNEL, CHANNEL_TAG, "頻道")
//                                .setRequired(false)) // 若未填則列出所在頻道數據
//        );
//        command.addCommands(
//                new CommandData("status", "列出房間數據")
//                        .addOptions(new OptionData(CHANNEL, CHANNEL_TAG, "頻道")
//                                .setRequired(false)) // 若未填則列出所在頻道數據
//        );
//        command.addCommands(
//                new CommandData("black", "將成員設入房間黑名單")
//                        .addOptions(new OptionData(USER, USER_TAG, "不要再進來煩了!")
//                                .setRequired(true))
//        );
//        command.addCommands(
//                new CommandData("remove", "移除成員房間間黑名單")
//                        .addOptions(new OptionData(USER, USER_TAG, "不要再進來煩了!")
//                                .setRequired(true))
//        );
        command.addCommands(
                new CommandData("warn", "警告成員")
                        .addOptions(new OptionData(USER, USER_TAG, "封禁你所選的成員")
                                .setRequired(true))
                        .addOptions(new OptionData(STRING, REASON, "原因")
                                .setRequired(false)) // 若未填則無原因
        );
        command.addCommands(
                new CommandData("remove_warn", "撤銷成員警告")
                        .addOptions(new OptionData(USER, USER_TAG, "封禁你所選的成員")
                                .setRequired(true))
        );
        command.addCommands(
                new CommandData("invite", "邀請成員")
                        .addOptions(new OptionData(USER, USER_TAG, "將你選的成員塞進伺服器或頻道")
                                .setRequired(true))
        );
        command.addCommands(
                new CommandData("clear", "打掃垃圾")
                        .addOptions(new OptionData(INTEGER, COUNT, "刪除介於 2 ~ 200 的訊息")
                                .setRequired(true))
        );
        command.addCommands(
                new CommandData("ban", "封鎖成員")
                        .addOptions(new OptionData(USER, USER_TAG, "封禁你所選的成員")
                                .setRequired(true))
                        .addOptions(new OptionData(INTEGER, DAYS, "時間長度 (天)")
                                .setRequired(false)) // 若未填則永久
                        .addOptions(new OptionData(STRING, REASON, "原因")
                                .setRequired(false)) // 若未填則無原因
        );
        command.addCommands(
                new CommandData("unban", "解除封鎖成員")
                        .addOptions(new OptionData(STRING, USER_ID, "解除封鎖你所選的成員")
                                .setRequired(true))
        );
        command.addCommands(
                new CommandData("play", "加入播放音樂")
                        .addOptions(new OptionData(STRING, NAME, "播放輸入的網址或歌曲名")
                                .setRequired(false)) // 若未填則開始播放音樂
        );
        command.addCommands(
                new CommandData("p", "加入播放音樂")
                        .addOptions(new OptionData(STRING, NAME, "播放輸入的網址或歌曲名")
                                .setRequired(false)) // 若未填則開始播放音樂
        );
        command.addCommands(
                new CommandData("queue", "顯示播放列表")
        );
        command.addCommands(
                new CommandData("skip", "切換至下一首")
//                        .addOptions(new OptionData(INTEGER, COUNT, "跳過至第幾首")
//                                .setRequired(false)) // 若未填則至下一首
        );
        command.addCommands(
                new CommandData("s", "切換至下一首")
//                        .addOptions(new OptionData(INTEGER, COUNT, "跳過至第幾首")
//                                .setRequired(false)) // 若未填則至下一首
        );
        command.addCommands(
                new CommandData("volume", "切換音量大小")
                        .addOptions(new OptionData(INTEGER, COUNT, "未填則預設")
                                .setRequired(false)) // 若未填則回覆預設
        );
        command.addCommands(
                new CommandData("loop", "循環模式")
        );
        command.addCommands(
                new CommandData("repeat", "單曲循環模式")
        );
        command.addCommands(
                new CommandData("pause", "暫停播放")
        );
        command.addCommands(
                new CommandData("poll", "發起投票")
                        .addOptions(new OptionData(STRING, QUESTION, "票選問題")
                                .setRequired(true))
                        .addOptions(new OptionData(STRING, CHOICE_A, "選項 A"))
                        .addOptions(new OptionData(STRING, CHOICE_B, "選項 B"))
                        .addOptions(new OptionData(STRING, CHOICE_C, "選項 C"))
                        .addOptions(new OptionData(STRING, CHOICE_D, "選項 D"))
                        .addOptions(new OptionData(STRING, CHOICE_E, "選項 E"))
                        .addOptions(new OptionData(STRING, CHOICE_F, "選項 F"))
                        .addOptions(new OptionData(STRING, CHOICE_G, "選項 G"))
                        .addOptions(new OptionData(STRING, CHOICE_H, "選項 H"))
                        .addOptions(new OptionData(STRING, CHOICE_I, "選項 I"))
                        .addOptions(new OptionData(STRING, CHOICE_J, "選項 J"))
        );
        command.addCommands(
                new CommandData("help", "顯示幫助列表")
        );
        command.addCommands(
                new CommandData("playing", "顯示播放列表")
        );
        command.addCommands(
                new CommandData("leave", "退出語音頻道")
        );
        command.addCommands(
                new CommandData("disconnect", "退出語音頻道")
        );
        command.addCommands(
                new CommandData("stop", "退出語音頻道")
        );
        command.addCommands(
                new CommandData("announcement", "將機器人使用教學傳送至此頻道")
        );
        command.addCommands(
                new CommandData("remove", "移除指定歌曲")
                        .addOptions(new OptionData(INTEGER, INDEX, "歌曲編號")
                        .setRequired(true)) // 若未填則回覆預設
        );

        command.queue();
    }

    private void addPublicSlashCommand(Guild guild) {

        CommandListUpdateAction command = guild.updateCommands();

        command.addCommands(
                new CommandData("clear", "打掃垃圾")
                        .addOptions(new OptionData(INTEGER, COUNT, "刪除介於 2 ~ 200 的訊息")
                                .setRequired(true))
        );
        command.addCommands(
                new CommandData("ban", "封鎖成員")
                        .addOptions(new OptionData(USER, USER_TAG, "封禁你所選的成員")
                                .setRequired(true))
                        .addOptions(new OptionData(INTEGER, DAYS, "時間長度 (天)")
                                .setRequired(false)) // 若未填則永久
                        .addOptions(new OptionData(STRING, REASON, "原因")
                                .setRequired(false)) // 若未填則無原因
        );
        command.addCommands(
                new CommandData("unban", "解除封鎖成員")
                        .addOptions(new OptionData(STRING, USER_ID, "解除封鎖你所選的成員")
                                .setRequired(true))
        );
        command.addCommands(
                new CommandData("play", "加入播放音樂")
                        .addOptions(new OptionData(STRING, NAME, "播放輸入的網址或歌曲名")
                                .setRequired(false)) // 若未填則開始播放音樂
        );
        command.addCommands(
                new CommandData("p", "加入播放音樂")
                        .addOptions(new OptionData(STRING, NAME, "播放輸入的網址或歌曲名")
                                .setRequired(false)) // 若未填則開始播放音樂
        );
        command.addCommands(
                new CommandData("queue", "顯示播放列表")
        );
        command.addCommands(
                new CommandData("skip", "切換至下一首")
//                        .addOptions(new OptionData(INTEGER, COUNT, "跳過至第幾首")
//                                .setRequired(false)) // 若未填則至下一首
        );
        command.addCommands(
                new CommandData("s", "切換至下一首")
//                        .addOptions(new OptionData(INTEGER, COUNT, "跳過至第幾首")
//                                .setRequired(false)) // 若未填則至下一首
        );
        command.addCommands(
                new CommandData("volume", "切換音量大小")
                        .addOptions(new OptionData(INTEGER, COUNT, "未填則預設")
                                .setRequired(false)) // 若未填則回覆預設
        );
        command.addCommands(
                new CommandData("loop", "循環模式")
        );
        command.addCommands(
                new CommandData("repeat", "單曲循環模式")
        );
        command.addCommands(
                new CommandData("pause", "暫停播放")
        );
        command.addCommands(
                new CommandData("poll", "發起投票")
                        .addOptions(new OptionData(STRING, QUESTION, "票選問題")
                                .setRequired(true))
                        .addOptions(new OptionData(STRING, CHOICE_A, "選項 A"))
                        .addOptions(new OptionData(STRING, CHOICE_B, "選項 B"))
                        .addOptions(new OptionData(STRING, CHOICE_C, "選項 C"))
                        .addOptions(new OptionData(STRING, CHOICE_D, "選項 D"))
                        .addOptions(new OptionData(STRING, CHOICE_E, "選項 E"))
                        .addOptions(new OptionData(STRING, CHOICE_F, "選項 F"))
                        .addOptions(new OptionData(STRING, CHOICE_G, "選項 G"))
                        .addOptions(new OptionData(STRING, CHOICE_H, "選項 H"))
                        .addOptions(new OptionData(STRING, CHOICE_I, "選項 I"))
                        .addOptions(new OptionData(STRING, CHOICE_J, "選項 J"))
        );
        command.addCommands(
                new CommandData("help", "顯示幫助列表")
        );
        command.addCommands(
                new CommandData("playing", "顯示播放列表")
        );
        command.addCommands(
                new CommandData("leave", "退出語音頻道")
        );
        command.addCommands(
                new CommandData("disconnect", "退出語音頻道")
        );
        command.addCommands(
                new CommandData("stop", "退出語音頻道")
        );
        command.addCommands(
                new CommandData("announcement", "將機器人使用教學傳送至此頻道")
        );
        //        command.addCommands(
//                new CommandData("playnow", "強制播放音樂")
//                        .addOptions(new OptionData(STRING, NAME, "強制插入並播放輸入的網址或歌曲名")
//                                .setRequired(true))
//        );
//        command.addCommands(
//                new CommandData("remove", "移除音樂")
//                        .addOptions(new OptionData(INTEGER, WHICH, "移除全部或是指定的歌曲")
//                                .setRequired(false)) // 若未填則移除全部歌曲並繼續播放音樂
//        );
//        command.addCommands(
//                new CommandData("random", "將歌單洗牌")
//        );
//        command.addCommands(
//                new CommandData("shuffle", "將歌單洗牌")
//        );

        command.addCommands(
                new CommandData("remove", "移除指定歌曲")
                        .addOptions(new OptionData(INTEGER, INDEX, "歌曲編號")
                                .setRequired(true)) // 若未填則回覆預設
        );
        command.complete();

    }

    public void addCommandEveryWhere(JDA jda) {

        CommandListUpdateAction command = jda.updateCommands();
        command.addCommands(
                new CommandData("nick", "更改專屬伺服器暱稱").setDefaultEnabled(false)
        );
        command.addCommands(
                new CommandData("join", "填寫專屬伺服器加入申請").setDefaultEnabled(false)
        );
        command.addCommands(
                new CommandData("ping", "延遲測試")
        );
        command.addCommands(
                new CommandData("support", "傳送問題回報")
                        .addOptions(new OptionData(STRING, MESSAGE, "訊息內容").setRequired(true))
        );
        command.addCommands(
                new CommandData("botinfo", "顯示機器人訊息")
        );

        command.queue();
    }
}