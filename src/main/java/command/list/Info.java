package main.java.command.list;

import main.java.Main;
import net.dv8tion.jda.api.entities.MessageEmbed;
import net.dv8tion.jda.api.events.interaction.SlashCommandEvent;

import java.util.ArrayList;
import java.util.List;

import static main.java.lang.LangKey.INFO_SERVER_COUNT;
import static main.java.util.EmbedCreator.createEmbed;

public class Info {

    @SuppressWarnings("ALL")
    public void onCommand(SlashCommandEvent event) {
        List<String> lang = Main.language.getGuildLang(event.getGuild().getId());
        List<MessageEmbed.Field> fields = new ArrayList<>();
        fields.add(new MessageEmbed.Field(lang.get(INFO_SERVER_COUNT), String.valueOf((long) event.getJDA().getGuilds().size()), false));

//        event.getHook().editOriginalEmbeds(createEmbed());

    }
}
