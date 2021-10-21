package main.java.util;

//
//import com.madgag.gif.fmsware.AnimatedGifEncoder;
//
//import javax.imageio.ImageIO;
//import java.awt.*;
//import java.awt.image.BufferedImage;
//import java.io.ByteArrayInputStream;
//import java.io.ByteArrayOutputStream;
//import java.io.IOException;
//import java.util.Base64;
//
public class GifGenerator {
//    public static byte[] gifGenerator(String[] base64Images) {
//        AnimatedGifEncoder encoder = new AnimatedGifEncoder();
//        encoder.setRepeat(0);
//        encoder.setFrameRate(30);
//        encoder.setBackground(new Color(54, 57, 63));
//        encoder.setTransparent(new Color(54, 57, 63));
//        ByteArrayOutputStream out = new ByteArrayOutputStream();
//        encoder.start(out);
//
//        try {
//            for (String base64Image : base64Images) {
//                BufferedImage image = ImageIO.read(new ByteArrayInputStream(Base64.getDecoder().decode(base64Image)));
//                encoder.addFrame(image);
//            }
//        } catch (IOException e) {
//            System.err.println(e.getMessage());
//        }
//        encoder.finish();
//        return out.toByteArray();
//    }
}
