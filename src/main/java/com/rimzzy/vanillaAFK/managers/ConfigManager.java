package com.rimzzy.vanillaAFK.managers;

import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.minimessage.MiniMessage;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.plugin.java.JavaPlugin;

public class ConfigManager {

    private final JavaPlugin plugin;
    private final MiniMessage miniMessage;
    private FileConfiguration config;

    public ConfigManager(JavaPlugin plugin) {
        this.plugin = plugin;
        this.miniMessage = MiniMessage.miniMessage();
        loadConfig();
    }

    public void loadConfig() {
        plugin.saveDefaultConfig();
        config = plugin.getConfig();

        config.addDefault("messages.afk-enabled", "&aВы вошли в режим AFK с текстом '&f<text>&a'");
        config.addDefault("messages.afk-updated", "&aВы обновили режим AFK с текстом '&f<text>&a'");
        config.addDefault("messages.afk-disabled", "&aВы вышли из режима АФК");
        config.addDefault("messages.action-bar", "&cДвиньтесь, чтобы выйти с АФК");
        config.addDefault("messages.default-afk-text", "&7AFK");
        config.addDefault("messages.custom-text-too-long", "&cТекст не может быть длиннее 55 символов");
        config.addDefault("messages.no-permission", "&cУ вас нет прав для использования этой команды");
        config.addDefault("messages.config-reloaded", "&aКонфигурация перезагружена!");

        config.addDefault("settings.bubble-height-offset", 0.2);
        config.addDefault("settings.action-bar-interval", 10);
        config.addDefault("settings.sandclock-interval", 20);
        config.addDefault("settings.sandclock-emojis", "⏳,⌛");
        config.addDefault("settings.sandclock-enabled", true);

        config.options().copyDefaults(true);
        plugin.saveConfig();
    }

    public Component getMessage(String path) {
        String message = config.getString(path, "");
        return parseMessage(message);
    }

    public Component getMessageWithText(String path, String plainText) {
        String message = config.getString(path, "");
        return parseMessage(message.replace("<text>", plainText));
    }

    private Component parseMessage(String message) {
        String miniMessageText = convertBukkitColorsToMiniMessage(message);
        try {
            return miniMessage.deserialize(miniMessageText);
        } catch (Exception e) {
            return Component.text(message);
        }
    }

    private String convertBukkitColorsToMiniMessage(String text) {
        return text.replace("&0", "<black>")
                .replace("&1", "<dark_blue>")
                .replace("&2", "<dark_green>")
                .replace("&3", "<dark_aqua>")
                .replace("&4", "<dark_red>")
                .replace("&5", "<dark_purple>")
                .replace("&6", "<gold>")
                .replace("&7", "<gray>")
                .replace("&8", "<dark_gray>")
                .replace("&9", "<blue>")
                .replace("&a", "<green>")
                .replace("&b", "<aqua>")
                .replace("&c", "<red>")
                .replace("&d", "<light_purple>")
                .replace("&e", "<yellow>")
                .replace("&f", "<white>")
                .replace("&k", "<obfuscated>")
                .replace("&l", "<bold>")
                .replace("&m", "<strikethrough>")
                .replace("&n", "<underlined>")
                .replace("&o", "<italic>")
                .replace("&r", "<reset>");
    }

    public String getString(String path) {
        return config.getString(path, "");
    }

    public double getDouble(String path) {
        return config.getDouble(path);
    }

    public int getInt(String path) {
        return config.getInt(path);
    }

    public boolean getBoolean(String path) {
        return config.getBoolean(path);
    }

    public String[] getStringList(String path) {
        return config.getString(path, "").split(",");
    }

    public void reloadConfig() {
        plugin.reloadConfig();
        config = plugin.getConfig();
    }
}