package main.java.command;

import main.java.funtion.JsonFileManager;
import net.dv8tion.jda.api.Permission;
import net.dv8tion.jda.api.entities.Member;
import net.dv8tion.jda.api.entities.MessageChannel;
import net.dv8tion.jda.api.entities.MessageEmbed;
import net.dv8tion.jda.api.entities.TextChannel;
import net.dv8tion.jda.api.events.interaction.SlashCommandEvent;
import net.dv8tion.jda.api.interactions.components.Button;
import org.json.JSONArray;
import org.json.JSONObject;

import java.time.OffsetDateTime;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static main.java.BotSetting.configFolder;
import static main.java.BotSetting.memberRole;
import static main.java.Main.emoji;
import static main.java.SlashCommandOption.CHANNEL_TAG;
import static main.java.SlashCommandOption.USER_TAG;
import static main.java.event.Join.memberData;
import static main.java.util.Funtions.*;
import static main.java.util.GuildUtil.guild;
import static main.java.util.JsonKeys.*;

public class VoiceChannel {

    public static JsonFileManager voiceChannelDataFile;
    public static JSONObject voiceChannelData;

    // channelOwnerID, channelID
    public static Map<String, String> vcChannelOwner = new HashMap<>();
    public static Map<String, String> tcChannelOwner = new HashMap<>();
    public static List<String> textChannel = new ArrayList<>();


    public VoiceChannel() {
        voiceChannelDataFile = new JsonFileManager(configFolder + "/voiceChannel.json");
        voiceChannelData = voiceChannelDataFile.data;

        for (String ownerID : voiceChannelData.keySet()) {
            JSONObject channelData = voiceChannelData.getJSONObject(ownerID);
            vcChannelOwner.put(channelData.getString(VOICE_CHANNEL_ID), ownerID);
            tcChannelOwner.put(channelData.getString(TEXT_CHANNEL_ID), ownerID);
            textChannel.add(channelData.getString(TEXT_CHANNEL_ID));
        }
    }

    private int commandState;

    public int onVoiceChannelCommand(SlashCommandEvent event) {
        commandState = 1;
        switch (event.getName()) {
            // Voice Channel
            case "unpromote":
                unpromote(event);
                break;
            case "promote":
                promote(event);
                break;
            case "state":
            case "status":
            case "info":
                if (inVC(event))
                    info(event);
                break;
            case "public":
                makeChannelPublic(event);
                break;
            case "private":
                makeChannelPrivate(event);
                break;
            case "invite":
                if (inVC(event))
                    invite(event);
                break;
            case "black":
                if (inVC(event))
                    black(event);
                break;
            case "remove":
                if (inVC(event))
                    remove(event);
                break;
            default:
                commandState = 0;
        }
        return commandState;
    }

    public void unpromote(SlashCommandEvent event) {

        Member targetMember = event.getOption(USER_TAG).getAsMember();

        String senderID = event.getUser().getId();

        JSONObject channelData = voiceChannelData.getJSONObject(senderID);

        // 判斷合理性
        if (senderID.equals(targetMember.getId())) {
            event.replyEmbeds(createEmbed("你不能移除自己的權限!", 0xFF0000)).setEphemeral(true).queue();
            return;
        }

        JSONArray admins;
        if (channelData.has(CHANNEL_ADMINS))
            admins = channelData.getJSONArray(CHANNEL_ADMINS);
        else {
            event.replyEmbeds(createEmbed("此頻道無任何管理員存在", 0xFF0000)).setEphemeral(true).queue();
            return;
        }

        int index = -1;
        for (int i = 0; i < admins.length(); i++) {
            if (admins.getString(i).equals(targetMember.getId())) {
                index = i;
                break;
            }
        }

        if (index > -1) {
            admins.remove(index);
            channelData.put(CHANNEL_ADMINS, admins);
            voiceChannelDataFile.saveFile();
            event.replyEmbeds(createEmbed("權限移除成功", 0x7fc89a)).setEphemeral(true).queue();
        } else {
            event.replyEmbeds(createEmbed("他並沒有權限", 0xFF0000)).setEphemeral(true).queue();
            return;
        }
    }

