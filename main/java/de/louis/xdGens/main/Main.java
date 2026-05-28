package de.louis.xdGens.main;

import de.louis.xdGens.command.PrestigeCommand;
import de.louis.xdGens.command.TestFieldCommand;
import de.louis.xdGens.command.WorkstationCommand;
import de.louis.xdGens.field.FieldManager;
import de.louis.xdGens.listener.*;
import de.louis.xdGens.manager.*;

import de.louis.xdGens.util.MessageUtil;
import de.louis.xdGens.workstation.WorkstationManager;
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
    private LobbyProtectionListener lobbyProtectionListener;
    private HoeUpgradeManager hoeUpgradeManager;
    @Override
    public void onEnable() {
        instance = this;

        saveDefaultConfig();

        this.currencyManager = new CurrencyManager(this);
        this.fieldManager = new FieldManager(this);
        this.hologramManager = new de.louis.xdGens.hologram.HologramManager(this);
        this.workstationManager = new WorkstationManager(this, hologramManager);
        this.actionBarManager = new ActionBarManager(this);
        this.scoreboardManager = new ScoreboardManager(this);
        this.progressionManager = new ProgressionManager(this);
        this.lobbyProtectionListener = new LobbyProtectionListener(this);
        this.hoeUpgradeManager = new HoeUpgradeManager(this);


        getServer().getPluginManager().registerEvents(new FieldListener(this), this);
        getServer().getPluginManager().registerEvents(new DropListener(), this);
        getServer().getPluginManager().registerEvents(new JoinListener(this), this);
        getServer().getPluginManager().registerEvents(new WorkstationListener(this, workstationManager), this);
        getServer().getPluginManager().registerEvents(new HoeProtectionListener(this), this);
        getServer().getPluginManager().registerEvents(lobbyProtectionListener, this);
        getServer().getPluginManager().registerEvents(new HoeUpgradeListener(this), this);


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

        if (getCommand("prestige") != null) {
            getCommand("prestige").setExecutor(new PrestigeCommand(this));
        }

        getLogger().info(MessageUtil.strip(MessageUtil.PREFIX + " <green>Plugin enabled.</green>"));
    }

    @Override
    public void onDisable() {
        if (currencyManager != null) {
            currencyManager.saveAll();
        }

        if (progressionManager != null) {
            progressionManager.saveAll();
        }

        if (workstationManager != null) {
            workstationManager.removeAll();
        }

        if (actionBarManager != null) {
            actionBarManager.stop();
        }
        if (hoeUpgradeManager != null) hoeUpgradeManager.saveAll();

        getLogger().info("xdGens disabled.");
    }

    public static Main get() {
        return instance;
    }

    public FieldManager getFieldManager() {
        return fieldManager;
    }

    public CurrencyManager getCurrencyManager() {
        return currencyManager;
    }

    public de.louis.xdGens.hologram.HologramManager getHologramManager() {
        return hologramManager;
    }

    public WorkstationManager getWorkstationManager() {
        return workstationManager;
    }

    public ActionBarManager getActionBarManager() {
        return actionBarManager;
    }

    public ScoreboardManager getScoreboardManager() {
        return scoreboardManager;
    }

    public ProgressionManager getProgressionManager() {
        return progressionManager;
    }
    public HoeUpgradeManager getHoeUpgradeManager() {
        return hoeUpgradeManager;
    }
}