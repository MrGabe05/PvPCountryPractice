package com.gabrielhd.practice.utils.others;

import org.bukkit.ChatColor;

import java.util.Arrays;
import java.util.List;
import java.util.regex.Pattern;

public final class StringUtil {

    public static final String SPLIT_PATTERN;
    private static final String MAX_LENGTH = "11111111111111111111111111111111111111111111111111111";
    private static final List<String> VOWELS;
    
    static {
        SPLIT_PATTERN = Pattern.compile("\\s").pattern();
        VOWELS = Arrays.asList("a", "e", "u", "i", "o");
    }
    
    private StringUtil() {
        throw new RuntimeException("Cannot instantiate a utility class.");
    }
    
    public static String toNiceString(String string) {
        string = ChatColor.stripColor(string).replace('_', ' ').toLowerCase();

        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < string.toCharArray().length; ++i) {
            char c = string.toCharArray()[i];
            boolean b = !Character.isDigit(string.toCharArray()[i + 1]);
            if (i > 0) {
                final char prev = string.toCharArray()[i - 1];
                if ((prev == ' ' || prev == '[' || prev == '(') && (i == string.toCharArray().length - 1 || c != 'x' || b)) {
                    c = Character.toUpperCase(c);
                }
            }
            else if (c != 'x' || b) {
                c = Character.toUpperCase(c);
            }
            sb.append(c);
        }
        return sb.toString();
    }
    
    public static String buildMessage(final String[] args, final int start) {
        if (start >= args.length) {
            return "";
        }

        return ChatColor.stripColor(String.join(" ", Arrays.copyOfRange(args, start, args.length)));
    }
    
    public static String getFirstSplit(final String s) {
        return s.split(StringUtil.SPLIT_PATTERN)[0];
    }
    
    public static String getAOrAn(final String input) {
        return StringUtil.VOWELS.contains(input.substring(0, 1).toLowerCase()) ? "an" : "a";
    }
}
