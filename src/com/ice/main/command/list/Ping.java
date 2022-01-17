package com.ice.main.command.list;

import net.dv8tion.jda.api.events.interaction.SlashCommandEvent;

import java.time.OffsetDateTime;

import static com.ice.main.util.EmbedCreator.createEmbed;

public class Ping {

    public void onCommand(SlashCommandEvent event) {
        event.getHook().editOriginalEmbeds(
                createEmbed("Pong!  \uD83C\uDFD3", "⌛ : xx ms\n\n⏱️ :  ms", "", "", "", OffsetDateTime.now(), 0x00FFFF)
        ).queue(
                i -> event.getHook().editOriginalEmbeds(createEmbed("Pong!  \uD83C\uDFD3", "⌛ : " +
                        (Integer.parseInt(String.valueOf(i.getTimeCreated().toInstant().toEpochMilli() -
                                event.getInteraction().getTimeCreated().toInstant().toEpochMilli() -
                                event.getJDA().getGatewayPing())) < 0 ? "1" : Integer.parseInt(String.valueOf(i.getTimeCreated().toInstant().toEpochMilli() -
                                event.getInteraction().getTimeCreated().toInstant().toEpochMilli() -
                                event.getJDA().getGatewayPing()))) +
                        " ms\n\n⏱️ : " +
                        event.getJDA().getGatewayPing() + " ms", "", "", "", OffsetDateTime.now(), 0x00FFFF)
                ).queue()
        );
    }
}
