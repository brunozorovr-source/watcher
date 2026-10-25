package com.watcher;

public class Morse {
    private static final String[] AZ = {".-", "-...", "-.-.", "-..", ".", "..-.", "--.", "....", "..", ".---", "-.-", ".-..", "--",
            "-.", "---", ".--.", "--.-", ".-.", "...", "-", "..-", "...-", ".--", "-..-", "-.--", "--.."};
    private static final String[] NUM = {"-----", ".----", "..---", "...--", "....-", ".....", "-....", "--...", "---..", "----."};

    public static String encode(String s) {
        StringBuilder b = new StringBuilder();
        for (char c : s.toLowerCase().toCharArray()) {
            String code;
            if (c >= 'a' && c <= 'z') code = AZ[c - 'a'];
            else if (c >= '0' && c <= '9') code = NUM[c - '0'];
            else if (c == '_') code = "..--.-";
            else continue;
            if (b.length() > 0) b.append(' ');
            b.append(code);
        }
        return b.toString();
    }
}
