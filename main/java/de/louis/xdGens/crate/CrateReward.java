package de.louis.xdGens.crate;

import org.bukkit.Material;

/**
 * Tier values:  0 = COMMON  |  1 = RARE  |  2 = EPIC  |  3 = LEGENDARY
 *
 * Weight guide:
 *   T0 COMMON     0.05 – 0.09
 *   T1 RARE       0.03 – 0.08
 *   T2 EPIC       0.005 – 0.012   (roughly 4-5× rarer than Common)
 *   T3 LEGENDARY  0.001 – 0.004   (roughly 3× rarer than Epic)
 *
 * Categories:
 *   TAG        — chat prefix tag
 *   NAME_COLOR — player name color / gradient
 *   CHAT_COLOR — chat message color
 *   GLOW       — entity glow effect (solid color or special cycling)
 */
public enum CrateReward {

    // ── economy pouches (not cosmetics) ──────────────────────────────
    MONEY  ("Money Pouch",  Material.SUNFLOWER,         1.00, 0, Type.POUCH),
    XP     ("XP Pouch",     Material.EXPERIENCE_BOTTLE, 1.00, 0, Type.POUCH),
    TOKENS ("Token Pouch",  Material.GOLD_INGOT,        1.00, 0, Type.POUCH),

    // ════════════════════════════════════════════════════════════════
    //  T A G S
    // ════════════════════════════════════════════════════════════════

    // ── tier 0 (COMMON) ──────────────────────────────────────────────
    TAG_FARMER    ("[Farmer]",       Material.WHEAT,              0.09, 0, Type.TAG),
    TAG_HUNTER    ("[Hunter]",       Material.BOW,                0.09, 0, Type.TAG),
    TAG_MINER     ("[Miner]",        Material.IRON_PICKAXE,       0.09, 0, Type.TAG),
    TAG_BUILDER   ("[Builder]",      Material.BRICKS,             0.09, 0, Type.TAG),
    TAG_FISHER    ("[Fisher]",       Material.FISHING_ROD,        0.08, 0, Type.TAG),
    TAG_WOODCUTTER("[Woodcutter]",   Material.OAK_LOG,            0.08, 0, Type.TAG),
    TAG_SHEPHERD  ("[Shepherd]",     Material.WHITE_WOOL,         0.08, 0, Type.TAG),
    TAG_DIGGER    ("[Digger]",       Material.IRON_SHOVEL,        0.08, 0, Type.TAG),
    TAG_COOK      ("\uD83C\uDF73 Cook",        Material.COOKED_BEEF,        0.07, 0, Type.TAG),
    TAG_BEEKEEPER ("\uD83D\uDC1D Beekeeper",   Material.HONEYCOMB,          0.07, 0, Type.TAG),
    TAG_NINJA     ("\u26F9 Ninja",       Material.LEATHER_BOOTS,      0.07, 0, Type.TAG),
    TAG_PIRATE    ("\uD83C\uDFF4\u200D\u2620\uFE0F Pirate",    Material.NAUTILUS_SHELL,     0.07, 0, Type.TAG),
    TAG_WANDERER  ("[Wanderer]",     Material.MAP,                0.06, 0, Type.TAG),
    TAG_HERBALIST ("\uD83C\uDF3F Herbalist",   Material.FERN,               0.06, 0, Type.TAG),
    TAG_ARCHER    ("[Archer]",       Material.ARROW,              0.06, 0, Type.TAG),
    TAG_LIBRARIAN ("\uD83D\uDCDA Librarian",   Material.BOOK,               0.06, 0, Type.TAG),
    TAG_HEALER    ("\u2695 Healer",       Material.GOLDEN_APPLE,       0.06, 0, Type.TAG),
    TAG_SAILOR    ("\u2693 Sailor",      Material.NAUTILUS_SHELL,     0.05, 0, Type.TAG),
    TAG_RANGER    ("[Ranger]",       Material.OAK_SAPLING,        0.05, 0, Type.TAG),
    TAG_HERBGATH  ("\uD83C\uDF30 Gatherer",   Material.SWEET_BERRIES,      0.05, 0, Type.TAG),
    TAG_BAKER     ("\uD83E\uDD56 Baker",      Material.BREAD,              0.05, 0, Type.TAG),
    TAG_SHEPHERD2 ("\uD83D\uDC11 Herder",     Material.SHEARS,             0.05, 0, Type.TAG),
    TAG_POTTERER  ("\uD83C\uDFFA Potter",     Material.FLOWER_POT,         0.05, 0, Type.TAG),
    TAG_SCHOLAR   ("[Scholar]",      Material.WRITABLE_BOOK,      0.05, 0, Type.TAG),
    TAG_STONECUTTER("[Stonecutter]", Material.STONE_BRICKS,       0.05, 0, Type.TAG),
    TAG_FLETCHER  ("[Fletcher]",     Material.FLETCHING_TABLE,    0.05, 0, Type.TAG),
    TAG_TANNER    ("[Tanner]",       Material.LEATHER,            0.05, 0, Type.TAG),
    TAG_BUTCHER   ("[Butcher]",      Material.PORKCHOP,          0.05, 0, Type.TAG),
    TAG_CARTOGRAPH("[Cartographer]", Material.FILLED_MAP,         0.05, 0, Type.TAG),
    TAG_CLERIC    ("\u271E Cleric",       Material.NETHER_WART,        0.05, 0, Type.TAG),
    TAG_TOOLSMITH ("[Toolsmith]",    Material.IRON_PICKAXE,       0.05, 0, Type.TAG),
    TAG_WEAPSMITH ("[Weaponsmith]",  Material.IRON_SWORD,         0.05, 0, Type.TAG),

    // ── tier 1 (RARE) ────────────────────────────────────────────────
    TAG_MERCHANT  ("[Merchant]",     Material.EMERALD,            0.08, 1, Type.TAG),
    TAG_EXPLORER  ("[Explorer]",     Material.COMPASS,            0.08, 1, Type.TAG),
    TAG_GUARDIAN  ("[Guardian]",     Material.SHIELD,             0.07, 1, Type.TAG),
    TAG_WARRIOR   ("[Warrior]",      Material.IRON_SWORD,         0.07, 1, Type.TAG),
    TAG_TRADER    ("[Trader]",       Material.VILLAGER_SPAWN_EGG, 0.06, 1, Type.TAG),
    TAG_SCOUT     ("[Scout]",        Material.SPYGLASS,           0.06, 1, Type.TAG),
    TAG_ALCHEMIST ("[Alchemist]",    Material.BREWING_STAND,      0.05, 1, Type.TAG),
    TAG_KNIGHT    ("[Knight]",       Material.CHAINMAIL_HELMET,   0.05, 1, Type.TAG),
    TAG_BLACKSMITH("[Blacksmith]",   Material.ANVIL,              0.04, 1, Type.TAG),

