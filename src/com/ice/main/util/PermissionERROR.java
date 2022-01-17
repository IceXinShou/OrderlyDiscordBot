package com.ice.main.util;

import net.dv8tion.jda.api.Permission;
import net.dv8tion.jda.api.entities.MessageEmbed;
import net.dv8tion.jda.api.events.interaction.SlashCommandEvent;

import static com.ice.main.BotSetting.botOwnerID;
import static com.ice.main.BotSetting.noPermissionStringERROR;
import static com.ice.main.util.EmbedCreator.createEmbed;

public class PermissionERROR {

    public static MessageEmbed noPermissionERROREmbed(Permission permission) {
        return createEmbed(
                noPermissionStringERROR + " `(" + permission.getName() + ")`", 0xFF0000);
    }

    public static boolean hasPermission(Permission permission, SlashCommandEvent event, Boolean ownerSkip) {
        if (ownerSkip)
            if (botOwnerID.contains(event.getUser().getId()))
                return true;
        if (event.getMember().hasPermission(permission))
            return true;
        else
            event.getHook().editOriginalEmbeds(createEmbed(noPermissionStringERROR + " `(" + permission.getName() + ")`", 0xFF0000)).queue();
        return false;
    }
}