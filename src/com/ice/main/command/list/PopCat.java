package com.ice.main.command.list;

import com.ice.main.util.file.PopCatHelper;
import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent;
import net.dv8tion.jda.api.events.interaction.component.SelectMenuInteractionEvent;
import net.dv8tion.jda.api.interactions.components.selections.SelectMenu;

import java.util.HashMap;
import java.util.Map;

public class PopCat {
    Map<String, Long> count = new HashMap<>();
    PopCatHelper helper = new PopCatHelper();


    public void onCommandTop(SlashCommandInteractionEvent event) {
        SelectMenu.Builder builder = SelectMenu.create("PopCat:top:" + event.getUser().getId() + ":1"); // page1
        helper.getAllData(builder, event, 1, true, true);
    }

    public void onCommandSpeed(SlashCommandInteractionEvent event) {
        SelectMenu.Builder builder = SelectMenu.create("PopCat:speed:" + event.getUser().getId() + ":1"); // page1
        helper.getAllData(builder, event, 1, true, false);
    }

    public void onSelectTop(SelectMenuInteractionEvent event, String[] args) {
        if (!args[0].equals("PopCat") || !args[1].equals("top"))
            return;
        if (event.getValues().get(0).startsWith("page")) {
            String[] data = event.getValues().get(0).split(":");
            SelectMenu.Builder builder = SelectMenu.create("PopCat:top:" + event.getUser().getId() + ":" + Integer.parseInt(data[2])); // page1
            helper.getAllData(builder, event, Integer.parseInt(data[2]), false, true);
        } else
            helper.getOneData(event, true);
    }

    public void onSelectSpeed(SelectMenuInteractionEvent event, String[] args) {
        if (!args[0].equals("PopCat") || !args[1].equals("speed"))
            return;
        if (event.getValues().get(0).startsWith("page")) {
            String[] data = event.getValues().get(0).split(":");
            SelectMenu.Builder builder = SelectMenu.create("PopCat:top:" + event.getUser().getId() + ":" + Integer.parseInt(data[2])); // page1
            helper.getAllData(builder, event, Integer.parseInt(data[2]), false, false);
        } else
            helper.getOneData(event, false);
    }
}