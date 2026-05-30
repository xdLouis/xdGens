package de.louis.xdGens.crate;

/**
 * Five tiers of pouches — each with a display name, colour tag and value multiplier.
 * The actual value ranges are defined in CrateManager and scaled by tier.
 *
 *  T1  Common      gray
 *  T2  Uncommon    green
 *  T3  Rare        aqua
 *  T4  Epic        light_purple
 *  T5  Legendary   gradient gold
 */
public enum PouchTier {

    T1("<gray>T1 Common</gray>",      "<gray>",                              1.0),
    T2("<green>T2 Uncommon</green>",  "<green>",                             2.0),
    T3("<aqua>T3 Rare</aqua>",        "<aqua>",                              3.5),
    T4("<light_purple>T4 Epic</light_purple>", "<light_purple>",             6.0),
    T5("<gradient:#f7971e:#ffd200><bold>T5 Legendary</bold></gradient>", "<gradient:#f7971e:#ffd200>", 12.0);

    private final String displayName;   // MiniMessage display name (for lore / chat)
    private final String colorTag;      // open MiniMessage tag for inline use
    private final double multiplier;    // value multiplier vs T1 baseline

    PouchTier(String displayName, String colorTag, double multiplier) {
        this.displayName  = displayName;
        this.colorTag     = colorTag;
        this.multiplier   = multiplier;
    }

    public String getDisplayName() { return displayName; }
    public String getColorTag()    { return colorTag; }
    public double getMultiplier()  { return multiplier; }

    /** Short label used in open-all summary lines, e.g. "T5 Legendary" */
    public String shortLabel() {
        return displayName;
    }
}
