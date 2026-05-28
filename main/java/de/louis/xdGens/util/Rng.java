package de.louis.xdGens.util;

import java.util.Random;

/**
 * Einfache Hilfsklasse für randomisierte Reward-Berechnung.
 */
public class Rng {

    private static final Random RNG = new Random();

    /**
     * Gibt einen zufälligen double-Wert zwischen min (inkl.) und max (inkl.) zurück.
     */
    public static double between(double min, double max) {
        return min + (max - min) * RNG.nextDouble();
    }

    /**
     * Gibt einen zufälligen int-Wert zwischen min (inkl.) und max (inkl.) zurück.
     */
    public static int between(int min, int max) {
        if (min == max) return min;
        return min + RNG.nextInt(max - min + 1);
    }

    /**
     * Gibt true zurück mit einer Wahrscheinlichkeit von 0.0–1.0.
     */
    public static boolean chance(double probability) {
        return RNG.nextDouble() < probability;
    }
}
