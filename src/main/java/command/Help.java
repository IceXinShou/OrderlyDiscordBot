package main.java.command;

import net.dv8tion.jda.api.Permission;
import net.dv8tion.jda.api.entities.Member;
import net.dv8tion.jda.api.entities.MessageEmbed;
import net.dv8tion.jda.api.events.interaction.SlashCommandEvent;

import java.time.OffsetDateTime;
import java.util.ArrayList;
import java.util.List;

import static main.java.util.Funtions.createEmbed;

public class Help {


    public List<MessageEmbed.Field> summonFields(Member member, boolean showALL) {

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
                        ┃　　`/previous` | 播放前一首音樂
                        ┃　　`/pause` | 暫停播放
                        ┃　　`/skip` | 播放下一首音樂
                        ┃　　`/loop` | 切換循環模式
                        ┃　　`/repeat` | 切換單曲循環模式
                        ┃　　`/queue` | 顯示播放數據或列表
                        ┃　　`/playing` | 顯示播放數據或列表
                        ┃　　`/volume (COUNT)` | 更改音樂音量 (1-100)
                        ┗"""
                , false));


        helpFields.add(new MessageEmbed.Field("", "" +
                "┏━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━\n" +
                "┃　 ** 通用 | General **\n" +
                "┃\n" +
                "┃　　`/help` | 顯示幫助列表\n" +
                (showALL || (member.hasPermission(Permission.KICK_MEMBERS)) ? "┃　　`/ban<@>` | 封鎖成員\n┃　　`/unban<ID>` | 解除封鎖成員\n" : "") +
                (showALL || (member.hasPermission(Permission.KICK_MEMBERS)) ? "┃　　`/kick<@>` | 踢出成員\n" : "") +
                (showALL || (member.hasPermission(Permission.MESSAGE_MANAGE)) ? "┃　　`/poll<Q> (A) ...` | 發起投票\n┃　　`/clear<COUNT>` | 刪除訊息\n" : "") +
                "┗"
                , false));

        return helpFields;
    }

    public void onCommand(SlashCommandEvent event) {
        event.getHook().editOriginalEmbeds(createEmbed("使用說明：", "", "", "", "", summonFields(event.getMember(), false), OffsetDateTime.now(), 0x00FFFF)).queue();
    }
}
