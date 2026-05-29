package de.louis.xdGens.crate;

import org.bukkit.Material;

/**
 * Tier values:  0 = COMMON  |  1 = RARE  |  2 = VERY_RARE  |  3 = LEGENDARY
 *
 * Cosmetic weights are intentionally tiny (0.01 – 0.09).
 * Tier-3 cosmetics ONLY from Legendary crates.
 *
 * Categories:
 *   TAG        — chat prefix tag
 *   NAME_COLOR — player name color / gradient
 *   CHAT_COLOR — chat message color
 *   GLOW       — entity glow effect (color or prismatic cycling)
 */
public enum CrateReward {

    // ── economy pouches (not cosmetics) ──────────────────────────────
    MONEY  ("Money Pouch",  Material.SUNFLOWER,         1.00, 0, Type.POUCH),
    XP     ("XP Pouch",     Material.EXPERIENCE_BOTTLE, 1.00, 0, Type.POUCH),
    TOKENS ("Token Pouch",  Material.GOLD_INGOT,        1.00, 0, Type.POUCH),

    // ── TAGS tier 1 ─────────────────────────────────────────────────
    TAG_FARMER    ("[Farmer]",       Material.WHEAT,              0.09, 1, Type.TAG),
    TAG_HUNTER    ("[Hunter]",       Material.BOW,                0.09, 1, Type.TAG),
    TAG_MINER     ("[Miner]",        Material.IRON_PICKAXE,       0.09, 1, Type.TAG),
    TAG_MERCHANT  ("[Merchant]",     Material.EMERALD,            0.08, 1, Type.TAG),
    TAG_EXPLORER  ("[Explorer]",     Material.COMPASS,            0.08, 1, Type.TAG),
    TAG_GUARDIAN  ("[Guardian]",     Material.SHIELD,             0.07, 1, Type.TAG),
    TAG_WARRIOR   ("[Warrior]",      Material.IRON_SWORD,         0.07, 1, Type.TAG),
    TAG_BUILDER   ("[Builder]",      Material.BRICKS,             0.07, 1, Type.TAG),
    TAG_FISHER    ("[Fisher]",       Material.FISHING_ROD,        0.06, 1, Type.TAG),
    TAG_WANDERER  ("[Wanderer]",     Material.MAP,                0.06, 1, Type.TAG),
    TAG_TRADER    ("[Trader]",       Material.VILLAGER_SPAWN_EGG, 0.06, 1, Type.TAG),
    TAG_SCOUT     ("[Scout]",        Material.SPYGLASS,           0.06, 1, Type.TAG),
    TAG_ALCHEMIST ("[Alchemist]",    Material.BREWING_STAND,      0.05, 1, Type.TAG),
    TAG_KNIGHT    ("[Knight]",       Material.CHAINMAIL_HELMET,   0.05, 1, Type.TAG),
    TAG_ARCHER    ("[Archer]",       Material.ARROW,              0.05, 1, Type.TAG),
    TAG_COOK      ("🍳 Cook",        Material.COOKED_BEEF,        0.05, 1, Type.TAG),
    TAG_SAILOR    ("⚓ Sailor",      Material.NAUTILUS_SHELL,     0.04, 1, Type.TAG),
    TAG_BLACKSMITH("[Blacksmith]",   Material.ANVIL,              0.04, 1, Type.TAG),
    TAG_HERBALIST ("🌿 Herbalist",   Material.FERN,               0.04, 1, Type.TAG),
    TAG_RANGER    ("[Ranger]",       Material.OAK_SAPLING,        0.04, 1, Type.TAG),

