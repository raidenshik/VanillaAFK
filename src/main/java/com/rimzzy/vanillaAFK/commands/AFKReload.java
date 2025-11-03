package com.rimzzy.vanillaAFK.commands;

import co.aikar.commands.BaseCommand;
import co.aikar.commands.annotation.CommandAlias;
import co.aikar.commands.annotation.CommandPermission;
import co.aikar.commands.annotation.Default;
import co.aikar.commands.annotation.Description;
import com.rimzzy.vanillaAFK.managers.ConfigManager;
import net.kyori.adventure.platform.bukkit.BukkitAudiences;
import net.kyori.adventure.text.Component;
import org.bukkit.entity.Player;

@CommandAlias("afk-reload")
@CommandPermission("vanillaafk.reload")
public class AFKReload extends BaseCommand {

    private final ConfigManager configManager;
    private final BukkitAudiences adventure;

    public AFKReload(ConfigManager configManager, BukkitAudiences adventure) {
        this.configManager = configManager;
        this.adventure = adventure;
    }

    @Default
    @Description("Перезагрузить конфигурацию")
    public void onReload(Player player) {
        configManager.reloadConfig();
        Component message = configManager.getMessage("messages.config-reloaded");
        adventure.player(player).sendMessage(message);
    }
}