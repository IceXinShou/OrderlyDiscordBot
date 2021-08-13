import org.jetbrains.annotations.NotNull;

public class Test {
    Test() {
        String input = "人數: ${2*((1+2}";
        int startIndex = input.indexOf("${");

        System.out.println(calculate(input, startIndex + 2));
        System.out.println(error == null ? "" : error);
    }

    String error = null;
    int newPos = 0;

    private float calculate(@NotNull String input, int startIndex) {
        float sum = 0;
        char symbol = '+';
        int valueStart = -1, valueEnd = -1;
        boolean hasCache = false;
        float valueCache = 0;
        for (int i = startIndex; i < input.length(); i++) {
            char thisChar = input.charAt(i);
            if (thisChar <= ' ')
                continue;

            if (thisChar == '(') {
                valueCache = calculate(input, i + 1);
                valueStart = 0;
                hasCache = true;
                i = newPos;
                continue;
            }
            if (isSymbol(thisChar) || thisChar == ')' || thisChar == '}') {
                if (valueStart != -1) {
                    float value = hasCache ? valueCache : Float.parseFloat(input.substring(valueStart, valueEnd + 1));
                    if (thisChar == '*' || thisChar == '/' || thisChar == '%') {
                        value = doFirst(value, input, i);
                        i = newPos;
                    }

                    if (symbol == '-')
                        sum -= value;
                    else if (symbol == '+')
                        sum += value;

                    valueStart = -1;
                    hasCache = false;
                    thisChar = input.charAt(i);
                }
                symbol = thisChar;
                if (thisChar == ')' || thisChar == '}') {
                    newPos = i;
                    return sum;
                }
            }

            if (valueStart == -1 && isDigit(thisChar)) {
                valueStart = i;
                valueEnd = i;
                continue;
            }

            if (valueStart != -1)
                valueEnd++;
        }
        if (error == null)
            error = "`(` should end with `)`";
        return 0;
    }

    private float doFirst(float outputValue, @NotNull String input, int startIndex) {
        char symbol = '\0';
        int valueStart = -1, valueEnd = -1;
        boolean hasCache = false;
        float valueCache = 0;
        for (int i = startIndex; i < input.length(); i++) {
            char thisChar = input.charAt(i);
            if (thisChar <= ' ')
                continue;

            if (thisChar == '(') {
                valueCache = calculate(input, i + 1);
                valueStart = 0;
                hasCache = true;
                i = newPos;
                continue;
            }

            if (isSymbol(thisChar) || thisChar == ')' || thisChar == '}') {
                if (valueStart != -1 && symbol != '\0') {
                    float value = hasCache ? valueCache : Float.parseFloat(input.substring(valueStart, valueEnd + 1));
                    if (symbol == '*')
                        outputValue *= value;
                    else if (symbol == '/')
                        outputValue /= value;
                    else
                        outputValue %= value;
                    valueStart = -1;
                }
                symbol = thisChar;
            }
            if (thisChar == ')' || thisChar == '}' || thisChar == '+' || thisChar == '-') {
                newPos = i;
                return outputValue;
            }

            if (valueStart == -1 && isDigit(thisChar)) {
                valueStart = i;
                valueEnd = i;
                continue;
            }

            if (valueStart != -1)
                valueEnd++;
        }
        return outputValue;
    }

    private boolean isSymbol(char input) {
        return input == '+' || input == '-' || input == '*' || input == '/' || input == '%';
    }

    private boolean isDigit(char input) {
        return input >= '0' && input <= '9';
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