    // ── TAGS tier 2 ─────────────────────────────────────────────────
    TAG_LEGEND     ("[Legend]",        Material.NETHER_STAR,        0.04, 2, Type.TAG),
    TAG_CELESTIAL  ("[Celestial]",     Material.CRYING_OBSIDIAN,    0.03, 2, Type.TAG),
    TAG_ANCIENT    ("[Ancient]",       Material.ANCIENT_DEBRIS,     0.03, 2, Type.TAG),
    TAG_DIVINE     ("[Divine]",        Material.BEACON,             0.02, 2, Type.TAG),
    TAG_PHANTOM    ("[Phantom]",       Material.PHANTOM_MEMBRANE,   0.02, 2, Type.TAG),
    TAG_WARDEN     ("[Warden]",        Material.SCULK_SENSOR,       0.02, 2, Type.TAG),
    TAG_DRAGON     ("❆ Dragon ❆",     Material.DRAGON_EGG,         0.02, 2, Type.TAG),
    TAG_SHADOW     ("🌑 Shadow",      Material.COAL,               0.02, 2, Type.TAG),
    TAG_INFERNO    ("🔥 Inferno",     Material.BLAZE_ROD,          0.02, 2, Type.TAG),
    TAG_SPECTER    ("👻 Specter",     Material.SOUL_LANTERN,       0.02, 2, Type.TAG),
    TAG_ARCANE     ("✨ Arcane",       Material.AMETHYST_SHARD,     0.02, 2, Type.TAG),
    TAG_TITAN      ("[🗿 TITAN]",     Material.COBBLESTONE,        0.02, 2, Type.TAG),
    TAG_REAPER     ("☠ Reaper",       Material.WITHER_SKELETON_SKULL,0.015,2, Type.TAG),
    TAG_STORM      ("⚡ Storm",        Material.LIGHTNING_ROD,      0.015, 2, Type.TAG),
    TAG_ENDER      ("[Ender]",         Material.ENDER_EYE,          0.015, 2, Type.TAG),

    // ── TAGS tier 3 ─────────────────────────────────────────────────
    TAG_GOD        ("⚡ GOD ⚡",        Material.TOTEM_OF_UNDYING,   0.015, 3, Type.TAG),
    TAG_ETERNAL    ("☯ Eternal ☯",    Material.END_CRYSTAL,        0.012, 3, Type.TAG),
    TAG_OVERLORD   ("👑 Overlord 👑",  Material.NETHERITE_INGOT,    0.010, 3, Type.TAG),
    TAG_VOID       ("▌ VOID▐",        Material.OBSIDIAN,           0.010, 3, Type.TAG),
    TAG_COSMOS     ("🌌 COSMOS 🌌",  Material.SPORE_BLOSSOM,      0.010, 3, Type.TAG),
    TAG_ABYSS_LORD ("🔮 Abyss Lord",  Material.NETHERITE_BLOCK,    0.010, 3, Type.TAG),
    TAG_NEXUS      ("✵ NEXUS ✵",       Material.CONDUIT,            0.010, 3, Type.TAG),

    // ── NAME COLORS tier 1 ──────────────────────────────────────────
    COLOR_AQUA         ("Aqua",          Material.CYAN_DYE,           0.09, 1, Type.NAME_COLOR),
    COLOR_GOLD         ("Gold",          Material.GOLD_NUGGET,        0.09, 1, Type.NAME_COLOR),
    COLOR_GREEN        ("Green",         Material.LIME_DYE,           0.09, 1, Type.NAME_COLOR),
    COLOR_YELLOW       ("Yellow",        Material.YELLOW_DYE,         0.08, 1, Type.NAME_COLOR),
    COLOR_LIGHT_PURPLE ("Light Purple",  Material.PURPLE_DYE,         0.08, 1, Type.NAME_COLOR),
    COLOR_RED          ("Red",           Material.RED_DYE,            0.07, 1, Type.NAME_COLOR),
    COLOR_WHITE        ("White",         Material.WHITE_DYE,          0.07, 1, Type.NAME_COLOR),
    COLOR_DARK_AQUA    ("Dark Aqua",     Material.CYAN_TERRACOTTA,    0.06, 1, Type.NAME_COLOR),
    COLOR_DARK_GREEN   ("Dark Green",    Material.GREEN_DYE,          0.06, 1, Type.NAME_COLOR),
    COLOR_BLUE         ("Blue",          Material.BLUE_DYE,           0.06, 1, Type.NAME_COLOR),
    COLOR_DARK_PURPLE  ("Dark Purple",   Material.MAGENTA_DYE,        0.05, 1, Type.NAME_COLOR),
    COLOR_ORANGE       ("Orange",        Material.ORANGE_DYE,         0.05, 1, Type.NAME_COLOR),
    COLOR_PINK         ("Pink",          Material.PINK_DYE,           0.05, 1, Type.NAME_COLOR),

