package com.ice.main;

import com.ice.main.command.CommandRegister;
import com.ice.main.command.list.*;
import com.ice.main.command.list.setting.*;
import com.ice.main.event.*;
import com.ice.main.util.file.GuildSettingHelper;
import com.ice.multiBot.MultiMusicBotManager;
import net.dv8tion.jda.api.Permission;
import net.dv8tion.jda.api.entities.ChannelType;
import net.dv8tion.jda.api.entities.Guild;
import net.dv8tion.jda.api.events.ReadyEvent;
import net.dv8tion.jda.api.events.guild.*;
import net.dv8tion.jda.api.events.guild.invite.GuildInviteCreateEvent;
import net.dv8tion.jda.api.events.guild.invite.GuildInviteDeleteEvent;
import net.dv8tion.jda.api.events.guild.member.GuildMemberJoinEvent;
import net.dv8tion.jda.api.events.guild.member.GuildMemberRemoveEvent;
import net.dv8tion.jda.api.events.guild.member.GuildMemberRoleAddEvent;
import net.dv8tion.jda.api.events.guild.member.GuildMemberRoleRemoveEvent;
import net.dv8tion.jda.api.events.guild.member.update.GuildMemberUpdateBoostTimeEvent;
import net.dv8tion.jda.api.events.guild.voice.*;
import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent;
import net.dv8tion.jda.api.events.interaction.component.ButtonInteractionEvent;
import net.dv8tion.jda.api.events.interaction.component.SelectMenuInteractionEvent;
import net.dv8tion.jda.api.events.message.MessageDeleteEvent;
import net.dv8tion.jda.api.events.message.MessageReceivedEvent;
import net.dv8tion.jda.api.events.message.MessageUpdateEvent;
import net.dv8tion.jda.api.events.message.react.MessageReactionAddEvent;
import net.dv8tion.jda.api.events.message.react.MessageReactionRemoveEmoteEvent;
import net.dv8tion.jda.api.events.message.react.MessageReactionRemoveEvent;
import net.dv8tion.jda.api.events.user.update.UserUpdateOnlineStatusEvent;
import net.dv8tion.jda.api.hooks.ListenerAdapter;
import org.jetbrains.annotations.NotNull;

import java.util.List;

import static com.ice.main.BotSetting.boostedRole;
import static com.ice.main.BotSetting.debugMode;
import static com.ice.main.Main.language;
import static com.ice.main.command.list.Invite.authChannelID;
import static com.ice.main.lang.LangKey.*;
import static com.ice.main.util.EmbedCreator.createEmbed;
import static com.ice.main.util.GuildUtil.guild;
import static com.ice.main.util.GuildUtil.guildID;
import static com.ice.main.util.PermissionERROR.permissionCheck;
import static com.ice.main.util.Tag.getMemberName;
import static com.ice.main.util.Tag.tagChannelID;

public class ListenerManager extends ListenerAdapter {
    //setting
    GuildSettingHelper guildSettingHelper = new GuildSettingHelper();
    SettingHelp settingHelp = new SettingHelp();
    SettingRoom settingRoom = new SettingRoom(guildSettingHelper);
    SettingYande settingYande = new SettingYande(guildSettingHelper);
    SettingVCC settingVCC = new SettingVCC(guildSettingHelper);
    SettingTicket settingTicket = new SettingTicket(guildSettingHelper);
    SettingChannelStatus settingChannelStatus = new SettingChannelStatus(guildSettingHelper);
    SettingJoinLeave settingJoinLeave = new SettingJoinLeave(guildSettingHelper);
    SettingSchool settingSchool = new SettingSchool();

    //listener
    CommandRegister commandRegister = new CommandRegister();
    Log log = new Log();
    OwnJoin join = new OwnJoin();
    Room room = new Room(guildSettingHelper);
    Level level = new Level();
    NewGuild newGuild = new NewGuild();
    QuickUse quickUse = new QuickUse();
    Ticket ticketChannel = new Ticket(guildSettingHelper);
    GeneralReplay generalReplay = new GeneralReplay();
    JoinLeaveMessage joinLeaveMessage = new JoinLeaveMessage(guildSettingHelper);
    MultiMusicBotManager musicManager = new MultiMusicBotManager();
    StatusListener statusListener = new StatusListener(guildSettingHelper);
    VoiceChannelCreator voiceChannelCreator = new VoiceChannelCreator(guildSettingHelper);
    Economy economy = new Economy(guildSettingHelper);
    InformationReaction informationReaction = new InformationReaction();
    NHentai nHentai = new NHentai();
    ButtonRole roleButton = new ButtonRole();

