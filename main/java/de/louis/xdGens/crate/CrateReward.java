package de.louis.xdGens.crate;

import org.bukkit.Material;

/**
 * Tier values:  0 = COMMON  |  1 = RARE  |  2 = VERY_RARE  |  3 = LEGENDARY
 *
 * Cosmetic weights are intentionally tiny (0.01 – 0.09).
 * Tier-3 cosmetics ONLY from Legendary crates.
 */
public enum CrateReward {

    // ── economy pouches (not cosmetics) ──────────────────────────────
    MONEY  ("Money Pouch",  Material.SUNFLOWER,          1.00, 0, Type.POUCH),
    XP     ("XP Pouch",     Material.EXPERIENCE_BOTTLE,  1.00, 0, Type.POUCH),
    TOKENS ("Token Pouch",  Material.GOLD_INGOT,         1.00, 0, Type.POUCH),

    // ── TAGS tier 1 (Rare crates+) ────────────────────────────────────
    TAG_FARMER    ("[Farmer]",      Material.WHEAT,              0.09, 1, Type.TAG),
    TAG_HUNTER    ("[Hunter]",      Material.BOW,                0.09, 1, Type.TAG),
    TAG_MINER     ("[Miner]",       Material.IRON_PICKAXE,       0.09, 1, Type.TAG),
    TAG_MERCHANT  ("[Merchant]",    Material.EMERALD,            0.08, 1, Type.TAG),
    TAG_EXPLORER  ("[Explorer]",    Material.COMPASS,            0.08, 1, Type.TAG),
    TAG_GUARDIAN  ("[Guardian]",    Material.SHIELD,             0.07, 1, Type.TAG),
    TAG_WARRIOR   ("[Warrior]",     Material.IRON_SWORD,         0.07, 1, Type.TAG),
    TAG_BUILDER   ("[Builder]",     Material.BRICKS,             0.07, 1, Type.TAG),
    TAG_FISHER    ("[Fisher]",      Material.FISHING_ROD,        0.06, 1, Type.TAG),
    TAG_WANDERER  ("[Wanderer]",    Material.MAP,                0.06, 1, Type.TAG),

    // ── TAGS tier 2 (Epic+ crates) ────────────────────────────────────
    TAG_LEGEND     ("[Legend]",      Material.NETHER_STAR,        0.04, 2, Type.TAG),
    TAG_CELESTIAL  ("[Celestial]",   Material.CRYING_OBSIDIAN,    0.03, 2, Type.TAG),
    TAG_ANCIENT    ("[Ancient]",     Material.ANCIENT_DEBRIS,     0.03, 2, Type.TAG),
    TAG_DIVINE     ("[Divine]",      Material.BEACON,             0.02, 2, Type.TAG),
    TAG_PHANTOM    ("[Phantom]",     Material.PHANTOM_MEMBRANE,   0.02, 2, Type.TAG),
    TAG_WARDEN     ("[Warden]",      Material.SCULK_SENSOR,       0.02, 2, Type.TAG),
    TAG_DRAGON     ("✦ Dragon ✦",    Material.DRAGON_EGG,         0.02, 2, Type.TAG),

    // ── TAGS tier 3 (Legendary only) ─────────────────────────────────
    TAG_GOD        ("⚡ GOD ⚡",      Material.TOTEM_OF_UNDYING,   0.015, 3, Type.TAG),
    TAG_ETERNAL    ("☯ Eternal ☯",   Material.END_CRYSTAL,        0.012, 3, Type.TAG),
    TAG_OVERLORD   ("👑 Overlord 👑",  Material.NETHERITE_INGOT,   0.010, 3, Type.TAG),
    TAG_VOID       ("▌VOID▐",        Material.OBSIDIAN,           0.010, 3, Type.TAG),

    // ── NAME COLORS tier 1 ────────────────────────────────────────────
    COLOR_AQUA         ("Aqua",          Material.CYAN_DYE,          0.09, 1, Type.NAME_COLOR),
    COLOR_GOLD         ("Gold",          Material.GOLD_NUGGET,        0.09, 1, Type.NAME_COLOR),
    COLOR_GREEN        ("Green",         Material.LIME_DYE,           0.09, 1, Type.NAME_COLOR),
    COLOR_YELLOW       ("Yellow",        Material.YELLOW_DYE,         0.08, 1, Type.NAME_COLOR),
    COLOR_LIGHT_PURPLE ("Light Purple",  Material.PURPLE_DYE,         0.08, 1, Type.NAME_COLOR),
    COLOR_RED          ("Red",           Material.RED_DYE,            0.07, 1, Type.NAME_COLOR),
    COLOR_WHITE        ("White",         Material.WHITE_DYE,          0.07, 1, Type.NAME_COLOR),

