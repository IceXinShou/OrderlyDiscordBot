package main.java.automatic;

import main.java.util.Funtions;
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
import static main.java.util.Funtions.createEmbed;
import static main.java.util.GuildUtil.guild;
import static main.java.util.JsonKeys.CHINESE_NICK;

public class TicketChannel extends ListenerAdapter {
    @Override
    public void onButtonClick(@NotNull ButtonClickEvent event) {
        if (event.getComponentId().equals("newTicket")) {
            String memberName = memberData.getJSONObject(event.getUser().getId()).getString(CHINESE_NICK);
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

                MessageBuilder builder = new MessageBuilder();
                builder.setContent(defaultServiceMessage.replace("%reporter%", event.getMember().getAsMention()) +
                        "_\u200B".repeat(397) + "_" + Funtions.tagRoles(serviceTagRoleID));
                builder.setActionRows(ActionRow.of(Button.of(ButtonStyle.DANGER, "deleteChannel", "刪除頻道", Emoji.fromEmote(emoji.trashCan))));
                m.sendMessage(builder.build()).queue();
            });
        } else if (event.getComponentId().equals("deleteChannel")) {
            if (event.getMember().hasPermission(Permission.MANAGE_CHANNEL))
                event.getTextChannel().delete().queue();
            else
                event.deferReply().addEmbeds(createEmbed(event.getMember().getAsMention() + " 嘗試刪除此頻道, 卻因沒有頻道權限而被拒絕", 0xFF0000)).queue();
        }
    }
}
