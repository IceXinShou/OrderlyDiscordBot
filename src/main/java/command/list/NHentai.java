package main.java.command.list;

import net.dv8tion.jda.api.entities.Emoji;
import net.dv8tion.jda.api.events.interaction.ButtonClickEvent;
import net.dv8tion.jda.api.events.interaction.SlashCommandEvent;
import net.dv8tion.jda.api.interactions.components.ActionRow;
import net.dv8tion.jda.api.interactions.components.Button;
import org.json.JSONObject;

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
        int jsonEnd = result.indexOf("start_page") - 19;

        JSONObject data = new JSONObject(result.substring(jsonStart, jsonEnd) + '}');

        String convertedID = data.getString("media_id");
        String title = data.getJSONObject("title").getString("pretty");
        int maxPage = data.getInt("num_pages");


        event.getHook().editOriginalEmbeds(createEmbed(
                        title, "https://nhentai.to/g/" + id,
                        "", "介紹", "nhentai", "https://t.dogehls.xyz/galleries/" + convertedID + "/cover.jpg", 0x00FFFF))
                .setActionRows(ActionRow.of(
                        Button.primary("NHentai:pP::0:" + id + ':' + maxPage + ':' + convertedID, Emoji.fromUnicode("⬅️")),
                        Button.success("NHentai:nP::0:" + id + ':' + maxPage + ':' + convertedID, Emoji.fromUnicode("➡️"))))
                .queue();
    }

    public void onButtonClick(ButtonClickEvent event, String[] args) {
        if (!args[0].equals("NHentai"))
            return;
        switch (args[1]) {
            case "pP" -> {
                if (args[3].equals("0")) { // 目的地錯誤
                    event.deferEdit().queue();
                    return;
                } else if (args[3].equals("1")) { // 目的地為首頁時

                    String result = getData("https://nhentai.to/g/" + args[4] + "/1");

                    int jsonStart = result.indexOf("gallery:") + 9;
                    int jsonEnd = result.indexOf("images") - 19;

                    JSONObject data = new JSONObject(result.substring(jsonStart, jsonEnd) + '}');

                    String title = data.getJSONObject("title").getString("pretty");

                    event.replyEmbeds(createEmbed(
                                    title, "https://nhentai.to/g/" + args[4],
                                    "", "介紹", "nhentai", "https://t.dogehls.xyz/galleries/" + args[5] + "/cover.jpg", 0x00FFFF))
                            .addActionRows(ActionRow.of(
                                    Button.primary("NHentai:pP::0:" + args[4] + ':' + args[5], Emoji.fromUnicode("⬅️")),
                                    Button.success("NHentai:nP::0:" + args[4] + ':' + args[5], Emoji.fromUnicode("➡️"))))
                            .setEphemeral(true).queue();
                    return;
                }

                int page = Integer.parseInt(args[3]) - 1;

                event.replyEmbeds(createEmbed( // 上一頁
                                args[4], "https://nhentai.to/g/" + args[4],
                                "", "第 " + (page) + " 頁", "nhentai", "https://t.dogehls.xyz/galleries/" + args[5] + "/cover.jpg", 0x00FFFF, "https://t.dogehls.xyz/galleries/" + args[5] + "/" + page + ".jpg"))
                        .addActionRows(ActionRow.of(
                                Button.primary("NHentai:pP::" + (page) + ":" + args[4] + ':' + args[5], Emoji.fromUnicode("⬅️")),
                                Button.success("NHentai:nP::" + (page) + ":" + args[4] + ':' + args[5], Emoji.fromUnicode("➡️"))))
                        .setEphemeral(true).queue();
            }
            case "nP" -> {

                int page = Integer.parseInt(args[3])+1;
                if (page > Integer.parseInt(args[5])){
                    event.deferEdit().queue();
                    return;
                }
                event.replyEmbeds(createEmbed(
                                args[4], "https://nhentai.to/g/" + args[4],
                                "", "第 " + page + " 頁", "nhentai", "https://t.dogehls.xyz/galleries/" + args[5] + "/cover.jpg", 0x00FFFF, "https://t.dogehls.xyz/galleries/" + args[5] + "/" + page + ".jpg"))
                        .addActionRows(ActionRow.of(
                                Button.primary("NHentai:pP::" + (page) + ":" + args[4] + ':' + args[5], Emoji.fromUnicode("⬅️")),
                                Button.success("NHentai:nP::" + (page) + ":" + args[4] + ':' + args[5], Emoji.fromUnicode("➡️"))))
                        .setEphemeral(true).queue();
            }
        }
    }
}
