package main.java.command.list;

import main.java.Main;
import main.java.util.file.GuildSettingHelper;
import net.dv8tion.jda.api.MessageBuilder;
import net.dv8tion.jda.api.Permission;
import net.dv8tion.jda.api.entities.*;
import net.dv8tion.jda.api.events.interaction.ButtonClickEvent;
import net.dv8tion.jda.api.interactions.components.ActionRow;
import net.dv8tion.jda.api.interactions.components.Button;
import net.dv8tion.jda.api.interactions.components.ButtonStyle;
import org.jetbrains.annotations.NotNull;
import org.json.JSONObject;

import java.util.*;

import static main.java.Main.emoji;
import static main.java.lang.LangKey.*;
import static main.java.util.EmbedCreator.createEmbed;
import static main.java.util.JsonKeys.*;
import static main.java.util.PermissionController.addPermission;
import static main.java.util.PermissionController.removePermission;
import static main.java.util.PermissionERROR.noPermissionERROREmbed;
import static main.java.util.PlaceholderReplacer.placeholderReplacer;
import static main.java.util.Tag.tagRoleID;
import static net.dv8tion.jda.api.Permission.*;

public class Ticket {
    GuildSettingHelper settingHelper;

    public Ticket(GuildSettingHelper settingHelper) {
        this.settingHelper = settingHelper;
    }

    //               ChannelID MessageID ButtonPosition Count
    private final Map<String, Map<String, Map<Byte, Integer>>> ticketCount = new HashMap<>(); // %num%

    //                 UserID    ChannelID+MessageID ButtonPosition
    private final Map<String, Map<String, List<Byte>>> userCount = new HashMap<>();
    //                 TextID  VoiceID
    private final Map<String, String> linkedVoiceChannel = new HashMap<>();

