package main.java;

public class Test2 {
    @SuppressWarnings("ALL")
    public static void main(String[] args) {
        for (int i = 1001; i < 7778; i += 2) {
            if (!String.valueOf(i).contains("9"))
                System.out.println(i);
        }
    }
}