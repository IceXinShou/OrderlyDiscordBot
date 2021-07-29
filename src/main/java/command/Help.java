package main.java.command;

import net.dv8tion.jda.api.Permission;
import net.dv8tion.jda.api.entities.Member;
import net.dv8tion.jda.api.entities.MessageEmbed;
import net.dv8tion.jda.api.events.interaction.SlashCommandEvent;
import net.dv8tion.jda.internal.requests.restaction.WebhookMessageUpdateActionImpl;

import java.time.OffsetDateTime;
import java.util.ArrayList;
import java.util.List;

import static main.java.util.Funtions.createEmbed;

public class Help {


    public List<MessageEmbed.Field> summonMemberFields(Member member, boolean showALL) {

        /**
         * Help Fields
         */

        List<MessageEmbed.Field> helpFields = new ArrayList<>();

        helpFields.add(new MessageEmbed.Field("",

                """
                        **__指令__ | __Commands__**

                        ┏━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━
                        ┃　**音樂 | Music**
                        ┃
                        ┃　　`/play (URL | NAME)` | 播放網址或查詢音樂
                        ┃　　`/pause` | 暫停播放
                        ┃　　`/skip` | 播放下一首音樂
                        ┃　　`/loop` | 切換循環模式
                        ┃　　`/repeat` | 切換單曲循環模式
                        ┃　　`/queue` | 顯示播放數據或列表
                        ┃　　`/playing` | 顯示播放數據或列表
                        ┃　　`/volume (COUNT)` | 更改音樂音量 (1-100)
                        ┗
                        """
                , false));


        helpFields.add(new MessageEmbed.Field("", "" +
                "┏━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━\n" +
                "┃　 ** 通用 | General **\n" +
                "┃\n" +
                "┃　　`/help` | 顯示幫助列表\n" +
                (showALL || (member.hasPermission(Permission.KICK_MEMBERS)) ? "┃　　`/ban<@>` | 封鎖成員\n┃　　`/unban<ID>` | 解除封鎖成員\n" : "") +
                (showALL || (member.hasPermission(Permission.KICK_MEMBERS)) ? "┃　　`/kick<@>` | 踢出成員\n" : "") +
                (showALL || (member.hasPermission(Permission.MESSAGE_MANAGE)) ? "┃　　`/poll<Q> (A) ...` | 發起投票\n┃　　`/clear<COUNT>` | 刪除訊息\n" : "") +
                "┃　　`/support` | 傳送問題回報\n" +
                "┃　　`/botinfo` | 顯示機器人訊息\n" +
                "┃　　`/ping` | 延遲測試\n" +
                "┗"
                , false));

        return helpFields;
    }

    public List<MessageEmbed.Field> summonAnnouncementFields(Member member, boolean showALL) {

        /**
         * Help Fields
         */

        List<MessageEmbed.Field> helpFields = new ArrayList<>();

        helpFields.add(new MessageEmbed.Field("",

                """
                        **__指令__ | __Commands__**

                        ┏━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━
                        ┃　**音樂 | Music**
                        ┃
                        ┃　　`/play (URL | NAME)` | 播放網址或查詢音樂
                        ┃　　`/pause` | 暫停播放
                        ┃　　`/skip` | 播放下一首音樂
                        ┃　　`/loop` | 切換循環模式
                        ┃　　`/repeat` | 切換單曲循環模式
                        ┃　　`/queue` | 顯示播放數據或列表
                        ┃　　`/playing` | 顯示播放數據或列表
                        ┃　　`/volume (COUNT)` | 更改音樂音量 (1-100)
                        ┗
                        """
                , false));


        helpFields.add(new MessageEmbed.Field("", "" +
                "┏━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━\n" +
                "┃　 ** 通用 | General **\n" +
                "┃\n" +
                "┃　　`/help` | 顯示幫助列表\n" +
                "┃　　`/support` | 傳送問題回報\n" +
                "┃　　`/botinfo` | 顯示機器人訊息\n" +
                "┃　　`/ping` | 延遲測試\n" +
                "┗"
                , false));

        return helpFields;
    }

    public void onMemberCommand(SlashCommandEvent event) {
        event.getHook().editOriginalEmbeds(createEmbed("使用說明：", "", "", "", "", summonMemberFields(event.getMember(), false), OffsetDateTime.now(), 0x00FFFF)).queue();
    }
    public void onAnnouncementCommand(SlashCommandEvent event) {
        if (event.getMember().hasPermission(Permission.ADMINISTRATOR)) {
            event.getChannel().sendMessageEmbeds(createEmbed("使用說明：", "", "", "", "", summonAnnouncementFields(event.getMember(), false), OffsetDateTime.now(), 0x00FFFF)).queue();
            event.getHook().editOriginalEmbeds(createEmbed("已傳送", 0x00FFFF)).queue();
        }
        else
            event.getHook().editOriginalEmbeds(createEmbed("你沒有權限 `(ADMINISTRATOR)`", 0xFF0000)).queue();
    }
}
