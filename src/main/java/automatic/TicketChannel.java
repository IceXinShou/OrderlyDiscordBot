package main.java.automatic;

import main.java.event.Log;
import net.dv8tion.jda.api.MessageBuilder;
import net.dv8tion.jda.api.Permission;
import net.dv8tion.jda.api.entities.Category;
import net.dv8tion.jda.api.entities.Emoji;
import net.dv8tion.jda.api.events.interaction.ButtonClickEvent;
import net.dv8tion.jda.api.hooks.ListenerAdapter;
import net.dv8tion.jda.api.interactions.components.ActionRow;
import net.dv8tion.jda.api.interactions.components.Button;
import net.dv8tion.jda.api.interactions.components.ButtonStyle;
import org.jetbrains.annotations.NotNull;

import static main.java.BotSetting.*;
import static main.java.Main.emoji;
import static main.java.event.Join.memberData;
import static main.java.util.EmbedUtil.createEmbed;
import static main.java.util.GuildUtil.guild;
import static main.java.util.JsonKeys.CHINESE_NICK;
import static main.java.util.MessageFormatting.tagRole;

public class TicketChannel extends ListenerAdapter {
    @Override
    public void onButtonClick(@NotNull ButtonClickEvent event) {
        String memberName = memberData.getJSONObject(event.getUser().getId()).getString(CHINESE_NICK);
        if (event.getComponentId().equals("newTicket")) {
            Category category = null;
            int i = 0;
            for (; i < serviceCategoryID.size(); i++) {
                category = guild.getCategoryById(serviceCategoryID.get(i));
                if (category.getChannels().size() < 50)
                    break;
            }
            if (i == serviceCategoryID.size()) {
                System.err.println("沒有足夠的空間");
                return;
            }

            guild.createTextChannel(newServiceName.replace("%name%", memberName), category).queue(m -> {
                for (String str : serviceTagRoleID)
                    m.createPermissionOverride(guild.getRoleById(str)).setAllow(Permission.VIEW_CHANNEL).queue();

                m.createPermissionOverride(event.getMember()).setAllow(Permission.VIEW_CHANNEL).queue();

                m.sendMessage(createEmbed(tagRole(serviceTagRoleID), 0x9740b9)).queue(n ->
                        Log.deleteNoLog(n, 1)
                );

                MessageBuilder builder = new MessageBuilder();
                builder.setContent(defaultServiceMessage.replace("%reporter%", memberName));
                builder.setActionRows(ActionRow.of(Button.of(ButtonStyle.DANGER, "deleteChannel", "刪除頻道", Emoji.fromEmote(emoji.trashCan))));
                m.sendMessage(builder.build()).queue();
            });
        } else if (event.getComponentId().equals("deleteChannel")) {
            if (event.getMember().hasPermission(Permission.MANAGE_CHANNEL))
                event.getTextChannel().delete().queue();
        }
        event.deferEdit().queue();
    }
}