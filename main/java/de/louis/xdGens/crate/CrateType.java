package de.louis.xdGens.crate;

import org.bukkit.Material;

public enum CrateType {

    COMMON("Common", "<gradient:#cfd9df:#e2ebf0>", Material.CHEST, 0.55),
    UNCOMMON("Uncommon", "<gradient:#a8ff78:#78ffd6>", Material.BARREL, 0.24),
    RARE("Rare", "<gradient:#7afcff:#00c2ff>", Material.ENDER_CHEST, 0.12),
    EPIC("Epic", "<gradient:#c471f5:#fa71cd>", Material.AMETHYST_CLUSTER, 0.06),
    LEGENDARY("Legendary", "<gradient:#f6d365:#fda085>", Material.NETHER_STAR, 0.03);

    private final String displayName;
    private final String gradient;
    private final Material icon;
    private final double weight;

    CrateType(String displayName, String gradient, Material icon, double weight) {
        this.displayName = displayName;
        this.gradient = gradient;
        this.icon = icon;
        this.weight = weight;
    }

    public String getDisplayName() {
        return displayName;
    }

    public String getGradient() {
        return gradient;
    }

    public Material getIcon() {
        return icon;
    }

    public double getWeight() {
        return weight;
    }
}
