package com.ice.main.command.list;

import net.dv8tion.jda.api.Permission;
import net.dv8tion.jda.api.entities.*;
import net.dv8tion.jda.api.interactions.components.buttons.ButtonStyle;

import java.util.*;

import static com.ice.main.util.PermissionController.addPermission;
import static net.dv8tion.jda.api.Permission.*;

public class TicketThread implements Runnable {
    TextChannel targetTextChannel;

    public TicketThread(Guild guild, Member member) {
        if (!makeChannel(guild, member)) return;


    }

    @Override
    public void run() {

    }

    private boolean makeChannel(Guild guild, Member member) {
        try {
            guild.createTextChannel("TicketCreating...")
                    .addMemberPermissionOverride(member.getIdLong(), Arrays.asList(VIEW_CHANNEL, MESSAGE_ADD_REACTION), Collections.emptyList())
                    .queue(textChannel -> {
                        addPermission(member, textChannel, Arrays.asList(Permission.VIEW_CHANNEL, Permission.MESSAGE_ADD_REACTION));
                        this.targetTextChannel = textChannel;
                    });
        } catch (Exception e) {
            System.err.println("Something ERROR while channel creating!");
            System.err.println(e.getMessage());
            return false;
        }
        return true;
    }

    private void printDefaultEmbed() {
        Collection<MessageEmbed.Field> fields = new ArrayList<>();

        fields.add(new MessageEmbed.Field("", "", false));
        fields.add(new MessageEmbed.Field("", "", false));
        fields.add(new MessageEmbed.Field("", "", false));


    }

    private void printButtonEmbed(ButtonData buttonData) {

        Collection<MessageEmbed.Field> fields = new ArrayList<>();

        fields.add(new MessageEmbed.Field("", "", false));
        fields.add(new MessageEmbed.Field("", "", false));
        fields.add(new MessageEmbed.Field("", "", false));


    }
}

class ButtonData {
    private String label;
    private Emoji emoji;
    private ButtonStyle buttonStyle;

    public ButtonData() {
        super();

    }

    protected String getLabel() {
        return label;
    }

    protected void setLabel(String label) {
        this.label = label;
    }

    protected Emoji getEmoji() {
        return emoji;
    }

    protected void setEmoji(Emoji emoji) {
        this.emoji = emoji;
    }

    protected ButtonStyle getButtonStyle() {
        return buttonStyle;
    }

    protected void setButtonStyle(ButtonStyle buttonStyle) {
        this.buttonStyle = buttonStyle;
    }

}