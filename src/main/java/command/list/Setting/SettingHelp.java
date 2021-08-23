package main.java.command.list.Setting;

import net.dv8tion.jda.api.entities.MessageEmbed;
import net.dv8tion.jda.api.events.interaction.SlashCommandEvent;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.List;

import static main.java.lang.LangKey.SETTINGHELP_ADVANCE_HELP;
import static main.java.util.EmbedCreator.createEmbed;

public class SettingHelp {

    public void onCommand(@NotNull SlashCommandEvent event) {
        List<String> lang = Main.lang.getGuildLang(event.getGuild().getId());
        event.getHook().editOriginalEmbeds(createEmbed(lang.get(SETTINGHELP_ADVANCE_HELP), helpEmbed(), 0x00FFFF)).queue();
    }

    private @NotNull List<MessageEmbed.Field> helpEmbed() {
        List<MessageEmbed.Field> helpFields = new ArrayList<>();

        helpFields.add(new MessageEmbed.Field("",

                """
                        **指令 | Commands**

                        ┏━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━
                        ┃　**設定個人房間**　
                        ┃　　
                        ┃　　`/setting newroom`\040
                        ┃　　
                        ┃　　**介紹**：當成員加入所偵測的頻道時
                        ┃　　　　　將會被自動分配新創建的語音頻道
                        ┃　　　　　一切的設定皆可自行控制
                        ┃　　
                        ┃　　**參數 | Parameter**
                        ┃　　
                        ┃　　`<detectchannel>` 偵測頻道
                        ┃　　`<voicename>` 新語音頻道名稱 (佔位符)
                        ┃　　`[textname]` 文字頻道名稱 (佔位符)
                        ┃　　`[voicecategory]` 語音頻道目錄
                        ┃　　`[textcategory]` 文字頻道目錄
                        ┃　　`[voicebitrate]` 語音頻道位元率 (kbp)
                        ┃　　`[memberlimit]` 語音人數限制 (1~99)
                        ┃　　
                        ┃　　**佔位符 | Placeholder**
                        ┃　　
                        ┃　　`%guild_name%` 伺服器名稱 (xx生存伺服器)
                        ┃　　`%user%` 標註使用者名稱 (<@!810822763601461318>)
                        ┃　　`%user_name%` 使用者名稱 (XinShou)
                        ┃　　`%user_tag%` 使用者標籤 (0404)
                        ┃　　`%nickname%` 使用者暱稱 (新手 - IceXinShou)
                        """
                , false));

        helpFields.add(new MessageEmbed.Field("",

                """
                        ┃　**移除個人房間**　
                        ┃　　
                        ┃　　**方法 1.**
                        ┃　　將偵測頻道刪掉即可
                        ┃　　
                        ┃　　**方法 2.**
                        ┃　　`/setting removeroom`\040
                        ┃　　
                        ┃　　**參數 | Parameter**
                        ┃　　
                        ┃　　`<detectchannel>` 偵測頻道
                        ┗
                        """
                , false));

        helpFields.add(new MessageEmbed.Field("",

                """
                        ┏━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━
                        ┃　**設定自動化語音頻道**　
                        ┃　　
                        ┃　　`/setting newautovc`\040
                        ┃　　
                        ┃　　**介紹**：當目錄中的語音頻道皆都有成員在時
                        ┃　　　　　將會自動新增空的語音頻道 (只偵測設定的頻道名稱)
                        ┃　　　　　一切的設定皆可自行控制
                        ┃　　
                        ┃　　**參數 | Parameter**
                        ┃　　
                        ┃　　`<detectcategory>` 偵測目錄
                        ┃　　`<voicename>` 語音頻道名稱
                        """
                , false));

        helpFields.add(new MessageEmbed.Field("",

                """
                        ┃　**移除自動化語音頻道**　
                        ┃　　
                        ┃　　**方法 1.**
                        ┃　　將偵測目錄刪掉即可
                        ┃　　
                        ┃　　**方法 2.**
                        ┃　　`/setting removeautovc`\040
                        ┃　　
                        ┃　　**參數 | Parameter**
                        ┃　　
                        ┃　　`<detectcategory>` 偵測目錄
                        ┗
                        """
                , false));

        helpFields.add(new MessageEmbed.Field("",

                """
                        ┏━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━
                        ┃　**設定客服按鈕**　
                        ┃　　
                        ┃　　`/setting newticket`\040
                        ┃　　
                        ┃　　**介紹**：自定義按鈕訊息
                        ┃　　　　　自動新增客服頻道
                        ┃　　　　　一切的設定皆可自行控制
                        ┃　　
                        ┃　　**參數 | Parameter**
                        ┃　　
                        ┃　　`<messageid>` 主要內容訊息ID
                        ┃　　`<messagechannel>` 主要內容訊息頻道
                        ┃　　`[textname]` 新文字頻道名稱 (佔位符)
                        ┃　　`[textcategory]` 新文字頻道目錄
                        ┃　　`[enteredmessage]` 頻道創建時發送的訊息
                        ┃　　`[hasvoicechannel]` 是否有語音頻道
                        ┃　　`[voicename]` 新語音頻道名稱
                        ┃　　`[voicecategory]` 新語音頻道目錄
                        ┃　　`[buttonname]` 按鈕名稱
                        ┃　　`[buttoncolor]` 按鈕顏色 (red, orange, blue or gray)
                        ┃　　`[buttonemoji]` 按鈕圖示
                        ┃　　`[allowrole]` 除了觸發者以外, 允許檢視此頻道的身分組
                        ┃　　`[allowtagrole]` 觸發時是否 tag 此身分組
                        ┃　　
                        ┃　　**佔位符 | Placeholder**
                        ┃　　
                        ┃　　`%num%` 編號 (1, 2, 3...)
                        ┃　　`%role%` 允許檢視的身分組 (<@&858972360609366037>)
                        ┃　　`%role_name%` 允許檢視的身分組名稱 (>⠀序之徒⠀|⠀ServerBot)
                        ┃　　`%guild_name%` 伺服器名稱 (xx生存伺服器)
                        ┃　　`%user%` 標註使用者名稱 (<@!810822763601461318>)
                        ┃　　`%user_name%` 使用者名稱 (XinShou)
                        ┃　　`%user_tag%` 使用者標籤 (0404)
                        ┃　　`%nickname%` 使用者暱稱 (新手 - IceXinShou)
                        """
                , false));


        helpFields.add(new MessageEmbed.Field("",

                """
                        ┏━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━
                        ┃　**添加同訊息多個客服按鈕**　
                        ┃　　
                        ┃　　`/setting addticket`\040
                        ┃　　
                        ┃　　**介紹**：更多自定義按鈕
                        ┃　　　　　自動新增客服頻道
                        ┃　　　　　一切的設定皆可自行控制
                        ┃　　
                        ┃　　**參數 | Parameter**
                        ┃　　
                        ┃　　`<messageid>` 需添加的訊息ID
                        ┃　　`<messagechannel>` 需添加的訊息頻道
                        ┃　　`[textname]` 新文字頻道名稱 (佔位符)
                        ┃　　`[textcategory]` 新文字頻道目錄
                        ┃　　`[enteredmessage]` 頻道創建時發送的訊息
                        ┃　　`[hasvoicechannel]` 是否有語音頻道
                        ┃　　`[voicename]` 新語音頻道名稱
                        ┃　　`[voicecategory]` 新語音頻道目錄
                        ┃　　`[buttonname]` 按鈕名稱
                        ┃　　`[buttoncolor]` 按鈕顏色 (red, orange, blue or gray)
                        ┃　　`[buttonemoji]` 按鈕圖示
                        ┃　　`[allowrole]` 除了觸發者以外, 允許檢視此頻道的身分組
                        ┃　　`[allowtagrole]` 觸發時是否 tag 此身分組
                        ┃　　
                        ┃　　**佔位符 | Placeholder**
                        ┃　　
                        ┃　　`%num%` 編號 (1, 2, 3...)
                        ┃　　`%role%` 允許檢視的身分組 (<@&858972360609366037>)
                        ┃　　`%role_name%` 允許檢視的身分組名稱 (>⠀序之徒⠀|⠀ServerBot)
                        ┃　　`%guild_name%` 伺服器名稱 (xx生存伺服器)
                        ┃　　`%user%` 標註使用者名稱 (<@!810822763601461318>)
                        ┃　　`%user_name%` 使用者名稱 (XinShou)
                        ┃　　`%user_tag%` 使用者標籤 (0404)
                        ┃　　`%nickname%` 使用者暱稱 (新手 - IceXinShou)
                        """
                , false));


        helpFields.add(new MessageEmbed.Field("",

                """
                        ┃　**移除客服按鈕**　
                        ┃　　
                        ┃　　**方法 1.**
                        ┃　　將含有客服按鈕的訊息刪掉即可
                        ┃　　
                        ┃　　**方法 2.**
                        ┃　　`/setting removeticket`\040
                        ┃　　
                        ┃　　**參數 | Parameter**
                        ┃　　
                        ┃　　`<detectcategory>` 偵測目錄
                        ┗
                        """
                , false));


        helpFields.add(new MessageEmbed.Field("",

                """
                        ┏━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━
                        ┃　**設定頻道顯示數據**　
                        ┃　　
                        ┃　　`/setting newchannelstatus`\040
                        ┃　　
                        ┃　　**介紹**：可用參數眾多
                        ┃　　　　　也可進行計算處理
                        ┃　　　　　一切的設定皆可自行控制
                        ┃　　
                        ┃　　**參數 | Parameter**
                        ┃　　
                        ┃　　`<channel>` 顯示數據頻道
                        ┃　　`<channelname>` 數據顯示文字 (佔位符)
                        ┃　　
                        ┃　　**佔位符 | Placeholder**
                        ┃　　
                        ┃　　`%member%` 成員人數
                        ┃　　`%member_bot%` 成員與機器人數
                        ┃　　`%online_member%` 線上成員人數
                        ┃　　`%online_member_bot%` 線上成員與機器人數
                        ┃　　`%offline_member%` 離線成員人數
                        ┃　　`%offline_member_bot%` 離線成員與機器人數
                        ┃　　`%afk_member%` 閒置成員人數
                        ┃　　`%afk_member_bot%` 閒置成員與機器人數
                        ┃　　`%working_member%` 勿擾成員人數
                        ┃　　`%working_member_bot%` 勿擾成員與機器人數
                        ┃　　`%in_voicechannel%` 通話成員人數
                        ┃　　`%in_voicechannel_bot%` 通話成員與機器人數
                        ┃　　`%not_in_voicechannel%` 通話成員人數
                        ┃　　`%not_in_voicechannel_bot%` 未在通話成員與機器人數
                        ┃　　`%stream%` 直播成員人數
                        ┃　　`%camera%` 視訊成員人數
                        ┃　　`%play_minecraft%` 正在遊玩 minecraft 人數
                        ┃　　(支援: Vanilla, Badlion Client, Lunar Client, LabyMod, Impact, Aristois)
                        ┃　　(若有未支援到的請使用: `/support` 提供建議)
                        """
                , false));

        helpFields.add(new MessageEmbed.Field("",

                """
                        ┃　　**運算規則 | Math**
                        ┃　　
                        ┃　　範例
                        ┃　　　　1.\040
                        ┃　　　　線上人數: `${%online_member% + %afk_member% + %working_member%}`
                        ┃　　　　或
                        ┃　　　　線上人數: `${%member% - %offline_member%}`
                        ┃　　　　
                        ┃　　　　2.\040
                        ┃　　　　語音人數比: `${%in_voicechannel%/%online_member%*100}`
                        ┃
                        ┃　　　　3. 　　
                        ┃　　　　直播除以視訊的餘數: `${%stream%%%camera%}`
                        ┃　　
                        ┃　　
                        ┃　**移除頻道顯示數據**　
                        ┃　　
                        ┃　　**方法 1.**
                        ┃　　將顯示數據頻道刪掉即可
                        ┃　　
                        ┃　　**方法 2.**
                        ┃　　`/setting removechannelstatus`\040
                        ┃　　
                        ┃　　**參數 | Parameter**
                        ┃　　
                        ┃　　`<channel>` 偵測目錄
                        ┗
                        """
                , false));

        return helpFields;
    }

}