    // ── NAME COLORS tier 2 ──────────────────────────────────────────
    COLOR_GRADIENT_FIRE    ("🔥 Fire",      Material.BLAZE_POWDER,       0.04, 2, Type.NAME_COLOR),
    COLOR_GRADIENT_OCEAN   ("🌊 Ocean",     Material.PRISMARINE_SHARD,   0.04, 2, Type.NAME_COLOR),
    COLOR_GRADIENT_NATURE  ("🌿 Nature",    Material.OAK_LEAVES,         0.03, 2, Type.NAME_COLOR),
    COLOR_GRADIENT_GALAXY  ("🌌 Galaxy",    Material.AMETHYST_SHARD,     0.03, 2, Type.NAME_COLOR),
    COLOR_GRADIENT_SUNSET  ("🌅 Sunset",    Material.ORANGE_DYE,         0.03, 2, Type.NAME_COLOR),
    COLOR_GRADIENT_ICE     ("❄ Ice",       Material.BLUE_ICE,           0.03, 2, Type.NAME_COLOR),
    COLOR_GRADIENT_TOXIC   ("☣ Toxic",     Material.SLIME_BALL,         0.02, 2, Type.NAME_COLOR),
    COLOR_GRADIENT_SHADOW  ("🌑 Shadow",    Material.COAL,               0.02, 2, Type.NAME_COLOR),
    COLOR_GRADIENT_ROSE    ("🌹 Rose",      Material.PINK_TULIP,         0.02, 2, Type.NAME_COLOR),
    COLOR_GRADIENT_BLOOD   ("🩸 Blood",     Material.REDSTONE,           0.02, 2, Type.NAME_COLOR),
    COLOR_GRADIENT_LAVA    ("🌋 Lava",      Material.LAVA_BUCKET,        0.02, 2, Type.NAME_COLOR),
    COLOR_GRADIENT_AURORA  ("🌌 Aurora",    Material.SEA_LANTERN,        0.02, 2, Type.NAME_COLOR),

    // ── NAME COLORS tier 3 ──────────────────────────────────────────
    COLOR_GRADIENT_RAINBOW  ("🌈 Rainbow",  Material.NETHER_STAR,        0.015, 3, Type.NAME_COLOR),
    COLOR_GRADIENT_DIVINE   ("✨ Divine",   Material.END_CRYSTAL,        0.012, 3, Type.NAME_COLOR),
    COLOR_GRADIENT_ABYSS    ("⚫ Abyss",    Material.NETHERITE_SCRAP,    0.010, 3, Type.NAME_COLOR),
    COLOR_GRADIENT_COSMOS   ("🌌 Cosmos",   Material.SPORE_BLOSSOM,      0.010, 3, Type.NAME_COLOR),
    COLOR_GRADIENT_VOID_FIRE("🔥⚫ Void Fire", Material.SOUL_TORCH,        0.010, 3, Type.NAME_COLOR),

