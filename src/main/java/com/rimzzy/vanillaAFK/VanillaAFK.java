package com.rimzzy.vanillaAFK;

import com.rimzzy.vanillaAFK.commands.AFKCommand;
import com.rimzzy.vanillaAFK.listeners.PlayerListener;
import com.rimzzy.vanillaAFK.managers.AFKManager;
import com.rimzzy.vanillaAFK.managers.ConfigManager;
import org.bukkit.plugin.java.JavaPlugin;

public final class VanillaAFK extends JavaPlugin {

    private AFKManager afkManager;
    private ConfigManager configManager;

    @Override
    public void onEnable() {
        this.configManager = new ConfigManager(this);
        this.afkManager = new AFKManager(this, configManager);

        getCommand("afk").setExecutor(new AFKCommand(afkManager, configManager));
        getServer().getPluginManager().registerEvents(new PlayerListener(afkManager, configManager, this), this);

        getLogger().info("VanillaAFK успешно запущен!");
    }

    @Override
    public void onDisable() {
        if (afkManager != null) {
            afkManager.cleanup();
        }
    }
}