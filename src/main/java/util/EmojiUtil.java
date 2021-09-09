package main.java.util;

import net.dv8tion.jda.api.entities.Emote;
import net.dv8tion.jda.api.entities.Guild;

import java.util.List;

public class EmojiUtil {
    private final String TAG = "[Emoji]";

    /**
     * 註冊表情符號
     */

    public Emote
            leaveEmoji, joinEmoji,
            yesEmoji, noEmoji,
            rule, setting, ping, bell, lock, minecraftGrassBlock, nitro1, boost, discord, no1, partner,
            trashCan, leftArrow, rightArrow, join, leave, cute,
            borderTop, borderRightTop, borderRightBottom, borderRight, borderLeftTop, borderLeftBottom, borderLeft, borderBottom,
            report, update, bot, voiceFull, voiceDown, next, back, pingGood, pingBad, youtubeIcon,
            osu_ssh, osu_sh, osu_ss, osu_s, osu_a, osu_b, osu_c, osu_d, osu_f;

    public static Emote[] dotEmojis = new Emote[10];

    public void loadEmoji(Guild guild) {
        youtubeIcon = getEmoji("YoutubeIcon", guild);
        leaveEmoji = getEmoji("LeaveBlue", guild);
        joinEmoji = getEmoji("JoinBlue", guild);
        yesEmoji = getEmoji("Yes_Tick", guild);
        noEmoji = getEmoji("No_Tick", guild);
        dotEmojis[0] = getEmoji("DotPink", guild);
        dotEmojis[1] = getEmoji("DotBrown", guild);
        dotEmojis[2] = getEmoji("DotOrange", guild);
        dotEmojis[3] = getEmoji("DotGreen", guild);
        dotEmojis[4] = getEmoji("DotBlue", guild);
        dotEmojis[5] = getEmoji("DotRed", guild);
        dotEmojis[6] = getEmoji("DotYellow", guild);
        dotEmojis[7] = getEmoji("DotPurple", guild);
        dotEmojis[8] = getEmoji("DotGray", guild);
        dotEmojis[9] = getEmoji("DotBlack", guild);
        rule = getEmoji("RuleBlue", guild);
        setting = getEmoji("SettingBlue", guild);
        ping = getEmoji("PingBlue", guild);
        bell = getEmoji("BellBlue", guild);
        lock = getEmoji("LockBlue", guild);
        minecraftGrassBlock = getEmoji("MinecraftGrassBlock", guild);
        nitro1 = getEmoji("NitroPack2", guild);
        boost = getEmoji("NitroBoostAnimate", guild);
        discord = getEmoji("DiscordGif", guild);
        no1 = getEmoji("No1Cup", guild);
        partner = getEmoji("PartnerBlue", guild);
        trashCan = getEmoji("Trash_Blue", guild);
        rightArrow = getEmoji("RightArrow", guild);
        leftArrow = getEmoji("LeftArrow", guild);
        join = getEmoji("Join_Ani", guild);
        leave = getEmoji("Leave_Ani", guild);
        cute = getEmoji("AxolotlGif", guild);
        borderTop = getEmoji("BT", guild);
        borderRightTop = getEmoji("BRT", guild);
        borderRightBottom = getEmoji("BRB", guild);
        borderRight = getEmoji("BR", guild);
        borderLeftTop = getEmoji("BLT", guild);
        borderLeftBottom = getEmoji("BLB", guild);
        borderLeft = getEmoji("BL", guild);
        borderBottom = getEmoji("BB", guild);
        report = getEmoji("Report", guild);
        update = getEmoji("Update", guild);
        bot = getEmoji("Bot", guild);
        voiceFull = getEmoji("VoiceBlueFull", guild);
        voiceDown = getEmoji("VoiceBlueLow", guild);
        next = getEmoji("NextBlue", guild);
        back = getEmoji("BackBlue", guild);
        pingGood = getEmoji("PingGood", guild);
        pingBad = getEmoji("PingBad", guild);
        osu_ssh = getEmoji("Osu_SSH", guild);
        osu_sh = getEmoji("Osu_SH", guild);
        osu_ss = getEmoji("Osu_SS", guild);
        osu_s = getEmoji("Osu_S", guild);
        osu_a = getEmoji("Osu_A", guild);
        osu_b = getEmoji("Osu_B", guild);
        osu_c = getEmoji("Osu_C", guild);
        osu_d = getEmoji("Osu_D", guild);
        osu_f = getEmoji("Osu_F", guild);

        System.out.println(TAG + " Emoji loaded");
    }


    /**
     * 取得表情驗證
     */

    private Emote getEmoji(String name, Guild guild) {
        List<Emote> emote = guild.getEmotesByName(name, false);
        if (emote.size() == 0) {
            System.err.println(TAG + " cant get emoji: " + name);
            return null;
        }
        return emote.get(0);
    }
}
