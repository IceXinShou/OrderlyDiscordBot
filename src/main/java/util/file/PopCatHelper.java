package main.java.util.file;

import main.java.util.graph.ImageGraphMaker;
import net.dv8tion.jda.api.entities.Emoji;
import net.dv8tion.jda.api.events.interaction.GenericInteractionCreateEvent;
import net.dv8tion.jda.api.events.interaction.SelectionMenuEvent;
import net.dv8tion.jda.api.interactions.components.ActionRow;
import net.dv8tion.jda.api.interactions.components.selections.SelectionMenu;
import org.json.JSONArray;
import org.json.JSONObject;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static main.java.event.Log.deleteNoLog;
import static main.java.lang.LangKey.*;
import static main.java.util.CountryCodeToEmoji.countryCodeToEmoji;
import static main.java.util.EmbedCreator.createEmbed;
import static main.java.util.SortByValue.sortByValue;
import static main.java.util.UrlDataGetter.getData;

public class PopCatHelper {
    public Map<String, String> countryCode = new HashMap<>();
    public Map<String, String> countryName = new HashMap<>();

    public PopCatHelper() {
        try {
            Map<String, Object> data = new JSONObject(Files.readString(Paths.get("countryCode.json"))).toMap();
            data.forEach((key, value) -> {
                countryCode.put((String) value, key);
                countryName.put(key, (String) value);
            });

        } catch (IOException e) {
            System.err.println(e.getMessage());
        }
    }

    private Map<String, Long> speedLeaderboard;
    private Map<String, Long> popLeaderboard;
    private long lastSpeedLeaderboardGetTime = System.currentTimeMillis();
    private long lastPopLeaderboardGetTime = System.currentTimeMillis();

    public void getPopLeaderboard(int offset, SelectionMenu.Builder builder) {
        if (System.currentTimeMillis() - lastPopLeaderboardGetTime > 10000 || popLeaderboard == null) {
            lastPopLeaderboardGetTime = System.currentTimeMillis();
            long endTime = lastPopLeaderboardGetTime / 1000;
            int step = 1;

            String popUrl = "https://grafana.tipsy.coffee/api/datasources/proxy/1/api/v1/query_range?query=max(popcat)%20by%20(region)&start=" + endTime + "&end=" + endTime + "&step=" + step;

            JSONObject resultJson = new JSONObject(getData(popUrl));
            JSONArray allRegion = resultJson.getJSONObject("data").getJSONArray("result");

            popLeaderboard = new HashMap<>();
            for (Object i : allRegion) {
                String countryCode = ((JSONObject) i).getJSONObject("metric").getString("region").toLowerCase();
                long value = Long.parseLong(((JSONObject) i).getJSONArray("values").getJSONArray(0).getString(1));
                popLeaderboard.put(countryCode, value);
            }
            popLeaderboard = sortByValue(popLeaderboard);
        }

        int count = 0;
        for (Map.Entry<String, Long> country : popLeaderboard.entrySet()) {
            if (builder.getOptions().size() == 24)
                break;
            if (++count > offset) {
                try {
                    builder.addOption(countryName.get(country.getKey().toUpperCase()), country.getKey().toUpperCase(), String.format("%,d", country.getValue()), Emoji.fromUnicode(countryCodeToEmoji(country.getKey())));

                } catch (Exception e) {
                    System.out.println(e.getMessage());
                }
            }
        }
    }

