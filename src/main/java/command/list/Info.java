package main.java.command.list;

import net.dv8tion.jda.api.entities.MessageEmbed;
import net.dv8tion.jda.api.events.interaction.SlashCommandEvent;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.List;

public class Info {

    @SuppressWarnings("ALL")
    public void onCommand(@NotNull SlashCommandEvent event) {

        List<MessageEmbed.Field> fields = new ArrayList<>();
        fields.add(new MessageEmbed.Field("**伺服器總數: **", String.valueOf((long) event.getJDA().getGuilds().size()), false));

//        event.getHook().editOriginalEmbeds(createEmbed());

    }
}