    // ── NAME COLORS tier 2 ────────────────────────────────────────────
    COLOR_GRADIENT_FIRE    ("🔥 Fire",      Material.BLAZE_POWDER,       0.04, 2, Type.NAME_COLOR),
    COLOR_GRADIENT_OCEAN   ("🌊 Ocean",     Material.PRISMARINE_SHARD,   0.04, 2, Type.NAME_COLOR),
    COLOR_GRADIENT_NATURE  ("🌿 Nature",    Material.OAK_LEAVES,         0.03, 2, Type.NAME_COLOR),
    COLOR_GRADIENT_GALAXY  ("🌌 Galaxy",    Material.AMETHYST_SHARD,     0.03, 2, Type.NAME_COLOR),
    COLOR_GRADIENT_SUNSET  ("🌅 Sunset",    Material.ORANGE_DYE,         0.03, 2, Type.NAME_COLOR),
    COLOR_GRADIENT_ICE     ("❄ Ice",       Material.BLUE_ICE,           0.03, 2, Type.NAME_COLOR),
    COLOR_GRADIENT_TOXIC   ("☣ Toxic",     Material.SLIME_BALL,         0.02, 2, Type.NAME_COLOR),
    COLOR_GRADIENT_SHADOW  ("🌑 Shadow",    Material.COAL,               0.02, 2, Type.NAME_COLOR),

    // ── NAME COLORS tier 3 ────────────────────────────────────────────
    COLOR_GRADIENT_RAINBOW  ("🌈 Rainbow",  Material.NETHER_STAR,        0.015, 3, Type.NAME_COLOR),
    COLOR_GRADIENT_DIVINE   ("✨ Divine",   Material.END_CRYSTAL,        0.012, 3, Type.NAME_COLOR),
    COLOR_GRADIENT_ABYSS    ("⚫ Abyss",    Material.NETHERITE_SCRAP,    0.010, 3, Type.NAME_COLOR),

    // ── CHAT COLORS tier 1 ────────────────────────────────────────────
    CHAT_COLOR_WHITE        ("White Chat",   Material.WHITE_CONCRETE,    0.09, 1, Type.CHAT_COLOR),
    CHAT_COLOR_YELLOW       ("Yellow Chat",  Material.YELLOW_CONCRETE,   0.08, 1, Type.CHAT_COLOR),
    CHAT_COLOR_GREEN        ("Green Chat",   Material.LIME_CONCRETE,     0.08, 1, Type.CHAT_COLOR),
    CHAT_COLOR_AQUA         ("Aqua Chat",    Material.CYAN_CONCRETE,     0.07, 1, Type.CHAT_COLOR),
    CHAT_COLOR_GOLD         ("Gold Chat",    Material.ORANGE_CONCRETE,   0.07, 1, Type.CHAT_COLOR),

    // ── CHAT COLORS tier 2 ────────────────────────────────────────────
    CHAT_COLOR_GRADIENT_FIRE   ("🔥 Fire Chat",    Material.BLAZE_ROD,         0.04, 2, Type.CHAT_COLOR),
    CHAT_COLOR_GRADIENT_OCEAN  ("🌊 Ocean Chat",   Material.HEART_OF_THE_SEA,  0.03, 2, Type.CHAT_COLOR),
    CHAT_COLOR_GRADIENT_GALAXY ("🌌 Galaxy Chat",  Material.AMETHYST_CLUSTER,  0.03, 2, Type.CHAT_COLOR),
    CHAT_COLOR_GRADIENT_NATURE ("🌿 Nature Chat",  Material.FERN,              0.03, 2, Type.CHAT_COLOR),
    CHAT_COLOR_GRADIENT_ICE    ("❄ Ice Chat",     Material.PACKED_ICE,        0.02, 2, Type.CHAT_COLOR),
    CHAT_COLOR_GRADIENT_SHADOW ("🌑 Shadow Chat",  Material.NETHERRACK,        0.02, 2, Type.CHAT_COLOR),

