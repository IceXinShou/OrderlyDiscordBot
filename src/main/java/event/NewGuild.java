package main.java.event;

import main.java.command.CommandRegister;
import main.java.command.list.Help;
import net.dv8tion.jda.api.events.guild.GuildJoinEvent;

import java.time.OffsetDateTime;

import static main.java.util.EmbedCreator.createEmbed;

public class NewGuild {
    public void onCommand(GuildJoinEvent event, CommandRegister commandRegister) {
        commandRegister.addPublicSlashCommand(event.getGuild());
        try {
            event.getGuild().retrieveOwner().complete().getUser().openPrivateChannel().queue(i ->
                    i.sendMessageEmbeds(createEmbed("您已邀請 <**" +
                            event.getGuild().getSelfMember().getUser().getAsTag() +
                            "**> 進入 <**" + event.getGuild().getName() + "**>\n輸入 `/help` 顯示幫助列表\n" +
                            "You have invited <**" + event.getGuild().getSelfMember().getUser().getAsTag() +
                            "**> join <**" + event.getGuild().getName() +
                            "**> Discord Server\nType `/help` to show helps", "", "", "", "", new Help().summonSelfMemberFields(null, true), OffsetDateTime.now(), 0x00FFFF)).queue());

        } catch (Exception ignored) {
        }
    }
}
