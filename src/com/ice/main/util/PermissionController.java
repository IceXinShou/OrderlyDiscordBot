package com.ice.main.util;

import net.dv8tion.jda.api.Permission;
import net.dv8tion.jda.api.entities.GuildChannel;
import net.dv8tion.jda.api.entities.Member;
import net.dv8tion.jda.api.entities.Role;

import java.util.List;

public class PermissionController {
    public static void addPermission(Member member, GuildChannel channel, List<Permission> permissions) {
        if (channel.getPermissionContainer().getPermissionOverride(member) == null)
            channel.getPermissionContainer().createPermissionOverride(member).grant(permissions).queue();
        else
            channel.getPermissionContainer().upsertPermissionOverride(member).grant(permissions).queue();
    }

    public static void addPermission(Role role, GuildChannel channel, List<Permission> permissions) {
        if (channel.getPermissionContainer().getPermissionOverride(role) == null)
            channel.getPermissionContainer().createPermissionOverride(role).grant(permissions).queue();
        else
            channel.getPermissionContainer().upsertPermissionOverride(role).grant(permissions).queue();
    }

    public static void removePermission(Member member, GuildChannel channel, List<Permission> permissions) {
        if (channel.getPermissionContainer().getPermissionOverride(member) == null)
            channel.getPermissionContainer().createPermissionOverride(member).deny(permissions).queue();
        else
            channel.getPermissionContainer().upsertPermissionOverride(member).deny(permissions).queue();
    }

    public static void removePermission(Role role, GuildChannel channel, List<Permission> permissions) {
        if (channel.getPermissionContainer().getPermissionOverride(role) == null)
            channel.getPermissionContainer().createPermissionOverride(role).deny(permissions).queue();
        else
            channel.getPermissionContainer().upsertPermissionOverride(role).deny(permissions).queue();
    }
}
