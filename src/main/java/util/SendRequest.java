package main.java.util;

import java.io.IOException;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;

import static main.java.BotSetting.botToken;

public class SendRequest {

    public HttpURLConnection sendRequestNoResponse(String endPoint, String method, byte[] payload, String contentType) {
        try {
            java.net.URL url = new URL("https://discordapp.com/api/v9" + URLEncoder.encode(endPoint, StandardCharsets.UTF_8));
            HttpURLConnection conn = (HttpURLConnection) url.openConnection();

            conn.setRequestProperty("Authorization", "Bot " + botToken);
            if (method.equals("PATCH")) {
                conn.setRequestProperty("X-HTTP-Method-Override", "PATCH");
                conn.setRequestMethod("POST");
            } else
                conn.setRequestMethod(method);
            conn.setRequestProperty("Content-Type", contentType);
            conn.setRequestProperty("User-Agent", "Mozilla/5.0 (compatible; Discordbot/2.0; +https://discordapp.com)");
            conn.setUseCaches(false);
            conn.setDoInput(true);

            //have payload
            if (payload != null && payload.length > 0) {
                conn.setDoOutput(true);
                conn.setRequestProperty("Content-Length", String.valueOf(payload.length));
                conn.getOutputStream().write(payload);
            }
            //get response code
            if (conn.getResponseCode() > 299) {
                if (conn.getResponseCode() > 399) {
                    System.err.println("request failed, ResponseCode: " + conn.getResponseCode() + ", URL: " + endPoint);
                    String errorString = readResponse(conn.getErrorStream());
                    System.out.println(errorString);
                    conn.disconnect();
                    return conn;
                }
                System.out.println("warn! ResponseCode: " + conn.getResponseCode() + ", URL: " + endPoint);
                String warnString = readResponse(conn.getInputStream());
                System.out.println(warnString);
                conn.disconnect();
            }
            return conn;
        } catch (IOException e) {
            System.err.println(e.getMessage());
            return null;
        }

    }

    private String readResponse(InputStream in) {
        try {
            StringBuilder builder = new StringBuilder();
            byte[] buff = new byte[1024];
            int length;
            //when read it end
            while ((length = in.read(buff)) > 0) {
                builder.append(new String(buff, 0, length));
            }
            in.close();
            return builder.toString();
        } catch (IOException e) {
            System.err.println("[SendRequest] " + e.getMessage());
            return null;
        }
    }

}
