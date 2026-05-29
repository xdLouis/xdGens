package de.louis.xdGens.main;

import de.louis.xdGens.command.*;
import de.louis.xdGens.field.FieldManager;
import de.louis.xdGens.listener.CrateListener;
import de.louis.xdGens.listener.DropListener;
import de.louis.xdGens.listener.FieldListener;
import de.louis.xdGens.listener.HoeProtectionListener;
import de.louis.xdGens.listener.HoeUpgradeListener;
import de.louis.xdGens.listener.JoinListener;
import de.louis.xdGens.listener.LobbyProtectionListener;
import de.louis.xdGens.listener.ShopListener;
import de.louis.xdGens.listener.WorkstationListener;
import de.louis.xdGens.manager.ActionBarManager;
import de.louis.xdGens.manager.BackpackManager;
import de.louis.xdGens.manager.CrateManager;
import de.louis.xdGens.manager.CurrencyManager;
import de.louis.xdGens.manager.HoeUpgradeManager;
import de.louis.xdGens.manager.PlayerCosmeticManager;
import de.louis.xdGens.manager.ProgressionManager;
import de.louis.xdGens.manager.ScoreboardManager;
import de.louis.xdGens.manager.VirtualKeyManager;
import de.louis.xdGens.manager.WorkstationManager;
import de.louis.xdGens.util.MessageUtil;
import org.bukkit.plugin.java.JavaPlugin;

public final class Main extends JavaPlugin {

    private static Main instance;

    private FieldManager fieldManager;
    private CurrencyManager currencyManager;
    private de.louis.xdGens.hologram.HologramManager hologramManager;
    private WorkstationManager workstationManager;
    private ActionBarManager actionBarManager;
    private ScoreboardManager scoreboardManager;
    private ProgressionManager progressionManager;
    private HoeUpgradeManager hoeUpgradeManager;
    private BackpackManager backpackManager;
    private CrateManager crateManager;
    private VirtualKeyManager virtualKeyManager;
    private PlayerCosmeticManager playerCosmeticManager;
    private LobbyProtectionListener lobbyProtectionListener;

    @Override
    public void onEnable() {
        instance = this;

        saveDefaultConfig();

        this.currencyManager       = new CurrencyManager(this);
        this.fieldManager          = new FieldManager(this);
        this.hologramManager       = new de.louis.xdGens.hologram.HologramManager(this);
        this.workstationManager    = new WorkstationManager(this, hologramManager);
        this.actionBarManager      = new ActionBarManager(this);
        this.scoreboardManager     = new ScoreboardManager(this);
        this.progressionManager    = new ProgressionManager(this);
        this.hoeUpgradeManager     = new HoeUpgradeManager(this);
        this.backpackManager       = new BackpackManager(this);
        this.virtualKeyManager     = new VirtualKeyManager(this);
        this.playerCosmeticManager = new PlayerCosmeticManager(this);
        this.crateManager          = new CrateManager(this);
        this.lobbyProtectionListener = new LobbyProtectionListener(this);

        getServer().getPluginManager().registerEvents(new FieldListener(this), this);
        getServer().getPluginManager().registerEvents(new DropListener(), this);
        getServer().getPluginManager().registerEvents(new JoinListener(this), this);
        getServer().getPluginManager().registerEvents(new WorkstationListener(this, workstationManager), this);
        getServer().getPluginManager().registerEvents(new HoeProtectionListener(this), this);
        getServer().getPluginManager().registerEvents(new HoeUpgradeListener(this), this);
        getServer().getPluginManager().registerEvents(new ShopListener(this), this);
        getServer().getPluginManager().registerEvents(new CrateListener(this), this);
        getServer().getPluginManager().registerEvents(lobbyProtectionListener, this);

        lobbyProtectionListener.applyToAllWorlds();

        getServer().getScheduler().runTaskTimer(this, () ->
                getServer().getWorlds().forEach(world -> {
                    world.setTime(1000);
                    world.setStorm(false);
                    world.setThundering(false);
                }), 0L, 200L);

        if (getCommand("testfield") != null) {
            TestFieldCommand testFieldCommand = new TestFieldCommand();
            getCommand("testfield").setExecutor(testFieldCommand);
            getCommand("testfield").setTabCompleter(testFieldCommand);
        }

        if (getCommand("workstation") != null) {
            getCommand("workstation").setExecutor(new WorkstationCommand(this));
        }

        if (getCommand("xdadmin") != null) {
            XdAdminCommand xdAdminCommand = new XdAdminCommand(this);
            getCommand("xdadmin").setExecutor(xdAdminCommand);
            getCommand("xdadmin").setTabCompleter(xdAdminCommand);
        }

        if (getCommand("prestige") != null) {
            getCommand("prestige").setExecutor(new PrestigeCommand(this));
        }

        if (getCommand("shop") != null) {
            getCommand("shop").setExecutor(new ShopCommand(this));
        }

        if (getCommand("sell") != null) {
            getCommand("sell").setExecutor(new SellCommand(this));
        }

        if (getCommand("diamond") != null) {
            getCommand("diamond").setExecutor(new DiamondCommand());
        }

        if (getCommand("crates") != null) {
            getCommand("crates").setExecutor(new CratesCommand(this));
        }

        getLogger().info(MessageUtil.strip(MessageUtil.PREFIX + " <green>Plugin enabled.</green>"));
    }

    @Override
    public void onDisable() {
        if (currencyManager       != null) currencyManager.saveAll();
        if (progressionManager    != null) progressionManager.saveAll();
        if (hoeUpgradeManager     != null) hoeUpgradeManager.saveAll();
        if (backpackManager       != null) backpackManager.saveAll();
        if (workstationManager    != null) workstationManager.removeAll();
        if (actionBarManager      != null) actionBarManager.stop();
        if (virtualKeyManager     != null) virtualKeyManager.save();
        if (playerCosmeticManager != null) playerCosmeticManager.save();
        getLogger().info("xdGens disabled.");
    }

    public static Main get()                                      { return instance; }
    public FieldManager getFieldManager()                         { return fieldManager; }
    public CurrencyManager getCurrencyManager()                   { return currencyManager; }
    public de.louis.xdGens.hologram.HologramManager getHologramManager() { return hologramManager; }
    public WorkstationManager getWorkstationManager()             { return workstationManager; }
    public ActionBarManager getActionBarManager()                 { return actionBarManager; }
    public ScoreboardManager getScoreboardManager()               { return scoreboardManager; }
    public ProgressionManager getProgressionManager()             { return progressionManager; }
    public HoeUpgradeManager getHoeUpgradeManager()               { return hoeUpgradeManager; }
    public BackpackManager getBackpackManager()                   { return backpackManager; }
    public CrateManager getCrateManager()                         { return crateManager; }
    public VirtualKeyManager getVirtualKeyManager()               { return virtualKeyManager; }
    public PlayerCosmeticManager getPlayerCosmeticManager()       { return playerCosmeticManager; }
}
