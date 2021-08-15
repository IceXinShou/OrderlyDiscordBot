import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.List;

public class Test {
    Test() {
        /*
        String input = "人數: ${2*((1+2}";
        int startIndex = input.indexOf("${");

        System.out.println(calculate(input, startIndex + 2));
        System.out.println(error == null ? "" : error);
        */


        List<Byte> test = new ArrayList<>();
        test.add((byte) 4);
        test.add((byte) 3);
        test.add((byte) 2);
        test.add((byte) 1);
        test.add((byte) 0);
        System.out.println(test.contains((byte) 4));
        System.out.println(test);

//        String input = "人數: ${100 - 1}_${7/10*100}";
//        StringCalculate calculate = new StringCalculate();
//        System.out.println(calculate.processes(input,"%.2f"));
//        System.out.println(calculate.getError());
    }

    String error;
    int newPos;

    public String processes(String input, int format) {
        StringBuilder result = new StringBuilder();
        int lastEndIndex = 0;
        int startIndex;
        while ((startIndex = input.indexOf("${", newPos)) != -1) {
            double value = 0;
            try {
                value = calculate(input, startIndex + 2, false);
            } catch (Exception e) {
                if (error != null)
                    error = "input wrong";
            }
            if (input.charAt(newPos) != '}' && error == null)
                error = "calculation should end with `}`";
            if (error != null)
                break;

            result.append(input.substring(lastEndIndex, startIndex))
                    .append(String.format("%." + format + "f", value));
            lastEndIndex = newPos + 1;
        }
        if (error != null)
            return input;
        return result.toString();
    }

    private double calculate(@NotNull String input, int startIndex, boolean isBrackets) {
        float sum = 0;
        char symbol = '+';
        int valueStart = -1, valueEnd = -1;
        boolean inValue = false;
        boolean hasCache = false;
        double valueCache = 0;
        for (int i = startIndex; i < input.length(); i++) {
            char thisChar = input.charAt(i);
            if (thisChar <= ' ')
                continue;

            if (thisChar == '(' && !inValue) {
                valueCache = calculate(input, i + 1, true);
                valueStart = 0;
                hasCache = true;
                i = newPos;
            }
            if (isSymbol(thisChar) || thisChar == ')' || thisChar == '}') {
                if (valueStart != -1) {
                    double value;
                    if (hasCache) {
                        value = valueCache;
                    } else if (valueStart <= valueEnd)
                        value = Double.parseDouble(input.substring(valueStart, valueEnd + 1));
                    else {
                        if (error == null)
                            error = "value wrong";
                        value = 0;
                    }

                    if (thisChar == '*' || thisChar == '/' || thisChar == '%') {
                        value = doFirst(value, input, i);
                        i = newPos;
                    }

                    if (symbol == '-')
                        sum -= value;
                    else if (symbol == '+')
                        sum += value;

                    valueStart = -1;
                    inValue = false;
                    hasCache = false;
                    thisChar = input.charAt(i);
                }
                symbol = thisChar;
            }
            if (thisChar == ')' || thisChar == '}') {
                if (thisChar == '}' && isBrackets && error == null)
                    error = "`(` should end with `)`";
                newPos = i;
                return sum;
            }
            // 數字開頭
            if (valueStart == -1 && isDigit(thisChar)) {
                valueStart = i;
                valueEnd = i;
                inValue = true;
                continue;
            }
            // 數字結束
            if (inValue && isDigit(thisChar))
                valueEnd++;
            else
                inValue = false;
        }
        if (error == null)
            error = "calculation should end with `}`";
        return 0;
    }

    private double doFirst(double outputValue, @NotNull String input, int startIndex) {
        char symbol = '*';
        int valueStart = -1, valueEnd = -1;
        boolean inValue = false;
        boolean hasCache = false;
        double valueCache = 0;
        for (int i = startIndex; i < input.length(); i++) {
            char thisChar = input.charAt(i);
            if (thisChar <= ' ')
                continue;

            if (thisChar == '(') {
                valueCache = calculate(input, i + 1, true);
                valueStart = 0;
                hasCache = true;
                i = newPos;
            }

            if (isSymbol(thisChar) || thisChar == ')' || thisChar == '}') {
                if (valueStart != -1) {
                    double value;
                    if (hasCache)
                        value = valueCache;
                    else if (valueStart <= valueEnd)
                        value = Double.parseDouble(input.substring(valueStart, valueEnd + 1));
                    else {
                        if (error == null)
                            error = "value wrong";
                        value = 0;
                    }

                    if (symbol == '*')
                        outputValue *= value;
                    else if (symbol == '/')
                        if (value == 0) {
                            if (error == null)
                                error = "value can not divide by zero";
                        } else
                            outputValue /= value;
                    else
                        outputValue %= value;
                    valueStart = -1;
                    inValue = false;
                    hasCache = false;
                }
                symbol = thisChar;
            }
            if (thisChar == ')' || thisChar == '}' || thisChar == '+' || thisChar == '-') {
                newPos = i;
                return outputValue;
            }
            // 數字開頭
            if (valueStart == -1 && isDigit(thisChar)) {
                valueStart = i;
                valueEnd = i;
                inValue = true;
                continue;
            }
            // 數字結束
            if (inValue && isDigit(thisChar))
                valueEnd++;
            else
                inValue = false;
        }
        if (error == null)
            error = "calculation should end with `}`";
        return outputValue;
    }

    private boolean isSymbol(char input) {
        return input == '+' || input == '-' || input == '*' || input == '/' || input == '%';
    }

    private boolean isDigit(char input) {
        return input >= '0' && input <= '9' || input == '.' || input == 'e' || input == 'E';
    }


    /**
     * ┃　　範例
     * ┃　　　　1.
     * ┃　　　　線上人數: `${%online_member% + %afk_member% + %working_member%}`
     * ┃　　　　或
     * ┃　　　　線上人數: `${%member% - %offline_member%}`
     * ┃
     * ┃　　　　2.
     * ┃　　　　語音人數比: `${%in_voicechannel%/%online_member%*100}`
     * ┃
     * ┃　　　　3.
     * ┃　　　　直播除以視訊的餘數: `${%stream%%%camera%}`
     */
    public static void main(String[] args) {
        new Test();
    }
}