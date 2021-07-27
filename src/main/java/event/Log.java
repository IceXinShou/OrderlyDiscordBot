package main.java.event;

import main.java.Main;
import main.java.funtion.ChannelLogHelper;
import main.java.funtion.JsonFileManager;
import net.dv8tion.jda.api.entities.Member;
import net.dv8tion.jda.api.entities.Message;
import net.dv8tion.jda.api.entities.TextChannel;
import net.dv8tion.jda.api.events.guild.member.GuildMemberRoleAddEvent;
import net.dv8tion.jda.api.events.guild.member.GuildMemberRoleRemoveEvent;
import net.dv8tion.jda.api.events.guild.voice.GuildVoiceJoinEvent;
import net.dv8tion.jda.api.events.guild.voice.GuildVoiceLeaveEvent;
import net.dv8tion.jda.api.events.interaction.ButtonClickEvent;
import net.dv8tion.jda.api.events.message.guild.GuildMessageDeleteEvent;
import net.dv8tion.jda.api.events.message.guild.GuildMessageReceivedEvent;
import net.dv8tion.jda.api.events.message.guild.GuildMessageUpdateEvent;
import net.dv8tion.jda.api.events.message.guild.react.GuildMessageReactionAddEvent;
import net.dv8tion.jda.api.events.message.guild.react.GuildMessageReactionRemoveEmoteEvent;
import net.dv8tion.jda.api.events.message.guild.react.GuildMessageReactionRemoveEvent;
import net.dv8tion.jda.api.hooks.ListenerAdapter;
import net.dv8tion.jda.api.interactions.InteractionHook;
import net.dv8tion.jda.api.requests.restaction.interactions.ReplyAction;
import org.jetbrains.annotations.NotNull;
import org.json.JSONObject;

import java.time.OffsetDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.TimeUnit;

import static main.java.Main.emoji;
import static main.java.util.Funtions.createEmbed;
import static main.java.util.GuildUtil.guild;
import static main.java.util.GuildUtil.guildID;
import static main.java.util.JsonKeys.*;

public class Log extends ListenerAdapter {
    public static TextChannel logChannel;
    public static String logChannelID;
    public static TextChannel consoleChannel;
    public static String consoleChannelID;
    public static List<String> ignoreMessageID = new ArrayList<>();

    ChannelLogHelper clh = new ChannelLogHelper();

    /**
     * 語音事件
     */

    @Override
    public void onGuildVoiceJoin(@NotNull GuildVoiceJoinEvent event) {
        if (!event.getGuild().getId().equals(guildID)) return;
        logChannel.sendMessageEmbeds(
                createEmbed(
                        (emoji.rightArrow.getAsMention() + " 進入 " +
                                event.getChannelJoined().getName()), null,
                        "進入房間",
                        event.getMember().getNickname() == null ? event.getMember().getUser().getAsTag() : event.getMember().getNickname(), event.getMember().getUser().getAvatarUrl(),
                        OffsetDateTime.now(), 0x34E718
                )
        ).queue();
    }

    @Override
    public void onGuildVoiceLeave(@NotNull GuildVoiceLeaveEvent event) {
        if (!event.getGuild().getId().equals(guildID)) return;
        logChannel.sendMessageEmbeds(
                createEmbed(
                        emoji.leftArrow.getAsMention() +
                                " 退出 " + event.getChannelLeft().getName()
                        , null,
                        "退出房間",
                        event.getMember().getNickname() == null ? event.getMember().getUser().getAsTag() : event.getMember().getNickname(), event.getMember().getUser().getAvatarUrl(),
                        OffsetDateTime.now(), 0xFF5151
                )
        ).queue();
    }

    /**
     * 訊息事件
     */