    //command
    Ping ping = new Ping();
    Kick kickCommand = new Kick();
    Ban banCommand = new Ban();
    UnBan unBanCommand = new UnBan();
    Clear clearCommand = new Clear();
    BotInfo botInfo = new BotInfo();
    Poll pollCommand = new Poll();
    Support support = new Support();
    Help helpCommand = new Help();
    Reload reload = new Reload();
    Invite createInviteCommand = new Invite();
    URLShort sortURL = new URLShort();
    FileConvert fileConvert = new FileConvert();
    PopCat popCat = new PopCat();
    Giveaway giveaway = new Giveaway(guildSettingHelper);
    Language langCommand = new Language(guildSettingHelper);
    ChannelChange channelChange = new ChannelChange();
    SettingOsu settingOsu = new SettingOsu();
    SettingHypixel settingHypixel = new SettingHypixel();
    NoneReaction noneReaction = new NoneReaction();
    MessageManager messageManager = new MessageManager();

    /**
     * Guild Message
     */

    // 條件:
    // always | 無條件執行
    // word | 特定字元執行
    // channel | 特定頻道執行
    // owner | 開發者執行
    // buttonID | 特定按鈕 ID
    // emoji | 特定 Emoji
    // member | 特定成員
    // guild | 特定公會
    @Override
    public void onMessageReceived(@NotNull MessageReceivedEvent event) {
        if (event.isFromGuild()) {
            quickUse.onGuildMessageReceived(event); // word("testCommand") && owner
            level.onGuildMessageReceived(event); // always
            generalReplay.onGuildMessageReceived(event); // word
            log.onGuildMessageReceived(event); // always
        } else if (event.isFromType(ChannelType.PRIVATE)) {
            join.onPrivateMessageReceived(event); // guild(own)
            System.out.printf("[Private] %s sent a message: %s (%s)"
                    , event.getAuthor().getAsTag(), event.getMessage().getContentRaw(), event.getAuthor().getId());
        }
    }

    @Override
    public void onMessageUpdate(@NotNull MessageUpdateEvent event) {
        if (event.isFromGuild()) {
            log.onGuildMessageUpdate(event); // guild(own)
        } else if (event.isFromType(ChannelType.PRIVATE)) {
            System.out.printf("[Private] %s update a message: %s (%s)"
                    , event.getAuthor().getAsTag(), event.getMessage().getContentRaw(), event.getAuthor().getId());
        }
    }

    @Override
    public void onMessageDelete(@NotNull MessageDeleteEvent event) {
        log.onGuildMessageDelete(event); // guild(own)
    }

    @Override
    public void onMessageReactionAdd(@NotNull MessageReactionAddEvent event) {
        if (event.isFromGuild()) {
            informationReaction.onGuildMessageReactionAdd(event); // channel && emoji("RightArrow") (Renew Announcement)
            log.onGuildMessageReactionAdd(event); // guild(own)
            noneReaction.onReaction(event);
        } else if (event.isFromType(ChannelType.PRIVATE)) {
            join.onPrivateMessageReactionAdd(event); // guild(own)
        }
    }

    @Override
    public void onMessageReactionRemove(@NotNull MessageReactionRemoveEvent event) {

        log.onGuildMessageReactionRemove(event); // guild(own)

    }

    @Override
    public void onMessageReactionRemoveEmote(@NotNull MessageReactionRemoveEmoteEvent event) {
        log.onGuildMessageReactionRemoveEmote(event); // guild(own)
    }

    /**
     * Guild Join
     */

    @Override
    public void onGuildJoin(GuildJoinEvent event) {
        language.loadGuildSetting(event.getGuild(), guildSettingHelper);
        newGuild.onCommand(event, commandRegister); // always
        statusListener.updateGuild(event.getGuild());
    }

    @Override
    public void onGuildLeave(GuildLeaveEvent event) {
    }

    /**
     * Ready
     */
    @Override
    public void onGuildReady(GuildReadyEvent event) {
        language.loadGuildSetting(event.getGuild(), guildSettingHelper);
        commandRegister.onGuildReady(event); // register commands
        voiceChannelCreator.onGuildReady(event);
        settingYande.onGuildReady(event);
        statusListener.onGuildReady(event.getGuild());
    }

