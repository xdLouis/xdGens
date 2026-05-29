package de.louis.xdGens.skill;

/**
 * Panda Roller — a panda has a chance to appear when harvesting wheat.
 * It rolls across the field for ~7 seconds and grants a percentage bonus
 * of XP + Tokens earned during that window.
 *
 * Spawn chance  : 1% at L1 → 10% at L10  (linear: level * 1%)
 * Reward bonus  : 20% at L1 → 120% at L10 (20 + (level-1)*~11.1%)
 * Duration      : always 7 seconds
 *
 * Rewards are proportional to the player's actual harvest output,
 * so the skill remains valuable at every prestige level.
 */
public class PandaRollerSkill implements Skill {

    @Override public String id()           { return "panda_roller"; }
    @Override public String displayName()  { return "<gradient:#a8e6cf:#88d8b0>\uD83D\uDC3C Panda Roller</gradient>"; }
    @Override public String iconMaterial() { return "BAMBOO"; }
    @Override public int    unlockCost()   { return 500; }

    @Override
    public int upgradeCost(int currentLevel) {
        // 250, 500, 750, 1000, 1500, 2000, 2500, 3000, 4000
        return switch (currentLevel) {
            case 1 -> 250;
            case 2 -> 500;
            case 3 -> 750;
            case 4 -> 1_000;
            case 5 -> 1_500;
            case 6 -> 2_000;
            case 7 -> 2_500;
            case 8 -> 3_000;
            case 9 -> 4_000;
            default -> Integer.MAX_VALUE;
        };
    }

    /** Spawn chance 0.0–1.0 */
    public double spawnChance(int level) {
        return level * 0.01;  // 1% per level
    }

    /** Reward multiplier (fraction of XP+Tokens earned during roll window) */
    public double rewardBonus(int level) {
        // L1=0.20, L5=0.64, L10=1.20
        return 0.20 + (level - 1) * (1.00 / 9.0);
    }

    /** Roll duration in ticks (7 seconds = 140 ticks) */
    public int rollDurationTicks() { return 140; }

    @Override
    public String description(int level) {
        if (level == 0) return "<gray>Unlock to summon a rolling panda!";
        int chancePct = level;  // level * 1%
        int bonusPct  = (int) Math.round(rewardBonus(level) * 100);
        return "<gray>Spawn chance: <white>" + chancePct + "%</white>\n"
             + "<gray>XP + Token bonus: <green>+" + bonusPct + "%</green> of 7s harvest\n"
             + "<dark_gray>Proportional — scales with prestige.";
    }
}
