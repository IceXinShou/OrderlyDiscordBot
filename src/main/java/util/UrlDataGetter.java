package main.java.util;

import org.jetbrains.annotations.NotNull;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.nio.charset.StandardCharsets;

public class UrlDataGetter {
    public static String getData(String url) {
        return getData(url, null);
    }

    public static String getDataAuthorization(String url, String authorization) {
        return getData(url, authorization);
    }

    public static String postData(String input, @NotNull String payload) {
        return postData(input, payload, null, null);
    }

    public static String postCookie(String input, @NotNull String payload, String cookie) {
        return postData(input, payload, cookie, null);
    }

    public static String postDataAuthorization(String input, @NotNull String payload, String authorization) {
        return postData(input, payload, null, authorization);
    }


    private static String getData(String urlStr, String authorization) {
        try {
            HttpURLConnection conn = (HttpURLConnection) new URL(urlStr).openConnection();
            conn.setRequestProperty("Content-Type", "application/json; charset=UTF-8");
            if (authorization != null)
                conn.setRequestProperty("Authorization", authorization);
            conn.setRequestMethod("GET");
            conn.setUseCaches(false);

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

    public static String postData(String input, @NotNull String payload, String cookie, String authorization) {
        try {
            HttpURLConnection conn = (HttpURLConnection) new URL(input).openConnection();
            conn.setRequestMethod("POST");
            conn.setRequestProperty("Content-Type", "application/json; charset=UTF-8");
            if (authorization != null)
                conn.setRequestProperty("Authorization", authorization);
            if (cookie != null)
                conn.setRequestProperty("Cookie", cookie);
            conn.setUseCaches(false);
            conn.setDoOutput(true);
            //post
            OutputStream payloadOut = conn.getOutputStream();
            payloadOut.write(payload.getBytes(StandardCharsets.UTF_8));
            payloadOut.flush();

            String result;
            if (conn.getResponseCode() > 399) {
                result = readResponse(conn.getErrorStream());
            } else
                result = readResponse(conn.getInputStream());
            conn.disconnect();
            return result;
        } catch (IOException e) {
            return null;
        }
    }

    private static String readResponse(@NotNull InputStream in) {
        try {
            ByteArrayOutputStream out = new ByteArrayOutputStream();
            byte[] buff = new byte[1024];
            int length;
            while ((length = in.read(buff)) > 0) {
                out.write(buff, 0, length);
            }
            in.close();
            return out.toString(StandardCharsets.UTF_8);
        } catch (IOException e) {
            System.err.println("[UrlDataGetter] " + e.fillInStackTrace().getMessage());
            return "";
        }
    }
}
