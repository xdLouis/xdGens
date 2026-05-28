package de.louis.xdGens.util;

import java.text.DecimalFormat;
import java.text.DecimalFormatSymbols;
import java.util.Locale;

public class NumberUtil {

    private static final String[] SUFFIXES = {"", "k", "m", "b", "t", "q"};
    private static final DecimalFormat FORMAT = new DecimalFormat("0.##", DecimalFormatSymbols.getInstance(Locale.US));

    public static String format(double value) {
        if (value < 1000) {
            if (value == (long) value) {
                return String.valueOf((long) value);
            }
            return FORMAT.format(value);
        }

        int index = 0;
        while (value >= 1000 && index < SUFFIXES.length - 1) {
            value /= 1000.0;
            index++;
        }

        return FORMAT.format(value) + SUFFIXES[index];
    }

    public static String format(long value) {
        return format((double) value);
    }

    public static String format(int value) {
        return format((double) value);
    }
}