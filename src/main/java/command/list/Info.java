package main.java.command.list;

import net.dv8tion.jda.api.entities.MessageEmbed;
import net.dv8tion.jda.api.events.interaction.SlashCommandEvent;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.List;

import static main.java.util.EmbedCreator.createEmbed;

public class Info {

    public void onCommand(@NotNull SlashCommandEvent event) {

        List<MessageEmbed.Field> fields = new ArrayList<>();
        fields.add(new MessageEmbed.Field("**伺服器總數: **", String.valueOf(event.getJDA().getGuilds().stream().count()), false));

//        event.getHook().editOriginalEmbeds(createEmbed());

    }
}
