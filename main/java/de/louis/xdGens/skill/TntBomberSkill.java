package de.louis.xdGens.skill;

/**
 * Stat calculations for the TNT Bomber upgrade.
 *
 * Spawn chance:  At lv 1 = 0.01% (1 in 10 000).
 *               At lv 1000 = 1.25% (1 in 80).
 *               Curve: base + (max-base) * (level/1000)^0.7
 */
public class TntBomberSkill implements Skill {

    private static final double SPAWN_BASE = 0.0001;  // 0.01% at lv 1
    private static final double SPAWN_MAX  = 0.0125;  // 1.25% at lv 1000
    private static final double SPAWN_EXP  = 0.70;
    private static final int    MAX_LEVEL  = 1000;

    @Override public String id()           { return "tnt_bomber"; }
    @Override public String displayName()  { return "<gradient:#ff6b6b:#ffd93d>\uD83D\uDCA3 TNT Bomber</gradient>"; }
    @Override public String iconMaterial() { return "TNT"; }
    @Override public int    maxLevel()     { return MAX_LEVEL; }
    @Override public int    unlockCost()   { return 500; }

    @Override
    public int upgradeCost(int currentLevel) {
        return (int) Math.round(500.0 * (1.0 + 0.20 * (currentLevel + 1))
                * Math.pow(1.034, currentLevel));
    }

    @Override
    public String description(int level) {
        return "<gray>Spawn: <white>" + spawnChancePct(level) + "%</white>";
    }

    public double spawnChance(int level) {
        if (level <= 0) return 0.0;
        double progress = (double) Math.min(level, MAX_LEVEL) / MAX_LEVEL;
        return SPAWN_BASE + (SPAWN_MAX - SPAWN_BASE) * Math.pow(progress, SPAWN_EXP);
    }

    public String spawnChancePct(int level) {
        return String.format("%.4f", spawnChance(level) * 100.0);
    }
}
