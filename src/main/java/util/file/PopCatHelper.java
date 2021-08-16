package main.java.util.file;

import main.java.util.graph.ImageGraphMaker;
import net.dv8tion.jda.api.entities.Emoji;
import net.dv8tion.jda.api.events.interaction.SelectionMenuEvent;
import net.dv8tion.jda.api.events.interaction.SlashCommandEvent;
import net.dv8tion.jda.api.interactions.components.selections.SelectionMenu;
import org.json.JSONArray;
import org.json.JSONObject;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.*;
import java.util.function.Consumer;

import static main.java.util.CountryCodeToEmoji.countryCodeToEmoji;
import static main.java.util.EmbedCreator.createEmbed;
import static main.java.util.SortByValue.sortByValue;
import static main.java.util.UrlDataGetter.getData;

public class PopCatHelper {
    List<Long> timeList = new ArrayList<>();
    List<Double> valueList = new ArrayList<>();
    public Map<String, String> countryCode = new HashMap<>();
    public Map<String, String> countryName = new HashMap<>();

    public PopCatHelper() {
        try {
            Map<String, Object> data = new JSONObject(new String(Files.readAllBytes(Paths.get("E:\\Plugin\\RunBot\\countryCode.json")), StandardCharsets.UTF_8)).toMap();
            data.forEach((key, value) -> {
                countryCode.put((String) value, key);
                countryName.put(key, (String) value);
            });

        } catch (IOException e) {
            System.err.println(e.getMessage());
        }
    }

    private Map<String, Long> leaderboard;
    private long lastLeaderboardGetTime = System.currentTimeMillis();

    public void getLeaderboard(boolean pop, int offset, Consumer<Map.Entry<String, Long>> consumer) {
        if (System.currentTimeMillis() - lastLeaderboardGetTime > 10000) {
            lastLeaderboardGetTime = System.currentTimeMillis();
            long endTime = System.currentTimeMillis() / 1000;
            int step = 1;

            String popUrl = "https://grafana.tipsy.coffee/api/datasources/proxy/1/api/v1/query_range?query=" +
                    (pop ? "max(popcat)" : "sum(rate(popcat%5B5m%5D))") +
                    "%20by%20(region)&start=" + endTime + "&end=" + endTime + "&step=" + step;

            JSONObject resultJson = new JSONObject(getData(popUrl));
            JSONArray allRegion = resultJson.getJSONObject("data").getJSONArray("result");

            leaderboard = new HashMap<>();
            for (Object i : allRegion) {
                String countryCode = ((JSONObject) i).getJSONObject("metric").getString("region").toLowerCase();
                long value = Long.parseLong(((JSONObject) i).getJSONArray("values").getJSONArray(0).getString(1));
                leaderboard.put(countryCode, value);
            }
            leaderboard = sortByValue(leaderboard);
        }
        int count = 0;
        for (Map.Entry<String, Long> country : leaderboard.entrySet())
            if (++count > offset && count - offset < 25)
                consumer.accept(country);

    }

    public void getOneData(boolean pop, SelectionMenuEvent event) {
        long endTime = System.currentTimeMillis() / 1000;
        int step = 1;
        String popUrl = "https://grafana.tipsy.coffee/api/datasources/proxy/1/api/v1/query_range?query=" +
                (pop ? "max(popcat)" : "sum(rate(popcat%5B5m%5D))") +
                "%20by%20(region)&start=" + endTime + "&end=" + endTime + "&step=" + step;

        JSONObject resultJson = new JSONObject(getData(popUrl));
        JSONArray allRegion = resultJson.getJSONObject("data").getJSONArray("result");
        for (Object i : allRegion)
            if (((JSONObject) i).getJSONObject("metric").getString("region").equals(event.getValues().get(0))) {
                JSONArray regionData = ((JSONObject) i).getJSONArray("values");
                for (Object j : regionData) {
                    JSONArray data = (JSONArray) j;
                    timeList.add(data.getLong(0));
                    valueList.add(data.getDouble(1));
                }
                break;
            }
        event.getInteraction().deferReply(true).addEmbeds(createEmbed(countryCode.get(event.getValues().get(0)), "https://www.countryflags.io/" + event.getValues().get(0) + "/flat/64.png", Base64.getEncoder().encodeToString(new ImageGraphMaker(800, 500, timeList, valueList).getImageBytes()), 0x00FFFF)).queue();
    }

    public void getAllData(boolean pop, SelectionMenu.Builder builder, SlashCommandEvent event, int page) {
        if (page != 1) {
            builder.addOption("上一頁", "page" + (page - 1), Emoji.fromUnicode("⬅️"));
        }
        getLeaderboard(pop, page == 1 ? 1 : 25 * (page - 1) + 2, i ->
                builder.addOption(countryName.get(i.getKey()), i.getKey(), i.getValue().toString(), Emoji.fromUnicode(countryCodeToEmoji(i.getKey()))));
        builder.addOption("下一頁", "page" + (page + 1), Emoji.fromUnicode("➡️"));
        event.getHook().editOriginalComponents().setEmbeds(createEmbed("點擊可查看詳細資訊", 0xa3d7fe)).setActionRow(builder.build()).queue();
    }
}