package main.java.command.list;

import main.java.util.file.PopCatHelper;
import net.dv8tion.jda.api.entities.MessageEmbed;
import net.dv8tion.jda.api.events.interaction.SelectionMenuEvent;
import net.dv8tion.jda.api.events.interaction.SlashCommandEvent;
import net.dv8tion.jda.api.interactions.components.selections.SelectionMenu;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static main.java.util.EmbedCreator.createEmbed;

public class PopCat {
    Map<String, Long> count = new HashMap<>();
    PopCatHelper helper = new PopCatHelper();


    public void onCommand(SlashCommandEvent event) {
        if (count.size() == 0) {
            event.getHook().editOriginalEmbeds(createEmbed("資料尚未取得, 請稍後再試", 0xFF0000)).queue();
        }

        List<MessageEmbed.Field> fields = new ArrayList<>();
        for (Map.Entry<String, Long> i : count.entrySet()) {
            fields.add(new MessageEmbed.Field(i.getKey(), i.getValue().toString(), false));
        }
        event.getHook().editOriginalEmbeds(createEmbed("世界排行：", fields, 0x00FFFF)).queue();
    }

    public void onCommandTop(@NotNull SlashCommandEvent event) {
        SelectionMenu.Builder builder = SelectionMenu.create("PopCat:top:" + event.getUser().getId() + ":1"); // page1
        helper.getAllData(true, builder, event, 1);
    }

    public void onCommandSpeed(@NotNull SlashCommandEvent event) {
        SelectionMenu.Builder builder = SelectionMenu.create("PopCat:speed:" + event.getUser().getId() + ":1"); // page1
        helper.getAllData(true, builder, event, 1);
    }

    public void onSelectTop(@NotNull SelectionMenuEvent event, String @NotNull [] args) {
        if (!args[0].equals("PopCat") && args[1].equals("top"))
            return;
        helper.getOneData(true, event);
    }

    public void onSelectSpeed(@NotNull SelectionMenuEvent event, String @NotNull [] args) {
        if (!args[0].equals("PopCat") && args[1].equals("speed"))
            return;
        helper.getOneData(false, event);
    }
}