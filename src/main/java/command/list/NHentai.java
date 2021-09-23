package main.java.command.list;

import net.dv8tion.jda.api.entities.Emoji;
import net.dv8tion.jda.api.events.interaction.ButtonClickEvent;
import net.dv8tion.jda.api.events.interaction.SlashCommandEvent;
import net.dv8tion.jda.api.interactions.components.ActionRow;
import net.dv8tion.jda.api.interactions.components.Button;
import org.json.JSONObject;

import static main.java.Main.emoji;
import static main.java.util.EmbedCreator.createEmbed;
import static main.java.util.UrlDataGetter.getData;

public class NHentai {
    public void onCommand(SlashCommandEvent event) {
        // https://nhentai.to/g/372323/1
        // https://t.dogehls.xyz/galleries/convertedID/cover.jpg

        String id = event.getOption("id").getAsString();

        String result = getData("https://nhentai.to/g/" + id + "/1");

        if (result.contains("Not Found"))
            event.getHook().editOriginalEmbeds(createEmbed("番號錯誤", 0xFF0000)).queue();

        int jsonStart = result.indexOf("gallery:") + 9;
        int jsonEnd = result.indexOf("images") - 19;

        JSONObject data = new JSONObject(result.substring(jsonStart, jsonEnd) + '}');

        String convertedID = data.getString("media_id");
        String title = data.getJSONObject("title").getString("pretty");


        event.getHook().editOriginalEmbeds(createEmbed(
                        title, "https://nhentai.to/g/" + id,
                        "", "介紹", "nhentai", "https://t.dogehls.xyz/galleries/" + convertedID + "/cover.jpg", 0x00FFFF))
                .setActionRows(ActionRow.of(Button.primary("NHentai:previousPage::0:" + id, Emoji.fromEmote(emoji.back)),
                        Button.success("NHentai:nextPage::0:" + id, Emoji.fromEmote(emoji.next))))
                .queue();
    }

    public void onButtonClick(ButtonClickEvent event, String[] args) {
        if (!args[0].equals("NHentai"))
            return;


        String result = getData("https://nhentai.to/g/" + args[4] + "/1");

        int jsonStart = result.indexOf("gallery:") + 9;
        int jsonEnd = result.indexOf("images") - 19;

        JSONObject data = new JSONObject(result.substring(jsonStart, jsonEnd) + '}');

        String convertedID = data.getString("media_id");
        String title = data.getJSONObject("title").getString("pretty");

        switch (args[1]) {
            case "previousPage" -> {
                if (args[3].equals("0")) {
                    event.deferEdit().queue();
                    return;
                }

                int page = Integer.parseInt(args[3]) - 1;

                event.replyEmbeds(createEmbed(
                                title, "https://nhentai.to/g/" + args[4],
                                "", "內文", "nhentai", "https://t.dogehls.xyz/galleries/" + convertedID + "/cover.jpg", 0x00FFFF, "https://t.dogehls.xyz/galleries/" + convertedID + "/" + page + (page > 1 ? ".png" : ".jpg")))
                        .addActionRows(ActionRow.of(Button.primary("NHentai:previousPage::0:" + page, Emoji.fromEmote(emoji.back)),
                                Button.success("NHentai:nextPage::" + page + ":" + args[4], Emoji.fromEmote(emoji.next))))
                        .setEphemeral(true).queue();
            }
            case "nextPage" -> {

                int page = Integer.parseInt(args[3]) + 1;

                System.out.println("https://t.dogehls.xyz/galleries/" + convertedID + "/" + page + (page > 1 ? ".png" : ".jpg"));

                event.replyEmbeds(createEmbed(
                                title, "https://nhentai.to/g/" + args[4],
                                "", "內文", "nhentai", "https://t.dogehls.xyz/galleries/" + convertedID + "/cover.jpg", 0x00FFFF, "https://t.dogehls.xyz/galleries/" + convertedID + "/" + page + (page > 1 ? ".png" : ".jpg")))
                        .addActionRows(ActionRow.of(Button.primary("NHentai:previousPage::0:" + page, Emoji.fromEmote(emoji.back)),
                                Button.success("NHentai:nextPage::" + page + ":" + args[4], Emoji.fromEmote(emoji.next))))
                        .setEphemeral(true).queue();
            }
        }

    }
}
