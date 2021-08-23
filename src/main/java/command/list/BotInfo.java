package main.java.command.list;

import net.dv8tion.jda.api.entities.MessageEmbed;
import net.dv8tion.jda.api.events.interaction.SlashCommandEvent;
import org.jetbrains.annotations.NotNull;

import java.time.OffsetDateTime;
import java.util.ArrayList;
import java.util.List;

import static main.java.lang.LangKey.*;
import static main.java.util.EmbedCreator.createEmbed;

public class BotInfo {

    public void onCommand(@NotNull SlashCommandEvent event) {
        List<MessageEmbed.Field> fields = new ArrayList<>();

        int members = 0;

        for (int i = 0; i < event.getJDA().getGuilds().size(); i++) {
            members = members + event.getJDA().getGuilds().get(i).getMemberCount();
        }

        fields.add(new MessageEmbed.Field(lang.get(BOTINFO_GUILD_TOTAL_COUNT), String.valueOf(event.getJDA().getGuilds().stream().count()), false));
        fields.add(new MessageEmbed.Field(lang.get(BOTINFO_MEMBER_TOTAL_COUNT), String.valueOf(members), false));
        event.getHook().editOriginalEmbeds(createEmbed(lang.get(BOTINFO_INFORMATION), "", "", "", "", fields, OffsetDateTime.now(), 0x00FFFF)).queue();
    }

}