package main.java.event;

import main.java.Main;
import net.dv8tion.jda.api.entities.TextChannel;
import net.dv8tion.jda.api.events.message.guild.GuildMessageReceivedEvent;
import org.jetbrains.annotations.NotNull;

import static main.java.util.EmbedCreator.createEmbed;
import static main.java.util.GuildUtil.guildID;


public class GeneralReplay {


    /**
     * 訊息接收事件反饋
     */

    public void onGuildMessageReceived(@NotNull GuildMessageReceivedEvent event) {
        if (event.getAuthor().getId().equals(Main.botID)) return;
        if (!event.getGuild().getId().equals(guildID)) return;
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
                event.getMessage().replyEmbeds(createEmbed("已刪除汙辱訊息 (" + event.getAuthor().getAsMention() + ")", 0xFF0000)).queue();
                event.getMessage().delete().queue();
                System.out.println("[" + event.getGuild().getName() + "] " + event.getAuthor().getAsTag() + " swore: " + event.getMessage().getContentRaw());
                break;
        }

        if (event.getMessage().getContentRaw().startsWith("喵")) {
            channel.sendMessage("抓抓~").queue(i -> Log.deleteNoLog(i, 1));
        }
    }
}
