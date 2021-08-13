package main.java.command.list;

import net.dv8tion.jda.api.events.interaction.SlashCommandEvent;
import org.jetbrains.annotations.NotNull;

import java.time.OffsetDateTime;

import static main.java.util.EmbedCreator.createEmbed;
import static main.java.util.GuildUtil.guild;
import static main.java.util.SlashCommandOption.MESSAGE;

public class Support {
    public void onCommand(@NotNull SlashCommandEvent event) {
        event.getHook().editOriginalEmbeds(createEmbed("傳送中...", 0x00FFFF)).queue();
        try {
            guild.getTextChannelById("858672866283356217").sendMessageEmbeds(
                            createEmbed(event.getUser().getAsTag(),
                                    event.getOption(MESSAGE).getAsString(),
                                    event.getGuild() == null ? "[Private]" : event.getGuild().getName(), OffsetDateTime.now(), 0x00FFFF))
                    .queue();
            event.getHook().editOriginalEmbeds(createEmbed("傳送成功", 0x50ff70)).queue();
        } catch (Exception e) {
            System.out.println(e.getMessage());
            event.getHook().editOriginalEmbeds(createEmbed("傳送失敗", 0xFF0000)).queue();
        }
    }
}