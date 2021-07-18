package main.java.YoutubeDownloader.downloader;

import java.io.*;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class ThreadFileWriter {
    private Map<Integer, ByteArrayOutputStream> outList = new HashMap<>();
    private Map<Integer, Boolean> outDone = new HashMap<>();
    private int threadID = 0;
    private int threadStep = 0;

    FileOutputStream fileOutput;

    public ThreadFileWriter(File file) {
        try {
            fileOutput = new FileOutputStream(file);
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        }
    }

    public int add(ByteArrayOutputStream out) {
        outList.put(threadID, out);
        outDone.put(threadID, false);
        return threadID++;
    }

    public synchronized void threadDone(int threadID) {
        outDone.put(threadID, true);
        writeAll();
    }

    public void writeAll() {
        List<Integer> removeList = new ArrayList<>();
        for (int i = 0; i < outList.size(); i++) {
            if (!outDone.containsKey(threadStep) || !outDone.get(threadStep))
                break;
            try {
                fileOutput.write(outList.get(threadStep).toByteArray());
            } catch (IOException e) {
                e.printStackTrace();
            }
            removeList.add(threadStep);
            threadStep++;
        }

        removeList.forEach(i->{
            outList.remove(i);
            outDone.remove(i);
        });
    }

    public void close() {
        try {
            fileOutput.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
