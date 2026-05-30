package de.louis.xdGens.util;

import java.math.BigDecimal;
import java.math.MathContext;
import java.text.DecimalFormat;
import java.text.DecimalFormatSymbols;
import java.util.Locale;

public class NumberUtil {

    /*
     * Suffix table — every 3 zeroes from 10^3 to 10^63
     * (covers values up to ~999.99 Vigintillion before falling back to sci-notation)
     *
     *  k   = Thousand      10^3
     *  M   = Million       10^6
     *  B   = Billion       10^9
     *  T   = Trillion      10^12
     *  Q   = Quadrillion   10^15
     *  Qi  = Quintillion   10^18
     *  Sx  = Sextillion    10^21
     *  Sp  = Septillion    10^24
     *  Oc  = Octillion     10^27
     *  No  = Nonillion     10^30
     *  Dc  = Decillion     10^33
     *  Udc = Undecillion   10^36
     *  Ddc = Duodecillion  10^39
     *  Tdc = Tredecillion  10^42
     *  Qdc = Quattuordecillion 10^45
     *  Qic = Quindecillion 10^48
     *  Sxc = Sexdecillion  10^51
     *  Spc = Septendecillion 10^54
     *  Ocd = Octodecillion 10^57
     *  Nod = Novemdecillion 10^60
     *  Vg  = Vigintillion  10^63
     */
    private static final String[] SUFFIXES = {
        "",
        "k",   // 10^3
        "M",   // 10^6
        "B",   // 10^9
        "T",   // 10^12
        "Q",   // 10^15
        "Qi",  // 10^18
        "Sx",  // 10^21
        "Sp",  // 10^24
        "Oc",  // 10^27
        "No",  // 10^30
        "Dc",  // 10^33
        "Udc", // 10^36
        "Ddc", // 10^39
        "Tdc", // 10^42
        "Qdc", // 10^45
        "Qic", // 10^48
        "Sxc", // 10^51
        "Spc", // 10^54
        "Ocd", // 10^57
        "Nod", // 10^60
        "Vg",  // 10^63
    };

    private static final BigDecimal THOUSAND = BigDecimal.valueOf(1000);
    private static final DecimalFormat FMT = new DecimalFormat("0.##",
            DecimalFormatSymbols.getInstance(Locale.US));

    // ── public API ────────────────────────────────────────────────────────────

    public static String format(long value) {
        if (value < 0) return "-" + format(-value);
        if (value < 1000) return String.valueOf(value);
        return formatBig(BigDecimal.valueOf(value));
    }

    public static String format(double value) {
        if (value < 0) return "-" + format(-value);
        if (value < 1000) {
            if (value == (long) value) return String.valueOf((long) value);
            return FMT.format(value);
        }
        return formatBig(BigDecimal.valueOf(value));
    }

    public static String format(int value) {
        return format((long) value);
    }

    public static String format(BigDecimal value) {
        if (value.signum() < 0) return "-" + format(value.negate());
        if (value.compareTo(THOUSAND) < 0) {
            double d = value.doubleValue();
            if (d == (long) d) return String.valueOf((long) d);
            return FMT.format(d);
        }
        return formatBig(value);
    }

    // ── internal ──────────────────────────────────────────────────────────────

    private static String formatBig(BigDecimal v) {
        int index = 0;
        while (v.compareTo(THOUSAND) >= 0 && index < SUFFIXES.length - 1) {
            v = v.divide(THOUSAND, MathContext.DECIMAL64);
            index++;
        }
        // if we ran out of suffixes fall back to scientific notation
        if (index >= SUFFIXES.length) {
            return FMT.format(v.doubleValue()) + "e+" + (index * 3);
        }
        return FMT.format(v.doubleValue()) + SUFFIXES[index];
    }
}
