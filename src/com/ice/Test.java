package com.ice;

import org.json.JSONObject;

import static com.ice.main.util.UrlDataGetter.getData;

public class Test {
    Test() {
//        long endTime = System.currentTimeMillis() / 1000;
//        long startTime = endTime - 60 * 60 * 24 * 3;
//        int step = (int) ((endTime - startTime) / 100);
//
//        String popUrl = "https://grafana.tipsy.coffee/api/datasources/proxy/1/api/v1/query_range?query=sum(rate(popcat%5B5m%5D))%20by%20(region)&start=" + startTime + "&end=" + endTime + "&step=" + step;
//
//        JSONObject resultJson = new JSONObject(getData(popUrl));
//        JSONArray allRegion = resultJson.getJSONObject("data").getJSONArray("result");
//
//        JSONArray val = null;
//        for (Object i : allRegion) {
//            String countryCode = ((JSONObject) i).getJSONObject("metric").getString("region").toLowerCase();
//            if (countryCode.equals("tw")) {
//                val = ((JSONObject) i).getJSONArray("values");
//                break;
//            }
//        }
//        for (Object i : val) {
//            JSONArray array = (JSONArray) i;
//            Object data;
//            if ((data = array.get(1)).equals(0)) {
//                break;
//            }
//        }

        String id = "362781";

        String result = getData("https://nhentai.to/g/" + id + "/1");

        int jsonStart = result.indexOf("gallery:") + 9;
        int jsonEnd = result.indexOf("images") - 19;


        System.out.println(new JSONObject(result.substring(jsonStart, jsonEnd) + '}'));


    }
//    private ImageGraphMaker getPopGraph() {
//
//    }

//    @SuppressWarnings("ALL")
//    public static void com.ice.main(String[] args) {
//        new com.ice.Test();
//    }
}