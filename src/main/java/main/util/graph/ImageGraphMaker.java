package main.util.graph;

import javax.imageio.ImageIO;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.text.DecimalFormat;
import java.util.List;

public class ImageGraphMaker {
    private final BufferedImage image;
    private final double valueEnd;
    private final double valueStart;

    public ImageGraphMaker(int imageWidth, int imageHeight, List<Long> timeList, List<Double> valueList) {
        ValueMapping valueMap = new ValueMapping(20, imageHeight - 30);
        ValueMapping timeMap = new ValueMapping(0, imageWidth);
        for (int i = 0; i < valueList.size(); i++) {
            valueMap.addValue(valueList.get(i));
            timeMap.addValue(timeList.get(i));
        }
        valueMap.calculateSlope();
        timeMap.calculateSlope();

        valueStart = valueList.get(0);
        valueEnd = valueList.get(valueList.size() - 1);
        image = new BufferedImage(imageWidth, imageHeight, BufferedImage.TYPE_INT_ARGB);
        Graphics2D canvas = image.createGraphics();
        //圖表
        final Color col;
        if (valueStart <= valueEnd)
            col = new Color(58, 172, 89);
        else
            col = new Color(172, 58, 58);
        canvas.setStroke(new BasicStroke(3));
        int[] polygonX = new int[valueList.size() + 2];
        int[] polygonY = new int[valueList.size() + 2];
        int lastX = 0, lastY = 0;
        int i = 0;
        for (; i < valueList.size(); i++) {
            long time = timeList.get(i);
            double value = valueList.get(i);
            int output = (int) valueMap.mapValue(value);

            int y = imageHeight - output - 1;
            int x = (int) timeMap.mapValue(time);
            if (i > 0) {
                canvas.setColor(col);
                canvas.drawLine(lastX, lastY, x, y);
            }
            polygonX[i] = x;
            polygonY[i] = y;

            lastX = x;
            lastY = y;
        }
        polygonX[i] = imageWidth - 1;
        polygonY[i] = imageHeight - 1;
        i++;
        polygonX[i] = 0;
        polygonY[i] = imageHeight - 1;
        //圖標
        int fontSize = 28;
        int minValueInt = (int) valueMap.getInputMin();
        double eachAdd = 0.1f;
        while (valueMap.mapValue(minValueInt + eachAdd) - valueMap.mapValue(minValueInt) < fontSize * 3f) {
            double value = cut(eachAdd + 0.2f);
//            System.out.println(value);
            if (value > 100000)
                eachAdd += 100000;
            else if (value > 10000)
                eachAdd += 10000;
            else if (value > 1000)
                eachAdd += 1000;
            else if (value > 100)
                eachAdd += 100;
            else if (value > 10)
                eachAdd += 2;
            else if (value >= 5)
                eachAdd = 10;
            else if (value > 2)
                eachAdd = 5;
            else if (value > 1)
                eachAdd += 1;
            else if (value > 0.5)
                eachAdd = 1;
            else if (value > 0.2f)
                eachAdd = 0.5f;
            else
                eachAdd += 0.1f;
        }
        double startValue = cut((valueMap.getInputMin() - (valueMap.getInputMin() % eachAdd) + eachAdd));
        int lineCount = (int) ((valueMap.getInputMax() - valueMap.getInputMin()) / eachAdd + 0.5);
        DecimalFormat formatter = new DecimalFormat("#,###");


        /*
          開始畫圖
         */
        canvas.setColor(Color.decode("#9EA0AF"));
        canvas.setStroke(new BasicStroke(1));
        for (i = 0; i < lineCount; i++) {
            double value = cut(startValue + i * eachAdd);
            int y = imageHeight - (int) valueMap.mapValue(value) - 1;
            canvas.drawLine(0, y, imageWidth - 1, y);
        }

        //畫漸層
        final Color color1 = new Color(col.getRed(), col.getGreen(), col.getBlue(), 0);
        final Color color2 = new Color(col.getRed(), col.getGreen(), col.getBlue(), 200);
        GradientPaint gradientPaint = new GradientPaint(
                0, imageHeight - 1, color1,
                0, 0, color2
        );
        canvas.setPaint(gradientPaint);
        canvas.fillPolygon(polygonX,
                polygonY,
                polygonX.length);

        canvas.setColor(Color.decode("#FFDD00"));
        canvas.setFont(new Font("微軟正黑體", Font.PLAIN, fontSize));
        for (i = 0; i < lineCount; i++) {
            Double value = cut(startValue + i * eachAdd);
            int y = imageHeight - (int) valueMap.mapValue(value) - 1;
            String str = formatter.format(value);
            canvas.drawString(str, 2, y + fontSize / 2 - 9);
        }
    }

    public double getPercent() {
        return ((valueEnd / valueStart) - 1) * 100;
    }

    public double endValue() {
        return valueEnd;
    }

    public byte[] getImageBytes() {
        return toByteArray(image);
    }

    private double cut(double in) {
        return (long) (in * 10) / 10f;
    }

    private byte[] toByteArray(BufferedImage bi) {
        ByteArrayOutputStream out = new ByteArrayOutputStream();
        try {
            ImageIO.write(bi, "png", out);
        } catch (IOException e) {
            return null;
        }
        return out.toByteArray();
    }
}