    // ── CHAT COLORS tier 3 ────────────────────────────────────────────
    CHAT_COLOR_GRADIENT_RAINBOW ("🌈 Rainbow Chat",  Material.NETHER_STAR,      0.012, 3, Type.CHAT_COLOR),
    CHAT_COLOR_GRADIENT_DIVINE  ("✨ Divine Chat",   Material.TOTEM_OF_UNDYING, 0.010, 3, Type.CHAT_COLOR),
    CHAT_COLOR_GRADIENT_VOID    ("⚫ Void Chat",     Material.OBSIDIAN,         0.010, 3, Type.CHAT_COLOR);

    // ── tier / type constants ─────────────────────────────────────────
    public static final int TIER_COMMON     = 0;
    public static final int TIER_RARE       = 1;
    public static final int TIER_VERY_RARE  = 2;
    public static final int TIER_LEGENDARY  = 3;

    public enum Type { POUCH, TAG, NAME_COLOR, CHAT_COLOR }

    private final String   displayName;
    private final Material icon;
    private final double   weight;
    private final int      tier;
    private final Type     type;

    CrateReward(String displayName, Material icon, double weight, int tier, Type type) {
        this.displayName = displayName;
        this.icon        = icon;
        this.weight      = weight;
        this.tier        = tier;
        this.type        = type;
    }

    public String   getDisplayName() { return displayName; }
    public Material getIcon()        { return icon; }
    public double   getWeight()      { return weight; }
    public int      getTier()        { return tier; }
    public Type     getType()        { return type; }

    public boolean isTag()       { return type == Type.TAG; }
    public boolean isColor()     { return type == Type.NAME_COLOR; }
    public boolean isChatColor() { return type == Type.CHAT_COLOR; }
    public boolean isPouch()     { return type == Type.POUCH; }

    public String tierLabel() {
        return switch (tier) {
            case 1  -> "<gradient:#7afcff:#00c2ff>Rare</gradient>";
            case 2  -> "<gradient:#c471f5:#fa71cd>Very Rare</gradient>";
            case 3  -> "<gradient:#f6d365:#fda085><bold>✦ LEGENDARY ✦</bold></gradient>";
            default -> "<gray>Common</gray>";
        };
    }

    // ── formatting ────────────────────────────────────────────────────