    public void promote(SlashCommandEvent event) {

        Member targetMember = event.getOption(USER_TAG).getAsMember();

        String senderID = event.getUser().getId();

        // 判斷合理性

        if (senderID.equals(targetMember.getId())) {
            event.replyEmbeds(createEmbed("你不能提拔自己!", 0xFF0000)).setEphemeral(true).queue();
            return;
        }

        event.replyEmbeds(createEmbed("您確定要將管理員權限給 " + memberData.getJSONObject(targetMember.getId()).getString(CHINESE_NICK) + " ?", 0x9740b9))
                .addActionRow(// add component
                        Button.danger(senderID + ":vc_giveAdmin:" + targetMember.getId() + ":" + voiceChannelData.getJSONObject(senderID).getString(VOICE_CHANNEL_ID), "Yes!"))
                .setEphemeral(true).queue();

    }

    public void makeChannelPrivate(SlashCommandEvent event) {
        String senderID = event.getUser().getId();
        JSONObject channelData = voiceChannelData.getJSONObject(senderID);

        net.dv8tion.jda.api.entities.VoiceChannel vc = guild.getVoiceChannelById(channelData.getString(VOICE_CHANNEL_ID));
        TextChannel tc = guild.getTextChannelById(channelData.getString(TEXT_CHANNEL_ID));

        if (!(vc.getPermissionOverride(memberRole).getAllowed().contains(Permission.VIEW_CHANNEL))) {
            event.replyEmbeds(createEmbed("頻道已設置已經是不公開了", 0xFF0000)).setEphemeral(true).queue();
            return;
        }

        vc.upsertPermissionOverride(memberRole).setDeny(Permission.VIEW_CHANNEL).queue();
        tc.upsertPermissionOverride(memberRole).setDeny(Permission.VIEW_CHANNEL).queue();


        event.replyEmbeds(createEmbed("頻道已設置為不公開", 0xb8d8be)).setEphemeral(true).queue();
    }

    public void makeChannelPublic(SlashCommandEvent event) {
        String senderID = event.getUser().getId();
        JSONObject channelData = voiceChannelData.getJSONObject(senderID);
        String voiceChannelID = channelData.getString(VOICE_CHANNEL_ID);
        String textChannelID = channelData.getString(TEXT_CHANNEL_ID);

        net.dv8tion.jda.api.entities.VoiceChannel vc = guild.getVoiceChannelById(voiceChannelID);
        TextChannel tc = guild.getTextChannelById(textChannelID);

        if ((vc.getPermissionOverride(memberRole).getAllowed().contains(Permission.VIEW_CHANNEL))) {
            event.replyEmbeds(createEmbed("頻道已設置已經是公開了", 0xFF0000)).setEphemeral(true).queue();
            return;
        }

        vc.upsertPermissionOverride(memberRole).setAllow(Permission.VIEW_CHANNEL).queue();
        tc.upsertPermissionOverride(memberRole).setAllow(Permission.VIEW_CHANNEL).queue();

        event.replyEmbeds(createEmbed("頻道已設置為公開", 0x7fc89a)).setEphemeral(true).queue();
    }

    public void black(SlashCommandEvent event) {

        String senderID = event.getUser().getId();
        JSONObject channelData = voiceChannelData.getJSONObject(senderID);
        String textChannelID = channelData.getString(TEXT_CHANNEL_ID);
        String voiceChannelID = channelData.getString(VOICE_CHANNEL_ID);

        net.dv8tion.jda.api.entities.VoiceChannel vc = event.getMember().getVoiceState().getChannel();
        TextChannel tc = guild.getTextChannelById(textChannelID);

        Member member = event.getOption(USER_TAG).getAsMember();

        JSONObject ownerInfo = voiceChannelData.getJSONObject(senderID);

        if (event.getMember().getVoiceState().getChannel().getId().equals(voiceChannelID)) { // 群主
            vc.createPermissionOverride(member).setDeny(Permission.VIEW_CHANNEL).queue();
            tc.createPermissionOverride(member).setDeny(Permission.VIEW_CHANNEL).queue();

            if (member.getVoiceState().inVoiceChannel() && member.getVoiceState().getChannel().getId().equals(voiceChannelID))
                guild.kickVoiceMember(member);
            event.replyEmbeds(createEmbed("已踢除", 0xb8d8be)).setEphemeral(true).queue();
            return;
        } else if (ownerInfo.has(CHANNEL_ADMINS) && ownerInfo.getJSONArray(CHANNEL_ADMINS).toList().contains(event.getId())) {
            if (ownerInfo.getJSONArray(CHANNEL_ADMINS).toList().contains(member.getId())) {
                event.replyEmbeds(createEmbed("你無法踢出具有管理員權限的人", 0xFF0000)).setEphemeral(true).queue();
                return;
            } else {
                vc.createPermissionOverride(member).setDeny(Permission.VIEW_CHANNEL).queue();
                tc.createPermissionOverride(member).setDeny(Permission.VIEW_CHANNEL).queue();

                if (member.getVoiceState().inVoiceChannel() && member.getVoiceState().getChannel().getId().equals(voiceChannelID))
                    guild.kickVoiceMember(member);
                event.replyEmbeds(createEmbed("已踢除", 0xb8d8be)).setEphemeral(true).queue();
            }
        } else {
            event.replyEmbeds(createEmbed("你並不是語音群主或是管理員", 0xFF0000)).setEphemeral(true).queue();
            return;
        }
    }

