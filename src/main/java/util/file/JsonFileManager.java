package main.java.util.file;

import org.json.JSONObject;

import java.io.*;
import java.nio.charset.StandardCharsets;

public class JsonFileManager {
    private final String TAG = "[FileManager]";
    private final String filepath;
    public JSONObject data;

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
            fileWriter.write(data.toString().getBytes(StandardCharsets.UTF_8));
            fileWriter.close();
        } catch (IOException e) {
            System.err.println(TAG + " can not save file");
        }
    }

    private String readFile() {
        File file = new File(filepath);
        if (!file.exists())
            return null;

        StringBuilder builder = new StringBuilder();
        try {
            InputStreamReader fileReader = new InputStreamReader(new FileInputStream(file), StandardCharsets.UTF_8);
            char[] buff = new char[4096];
            int length;
            while ((length = fileReader.read(buff)) > 0)
                builder.append(buff, 0, length);
            fileReader.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
        return builder.toString();
    }

    public void removeKey(String key) {
        data.remove(key);
        saveFile();
    }
}
