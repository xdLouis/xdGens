package de.louis.xdGens.listener;

import de.louis.xdGens.field.FieldManager;
import de.louis.xdGens.main.Main;
import de.louis.xdGens.manager.CurrencyManager;
import de.louis.xdGens.util.MessageUtil;
import de.louis.xdGens.util.Rng;
import org.bukkit.GameMode;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.block.data.Ageable;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.event.block.BlockFromToEvent;
import org.bukkit.event.block.MoistureChangeEvent;

public class FieldListener implements Listener {

    private final Main plugin;
    private final FieldManager fieldManager;
    private final CurrencyManager currency;

    public FieldListener(Main plugin) {
        this.plugin       = plugin;
        this.fieldManager = plugin.getFieldManager();
        this.currency     = plugin.getCurrencyManager();
    }

    // ── Weizen ernten ──────────────────────────────────────────────────────────

    @EventHandler(priority = EventPriority.HIGH, ignoreCancelled = true)
    public void onBlockBreak(BlockBreakEvent event) {
        Player player = event.getPlayer();
        Block  block  = event.getBlock();

        // Nur im Feld
        if (!fieldManager.isInField(block.getLocation())) return;

        // Nur Weizen-Blöcke
        if (block.getType() != Material.WHEAT) return;

        // Nur mit Hacke (jede Art)
        Material hand = player.getInventory().getItemInMainHand().getType();
        if (!isHoe(hand)) {
            event.setCancelled(true);
            MessageUtil.send(player,
                MessageUtil.ERROR + "Benutze eine Hacke zum Ernten!" + MessageUtil.CLOSE);
            return;
        }

        // Wachstum prüfen – nur vollständig gewachsener Weizen
        Ageable ageable = (Ageable) block.getBlockData();
        if (ageable.getAge() < ageable.getMaximumAge()) {
            event.setCancelled(true);
            MessageUtil.send(player,
                MessageUtil.INFO + "Der Weizen ist noch nicht vollständig gewachsen." + MessageUtil.CLOSE);
            return;
        }

        // Drops unterdrücken
        event.setDropItems(false);
        event.setExpToDrop(0);

        // Reward berechnen
        double money  = 0;
        int    tokens = 0;
        int    gems   = 0;

        // Konfigurierbare Rewards aus config.yml
        double moneyMin  = plugin.getConfig().getDouble("rewards.wheat.money.min",  5.0);
        double moneyMax  = plugin.getConfig().getDouble("rewards.wheat.money.max", 20.0);
        int    tokenMin  = plugin.getConfig().getInt(   "rewards.wheat.tokens.min",  1);
        int    tokenMax  = plugin.getConfig().getInt(   "rewards.wheat.tokens.max",  5);
        double gemChance = plugin.getConfig().getDouble("rewards.wheat.gems.chance", 0.05);
        int    gemMin    = plugin.getConfig().getInt(   "rewards.wheat.gems.min",    1);
        int    gemMax    = plugin.getConfig().getInt(   "rewards.wheat.gems.max",    2);

        money  = Rng.between(moneyMin, moneyMax);
        tokens = Rng.between(tokenMin, tokenMax);
        if (Rng.chance(gemChance)) gems = Rng.between(gemMin, gemMax);

        // Direkt ins Inventar – Weizen-Item
        if (player.getGameMode() != GameMode.CREATIVE) {
            player.getInventory().addItem(new org.bukkit.inventory.ItemStack(Material.WHEAT));
        }

        // Währungen gutschreiben
        currency.addMoney(player,  money);
        currency.addTokens(player, tokens);
        if (gems > 0) currency.addGems(player, gems);

        // Nachricht
        MessageUtil.sendHarvestReward(player, money, tokens, gems);

        // Weizen nachwachsen lassen
        Bukkit_scheduleRegrow(block);
    }

    // ── Farmland nie austrocknen lassen ───────────────────────────────────────

    @EventHandler(ignoreCancelled = true)
    public void onMoistureChange(MoistureChangeEvent event) {
        if (fieldManager.isInField(event.getBlock().getLocation())) {
            event.setCancelled(true);
        }
    }

    // ── Wasser darf Farmland nicht wegspülen ──────────────────────────────────

    @EventHandler(ignoreCancelled = true)
    public void onBlockFromTo(BlockFromToEvent event) {
        if (fieldManager.isInField(event.getToBlock().getLocation())) {
            event.setCancelled(true);
        }
    }

    // ── Hilfsmethoden ─────────────────────────────────────────────────────────

    private boolean isHoe(Material mat) {
        return mat == Material.WOODEN_HOE
            || mat == Material.STONE_HOE
            || mat == Material.IRON_HOE
            || mat == Material.GOLDEN_HOE
            || mat == Material.DIAMOND_HOE
            || mat == Material.NETHERITE_HOE;
    }

    /**
     * Lässt den Weizen nach kurzer Verzögerung nachwachsen.
     * Delay aus config (Standard: 100 Ticks = 5 Sekunden).
     */
    private void Bukkit_scheduleRegrow(Block block) {
        long delay = plugin.getConfig().getLong("field.regrow-delay-ticks", 100L);
        org.bukkit.Bukkit.getScheduler().runTaskLater(plugin, () -> {
            if (block.getType() == Material.AIR || block.getType() == Material.WHEAT) {
                block.setType(Material.WHEAT);
                FieldManager.setWheatFullyGrown(block);
                FieldManager.moisturizeFarmland(block.getRelative(0, -1, 0));
            }
        }, delay);
    }
}
