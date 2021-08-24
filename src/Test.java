import org.json.JSONArray;
import org.json.JSONObject;

import java.util.Objects;

import static main.java.util.UrlDataGetter.getData;

public class Test {
    Test() {
        long endTime = System.currentTimeMillis() / 1000;
        long startTime = endTime - 60 * 60 * 24 * 3;
        int step = (int) ((endTime - startTime) / 100);

        String popUrl = "https://grafana.tipsy.coffee/api/datasources/proxy/1/api/v1/query_range?query=sum(rate(popcat%5B5m%5D))%20by%20(region)&start=" + startTime + "&end=" + endTime + "&step=" + step;

        JSONObject resultJson = new JSONObject(getData(popUrl));
        JSONArray allRegion = resultJson.getJSONObject("data").getJSONArray("result");

        JSONArray val = null;
        for (Object i : allRegion) {
            String countryCode = ((JSONObject) i).getJSONObject("metric").getString("region").toLowerCase();
            if (countryCode.equals("tw")) {
                val = ((JSONObject) i).getJSONArray("values");
                break;
            }
        }
        for (Object i : val)) {
            JSONArray array = (JSONArray) i;
            Object data;
            if ((data = array.get(1)).equals(0)) {
                break;
            }
            System.out.println(data);
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