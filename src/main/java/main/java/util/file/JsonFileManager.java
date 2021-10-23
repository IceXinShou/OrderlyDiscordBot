package main.java.util.file;

import org.json.JSONObject;

import java.io.*;
import java.nio.charset.StandardCharsets;

public class JsonFileManager {
    private final String TAG = "[FileManager]";
    private final String filepath;
    public final JSONObject data;

    public JsonFileManager(String filepath) {
        this.filepath = filepath;
        String stringData = readFile();
        if (stringData == null)
            data = new JSONObject();
        else
            data = new JSONObject(stringData);
    }

    public void saveFile() {
        try {
            FileOutputStream fileWriter = new FileOutputStream(filepath);
            ByteArrayInputStream in = new ByteArrayInputStream(data.toString().getBytes(StandardCharsets.UTF_8));
            byte[] buff = new byte[1024];
            int length;
            while ((length = in.read(buff)) > 0) {
                fileWriter.write(buff, 0, length);
            }
            in.close();
            fileWriter.close();
        } catch (IOException e) {
            System.err.println(TAG + " can not save file");
        }
    }

    private String readFile() {
        File file = new File(filepath);
        if (!file.exists())
            return null;

        ByteArrayOutputStream out = new ByteArrayOutputStream();
        try {
            InputStream in = new FileInputStream(file);
            byte[] buff = new byte[1024];
            int length;
            while ((length = in.read(buff)) > 0) {
                out.write(buff, 0, length);
            }
            in.close();
            out.close();
            return out.toString(StandardCharsets.UTF_8);
        } catch (IOException e) {
            e.printStackTrace();
            return null;
        }
    }

    public void removeKey(String key) {
        data.remove(key);
        saveFile();
    }
}
