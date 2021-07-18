package main.java.YoutubeDownloader;

import main.java.YoutubeDownloader.convert.ConvertVideo;
import main.java.YoutubeDownloader.downloader.VideoDownloader;
import main.java.YoutubeDownloader.util.Container;
import main.java.YoutubeDownloader.util.ITag;
import main.java.YoutubeDownloader.util.VideoQuality;

import java.io.ByteArrayInputStream;
import java.io.File;
import java.util.Map;
import java.util.Scanner;
import java.util.concurrent.CountDownLatch;

public class Main {
    Main() {
        System.setIn(new ByteArrayInputStream("https://www.youtube.com/watch?v=-Z5Xy9WYAh8\n720p60".getBytes()));
        Scanner userInput = new Scanner(System.in);
        GetVideoInfo videoInfo = new GetVideoInfo(userInput.nextLine());


        //get video
        Map<ITag, VideoObject> videoData = videoInfo.getVideoData();
//        System.out.println(videoData.keySet().toString());
        videoData.forEach((key, value) -> {
            if (value.getQualityLabel() != null) {
                System.out.print("Video: " + value.getQualityLabel());
            } else {
                System.out.print("Audio:" + value.getBitrate());
            }
            System.out.print(",");
        });
        System.out.println();


        String[] input = userInput.nextLine().split("p");
        VideoQuality videoQuality = VideoQuality.getQuality(input[0]);
        VideoObject videoObject;
        if (input.length > 1)
            videoObject = videoInfo.getVideoByQualityFpsType(videoQuality, Integer.parseInt(input[1]), Container.Mp4);
        else
            videoObject = videoInfo.getVideoByQuality(videoQuality).get(0);

        String videoUrl = videoObject.getUrl();
        System.out.println(videoUrl);
        System.out.println(videoObject.getContentLength());


        String outputDir = "E:\\Plugin\\DiscordBot";
        File tempVideo = new File(outputDir, "download.mp4");
        File tempAudio = new File(outputDir, "audio.m4a");

//        VideoDownloader videoDownloader = new VideoDownloader(videoObject, tempVideo);
//        VideoDownloader audioDownloader = new VideoDownloader(videoInfo.getBestAudio(), tempAudio);
//        CountDownLatch videoTask = videoDownloader.startDownload();
//        CountDownLatch audioTask = audioDownloader.startDownload();
//
//        try {
//            audioTask.await();
//            videoTask.await();
//        } catch (InterruptedException e) {
//            e.printStackTrace();
//        }

        String title = videoInfo.getTitle();
        title = title.replaceAll("[/:*?\"><|]", "_");

        new ConvertVideo(title+ ".mp4", tempVideo, tempAudio, outputDir);
        System.out.println("done");
    }


    public static void main(String[] args) {
        new Main();
    }
}