    @Override
    public void onReady(ReadyEvent event) {
        musicManager.setupAllBot();
        statusListener.startListen(event.getJDA());
        settingYande.startThread(event.getJDA());
    }

    /**
     * Guild Ban
     */

    @Override
    public void onGuildBan(GuildBanEvent event) {
    }

    @Override
    public void onGuildUnban(GuildUnbanEvent event) {
    }

    /**
     * Guild Invite
     */

    @Override
    public void onGuildInviteCreate(GuildInviteCreateEvent event) {
    }

    @Override
    public void onGuildInviteDelete(GuildInviteDeleteEvent event) {
    }

    /**
     * Guild Member
     */

    @Override
    public void onGuildMemberRemove(GuildMemberRemoveEvent event) {
    }

    @Override
    public void onGuildMemberJoin(GuildMemberJoinEvent event) {
        join.onGuildMemberJoin(event); // guild(own)
        joinLeaveMessage.onGuildMemberJoin(event);
        statusListener.memberJoin(event.getMember());
    }

    @Override
    public void onGuildMemberRoleAdd(GuildMemberRoleAddEvent event) {
        log.onGuildMemberRoleAdd(event); // guild(own)
    }

    @Override
    public void onGuildMemberRoleRemove(GuildMemberRoleRemoveEvent event) {
        log.onGuildMemberRoleRemove(event); // guild(own)
    }

    @Override
    public void onGuildMemberUpdateBoostTime(GuildMemberUpdateBoostTimeEvent event) {
        if (event.getGuild().getId().equals(guildID) && !event.getMember().getRoles().contains(boostedRole))
            guild.addRoleToMember(event.getMember(), boostedRole).queue();

    }

    @Override
    public void onUserUpdateOnlineStatus(UserUpdateOnlineStatusEvent event) {
        statusListener.statusChange(event);
    }

    /**
     * Guild Voice
     */

    @Override
    public void onGuildVoiceJoin(GuildVoiceJoinEvent event) {
        room.onGuildVoiceJoin(event); // guild(own)
        log.onGuildVoiceJoin(event); // guild(own)
        voiceChannelCreator.onGuildVoiceJoin(event);
        statusListener.memberVoiceJoin(event.getMember());
    }

    @Override
    public void onGuildVoiceLeave(GuildVoiceLeaveEvent event) {
        room.onGuildVoiceLeave(event); // guild(own)
        log.onGuildVoiceLeave(event); // guild(own)
        musicManager.onVoiceLeave(event);
        voiceChannelCreator.onGuildVoiceLeave(event);
        statusListener.memberVoiceLeave(event.getMember());
    }

    @Override
    public void onGuildVoiceMove(GuildVoiceMoveEvent event) {
        room.onGuildVoiceMove(event); // guild(own)
        voiceChannelCreator.onGuildVoiceMove(event);
    }

    @Override
    public void onGuildVoiceStream(GuildVoiceStreamEvent event) {
        statusListener.memberVoiceStream(event.getMember());
    }

    @Override
    public void onGuildVoiceVideo(GuildVoiceVideoEvent event) {
        statusListener.memberVoiceVideo(event.getMember());
    }

    /**
     * Guild Command
     */

    @Override
    public void onButtonInteraction(ButtonInteractionEvent event) {
        // CLASS:TYPE:USERID:xxx
        String[] args = event.getComponentId().split(":");
        if (!args[2].equals(event.getUser().getId()) && !args[2].equals("") && !args[2].equals("everyone")) {
            List<String> lang = Main.language.getGuildLang(event.getGuild().getId());
            event.getInteraction().deferReply(true).addEmbeds(
                    createEmbed(lang.get(LISTENERMANAGER_NOT_YOUR_BUTTON), 0xFF0000)).queue();
            return;
        }
        createInviteCommand.onButton(event, args);
        quickUse.onButtonClick(event, args);
        informationReaction.onButtonClick(event, args);
        ticketChannel.onButtonClick(event, args);
        clearCommand.onButton(event, args);
        musicManager.onButton(event, args);
        economy.onDailyCheck(event, args);
        nHentai.onButtonClick(event, args);
        roleButton.onButtonClick(event, args);
    }

