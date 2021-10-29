package main.util;

public class Base62Manager {
    private static final String characters = "0123456789abcdefghijklmnopqrstuvwxyzABCDEFGHIJKLMNOPQRSTUVWXYZ";

    public static String encode(long seq) {
        if (seq < 0) throw new IllegalArgumentException("b10 must be nonnegative");
        StringBuilder sBuilder = new StringBuilder();
        while (seq > 0) {
            sBuilder.append(characters.charAt((int) (seq % 62)));
            seq /= 62;
        }
        return sBuilder.toString();
    }

    public static long base62decode(String b62) {
        long[] ret = new long[]{0, 1};

        b62.chars().forEach(character -> {
            if (characters.indexOf(character) == -1) // 不合理的字
                throw new IllegalArgumentException("Invalid character(s) in string: " + character);

            ret[0] += characters.indexOf(character) * ret[1];
            ret[1] = ret[1] * 62;
        });
        return ret[0];
    }
}
