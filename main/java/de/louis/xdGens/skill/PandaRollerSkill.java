package de.louis.xdGens.skill;

/**
 * Stat calculations for the Panda Roller upgrade.
 *
 * Spawn chance:  At lv 1 = 0.01% (1 in 10 000).
 *               At lv 1000 = 1.25% (1 in 80).
 *               Curve: base + (max-base) * (level/1000)^0.7
 *
 * Reward bonus:  At lv 1  = +0.5%
 *               At lv 1000 = +50%
 *               Linear: 0.0005 * level
 */
public class PandaRollerSkill extends Skill {

    private static final double SPAWN_BASE    = 0.0001;   // 0.01% at lv 1
    private static final double SPAWN_MAX     = 0.0125;   // 1.25% at lv 1000
    private static final double SPAWN_EXP     = 0.70;
    private static final int    MAX_LEVEL     = 1000;
    private static final double BONUS_PER_LVL = 0.0005;   // 0.05% per level → +50% at lv 1000

    public PandaRollerSkill() {
        super("panda_roller", "Panda Roller");
    }

    /**
     * Returns the spawn-chance (0.0–1.0) for the given level.
     * Level must be >= 1.
     */
    public double spawnChance(int level) {
        if (level <= 0) return 0.0;
        double progress = (double) Math.min(level, MAX_LEVEL) / MAX_LEVEL;
        return SPAWN_BASE + (SPAWN_MAX - SPAWN_BASE) * Math.pow(progress, SPAWN_EXP);
    }

    /** Returns the bonus multiplier (0.0–0.50) for the given level. */
    public double rewardBonus(int level) {
        if (level <= 0) return 0.0;
        return Math.min(BONUS_PER_LVL * level, 0.50);
    }

    /** Human-readable spawn chance string, e.g. "0.0100%". */
    public String spawnChancePct(int level) {
        return String.format("%.4f", spawnChance(level) * 100.0);
    }

    /** Human-readable reward bonus string, e.g. "+5.00%". */
    public String rewardBonusPct(int level) {
        return String.format("+%.2f", rewardBonus(level) * 100.0);
    }
}