    /** For tags: the full prefix component. For name/chat colors: wrap {name} or {msg}. */
    public String getCosmeticFormat() {
        return switch (this) {
            // Tags
            case TAG_FARMER    -> "<green><bold>[Farmer]</bold></green>";
            case TAG_HUNTER    -> "<red><bold>[Hunter]</bold></red>";
            case TAG_MINER     -> "<gray><bold>[Miner]</bold></gray>";
            case TAG_MERCHANT  -> "<gold><bold>[Merchant]</bold></gold>";
            case TAG_EXPLORER  -> "<aqua><bold>[Explorer]</bold></aqua>";
            case TAG_GUARDIAN  -> "<blue><bold>[Guardian]</bold></blue>";
            case TAG_WARRIOR   -> "<dark_red><bold>[Warrior]</bold></dark_red>";
            case TAG_BUILDER   -> "<yellow><bold>[Builder]</bold></yellow>";
            case TAG_FISHER    -> "<dark_aqua><bold>[Fisher]</bold></dark_aqua>";
            case TAG_WANDERER  -> "<dark_purple><bold>[Wanderer]</bold></dark_purple>";
            case TAG_LEGEND    -> "<gradient:#f6d365:#fda085><bold>[Legend]</bold></gradient>";
            case TAG_CELESTIAL -> "<gradient:#a18cd1:#fbc2eb><bold>[Celestial]</bold></gradient>";
            case TAG_ANCIENT   -> "<gradient:#cfd9df:#e2ebf0><bold>[Ancient]</bold></gradient>";
            case TAG_DIVINE    -> "<gradient:#fffde4:#005c97><bold>[Divine]</bold></gradient>";
            case TAG_PHANTOM   -> "<gradient:#7f7fd5:#86a8e7:#91eae4><bold>[Phantom]</bold></gradient>";
            case TAG_WARDEN    -> "<gradient:#00b4db:#0083b0><bold>[Warden]</bold></gradient>";
            case TAG_DRAGON    -> "<gradient:#c471f5:#fa71cd><bold>✦ Dragon ✦</bold></gradient>";
            case TAG_GOD       -> "<gradient:#f7971e:#ffd200><bold>⚡ GOD ⚡</bold></gradient>";
            case TAG_ETERNAL   -> "<gradient:#e0eafc:#cfdef3><bold>☯ Eternal ☯</bold></gradient>";
            case TAG_OVERLORD  -> "<gradient:#c94b4b:#4b134f><bold>👑 Overlord 👑</bold></gradient>";
            case TAG_VOID      -> "<dark_gray><bold>▌VOID▐</bold></dark_gray>";
            // Name colors
            case COLOR_AQUA          -> "<aqua>{name}</aqua>";
            case COLOR_GOLD          -> "<gold>{name}</gold>";
            case COLOR_GREEN         -> "<green>{name}</green>";
            case COLOR_YELLOW        -> "<yellow>{name}</yellow>";
            case COLOR_LIGHT_PURPLE  -> "<light_purple>{name}</light_purple>";
            case COLOR_RED           -> "<red>{name}</red>";
            case COLOR_WHITE         -> "<white>{name}</white>";
            case COLOR_GRADIENT_FIRE    -> "<gradient:#f83600:#fe8c00>{name}</gradient>";
            case COLOR_GRADIENT_OCEAN   -> "<gradient:#1a6dff:#00d2ff>{name}</gradient>";
            case COLOR_GRADIENT_NATURE  -> "<gradient:#56ab2f:#a8e063>{name}</gradient>";
            case COLOR_GRADIENT_GALAXY  -> "<gradient:#654ea3:#eaafc8>{name}</gradient>";
            case COLOR_GRADIENT_SUNSET  -> "<gradient:#f7971e:#ffd200>{name}</gradient>";
            case COLOR_GRADIENT_ICE     -> "<gradient:#74ebd5:#acb6e5>{name}</gradient>";
            case COLOR_GRADIENT_TOXIC   -> "<gradient:#56ab2f:#a8e063><bold>{name}</bold></gradient>";
            case COLOR_GRADIENT_SHADOW  -> "<gradient:#232526:#414345>{name}</gradient>";
            case COLOR_GRADIENT_RAINBOW -> "<gradient:#ff0000:#ff7700:#ffff00:#00ff00:#0000ff:#8b00ff>{name}</gradient>";
            case COLOR_GRADIENT_DIVINE  -> "<gradient:#fffde4:#005c97><bold>✨{name}✨</bold></gradient>";
            case COLOR_GRADIENT_ABYSS   -> "<gradient:#000000:#434343><bold>{name}</bold></gradient>";
            // Chat colors
            case CHAT_COLOR_WHITE         -> "<white>{msg}</white>";
            case CHAT_COLOR_YELLOW        -> "<yellow>{msg}</yellow>";
            case CHAT_COLOR_GREEN         -> "<green>{msg}</green>";
            case CHAT_COLOR_AQUA          -> "<aqua>{msg}</aqua>";
            case CHAT_COLOR_GOLD          -> "<gold>{msg}</gold>";
            case CHAT_COLOR_GRADIENT_FIRE    -> "<gradient:#f83600:#fe8c00>{msg}</gradient>";
            case CHAT_COLOR_GRADIENT_OCEAN   -> "<gradient:#1a6dff:#00d2ff>{msg}</gradient>";
            case CHAT_COLOR_GRADIENT_GALAXY  -> "<gradient:#654ea3:#eaafc8>{msg}</gradient>";
            case CHAT_COLOR_GRADIENT_NATURE  -> "<gradient:#56ab2f:#a8e063>{msg}</gradient>";
            case CHAT_COLOR_GRADIENT_ICE     -> "<gradient:#74ebd5:#acb6e5>{msg}</gradient>";
            case CHAT_COLOR_GRADIENT_SHADOW  -> "<gradient:#232526:#414345><bold>{msg}</bold></gradient>";
            case CHAT_COLOR_GRADIENT_RAINBOW -> "<gradient:#ff0000:#ff7700:#ffff00:#00ff00:#0000ff:#8b00ff>{msg}</gradient>";
            case CHAT_COLOR_GRADIENT_DIVINE  -> "<gradient:#fffde4:#005c97>{msg}</gradient>";
            case CHAT_COLOR_GRADIENT_VOID    -> "<dark_gray><bold>{msg}</bold></dark_gray>";
            default -> "{msg}";
        };
    }
}