    // ── CHAT COLORS tier 1 ──────────────────────────────────────────
    CHAT_WHITE        ("White Chat",    Material.WHITE_CONCRETE,     0.09, 1, Type.CHAT_COLOR),
    CHAT_YELLOW       ("Yellow Chat",   Material.YELLOW_CONCRETE,    0.08, 1, Type.CHAT_COLOR),
    CHAT_GREEN        ("Green Chat",    Material.LIME_CONCRETE,      0.08, 1, Type.CHAT_COLOR),
    CHAT_AQUA         ("Aqua Chat",     Material.CYAN_CONCRETE,      0.07, 1, Type.CHAT_COLOR),
    CHAT_GOLD         ("Gold Chat",     Material.ORANGE_CONCRETE,    0.07, 1, Type.CHAT_COLOR),
    CHAT_RED          ("Red Chat",      Material.RED_CONCRETE,       0.06, 1, Type.CHAT_COLOR),
    CHAT_BLUE         ("Blue Chat",     Material.BLUE_CONCRETE,      0.06, 1, Type.CHAT_COLOR),
    CHAT_LIGHT_PURPLE ("Purple Chat",   Material.PURPLE_CONCRETE,    0.06, 1, Type.CHAT_COLOR),
    CHAT_PINK         ("Pink Chat",     Material.PINK_CONCRETE,      0.05, 1, Type.CHAT_COLOR),
    CHAT_ORANGE       ("Orange Chat",   Material.ORANGE_CONCRETE,    0.05, 1, Type.CHAT_COLOR),

    // ── CHAT COLORS tier 2 ──────────────────────────────────────────
    CHAT_G_FIRE    ("🔥 Fire Chat",      Material.BLAZE_ROD,          0.04, 2, Type.CHAT_COLOR),
    CHAT_G_OCEAN   ("🌊 Ocean Chat",     Material.HEART_OF_THE_SEA,   0.03, 2, Type.CHAT_COLOR),
    CHAT_G_GALAXY  ("🌌 Galaxy Chat",    Material.AMETHYST_CLUSTER,   0.03, 2, Type.CHAT_COLOR),
    CHAT_G_NATURE  ("🌿 Nature Chat",    Material.FERN,               0.03, 2, Type.CHAT_COLOR),
    CHAT_G_ICE     ("❄ Ice Chat",       Material.PACKED_ICE,         0.02, 2, Type.CHAT_COLOR),
    CHAT_G_SHADOW  ("🌑 Shadow Chat",    Material.NETHERRACK,         0.02, 2, Type.CHAT_COLOR),
    CHAT_G_ROSE    ("🌹 Rose Chat",      Material.PINK_TULIP,         0.02, 2, Type.CHAT_COLOR),
    CHAT_G_TOXIC   ("☣ Toxic Chat",     Material.SLIME_BALL,         0.02, 2, Type.CHAT_COLOR),
    CHAT_G_LAVA    ("🌋 Lava Chat",      Material.LAVA_BUCKET,        0.02, 2, Type.CHAT_COLOR),
    CHAT_G_STORM   ("⚡ Storm Chat",    Material.LIGHTNING_ROD,      0.02, 2, Type.CHAT_COLOR),

    // ── CHAT COLORS tier 3 ──────────────────────────────────────────
    CHAT_G_RAINBOW ("🌈 Rainbow Chat",  Material.NETHER_STAR,        0.012, 3, Type.CHAT_COLOR),
    CHAT_G_DIVINE  ("✨ Divine Chat",   Material.TOTEM_OF_UNDYING,   0.010, 3, Type.CHAT_COLOR),
    CHAT_G_VOID    ("⚫ Void Chat",     Material.OBSIDIAN,           0.010, 3, Type.CHAT_COLOR),
    CHAT_G_COSMOS  ("🌌 Cosmos Chat",   Material.SPORE_BLOSSOM,      0.010, 3, Type.CHAT_COLOR),