    // ── tier 2 (EPIC) ────────────────────────────────────────────────
    TAG_LEGEND     ("[Legend]",        Material.NETHER_STAR,        0.012, 2, Type.TAG),
    TAG_CELESTIAL  ("[Celestial]",     Material.CRYING_OBSIDIAN,    0.010, 2, Type.TAG),
    TAG_ANCIENT    ("[Ancient]",       Material.ANCIENT_DEBRIS,     0.010, 2, Type.TAG),
    TAG_DIVINE     ("[Divine]",        Material.BEACON,             0.008, 2, Type.TAG),
    TAG_PHANTOM    ("[Phantom]",       Material.PHANTOM_MEMBRANE,   0.008, 2, Type.TAG),
    TAG_WARDEN     ("[Warden]",        Material.SCULK_SENSOR,       0.008, 2, Type.TAG),
    TAG_DRAGON     ("\u2746 Dragon \u2746",     Material.DRAGON_EGG,         0.008, 2, Type.TAG),
    TAG_SHADOW     ("\uD83C\uDF11 Shadow",      Material.COAL,               0.007, 2, Type.TAG),
    TAG_INFERNO    ("\uD83D\uDD25 Inferno",     Material.BLAZE_ROD,          0.007, 2, Type.TAG),
    TAG_SPECTER    ("\uD83D\uDC7B Specter",     Material.SOUL_LANTERN,       0.007, 2, Type.TAG),
    TAG_ARCANE     ("\u2728 Arcane",       Material.AMETHYST_SHARD,     0.007, 2, Type.TAG),
    TAG_TITAN      ("[\uD83D\uDDFF TITAN]",     Material.COBBLESTONE,        0.006, 2, Type.TAG),
    TAG_REAPER     ("\u2620 Reaper",       Material.WITHER_SKELETON_SKULL, 0.006, 2, Type.TAG),
    TAG_STORM      ("\u26A1 Storm",        Material.LIGHTNING_ROD,      0.006, 2, Type.TAG),
    TAG_ENDER      ("[Ender]",         Material.ENDER_EYE,          0.006, 2, Type.TAG),
    TAG_FROST      ("\u2744 Frost",        Material.PACKED_ICE,         0.008, 2, Type.TAG),
    TAG_DEMON      ("\uD83D\uDC79 Demon",       Material.SOUL_SAND,          0.008, 2, Type.TAG),
    TAG_PLAGUE     ("\u2623 Plague",       Material.POISONOUS_POTATO,   0.007, 2, Type.TAG),
    TAG_TEMPLAR    ("[Templar]",       Material.GOLDEN_SWORD,       0.007, 2, Type.TAG),
    TAG_BERSERKER  ("\uD83D\uDDE1 Berserker",   Material.NETHERITE_AXE,      0.007, 2, Type.TAG),
    TAG_CURSED     ("\uD83D\uDC80 Cursed",      Material.WITHER_ROSE,        0.006, 2, Type.TAG),
    TAG_ORACLE     ("\uD83D\uDD2E Oracle",       Material.AMETHYST_CLUSTER,   0.006, 2, Type.TAG),
    TAG_UNDEAD     ("\uD83E\uDDDF Undead",      Material.ROTTEN_FLESH,       0.006, 2, Type.TAG),

    // ── tier 3 (LEGENDARY) ───────────────────────────────────────────
    TAG_GOD        ("\u26A1 GOD \u26A1",        Material.TOTEM_OF_UNDYING,   0.003, 3, Type.TAG),
    TAG_ETERNAL    ("\u262F Eternal \u262F",    Material.END_CRYSTAL,        0.003, 3, Type.TAG),
    TAG_OVERLORD   ("\uD83D\uDC51 Overlord \uD83D\uDC51",  Material.NETHERITE_INGOT,    0.002, 3, Type.TAG),
    TAG_VOID       ("\u258C VOID\u2590",        Material.OBSIDIAN,           0.002, 3, Type.TAG),
    TAG_COSMOS     ("\uD83C\uDF0C COSMOS \uD83C\uDF0C",  Material.SPORE_BLOSSOM,      0.002, 3, Type.TAG),
    TAG_ABYSS_LORD ("\uD83D\uDD2E Abyss Lord",  Material.NETHERITE_BLOCK,    0.002, 3, Type.TAG),
    TAG_NEXUS      ("\u2735 NEXUS \u2735",       Material.CONDUIT,            0.002, 3, Type.TAG),
    TAG_PRIMORDIAL ("\uD83C\uDF0A Primordial",  Material.SPONGE,             0.002, 3, Type.TAG),
    TAG_ASCENDED   ("\uD83D\uDD1F Ascended \uD83D\uDD1F", Material.NETHER_STAR,        0.002, 3, Type.TAG),
    TAG_SOVEREIGN  ("\u2654 SOVEREIGN",    Material.GOLDEN_HELMET,      0.001, 3, Type.TAG),
    TAG_DEVOURER   ("\uD83D\uDC32 Devourer",    Material.DRAGON_BREATH,      0.001, 3, Type.TAG),

    // ════════════════════════════════════════════════════════════════
    //  N A M E   C O L O R S
    // ════════════════════════════════════════════════════════════════

