package main.java.event;

import main.java.Main;
import net.dv8tion.jda.api.entities.TextChannel;
import net.dv8tion.jda.api.events.message.guild.GuildMessageReceivedEvent;

import java.text.MessageFormat;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.TimeUnit;

import static main.java.BotSetting.memberRole;
import static main.java.util.EmbedCreator.createEmbed;
import static main.java.util.GuildUtil.guildID;


public class GeneralReplay {


    Map<String, Integer> warnCount = new HashMap<>();

    /**
     * 訊息接收事件反饋
     */

    public void warnMember(String id) {
        if (warnCount.containsKey(id))
            warnCount.put(id, warnCount.get(id) + 1);
        else
            warnCount.put(id, 0);
    }

    public void onGuildMessageReceived(GuildMessageReceivedEvent event) {
        if (event.getAuthor().getId().equals(Main.botID)) return;
//        if (event.getGuild().getId().equals("882605953382514718")) {
//            if (event.getMessage().getContentRaw().contains("qaq") || event.getMessage().getContentRaw().contains("QAQ") || event.getMessage().getContentRaw().contains("QQ") || event.getMessage().getContentRaw().contains("qq"))
//                if (System.currentTimeMillis() % 2 == 0)
//                    event.getMessage().reply("哭屁!").queue();
//                else
//                    event.getMessage().reply("賣靠邀!").queue();
//            if (event.getMessage().getContentRaw().contains("笑死"))
//                if (System.currentTimeMillis() % 2 == 0)
//                    event.getMessage().reply("笑屁!").queue();
//                else
//                    event.getMessage().reply("賣靠邀!").queue();
//        }
        if (!event.getGuild().getId().equals(guildID)) return;

        if (event.getChannel().getId().equals("860469251176792065") && !event.getAuthor().isSystem()) {
            event.getMessage().replyEmbeds(createEmbed("請勿在此處發送訊息，請使用 `/invite` 指令", 0xFF0000))
                    .queue(i -> i.delete().queueAfter(3000, TimeUnit.MILLISECONDS));
            event.getMessage().delete().queue();

            warnMember(event.getMember().getId());
            if (warnCount.get(event.getMember().getId()) > 3)
                event.getGuild().removeRoleFromMember(event.getMember(), memberRole);

            return;
        }


        TextChannel channel = event.getChannel();
        if (event.getMessage().getContentRaw().startsWith("你好乖")) {
            channel.sendMessage("謝謝讚美").queue(i -> Log.deleteNoLog(i, 1));
        }
        switch (event.getMessage().getContentRaw()) {
            case "喵嗚~":
                if (event.getAuthor().getId().equals("286467027705790464")) {
                    channel.sendMessage("(蹭蹭喵~").queue();
                    return;
                }
                break;
            case "hi":
            case "hi~":
            case "Hi":
            case "Hi~":
                channel.sendMessage("Hello~").queue(i -> Log.deleteNoLog(i, 1));
                break;
            case "要玩嗎":
                channel.sendMessage("我考慮w").queue(i -> Log.deleteNoLog(i, 1));
                break;
            case "欸":
                channel.sendMessage("穿山甲欸").queue(i -> Log.deleteNoLog(i, 1));
                break;
            case "好無聊":
                channel.sendMessage("起來嗨!").queue(i -> Log.deleteNoLog(i, 1));
                break;
            case "fk":
            case "fuck":
            case "fuck you":
            case "fuck u":
            case "fk u":
            case "幹":
            case "幹你":
            case "幹妳":
            case "幹你娘":
            case "幹妳娘":
            case "肏":
            case "操":
            case "操你媽":
            case "操妳媽":
                event.getMessage().replyEmbeds(createEmbed(MessageFormat.format("已刪除汙辱訊息 (%s)", event.getAuthor().getAsMention()), 0xFF0000)).queue();
                event.getMessage().delete().queue();
                break;
        }

        if (event.getMessage().getContentRaw().startsWith("喵")) {
            channel.sendMessage("抓抓~").queue(i -> Log.deleteNoLog(i, 1));
        }
    }
}