    // ── GLOW tier 1 (Rare) — solid team colors ─────────────────────────
    GLOW_WHITE   ("White Glow",       Material.WHITE_STAINED_GLASS,  0.07, 1, Type.GLOW),
    GLOW_YELLOW  ("Yellow Glow",      Material.YELLOW_STAINED_GLASS, 0.07, 1, Type.GLOW),
    GLOW_GREEN   ("Green Glow",       Material.LIME_STAINED_GLASS,   0.06, 1, Type.GLOW),
    GLOW_AQUA    ("Aqua Glow",        Material.CYAN_STAINED_GLASS,   0.06, 1, Type.GLOW),
    GLOW_RED     ("Red Glow",         Material.RED_STAINED_GLASS,    0.06, 1, Type.GLOW),
    GLOW_BLUE    ("Blue Glow",        Material.BLUE_STAINED_GLASS,   0.05, 1, Type.GLOW),
    GLOW_PINK    ("Pink Glow",        Material.PINK_STAINED_GLASS,   0.05, 1, Type.GLOW),
    GLOW_ORANGE  ("Orange Glow",      Material.ORANGE_STAINED_GLASS, 0.05, 1, Type.GLOW),

    // ── GLOW tier 2 (Very Rare) — rarer colors ────────────────────────
    GLOW_PURPLE  ("Purple Glow",      Material.PURPLE_STAINED_GLASS, 0.03, 2, Type.GLOW),
    GLOW_DARK_RED("Dark Red Glow",    Material.CRIMSON_STEM,         0.03, 2, Type.GLOW),
    GLOW_DARK_BLUE("Dark Blue Glow",  Material.BLUE_TERRACOTTA,      0.03, 2, Type.GLOW),
    GLOW_BLACK   ("Black Glow",       Material.BLACK_STAINED_GLASS,  0.02, 2, Type.GLOW),
    GLOW_GOLD    ("👑 Gold Glow",     Material.GOLD_BLOCK,           0.02, 2, Type.GLOW),
    GLOW_DARK_GREEN("Dark Green Glow",Material.GREEN_STAINED_GLASS,  0.02, 2, Type.GLOW),

    // ── GLOW tier 3 (Legendary only) — special effects ──────────────
    GLOW_PRISMATIC("🌈 Prismatic Glow",  Material.NETHER_STAR,          0.012, 3, Type.GLOW),
    GLOW_INFERNO  ("🔥 Inferno Glow",    Material.BLAZE_POWDER,         0.010, 3, Type.GLOW),
    GLOW_VOID_DARK("⚫ Void Glow",      Material.OBSIDIAN,             0.010, 3, Type.GLOW);

    // ── tier / type constants ─────────────────────────────────────────────
    public static final int TIER_COMMON    = 0;
    public static final int TIER_RARE      = 1;
    public static final int TIER_VERY_RARE = 2;
    public static final int TIER_LEGENDARY = 3;

    public enum Type { POUCH, TAG, NAME_COLOR, CHAT_COLOR, GLOW }

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
    public boolean isGlow()      { return type == Type.GLOW; }
    public boolean isPouch()     { return type == Type.POUCH; }

    /** True for GLOW_PRISMATIC — signals that the glow color should cycle/animate. */
    public boolean isPrismaticGlow() { return this == GLOW_PRISMATIC; }

    public String tierLabel() {
        return switch (tier) {
            case 1  -> "<gradient:#7afcff:#00c2ff>Rare</gradient>";
            case 2  -> "<gradient:#c471f5:#fa71cd>Very Rare</gradient>";
            case 3  -> "<gradient:#f6d365:#fda085><bold>❆ LEGENDARY ❆</bold></gradient>";
            default -> "<gray>Common</gray>";
        };
    }

