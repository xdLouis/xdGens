package de.louis.xdGens.skill;

/**
 * Shadow Clone — Hoe skill that surrounds the player with 4 packet-based NPC
 * clones that mirror their movement. While active every harvest reward is
 * multiplied by 4.
 *
 * Duration scaling (seconds):
 *   lv 1   →  6 s  (120 ticks)
 *   lv 1000 → 30 s  (600 ticks)
 *   Curve: linear 6 + 24 * (level / 1000)
 *
 * Cooldown (seconds):
 *   lv 1   → 120 s
 *   lv 1000 →  30 s
 *   Curve: linear 120 - 90 * (level / 1000)
 *
 * Cost curve: mirrors Panda / TNT (EXP_SCALE = 1.007)
 */
public class ShadowCloneSkill implements Skill {

    public static final int    MAX_LEVEL         = 1000;
    private static final double BASE_COST        = 400.0;
    private static final double LINEAR_SCALE     = 0.20;
    private static final double EXP_SCALE        = 1.007;

    private static final double MIN_DURATION_S   = 6.0;
    private static final double MAX_DURATION_S   = 30.0;
    private static final double MIN_COOLDOWN_S   = 30.0;
    private static final double MAX_COOLDOWN_S   = 120.0;

    // ── Skill interface ───────────────────────────────────────────────────────

    @Override public String id()           { return "shadow_clone"; }
    @Override public String displayName()  { return "<gradient:#7f7fd5:#86a8e7>\uD83D\uDC64 Shadow Clone</gradient>"; }
    @Override public String iconMaterial() { return "NETHER_STAR"; }
    @Override public int    maxLevel()     { return MAX_LEVEL; }
    @Override public int    unlockCost()   { return 500; }

    @Override
    public int upgradeCost(int currentLevel) {
        double raw = BASE_COST
                * (1.0 + LINEAR_SCALE * (currentLevel + 1))
                * Math.pow(EXP_SCALE, currentLevel);
        if (Double.isNaN(raw) || raw < 0) return Integer.MAX_VALUE;
        return (int) Math.min(raw, Integer.MAX_VALUE);
    }

    @Override
    public String description(int level) {
        return "<gray>Duration: <white>" + durationSeconds(level) + "s</white>"
             + " <gray>\u00b7 Cooldown: <white>" + cooldownSeconds(level) + "s</white>"
             + " <gray>\u00b7 Harvest: <gold>x4</gold>";
    }

    // ── Helpers ───────────────────────────────────────────────────────────────

    /** Active duration in ticks. */
    public int durationTicks(int level) {
        return (int)(durationSeconds(level) * 20.0);
    }

    /** Active duration in seconds, rounded to one decimal. */
    public String durationSeconds(int level) {
        double s = MIN_DURATION_S + (MAX_DURATION_S - MIN_DURATION_S) * ((double) Math.min(level, MAX_LEVEL) / MAX_LEVEL);
        return String.format("%.1f", s);
    }

    /** Cooldown in seconds (full number). */
    public String cooldownSeconds(int level) {
        double s = MAX_COOLDOWN_S - (MAX_COOLDOWN_S - MIN_COOLDOWN_S) * ((double) Math.min(level, MAX_LEVEL) / MAX_LEVEL);
        return String.format("%.0f", s);
    }

    /** Cooldown in ticks. */
    public int cooldownTicks(int level) {
        double s = MAX_COOLDOWN_S - (MAX_COOLDOWN_S - MIN_COOLDOWN_S) * ((double) Math.min(level, MAX_LEVEL) / MAX_LEVEL);
        return (int)(s * 20.0);
    }
}