    // ── tier 0 (COMMON) ──────────────────────────────────────────────
    COLOR_AQUA         ("Aqua",          Material.CYAN_DYE,           0.09, 0, Type.NAME_COLOR),
    COLOR_GOLD         ("Gold",          Material.GOLD_NUGGET,        0.09, 0, Type.NAME_COLOR),
    COLOR_GREEN        ("Green",         Material.LIME_DYE,           0.09, 0, Type.NAME_COLOR),
    COLOR_YELLOW       ("Yellow",        Material.YELLOW_DYE,         0.08, 0, Type.NAME_COLOR),
    COLOR_RED          ("Red",           Material.RED_DYE,            0.08, 0, Type.NAME_COLOR),
    COLOR_WHITE        ("White",         Material.WHITE_DYE,          0.08, 0, Type.NAME_COLOR),
    COLOR_BLUE         ("Blue",          Material.BLUE_DYE,           0.07, 0, Type.NAME_COLOR),
    COLOR_ORANGE       ("Orange",        Material.ORANGE_DYE,         0.07, 0, Type.NAME_COLOR),
    COLOR_PINK         ("Pink",          Material.PINK_DYE,           0.07, 0, Type.NAME_COLOR),
    COLOR_DARK_RED     ("Dark Red",      Material.RED_TERRACOTTA,     0.07, 0, Type.NAME_COLOR),
    COLOR_GRAY         ("Gray",          Material.GRAY_DYE,           0.06, 0, Type.NAME_COLOR),
    COLOR_LIME         ("Lime",          Material.LIME_CONCRETE,      0.06, 0, Type.NAME_COLOR),
    COLOR_CORAL        ("Coral",         Material.BRAIN_CORAL,        0.06, 0, Type.NAME_COLOR),
    COLOR_TEAL         ("Teal",          Material.CYAN_CONCRETE,      0.06, 0, Type.NAME_COLOR),
    COLOR_MINT         ("Mint",          Material.SEA_PICKLE,         0.06, 0, Type.NAME_COLOR),
    COLOR_LAVENDER     ("Lavender",      Material.AMETHYST_SHARD,     0.05, 0, Type.NAME_COLOR),

    // ── tier 1 (RARE) ────────────────────────────────────────────────
    COLOR_LIGHT_PURPLE ("Light Purple",  Material.PURPLE_DYE,         0.08, 1, Type.NAME_COLOR),
    COLOR_DARK_AQUA    ("Dark Aqua",     Material.CYAN_TERRACOTTA,    0.06, 1, Type.NAME_COLOR),
    COLOR_DARK_GREEN   ("Dark Green",    Material.GREEN_DYE,          0.06, 1, Type.NAME_COLOR),
    COLOR_DARK_PURPLE  ("Dark Purple",   Material.MAGENTA_DYE,        0.05, 1, Type.NAME_COLOR),

    // ── tier 2 (EPIC) ────────────────────────────────────────────────
    COLOR_GRADIENT_FIRE    ("\uD83D\uDD25 Fire",      Material.BLAZE_POWDER,       0.012, 2, Type.NAME_COLOR),
    COLOR_GRADIENT_OCEAN   ("\uD83C\uDF0A Ocean",     Material.PRISMARINE_SHARD,   0.012, 2, Type.NAME_COLOR),
    COLOR_GRADIENT_NATURE  ("\uD83C\uDF3F Nature",    Material.OAK_LEAVES,         0.010, 2, Type.NAME_COLOR),
    COLOR_GRADIENT_GALAXY  ("\uD83C\uDF0C Galaxy",    Material.AMETHYST_SHARD,     0.010, 2, Type.NAME_COLOR),
    COLOR_GRADIENT_SUNSET  ("\uD83C\uDF05 Sunset",    Material.ORANGE_DYE,         0.010, 2, Type.NAME_COLOR),
    COLOR_GRADIENT_ICE     ("\u2744 Ice",       Material.BLUE_ICE,           0.010, 2, Type.NAME_COLOR),
    COLOR_GRADIENT_TOXIC   ("\u2623 Toxic",     Material.SLIME_BALL,         0.008, 2, Type.NAME_COLOR),
    COLOR_GRADIENT_SHADOW  ("\uD83C\uDF11 Shadow",    Material.COAL,               0.008, 2, Type.NAME_COLOR),
    COLOR_GRADIENT_ROSE    ("\uD83C\uDF39 Rose",      Material.PINK_TULIP,         0.008, 2, Type.NAME_COLOR),
    COLOR_GRADIENT_BLOOD   ("\uD83E\uDE78 Blood",     Material.REDSTONE,           0.008, 2, Type.NAME_COLOR),
    COLOR_GRADIENT_LAVA    ("\uD83C\uDF0B Lava",      Material.LAVA_BUCKET,        0.007, 2, Type.NAME_COLOR),
    COLOR_GRADIENT_AURORA  ("\uD83C\uDF0C Aurora",    Material.SEA_LANTERN,        0.007, 2, Type.NAME_COLOR),
    COLOR_GRADIENT_CANDY   ("\uD83C\uDF6C Candy",     Material.PINK_DYE,           0.010, 2, Type.NAME_COLOR),
    COLOR_GRADIENT_THUNDER ("\u26A1 Thunder",   Material.LIGHTNING_ROD,      0.008, 2, Type.NAME_COLOR),
    COLOR_GRADIENT_FOREST  ("\uD83C\uDF33 Forest",    Material.MOSS_BLOCK,         0.008, 2, Type.NAME_COLOR),
    COLOR_GRADIENT_DEEP_SEA("\uD83D\uDC20 Deep Sea",  Material.HEART_OF_THE_SEA,   0.007, 2, Type.NAME_COLOR),
    COLOR_GRADIENT_MAGMA   ("\uD83D\uDD25 Magma",     Material.MAGMA_BLOCK,        0.007, 2, Type.NAME_COLOR),
    COLOR_GRADIENT_NEON    ("\uD83D\uDFE2 Neon",      Material.LIME_STAINED_GLASS, 0.007, 2, Type.NAME_COLOR),
    COLOR_GRADIENT_SAKURA  ("\uD83C\uDF38 Sakura",    Material.CHERRY_LEAVES,      0.007, 2, Type.NAME_COLOR),
    COLOR_GRADIENT_COPPER  ("\uD83E\uDD16 Copper",    Material.COPPER_INGOT,       0.006, 2, Type.NAME_COLOR),