    @Override
    public void onSelectMenuInteraction(SelectMenuInteractionEvent event) {
        String[] args = event.getComponentId().split(":");
        if (!args[2].equals(event.getUser().getId()) && !args[2].equals(""))
            return;
        musicManager.onSelectMenu(event, args);
        langCommand.onSelect(event, args, commandRegister);
        popCat.onSelectTop(event, args);
        popCat.onSelectSpeed(event, args);
        settingSchool.onSelectInfo(event, args);
    }


    @SuppressWarnings("ConstantConditions")
    @Override
    public void onSlashCommandInteraction(SlashCommandInteractionEvent event) {
        event.getInteraction().deferReply(true).queue();
        // 如果找不到伺服器 ->
        if (event.getGuild() == null) {
            if (debugMode)
                System.out.printf("[Private] %s issued command: `%s` (%s)", event.getUser().getAsTag() + ((event.getUser().isBot() ? " (Bot)" : (event.getUser().isSystem() ? " (System)" : ""))), event.getCommandString(), event.getUser().getId());

            switch (event.getName()) {
                case "ping" -> {
                    ping.onCommand(event);
                    return;
                }
                case "support" -> {
                    support.onCommand(event);
                    return;
                }
                case "botinfo" -> {
                    botInfo.onCommand(event);
                    return;
                }
                case "join" -> {
                    join.onCommand(event);
                    return;
                }
            }

            List<String> lang = Main.language.getGuildLang(event.getGuild().getId());
            event.getHook().editOriginalEmbeds(createEmbed(lang.get(LISTENERMANAGER_CANT_DO_THIS), 0xFF0000)).queue();
            return;
        }

        if (debugMode) {
            if (event.getGuild().getId().equals("882605953382514718"))
                event.getJDA().getGuildById("882605953382514718").getTextChannelById("884425527513985024")
                        .sendMessage(String.format(
                                "[%s] %s issued command: `%s`",
                                event.getGuild().getName(),
                                getMemberName(event),
                                event.getCommandString())).queue();

            System.out.printf("[%s] %s issued command: `%s` (%s - %s - %s)",
                    event.getGuild().getName(),
                    getMemberName(event),
                    event.getCommandString(),
                    event.getGuild().getId(),
                    event.getChannel().getId(),
                    event.getUser().getId());
        }

        // 取得輸入指令的頻道
        String channelID = event.getChannel().getId();

        int type;

        type = musicManager.onCommand(event);
        if (type == -1)  // 輸入的頻道錯誤
            return;
        else if (type == 1)  // 已經執行完成並 return
            return;


        // 邀請
        if (event.getName().equals("invite")) {
            if (channelID.equals(authChannelID))
                createInviteCommand.onCommand(event);
            else {
                List<String> lang = Main.language.getGuildLang(event.getGuild().getId());
                event.getHook().editOriginalEmbeds(createEmbed(
                        String.format("%s (%s)", lang.get(LISTENERMANAGER_WRONG_CHANNEL), tagChannelID(authChannelID)), 0xFF0000)).queue();
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
            case "removemessage" -> {
                messageManager.onDeleteCommand(event);
                return;
            }
            case "removereactions" -> {
                messageManager.onRemoveReactionsCommand(event);
                return;
            }
            case "pinmessage" -> {
                messageManager.onPinCommand(event);
                return;
            }
            case "unpinmessage" -> {
                messageManager.onUnPinCommand(event);
                return;
            }
            case "ping" -> {
                ping.onCommand(event);
                return;
            }
            case "botinfo" -> {
                botInfo.onCommand(event);
                return;
            }
            case "support" -> {
                support.onCommand(event);
                return;
            }
            case "nonereaction" -> {
                noneReaction.onCommand(event);
                return;
            }
//            case "surl" -> {
//                sortURL.onCommand(event, false, null, Main.language.getGuildLang(event.getGuild().getId()));
//                return;
//            }
            case "mp4togif" -> {
                fileConvert.onCommand(event, sortURL);
                return;
            }
            case "poptop" -> {
                popCat.onCommandTop(event);
                return;
            }
            case "popspeed" -> {
                popCat.onCommandSpeed(event);
                return;
            }
            case "newgiveaway" -> {
                giveaway.newGiveaway(event);
                return;
            }
            case "lang" -> {
                langCommand.onCommand(event);
                return;
            }
            case "renamechannel", "setchannelname" -> {
                channelChange.setName(event);
                return;
            }
            case "rebitrate", "setbitrate" -> {
                channelChange.setBitrate(event);
                return;
            }
            case "nhentai" -> {
                nHentai.onCommand(event);
                return;
            }
            case "rolebutton" -> {
                roleButton.onCommand(event);
                return;
            }
            case "reload" -> {
                if (reload.onCommand(event))
                    reload(event.getGuild());
                return;
            }
            case "help" -> {
                switch (event.getSubcommandName()) {
                    case "orderly" -> {
                        helpCommand.onSelfMemberCommand(event);
                        return;
                    }
                    case "orderlyannouncement" -> {
                        helpCommand.onSelfAnnouncementCommand(event);
                        return;
                    }
                    case "neko" -> {
                        helpCommand.onNekoBotMemberCommand(event);
                        return;
                    }
                    case "nekoannouncement" -> {
                        helpCommand.onNekoBotAnnouncementCommand(event);
                        return;
                    }
                }
            }
            case "osu" -> {
                switch (event.getSubcommandName()) {
                    case "info" -> {
                        settingOsu.info(event);
                        return;
                    }
                    case "setuser" -> {
                        settingOsu.onRegister(event);
                        return;
                    }
                    case "last" -> {
                        settingOsu.onPrevious(event);
                        return;
                    }
                    case "top" -> {
                        settingOsu.onTop(event);
                        return;
                    }
                }
            }
            case "school" -> {
                switch (event.getSubcommandName()) {
                    case "login" -> {
                        settingSchool.onSchoolLogin(event);
                        return;
                    }
                    case "info" -> {
                        settingSchool.onSlashInfo(event);
                        return;
                    }
                }
            }
            case "hy" -> {
                switch (event.getSubcommandName()) {
                    case "info" -> {
                        settingHypixel.info(event);
                        return;
                    }
                    case "setuser" -> {
                        settingHypixel.onRegister(event);
                        return;
                    }
                    case "skyblock", "bedwars", "skywars" -> {
//                        settingHypixel.onPrevious(event);
                        event.getHook().editOriginalEmbeds(createEmbed("Making...", 0xFF0000)).queue();
                        return;
                    }
                    //                        settingHypixel.onTop(event);
                    //                        settingHypixel.onTop(event);
                }
            }

            case "setting" -> {
                if (!permissionCheck(Permission.ADMINISTRATOR, event, true))
                    return;
                switch (event.getSubcommandName()) {
                    case "newyande" -> {
                        settingYande.newYande(event);
                        return;
                    }
                    case "removeyande" -> {
                        settingYande.removeYande(event);
                        return;
                    }
                    case "newroom" -> {
                        settingRoom.newRoom(event);
                        return;
                    }
                    case "newautovc" -> {
                        settingVCC.newVCC(event);
                        return;
                    }
                    case "removeroom" -> {
                        settingRoom.removeRoom(event);
                        return;
                    }
                    case "removeautovc" -> {
                        settingVCC.removeVCC(event);
                        return;
                    }
                    case "newticket" -> {
                        settingTicket.newTicket(event, true);
                        return;
                    }
                    case "addticket" -> {
                        settingTicket.newTicket(event, false);
                        return;
                    }
                    case "removeticket" -> {
                        settingTicket.removeTicket(event, ticketChannel);
                        return;
                    }
                    case "newchannelstatus" -> {
                        settingChannelStatus.newCS(event, statusListener);
                        return;
                    }
                    case "removechannelstatus" -> {
                        settingChannelStatus.removeCS(event);
                        return;
                    }
                    case "newjoin" -> {
                        settingJoinLeave.newJoin(event);
                        return;
                    }
                    case "removejoin" -> {
                        settingJoinLeave.removeJoin(event);
                        return;
                    }
                    case "newleave" -> {
                        settingJoinLeave.newLeave(event);
                        return;
                    }
                    case "removeleave" -> {
                        settingJoinLeave.removeLeave(event);
                        return;
                    }
                    case "help" -> {
                        settingHelp.onCommand(event);
                        return;
                    }
                }
            }
        }
        List<String> lang = Main.language.getGuildLang(event.getGuild().getId());
        event.getHook().editOriginalEmbeds(createEmbed(lang.get(LISTENERMANAGER_CANT_DO_THIS), 0xFF0000)).queue();
    }

    public void reload(Guild guild) {
        commandRegister.getMainGuildVariable(guild); // guild(own)
    }
}