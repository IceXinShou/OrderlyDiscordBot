package main.java.command;

import main.java.Main;
import main.java.util.GuildUtil;
import net.dv8tion.jda.api.JDA;
import net.dv8tion.jda.api.entities.Guild;
import net.dv8tion.jda.api.events.guild.GuildReadyEvent;
import net.dv8tion.jda.api.interactions.commands.build.CommandData;
import net.dv8tion.jda.api.interactions.commands.build.OptionData;
import net.dv8tion.jda.api.interactions.commands.build.SubcommandData;

import java.util.List;

import static main.java.BotSetting.*;
import static main.java.command.list.Invite.authChannel;
import static main.java.command.list.Invite.authChannelID;
import static main.java.event.InformationReaction.internalRole;
import static main.java.event.InformationReaction.logRole;
import static main.java.event.Log.*;
import static main.java.lang.LangKey.*;
import static main.java.util.GuildUtil.guildID;
import static main.java.util.SlashCommandOption.*;
import static net.dv8tion.jda.api.interactions.commands.OptionType.*;

public class CommandRegister {

    public void addOwnSlashCommand(Guild guild) {
        guild.updateCommands().addCommands(publicCommands(guild.getId())).addCommands(ownCommands(guild.getId())).queue();
    }

    public void addPublicSlashCommand(Guild guild) {
        guild.updateCommands().addCommands(publicCommands(guild.getId())).queue();
    }

    public void addCommandEveryWhere(JDA jda) {
        jda.updateCommands().addCommands(everywhereCommands()).queue();
    }

    public void onGuildReady(GuildReadyEvent event) {
        if (event.getGuild().getId().equals(guildID))
            getMainGuildVariable(event.getGuild());
        else if (event.getGuild().getId().equals("817940392418607124")) // sub emoji library
            Main.emoji.loadSubEmoji(event.getGuild());
        else {
            try {
                addPublicSlashCommand(event.getGuild());
            } catch (Exception e) {
                System.err.printf("[%s%n] Command Update Failed!", event.getGuild().getName());
                System.err.println(e.getMessage());
            }
        }
    }

