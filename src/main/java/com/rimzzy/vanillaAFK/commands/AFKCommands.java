package com.rimzzy.vanillaAFK.commands;

import com.rimzzy.vanillaAFK.utils.TextUtils;
import co.aikar.commands.BaseCommand;
import co.aikar.commands.annotation.*;
import com.rimzzy.vanillaAFK.managers.AFKManager;
import com.rimzzy.vanillaAFK.managers.ConfigManager;
import net.kyori.adventure.platform.bukkit.BukkitAudiences;
import net.kyori.adventure.text.Component;
import org.bukkit.entity.Player;

@CommandAlias("afk")
public class AFKCommands extends BaseCommand {

    private final AFKManager afkManager;
    private final ConfigManager configManager;
    private final BukkitAudiences adventure;

    public AFKCommands(AFKManager afkManager, ConfigManager configManager, BukkitAudiences adventure) {
        this.afkManager = afkManager;
        this.configManager = configManager;
        this.adventure = adventure;
    }

    @Default
    @Description("Войти/выйти из AFK режима")
    @CommandCompletion("@nothing")
    public void onAFK(Player player) {
        if (!player.hasPermission(configManager.getString("permissions.use"))) {
            player.sendMessage("Нет прав! Обратитесь к Администратору!");
            return;
        }

        if (afkManager.isAFK(player)) {
            afkManager.removeAFK(player);
        } else {
            afkManager.setAFK(player, null);
        }
    }

    @Default
    @CommandCompletion("<текст>")
    @Description("Войти в AFK с кастомным текстом")
    public void onAFKText(Player player, String text) {
        String plainText = TextUtils.removeColorCodes(text);
        if (plainText.length() > configManager.getMaxCustomTextLength()) {
            sendMessage(player, "messages.custom-text-too-long");
            return;
        }

        if (!player.hasPermission("vanillaafk.customtext")) {
            sendMessage(player, "messages.no-permission");
            return;
        }

        afkManager.setAFK(player, text);
    }

    private void sendMessage(Player player, String messageKey) {
        Component message = configManager.getMessage(messageKey);
        adventure.player(player).sendMessage(message);
    }

    private String removeColorCodes(String text) {
        return text.replaceAll("&[0-9a-fk-or]", "");
    }
}