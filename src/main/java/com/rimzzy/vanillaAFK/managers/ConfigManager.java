package com.rimzzy.vanillaAFK.managers;

import com.github.benmanes.caffeine.cache.Cache;
import com.github.benmanes.caffeine.cache.Caffeine;
import net.kyori.adventure.platform.bukkit.BukkitAudiences;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.serializer.legacy.LegacyComponentSerializer;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.plugin.java.JavaPlugin;

import java.util.Arrays;
import java.util.concurrent.TimeUnit;

public class ConfigManager {

    private final JavaPlugin plugin;
    private final BukkitAudiences adventure;
    private FileConfiguration config;

    private final Cache<String, Component> messageCache;
    private final Cache<String, String[]> listCache;

    public ConfigManager(JavaPlugin plugin) {
        this.plugin = plugin;
        this.adventure = BukkitAudiences.create(plugin);

        this.messageCache = Caffeine.newBuilder()
                .expireAfterAccess(10, TimeUnit.MINUTES)
                .maximumSize(100)
                .build();

        this.listCache = Caffeine.newBuilder()
                .expireAfterAccess(5, TimeUnit.MINUTES)
                .maximumSize(50)
                .build();

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

        // Раздельные настройки высоты для текста и песочных часов
        config.addDefault("settings.text-height-offset", 2.2);    // Высота основного текста
        config.addDefault("settings.sandclock-height-offset", 1.8); // Высота песочных часов (ниже текста)
        config.addDefault("settings.action-bar-interval", 10);
        config.addDefault("settings.sandclock-interval", 20);
        config.addDefault("settings.sandclock-emojis", "⏳,⌛");
        config.addDefault("settings.sandclock-enabled", true);

        config.options().copyDefaults(true);
        plugin.saveConfig();

        messageCache.invalidateAll();
        listCache.invalidateAll();
    }

    public Component getMessage(String path) {
        return messageCache.get(path, this::loadMessage);
    }

    public Component getMessageWithText(String path, String plainText) {
        String cacheKey = path + "|" + plainText;
        return messageCache.get(cacheKey, k -> {
            String message = config.getString(path, "");
            return parseMessage(message.replace("<text>", plainText));
        });
    }

    private Component loadMessage(String path) {
        String message = config.getString(path, "");
        return parseMessage(message);
    }

    private Component parseMessage(String message) {
        if (message == null || message.isEmpty()) {
            return Component.empty();
        }

        return LegacyComponentSerializer.legacyAmpersand().deserialize(message);
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
        return listCache.get(path, k -> {
            String value = config.getString(path, "");
            return Arrays.stream(value.split(","))
                    .map(String::trim)
                    .filter(s -> !s.isEmpty())
                    .toArray(String[]::new);
        });
    }

    public int getMaxCustomTextLength() {
        return 55;
    }

    public void reloadConfig() {
        plugin.reloadConfig();
        config = plugin.getConfig();
        messageCache.invalidateAll();
        listCache.invalidateAll();
    }
}