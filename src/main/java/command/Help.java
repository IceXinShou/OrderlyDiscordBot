package main.java.command;

import net.dv8tion.jda.api.events.interaction.SlashCommandEvent;

import java.time.OffsetDateTime;

import static main.java.BotSetting.helpFields;
import static main.java.util.Funtions.createEmbed;

public class Help {

    public void onCommand(SlashCommandEvent event) {
        event.getHook().editOriginalEmbeds(createEmbed("使用說明：", "", "", "", "", helpFields, OffsetDateTime.now(), 0x00FFFF)).queue();
    }
}
