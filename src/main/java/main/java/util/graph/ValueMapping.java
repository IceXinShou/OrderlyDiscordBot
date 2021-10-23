package main.java.util.graph;

public class ValueMapping {
    private double inputMin;
    private double inputMax;
    private final double outputMin;
    private final double outputMax;

    public ValueMapping(double outputMin, double outputMax) {
        this.inputMin = -1;
        this.inputMax = -1;
        this.outputMin = outputMin;
        this.outputMax = outputMax;
    }

    double slope;

    public ValueMapping(double inputMin, double inputMax, double outputMin, double outputMax) {
        this.inputMin = inputMin;
        this.inputMax = inputMax;
        this.outputMin = outputMin;
        this.outputMax = outputMax;
        calculateSlope();
    }

    public void calculateSlope() {
        slope = (outputMax - outputMin) / (inputMax - inputMin);
    }

    public double mapValue(double value) {
        return outputMin + slope * (value - inputMin);
    }

    public void addValue(double value) {
        if (inputMin == -1)
            inputMin = value;
        //最大最小值
        if (value > inputMax)
            inputMax = value;
        else if (value < inputMin)
            inputMin = value;
    }

    public double getInputMin() {
        return inputMin;
    }

    public double getInputMax() {
        return inputMax;
    }
}
