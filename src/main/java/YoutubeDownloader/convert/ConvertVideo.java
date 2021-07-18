package main.java.YoutubeDownloader.convert;

import com.coremedia.iso.boxes.Container;
import com.googlecode.mp4parser.FileDataSourceImpl;
import com.googlecode.mp4parser.authoring.Movie;
import com.googlecode.mp4parser.authoring.builder.DefaultMp4Builder;
import com.googlecode.mp4parser.authoring.container.mp4.MovieCreator;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.channels.FileChannel;

public class ConvertVideo extends File {

    public ConvertVideo(String fileName, File videoFile, File audioFile, String outputDir) {
        super(outputDir + "/" + fileName);
        Movie video;
        Movie audio;

        try {
            video = MovieCreator.build(new FileDataSourceImpl(videoFile.getPath()));
            audio = MovieCreator.build(new FileDataSourceImpl(audioFile.getPath()));

        } catch (RuntimeException | IOException e) {
            e.printStackTrace();
            return;
        }

        Movie finalVideo = new Movie();
        finalVideo.addTrack(video.getTracks().get(0));
        finalVideo.addTrack(audio.getTracks().get(0));
        Container out = new DefaultMp4Builder().build(finalVideo);

        FileChannel fos;
        try {
//            fos = new FileOutputStream(outputDir + "/" + fileName);
            fos = new FileOutputStream(outputDir + "/" + fileName).getChannel();
        } catch (FileNotFoundException e) {
            e.printStackTrace();
            return;
        }

//        BufferedWritableFileByteChannel byteBufferByteChannel = new BufferedWritableFileByteChannel(fos);
        try {
            out.writeContainer(fos);
//            byteBufferByteChannel.close();
            fos.close();
            videoFile.delete();
            audioFile.delete();
        } catch (IOException e) {
            e.printStackTrace();
        }
//        return new File(outputDir + "/" + videoFile.getName() + ".mp4");
    }
}