    public void onButtonClick(@NotNull ButtonClickEvent event, String @NotNull [] args) {
        List<String> lang = Main.language.getGuildLang(event.getGuild().getId());
        if (!args[0].equals("Ticket"))
            return;
        Guild guild;
        if ((guild = event.getGuild()) == null)
            return;
        String channelID = event.getChannel().getId();
        String messageID = event.getMessageId();
        Member member = event.getMember();
        byte buttonPos = Byte.parseByte(args[3]);

        switch (args[1]) {
            case "newTicket":
                JSONObject nData;
                if ((nData = settingHelper.getSettingData(guild, TICKET_SETTING)) == null || !nData.has(event.getChannel().getId()))
                    return;
                JSONObject data = nData.getJSONObject(channelID).getJSONArray(messageID).getJSONObject(buttonPos);

                String messageKey = channelID + messageID;
                boolean buttonPressed = addButtonPress(messageKey, member.getId(), buttonPos);

                if (data.getBoolean(TICKET_ONLY_ONE) && buttonPressed) {
                    event.getInteraction().deferReply(true).addEmbeds(createEmbed(lang.get(TICKET_ALREADY_CLICKED), 0xFF0000)).queue();
                    return;
                }

                Role allowRole = guild.getRoleById(data.getString(TICKET_ALLOW_ROLE_ID));

                if (guild.getCategoryById(data.getString(TICKET_TEXT_CATEGORY_ID)).getChannels().size() == 50) {
                    event.getInteraction().deferReply(true).addEmbeds(createEmbed(lang.get(TICKET_SIZE_LIMIT), 0xFF0000)).queue();
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
                                guild.getCategoryById(data.getString(TICKET_TEXT_CATEGORY_ID))
                        )
                        .addMemberPermissionOverride(member.getIdLong(), Arrays.asList(VIEW_CHANNEL, MESSAGE_WRITE), Collections.emptyList())
                        .queue(tc -> {
                            if (data.has(VC_VOICE_CHANNEL_ID)) {
                                guild.createVoiceChannel(
                                                placeholderReplacer(data.getString(TICKET_VOICE_NAME), member).replace("%num%", countStr),
                                                guild.getCategoryById(data.getString(TICKET_VOICE_CATEGORY_ID)))
                                        .addMemberPermissionOverride(member.getIdLong(), Arrays.asList(VIEW_CHANNEL, VOICE_CONNECT), Collections.emptyList())
                                        .queue(vc -> {
                                            vc.getManager().clearOverridesAdded().queue();
                                            vc.getManager().clearOverridesRemoved().queue();
                                            linkedVoiceChannel.put(tc.getId(), vc.getId());
                                            removePermission(guild.getPublicRole(), vc, Collections.singletonList(VIEW_CHANNEL));
                                            addPermission(allowRole, vc, Arrays.asList(VIEW_CHANNEL, MANAGE_CHANNEL, VOICE_CONNECT));
                                        });
                            }
                            removePermission(guild.getPublicRole(), tc, Collections.singletonList(VIEW_CHANNEL));
                            addPermission(allowRole, tc, Arrays.asList(VIEW_CHANNEL, MANAGE_CHANNEL, MESSAGE_WRITE));

                            MessageBuilder builder = new MessageBuilder();
                            builder.setContent(placeholderReplacer(data.getString(TICKET_ENTERED_MESSAGE), member)
                                    .replace("%role%", tagRoleID(data.getString(TICKET_ALLOW_ROLE_ID)))
                                    .replace("%role_name%", allowRole.getName())
                                    .replace("%num%", countStr)
                                    + "_\u200B".repeat(397) + '_' + (data.getBoolean(TICKET_ALLOW_TAG) ? tagRoleID(data.getString(TICKET_ALLOW_ROLE_ID)) : "") + member.getAsMention());
                            builder.setActionRows(ActionRow.of(
                                    Button.of(ButtonStyle.PRIMARY, "Ticket:lock::" + buttonPos + ':' + member.getId() + ':' + allowRole.getId() + ":" + messageKey, lang.get(TICKET_LOCK), Emoji.fromUnicode("\uD83D\uDCC1")),
                                    Button.of(ButtonStyle.DANGER, "Ticket:delC::" + buttonPos + ":" + member.getId() + ':' + messageKey, lang.get(TICKET_DELETE), Emoji.fromEmote(emoji.trashCan))
                            ));
                            tc.sendMessage(builder.build()).queue();
                            event.deferEdit().queue();
                        });
                break;
            case "delC":
                if (member.hasPermission(Permission.MANAGE_CHANNEL)) {
                    String voiceChannelID;
                    VoiceChannel voiceChannel;
                    if ((voiceChannelID = linkedVoiceChannel.get(event.getTextChannel().getId())) != null && (voiceChannel = guild.getVoiceChannelById(voiceChannelID)) != null) {
                        voiceChannel.delete().queue();
                    }
                    event.getTextChannel().delete().queue();
                    removeButtonPress(args, buttonPos);
                } else
                    event.getInteraction().deferReply(true).addEmbeds(noPermissionERROREmbed(MANAGE_CHANNEL)).queue();
                break;
            case "lock":
                if (member.hasPermission(Permission.MANAGE_CHANNEL) || member.getRoles().contains(guild.getRoleById(args[5]))) {
                    event.getHook().editOriginalEmbeds().setActionRows(
                            ActionRow.of(
                                    Button.of(ButtonStyle.SUCCESS, "Ticket:uLock::" + args[3] + ':' + args[4] + ':' + args[5] + ':' + args[6], lang.get(TICKET_UNLOCK), Emoji.fromUnicode("\uD83D\uDCC1")),
                                    Button.of(ButtonStyle.DANGER, "Ticket:delC::" + args[3] + ':' + args[4] + ":" + args[6], lang.get(TICKET_DELETE), Emoji.fromEmote(emoji.trashCan))
                            )).queue();
                    removePermission(guild.getRoleById(args[5]), event.getGuildChannel(), Arrays.asList(MESSAGE_WRITE, MANAGE_CHANNEL));
                    removePermission(guild.retrieveMemberById(args[4]).complete(), event.getGuildChannel(), List.of(MESSAGE_WRITE));
                    String voiceChannelID;
                    VoiceChannel voiceChannel;
                    if ((voiceChannelID = linkedVoiceChannel.get(event.getTextChannel().getId())) != null && (voiceChannel = guild.getVoiceChannelById(voiceChannelID)) != null) {
                        removePermission(guild.getRoleById(args[5]), voiceChannel, Arrays.asList(VOICE_CONNECT, MANAGE_CHANNEL));
                        removePermission(guild.retrieveMemberById(args[4]).complete(), voiceChannel, List.of(VOICE_CONNECT));
                    }
                    event.deferEdit().queue();
                } else {
                    event.getInteraction().deferReply(true).addEmbeds(noPermissionERROREmbed(Permission.MANAGE_CHANNEL)).queue();
                }
                break;
            case "uLock":
                if (member.hasPermission(Permission.MANAGE_CHANNEL) || member.getRoles().contains(guild.getRoleById(args[5]))) {
                    event.getHook().editOriginalEmbeds().setActionRows(
                            ActionRow.of(
                                    Button.of(ButtonStyle.PRIMARY, "Ticket:lock::" + args[3] + ':' + args[4] + ':' + args[5] + ':' + args[6], lang.get(TICKET_LOCK), Emoji.fromUnicode("\uD83D\uDCC1")),
                                    Button.of(ButtonStyle.DANGER, "Ticket:delC::" + args[3] + ':' + args[4] + ':' + args[6], lang.get(TICKET_DELETE), Emoji.fromEmote(emoji.trashCan))
                            )).queue();
                    addPermission(guild.getRoleById(args[5]), event.getGuildChannel(), Arrays.asList(MESSAGE_WRITE, MANAGE_CHANNEL));
                    addPermission(guild.retrieveMemberById(args[4]).complete(), event.getGuildChannel(), List.of(MESSAGE_WRITE));
                    String voiceChannelID;
                    VoiceChannel voiceChannel;
                    if ((voiceChannelID = linkedVoiceChannel.get(event.getTextChannel().getId())) != null && (voiceChannel = guild.getVoiceChannelById(voiceChannelID)) != null) {
                        addPermission(guild.getRoleById(args[5]), voiceChannel, Arrays.asList(VOICE_CONNECT, MANAGE_CHANNEL));
                        addPermission(guild.retrieveMemberById(args[4]).complete(), voiceChannel, List.of(VOICE_CONNECT));
                    }
                    event.deferEdit().queue();
                } else {
                    event.getInteraction().deferReply(true).addEmbeds(noPermissionERROREmbed(Permission.MANAGE_CHANNEL)).queue();
                }
                break;
        }
    }

