package main.java.youtube;

import org.json.JSONArray;
import org.json.JSONObject;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class YoutubeDecode {
    private final Map<String, Integer> functionType = new HashMap<>();
    private String[] steps;

    public YoutubeDecode(String videoID) {
        String videoEmbedURL = "https://www.youtube.com/embed/" + videoID;
        //get base.js
        String result = GetYoutubeInfo.getUrl(videoEmbedURL);
        int startIndex = result.indexOf("ytcfg.set(");
        String baseJsUrl = "https://www.youtube.com" +
                new JSONObject(result.substring(startIndex + 10))
                        .getJSONObject("WEB_PLAYER_CONTEXT_CONFIGS")
                        .getJSONObject("WEB_PLAYER_CONTEXT_CONFIG_ID_EMBEDDED_PLAYER")
                        .getString("jsUrl");

//        baseJsUrl = "https://www.youtube.com/yts/jsbin/player-vflYXLM5n/en_US/base.js";

        String baseJS = GetYoutubeInfo.getUrl(baseJsUrl);
//        System.out.println(baseJS.substring(1000000));

        //尋找亂序函式
        Pattern pattern = Pattern.compile("\\bfunction\\(\\w+\\)\\{[A-Za-z_=.(\")]+;([A-Za-z0-9_=.,(\");]*);return \\w+.join[(\")]*};");
        Matcher match = pattern.matcher(baseJS);
        if (match.find()) {
            steps = match.group(1).split(";");
        } else {
            System.err.println("Could not find the entry function for signature");
            return;
        }

        //尋找函式定義
        String fnMapName = steps[0].split("\\.")[0];
        pattern = Pattern.compile("\\w+ " + fnMapName + "=\\{(.*\\n*.*\\n*.*})};");
        match = pattern.matcher(baseJS);
        String functions;
        if (match.find()) {
            functions = match.group(1).replace("\n", "");
        } else {
            System.err.println("Could not find the signature decipher function body");
            return;
        }
        //分出函式
        pattern = Pattern.compile("(\\w+):function\\(\\w*,*\\w*\\)\\{([A-Za-z0-9,.() =;%\\[\\]]*)}");
        match = pattern.matcher(functions);
        while (match.find()) {
            String fct = match.group(2);
            int type;
            if (fct.contains("splice"))
                type = DecipherFunctionType.SLICE;
            else if (fct.contains("reverse"))
                type = DecipherFunctionType.REVERSE;
            else if (fct.contains("var") && fct.contains("="))
                type = DecipherFunctionType.SWAP;
            else
                type = 4;
            functionType.put(match.group(1), type);
        }

//        System.out.println(functions);
//        System.out.println(Arrays.toString(steps));
//        System.out.println(functionType);


//        System.out.println(Arrays.toString(steps));
//        System.out.println(signature);

    }

    public String decode(String signature){
        //解譯signature
        for (String i : steps) {
            String funName = i.split("\\.")[1];
            funName = funName.substring(0, funName.indexOf("("));
            String valueString = i.split(",")[1];
            int value = Integer.parseInt(valueString.substring(0, valueString.indexOf(")")));
            int type = functionType.get(funName);


            if (type == DecipherFunctionType.REVERSE) {
                StringBuilder builder = new StringBuilder(signature);
                signature = builder.reverse().toString();
            } else if (type == DecipherFunctionType.SLICE) {
                signature = signature.substring(value);
            } else if (type == DecipherFunctionType.SWAP) {
                char c = signature.charAt(0);
                value = value % signature.length();
                StringBuilder builder = new StringBuilder(signature);
                builder.setCharAt(0, signature.charAt(value));
                builder.setCharAt(value, c);
                signature = builder.toString();
            }
        }
        return signature;
    }
}