    public void remove(SlashCommandEvent event) {

        String senderID = event.getUser().getId();
        JSONObject channelData = voiceChannelData.getJSONObject(senderID);
        String textChannelID = channelData.getString(TEXT_CHANNEL_ID);
        String voiceChannelID = channelData.getString(VOICE_CHANNEL_ID);

        net.dv8tion.jda.api.entities.VoiceChannel vc = event.getMember().getVoiceState().getChannel();
        TextChannel tc = guild.getTextChannelById(textChannelID);

        Member member = event.getOption(USER_TAG).getAsMember();

        JSONObject ownerInfo = voiceChannelData.getJSONObject(senderID);

        if (event.getMember().getVoiceState().getChannel().getId().equals(voiceChannelID)) { // 群主
            vc.createPermissionOverride(member).reset().queue();
            tc.createPermissionOverride(member).reset().queue();

            if (member.getVoiceState().inVoiceChannel() && member.getVoiceState().getChannel().getId().equals(voiceChannelID))
                guild.kickVoiceMember(member);
            event.replyEmbeds(createEmbed("已從房間名單移除", 0xb8d8be)).setEphemeral(true).queue();
            return;
        } else if (ownerInfo.has(CHANNEL_ADMINS) && ownerInfo.getJSONArray(CHANNEL_ADMINS).toList().contains(event.getId())) {
            if (ownerInfo.getJSONArray(CHANNEL_ADMINS).toList().contains(member.getId())) {
                event.replyEmbeds(createEmbed("你無法移除具有管理員權限的人", 0xFF0000)).setEphemeral(true).queue();
                return;
            } else {
                vc.createPermissionOverride(member).setDeny(Permission.VIEW_CHANNEL).queue();
                tc.createPermissionOverride(member).setDeny(Permission.VIEW_CHANNEL).queue();

                if (member.getVoiceState().inVoiceChannel() && member.getVoiceState().getChannel().getId().equals(voiceChannelID))
                    guild.kickVoiceMember(member);
                event.replyEmbeds(createEmbed("已移除", 0xb8d8be)).setEphemeral(true).queue();
            }
        } else {
            event.replyEmbeds(createEmbed("你並不是語音群主或是管理員", 0xFF0000)).setEphemeral(true).queue();
            return;
        }
    }

    public void invite(SlashCommandEvent event) {
        String senderID = event.getUser().getId();
        JSONObject channelData = voiceChannelData.getJSONObject(senderID);
        String textChannelID = channelData.getString(TEXT_CHANNEL_ID);
        String voiceChannelID = channelData.getString(VOICE_CHANNEL_ID);

        net.dv8tion.jda.api.entities.VoiceChannel vc = event.getMember().getVoiceState().getChannel();
        TextChannel tc = guild.getTextChannelById(textChannelID);

        Member member = event.getOption(USER_TAG).getAsMember();

        JSONObject ownerInfo = voiceChannelData.getJSONObject(senderID);

        if ((ownerInfo.has(CHANNEL_ADMINS) && ownerInfo.getJSONArray(CHANNEL_ADMINS).toList().contains(event.getId())) || !event.getMember().getVoiceState().getChannel().getId().equals(voiceChannelID)) {
            event.replyEmbeds(createEmbed("你並不是語音群主或是管理員", 0xFF0000)).setEphemeral(true).queue();
            return;
        }

        vc.createPermissionOverride(member).setAllow(Permission.VIEW_CHANNEL).queue();
        tc.createPermissionOverride(member).setAllow(Permission.VIEW_CHANNEL).queue();

        event.replyEmbeds(createEmbed("已邀請", 0xb8d8be)).setEphemeral(true).queue();

    }

