package main.java.event;

import main.java.Main;
import net.dv8tion.jda.api.entities.TextChannel;
import net.dv8tion.jda.api.events.message.guild.GuildMessageReceivedEvent;
import net.dv8tion.jda.api.hooks.ListenerAdapter;
import org.jetbrains.annotations.NotNull;


public class GeneralReplay extends ListenerAdapter {


    /**
     * 訊息接收事件反饋
     */

    @Override
    public void onGuildMessageReceived(@NotNull GuildMessageReceivedEvent event) {
        if (event.getAuthor().getId().equals(Main.botID)) return;
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
            case "Hi":
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
        }

        if (event.getMessage().getContentRaw().startsWith("喵")) {
            channel.sendMessage("抓抓~").queue(i -> Log.deleteNoLog(i, 1));
        }
    }
}
