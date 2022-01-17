package com.ice.main.util;

import net.dv8tion.jda.api.entities.Emoji;
import net.dv8tion.jda.api.interactions.components.Button;
import net.dv8tion.jda.api.interactions.components.ButtonStyle;
import net.dv8tion.jda.api.interactions.components.Component;

public class ButtonEditor {
    Byte actionRowPos, buttonPos;
    ButtonStyle buttonStyle = ButtonStyle.SUCCESS;
    String buttonLabel = "undefined", buttonId = "undefined";
    Emoji buttonEmoji = null;

    ButtonEditor(Byte actionRow, Byte buttonPos) {
        this.actionRowPos = actionRow;
        this.buttonPos = buttonPos;
    }

    public Byte getActionRowPos() {
        return this.actionRowPos;
    }

    public Byte getButtonPos() {
        return this.buttonPos;
    }

    public boolean setStyle(ButtonStyle buttonStyle) {
        try {
            this.buttonStyle = buttonStyle;
        } catch (Exception e) {
            System.err.println("Something ERROR at the button style setting!");
            System.err.println(e.getMessage());
            return false;
        }
        return true;
    }


    public boolean setLabel(String buttonLabel) {
        try {
            this.buttonLabel = buttonLabel;
        } catch (Exception e) {
            System.err.println("Something ERROR at the button label setting!");
            System.err.println(e.getMessage());
            return false;
        }
        return true;
    }


    public boolean setId(String buttonId) {
        try {
            this.buttonId = buttonId;
        } catch (Exception e) {
            System.err.println("Something ERROR at the button id setting!");
            System.err.println(e.getMessage());
            return false;
        }
        return true;
    }


    public boolean setEmoji(Emoji buttonEmoji) {
        try {
            this.buttonEmoji = buttonEmoji;
        } catch (Exception e) {
            System.err.println("Something ERROR at the button emoji setting!");
            System.err.println(e.getMessage());
            return false;
        }
        return true;
    }

    public Component finish() {
        Button button;
        try {
            if (buttonEmoji == null)
                button = Button.of(buttonStyle, buttonId, buttonLabel);
            else
                button = Button.of(buttonStyle, buttonId, buttonLabel, buttonEmoji);
        } catch (Exception e) {
            System.err.println("Something ERROR at the button finishing!");
            System.err.println(e.getMessage());
            return null;
        }
        return button;
    }
}