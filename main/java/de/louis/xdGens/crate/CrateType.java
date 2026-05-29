package de.louis.xdGens.crate;

import org.bukkit.Material;

/**
 * The 5 crate tiers, ordered from most common (COMMON) to rarest (LEGENDARY).
 *
 * dropWeight  – relative weight used when deciding WHICH crate a key opens.
 *               Higher = more likely to be selected when multiple keys are in
 *               rotation. Currently every key is tied to a specific tier, so
 *               this field is mainly used