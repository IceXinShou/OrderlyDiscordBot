package main.java.util;

import net.dv8tion.jda.api.Permission;
import net.dv8tion.jda.api.entities.GuildChannel;
import net.dv8tion.jda.api.entities.Member;
import net.dv8tion.jda.api.entities.Role;
import org.jetbrains.annotations.NotNull;

import java.util.List;

public class PermissionController {
    public static void addPermission(Member member, @NotNull GuildChannel channel, List<Permission> permissions) {
        if (channel.getPermissionOverride(member) == null)
            channel.putPermissionOverride(member).setAllow(permissions).queue();
        else
            channel.createPermissionOverride(member).setAllow(permissions).queue();
    }

    public static void addPermission(Role role, @NotNull GuildChannel channel, List<Permission> permissions) {
        if (channel.getPermissionOverride(role) == null)
            channel.putPermissionOverride(role).setAllow(Permission.VIEW_CHANNEL).queue();
        else
            channel.createPermissionOverride(role).setAllow(Permission.VIEW_CHANNEL).queue();
    }

    public static void removePermission(Member member, @NotNull GuildChannel channel, List<Permission> permissions) {
        if (channel.getPermissionOverride(member) == null)
            channel.putPermissionOverride(member).setDeny(permissions).queue();
        else
            channel.createPermissionOverride(member).setDeny(permissions).queue();
    }

    public static void removePermission(Role role, @NotNull GuildChannel channel, List<Permission> permissions) {
        if (channel.getPermissionOverride(role) == null)
            channel.putPermissionOverride(role).setDeny(permissions).queue();
        else
            channel.createPermissionOverride(role).setDeny(permissions).queue();
    }
}