    // ── cosmetic formatting ──────────────────────────────────────────────
    public String getCosmeticFormat() {
        return switch (this) {
            // ─ Tags ─
            case TAG_FARMER     -> "<green><bold>[Farmer]</bold></green>";
            case TAG_HUNTER     -> "<red><bold>[Hunter]</bold></red>";
            case TAG_MINER      -> "<gray><bold>[Miner]</bold></gray>";
            case TAG_MERCHANT   -> "<gold><bold>[Merchant]</bold></gold>";
            case TAG_EXPLORER   -> "<aqua><bold>[Explorer]</bold></aqua>";
            case TAG_GUARDIAN   -> "<blue><bold>[Guardian]</bold></blue>";
            case TAG_WARRIOR    -> "<dark_red><bold>[Warrior]</bold></dark_red>";
            case TAG_BUILDER    -> "<yellow><bold>[Builder]</bold></yellow>";
            case TAG_FISHER     -> "<dark_aqua><bold>[Fisher]</bold></dark_aqua>";
            case TAG_WANDERER   -> "<dark_purple><bold>[Wanderer]</bold></dark_purple>";
            case TAG_TRADER     -> "<green><bold>[Trader]</bold></green>";
            case TAG_SCOUT      -> "<aqua><bold>[Scout]</bold></aqua>";
            case TAG_ALCHEMIST  -> "<dark_purple><bold>[Alchemist]</bold></dark_purple>";
            case TAG_KNIGHT     -> "<gray><bold>[Knight]</bold></gray>";
            case TAG_ARCHER     -> "<green><bold>[Archer]</bold></green>";
            case TAG_COOK       -> "<yellow><bold>🍳 Cook</bold></yellow>";
            case TAG_SAILOR     -> "<aqua><bold>⚓ Sailor</bold></aqua>";
            case TAG_BLACKSMITH -> "<dark_gray><bold>[Blacksmith]</bold></dark_gray>";
            case TAG_HERBALIST  -> "<green><bold>🌿 Herbalist</bold></green>";
            case TAG_RANGER     -> "<dark_green><bold>[Ranger]</bold></dark_green>";
            case TAG_LEGEND     -> "<gradient:#f6d365:#fda085><bold>[Legend]</bold></gradient>";
            case TAG_CELESTIAL  -> "<gradient:#a18cd1:#fbc2eb><bold>[Celestial]</bold></gradient>";
            case TAG_ANCIENT    -> "<gradient:#cfd9df:#e2ebf0><bold>[Ancient]</bold></gradient>";
            case TAG_DIVINE     -> "<gradient:#fffde4:#005c97><bold>[Divine]</bold></gradient>";
            case TAG_PHANTOM    -> "<gradient:#7f7fd5:#86a8e7:#91eae4><bold>[Phantom]</bold></gradient>";
            case TAG_WARDEN     -> "<gradient:#00b4db:#0083b0><bold>[Warden]</bold></gradient>";
            case TAG_DRAGON     -> "<gradient:#c471f5:#fa71cd><bold>❆ Dragon ❆</bold></gradient>";
            case TAG_SHADOW     -> "<gradient:#232526:#414345><bold>🌑 Shadow</bold></gradient>";
            case TAG_INFERNO    -> "<gradient:#f83600:#fe8c00><bold>🔥 Inferno</bold></gradient>";
            case TAG_SPECTER    -> "<gradient:#7f8c8d:#bdc3c7><bold>👻 Specter</bold></gradient>";
            case TAG_ARCANE     -> "<gradient:#654ea3:#eaafc8><bold>✨ Arcane</bold></gradient>";
            case TAG_TITAN      -> "<gradient:#636363:#a2ab58><bold>[🗿 TITAN]</bold></gradient>";
            case TAG_REAPER     -> "<gradient:#000000:#434343><bold>☠ Reaper</bold></gradient>";
            case TAG_STORM      -> "<gradient:#373b44:#4286f4><bold>⚡ Storm</bold></gradient>";
            case TAG_ENDER      -> "<gradient:#6a3093:#a044ff><bold>[Ender]</bold></gradient>";
            case TAG_GOD        -> "<gradient:#f7971e:#ffd200><bold>⚡ GOD ⚡</bold></gradient>";
            case TAG_ETERNAL    -> "<gradient:#e0eafc:#cfdef3><bold>☯ Eternal ☯</bold></gradient>";
            case TAG_OVERLORD   -> "<gradient:#c94b4b:#4b134f><bold>👑 Overlord 👑</bold></gradient>";
            case TAG_VOID       -> "<dark_gray><bold>▌ VOID▐</bold></dark_gray>";
            case TAG_COSMOS     -> "<gradient:#1a1a2e:#16213e:#0f3460:#533483><bold>🌌 COSMOS 🌌</bold></gradient>";
            case TAG_ABYSS_LORD -> "<gradient:#000000:#3d0000><bold>🔮 Abyss Lord</bold></gradient>";
            case TAG_NEXUS      -> "<gradient:#c471f5:#12c2e9:#f64f59><bold>✵ NEXUS ✵</bold></gradient>";
            // ─ Name colors ─
            case COLOR_AQUA          -> "<aqua>{name}</aqua>";
            case COLOR_GOLD          -> "<gold>{name}</gold>";
            case COLOR_GREEN         -> "<green>{name}</green>";
            case COLOR_YELLOW        -> "<yellow>{name}</yellow>";
            case COLOR_LIGHT_PURPLE  -> "<light_purple>{name}</light_purple>";
            case COLOR_RED           -> "<red>{name}</red>";
            case COLOR_WHITE         -> "<white>{name}</white>";
            case COLOR_DARK_AQUA     -> "<dark_aqua>{name}</dark_aqua>";
            case COLOR_DARK_GREEN    -> "<dark_green>{name}</dark_green>";
            case COLOR_BLUE          -> "<blue>{name}</blue>";
            case COLOR_DARK_PURPLE   -> "<dark_purple>{name}</dark_purple>";
            case COLOR_ORANGE        -> "<color:#ff8c00>{name}</color>";
            case COLOR_PINK          -> "<color:#ff69b4>{name}</color>";
            case COLOR_GRADIENT_FIRE     -> "<gradient:#f83600:#fe8c00>{name}</gradient>";
            case COLOR_GRADIENT_OCEAN    -> "<gradient:#1a6dff:#00d2ff>{name}</gradient>";
            case COLOR_GRADIENT_NATURE   -> "<gradient:#56ab2f:#a8e063>{name}</gradient>";
            case COLOR_GRADIENT_GALAXY   -> "<gradient:#654ea3:#eaafc8>{name}</gradient>";
            case COLOR_GRADIENT_SUNSET   -> "<gradient:#f7971e:#ffd200>{name}</gradient>";
            case COLOR_GRADIENT_ICE      -> "<gradient:#74ebd5:#acb6e5>{name}</gradient>";
            case COLOR_GRADIENT_TOXIC    -> "<gradient:#56ab2f:#a8e063><bold>{name}</bold></gradient>";
            case COLOR_GRADIENT_SHADOW   -> "<gradient:#232526:#414345>{name}</gradient>";
            case COLOR_GRADIENT_ROSE     -> "<gradient:#f953c6:#b91d73>{name}</gradient>";
            case COLOR_GRADIENT_BLOOD    -> "<gradient:#c0392b:#8e0000>{name}</gradient>";
            case COLOR_GRADIENT_LAVA     -> "<gradient:#ff4e00:#ec9f05>{name}</gradient>";
            case COLOR_GRADIENT_AURORA   -> "<gradient:#00c3ff:#ffff1c>{name}</gradient>";
            case COLOR_GRADIENT_RAINBOW  -> "<gradient:#ff0000:#ff7700:#ffff00:#00ff00:#0000ff:#8b00ff>{name}</gradient>";
            case COLOR_GRADIENT_DIVINE   -> "<gradient:#fffde4:#005c97><bold>✨{name}✨</bold></gradient>";
            case COLOR_GRADIENT_ABYSS    -> "<gradient:#000000:#434343><bold>{name}</bold></gradient>";
            case COLOR_GRADIENT_COSMOS   -> "<gradient:#1a1a2e:#16213e:#0f3460:#533483>{name}</gradient>";
            case COLOR_GRADIENT_VOID_FIRE-> "<gradient:#000000:#ff4e00>{name}</gradient>";
            // ─ Chat colors ─
            case CHAT_WHITE          -> "<white>{msg}</white>";
            case CHAT_YELLOW         -> "<yellow>{msg}</yellow>";
            case CHAT_GREEN          -> "<green>{msg}</green>";
            case CHAT_AQUA           -> "<aqua>{msg}</aqua>";
            case CHAT_GOLD           -> "<gold>{msg}</gold>";
            case CHAT_RED            -> "<red>{msg}</red>";
            case CHAT_BLUE           -> "<blue>{msg}</blue>";
            case CHAT_LIGHT_PURPLE   -> "<light_purple>{msg}</light_purple>";
            case CHAT_PINK           -> "<color:#ff69b4>{msg}</color>";
            case CHAT_ORANGE         -> "<color:#ff8c00>{msg}</color>";
            case CHAT_G_FIRE   -> "<gradient:#f83600:#fe8c00>{msg}</gradient>";
            case CHAT_G_OCEAN  -> "<gradient:#1a6dff:#00d2ff>{msg}</gradient>";
            case CHAT_G_GALAXY -> "<gradient:#654ea3:#eaafc8>{msg}</gradient>";
            case CHAT_G_NATURE -> "<gradient:#56ab2f:#a8e063>{msg}</gradient>";
            case CHAT_G_ICE    -> "<gradient:#74ebd5:#acb6e5>{msg}</gradient>";
            case CHAT_G_SHADOW -> "<gradient:#232526:#414345><bold>{msg}</bold></gradient>";
            case CHAT_G_ROSE   -> "<gradient:#f953c6:#b91d73>{msg}</gradient>";
            case CHAT_G_TOXIC  -> "<gradient:#11998e:#38ef7d>{msg}</gradient>";
            case CHAT_G_LAVA   -> "<gradient:#ff4e00:#ec9f05>{msg}</gradient>";
            case CHAT_G_STORM  -> "<gradient:#373b44:#4286f4><bold>{msg}</bold></gradient>";
            case CHAT_G_RAINBOW-> "<gradient:#ff0000:#ff7700:#ffff00:#00ff00:#0000ff:#8b00ff>{msg}</gradient>";
            case CHAT_G_DIVINE -> "<gradient:#fffde4:#005c97>{msg}</gradient>";
            case CHAT_G_VOID   -> "<dark_gray><bold>{msg}</bold></dark_gray>";
            case CHAT_G_COSMOS -> "<gradient:#1a1a2e:#16213e:#0f3460:#533483>{msg}</gradient>";
            // ─ Glow — store the ChatColor/TeamColor name used by the glow system ─
            // The glow manager reads getGlowColor() instead of this format string.
            case GLOW_WHITE    -> "WHITE";
            case GLOW_YELLOW   -> "YELLOW";
            case GLOW_GREEN    -> "GREEN";
            case GLOW_AQUA     -> "AQUA";
            case GLOW_RED      -> "RED";
            case GLOW_BLUE     -> "BLUE";
            case GLOW_PINK     -> "LIGHT_PURPLE";
            case GLOW_ORANGE   -> "GOLD";
            case GLOW_PURPLE   -> "DARK_PURPLE";
            case GLOW_DARK_RED -> "DARK_RED";
            case GLOW_DARK_BLUE-> "DARK_BLUE";
            case GLOW_BLACK    -> "BLACK";
            case GLOW_GOLD     -> "GOLD";
            case GLOW_DARK_GREEN -> "DARK_GREEN";
            // Tier-3 glow: prismatic cycles — handled separately by GlowManager
            case GLOW_PRISMATIC -> "PRISMATIC";
            case GLOW_INFERNO   -> "INFERNO";
            case GLOW_VOID_DARK -> "VOID";
            default -> "{msg}";
        };
    }

    /**
     * For GLOW cosmetics: returns the Bukkit ChatColor name used for the scoreboard team.
     * Returns null for non-glow rewards.
     */
    public String getGlowColor() {
        if (!isGlow()) return null;
        return getCosmeticFormat();
    }
}