    // ── tier 3 (LEGENDARY) ───────────────────────────────────────────
    COLOR_GRADIENT_RAINBOW  ("\uD83C\uDF08 Rainbow",  Material.NETHER_STAR,        0.004, 3, Type.NAME_COLOR),
    COLOR_GRADIENT_DIVINE   ("\u2728 Divine",   Material.END_CRYSTAL,        0.003, 3, Type.NAME_COLOR),
    COLOR_GRADIENT_ABYSS    ("\u26AB Abyss",    Material.NETHERITE_SCRAP,    0.002, 3, Type.NAME_COLOR),
    COLOR_GRADIENT_COSMOS   ("\uD83C\uDF0C Cosmos",   Material.SPORE_BLOSSOM,      0.002, 3, Type.NAME_COLOR),
    COLOR_GRADIENT_VOID_FIRE("\uD83D\uDD25\u26AB Void Fire", Material.SOUL_TORCH,  0.002, 3, Type.NAME_COLOR),
    COLOR_GRADIENT_SPECTRAL ("\uD83D\uDC7D Spectral",  Material.GHAST_TEAR,         0.002, 3, Type.NAME_COLOR),
    COLOR_GRADIENT_SOLAR    ("\u2600 Solar",    Material.SHROOMLIGHT,        0.002, 3, Type.NAME_COLOR),
    COLOR_GRADIENT_ABYSSAL  ("\uD83D\uDEF0 Abyssal",  Material.CONDUIT,            0.001, 3, Type.NAME_COLOR),

    // ════════════════════════════════════════════════════════════════
    //  C H A T   C O L O R S
    // ════════════════════════════════════════════════════════════════

    // ── tier 0 (COMMON) ──────────────────────────────────────────────
    CHAT_WHITE        ("White Chat",    Material.WHITE_CONCRETE,     0.09, 0, Type.CHAT_COLOR),
    CHAT_YELLOW       ("Yellow Chat",   Material.YELLOW_CONCRETE,    0.08, 0, Type.CHAT_COLOR),
    CHAT_GREEN        ("Green Chat",    Material.LIME_CONCRETE,      0.08, 0, Type.CHAT_COLOR),
    CHAT_AQUA         ("Aqua Chat",     Material.CYAN_CONCRETE,      0.07, 0, Type.CHAT_COLOR),
    CHAT_GOLD         ("Gold Chat",     Material.ORANGE_CONCRETE,    0.07, 0, Type.CHAT_COLOR),
    CHAT_RED          ("Red Chat",      Material.RED_CONCRETE,       0.06, 0, Type.CHAT_COLOR),
    CHAT_BLUE         ("Blue Chat",     Material.BLUE_CONCRETE,      0.06, 0, Type.CHAT_COLOR),
    CHAT_LIGHT_PURPLE ("Purple Chat",   Material.PURPLE_CONCRETE,    0.06, 0, Type.CHAT_COLOR),
    CHAT_PINK         ("Pink Chat",     Material.PINK_CONCRETE,      0.05, 0, Type.CHAT_COLOR),
    CHAT_ORANGE       ("Orange Chat",   Material.ORANGE_CONCRETE,    0.05, 0, Type.CHAT_COLOR),
    CHAT_DARK_GREEN   ("Dark Green Chat",  Material.GREEN_CONCRETE,  0.07, 0, Type.CHAT_COLOR),
    CHAT_DARK_AQUA    ("Dark Aqua Chat",   Material.CYAN_TERRACOTTA, 0.06, 0, Type.CHAT_COLOR),
    CHAT_GRAY         ("Gray Chat",        Material.GRAY_CONCRETE,   0.05, 0, Type.CHAT_COLOR),
    CHAT_LIME         ("Lime Chat",        Material.LIME_DYE,        0.05, 0, Type.CHAT_COLOR),
    CHAT_TEAL         ("Teal Chat",        Material.CYAN_DYE,        0.05, 0, Type.CHAT_COLOR),
    CHAT_CORAL        ("Coral Chat",       Material.BRAIN_CORAL,     0.04, 0, Type.CHAT_COLOR),

    // ── tier 1 (RARE) ────────────────────────────────────────────────
    CHAT_DARK_PURPLE  ("Dark Purple Chat", Material.PURPLE_TERRACOTTA,0.05, 1, Type.CHAT_COLOR),
    CHAT_DARK_RED     ("Dark Red Chat",    Material.RED_TERRACOTTA,  0.05, 1, Type.CHAT_COLOR),

    // ── tier 2 (EPIC) ────────────────────────────────────────────────
    CHAT_G_FIRE    ("\uD83D\uDD25 Fire Chat",      Material.BLAZE_ROD,          0.012, 2, Type.CHAT_COLOR),
    CHAT_G_OCEAN   ("\uD83C\uDF0A Ocean Chat",     Material.HEART_OF_THE_SEA,   0.010, 2, Type.CHAT_COLOR),
    CHAT_G_GALAXY  ("\uD83C\uDF0C Galaxy Chat",    Material.AMETHYST_CLUSTER,   0.010, 2, Type.CHAT_COLOR),
    CHAT_G_NATURE  ("\uD83C\uDF3F Nature Chat",    Material.FERN,               0.010, 2, Type.CHAT_COLOR),
    CHAT_G_ICE     ("\u2744 Ice Chat",       Material.PACKED_ICE,         0.008, 2, Type.CHAT_COLOR),
    CHAT_G_SHADOW  ("\uD83C\uDF11 Shadow Chat",    Material.NETHERRACK,         0.008, 2, Type.CHAT_COLOR),
    CHAT_G_ROSE    ("\uD83C\uDF39 Rose Chat",      Material.PINK_TULIP,         0.008, 2, Type.CHAT_COLOR),
    CHAT_G_TOXIC   ("\u2623 Toxic Chat",     Material.SLIME_BALL,         0.008, 2, Type.CHAT_COLOR),
    CHAT_G_LAVA    ("\uD83C\uDF0B Lava Chat",      Material.LAVA_BUCKET,        0.007, 2, Type.CHAT_COLOR),
    CHAT_G_STORM   ("\u26A1 Storm Chat",    Material.LIGHTNING_ROD,      0.007, 2, Type.CHAT_COLOR),
    CHAT_G_CANDY   ("\uD83C\uDF6C Candy Chat",     Material.PINK_DYE,           0.010, 2, Type.CHAT_COLOR),
    CHAT_G_FOREST  ("\uD83C\uDF33 Forest Chat",    Material.MOSS_BLOCK,         0.008, 2, Type.CHAT_COLOR),
    CHAT_G_NEON    ("\uD83D\uDFE2 Neon Chat",      Material.LIME_STAINED_GLASS, 0.008, 2, Type.CHAT_COLOR),
    CHAT_G_SAKURA  ("\uD83C\uDF38 Sakura Chat",    Material.CHERRY_LEAVES,      0.007, 2, Type.CHAT_COLOR),
    CHAT_G_THUNDER ("\u26A1 Thunder Chat",  Material.LIGHTNING_ROD,      0.007, 2, Type.CHAT_COLOR),
    CHAT_G_DEEP_SEA("\uD83D\uDC20 Deep Sea Chat", Material.HEART_OF_THE_SEA,   0.007, 2, Type.CHAT_COLOR),
    CHAT_G_MAGMA   ("\uD83D\uDD25 Magma Chat",     Material.MAGMA_BLOCK,        0.007, 2, Type.CHAT_COLOR),
    CHAT_G_COPPER  ("\uD83E\uDD16 Copper Chat",    Material.COPPER_INGOT,       0.006, 2, Type.CHAT_COLOR),

