import org.json.JSONArray;
import org.json.JSONObject;

import java.util.Objects;

import static main.java.util.UrlDataGetter.getData;

public class Test {
    Test() {
        long endTime = System.currentTimeMillis() / 1000;
        long startTime = endTime;
        int step = 1;

        String popUrl = "https://grafana.tipsy.coffee/api/datasources/proxy/1/api/v1/query_range?query=max(popcat)%20by%20(region)&start=" + startTime + "&end=" + endTime + "&step=" + step;

        JSONObject resultJson = new JSONObject(getData(popUrl));
        JSONArray allRegion = resultJson.getJSONObject("data").getJSONArray("result");

        Map<String, Long> countryInfo = new HashMap<>();
        for (Object i : allRegion) {
            String countryCode = ((JSONObject) i).getJSONObject("metric").getString("region").toLowerCase();
            long value = Long.parseLong(((JSONObject) i).getJSONArray("values").getJSONArray(0).getString(1));
            countryInfo.put(countryCode, value);
        }
        countryInfo = sortByValue(countryInfo);
        for (Map.Entry<String, Long> country : countryInfo.entrySet()) {
            countryCodeToEmoji(country.getKey());
        }
    }
//    private ImageGraphMaker getPopGraph() {
//
//    }

    @SuppressWarnings("ALL")
    public static void main(String[] args) {
        new Test();
    }
}