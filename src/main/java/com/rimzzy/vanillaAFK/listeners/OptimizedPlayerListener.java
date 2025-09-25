package com.rimzzy.vanillaAFK.listeners;

import com.rimzzy.vanillaAFK.managers.AFKManager;
import com.rimzzy.vanillaAFK.managers.ConfigManager;
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
                List<Player> afkPlayers = getOnlineAFKPlayers();

                for (Player player : afkPlayers) {
                    if (tickCounter % 2 == 0) {
                        afkManager.updateDisplayPositions(player);
                    }

                    if (tickCounter % configManager.getInt("settings.action-bar-interval") == 0) {
                        afkManager.updateActionBar(player);
                    }

                    if (tickCounter % configManager.getInt("settings.sandclock-interval") == 0) {
                        afkManager.updateSandclock(player);
                    }
                }

                if (tickCounter >= 100) {
                    tickCounter = 0;
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
        if (event.getFrom().getBlockX() == event.getTo().getBlockX() &&
                event.getFrom().getBlockY() == event.getTo().getBlockY() &&
                event.getFrom().getBlockZ() == event.getTo().getBlockZ()) {
            return;
        }

        Player player = event.getPlayer();

        if (afkManager.isAFK(player) && afkManager.hasMoved(player)) {
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