package com.rimzzy.vanillaAFK;

import com.rimzzy.vanillaAFK.commands.AFKCommandManager;
import com.rimzzy.vanillaAFK.listeners.OptimizedPlayerListener;
import com.rimzzy.vanillaAFK.managers.AFKManager;
import com.rimzzy.vanillaAFK.managers.ConfigManager;
import net.kyori.adventure.platform.bukkit.BukkitAudiences;
import org.bukkit.plugin.java.JavaPlugin;

public final class VanillaAFK extends JavaPlugin {

    private BukkitAudiences adventure;
    private AFKManager afkManager;
    private ConfigManager configManager;
    private AFKCommandManager commandManager;

    @Override
    public void onEnable() {
        this.adventure = BukkitAudiences.create(this);
        this.configManager = new ConfigManager(this);
        this.afkManager = new AFKManager(this, configManager, adventure);
        this.commandManager = new AFKCommandManager(afkManager, configManager, adventure);

        commandManager.registerCommands();

        getServer().getPluginManager().registerEvents(
                new OptimizedPlayerListener(afkManager, configManager, this), this
        );
    }

    @Override
    public void onDisable() {
        if (afkManager != null) {
            afkManager.cleanup();
        }
        if (adventure != null) {
            adventure.close();
        }
    }

    public BukkitAudiences getAdventure() {
        return adventure;
    }
}