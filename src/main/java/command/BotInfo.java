package main.java.command;

import net.dv8tion.jda.api.entities.MessageEmbed;
import net.dv8tion.jda.api.events.interaction.SlashCommandEvent;

import java.time.OffsetDateTime;
import java.util.ArrayList;
import java.util.List;

import static main.java.util.Funtions.createEmbed;

public class BotInfo {

    public void onCommand(SlashCommandEvent event) {
        List<MessageEmbed.Field> fields = new ArrayList<>();

        int members = 0;

        for (int i = 0; i < event.getJDA().getGuilds().size(); i++) {
            members = members + event.getJDA().getGuilds().get(i).getMemberCount();
        }

        fields.add(new MessageEmbed.Field("**伺服器總數: **", String.valueOf(event.getJDA().getGuilds().stream().count()), false));
        fields.add(new MessageEmbed.Field("**成員總數: **", String.valueOf(members), false));
        event.getHook().editOriginalEmbeds(createEmbed("機器人資訊", "", "", "", "", fields, OffsetDateTime.now(), 0x00FFFF)).queue();
    }

}