    public void getMainGuildVariable(Guild guild) {
        // 註冊全域指令
        addCommandEveryWhere(guild.getJDA());
        // 註冊主要公會指令
        addOwnSlashCommand(guild);

        Main.emoji.loadMainEmoji(guild);
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


    private CommandData[] publicCommands(String guildID) {
        List<String> lang = Main.language.getGuildLang(guildID);
        return new CommandData[]{

                // General
//                new CommandData("warn", lang.get(COMMANDREGISTER_WARN)).addOptions(
//                        new OptionData(USER, USER_TAG, lang.get(COMMANDREGISTER_WARN_MEMBER_YOU_CHOOSE), true),
//                        new OptionData(STRING, REASON, lang.get(COMMANDREGISTER_WARN_REASON))
//                ),
//                new CommandData("remove_warn", lang.get(COMMANDREGISTER_REMOVE_WARM))
//                      .addOption(USER, USER_TAG, lang.get(COMMANDREGISTER_REMOVE_MEMBER_WARN_YOU_CHOOSE), true),
                new CommandData("kick", lang.get(COMMANDREGISTER_KICK))
                        .addOption(USER, USER_TAG, lang.get(COMMANDREGISTER_KICK_MEMBER_YOU_CHOOSE), true),
                new CommandData("ban", lang.get(COMMANDREGISTER_BAN)).addOptions(
                        new OptionData(USER, USER_TAG, lang.get(COMMANDREGISTER_BAN_MEMBER_YOU_CHOOSE), true),
                        new OptionData(INTEGER, DAYS, lang.get(COMMANDREGISTER_BAN_TIME)),
                        new OptionData(STRING, REASON, lang.get(COMMANDREGISTER_BAN_REASON))
                ),
                new CommandData("unban", lang.get(COMMANDREGISTER_UNBAN))
                        .addOption(STRING, USER_ID, lang.get(COMMANDREGISTER_UNBAN_MEMBER_YOU_CHOOSE), true),
                new CommandData("clear", lang.get(COMMANDREGISTER_CLEAR))
                        .addOption(INTEGER, COUNT, lang.get(COMMANDREGISTER_CLEAR_BETWEEN_2_TO_200), true),
                new CommandData("poll", lang.get(COMMANDREGISTER_POLL)).addOptions(
                        new OptionData(STRING, QUESTION, lang.get(COMMANDREGISTER_POLL_QUESTION), true),
                        new OptionData(STRING, CHOICE_A, lang.get(COMMANDREGISTER_POLL_OPTION_A)),
                        new OptionData(STRING, CHOICE_B, lang.get(COMMANDREGISTER_POLL_OPTION_B)),
                        new OptionData(STRING, CHOICE_C, lang.get(COMMANDREGISTER_POLL_OPTION_C)),
                        new OptionData(STRING, CHOICE_D, lang.get(COMMANDREGISTER_POLL_OPTION_D)),
                        new OptionData(STRING, CHOICE_E, lang.get(COMMANDREGISTER_POLL_OPTION_E)),
                        new OptionData(STRING, CHOICE_F, lang.get(COMMANDREGISTER_POLL_OPTION_F)),
                        new OptionData(STRING, CHOICE_G, lang.get(COMMANDREGISTER_POLL_OPTION_G)),
                        new OptionData(STRING, CHOICE_H, lang.get(COMMANDREGISTER_POLL_OPTION_H)),
                        new OptionData(STRING, CHOICE_I, lang.get(COMMANDREGISTER_POLL_OPTION_I)),
                        new OptionData(STRING, CHOICE_J, lang.get(COMMANDREGISTER_POLL_OPTION_J))),

                // Music
                new CommandData("play", lang.get(COMMANDREGISTER_PLAY))
                        .addOption(STRING, NAME, lang.get(COMMANDREGISTER_PLAY_NAME_OR_URL), true), // 若未填則開始播放音樂
                new CommandData("p", lang.get(COMMANDREGISTER_PLAY))
                        .addOption(STRING, NAME, lang.get(COMMANDREGISTER_PLAY_NAME_OR_URL), true), // 若未填則開始播放音樂
                new CommandData("playnow", lang.get(COMMANDREGISTER_PLAYNOW))
                        .addOption(STRING, NAME, lang.get(COMMANDREGISTER_PN_NAME_OR_URL), true),
                new CommandData("pn", lang.get(COMMANDREGISTER_PLAYNOW))
                        .addOption(STRING, NAME, lang.get(COMMANDREGISTER_PN_NAME_OR_URL), true),
                new CommandData("queue", lang.get(COMMANDREGISTER_QUEUE)),
                new CommandData("q", lang.get(COMMANDREGISTER_QUEUE)),
                new CommandData("playing", lang.get(COMMANDREGISTER_QUEUE)),
                new CommandData("skip", lang.get(COMMANDREGISTER_SKIP)),
                new CommandData("s", lang.get(COMMANDREGISTER_SKIP)),
                new CommandData("remove", lang.get(COMMANDREGISTER_REMOVE))
                        .addOption(INTEGER, INDEX, lang.get(COMMANDREGISTER_REMOVE_POS), true),
                new CommandData("volume", lang.get(COMMANDREGISTER_VOLUME))
                        .addOption(INTEGER, COUNT, lang.get(COMMANDREGISTER_VOLUME_COUNT)), // 若未填則回覆預設
                new CommandData("loop", lang.get(COMMANDREGISTER_LOOP)),
                new CommandData("repeat", lang.get(COMMANDREGISTER_REPEAT)),
                new CommandData("pause", lang.get(COMMANDREGISTER_PAUSE)),
                new CommandData("leave", lang.get(COMMANDREGISTER_LEAVE)),
                new CommandData("disconnect", lang.get(COMMANDREGISTER_LEAVE)),
                new CommandData("stop", lang.get(COMMANDREGISTER_LEAVE)),
                // Tool

                new CommandData("rename", lang.get(COMMANDREGISTER_CHANNEL_RENAME)).addOptions(
                        new OptionData(STRING, "name", lang.get(COMMANDREGISTER_CHANNEL_RENAME_NAME), true),
                        new OptionData(CHANNEL, "channel", lang.get(COMMANDREGISTER_CHANNEL_RENAME_CHANNEL))
                ),
                new CommandData("setname", lang.get(COMMANDREGISTER_CHANNEL_RENAME)).addOptions(
                        new OptionData(STRING, "name", lang.get(COMMANDREGISTER_CHANNEL_RENAME_NAME), true),
                        new OptionData(CHANNEL, "channel", lang.get(COMMANDREGISTER_CHANNEL_RENAME_CHANNEL))
                ),
                new CommandData("rebitrate", lang.get(COMMANDREGISTER_VOICECHANNEL_REBITRATE)).addOptions(
                        new OptionData(CHANNEL, "channel", lang.get(COMMANDREGISTER_VOICECHANNEL_REBITRATE_CHANNEL), true),
                        new OptionData(INTEGER, "bitrate", lang.get(COMMANDREGISTER_VOICECHANNEL_REBITRATE_INTEGER), true)
                ),
                new CommandData("setbitrate", lang.get(COMMANDREGISTER_VOICECHANNEL_REBITRATE)).addOptions(
                        new OptionData(CHANNEL, "channel", lang.get(COMMANDREGISTER_VOICECHANNEL_REBITRATE_CHANNEL), true),
                        new OptionData(INTEGER, "bitrate", lang.get(COMMANDREGISTER_VOICECHANNEL_REBITRATE_INTEGER), true)
                ),
                new CommandData("surl", lang.get(COMMANDREGISTER_SURL))
                        .addOption(STRING, URL, lang.get(COMMANDREGISTER_SURL_URL), true),
                new CommandData("nonereaction", "拒絕反應")
                        .addOption(STRING, "messageid", "訊息ID (需在同一個頻道)", true),
                new CommandData("rolebutton", "按鈕切換身分組").addOptions(
                        new OptionData(CHANNEL, "channel", "請輸入來源訊息頻道", true),
                        new OptionData(STRING, "messageid", "請輸入來源訊息ID", true),
                        new OptionData(ROLE, "role", "請選擇身分組", true),
                        new OptionData(STRING, "buttonname", "請輸入按鈕名稱"),
                        new OptionData(STRING, "buttonemoji", "請輸入按鈕圖示"),
                        new OptionData(STRING, "buttonstyle", "請輸入按鈕類型 (green, red, blue, gray)")
                ),
                new CommandData("removemessage", "刪除訊息").addOptions(
                        new OptionData(CHANNEL, "channel", "請輸入訊息頻道", true),
                        new OptionData(STRING, "messageid", "請輸入訊息ID", true)
                ),
                new CommandData("removereactions", "刪除訊息反應").addOptions(
                        new OptionData(CHANNEL, "channel", "請輸入訊息頻道", true),
                        new OptionData(STRING, "messageid", "請輸入訊息ID", true)
                ),
                new CommandData("pinmessage", "釘選訊息").addOptions(
                        new OptionData(CHANNEL, "channel", "請輸入訊息頻道", true),
                        new OptionData(STRING, "messageid", "請輸入訊息ID", true)
                ),
                new CommandData("unpinmessage", "取消釘選訊息").addOptions(
                        new OptionData(CHANNEL, "channel", "請輸入訊息頻道", true),
                        new OptionData(STRING, "messageid", "請輸入訊息ID", true)
                ),
                new CommandData("nhentai", "取得本本").addOption(INTEGER, "id", "請輸入番號", true),
//                new CommandData("popspeed", lang.get(COMMANDREGISTER_POP_SPEED)),
//                new CommandData("poptop", lang.get(COMMANDREGISTER_POP_TOP)),
                new CommandData("giveaway", lang.get(COMMANDREGISTER_GIVEAWAY)).addOptions(
                        new OptionData(STRING, "name", lang.get(COMMANDREGISTER_GIVEAWAY_NAME), true),
                        new OptionData(INTEGER, "winnercount", lang.get(COMMANDREGISTER_GIVEAWAY_WIN_COUNT)),
                        new OptionData(INTEGER, "emoji", lang.get(COMMANDREGISTER_GIVEAWAY_EMOJI)),
                        new OptionData(INTEGER, "month", lang.get(COMMANDREGISTER_GIVEAWAY_MONTH)),
                        new OptionData(INTEGER, "week", lang.get(COMMANDREGISTER_GIVEAWAY_WEEK)),
                        new OptionData(INTEGER, "day", lang.get(COMMANDREGISTER_GIVEAWAY_DAY)),
                        new OptionData(INTEGER, "hour", lang.get(COMMANDREGISTER_GIVEAWAY_HOUR)),
                        new OptionData(INTEGER, "minute", lang.get(COMMANDREGISTER_GIVEAWAY_MINUTE)),
                        new OptionData(INTEGER, "second", lang.get(COMMANDREGISTER_GIVEAWAY_SECOND)),
                        new OptionData(STRING, "time", lang.get(COMMANDREGISTER_GIVEAWAY_TIME))
                ),

                new CommandData("lang", lang.get(LANG_CHANGE)),
                new CommandData("mp4togif", lang.get(COMMANDREGISTER_MP4_TO_GIF)).addOptions(
                        new OptionData(STRING, URL, lang.get(COMMANDREGISTER_MTG_URL), true),
                        new OptionData(STRING, "outputname", lang.get(COMMANDREGISTER_MTG_OUT_PUT_NAME)),
                        new OptionData(STRING, "fps", lang.get(COMMANDREGISTER_MTG_FPS))
                ),

                // osu
                new CommandData("osu", lang.get(COMMANDREGISTER_OSU)).addSubcommands(
                        new SubcommandData("setuser", lang.get(COMMANDREGISTER_OSU_BIND_ACCOUNT))
                                .addOption(STRING, "name", lang.get(COMMANDREGISTER_OSU_PLEASE_TYPE_NAME), true),
                        new SubcommandData("info", lang.get(COMMANDREGISTER_OSU_GET_INFO))
                                .addOption(STRING, "name", lang.get(COMMANDREGISTER_OSU_PLEASE_TYPE_NAME)),
                        new SubcommandData("last", lang.get(COMMANDREGISTER_OSU_GET_LAST))
                                .addOption(STRING, "name", lang.get(COMMANDREGISTER_OSU_PLEASE_TYPE_NAME)),
                        new SubcommandData("top", lang.get(COMMANDREGISTER_OSU_SHOW_TOP))
                                .addOption(STRING, "name", lang.get(COMMANDREGISTER_OSU_PLEASE_TYPE_NAME))
                ),
                // Hypixel
                new CommandData("hy", lang.get(COMMANDREGISTER_HYPIXEL)).addSubcommands(
                        new SubcommandData("setuser", lang.get(COMMANDREGISTER_HYPIXEL_BIND_ACCOUNT))
                                .addOption(STRING, "name", lang.get(COMMANDREGISTER_HYPIXEL_PLEASE_TYPE_NAME), true),
                        new SubcommandData("info", lang.get(COMMANDREGISTER_HYPIXEL_GET_INFO))
                                .addOption(STRING, "name", lang.get(COMMANDREGISTER_HYPIXEL_PLEASE_TYPE_NAME)),
                        new SubcommandData("bedwars", lang.get(COMMANDREGISTER_HYPIXEL_GET_BEDWARS))
                                .addOption(STRING, "name", lang.get(COMMANDREGISTER_HYPIXEL_PLEASE_TYPE_NAME)),
                        new SubcommandData("skywars", lang.get(COMMANDREGISTER_HYPIXEL_GET_SKYWARS))
                                .addOption(STRING, "name", lang.get(COMMANDREGISTER_HYPIXEL_PLEASE_TYPE_NAME)),
                        new SubcommandData("skyblock", lang.get(COMMANDREGISTER_HYPIXEL_GET_SKYBLOCK))
                                .addOption(STRING, "name", lang.get(COMMANDREGISTER_HYPIXEL_PLEASE_TYPE_NAME))
                ),
                // Help
                new CommandData("help", lang.get(COMMANDREGISTER_HELP)).addSubcommands(
                        new SubcommandData("orderly", lang.get(COMMANDREGISTER_HELP_ORDERLY)),
                        new SubcommandData("orderlyannouncement", lang.get(COMMANDREGISTER_HELP_ORDERLY_ANNOUNCEMENT)),
                        new SubcommandData("neko", lang.get(COMMANDREGISTER_HELP_NEKO_BOT)),
                        new SubcommandData("nekoannouncement", lang.get(COMMANDREGISTER_HELP_NEKO_BOT_ANNOUNCEMENT))
                ),

                // Setting
                new CommandData("setting", lang.get(COMMANDREGISTER_SETTING)).addSubcommands(
                        new SubcommandData("help", lang.get(COMMANDREGISTER_S_HELP)),

                        // -------------------
                        new SubcommandData("newroom", lang.get(COMMANDREGISTER_S_NEW_ROOM)).addOptions(
                                new OptionData(CHANNEL, "detectchannel", lang.get(COMMANDREGISTER_S_NR_DETECT_CHANNEL), true),
                                new OptionData(STRING, "voicename", lang.get(COMMANDREGISTER_S_NR_VOICE_NAME), true),
                                new OptionData(STRING, "textname", lang.get(COMMANDREGISTER_S_NR_TEXT_NAME)),
                                new OptionData(CHANNEL, "voicecategory", lang.get(COMMANDREGISTER_S_NR_VOICE_CATEGORY)),
                                new OptionData(CHANNEL, "textcategory", lang.get(COMMANDREGISTER_S_NR_TEXT_CATEGORY)),
                                new OptionData(INTEGER, "voicebitrate", lang.get(COMMANDREGISTER_S_NR_VOICE_BITRATE)),
                                new OptionData(INTEGER, "memberlimit", lang.get(COMMANDREGISTER_S_NR_MEMBER_LIMIT))
                        ),

                        new SubcommandData("removeroom", lang.get(COMMANDREGISTER_S_REMOVE_ROOM))
                                .addOption(CHANNEL, "detectchannel", lang.get(COMMANDREGISTER_S_RR_DETECT_CHANNEL), true),

                        // -------------------
                        new SubcommandData("newautovc", lang.get(COMMANDREGISTER_S_NEW_AUTO_VC)).addOptions(
                                new OptionData(CHANNEL, "detectcategory", lang.get(COMMANDREGISTER_S_NAVC_DETECT_CATEGORY), true),
                                new OptionData(STRING, "voicename", lang.get(COMMANDREGISTER_S_NAVC_VOICE_NAME), true)
                        ),

                        new SubcommandData("removeautovc", lang.get(COMMANDREGISTER_S_REMOVE_AUTO_VC))
                                .addOption(CHANNEL, "detectcategory", lang.get(COMMANDREGISTER_S_RAVC_DETECT_CATEGORY), true),

                        // -------------------
                        new SubcommandData("newticket", lang.get(COMMANDREGISTER_S_NEW_TICKET)).addOptions(
                                new OptionData(STRING, "messageid", lang.get(COMMANDREGISTER_S_NT_MESSAGE_ID), true),
                                new OptionData(CHANNEL, "messagechannel", lang.get(COMMANDREGISTER_S_NT_MESSAGE_CHANNEL), true),
                                new OptionData(STRING, "enteredmessage", lang.get(COMMANDREGISTER_S_NT_ENTERED_MESSAGE), true),
                                new OptionData(ROLE, "allowrole", lang.get(COMMANDREGISTER_S_NT_ALLOW_ROLE), true),
                                new OptionData(BOOLEAN, "allowtagrole", lang.get(COMMANDREGISTER_S_NT_ALLOW_TAG_ROLE)),
                                new OptionData(STRING, "textname", lang.get(COMMANDREGISTER_S_NT_TEXT_NAME)),
                                new OptionData(CHANNEL, "textcategory", lang.get(COMMANDREGISTER_S_NT_TEXT_CATEGORY)),
                                new OptionData(BOOLEAN, "hasvoicechannel", lang.get(COMMANDREGISTER_S_NT_HAS_VOICE_CHANNEL)),
                                new OptionData(STRING, "voicename", lang.get(COMMANDREGISTER_S_NT_VOICE_NAME)),
                                new OptionData(CHANNEL, "voicecategory", lang.get(COMMANDREGISTER_S_NT_VOICE_CATEGORY)),
                                new OptionData(STRING, "buttonname", lang.get(COMMANDREGISTER_S_NT_BUTTON_NAME)),
                                new OptionData(STRING, "buttoncolor", lang.get(COMMANDREGISTER_S_NT_BUTTON_COLOR)),
                                new OptionData(STRING, "buttonemoji", lang.get(COMMANDREGISTER_S_NT_BUTTON_EMOJI)),
                                new OptionData(BOOLEAN, "onlyone", lang.get(COMMANDREGISTER_S_NT_ONLY_ONE))
                        ),

                        new SubcommandData("addticket", lang.get(COMMANDREGISTER_S_ADD_TICKET)).addOptions(
                                new OptionData(STRING, "messageid", lang.get(COMMANDREGISTER_S_AT_MESSAGE_ID), true),
                                new OptionData(CHANNEL, "messagechannel", lang.get(COMMANDREGISTER_S_AT_MESSAGE_CHANNEL), true),
                                new OptionData(STRING, "enteredmessage", lang.get(COMMANDREGISTER_S_AT_ENTERED_MESSAGE), true),
                                new OptionData(ROLE, "allowrole", lang.get(COMMANDREGISTER_S_AT_ALLOW_ROLE), true),
                                new OptionData(BOOLEAN, "allowtagrole", lang.get(COMMANDREGISTER_S_AT_ALLOW_TAG_ROLE)),
                                new OptionData(STRING, "textname", lang.get(COMMANDREGISTER_S_AT_TEXT_NAME)),
                                new OptionData(CHANNEL, "textcategory", lang.get(COMMANDREGISTER_S_AT_TEXT_CATEGORY)),
                                new OptionData(BOOLEAN, "hasvoicechannel", lang.get(COMMANDREGISTER_S_AT_HAS_VOICE_CHANNEL)),
                                new OptionData(STRING, "voicename", lang.get(COMMANDREGISTER_S_AT_VOICE_NAME)),
                                new OptionData(CHANNEL, "voicecategory", lang.get(COMMANDREGISTER_S_AT_VOICE_CATEGORY)),
                                new OptionData(STRING, "buttonname", lang.get(COMMANDREGISTER_S_AT_BUTTON_NAME)),
                                new OptionData(STRING, "buttonemoji", lang.get(COMMANDREGISTER_S_AT_BUTTON_COLOR)),
                                new OptionData(STRING, "buttoncolor", lang.get(COMMANDREGISTER_S_AT_BUTTON_EMOJI)),
                                new OptionData(BOOLEAN, "onlyone", lang.get(COMMANDREGISTER_S_AT_ONLY_ONE))
                        ),

                        new SubcommandData("removeticket", lang.get(COMMANDREGISTER_S_REMOVE_TICKET)).addOptions(
                                new OptionData(STRING, "messageid", lang.get(COMMANDREGISTER_S_RT_MESSAGE_ID), true),
                                new OptionData(CHANNEL, "messagechannel", lang.get(COMMANDREGISTER_S_RT_MESSAGE_CHANNEL), true),
                                new OptionData(STRING, "position", lang.get(COMMANDREGISTER_S_RT_POSITION), true)
                        ),

                        // -------------------
                        new SubcommandData("newchannelstatus", lang.get(COMMANDREGISTER_S_NEW_CHANNEL_STATUS)).addOptions(
                                new OptionData(CHANNEL, "channel", lang.get(COMMANDREGISTER_S_NCS_CHANNEL), true),
                                new OptionData(STRING, "channelname", lang.get(COMMANDREGISTER_S_NCS_CHANNEL_NAME), true),
                                new OptionData(INTEGER, "format", lang.get(COMMANDREGISTER_S_NCS_FORMAT), true)
                        ),
                        new SubcommandData("removechannelstatus", lang.get(COMMANDREGISTER_S_REMOVE_CHANNEL_STATUS))
                                .addOption(CHANNEL, "channel", lang.get(COMMANDREGISTER_S_RSC_CHANNEL), true),

                        // -------------------
                        new SubcommandData("newjoin", lang.get(COMMANDREGISTER_S_NEW_JOIN)).addOptions(
                                new OptionData(CHANNEL, "channel", lang.get(COMMANDREGISTER_S_NJ_CHANNEL), true),
                                new OptionData(STRING, "message", lang.get(COMMANDREGISTER_S_NJ_MESSAGE), true),
                                new OptionData(STRING, "dm", lang.get(COMMANDREGISTER_S_NJ_DM)),
                                new OptionData(ROLE, "role1", lang.get(COMMANDREGISTER_S_NJ_ROLE1)),
                                new OptionData(ROLE, "role2", lang.get(COMMANDREGISTER_S_NJ_ROLE2)),
                                new OptionData(ROLE, "role3", lang.get(COMMANDREGISTER_S_NJ_ROLE3)),
                                new OptionData(ROLE, "role4", lang.get(COMMANDREGISTER_S_NJ_ROLE4)),
                                new OptionData(ROLE, "role5", lang.get(COMMANDREGISTER_S_NJ_ROLE5)),
                                new OptionData(ROLE, "role6", lang.get(COMMANDREGISTER_S_NJ_ROLE6)),
                                new OptionData(ROLE, "role7", lang.get(COMMANDREGISTER_S_NJ_ROLE7)),
                                new OptionData(ROLE, "role8", lang.get(COMMANDREGISTER_S_NJ_ROLE8)),
                                new OptionData(ROLE, "role9", lang.get(COMMANDREGISTER_S_NJ_ROLE9)),
                                new OptionData(ROLE, "role10", lang.get(COMMANDREGISTER_S_NJ_ROLE10))
                        ),
                        new SubcommandData("removejoin", lang.get(COMMANDREGISTER_S_REMOVE_JOIN))
                                .addOption(CHANNEL, "channel", lang.get(COMMANDREGISTER_S_RJ_CHANNEL), true),

                        // -------------------
                        new SubcommandData("newleave", lang.get(COMMANDREGISTER_S_NEW_LEAVE))
                                .addOptions(
                                        new OptionData(CHANNEL, "channel", lang.get(COMMANDREGISTER_S_NL_CHANNEL), true),
                                        new OptionData(STRING, "message", lang.get(COMMANDREGISTER_S_NL_MESSAGE), true)
                                ),
                        new SubcommandData("removeleave", lang.get(COMMANDREGISTER_S_REMOVE_LEAVE))
                                .addOption(CHANNEL, "channel", lang.get(COMMANDREGISTER_S_RL_CHANNEL), true),
                        new SubcommandData("newyande", lang.get(COMMANDREGISTER_NEWYANDE)).addOptions(
                                new OptionData(STRING, "tag1", lang.get(COMMANDREGISTER_NEWYANDE_TAG), true),
                                new OptionData(STRING, "tag2", lang.get(COMMANDREGISTER_NEWYANDE_TAG)),
                                new OptionData(STRING, "tag3", lang.get(COMMANDREGISTER_NEWYANDE_TAG)),
                                new OptionData(STRING, "tag4", lang.get(COMMANDREGISTER_NEWYANDE_TAG)),
                                new OptionData(STRING, "tag5", lang.get(COMMANDREGISTER_NEWYANDE_TAG)),
                                new OptionData(STRING, "tag6", lang.get(COMMANDREGISTER_NEWYANDE_TAG)),
                                new OptionData(STRING, "tag7", lang.get(COMMANDREGISTER_NEWYANDE_TAG)),
                                new OptionData(STRING, "tag8", lang.get(COMMANDREGISTER_NEWYANDE_TAG)),
                                new OptionData(STRING, "tag9", lang.get(COMMANDREGISTER_NEWYANDE_TAG)),
                                new OptionData(CHANNEL, "channel", lang.get(COMMANDREGISTER_NEWYANDE_CHANNEL))),
                        new SubcommandData("removeyande", lang.get(COMMANDREGISTER_REMOVEYANDE))
                                .addOption(CHANNEL, "channel", lang.get(COMMANDREGISTER_REMOVEYANDE_CHANNEL), true)

                        // -------------------
                ),
        };
    }

    private CommandData[] ownCommands(String guildID) {
        List<String> lang = Main.language.getGuildLang(guildID);
        return new CommandData[]{
                new CommandData("reload", lang.get(COMMANDREGISTER_RELOAD)),
                new CommandData("nick", lang.get(COMMANDREGISTER_NICK)),
                new CommandData("invite", lang.get(COMMANDREGISTER_INVITE))
                        .addOption(USER, USER_TAG, lang.get(COMMANDREGISTER_INVITE_MEMBER_YOU_CHOOSE), true),
        };
    }

    private CommandData[] everywhereCommands() {
        return new CommandData[]{
                new CommandData("ping", "Ping Test (延遲測試)"),
                new CommandData("support", "Report ERROR or Support us (傳送問題回報)")
                        .addOption(STRING, MESSAGE, "Message (訊息內容)", true),
                new CommandData("botinfo", "Show about Bot (顯示機器人訊息)"),
        };
    }
}