    @Override
    public void onGuildMessageReceived(@NotNull GuildMessageReceivedEvent event) {
        String channelID = event.getMessage().getChannel().getId();

        if (channelID.equals(consoleChannelID) || channelID.equals(logChannelID) || event.getMessage().getContentRaw().length() == 0)
            return;
        JsonFileManager channelFileManager = clh.getChannelFileManager(channelID);
        JSONObject data = channelFileManager.data;
        JSONObject messageContent = new JSONObject()
                .put(MESSAGE, event.getMessage().getContentRaw()) // Message
                .put(SENDER, event.getAuthor().getId()) // Sender
                .put(TIME, event.getMessage().getTimeCreated()) // Time
                ;
        data.put(event.getMessage().getId(), messageContent);
        channelFileManager.saveFile();
    }

    @Override
    public void onGuildMessageUpdate(@NotNull GuildMessageUpdateEvent event) {
        if (!event.getGuild().getId().equals(guildID)) return;

        JsonFileManager channelFileManager = clh.getChannelFileManager(event.getChannel().getId());
        JSONObject data = channelFileManager.data;
        JSONObject messageContent = new JSONObject()
                .put(MESSAGE, event.getMessage().getContentRaw()) // Message
                .put(SENDER, event.getAuthor().getId()) // Sender
                .put(TIME, event.getMessage().getTimeCreated()) // Time
                ;
        data.put(event.getMessage().getId(), messageContent);
        channelFileManager.saveFile();

        logChannel.sendMessageEmbeds(
                createEmbed(
                        "更改訊息", null,
                        "更改訊息",
                        event.getMember().getNickname() == null ? event.getMember().getUser().getAsTag() : event.getMember().getNickname(), event.getMember().getUser().getAvatarUrl(),
                        OffsetDateTime.now(), 0x51FFFF
                )
        ).queue();
    }

    @Override
    public void onGuildMessageDelete(@NotNull GuildMessageDeleteEvent event) {
        if (!event.getGuild().getId().equals(guildID)) return;
        if (ignoreMessageID.contains(event.getMessageId())) return;

        // get message
        JSONObject channelLog = clh.getChannelFileManager(event.getChannel().getId()).data;
        String nickname, avatarUrl, message, dateString;
        // 有訊息資料
        if (channelLog.has(event.getMessageId())) {
            // 取得訊息資料
            JSONObject messageLog = channelLog.getJSONObject(event.getMessageId());
            DateTimeFormatter dateTimeFormatter = DateTimeFormatter.ofPattern("yyyy/MM/dd HH:mm");
            // get message time
            OffsetDateTime offsetDateTime;
            if (messageLog.get(TIME) instanceof String)
                offsetDateTime = OffsetDateTime.parse(messageLog.getString(TIME));
            else
                offsetDateTime = (OffsetDateTime) messageLog.get(TIME);
            dateString = dateTimeFormatter.format(offsetDateTime);

            // get user nickname
            Member member = guild.retrieveMemberById(messageLog.getString(SENDER)).complete();
            if (member == null) {
                nickname = "未知姓名";
                avatarUrl = Main.self.getDefaultAvatarUrl();
            } else {
                avatarUrl = member.getUser().getAvatarUrl();
                nickname = member.getNickname();
                if (nickname == null)
                    nickname = member.getUser().getAsTag();
            }
            message = messageLog.getString(MESSAGE);
        } else {
            nickname = "未知姓名";
            message = "未知訊息";
            dateString = "未知時間";
            avatarUrl = Main.self.getDefaultAvatarUrl();
        }

        logChannel.sendMessageEmbeds(
                createEmbed(event.getChannel().getName()
                        , message,
                        "刪除訊息",
                        nickname + " • " + dateString, avatarUrl,
                        OffsetDateTime.now(), 0xFF5151
                )
        ).queue();
    }

    /**
     * 身分組事件
     */

