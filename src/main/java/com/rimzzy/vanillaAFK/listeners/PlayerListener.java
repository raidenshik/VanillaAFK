package com.rimzzy.vanillaAFK.listeners;

import com.rimzzy.vanillaAFK.managers.AFKManager;
import com.rimzzy.vanillaAFK.managers.ConfigManager;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerMoveEvent;
import org.bukkit.event.player.PlayerQuitEvent;
import org.bukkit.scheduler.BukkitRunnable;

public class PlayerListener implements Listener {

    private final AFKManager afkManager;
    private final ConfigManager configManager;
    private final com.rimzzy.vanillaAFK.VanillaAFK plugin;

    public PlayerListener(AFKManager afkManager, ConfigManager configManager, com.rimzzy.vanillaAFK.VanillaAFK plugin) {
        this.afkManager = afkManager;
        this.configManager = configManager;
        this.plugin = plugin;
        startSchedulers();
    }

    private void startSchedulers() {
        new BukkitRunnable() {
            @Override
            public void run() {
                for (Player player : Bukkit.getOnlinePlayers()) {
                    if (afkManager.isAFK(player)) {
                        afkManager.updateArmorStandPositions(player);
                    }
                }
            }
        }.runTaskTimer(plugin, 0L, 5L);

        new BukkitRunnable() {
            @Override
            public void run() {
                for (Player player : Bukkit.getOnlinePlayers()) {
                    if (afkManager.isAFK(player)) {
                        afkManager.updateActionBar(player);
                    }
                }
            }
        }.runTaskTimer(plugin, 0L, configManager.getInt("settings.action-bar-interval"));

        new BukkitRunnable() {
            @Override
            public void run() {
                for (Player player : Bukkit.getOnlinePlayers()) {
                    if (afkManager.isAFK(player)) {
                        afkManager.updateSandclock(player);
                    }
                }
            }
        }.runTaskTimer(plugin, 0L, configManager.getInt("settings.sandclock-interval"));
    }

    @EventHandler
    public void onPlayerMove(PlayerMoveEvent event) {
        Player player = event.getPlayer();

        if (afkManager.isAFK(player) && afkManager.hasMoved(player)) {
            afkManager.removeAFK(player);
            player.sendMessage(configManager.getMessage("messages.afk-disabled"));
        }
    }

    @EventHandler
    public void onPlayerQuit(PlayerQuitEvent event) {
        Player player = event.getPlayer();

        if (afkManager.isAFK(player)) {
            afkManager.removeAFK(player);
        }
    }
}