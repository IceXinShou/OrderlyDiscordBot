package main.java;

import main.java.command.CommandRegister;
import main.java.command.list.*;
import main.java.command.list.Setting.*;
import main.java.event.*;
import main.java.util.file.GuildSettingHelper;
import multiBot.MultiMusicBotManager;
import net.dv8tion.jda.api.Permission;
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
import net.dv8tion.jda.api.events.interaction.ButtonClickEvent;
import net.dv8tion.jda.api.events.interaction.SelectionMenuEvent;
import net.dv8tion.jda.api.events.interaction.SlashCommandEvent;
import net.dv8tion.jda.api.events.message.guild.GuildMessageDeleteEvent;
import net.dv8tion.jda.api.events.message.guild.GuildMessageEmbedEvent;
import net.dv8tion.jda.api.events.message.guild.GuildMessageReceivedEvent;
import net.dv8tion.jda.api.events.message.guild.GuildMessageUpdateEvent;
import net.dv8tion.jda.api.events.message.guild.react.GuildMessageReactionAddEvent;
import net.dv8tion.jda.api.events.message.guild.react.GuildMessageReactionRemoveAllEvent;
import net.dv8tion.jda.api.events.message.guild.react.GuildMessageReactionRemoveEmoteEvent;
import net.dv8tion.jda.api.events.message.guild.react.GuildMessageReactionRemoveEvent;
import net.dv8tion.jda.api.events.message.priv.PrivateMessageDeleteEvent;
import net.dv8tion.jda.api.events.message.priv.PrivateMessageEmbedEvent;
import net.dv8tion.jda.api.events.message.priv.PrivateMessageReceivedEvent;
import net.dv8tion.jda.api.events.message.priv.PrivateMessageUpdateEvent;
import net.dv8tion.jda.api.events.message.priv.react.PrivateMessageReactionAddEvent;
import net.dv8tion.jda.api.events.message.priv.react.PrivateMessageReactionRemoveEvent;
import net.dv8tion.jda.api.events.user.update.UserUpdateOnlineStatusEvent;
import net.dv8tion.jda.api.hooks.ListenerAdapter;
import org.jetbrains.annotations.NotNull;

import static main.java.BotSetting.boostedRole;
import static main.java.BotSetting.debugMode;
import static main.java.command.list.Invite.authChannelID;
import static main.java.util.EmbedCreator.createEmbed;
import static main.java.util.GuildUtil.guild;
import static main.java.util.PermissionERROR.hasPermission;
import static main.java.util.Tag.tagChannelID;

public class ListenerManager extends ListenerAdapter {
    //setting
    GuildSettingHelper guildSettingHelper = new GuildSettingHelper();
    SettingHelp settingHelp = new SettingHelp();
    SettingRoom settingRoom = new SettingRoom(guildSettingHelper);
    SettingVCC settingVCC = new SettingVCC(guildSettingHelper);
    SettingTicket settingTicket = new SettingTicket(guildSettingHelper);
    SettingChannelStatus settingChannelStatus = new SettingChannelStatus(guildSettingHelper);
    SettingJoinLeave settingJoinLeave = new SettingJoinLeave(guildSettingHelper);

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
    InformationReaction informationReaction = new InformationReaction();

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
    public void onGuildMessageReceived(@NotNull GuildMessageReceivedEvent event) {
        quickUse.onGuildMessageReceived(event); // word("testCommand") && owner
        level.onGuildMessageReceived(event); // always
        generalReplay.onGuildMessageReceived(event); // word
        log.onGuildMessageReceived(event); // always
    }

    @Override
    public void onGuildMessageUpdate(@NotNull GuildMessageUpdateEvent event) {
        log.onGuildMessageUpdate(event); // guild(own)
    }

    @Override
    public void onGuildMessageDelete(@NotNull GuildMessageDeleteEvent event) {
        log.onGuildMessageDelete(event); // guild(own)
    }

    @Override
    public void onGuildMessageEmbed(@NotNull GuildMessageEmbedEvent event) {
    }

    @Override
    public void onGuildMessageReactionAdd(@NotNull GuildMessageReactionAddEvent event) {
        informationReaction.onGuildMessageReactionAdd(event); // channel && emoji("RightArrow") (Renew Announcement)
        log.onGuildMessageReactionAdd(event); // guild(own)
    }

