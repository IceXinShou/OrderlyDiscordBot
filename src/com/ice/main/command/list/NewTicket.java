package com.ice.main.command.list;

import net.dv8tion.jda.api.Permission;
import net.dv8tion.jda.api.events.interaction.SlashCommandEvent;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

import static com.ice.main.util.PermissionERROR.permissionCheck;

public class NewTicket {
    List<String> guildLimit = new ArrayList<>();
    ScheduledExecutorService executor;

    NewTicket() {
        executor = Executors.newScheduledThreadPool(1);
    }

    public boolean onCommand(SlashCommandEvent event) {
        // command not from guild
        if (!event.isFromGuild())
            return false;
        // no permission or has already setting
        if (!permissionCheck(Permission.ADMINISTRATOR, event, true) || guildLimit.contains(event.getGuild().getId()))
            return false;

        // add guild limit
        guildLimit.add(event.getGuild().getId());

        // create thread
        Future<?> future = executor.submit(new TicketThread(event.getGuild(), event.getMember()));

        // thread time limit
        executor.schedule(() -> {
            future.cancel(true);
        }, 5, TimeUnit.MINUTES);
        executor.shutdown();

        // remove guild limit
        guildLimit.remove(event.getGuild().getId());

        return true;
    }
}