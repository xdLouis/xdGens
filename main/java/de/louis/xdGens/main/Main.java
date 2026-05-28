package de.louis.xdGens.main;

import de.louis.xdGens.field.FieldManager;
import de.louis.xdGens.listener.FieldListener;
import de.louis.xdGens.listener.DropListener;
import de.louis.xdGens.manager.CurrencyManager;
import de.louis.xdGens.util.MessageUtil;
import org.bukkit.plugin.java.JavaPlugin;

public final class Main extends JavaPlugin {

    private static Main instance;
    private FieldManager fieldManager;
    private CurrencyManager currencyManager;

    @Override
    public void onEnable() {
        instance = this;

        saveDefaultConfig();

        this.currencyManager = new CurrencyManager(this);
        this.fieldManager = new FieldManager(this);

        getServer().getPluginManager().registerEvents(new FieldListener(this), this);
        getServer().getPluginManager().registerEvents(new DropListener(), this);

        getLogger().info(MessageUtil.strip(MessageUtil.GRADIENT_PREFIX + " Plugin enabled!"));
    }

    @Override
    public void onDisable() {
        if (currencyManager != null) currencyManager.saveAll();
        getLogger().info("xdGens disabled.");
    }

    public static Main get() { return instance; }
    public FieldManager getFieldManager() { return fieldManager; }
    public CurrencyManager getCurrencyManager() { return currencyManager; }
}