    public void getSpeedLeaderboard(int offset, SelectionMenu.Builder builder) {
        if (System.currentTimeMillis() - lastSpeedLeaderboardGetTime > 10000 || speedLeaderboard == null) {
            lastSpeedLeaderboardGetTime = System.currentTimeMillis();
            long endTime = lastSpeedLeaderboardGetTime / 1000;
            int step = 1;

            String popUrl = "https://grafana.tipsy.coffee/api/datasources/proxy/1/api/v1/query_range?query=sum(rate(popcat%5B5m%5D))%20by%20(region)&start=" + endTime + "&end=" + endTime + "&step=" + step;

            JSONObject resultJson = new JSONObject(getData(popUrl));
            JSONArray allRegion = resultJson.getJSONObject("data").getJSONArray("result");

            speedLeaderboard = new HashMap<>();
            for (Object i : allRegion) {
                String countryCode = ((JSONObject) i).getJSONObject("metric").getString("region").toLowerCase();
                long value = Long.parseLong(String.format("%.0f", ((JSONObject) i).getJSONArray("values").getJSONArray(0).getDouble(1)));
                speedLeaderboard.put(countryCode, value);
            }
            speedLeaderboard = sortByValue(speedLeaderboard);
        }

        int count = 0;
        for (Map.Entry<String, Long> country : speedLeaderboard.entrySet()) {
            if (builder.getOptions().size() == 24)
                break;
            if (++count > offset) {
                try {
                    builder.addOption(countryName.get(country.getKey().toUpperCase()), country.getKey().toUpperCase(), String.format("%,d", country.getValue()) + " PPS", Emoji.fromUnicode(countryCodeToEmoji(country.getKey())));

                } catch (Exception e) {
                    System.out.println(e.getMessage());
                }
            }
        }
    }

    public void getAllData(SelectionMenu.Builder builder, GenericInteractionCreateEvent event, int page, boolean first, boolean pop) {
        if (page > 1) {
            builder.addOption(lang.get(POPCATHELPER_PREVIEWS), "page:up:" + (page - 1), Emoji.fromUnicode("⬅️"));
        }
        if (pop)
            getPopLeaderboard(page == 1 ? 0 : page * 23 - 22, builder);
        else
            getSpeedLeaderboard(page == 1 ? 0 : page * 23 - 22, builder);
        builder.addOption(lang.get(POPCATHELPER_NEXT), "page:down:" + (page + 1), Emoji.fromUnicode("➡️"));
        if (first)
            event.getHook().editOriginalEmbeds().setActionRows(ActionRow.of(builder.build())).setEmbeds(createEmbed(lang.get(POPCATHELPER_DETAIL_INFORMATION), 0xa3d7fe)).queue();
        else
            event.deferReply(true).addActionRows(ActionRow.of(builder.build())).addEmbeds(createEmbed(lang.get(POPCATHELPER_DETAIL_INFORMATION), 0xa3d7fe)).queue();

    }

    public void getOneData(SelectionMenuEvent event, boolean pop) {
        long endTime = System.currentTimeMillis() / 1000;
        long startTime = endTime - 60 * 60 * 24 * 3;
        int step = (int) ((endTime - startTime) / 50);

        String popUrl = "https://grafana.tipsy.coffee/api/datasources/proxy/1/api/v1/query_range?query=" +
                (pop ? "max(popcat)" : "sum(rate(popcat%5B5m%5D))") +
                "%20by%20(region)&start=" + startTime + "&end=" + endTime + "&step=" + step;

        List<Long> timeList = new ArrayList<>();
        List<Double> valueList = new ArrayList<>();
        JSONObject resultJson = new JSONObject(getData(popUrl));
        JSONArray allRegion = resultJson.getJSONObject("data").getJSONArray("result");
        for (Object i : allRegion) {
            if (((JSONObject) i).getJSONObject("metric").getString("region").equals(event.getValues().get(0))) {
                JSONArray regionData = ((JSONObject) i).getJSONArray("values");
                for (Object j : regionData) {
                    JSONArray data = (JSONArray) j;
                    timeList.add(data.getLong(0));
                    valueList.add(data.getDouble(1));
                }
                break;

            }
        }


        event.getChannel().sendMessageEmbeds(
                createEmbed(countryName.get(event.getValues().get(0)),
                        "https://www.countryflags.io/" + event.getValues().get(0) + "/flat/64.png",
                        "attachment://cat.png",
                        0x00FFFF)
        ).addFile(new ImageGraphMaker(800, 500, timeList, valueList).getImageBytes(), "cat.png").queue(i -> deleteNoLog(i, 10));
        event.deferEdit().queue();
    }
}