package main.command.list;

import main.Main;
import net.dv8tion.jda.api.entities.TextChannel;
import net.dv8tion.jda.api.events.interaction.SlashCommandEvent;

import java.time.OffsetDateTime;
import java.util.List;

import static main.lang.LangKey.*;
import static main.util.EmbedCreator.createEmbed;
import static main.util.GuildUtil.guild;
import static main.util.SlashCommandOption.MESSAGE;

public class Support {
    public void onCommand(SlashCommandEvent event) {
        List<String> lang = Main.language.getGuildLang(event.getGuild().getId());
        event.getHook().editOriginalEmbeds(createEmbed(lang.get(SUPPORT_SENDING), 0x00FFFF)).queue();
        TextChannel channel;
        if ((channel = guild.getTextChannelById("858672866283356217")) == null) {
            event.getHook().editOriginalEmbeds(createEmbed(lang.get(SUPPORT_FAIL), 0xFF0000)).queue();
            return;
        }
        channel.sendMessageEmbeds(
                        createEmbed(event.getUser().getAsTag(),
                                event.getOption(MESSAGE).getAsString(),
                                event.getGuild() == null ? "[Private]" : event.getGuild().getName(), OffsetDateTime.now(), 0x00FFFF))
                .queue();
        event.getHook().editOriginalEmbeds(createEmbed(lang.get(SUPPORT_SUCCESS), 0x50ff70)).queue();
    }
}