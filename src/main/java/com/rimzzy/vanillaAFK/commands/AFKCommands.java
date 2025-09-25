package com.rimzzy.vanillaAFK.commands;

import co.aikar.commands.BaseCommand;
import co.aikar.commands.annotation.*;
import com.rimzzy.vanillaAFK.managers.AFKManager;
import com.rimzzy.vanillaAFK.managers.ConfigManager;
import net.kyori.adventure.platform.bukkit.BukkitAudiences;
import net.kyori.adventure.text.Component;
import org.bukkit.entity.Player;

@CommandAlias("afk")
@CommandPermission("vanillaafk.use")
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
    public void onAFK(Player player) {
        if (afkManager.isAFK(player)) {
            afkManager.removeAFK(player);
            sendMessage(player, "messages.afk-disabled");
        } else {
            afkManager.setAFK(player, null);
        }
    }

    @Subcommand("текст")
    @CommandPermission("vanillaafk.customtext")
    @Description("Войти в AFK с кастомным текстом")
    @Syntax("<текст>")
    public void onAFKText(Player player, String text) {
        String plainText = removeColorCodes(text);
        if (plainText.length() > configManager.getMaxCustomTextLength()) {
            sendMessage(player, "messages.custom-text-too-long");
            return;
        }

        afkManager.setAFK(player, text);
    }

    @Subcommand("reload")
    @CommandPermission("vanillaafk.reload")
    @Description("Перезагрузить конфигурацию")
    public void onReload(Player player) {
        configManager.reloadConfig();
        sendMessage(player, "messages.config-reloaded");
    }

    private void sendMessage(Player player, String messageKey) {
        Component message = configManager.getMessage(messageKey);
        adventure.player(player).sendMessage(message);
    }

    private String removeColorCodes(String text) {
        return text.replaceAll("&[0-9a-fk-or]", "");
    }
}