    @Override
    public void onGuildMessageReactionRemove(@NotNull GuildMessageReactionRemoveEvent event) {
        log.onGuildMessageReactionRemove(event); // guild(own)
    }

    @Override
    public void onGuildMessageReactionRemoveAll(@NotNull GuildMessageReactionRemoveAllEvent event) {
    }

    @Override
    public void onGuildMessageReactionRemoveEmote(@NotNull GuildMessageReactionRemoveEmoteEvent event) {
        log.onGuildMessageReactionRemoveEmote(event); // guild(own)
    }

    /**
     * Guild Join
     */

    @Override
    public void onGuildJoin(@NotNull GuildJoinEvent event) {
        newGuild.onCommand(event, commandRegister); // always
        statusListener.updateGuild(event.getGuild());
    }

    @Override
    public void onGuildLeave(@NotNull GuildLeaveEvent event) {
    }

    /**
     * Ready
     */
    @Override
    public void onGuildReady(@NotNull GuildReadyEvent event) {
        commandRegister.onGuildReady(event); // register commands
        voiceChannelCreator.onGuildReady(event);
        statusListener.updateGuild(event.getGuild());
    }

    @Override
    public void onReady(@NotNull ReadyEvent event) {
        musicManager.setupAllBot();
        statusListener.startListen(event.getJDA());
    }

    /**
     * Guild Ban
     */

    @Override
    public void onGuildBan(@NotNull GuildBanEvent event) {
    }

    @Override
    public void onGuildUnban(@NotNull GuildUnbanEvent event) {
    }

    /**
     * Guild Invite
     */

    @Override
    public void onGuildInviteCreate(@NotNull GuildInviteCreateEvent event) {
    }

    @Override
    public void onGuildInviteDelete(@NotNull GuildInviteDeleteEvent event) {
    }

    /**
     * Guild Member
     */

    @Override
    public void onGuildMemberRemove(@NotNull GuildMemberRemoveEvent event) {
        joinLeaveMessage.onGuildMemberRemove(event);
        statusListener.memberLeave(event);
    }

    @Override
    public void onGuildMemberJoin(@NotNull GuildMemberJoinEvent event) {
        join.onGuildMemberJoin(event); // guild(own)
        joinLeaveMessage.onGuildMemberJoin(event);
        statusListener.memberJoin(event.getMember());
    }

    @Override
    public void onGuildMemberRoleAdd(@NotNull GuildMemberRoleAddEvent event) {
        log.onGuildMemberRoleAdd(event); // guild(own)
    }

    @Override
    public void onGuildMemberRoleRemove(@NotNull GuildMemberRoleRemoveEvent event) {
        log.onGuildMemberRoleRemove(event); // guild(own)
    }

    @Override
    public void onGuildMemberUpdateBoostTime(@NotNull GuildMemberUpdateBoostTimeEvent event) {
        if (!event.getMember().getRoles().contains(boostedRole)) {
            guild.addRoleToMember(event.getMember(), boostedRole).queue();
        }
    }

    @Override
    public void onUserUpdateOnlineStatus(@NotNull UserUpdateOnlineStatusEvent event) {
        statusListener.statusChange(event);
    }

    /**
     * Guild Voice
     */

    @Override
    public void onGuildVoiceJoin(@NotNull GuildVoiceJoinEvent event) {
        room.onGuildVoiceJoin(event); // guild(own)
        log.onGuildVoiceJoin(event); // guild(own)
        voiceChannelCreator.onGuildVoiceJoin(event);
        statusListener.memberVoiceJoin(event.getMember());
    }

    @Override
    public void onGuildVoiceLeave(@NotNull GuildVoiceLeaveEvent event) {
        room.onGuildVoiceLeave(event); // guild(own)
        log.onGuildVoiceLeave(event); // guild(own)
        musicManager.onVoiceLeave(event);
        voiceChannelCreator.onGuildVoiceLeave(event);
        statusListener.memberVoiceLeave(event.getMember());
    }

