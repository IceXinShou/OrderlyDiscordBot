package main.java.util;

import net.dv8tion.jda.api.Permission;
import net.dv8tion.jda.api.entities.MessageEmbed;
import net.dv8tion.jda.api.events.interaction.SlashCommandEvent;
import org.jetbrains.annotations.NotNull;

import java.util.Objects;

import static main.java.BotSetting.botOwnerID;
import static main.java.BotSetting.noPermissionStringERROR;
import static main.java.util.EmbedCreator.createEmbed;

public class PermissionERROR {

    public static @NotNull MessageEmbed noPermissionERROREmbed(@NotNull Permission permission) {
        return createEmbed(noPermissionStringERROR + " `(" + permission.getName() + ")`", 0xFF0000);
    }

    public static boolean hasPermission(Permission permission, @NotNull SlashCommandEvent event, @NotNull Boolean ownerSkip) {
        if (ownerSkip)
            if (botOwnerID.contains(event.getUser().getId()))
                return true;
        if (Objects.requireNonNull(event.getMember()).hasPermission(permission))
            return true;
        else
            event.getHook().editOriginalEmbeds(createEmbed(noPermissionStringERROR + " `(" + permission.getName() + ")`", 0xFF0000)).queue();
        return false;
    }
}
