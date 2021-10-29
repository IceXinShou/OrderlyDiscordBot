package main.command.list;

import main.Main;
import net.dv8tion.jda.api.Permission;
import net.dv8tion.jda.api.entities.Member;
import net.dv8tion.jda.api.entities.MessageEmbed;
import net.dv8tion.jda.api.events.interaction.SlashCommandEvent;

import java.time.OffsetDateTime;
import java.util.ArrayList;
import java.util.List;

import static main.lang.LangKey.HELP_USAGE;
import static main.lang.LangKey.LANG_CREATE_SUCCESS;
import static main.util.EmbedCreator.createEmbed;
import static main.util.PermissionERROR.hasPermission;

public class Help {

    public void onSelfAnnouncementCommand(SlashCommandEvent event) {
        List<String> lang = Main.language.getGuildLang(event.getGuild().getId());
        if (!hasPermission(Permission.ADMINISTRATOR, event, true))
            return;
        event.getChannel().sendMessageEmbeds(createEmbed("Orderly " + lang.get(HELP_USAGE), "", "", "", "",
                summonSelfAnnouncementFields(), OffsetDateTime.now(), 0x00FFFF)).queue();
        event.getHook().editOriginalEmbeds(createEmbed(lang.get(LANG_CREATE_SUCCESS), 0x0FFFF)).queue();

    }

    public void onSelfMemberCommand(SlashCommandEvent event) {
        List<String> lang = Main.language.getGuildLang(event.getGuild().getId());
        event.getHook().editOriginalEmbeds(createEmbed("Orderly " + lang.get(HELP_USAGE), "", "", "", "",
                summonSelfMemberFields(event.getMember(), false), OffsetDateTime.now(), 0x00FFFF)).queue();
    }

    public void onNekoBotAnnouncementCommand(SlashCommandEvent event) {
        List<String> lang = Main.language.getGuildLang(event.getGuild().getId());
        if (!hasPermission(Permission.ADMINISTRATOR, event, true))
            return;
        event.getChannel().sendMessageEmbeds(createEmbed("Neko Bot " + lang.get(HELP_USAGE), "", "", "", "",
                summonNekoBotAnnouncementFields(), OffsetDateTime.now(), 0x00FFFF)).queue();
        event.getHook().editOriginalEmbeds(createEmbed(lang.get(LANG_CREATE_SUCCESS), 0x0FFFF)).queue();

    }

    public void onNekoBotMemberCommand(SlashCommandEvent event) {
        List<String> lang = Main.language.getGuildLang(event.getGuild().getId());
        event.getHook().editOriginalEmbeds(createEmbed("Neko Bot " + lang.get(HELP_USAGE), "", "", "", "",
                summonNekoBotMemberFields(event.getMember(), false), OffsetDateTime.now(), 0x00FFFF)).queue();
    }

    public List<MessageEmbed.Field> summonSelfMemberFields(Member member, boolean showALL) {

        /**
         * Help Fields
         */

        List<MessageEmbed.Field> helpFields = new ArrayList<>();

        helpFields.add(new MessageEmbed.Field("",

                """
                        **__指令__ | __Commands__**

                        ┏━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━
                        ┃　**音樂 | Music**
                        ┃
                        ┃　　`/p <URL | NAME>` | 播放網址或查詢音樂
                        ┃　　`/s` | 播放下一首音樂
                        ┃　　`/q` | 顯示播放數據或列表
                        ┃　　`/pause` | 暫停播放
                        ┃　　`/loop` | 切換循環模式
                        ┃　　`/repeat` | 切換單曲循環模式
                        ┃　　`/volume [COUNT]` | 更改音樂音量 (1-100)
                        ┗
                        """
                , false));


        helpFields.add(new MessageEmbed.Field("", "" +
                "┏━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━\n" +
                "┃　 ** 通用 | General **\n" +
                "┃\n" +
                "┃　　`/help` | 顯示通用幫助列表\n" +
                "┃　　`/info` | 顯示伺服器資訊\n" +
                (showALL || (member.hasPermission(Permission.KICK_MEMBERS)) ? "┃　　`/helpannouncement` | 將幫助列表以公告呈現\n" : "") +
                (showALL || (member.hasPermission(Permission.KICK_MEMBERS)) ? "┃　　`/ban <@>` | 封鎖成員\n┃　　`/unban <ID>` | 解除封鎖成員\n" : "") +
                (showALL || (member.hasPermission(Permission.BAN_MEMBERS)) ? "┃　　`/kick <@>` | 踢出成員\n" : "") +
                (showALL || (member.hasPermission(Permission.MESSAGE_MANAGE)) ? "┃　　`/poll <Q> [A] ...` | 發起投票\n┃　　`/clear <COUNT>` | 刪除訊息\n" : "") +
                "┃　　`/support` | 傳送問題回報\n" +
                "┃　　`/botinfo` | 顯示機器人訊息\n" +
                "┃　　`/ping` | 延遲測試\n" +
                "┃　　`/surl` | 縮短網址\n" +
                "┃　　`/popcat` | 查看 PopCat 排行榜\n" +
                "┃　　`/mp4togif` | 將影片連結轉換為 Gif\n" +
                "┗"
                , false));

        helpFields.add(new MessageEmbed.Field("",
                """
                        ┏━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━
                        ┃　 ** 進階 | Advance **
                        ┃
                        ┃　　`/setting help` | 顯示進階幫助列表
                        ┗
                        """
                , false));

        return helpFields;
    }

