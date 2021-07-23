package main.java.command;

import main.java.funtion.JsonFileManager;
import net.dv8tion.jda.api.Permission;
import net.dv8tion.jda.api.entities.Category;
import net.dv8tion.jda.api.entities.Member;
import net.dv8tion.jda.api.entities.Role;
import net.dv8tion.jda.api.entities.TextChannel;
import net.dv8tion.jda.api.events.interaction.ButtonClickEvent;
import net.dv8tion.jda.api.events.interaction.SlashCommandEvent;
import net.dv8tion.jda.api.interactions.components.Button;
import org.json.JSONArray;
import org.json.JSONObject;

import java.time.OffsetDateTime;
import java.util.ArrayList;
import java.util.Collection;

import static main.java.BotSetting.*;
import static main.java.Main.emoji;
import static main.java.SlashCommandOption.USER_TAG;
import static main.java.command.VoiceChannelCommand.*;
import static main.java.event.Join.memberData;
import static main.java.event.Log.logChannel;
import static main.java.util.Funtions.createEmbed;
import static main.java.util.GuildUtil.guild;
import static main.java.util.JsonKeys.*;

public class InviteCommand {
    public static String authChannelID;
    public static TextChannel authChannel;
    public static JSONObject genealogyData;
    private final JsonFileManager genealogyFile;

    /**
     * new class 的時候執行創建變數與取得資料
     */

    public InviteCommand() {
        // 族譜
        genealogyFile = new JsonFileManager(configFolder + "/genealogy.json");
        genealogyData = genealogyFile.data;
    }

    // 加入族譜
    private void addMemberToGenealogy(Member inviter, String invitedUserID) {
        JSONArray invited;
        if (genealogyData.has(inviter.getId()))
            invited = genealogyData.getJSONArray(inviter.getId());
        else
            invited = new JSONArray();
        invited.put(invitedUserID);
        genealogyData.put(inviter.getId(), invited);
        genealogyFile.saveFile();
    }

    public void onCommand(SlashCommandEvent event) {
        Member member = event.getOption(USER_TAG).getAsMember();
        String userId = event.getUser().getId();
        // 還沒完成使用者設定
        if (!memberData.has(member.getId())) {
            event.replyEmbeds(createEmbed("此成員尚未完設暱稱設定！請成員私訊機器人代碼：`J`", 0xFF0000)).setEphemeral(true).queue();
            return;
        }

        event.replyEmbeds(createEmbed("確定要邀請 " + (member.getNickname() == null ? member.getUser().getAsTag() : member.getNickname()) + " ?", 0xbc153b))
                .addActionRow( // add component
                        Button.danger(userId + ":invite:" + member.getId(), "我會為他負責"))
                .setEphemeral(true).queue();
    }

    public void onButton(ButtonClickEvent event, String[] args) {
        if (args[1].equals("invite")) {
            // 這個user已經設定好伺服器暱稱
            if (memberData.has(args[2])) {
                // 加role
                for (Role role : confirmRoleID) {
                    guild.addRoleToMember(args[2], role).queue();
                }
                addMemberToGenealogy(event.getMember(), args[2]);

                Member targetMember = guild.retrieveMemberById(args[2]).complete();
                logChannel.sendMessage(
                        createEmbed(
                                "增產報國!", emoji.cute.getAsMention() + event.getMember().getAsMention() + "  生出了 " + targetMember.getAsMention(),
                                "成員誕生",
                                event.getMember().getNickname() == null ? event.getUser().getAsTag() : event.getMember().getNickname(), targetMember.getUser().getAvatarUrl(),
                                OffsetDateTime.now(), 0xFFD1DC
                        )
                ).queue();
                Collection<Permission> allow = new ArrayList<>();
                allow.add(Permission.VIEW_CHANNEL);

                for (int i = 0; i <= roomCategoryID.size(); i++) {
                    Category category = guild.getCategoryById(roomCategoryID.get(i));
                    if (category.getChannels().size() < 49) {
                        // 新增私人頻道
                        guild.createVoiceChannel(defaultRoomName.replace("%name%", memberData.getJSONObject(args[2]).getString(CHINESE_NICK)), category).queue(nvc -> {
                            // 創建專屬文字頻道
                            guild.createTextChannel(defaultRoomChatName.replace("%name%", memberData.getJSONObject(args[2]).getString(CHINESE_NICK)), category).setBitrate(roomBitrate)
                                    .queue(ntc -> {
                                        nvc.createPermissionOverride(targetMember).setAllow(allow).queue();
                                        ntc.createPermissionOverride(targetMember).setAllow(allow).queue();

                                        vcChannelOwner.put(nvc.getId(), args[2]);
                                        tcChannelOwner.put(ntc.getId(), args[2]);
                                        textChannel.add(ntc.getId());
                                        JSONObject vcInfo = new JSONObject();
                                        vcInfo.put(VOICE_CHANNEL_ID, nvc.getId());
                                        vcInfo.put(TEXT_CHANNEL_ID, ntc.getId());
                                        voiceChannelData.put(args[2], vcInfo);
                                        voiceChannelDataFile.saveFile();
                                    });
                        });
                        break;
                    }
                }
            }
        }
    }
}