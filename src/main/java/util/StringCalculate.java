package main.java.util;

public class StringCalculate {
    String error;
    int newPos;

    public String processes(String input, String format) {
        StringBuilder result = new StringBuilder();
        int lastEndIndex = 0;
        int startIndex;
        while ((startIndex = input.indexOf("${", newPos)) != -1) {
            double value = 0;
            try {
                value = calculate(input, startIndex + 2, false);
            } catch (Exception e) {
                if (error == null)
                    error = "input wrong";
            }
            if (input.charAt(newPos) != '}' && error == null)
                error = "calculation should end with `}`";
            if (haveError())
                break;

            result.append(input, lastEndIndex, startIndex)
                    .append(String.format(format, value));
            lastEndIndex = newPos + 1;
        }
        if(lastEndIndex == 0)
            return input;
        result.append(input, lastEndIndex, input.length());
        if (haveError())
            return input;
        return result.toString();
    }

    public boolean haveError() {
        return error != null;
    }

    public String getError() {
        return error;
    }

    private double calculate(String input, int startIndex, boolean isBrackets) {
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

    private double doFirst(double outputValue, String input, int startIndex) {
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
}
