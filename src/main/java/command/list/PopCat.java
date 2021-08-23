package main.java.command.list;

import main.java.util.file.PopCatHelper;
import net.dv8tion.jda.api.events.interaction.SelectionMenuEvent;
import net.dv8tion.jda.api.events.interaction.SlashCommandEvent;
import net.dv8tion.jda.api.interactions.components.selections.SelectionMenu;
import org.jetbrains.annotations.NotNull;

import java.util.HashMap;
import java.util.Map;

public class PopCat {
    Map<String, Long> count = new HashMap<>();
    PopCatHelper helper = new PopCatHelper();


    public void onCommandTop(@NotNull SlashCommandEvent event) {
        SelectionMenu.Builder builder = SelectionMenu.create("PopCat:top:" + event.getUser().getId() + ":1"); // page1
        helper.getAllData(builder, event, 1, true, true);
    }

    public void onCommandSpeed(@NotNull SlashCommandEvent event) {
        SelectionMenu.Builder builder = SelectionMenu.create("PopCat:speed:" + event.getUser().getId() + ":1"); // page1
        helper.getAllData(builder, event, 1, true, false);
    }

    public void onSelectTop(@NotNull SelectionMenuEvent event, String @NotNull [] args) {
        if (!args[0].equals("PopCat") || !args[1].equals("top"))
            return;
        if (event.getValues().get(0).startsWith("page")) {
            String[] data = event.getValues().get(0).split(":");
            SelectionMenu.Builder builder = SelectionMenu.create("PopCat:top:" + event.getUser().getId() + ":" + Integer.parseInt(data[2])); // page1
            helper.getAllData(builder, event, Integer.parseInt(data[2]), false, true);
        } else
            helper.getOneData(event, true);
    }

    public void onSelectSpeed(@NotNull SelectionMenuEvent event, String @NotNull [] args) {
        if (!args[0].equals("PopCat") || !args[1].equals("speed"))
            return;
        if (event.getValues().get(0).startsWith("page")) {
            String[] data = event.getValues().get(0).split(":");
            SelectionMenu.Builder builder = SelectionMenu.create("PopCat:top:" + event.getUser().getId() + ":" + Integer.parseInt(data[2])); // page1
            helper.getAllData(builder, event, Integer.parseInt(data[2]), false, false);
        } else
            helper.getOneData(event, false);
    }
}