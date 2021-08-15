package main.java.command.list;

import main.java.util.file.GuildSettingHelper;
import net.dv8tion.jda.api.MessageBuilder;
import net.dv8tion.jda.api.Permission;
import net.dv8tion.jda.api.entities.Emoji;
import net.dv8tion.jda.api.entities.Guild;
import net.dv8tion.jda.api.entities.Member;
import net.dv8tion.jda.api.entities.Role;
import net.dv8tion.jda.api.events.interaction.ButtonClickEvent;
import net.dv8tion.jda.api.interactions.components.ActionRow;
import net.dv8tion.jda.api.interactions.components.Button;
import net.dv8tion.jda.api.interactions.components.ButtonStyle;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.json.JSONObject;

import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static main.java.Main.emoji;
import static main.java.util.EmbedCreator.createEmbed;
import static main.java.util.JsonKeys.*;
import static main.java.util.PermissionERROR.noPermissionERROREmbed;
import static main.java.util.PlaceholderReplacer.placeholderReplacer;
import static main.java.util.Tag.tagRoleID;

public class Ticket {

    //               ChannelID MessageID ButtonPosition Count
    private final Map<String, Map<String, Map<Byte, Integer>>> ticketCount = new HashMap<>(); // %num%

    //                 UserID    ChannelID+MessageID ButtonPosition
    private final Map<String, Map<String, List<Byte>>> userCount = new HashMap<>();