    private List<MessageEmbed.Field> summonSelfAnnouncementFields() {

        /**
         * Help Fields
         */

        List<MessageEmbed.Field> helpFields = new ArrayList<>();

        helpFields.add(new MessageEmbed.Field("",
                """
                        **__指令__ | __Commands__**

                        ┏━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━
                        ┃　**音樂 | Music**
                        ┃
                        ┃　　`/p <URL | NAME>` | 播放網址或查詢音樂
                        ┃　　`/s` | 播放下一首音樂
                        ┃　　`/q` | 顯示播放數據或列表
                        ┃　　`/pause` | 暫停播放
                        ┃　　`/loop` | 切換循環模式
                        ┃　　`/repeat` | 切換單曲循環模式
                        ┃　　`/volume [COUNT]` | 更改音樂音量 (1-100)
                        ┗
                        """
                , false));

        helpFields.add(new MessageEmbed.Field("",
                """
                        ┏━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━
                        ┃　 ** OSU **
                        ┃
                        ┃　　`/osu setuser` | 綁定帳號
                        ┃　　`/osu search` | 搜尋玩家資料
                        ┃　　`/osu top` | 查看玩家最佳成績
                        ┃　　`/osu last` | 查看上一首歌的成績
                        ┗
                        """
                , false));

        helpFields.add(new MessageEmbed.Field("",
                """
                        ┏━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━
                        ┃　 ** 通用 | General **
                        ┃
                        ┃　　`/help` | 顯示幫助列表
                        ┃　　`/support` | 傳送問題回報
                        ┃　　`/botinfo` | 顯示機器人訊息
                        ┃　　`/ping` | 延遲測試
                        ┗
                        """
                , false));

        return helpFields;
    }

    public List<MessageEmbed.Field> summonNekoBotMemberFields(Member member, boolean showALL) {

        /**
         * Help Fields
         */

        List<MessageEmbed.Field> helpFields = new ArrayList<>();

        helpFields.add(new MessageEmbed.Field("",

                """
                        **__指令__ | __Commands__**

                        預設前綴：`n!` 或 <@!310039170792030211>

                        ┏━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━
                        ┃　**贊助者 | Donator**
                        ┃
                        ┃　　`apikey` | 取得 API KEY
                        ┃　　`autolooder` | 切換自動載入
                        ┃　　`autoloodsetting` | 自動載入設定
                        ┃　　`donate` | 贊助機器人
                        ┃　　`twitter` | twitter 提要
                        ┗
                        """
                , false));


        helpFields.add(new MessageEmbed.Field("", """
                ┏━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━
                ┃　 ** 經濟 | Economy **
                ┃
                ┃　　`balance` | 查看口袋摳摳
                ┃　　`coinflip` | 賭博
                ┃　　`daily` | 每日簽到
                ┃　　`profile` | 顯示個人資料
                ┃　　`register` | 申請並註冊帳戶
                ┃　　`rep` | 投票聲望
                ┃　　`roulette` | 輪盤 <金額> <投註名稱/號碼> <投注選項>
                ┃　　`setdesc` | 設定個人資料敘述
                ┃　　`top` | 顯示金錢排行榜
                ┃　　`transfer` | 支付金錢給其他成員
                ┗
                """
                , false));


        helpFields.add(new MessageEmbed.Field("", """
                ┏━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━
                ┃　 ** NSFW | 高鐵 **
                ┃
                ┃　　`4k` | 4K 母湯
                ┃　　`anal` | 菊花
                ┃　　`ass` | 挺翹的桃子
                ┃　　`boobs` | 棉花糖
                ┃　　`bigboobs` | 大棉花糖
                ┃　　`blowjob` | 好吃
                ┃　　`collared` | 佔有
                ┃　　`cosplay` | 角色扮演
                ┃　　`creampie` | 中間出來
                ┃　　`cumsluts` | 性慾發洩
                ┃　　`feet` | 腳腳
                ┃　　`futa` | 雙性
                ┃　　`gonewild` | 狂野
                ┃　　`hass` | 動漫挺翹的桃子
                ┃　　`hboobs` | 動漫棉花糖
                ┃　　`hentai` | 動漫
                ┃　　`hmidriff` | 動漫比基尼
                ┃　　`hthighs` | 動漫大腿 
                ┃　　`hyuri` | 百合
                ┃　　`lewdkitsune` | 動漫淫蕩
                ┃　　`paizuri` | 乳交
                ┃　　`pantsu` | 胖次
                ┃　　`pgif` | 真人動畫 
                ┃　　`pussy` | 海鮮
                ┃　　`swimsuit` | 泳裝
                ┃　　`tentacle` | 觸手
                ┃　　`thighs` | 真人大腿
                ┃　　`yaoi` | 男同
                ┃　　`girl` | 幼女 (無資料來源)
                ┃　　`source` | 尋找圖片來源
                ┃　　`yandere` | 搜圖平台
                ┃　　`rule34` | 搜圖平台
                ┃　　`nsfw` | 切換限制級
                ┃　　`jav` | 根據 JAV 代碼獲取 JAV 數據
                ┗
                """
                , false));

        return helpFields;
    }

