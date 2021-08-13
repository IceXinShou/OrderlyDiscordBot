package main.java.command.list;

import net.dv8tion.jda.api.events.interaction.SlashCommandEvent;
import org.jetbrains.annotations.NotNull;

import static main.java.Main.setting;
import static main.java.util.EmbedCreator.createEmbed;
import static main.java.util.PermissionERROR.hasPermission;
import static net.dv8tion.jda.api.Permission.ADMINISTRATOR;

public class Reload {

    public boolean onCommand(@NotNull SlashCommandEvent event) {
        if (!hasPermission(ADMINISTRATOR, event, true))
            return false;
        else {
            setting.reloadConfig();
            event.getHook().editOriginalEmbeds(createEmbed("重新載入完成", 0x00FFFF)).queue();
            return true;
        }
    }

}