    public void onButtonClick(@NotNull ButtonClickEvent event, String @NotNull [] args, GuildSettingHelper settingHelper) {
        if (!args[0].equals("Ticket"))
            return;
        Guild guild = event.getGuild();
        Member member = event.getMember();
        if (args[1].equals("newTicket")) {
            JSONObject datas;
            if ((datas = getSettingData(guild, settingHelper)) == null || !datas.has(event.getChannel().getId()))
                return;
            JSONObject data = datas.getJSONObject(channelID).getJSONArray(messageID).getJSONObject(buttonPos);

            String messageKey = channelID + messageID;
            boolean buttonPressed = addButtonPress(messageKey, member.getId(), buttonPos);

            if (data.getBoolean(TICKET_ONLY_ONE) && buttonPressed) {
                event.getInteraction().deferReply(true).addEmbeds(createEmbed("您已經使用過此按扭了", 0xFF0000)).queue();
                return;
            }

            Role allowRole = guild.getRoleById(data.getString(TICKET_ALLOW_ROLE_ID));

            if (guild.getCategoryById(data.getString(TICKET_TEXT_CATEGORY_ID)).getChannels().size() == 50) {
                event.getInteraction().deferReply(true).addEmbeds(createEmbed("沒有足夠的空間 (類別中頻道數量達到最大值)", 0xFF0000)).queue();
                return;
            }

            Map<String, Map<Byte, Integer>> messageIDMap;
            Map<Byte, Integer> positionMap;
            if ((messageIDMap = ticketCount.get(channelID)) == null) {
                messageIDMap = new HashMap<>();
                ticketCount.put(channelID, messageIDMap);
            }
            if ((positionMap = messageIDMap.get(event.getMessageId())) == null) {
                positionMap = new HashMap<>();
                messageIDMap.put(event.getMessageId(), positionMap);
            }

            // add
            final int count = positionMap.getOrDefault(Byte.valueOf(args[3]), 0) + 1;
            String countStr = String.valueOf(count);
            positionMap.put(Byte.valueOf(args[3]), count);
            guild.createTextChannel(
                    placeholderReplacer(data.getString(TICKET_TEXT_NAME), member).replace("%num%", countStr),
                    guild.getCategoryById(data.getString(TICKET_TEXT_CATEGORY_ID))).queue(tc -> {
                if (data.has(VC_VOICE_CHANNEL_ID)) {
                    guild.createVoiceChannel(
                            placeholderReplacer(data.getString(TICKET_VOICE_NAME), member).replace("%num%", countStr),
                            guild.getCategoryById(data.getString(TICKET_VOICE_CATEGORY_ID))).queue(vc -> {
                        vc.createPermissionOverride(guild.getPublicRole()).setDeny(Permission.VIEW_CHANNEL).queue();
                        vc.createPermissionOverride(allowRole).setAllow(Permission.VIEW_CHANNEL, Permission.MANAGE_CHANNEL).queue();
                        vc.createPermissionOverride(member).setAllow(Permission.VIEW_CHANNEL).queue();
                    });
                }
                tc.putPermissionOverride(guild.getPublicRole()).setDeny(Permission.VIEW_CHANNEL).queue();
                tc.createPermissionOverride(member).setAllow(Permission.VIEW_CHANNEL).queue();
                tc.createPermissionOverride(allowRole).setAllow(Permission.VIEW_CHANNEL, Permission.MANAGE_CHANNEL, Permission.MESSAGE_WRITE).queue();

                MessageBuilder builder = new MessageBuilder();
                builder.setContent(placeholderReplacer(data.getString(TICKET_ENTERED_MESSAGE), member)
                        .replace("%role%", tagRoleID(data.getString(TICKET_ALLOW_ROLE_ID)))
                        .replace("%role_name%", allowRole.getName())
                        .replace("%num%", countStr)
                        + "_\u200B".repeat(397) + '_' + (data.getBoolean(TICKET_ALLOW_TAG) ? tagRoleID(data.getString(TICKET_ALLOW_ROLE_ID)) : "") + member.getAsMention());
                builder.setActionRows(ActionRow.of(
                        Button.of(ButtonStyle.PRIMARY, "Ticket:lock::" + buttonPos + ':' + member.getId() + ':' + allowRole.getId() + ":" + messageKey, "封存", Emoji.fromUnicode("\uD83D\uDCC1")),
                        Button.of(ButtonStyle.DANGER, "Ticket:delC::" + buttonPos + ":" + member.getId() + ':' + messageKey, "刪除", Emoji.fromEmote(emoji.trashCan))
                ));
                tc.sendMessage(builder.build()).queue();
                event.deferEdit().queue();
            });
        } else if (args[1].equals("delC")) {
            if (member.hasPermission(Permission.MANAGE_CHANNEL)) {
                event.getTextChannel().delete().queue();
                removeButtonPress(args, buttonPos);
            } else
                event.getInteraction().deferReply(true).addEmbeds(noPermissionERROREmbed(Permission.MANAGE_CHANNEL)).queue();
        } else if (args[1].equals("lock")) {
            if (member.hasPermission(Permission.MANAGE_CHANNEL) || member.getRoles().contains(guild.getRoleById(args[5]))) {
                event.getHook().editOriginalEmbeds().setActionRows(
                        ActionRow.of(
                                Button.of(ButtonStyle.SUCCESS, "Ticket:uLock::" + args[3] + ':' + args[4] + ':' + args[5] + ':' + args[6], "解除封存", Emoji.fromUnicode("\uD83D\uDCC1")),
                                Button.of(ButtonStyle.DANGER, "Ticket:delC::" + args[3] + ':' + args[4] + ":" + args[6], "刪除", Emoji.fromEmote(emoji.trashCan))
                        )).queue();
                event.getTextChannel().putPermissionOverride(guild.getRoleById(args[5])).setDeny(Permission.MESSAGE_WRITE, Permission.MANAGE_CHANNEL).queue();
                event.deferEdit().queue();
            } else {
                event.getInteraction().deferReply(true).addEmbeds(noPermissionERROREmbed(Permission.MANAGE_CHANNEL)).queue();
            }
        } else if (args[1].equals("unlock")) {
            if (member.hasPermission(Permission.MANAGE_CHANNEL) || member.getRoles().contains(guild.getRoleById(args[5]))) {
                event.getHook().editOriginalEmbeds().setActionRows(
                        ActionRow.of(
                                Button.of(ButtonStyle.PRIMARY, "Ticket:lock::" + args[3] + ':' + args[4] + ':' + args[5] + ':' + args[6], "封存", Emoji.fromUnicode("\uD83D\uDCC1")),
                                Button.of(ButtonStyle.DANGER, "Ticket:delC::" + args[3] + ':' + args[4] + ':' + args[6], "刪除", Emoji.fromEmote(emoji.trashCan))
                        )).queue();
                event.getTextChannel().putPermissionOverride(guild.getRoleById(args[5])).setAllow(Permission.VIEW_CHANNEL, Permission.MESSAGE_WRITE, Permission.MANAGE_CHANNEL).queue();
                event.deferEdit().queue();
            } else {
                event.getInteraction().deferReply(true).addEmbeds(noPermissionERROREmbed(Permission.MANAGE_CHANNEL)).queue();
            }
        }
    }

    private @Nullable JSONObject getSettingData(@NotNull Guild guild, @NotNull GuildSettingHelper settingHelper) {
        if (settingHelper.getGuildSettingManager(guild.getId()).data.has(TICKET_SETTING))
            return settingHelper.getGuildSettingManager(guild.getId()).data.getJSONObject(TICKET_SETTING);
        else {
            return null;
        }
    }
}
