package de.louis.xdGens.skill;

/**
 * Shadow Clone — active skill triggered by right-clicking the xdGens hoe.
 *
 * Duration:  Lv 1 = 6 s  →  Lv 1000 = 30 s  (linear interpolation)
 * Cooldown:  Lv 1 = 120 s → Lv 1000 = 30 s  (linear interpolation)
 * Effect:    4 packet-only NPC clones mirror the player. All tokens + XP × 4.
 *
 * Requires Prestige 5 to unlock.
 */
public class ShadowCloneSkill implements Skill {

    public static final int MAX_LEVEL            = 1000;
    public static final int REQUIRED_PRESTIGE    = 5;

    private static final double BASE_COST    = 500.0;
    private static final double LINEAR_SCALE = 0.20;
    private static final double EXP_SCALE    = 1.007;

    // Duration: 6 s at lv 1, 30 s at lv 1000
    private static final int MIN_DURATION_TICKS = 6  * 20;  // 120 ticks
    private static final int MAX_DURATION_TICKS = 30 * 20;  // 600 ticks

    // Cooldown: 120 s at lv 1, 30 s at lv 1000
    private static final long MIN_COOLDOWN_MS = 30  * 1000L;
    private static final long MAX_COOLDOWN_MS = 120 * 1000L;

    @Override public String id()           { return "shadow_clone"; }
    @Override public String displayName()  { return "<gradient:#9d50bb:#6e48aa>\uD83D\uDC64 Shadow Clone</gradient>"; }
    @Override public String iconMaterial() { return "DARK_OAK_SAPLING"; }
    @Override public int    maxLevel()     { return MAX_LEVEL; }
    @Override public int    unlockCost()   { return 500; }

    @Override
    public int upgradeCost(int currentLevel) {
        return (int) Math.round(BASE_COST
                * (1.0 + LINEAR_SCALE * (currentLevel + 1))
                * Math.pow(EXP_SCALE, currentLevel));
    }

    @Override
    public String description(int level) {
        return "<gray>Duration: <white>" + (getDurationTicks(level) / 20) + "s</white>"
             + " <gray>\u00b7 Cooldown: <white>" + (getCooldownMs(level) / 1000) + "s</white>"
             + " <gray>\u00b7 Bonus: <white>\u00d74 all</white>";
    }

    /** Duration in ticks for the given level. */
    public int getDurationTicks(int level) {
        if (level <= 0) return 0;
        double progress = (double) Math.min(level, MAX_LEVEL) / MAX_LEVEL;
        return (int) Math.round(MIN_DURATION_TICKS + (MAX_DURATION_TICKS - MIN_DURATION_TICKS) * progress);
    }

    /** Cooldown in milliseconds for the given level. */
    public long getCooldownMs(int level) {
        if (level <= 0) return MAX_COOLDOWN_MS;
        double progress = (double) Math.min(level, MAX_LEVEL) / MAX_LEVEL;
        return Math.round(MAX_COOLDOWN_MS - (MAX_COOLDOWN_MS - MIN_COOLDOWN_MS) * progress);
    }
}
