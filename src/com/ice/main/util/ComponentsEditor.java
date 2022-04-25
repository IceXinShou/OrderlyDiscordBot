package com.ice.main.util;

import net.dv8tion.jda.api.interactions.components.ActionRow;
import net.dv8tion.jda.api.interactions.components.Component;
import net.dv8tion.jda.api.interactions.components.ItemComponent;

import java.util.ArrayList;
import java.util.Collection;

public class ComponentsEditor {
    //    List<MessageEmbed> embeds = new ArrayList<>();
    ButtonEditor[][] buttonControllers = new ButtonEditor[5][5];
    Byte[] actionRowSize = new Byte[5];

    ComponentsEditor() {

    }

    public Collection<ActionRow> finishEdit() {
        Collection<ActionRow> actionRows = new ArrayList<>();

        for (int i = 0; i < 5; ++i) {
            if (actionRowSize[i] == 0)
                continue;
            Collection<ItemComponent> components = new ArrayList<>();
            for (int j = 0; j < i; ++j)
                components.add((ItemComponent) buttonControllers[i][j].finish());

            actionRows.add(ActionRow.of(components));
        }
        return actionRows;
    }
//
//    public boolean addEmbed(MessageEmbed embed) {
//        try {
//            embeds.add(embed);
//        } catch (Exception e) {
//            System.err.println("Something ERROR at the embed adding!");
//            System.err.println(e.getMessage());
//            return false;
//        }
//        return true;
//    }
//
//    public boolean removeEmbed(int pos) {
//        try {
//            embeds.remove(pos);
//        } catch (Exception e) {
//            System.err.println("Something ERROR at the embed removing!");
//            System.err.println(e.getMessage());
//            return false;
//        }
//        return true;
//    }
//
//    public boolean addEmbeds(Collection<MessageEmbed> embeds) {
//        try {
//            this.embeds.addAll(embeds);
//        } catch (Exception e) {
//            System.err.println("Something ERROR at the embeds adding!");
//            System.err.println(e.getMessage());
//            return false;
//        }
//        return true;
//    }
//
//    public boolean clearEmbeds() {
//        try {
//            embeds.clear();
//        } catch (Exception e) {
//            System.err.println("Something ERROR at the embeds clearing!");
//            System.err.println(e.getMessage());
//            return false;
//        }
//        return true;
//    }

    public ButtonEditor addButton(Byte row) {
        ButtonEditor controller;
        try {
            if (actionRowSize[row] == 5) {
                System.err.println("This actionRow is full of buttons!");
                return null;
            }
            controller = new ButtonEditor(row, actionRowSize[row]);
            buttonControllers[row][actionRowSize[row]] = controller;
        } catch (Exception e) {
            System.err.println("Something ERROR at the embeds clearing!");
            System.err.println(e.getMessage());
            return null;
        }
        ++actionRowSize[row];
        return controller;
    }

}
/**
 * EmbedGUI gui = new EmbedGUI();
 * gui.setEmbed(int pos, Embed);
 * gui.addEmbed(Embed);
 * gui.setEmbeds(Embeds);
 * gui
 */