    public void info(SlashCommandEvent event) {
        String vcOwnerID;
        String vcName;
        if (event.getOption(CHANNEL_TAG) != null) {
            MessageChannel tc = event.getOption(CHANNEL_TAG).getAsMessageChannel();
            vcOwnerID = tcChannelOwner.get(tc.getId());
            vcName = guild.getVoiceChannelById(voiceChannelData.getJSONObject(vcOwnerID).getString(VOICE_CHANNEL_ID)).getName();
        } else {
            net.dv8tion.jda.api.entities.VoiceChannel senderVC = event.getMember().getVoiceState().getChannel();
            vcOwnerID = vcChannelOwner.get(senderVC.getId());
            vcName = senderVC.getName();
        }
        JSONObject vcInfo = voiceChannelData.getJSONObject(vcOwnerID);

        StringBuilder adminsBuilder = new StringBuilder();

        // to String
        if (vcInfo.has(CHANNEL_ADMINS))
            vcInfo.getJSONArray(CHANNEL_ADMINS).toList().forEach((obj) -> {
                String memberID = (String) obj;
                adminsBuilder.append(tagUser(memberID))
                        .append(System.lineSeparator());
            });
        else
            adminsBuilder.append("無");


        Member vcOwner = guild.retrieveMemberById(vcOwnerID).complete();
        List<MessageEmbed.Field> fields = new ArrayList<>();
        fields.add(new MessageEmbed.Field(emoji.dot4.getAsMention() + " 主人: ", tagUser(vcOwnerID), false));
        fields.add(new MessageEmbed.Field(emoji.dot7.getAsMention() + " 管理員: ", adminsBuilder.toString(), false));

        event.replyEmbeds(createEmbed(
                vcName + " 的資訊", null,
                "頻道資訊",
                vcOwner.getNickname(), vcOwner.getUser().getAvatarUrl(),
                fields,
                OffsetDateTime.now(), 0xFFD1DC
        )).setEphemeral(true).queue();
    }

    private boolean inVC(SlashCommandEvent event) {
        // 不在語音頻道內或指令頻道
        if (!event.getMember().getVoiceState().inVoiceChannel()) {
            event.replyEmbeds(createEmbed("你並沒有處於任何語音頻道", 0xFF0000)).setEphemeral(true).queue();
            commandState = -1;
            return false;
        }

        // 確認有沒有在專屬於語音頻道的文字頻道內發送指令
        net.dv8tion.jda.api.entities.VoiceChannel senderVC = event.getMember().getVoiceState().getChannel();
        // 沒有在任何的專屬語音頻道
        if (!vcChannelOwner.containsKey(senderVC.getId())) {
            event.replyEmbeds(createEmbed("請在房間語音頻道內使用此指令", 0xFF0000)).setEphemeral(true).queue();
            commandState = -1;
            return false;
        }

        String vcOwnerID = vcChannelOwner.get(senderVC.getId());
        JSONObject vcInfo = voiceChannelData.getJSONObject(vcOwnerID);
        String vcTextChannel = vcInfo.getString(TEXT_CHANNEL_ID);
        // 如果不在指令頻道也不在專屬的頻道
        if (!vcTextChannel.equals(event.getChannel().getId())) {
            event.replyEmbeds(createEmbed("請到 " + tagChannel(vcTextChannel) + " 來執行此指令", 0xFF0000)).setEphemeral(true).queue();
            commandState = -1;
            return false;
        }
        return true;
    }

    public void onButton(String[] args) {
        if (args[1].equals("vc_giveAdmin")) {
            JSONObject ownerInfo = voiceChannelData.getJSONObject(args[0]);

            JSONArray admins;
            if (ownerInfo.has(CHANNEL_ADMINS))
                admins = ownerInfo.getJSONArray(CHANNEL_ADMINS);
            else
                admins = new JSONArray();

            boolean haveNoYet = true;
            for (Object i : admins) {
                if (i.equals(args[2])) {
                    haveNoYet = false;
                    break;
                }
            }
            if (haveNoYet) {
                admins.put(args[2]);
                ownerInfo.put(CHANNEL_ADMINS, admins);
                voiceChannelDataFile.saveFile();
            }
        }
    }
}