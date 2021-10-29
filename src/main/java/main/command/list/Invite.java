package main.command.list;

import main.util.file.JsonFileManager;
import net.dv8tion.jda.api.entities.Member;
import net.dv8tion.jda.api.entities.Role;
import net.dv8tion.jda.api.entities.TextChannel;
import net.dv8tion.jda.api.events.interaction.ButtonClickEvent;
import net.dv8tion.jda.api.events.interaction.SlashCommandEvent;
import net.dv8tion.jda.api.interactions.components.Button;
import org.json.JSONArray;
import org.json.JSONObject;

import java.time.OffsetDateTime;

import static main.BotSetting.*;
import static main.Main.emoji;
import static main.event.Log.logChannel;
import static main.event.OwnJoin.memberData;
import static main.util.EmbedCreator.createEmbed;
import static main.util.GuildUtil.guild;
import static main.util.SlashCommandOption.USER_TAG;
import static main.util.Tag.getMemberName;

public class Invite {
    public static String authChannelID;
    public static TextChannel authChannel;
    public static JSONObject genealogyData;
    private final JsonFileManager genealogyFile;

    /**
     * new class 的時候執行創建變數與取得資料
     */

    public Invite() {
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
            event.getHook().editOriginalEmbeds(createEmbed("此成員尚未完設暱稱設定！請成員私訊機器人代碼：`J`", 0xFF0000)).queue();
            return;
        } else if (guild.retrieveMemberById(member.getId()).complete().getRoles().contains(memberRole)) {
            event.getHook().editOriginalEmbeds(createEmbed("此成員已被邀請過！", 0xFF0000)).setActionRows().queue();
            return;
        } else if (member.getUser().isBot()) {
            event.getHook().editOriginalEmbeds(createEmbed("無法邀請機器人！", 0xFF0000)).setActionRows().queue();
            return;
        }

        event.getHook().editOriginalEmbeds(createEmbed(String.format("確定要邀請 %s ?", getMemberName(event)), 0xbc153b))
                .setActionRow(Button.danger("Invite:invite:" + userId + ':' + member.getId(), "我會為他負責"))
                .queue();
    }

    public void onButton(ButtonClickEvent event, String[] args) {
        if (!args[0].equals("Invite"))
            return;

        if (args[1].equals("invite")) {
            Member targetMember = guild.retrieveMemberById(args[3]).complete();

            // 加role
            for (Role role : confirmRoleID)
                guild.addRoleToMember(targetMember, role).queue();

            addMemberToGenealogy(event.getMember(), args[3]);

            logChannel.sendMessageEmbeds(
                    createEmbed(
                            "增產報國!",
                            String.format("%s %s 生出了 %s", emoji.cute.getAsMention(), event.getMember().getAsMention(), targetMember.getAsMention()),
                            "成員誕生",
                            getMemberName(event), targetMember.getUser().getAvatarUrl(),
                            OffsetDateTime.now(), 0xFFD1DC
                    )
            ).queue();
            event.deferEdit().setEmbeds(createEmbed("添加成功", 0x9740b9)).setActionRows().queue();
        }
    }
}