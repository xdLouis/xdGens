package de.louis.xdGens.crate;

import org.bukkit.Material;

/**
 * All possible rewards that can come out of a crate.
 *
 * Tier values:  0 = COMMON  |  1 = RARE  |  2 = VERY_RARE
 *
 * Java enums cannot reference their own static fields inside enum-constant
 * constructor calls, so raw int literals are used here.
 * The public TIER_* constants below are for external use (CrateManager etc.).
 */
public enum CrateReward {

    // ── standard economy pouches ──────────────────────────────────────────
    MONEY  ("Money Pouch",  Material.SUNFLOWER,          1.00, 0),
    XP     ("XP Pouch",     Material.EXPERIENCE_BOTTLE,  1.00, 0),
    TOKENS ("Token Pouch",  Material.GOLD_INGOT,         1.00, 0),

    // ── basic chat tags (tier 1 – available from COMMON+ crates) ─────────
    TAG_FARMER   ("[Farmer]",   Material.WHEAT,    0.35, 1),
    TAG_HUNTER   ("[Hunter]",   Material.BOW,      0.35, 1),
    TAG_MERCHANT ("[Merchant]", Material.EMERALD,  0.30, 1),
    TAG_EXPLORER ("[Explorer]", Material.COMPASS,  0.30, 1),
    TAG_GUARDIAN ("[Guardian]", Material.SHIELD,   0.25, 1),

    // ── rare chat tags (tier 2 – RARE+ crates only) ───────────────────────
    TAG_LEGEND    ("[Legend]",    Material.NETHER_STAR,     0.08, 2),
    TAG_CELESTIAL ("[Celestial]", Material.CRYING_OBSIDIAN, 0.06, 2),
    TAG_ANCIENT   ("[Ancient]",   Material.ANCIENT_DEBRIS,  0.06, 2),
    TAG_DIVINE    ("[Divine]",    Material.BEACON,          0.04, 2),

    // ── basic chat colors (tier 1) ────────────────────────────────────────
    COLOR_AQUA         ("Aqua Name",   Material.CYAN_DYE,    0.30, 1),
    COLOR_GOLD         ("Gold Name",   Material.GOLD_NUGGET, 0.30, 1),
    COLOR_GREEN        ("Green Name",  Material.LIME_DYE,    0.30, 1),
    COLOR_LIGHT_PURPLE ("Purple Name", Material.PURPLE_DYE,  0.25, 1),

    // ── gradient colors (tier 2 – RARE+ crates only) ─────────────────────
    COLOR_GRADIENT_FIRE   ("Fire Gradient",   Material.BLAZE_POWDER,     0.07, 2),
    COLOR_GRADIENT_OCEAN  ("Ocean Gradient",  Material.PRISMARINE_SHARD, 0.07, 2),
    COLOR_GRADIENT_NATURE ("Nature Gradient", Material.OAK_LEAVES,       0.06, 2),
    COLOR_GRADIENT_GALAXY ("Galaxy Gradient", Material.AMETHYST_SHARD,   0.04, 2);

    // ── tier constants (for external use only) ────────────────────────────
    public static final int TIER_COMMON    = 0;
    public static final int TIER_RARE      = 1;
    public static final int TIER_VERY_RARE = 2;

    private final String   displayName;
    private final Material icon;
    private final double   weight;
    private final int      tier;

    CrateReward(String displayName, Material icon, double weight, int tier) {
        this.displayName = displayName;
        this.icon        = icon;
        this.weight      = weight;
        this.tier        = tier;
    }

    public String   getDisplayName() { return displayName; }
    public Material getIcon()        { return icon; }
    public double   getWeight()      { return weight; }
    public int      getTier()        { return tier; }

    public boolean isTag()   { return name().startsWith("TAG_"); }
    public boolean isColor() { return name().startsWith("COLOR_"); }
    public boolean isPouch() { return this == MONEY || this == XP || this == TOKENS; }

    public String tierLabel() {
        return switch (tier) {
            case 1  -> "<gradient:#7afcff:#00c2ff>Rare</gradient>";
            case 2  -> "<gradient:#c471f5:#fa71cd>Very Rare</gradient>";
            default -> "<gray>Common</gray>";
        };
    }

    public String getCosmeticFormat() {
        return switch (this) {
            case TAG_FARMER    -> "<green>[Farmer]</green>";
            case TAG_HUNTER    -> "<red>[Hunter]</red>";
            case TAG_MERCHANT  -> "<gold>[Merchant]</gold>";
            case TAG_EXPLORER  -> "<aqua>[Explorer]</aqua>";
            case TAG_GUARDIAN  -> "<blue>[Guardian]</blue>";
            case TAG_LEGEND    -> "<gradient:#f6d365:#fda085>[Legend]</gradient>";
            case TAG_CELESTIAL -> "<gradient:#a18cd1:#fbc2eb>[Celestial]</gradient>";
            case TAG_ANCIENT   -> "<gradient:#cfd9df:#e2ebf0>[Ancient]</gradient>";
            case TAG_DIVINE    -> "<gradient:#fffde4:#005c97>[Divine]</gradient>";
            case COLOR_AQUA         -> "<aqua>{name}</aqua>";
            case COLOR_GOLD         -> "<gold>{name}</gold>";
            case COLOR_GREEN        -> "<green>{name}</green>";
            case COLOR_LIGHT_PURPLE -> "<light_purple>{name}</light_purple>";
            case COLOR_GRADIENT_FIRE   -> "<gradient:#f83600:#fe8c00>{name}</gradient>";
            case COLOR_GRADIENT_OCEAN  -> "<gradient:#1a6dff:#00d2ff>{name}</gradient>";
            case COLOR_GRADIENT_NATURE -> "<gradient:#56ab2f:#a8e063>{name}</gradient>";
            case COLOR_GRADIENT_GALAXY -> "<gradient:#654ea3:#eaafc8>{name}</gradient>";
            default -> "";
        };
    }
}
