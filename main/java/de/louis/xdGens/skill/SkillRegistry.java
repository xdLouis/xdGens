package de.louis.xdGens.skill;

import java.util.Collection;
import java.util.LinkedHashMap;
import java.util.Map;

/**
 * Holds all registered {@link Skill} definitions.
 * Add new skills here — order determines GUI slot order.
 */
public final class SkillRegistry {

    private static final Map<String, Skill> REGISTRY = new LinkedHashMap<>();

    static {
        register(new PandaRollerSkill());
        // register(new BeeHiveSkill());   ← add future skills here
    }

    public static void register(Skill skill) {
        REGISTRY.put(skill.id(), skill);
    }

    public static Skill get(String id) {
        return REGISTRY.get(id);
    }

    public static Collection<Skill> all() {
        return REGISTRY.values();
    }
}
