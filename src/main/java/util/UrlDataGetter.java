package main.java.util;

import org.jetbrains.annotations.NotNull;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.nio.charset.StandardCharsets;

public class UrlDataGetter {
    public static String getUrlData(String urlStr) {
        try {
            HttpURLConnection conn = (HttpURLConnection) new URL(urlStr).openConnection();
            conn.setRequestMethod("GET");
            conn.setUseCaches(false);
            conn.setDoInput(true);

            //get response code
            String result;
            if (conn.getResponseCode() > 399) {
                result = readResponse(conn.getErrorStream());
            } else
                result = readResponse(conn.getInputStream());
            conn.disconnect();
            return result;
        } catch (IOException e) {
            return "";
        }
    }

    public static String postData(String input, @NotNull String payload) {
        return postData(input, payload, null);
    }

    public static String postData(String input, @NotNull String payload, String cookie) {
        try {
            HttpURLConnection connection = (HttpURLConnection) new URL(input).openConnection();
            connection.setRequestMethod("POST");
            connection.setRequestProperty("Content-Type", "application/json; charset=UTF-8");
            if (cookie != null)
                connection.setRequestProperty("Cookie", cookie);
            connection.setUseCaches(false);
            connection.setDoOutput(true);
            //post
            OutputStream payloadOut = connection.getOutputStream();
            payloadOut.write(payload.getBytes(StandardCharsets.UTF_8));
            payloadOut.flush();
            //get
            InputStream in;
            if (connection.getResponseCode() > 399)
                in = connection.getErrorStream();
            else
                in = connection.getInputStream();
            return readResponse(in);
        } catch (IOException e) {
            return null;
        }
    }

    private static String readResponse(@NotNull InputStream in) {
        try {
            StringBuilder builder = new StringBuilder();
            byte[] buff = new byte[1024];
            int length;
            //when readed it end
            while ((length = in.read(buff)) > 0) {
                builder.append(new String(buff, 0, length));
            }
            in.close();
            return builder.toString();
        } catch (IOException e) {
            System.err.println(e.fillInStackTrace().getMessage());
            return "";
        }
    }
}
