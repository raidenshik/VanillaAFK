package com.rimzzy.vanillaAFK.commands;

import com.rimzzy.vanillaAFK.managers.AFKManager;
import com.rimzzy.vanillaAFK.managers.ConfigManager;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabCompleter;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;

public class AFKCommand implements CommandExecutor, TabCompleter {

    private final AFKManager afkManager;
    private final ConfigManager configManager;

    public AFKCommand(AFKManager afkManager, ConfigManager configManager) {
        this.afkManager = afkManager;
        this.configManager = configManager;
    }

    @Override
    public boolean onCommand(@NotNull CommandSender sender, @NotNull Command command, @NotNull String label, String[] args) {
        if (args.length > 0 && args[0].equalsIgnoreCase("reload")) {
            if (!sender.hasPermission("vanillaafk.reload")) {
                sender.sendMessage(configManager.getMessage("messages.no-permission"));
                return true;
            }

            configManager.reloadConfig();
            sender.sendMessage(configManager.getMessage("messages.config-reloaded"));
            return true;
        }

        if (!(sender instanceof Player player)) {
            sender.sendMessage("Эта команда только для игроков!");
            return true;
        }

        if (afkManager.isAFK(player) && args.length == 0) {
            removeAFK(player);
            return true;
        }

        if (args.length > 0) {
            String customMessage = String.join(" ", args);

            String plainText = removeColorCodes(customMessage);
            if (plainText.length() > 55) {
                player.sendMessage(configManager.getMessage("messages.custom-text-too-long"));
                return true;
            }

            if (!player.hasPermission("vanillaafk.customtext")) {
                player.sendMessage(configManager.getMessage("messages.no-permission"));
                setAFK(player, null);
                return true;
            }

            boolean wasAFK = afkManager.setAFK(player, customMessage);
            String rawMessage = afkManager.getRawAFKMessage(player);

            if (wasAFK) {
                player.sendMessage(configManager.getMessageWithText("messages.afk-updated", rawMessage));
            } else {
                player.sendMessage(configManager.getMessageWithText("messages.afk-enabled", rawMessage));
            }
        } else {
            boolean wasAFK = afkManager.setAFK(player, null);
            String rawMessage = afkManager.getRawAFKMessage(player);

            if (wasAFK) {
                player.sendMessage(configManager.getMessageWithText("messages.afk-updated", rawMessage));
            } else {
                player.sendMessage(configManager.getMessageWithText("messages.afk-enabled", rawMessage));
            }
        }

        return true;
    }

    private String removeColorCodes(String text) {
        return text.replace("&0", "")
                .replace("&1", "")
                .replace("&2", "")
                .replace("&3", "")
                .replace("&4", "")
                .replace("&5", "")
                .replace("&6", "")
                .replace("&7", "")
                .replace("&8", "")
                .replace("&9", "")
                .replace("&a", "")
                .replace("&b", "")
                .replace("&c", "")
                .replace("&d", "")
                .replace("&e", "")
                .replace("&f", "")
                .replace("&k", "")
                .replace("&l", "")
                .replace("&m", "")
                .replace("&n", "")
                .replace("&o", "")
                .replace("&r", "");
    }

    private void setAFK(Player player, String customMessage) {
        boolean wasAFK = afkManager.setAFK(player, customMessage);
        String rawMessage = afkManager.getRawAFKMessage(player);

        if (wasAFK) {
            player.sendMessage(configManager.getMessageWithText("messages.afk-updated", rawMessage));
        } else {
            player.sendMessage(configManager.getMessageWithText("messages.afk-enabled", rawMessage));
        }
    }

    private void removeAFK(Player player) {
        afkManager.removeAFK(player);
        player.sendMessage(configManager.getMessage("messages.afk-disabled"));
    }

    @Override
    public @Nullable List<String> onTabComplete(@NotNull CommandSender sender, @NotNull Command command, @NotNull String alias, @NotNull String[] args) {
        if (args.length == 1) {
            if (sender.hasPermission("vanillaafk.reload")) {
                return Arrays.asList("Текст для афк", "reload");
            }
            return List.of("Текст для афк");
        }
        return Collections.emptyList();
    }
}