    // ── tier 3 (LEGENDARY) ───────────────────────────────────────────
    CHAT_G_RAINBOW ("\uD83C\uDF08 Rainbow Chat",  Material.NETHER_STAR,        0.004, 3, Type.CHAT_COLOR),
    CHAT_G_DIVINE  ("\u2728 Divine Chat",   Material.TOTEM_OF_UNDYING,   0.003, 3, Type.CHAT_COLOR),
    CHAT_G_VOID    ("\u26AB Void Chat",     Material.OBSIDIAN,           0.002, 3, Type.CHAT_COLOR),
    CHAT_G_COSMOS  ("\uD83C\uDF0C Cosmos Chat",   Material.SPORE_BLOSSOM,      0.002, 3, Type.CHAT_COLOR),
    CHAT_G_ABYSSAL ("\uD83D\uDEF0 Abyssal Chat",  Material.CONDUIT,            0.002, 3, Type.CHAT_COLOR),
    CHAT_G_SOLAR   ("\u2600 Solar Chat",    Material.SHROOMLIGHT,        0.002, 3, Type.CHAT_COLOR),
    CHAT_G_SPECTRAL("\uD83D\uDC7D Spectral Chat", Material.GHAST_TEAR,         0.001, 3, Type.CHAT_COLOR),

    // ════════════════════════════════════════════════════════════════
    //  G L O W
    // ════════════════════════════════════════════════════════════════

    // ── tier 0 (COMMON) ──────────────────────────────────────────────
    GLOW_WHITE    ("White Glow",        Material.WHITE_STAINED_GLASS,   0.09, 0, Type.GLOW),
    GLOW_YELLOW   ("Yellow Glow",       Material.YELLOW_STAINED_GLASS,  0.09, 0, Type.GLOW),
    GLOW_GREEN    ("Green Glow",        Material.LIME_STAINED_GLASS,    0.08, 0, Type.GLOW),
    GLOW_RED      ("Red Glow",          Material.RED_STAINED_GLASS,     0.08, 0, Type.GLOW),
    GLOW_AQUA     ("Aqua Glow",         Material.CYAN_STAINED_GLASS,    0.07, 0, Type.GLOW),
    GLOW_BLUE     ("Blue Glow",         Material.BLUE_STAINED_GLASS,    0.07, 0, Type.GLOW),
    GLOW_PINK     ("Pink Glow",         Material.PINK_STAINED_GLASS,    0.07, 0, Type.GLOW),
    GLOW_ORANGE   ("Orange Glow",       Material.ORANGE_STAINED_GLASS,  0.06, 0, Type.GLOW),
    GLOW_GRAY     ("Gray Glow",         Material.GRAY_STAINED_GLASS,    0.06, 0, Type.GLOW),
    GLOW_LIGHT_BLUE("Light Blue Glow",  Material.LIGHT_BLUE_STAINED_GLASS, 0.06, 0, Type.GLOW),
    GLOW_LIME     ("Lime Glow",         Material.LIME_CONCRETE,         0.05, 0, Type.GLOW),
    GLOW_MAGENTA  ("Magenta Glow",      Material.MAGENTA_STAINED_GLASS, 0.05, 0, Type.GLOW),
    GLOW_BROWN    ("Brown Glow",        Material.BROWN_STAINED_GLASS,   0.05, 0, Type.GLOW),

    // ── tier 1 (RARE) ────────────────────────────────────────────────
    GLOW_PURPLE    ("Purple Glow",      Material.PURPLE_STAINED_GLASS,  0.04, 1, Type.GLOW),
    GLOW_DARK_GREEN("Dark Green Glow",  Material.GREEN_STAINED_GLASS,   0.04, 1, Type.GLOW),
    GLOW_DARK_GRAY ("Dark Gray Glow",   Material.GRAY_CONCRETE,         0.04, 1, Type.GLOW),
    GLOW_CYAN      ("Cyan Glow",        Material.CYAN_CONCRETE,         0.04, 1, Type.GLOW),

    // ── tier 2 (EPIC) ────────────────────────────────────────────────
    GLOW_DARK_RED  ("Dark Red Glow",    Material.CRIMSON_STEM,          0.010, 2, Type.GLOW),
    GLOW_DARK_BLUE ("Dark Blue Glow",   Material.BLUE_TERRACOTTA,       0.010, 2, Type.GLOW),
    GLOW_BLACK     ("Black Glow",       Material.BLACK_STAINED_GLASS,   0.008, 2, Type.GLOW),
    GLOW_GOLD      ("\uD83D\uDC51 Gold Glow",    Material.GOLD_BLOCK,            0.008, 2, Type.GLOW),
    GLOW_CRIMSON   ("\u2665 Crimson Glow",  Material.CRIMSON_NYLIUM,     0.008, 2, Type.GLOW),
    GLOW_NEON_GREEN("\uD83D\uDFE2 Neon Glow",   Material.SLIME_BLOCK,           0.007, 2, Type.GLOW),
    GLOW_SOUL      ("\uD83D\uDC9C Soul Glow",     Material.SOUL_LANTERN,          0.007, 2, Type.GLOW),

    // ── tier 3 (LEGENDARY) ───────────────────────────────────────────
    GLOW_PRISMATIC ("\uD83C\uDF08 Prismatic Glow", Material.NETHER_STAR,          0.004, 3, Type.GLOW),
    GLOW_INFERNO   ("\uD83D\uDD25 Inferno Glow",    Material.BLAZE_POWDER,         0.003, 3, Type.GLOW),
    GLOW_VOID_DARK ("\u26AB Void Glow",             Material.OBSIDIAN,             0.002, 3, Type.GLOW),
    GLOW_AURORA    ("\uD83C\uDF0C Aurora Glow",     Material.SEA_LANTERN,          0.002, 3, Type.GLOW),
    GLOW_DIVINE    ("\u2728 Divine Glow",           Material.END_CRYSTAL,          0.001, 3, Type.GLOW);

