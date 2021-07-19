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
            dot1, dot2, dot3, dot4, dot5, dot6, dot7, dot8, dot9, dot10,
            rule, setting, ping, bell, lock, minecraftGrassBlock, nitro1, boost, discord, no1, partner,
            trashCan, leftArrow, rightArrow, join, leave, cute,
            borderTop, borderRightTop, borderRightBottom, borderRight, borderLeftTop, borderLeftBottom, borderLeft, borderBottom,
            report, update, bot, voiceFull, voiceDown, next, back, pingGood, pingBad;

    public void loadEmoji(Guild guild) {
        leaveEmoji = getEmoji("LeaveBlue", guild);
        joinEmoji = getEmoji("JoinBlue", guild);
        yesEmoji = getEmoji("Yes_Tick", guild);
        noEmoji = getEmoji("No_Tick", guild);
        dot1 = getEmoji("DotPink", guild);
        dot2 = getEmoji("DotBrown", guild);
        dot3 = getEmoji("DotOrange", guild);
        dot4 = getEmoji("DotGreen", guild);
        dot5 = getEmoji("DotBlue", guild);
        dot6 = getEmoji("DotRed", guild);
        dot7 = getEmoji("DotYellow", guild);
        dot8 = getEmoji("DotPurple", guild);
        dot9 = getEmoji("DotGray", guild);
        dot10 = getEmoji("DotBlack", guild);
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
