// Обновленный OptimizedPlayerListener.java
package com.rimzzy.vanillaAFK.listeners;

import com.rimzzy.vanillaAFK.managers.AFKManager;
import com.rimzzy.vanillaAFK.managers.ConfigManager;
import org.bukkit.Location;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerMoveEvent;
import org.bukkit.event.player.PlayerQuitEvent;
import org.bukkit.scheduler.BukkitRunnable;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

public class OptimizedPlayerListener implements Listener {

    private final AFKManager afkManager;
    private final ConfigManager configManager;
    private final com.rimzzy.vanillaAFK.VanillaAFK plugin;
    private int tickCounter = 0;
    private int soundTickCounter = 0;

    public OptimizedPlayerListener(AFKManager afkManager, ConfigManager configManager, com.rimzzy.vanillaAFK.VanillaAFK plugin) {
        this.afkManager = afkManager;
        this.configManager = configManager;
        this.plugin = plugin;
        startOptimizedScheduler();
    }

    private void startOptimizedScheduler() {
        new BukkitRunnable() {
            @Override
            public void run() {
                tickCounter++;
                soundTickCounter++;
                List<Player> afkPlayers = getOnlineAFKPlayers();

                for (Player player : afkPlayers) {
                    if (tickCounter % 2 == 0) {
                        afkManager.updateDisplayPositions(player);
                    }

                    if (tickCounter % 20 == 0) {
                        afkManager.updateActionBar(player);
                    }

                    if (tickCounter % configManager.getInt("settings.sandclock-interval") == 0) {
                        afkManager.updateSandclock(player);
                    }
                }

                int soundInterval = afkManager.getSoundInterval();
                if (soundInterval > 0 && soundTickCounter % soundInterval == 0) {
                    for (Player player : afkPlayers) {
                        afkManager.playAFKSound(player);
                    }
                }

                if (tickCounter >= 100) {
                    tickCounter = 0;
                }

                if (soundTickCounter >= 2400) { // 2 минуты максимум для звукового счетчика
                    soundTickCounter = 0;
                }
            }
        }.runTaskTimer(plugin, 1L, 1L);
    }

    private List<Player> getOnlineAFKPlayers() {
        List<Player> onlinePlayers = new ArrayList<>();
        for (UUID playerId : afkManager.getAFKPlayers()) {
            Player player = plugin.getServer().getPlayer(playerId);
            if (player != null && player.isOnline()) {
                onlinePlayers.add(player);
            }
        }
        return onlinePlayers;
    }

    @EventHandler
    public void onPlayerMove(PlayerMoveEvent event) {
        Player player = event.getPlayer();

        if (!afkManager.isAFK(player)) {
            return;
        }

        Location from = event.getFrom();
        Location to = event.getTo();

        boolean hasMoved = Math.abs(from.getX() - to.getX()) > 0.000001 ||
                Math.abs(from.getY() - to.getY()) > 0.000001 ||
                Math.abs(from.getZ() - to.getZ()) > 0.000001 ||
                from.getWorld() != to.getWorld();

        if (hasMoved) {
            afkManager.removeAFK(player);
        }
    }

    @EventHandler
    public void onPlayerQuit(PlayerQuitEvent event) {
        Player player = event.getPlayer();

        if (afkManager.isAFK(player)) {
            afkManager.removeAFKSilently(player);
        }
    }
}