    @Override
    public void onGuildVoiceMove(@NotNull GuildVoiceMoveEvent event) {
        room.onGuildVoiceMove(event); // guild(own)
        voiceChannelCreator.onGuildVoiceMove(event);
    }

    @Override
    public void onGuildVoiceStream(@NotNull GuildVoiceStreamEvent event) {
        statusListener.memberVoiceStream(event.getMember());
    }

    @Override
    public void onGuildVoiceVideo(@NotNull GuildVoiceVideoEvent event) {
        statusListener.memberVoiceVideo(event.getMember());
    }


    /**
     * Guild Command
     */

    @Override
    public void onButtonClick(@NotNull ButtonClickEvent event) {
        // CLASS:TYPE:USERID:xxx
        String[] args = event.getComponentId().split(":");
        if (!args[2].equals(event.getUser().getId()) && !args[2].equals("") && !args[2].equals("everyone")) {
            event.getInteraction().deferReply(true).addEmbeds(createEmbed("此為他人的按鈕", 0xFF0000)).queue();
            return;
        }
        createInviteCommand.onButton(event, args);
        quickUse.onButtonClick(event, args);
        informationReaction.onButtonClick(event, args);
        ticketChannel.onButtonClick(event, args);
        clearCommand.onButton(event, args);
        musicManager.onButton(event, args);
    }

    @SuppressWarnings("ConstantConditions")
    @Override
    public void onSlashCommand(@NotNull SlashCommandEvent event) {
        event.getInteraction().deferReply(true).queue();

        // 如果找不到伺服器 ->
        if (event.getGuild() == null) {
            if (debugMode) {
                System.out.println("[Private] " + event.getUser().getAsTag() + " issued command: " + event.getCommandString());
            }

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
            event.getHook().editOriginalEmbeds(createEmbed("目前無法處理此命令", 0xFF0000)).queue();
            return;
        }

        if (debugMode) {
            System.out.println("[" + event.getGuild().getName() + "] " + event.getUser().getAsTag() + " issued command: " + event.getCommandString());
        }

        // 取得輸入指令的頻道
        String channelID = event.getChannel().getId();

        int type;

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
                event.getHook().editOriginalEmbeds(createEmbed("請到指定位置使用此指令 (" + tagChannelID(authChannelID) + ")", 0xFF0000)).queue();
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
            case "helpannouncement" -> {
                helpCommand.onAnnouncementCommand(event);
                return;
            }
            case "surl" -> {
                sortURL.onCommand(event, false, null);
                return;
            }
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
            case "reload" -> {
                if (reload.onCommand(event))
                    reload(event.getGuild());
                return;
            }
            case "setting" -> {
                if (!hasPermission(Permission.ADMINISTRATOR, event, true))
                    return;
                switch (event.getSubcommandName()) {
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
        event.getHook().editOriginalEmbeds(createEmbed("目前無法處理此命令", 0xFF0000)).queue();
    }

    @Override
    public void onSelectionMenu(@NotNull SelectionMenuEvent event) {
        String[] args = event.getComponentId().split(":");
        if (!args[2].equals(event.getUser().getId()) && !args[2].equals(""))
            return;
        musicManager.onSelectMenu(event, args);
        popCat.onSelectTop(event, args);
        popCat.onSelectSpeed(event, args);
    }

    /**
     * Guild PrivateMessage
     */

    @Override
    public void onPrivateMessageReceived(@NotNull PrivateMessageReceivedEvent event) {
        join.onPrivateMessageReceived(event); // guild(own)
    }

    @Override
    public void onPrivateMessageUpdate(@NotNull PrivateMessageUpdateEvent event) {
    }

    @Override
    public void onPrivateMessageDelete(@NotNull PrivateMessageDeleteEvent event) {
    }

    @Override
    public void onPrivateMessageEmbed(@NotNull PrivateMessageEmbedEvent event) {
    }

    @Override
    public void onPrivateMessageReactionAdd(@NotNull PrivateMessageReactionAddEvent event) {
        join.onPrivateMessageReactionAdd(event); // guild(own)
    }

    @Override
    public void onPrivateMessageReactionRemove(@NotNull PrivateMessageReactionRemoveEvent event) {
    }

    public void reload(Guild guild) {
        commandRegister.getMainGuildVariable(guild); // guild(own)
    }
}