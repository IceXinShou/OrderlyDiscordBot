package main.java.command;

import net.dv8tion.jda.api.events.interaction.SlashCommandEvent;

import java.time.OffsetDateTime;

import static main.java.SlashCommandOption.MESSAGE;
import static main.java.util.Funtions.createEmbed;
import static main.java.util.GuildUtil.guild;

public class Support {
    public void onCommand(SlashCommandEvent event) {
        event.getHook().editOriginalEmbeds(createEmbed("傳送中...", 0x00FFFF)).queue();
        try {
            guild.getTextChannelById("858672866283356217").sendMessageEmbeds(createEmbed(event.getMember().getUser().getAsTag(), event.getOption(MESSAGE).getAsString(), event.getGuild().getName(), OffsetDateTime.now(), 0x00FFFF)).queue();
            event.getHook().editOriginalEmbeds(createEmbed("傳送成功", 0x50ff70)).queue();
        } catch (Exception e) {
            System.out.println(e.getMessage());
            event.getHook().editOriginalEmbeds(createEmbed("傳送失敗", 0xFF0000)).queue();
        }
    }
}