    @Override
    public void onGuildMemberRoleAdd(@NotNull GuildMemberRoleAddEvent event) {
        if (!event.getGuild().getId().equals(guildID)) return;
        logChannel.sendMessageEmbeds(
                createEmbed(
                        "新增權限: ", event.getRoles().get(0).toString(),
                        "新增權限",
                        event.getMember().getNickname(), event.getMember().getUser().getAvatarUrl(), OffsetDateTime.now(), 0xFF5151
                )
        ).queue();
    }

    @Override
    public void onGuildMemberRoleRemove(@NotNull GuildMemberRoleRemoveEvent event) {
        if (!event.getGuild().getId().equals(guildID)) return;
    }

    @Override
    public void onGuildMessageReactionAdd(@NotNull GuildMessageReactionAddEvent event) {
        if (!event.getGuild().getId().equals(guildID)) return;
    }

    /**
     * 反應事件
     */

    @Override
    public void onGuildMessageReactionRemove(@NotNull GuildMessageReactionRemoveEvent event) {
        if (!event.getGuild().getId().equals(guildID)) return;
    }

    @Override
    public void onGuildMessageReactionRemoveEmote(@NotNull GuildMessageReactionRemoveEmoteEvent event) {
        if (!event.getGuild().getId().equals(guildID)) return;
    }


    /**
     * 錯誤
     */

    public static void logError(String errorMessage) {
        if (errorMessage == null)
            logChannel.sendMessageEmbeds(
                    createEmbed(
                            "ERROR!!!", errorMessage,
                            "錯誤",
                            Main.botNickname, Main.botAvatarUrl,
                            OffsetDateTime.now(), 0xFF5151
                    )
            ).queue();
    }

    public static void deleteNoLog(ReplyAction action, long delaySec) {
        action.queue(act -> {
            ignoreMessageID.add(act.getInteraction().getId());
            act.deleteOriginal().queueAfter(delaySec, TimeUnit.SECONDS);
        });
    }

    public static void deleteNoLog(InteractionHook message, long delaySec) {
        ignoreMessageID.add(message.getInteraction().getId());
        message.deleteOriginal().queueAfter(delaySec, TimeUnit.SECONDS);
    }

    public static void deleteNoLog(Message message, long delaySec) {
        ignoreMessageID.add(message.getId());
        message.delete().queueAfter(delaySec, TimeUnit.SECONDS);
    }

    public static void deleteNoLog(Message message) {
        ignoreMessageID.add(message.getId());
        message.delete().queue();
    }

    public static void deleteNoLog(ButtonClickEvent event) {
        ignoreMessageID.add(event.getMessageId());
        event.getHook().deleteOriginal().queue();
    }
//    @Override
//    public void onGenericTextChannel(@NotNull GenericTextChannelEvent event) {
//        super.onGenericTextChannel(event);
//        if (!guild.getId().equals(Main.guildID)) return;
//    }
//
//    @Override
//    public void onGenericTextChannelUpdate(@NotNull GenericTextChannelUpdateEvent event) {
//        super.onGenericTextChannelUpdate(event);
//        if (!guild.getId().equals(Main.guildID)) return;
//    }
//
//    @Override
//    public void onGenericVoiceChannel(@NotNull GenericVoiceChannelEvent event) {
//        super.onGenericVoiceChannel(event);
//        if (!guild.getId().equals(Main.guildID)) return;
//    }
//
//    @Override
//    public void onGenericVoiceChannelUpdate(@NotNull GenericVoiceChannelUpdateEvent event) {
//        super.onGenericVoiceChannelUpdate(event);
//        if (!guild.getId().equals(Main.guildID)) return;
//    }
//
//    @Override
//    public void onGenericEmote(@NotNull GenericEmoteEvent event) {
//        super.onGenericEmote(event);
//        if (!guild.getId().equals(Main.guildID)) return;
//    }
//
//    @Override
//    public void onGenericEmoteUpdate(@NotNull GenericEmoteUpdateEvent event) {
//        super.onGenericEmoteUpdate(event);
//        if (!guild.getId().equals(Main.guildID)) return;
//    }
}
