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
            channel.createPermissionOverride(member).grant(permissions).queue();
        else
            channel.upsertPermissionOverride(member).grant(permissions).queue();
    }

    public static void addPermission(Role role, @NotNull GuildChannel channel, List<Permission> permissions) {
        if (channel.getPermissionOverride(role) == null)
            channel.createPermissionOverride(role).grant(permissions).queue();
        else
            channel.upsertPermissionOverride(role).grant(permissions).queue();
    }

    public static void removePermission(Member member, @NotNull GuildChannel channel, List<Permission> permissions) {
        if (channel.getPermissionOverride(member) == null)
            channel.createPermissionOverride(member).deny(permissions).queue();
        else
            channel.upsertPermissionOverride(member).deny(permissions).queue();
    }

    public static void removePermission(Role role, @NotNull GuildChannel channel, List<Permission> permissions) {
        if (channel.getPermissionOverride(role) == null)
            channel.createPermissionOverride(role).deny(permissions).queue();
        else
            channel.upsertPermissionOverride(role).deny(permissions).queue();
    }
}
