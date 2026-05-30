package de.louis.xdGens.skill;

/**
 * Base interface for all player skills.
 * Every skill has 10 upgrade levels (1–10).
 * Level 0 = not yet unlocked.
 */
public interface Skill {

    /** Unique identifier used in save files. */
    String id();

    /** Display name (MiniMessage). */
    String displayName();

    /** Icon material name for the GUI. */
    String iconMaterial();

    /** Max upgrade level. */
    default int maxLevel() { return 10; }

    /** Token cost to unlock (level 0 → 1). */
    int unlockCost();

    /** Token cost to upgrade from {@code currentLevel} to {@code currentLevel+1}. */
    int upgradeCost(int currentLevel);

    /** Human-readable description of what the skill does at the given level (MiniMessage). */
    String description(int level);
}