    private List<MessageEmbed.Field> summonNekoBotAnnouncementFields() {

        /**
         * Help Fields
         */

        List<MessageEmbed.Field> helpFields = new ArrayList<>();

        helpFields.add(new MessageEmbed.Field("",

                """
                        **__指令__ | __Commands__**

                        預設前綴：`n!` 或 <@!310039170792030211>

                        ┏━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━
                        ┃　**贊助者 | Donator**
                        ┃
                        ┃　　`apikey` | 取得 API KEY
                        ┃　　`autolooder` | 切換自動載入
                        ┃　　`autoloodsetting` | 自動載入設定
                        ┃　　`donate` | 贊助機器人
                        ┃　　`twitter` | twitter 提要
                        ┗
                        """
                , false));


        helpFields.add(new MessageEmbed.Field("", """
                ┏━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━
                ┃　 ** 經濟 | Economy **
                ┃
                ┃　　`balance` | 查看口袋摳摳
                ┃　　`coinflip` | 賭博
                ┃　　`daily` | 每日簽到
                ┃　　`profile` | 顯示個人資料
                ┃　　`register` | 申請並註冊帳戶
                ┃　　`rep` | 投票聲望
                ┃　　`roulette` | 輪盤 <金額> <投註名稱/號碼> <投注選項>
                ┃　　`setdesc` | 設定個人資料敘述
                ┃　　`top` | 顯示金錢排行榜
                ┃　　`transfer` | 支付金錢給其他成員
                ┗
                """
                , false));


        helpFields.add(new MessageEmbed.Field("", """
                ┏━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━
                ┃　 ** NSFW | 高鐵 **
                ┃
                ┃　　`4k` | 4K 母湯
                ┃　　`anal` | 菊花
                ┃　　`ass` | 挺翹的桃子
                ┃　　`boobs` | 棉花糖
                ┃　　`bigboobs` | 大棉花糖
                ┃　　`blowjob` | 好吃
                ┃　　`collared` | 佔有
                ┃　　`cosplay` | 角色扮演
                ┃　　`creampie` | 中間出來
                ┃　　`cumsluts` | 性慾發洩
                ┃　　`feet` | 腳腳
                ┃　　`futa` | 雙性
                ┃　　`gonewild` | 狂野
                ┃　　`hass` | 動漫挺翹的桃子
                ┃　　`hboobs` | 動漫棉花糖
                ┃　　`hentai` | 動漫
                ┃　　`hmidriff` | 動漫比基尼
                ┃　　`hthighs` | 動漫大腿 
                ┃　　`hyuri` | 百合
                ┃　　`lewdkitsune` | 動漫淫蕩
                ┃　　`paizuri` | 乳交
                ┃　　`pantsu` | 胖次
                ┃　　`pgif` | 真人動畫 
                ┃　　`pussy` | 海鮮
                ┃　　`swimsuit` | 泳裝
                ┃　　`tentacle` | 觸手
                ┃　　`thighs` | 真人大腿
                ┃　　`yaoi` | 男同
                ┃　　`girl` | 幼女 (無資料來源)
                ┃　　`source` | 尋找圖片來源
                ┃　　`yandere` | 搜圖平台
                ┃　　`rule34` | 搜圖平台
                ┃　　`nsfw` | 切換限制級
                ┃　　`jav` | 根據 JAV 代碼獲取 JAV 數據
                ┗
                        """
                , false));

        return helpFields;
    }

}
