package main.java.YoutubeDownloader.downloader;

import main.java.YoutubeDownloader.VideoObject;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.Executors;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;

public class VideoDownloader extends Thread {

    private String url;
    private File file;
    private int fileSize;

    public VideoDownloader(VideoObject videoObject, File file) {
        this.url = videoObject.getUrl();
        this.fileSize = videoObject.getContentLength();
        this.file = file;
    }

    private CountDownLatch count = new CountDownLatch(1);

    public CountDownLatch startDownload() {
        new Thread(this).start();
        return count;
    }

    @Override
    public void run() {
        ThreadFileWriter fileWriter = new ThreadFileWriter(file);
        //download
        ThreadPoolExecutor executor = (ThreadPoolExecutor) Executors.newFixedThreadPool(30);
        //buff
        int buffSize = 1024 * 1024 * 10;
        //now byte pos
        int nowPos = 0;

        AtomicInteger downloadByte = new AtomicInteger();
        long startTime = System.currentTimeMillis();
        do {
            String rangeHeader = "bytes=" + nowPos + "-" + (nowPos + buffSize - 1);
            nowPos += buffSize;

            ByteArrayOutputStream out = new ByteArrayOutputStream();
            int threadID = fileWriter.add(out);
            executor.execute(() -> {
                try {
                    HttpURLConnection connection = (HttpURLConnection) new URL(url).openConnection();
                    connection.setRequestProperty("Range", rangeHeader);
                    InputStream in = connection.getInputStream();
                    //下載檔案
                    byte[] buff = new byte[1024 * 10];
                    int length;
                    while ((length = in.read(buff)) > 0) {
                        out.write(buff, 0, length);
//                        System.out.print("\r" + ((float) downloadByte.addAndGet(length) / fileSize) * 100);
                        float nowLengthMB = (float) (downloadByte.addAndGet(length) / 1000) / 1000;
                        System.out.print("\rdone: " + executor.getCompletedTaskCount() +
                                ", inThread: " + executor.getActiveCount() +
                                ", Speed: " + String.format("%.1fMB/s",nowLengthMB / ((System.currentTimeMillis() - startTime) / 1000))
                        );
                    }
                    fileWriter.threadDone(threadID);
                } catch (IOException e) {
                    e.printStackTrace();
                }
            });

            //wait some time
            try {
                Thread.sleep(100);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        } while (fileSize - nowPos > 0);
        //wait download
        try {
            executor.shutdown();
            executor.awaitTermination(Long.MAX_VALUE, TimeUnit.MILLISECONDS);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
        fileWriter.writeAll();
        fileWriter.close();
        count.countDown();
    }
}