    public boolean isButtonUsed(String channelID, String messageID, Byte buttonPos) {
        String messageKey = channelID + messageID;
        for (Map<String, List<Byte>> userData : userCount.values()) {
            List<Byte> usedButton;
            if ((usedButton = userData.get(messageKey)) != null) {
                if (usedButton.contains(buttonPos))
                    return true;
            }
        }
        return false;
    }


    private boolean addButtonPress(String messageKey, String memberID, Byte buttonPos) {
        Map<String, List<Byte>> userPressedMessage;
        List<Byte> buttonPressed;
        if ((userPressedMessage = userCount.get(memberID)) == null) {
            userPressedMessage = new HashMap<>();
            buttonPressed = Collections.singletonList(buttonPos);
            userPressedMessage.put(messageKey, buttonPressed);
            userCount.put(memberID, userPressedMessage);
            return false;
        } else if ((buttonPressed = userPressedMessage.get(messageKey)) == null) {
            buttonPressed = Collections.singletonList(buttonPos);
            userPressedMessage.put(messageKey, buttonPressed);
            return false;
        } else if (!buttonPressed.contains(buttonPos)) {
            buttonPressed.add(buttonPos);
            return false;
        }
        return true;
    }

    private void removeButtonPress(String @NotNull [] args, byte buttonPos) {
        Map<String, List<Byte>> userPressedMessage;
        if ((userPressedMessage = userCount.get(args[4])) == null)
            return;
        List<Byte> data = userPressedMessage.get(args[5]);
        if (data.size() == 1)
            userPressedMessage.remove(args[5]);
        else
            data.remove((Byte) buttonPos);
        if (userPressedMessage.size() == 0)
            userCount.remove(args[4]);
    }
}