    // ── tier / type constants ─────────────────────────────────────────
    public static final int TIER_COMMON    = 0;
    public static final int TIER_RARE      = 1;
    public static final int TIER_EPIC      = 2;
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

    public boolean isPrismaticGlow() { return this == GLOW_PRISMATIC; }

    public String tierLabel() {
        return switch (tier) {
            case 1  -> "<gradient:#7afcff:#00c2ff>Rare</gradient>";
            case 2  -> "<gradient:#c471f5:#fa71cd>Epic</gradient>";
            case 3  -> "<gradient:#f6d365:#fda085><bold>\u2746 LEGENDARY \u2746</bold></gradient>";
            default -> "<gray>Common</gray>";
        };
    }

    // ── cosmetic formatting ──────────────────────────────────────────
    public String getCosmeticFormat() {
        return switch (this) {

            // ─────────── TAGS ─────────────────────────────────────────
            case TAG_FARMER      -> "<green><bold>[Farmer]</bold></green>";
            case TAG_HUNTER      -> "<red><bold>[Hunter]</bold></red>";
            case TAG_MINER       -> "<gray><bold>[Miner]</bold></gray>";
            case TAG_BUILDER     -> "<yellow><bold>[Builder]</bold></yellow>";
            case TAG_FISHER      -> "<dark_aqua><bold>[Fisher]</bold></dark_aqua>";
            case TAG_WOODCUTTER  -> "<dark_green><bold>[Woodcutter]</bold></dark_green>";
            case TAG_SHEPHERD    -> "<white><bold>[Shepherd]</bold></white>";
            case TAG_DIGGER      -> "<gray><bold>[Digger]</bold></gray>";
            case TAG_COOK        -> "<yellow><bold>\uD83C\uDF73 Cook</bold></yellow>";
            case TAG_BEEKEEPER   -> "<gold><bold>\uD83D\uDC1D Beekeeper</bold></gold>";
            case TAG_NINJA       -> "<dark_gray><bold>\u26F9 Ninja</bold></dark_gray>";
            case TAG_PIRATE      -> "<dark_gray><bold>\uD83C\uDFF4\u200D\u2620\uFE0F Pirate</bold></dark_gray>";
            case TAG_WANDERER    -> "<dark_purple><bold>[Wanderer]</bold></dark_purple>";
            case TAG_HERBALIST   -> "<green><bold>\uD83C\uDF3F Herbalist</bold></green>";
            case TAG_ARCHER      -> "<green><bold>[Archer]</bold></green>";
            case TAG_LIBRARIAN   -> "<aqua><bold>\uD83D\uDCDA Librarian</bold></aqua>";
            case TAG_HEALER      -> "<green><bold>\u2695 Healer</bold></green>";
            case TAG_SAILOR      -> "<aqua><bold>\u2693 Sailor</bold></aqua>";
            case TAG_RANGER      -> "<dark_green><bold>[Ranger]</bold></dark_green>";
            case TAG_HERBGATH    -> "<green><bold>\uD83C\uDF30 Gatherer</bold></green>";
            case TAG_BAKER       -> "<gold><bold>\uD83E\uDD56 Baker</bold></gold>";
            case TAG_SHEPHERD2   -> "<white><bold>\uD83D\uDC11 Herder</bold></white>";
            case TAG_POTTERER    -> "<yellow><bold>\uD83C\uDFFA Potter</bold></yellow>";
            case TAG_SCHOLAR     -> "<aqua><bold>[Scholar]</bold></aqua>";
            case TAG_STONECUTTER -> "<gray><bold>[Stonecutter]</bold></gray>";
            case TAG_FLETCHER    -> "<dark_green><bold>[Fletcher]</bold></dark_green>";
            case TAG_TANNER      -> "<dark_red><bold>[Tanner]</bold></dark_red>";
            case TAG_BUTCHER     -> "<red><bold>[Butcher]</bold></red>";
            case TAG_CARTOGRAPH  -> "<aqua><bold>[Cartographer]</bold></aqua>";
            case TAG_CLERIC      -> "<light_purple><bold>\u271E Cleric</bold></light_purple>";
            case TAG_TOOLSMITH   -> "<gray><bold>[Toolsmith]</bold></gray>";
            case TAG_WEAPSMITH   -> "<dark_red><bold>[Weaponsmith]</bold></dark_red>";
            case TAG_MERCHANT    -> "<gold><bold>[Merchant]</bold></gold>";
            case TAG_EXPLORER    -> "<aqua><bold>[Explorer]</bold></aqua>";
            case TAG_GUARDIAN    -> "<blue><bold>[Guardian]</bold></blue>";
            case TAG_WARRIOR     -> "<dark_red><bold>[Warrior]</bold></dark_red>";
            case TAG_TRADER      -> "<green><bold>[Trader]</bold></green>";
            case TAG_SCOUT       -> "<aqua><bold>[Scout]</bold></aqua>";
            case TAG_ALCHEMIST   -> "<dark_purple><bold>[Alchemist]</bold></dark_purple>";
            case TAG_KNIGHT      -> "<gray><bold>[Knight]</bold></gray>";
            case TAG_BLACKSMITH  -> "<dark_gray><bold>[Blacksmith]</bold></dark_gray>";
            case TAG_LEGEND      -> "<gradient:#f6d365:#fda085><bold>[Legend]</bold></gradient>";
            case TAG_CELESTIAL   -> "<gradient:#a18cd1:#fbc2eb><bold>[Celestial]</bold></gradient>";
            case TAG_ANCIENT     -> "<gradient:#cfd9df:#e2ebf0><bold>[Ancient]</bold></gradient>";
            case TAG_DIVINE      -> "<gradient:#fffde4:#005c97><bold>[Divine]</bold></gradient>";
            case TAG_PHANTOM     -> "<gradient:#7f7fd5:#86a8e7:#91eae4><bold>[Phantom]</bold></gradient>";
            case TAG_WARDEN      -> "<gradient:#00b4db:#0083b0><bold>[Warden]</bold></gradient>";
            case TAG_DRAGON      -> "<gradient:#c471f5:#fa71cd><bold>\u2746 Dragon \u2746</bold></gradient>";
            case TAG_SHADOW      -> "<gradient:#232526:#414345><bold>\uD83C\uDF11 Shadow</bold></gradient>";
            case TAG_INFERNO     -> "<gradient:#f83600:#fe8c00><bold>\uD83D\uDD25 Inferno</bold></gradient>";
            case TAG_SPECTER     -> "<gradient:#7f8c8d:#bdc3c7><bold>\uD83D\uDC7B Specter</bold></gradient>";
            case TAG_ARCANE      -> "<gradient:#654ea3:#eaafc8><bold>\u2728 Arcane</bold></gradient>";
            case TAG_TITAN       -> "<gradient:#636363:#a2ab58><bold>[\uD83D\uDDFF TITAN]</bold></gradient>";
            case TAG_REAPER      -> "<gradient:#000000:#434343><bold>\u2620 Reaper</bold></gradient>";
            case TAG_STORM       -> "<gradient:#373b44:#4286f4><bold>\u26A1 Storm</bold></gradient>";
            case TAG_ENDER       -> "<gradient:#6a3093:#a044ff><bold>[Ender]</bold></gradient>";
            case TAG_FROST       -> "<gradient:#74ebd5:#acb6e5><bold>\u2744 Frost</bold></gradient>";
            case TAG_DEMON       -> "<gradient:#8e0000:#ff4e00><bold>\uD83D\uDC79 Demon</bold></gradient>";
            case TAG_PLAGUE      -> "<gradient:#11998e:#38ef7d><bold>\u2623 Plague</bold></gradient>";
            case TAG_TEMPLAR     -> "<gradient:#f7971e:#ffd200><bold>[Templar]</bold></gradient>";
            case TAG_BERSERKER   -> "<gradient:#c0392b:#8e0000><bold>\uD83D\uDDE1 Berserker</bold></gradient>";
            case TAG_CURSED      -> "<gradient:#434343:#1a1a2e><bold>\uD83D\uDC80 Cursed</bold></gradient>";
            case TAG_ORACLE      -> "<gradient:#654ea3:#eaafc8><bold>\uD83D\uDD2E Oracle</bold></gradient>";
            case TAG_UNDEAD      -> "<gradient:#7f8c8d:#2d3436><bold>\uD83E\uDDDF Undead</bold></gradient>";
            case TAG_GOD         -> "<gradient:#f7971e:#ffd200><bold>\u26A1 GOD \u26A1</bold></gradient>";
            case TAG_ETERNAL     -> "<gradient:#e0eafc:#cfdef3><bold>\u262F Eternal \u262F</bold></gradient>";
            case TAG_OVERLORD    -> "<gradient:#c94b4b:#4b134f><bold>\uD83D\uDC51 Overlord \uD83D\uDC51</bold></gradient>";
            case TAG_VOID        -> "<dark_gray><bold>\u258C VOID\u2590</bold></dark_gray>";
            case TAG_COSMOS      -> "<gradient:#1a1a2e:#16213e:#0f3460:#533483><bold>\uD83C\uDF0C COSMOS \uD83C\uDF0C</bold></gradient>";
            case TAG_ABYSS_LORD  -> "<gradient:#000000:#3d0000><bold>\uD83D\uDD2E Abyss Lord</bold></gradient>";
            case TAG_NEXUS       -> "<gradient:#c471f5:#12c2e9:#f64f59><bold>\u2735 NEXUS \u2735</bold></gradient>";
            case TAG_PRIMORDIAL  -> "<gradient:#00c3ff:#0f3460:#533483><bold>\uD83C\uDF0A Primordial</bold></gradient>";
            case TAG_ASCENDED    -> "<gradient:#fffde4:#ffd200:#ff8c00><bold>\uD83D\uDD1F Ascended \uD83D\uDD1F</bold></gradient>";
            case TAG_SOVEREIGN   -> "<gradient:#c94b4b:#4b134f:#000000><bold>\u2654 SOVEREIGN</bold></gradient>";
            case TAG_DEVOURER    -> "<gradient:#8e0000:#000000><bold>\uD83D\uDC32 Devourer</bold></gradient>";

            // ─────────── NAME COLORS ───────────────────────────────────
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
            case COLOR_DARK_RED      -> "<dark_red>{name}</dark_red>";
            case COLOR_GRAY          -> "<gray>{name}</gray>";
            case COLOR_TEAL          -> "<color:#009688>{name}</color>";
            case COLOR_LIME          -> "<color:#a8ff3e>{name}</color>";
            case COLOR_CORAL         -> "<color:#ff6b6b>{name}</color>";
            case COLOR_LAVENDER      -> "<color:#b39ddb>{name}</color>";
            case COLOR_MINT          -> "<color:#98ff98>{name}</color>";
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
            case COLOR_GRADIENT_CANDY    -> "<gradient:#f953c6:#fbc2eb>{name}</gradient>";
            case COLOR_GRADIENT_THUNDER  -> "<gradient:#373b44:#4286f4><bold>{name}</bold></gradient>";
            case COLOR_GRADIENT_FOREST   -> "<gradient:#134e5e:#71b280>{name}</gradient>";
            case COLOR_GRADIENT_DEEP_SEA -> "<gradient:#0f2027:#203a43:#2c5364>{name}</gradient>";
            case COLOR_GRADIENT_MAGMA    -> "<gradient:#ff512f:#dd2476>{name}</gradient>";
            case COLOR_GRADIENT_NEON     -> "<gradient:#a8ff3e:#2fff8f>{name}</gradient>";
            case COLOR_GRADIENT_SAKURA   -> "<gradient:#ffc3a0:#ffafbd>{name}</gradient>";
            case COLOR_GRADIENT_COPPER   -> "<gradient:#b45309:#78350f>{name}</gradient>";
            case COLOR_GRADIENT_RAINBOW  -> "<gradient:#ff0000:#ff7700:#ffff00:#00ff00:#0000ff:#8b00ff>{name}</gradient>";
            case COLOR_GRADIENT_DIVINE   -> "<gradient:#fffde4:#005c97><bold>\u2728{name}\u2728</bold></gradient>";
            case COLOR_GRADIENT_ABYSS    -> "<gradient:#000000:#434343><bold>{name}</bold></gradient>";
            case COLOR_GRADIENT_COSMOS   -> "<gradient:#1a1a2e:#16213e:#0f3460:#533483>{name}</gradient>";
            case COLOR_GRADIENT_VOID_FIRE-> "<gradient:#000000:#ff4e00>{name}</gradient>";
            case COLOR_GRADIENT_SPECTRAL -> "<gradient:#f5f7fa:#c3cfe2><bold>\uD83D\uDC7D{name}</bold></gradient>";
            case COLOR_GRADIENT_SOLAR    -> "<gradient:#f7971e:#ffd200:#ff6b6b>{name}</gradient>";
            case COLOR_GRADIENT_ABYSSAL  -> "<gradient:#000428:#004e92><bold>{name}</bold></gradient>";

            // ─────────── CHAT COLORS ───────────────────────────────────
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
            case CHAT_DARK_GREEN     -> "<dark_green>{msg}</dark_green>";
            case CHAT_DARK_AQUA      -> "<dark_aqua>{msg}</dark_aqua>";
            case CHAT_DARK_PURPLE    -> "<dark_purple>{msg}</dark_purple>";
            case CHAT_GRAY           -> "<gray>{msg}</gray>";
            case CHAT_DARK_RED       -> "<dark_red>{msg}</dark_red>";
            case CHAT_LIME           -> "<color:#a8ff3e>{msg}</color>";
            case CHAT_TEAL           -> "<color:#009688>{msg}</color>";
            case CHAT_CORAL          -> "<color:#ff6b6b>{msg}</color>";
            case CHAT_G_FIRE    -> "<gradient:#f83600:#fe8c00>{msg}</gradient>";
            case CHAT_G_OCEAN   -> "<gradient:#1a6dff:#00d2ff>{msg}</gradient>";
            case CHAT_G_GALAXY  -> "<gradient:#654ea3:#eaafc8>{msg}</gradient>";
            case CHAT_G_NATURE  -> "<gradient:#56ab2f:#a8e063>{msg}</gradient>";
            case CHAT_G_ICE     -> "<gradient:#74ebd5:#acb6e5>{msg}</gradient>";
            case CHAT_G_SHADOW  -> "<gradient:#232526:#414345><bold>{msg}</bold></gradient>";
            case CHAT_G_ROSE    -> "<gradient:#f953c6:#b91d73>{msg}</gradient>";
            case CHAT_G_TOXIC   -> "<gradient:#11998e:#38ef7d>{msg}</gradient>";
            case CHAT_G_LAVA    -> "<gradient:#ff4e00:#ec9f05>{msg}</gradient>";
            case CHAT_G_STORM   -> "<gradient:#373b44:#4286f4><bold>{msg}</bold></gradient>";
            case CHAT_G_CANDY    -> "<gradient:#f953c6:#fbc2eb>{msg}</gradient>";
            case CHAT_G_FOREST   -> "<gradient:#134e5e:#71b280>{msg}</gradient>";
            case CHAT_G_NEON     -> "<gradient:#a8ff3e:#2fff8f><bold>{msg}</bold></gradient>";
            case CHAT_G_SAKURA   -> "<gradient:#ffc3a0:#ffafbd>{msg}</gradient>";
            case CHAT_G_THUNDER  -> "<gradient:#373b44:#4286f4><bold>{msg}</bold></gradient>";
            case CHAT_G_DEEP_SEA -> "<gradient:#0f2027:#203a43:#2c5364>{msg}</gradient>";
            case CHAT_G_MAGMA    -> "<gradient:#ff512f:#dd2476>{msg}</gradient>";
            case CHAT_G_COPPER   -> "<color:#b45309>{msg}</color>";
            case CHAT_G_RAINBOW  -> "<gradient:#ff0000:#ff7700:#ffff00:#00ff00:#0000ff:#8b00ff>{msg}</gradient>";
            case CHAT_G_DIVINE   -> "<gradient:#fffde4:#005c97>{msg}</gradient>";
            case CHAT_G_VOID     -> "<dark_gray><bold>{msg}</bold></dark_gray>";
            case CHAT_G_COSMOS   -> "<gradient:#1a1a2e:#16213e:#0f3460:#533483>{msg}</gradient>";
            case CHAT_G_ABYSSAL  -> "<gradient:#000428:#004e92><bold>{msg}</bold></gradient>";
            case CHAT_G_SOLAR    -> "<gradient:#f7971e:#ffd200:#ff6b6b>{msg}</gradient>";
            case CHAT_G_SPECTRAL -> "<gradient:#f5f7fa:#c3cfe2><bold>{msg}</bold></gradient>";

            // ─────────── GLOW ─────────────────────────────────────────
            case GLOW_WHITE      -> "WHITE";
            case GLOW_YELLOW     -> "YELLOW";
            case GLOW_GREEN      -> "GREEN";
            case GLOW_AQUA       -> "AQUA";
            case GLOW_RED        -> "RED";
            case GLOW_BLUE       -> "BLUE";
            case GLOW_PINK       -> "LIGHT_PURPLE";
            case GLOW_ORANGE     -> "GOLD";
            case GLOW_GRAY       -> "GRAY";
            case GLOW_LIGHT_BLUE -> "BLUE";
            case GLOW_BROWN      -> "GOLD";
            case GLOW_LIME       -> "GREEN";
            case GLOW_MAGENTA    -> "LIGHT_PURPLE";
            case GLOW_PURPLE     -> "DARK_PURPLE";
            case GLOW_DARK_RED   -> "DARK_RED";
            case GLOW_DARK_BLUE  -> "DARK_BLUE";
            case GLOW_BLACK      -> "BLACK";
            case GLOW_GOLD       -> "GOLD";
            case GLOW_DARK_GREEN -> "DARK_GREEN";
            case GLOW_DARK_GRAY  -> "DARK_GRAY";
            case GLOW_CYAN       -> "AQUA";
            case GLOW_CRIMSON    -> "DARK_RED";
            case GLOW_NEON_GREEN -> "GREEN";
            case GLOW_SOUL       -> "DARK_AQUA";
            // special cycling glows
            case GLOW_PRISMATIC  -> "PRISMATIC";
            case GLOW_INFERNO    -> "INFERNO";
            case GLOW_VOID_DARK  -> "VOID";
            case GLOW_AURORA     -> "AURORA";
            case GLOW_DIVINE     -> "DIVINE";

            default -> "{msg}";
        };
    }

    public String getGlowColor() {
        if (!isGlow()) return null;
        return getCosmeticFormat();
    }
}
