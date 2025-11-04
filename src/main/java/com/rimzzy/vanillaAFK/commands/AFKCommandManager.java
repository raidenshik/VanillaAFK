package com.rimzzy.vanillaAFK.commands;

import co.aikar.commands.PaperCommandManager;
import com.rimzzy.vanillaAFK.managers.AFKManager;
import com.rimzzy.vanillaAFK.managers.ConfigManager;
import net.kyori.adventure.platform.bukkit.BukkitAudiences;
import org.bukkit.plugin.java.JavaPlugin;

import java.util.List;

public class AFKCommandManager {

    private final PaperCommandManager commandManager;
    private final AFKCommands afkCommands;
    private final AFKReload afkReloadCommand;
    private final ConfigManager configManager;

    public AFKCommandManager(AFKManager afkManager, ConfigManager configManager, BukkitAudiences adventure) {
        this.commandManager = new PaperCommandManager((JavaPlugin) afkManager.getPlugin());
        this.afkCommands = new AFKCommands(afkManager, configManager, adventure);
        this.afkReloadCommand = new AFKReload(configManager, adventure);
        this.configManager = configManager;

        configureCommandManager();
    }

    private void configureCommandManager() {
        commandManager.getCommandCompletions().registerCompletion("afkcommands", c -> {
            if (c.getPlayer().hasPermission(configManager.getString("permissions.reload"))) {
                return List.of("<текст>");
            }
            return List.of("<текст>");
        });

        commandManager.usePerIssuerLocale(true);
    }

    public void registerCommands() {
        commandManager.registerCommand(afkCommands);
        commandManager.registerCommand(afkReloadCommand);

        commandManager.getCommandContexts().registerContext(String.class, context ->
                String.join(" ", context.getArgs())
        );
    }
}