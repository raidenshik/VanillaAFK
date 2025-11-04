// ConfigManager.java - обновленный класс
package com.rimzzy.vanillaAFK.managers;

import com.github.benmanes.caffeine.cache.Cache;
import com.github.benmanes.caffeine.cache.Caffeine;
import com.rimzzy.vanillaAFK.utils.TextUtils;
import net.kyori.adventure.text.Component;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.plugin.java.JavaPlugin;

import java.util.Arrays;
import java.util.concurrent.TimeUnit;

public class ConfigManager {

    private final JavaPlugin plugin;
    private FileConfiguration config;

    private final Cache<String, Component> messageCache;
    private final Cache<String, String[]> listCache;

    public ConfigManager(JavaPlugin plugin) {
        this.plugin = plugin;

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
        config.addDefault("messages.action-bar", "<red>Двиньтесь, чтобы выйти с АФК <white>| <gray>Вы в АФК: {time}");
        config.addDefault("messages.default-afk-text", "&7AFK");
        config.addDefault("messages.custom-text-too-long", "&cТекст не может быть длиннее 55 символов");
        config.addDefault("messages.no-permission", "&cУ вас нет прав для использования этой команды");
        config.addDefault("messages.config-reloaded", "&aКонфигурация перезагружена!");
        config.addDefault("messages.afk-time-formats.less-than-minute", "{seconds}сек");
        config.addDefault("messages.afk-time-formats.less-than-hour", "{minutes}м {seconds}с");
        config.addDefault("messages.afk-time-formats.hour-or-more", "{hours}ч {minutes}м {seconds}с");

        config.addDefault("settings.text-height-offset", 2.3);
        config.addDefault("settings.sandclock-height-offset", 2.0);
        config.addDefault("settings.text-scale", 1.0);
        config.addDefault("settings.text-startscale", 0.5f);
        config.addDefault("settings.action-bar-interval", 10);
        config.addDefault("settings.sandclock-interval", 20);
        config.addDefault("settings.scale-interpolation", 5);
        config.addDefault("settings.scale-interpolation-delay", 0);
        config.addDefault("settings.scale-run-task-later-delay", 2L);
        config.addDefault("settings.sandclock-emojis", "⏳,⌛");
        config.addDefault("settings.sandclock-enabled", true);
        config.addDefault("settings.sound.enabled", true);
        config.addDefault("settings.sound.sound", "minecraft:minecraft:block.sand.step");
        config.addDefault("settings.sound.volume", 1.0);
        config.addDefault("settings.sound.pitch", 1.0);
        config.addDefault("settings.sound.interval", 20);

        config.addDefault("permissions.reload", "vanillaafk.reload");
        config.addDefault("permissions.customtext", "vanillaafk.customtext");
        config.addDefault("permissions.use", "vanillaafk.use");

        config.options().copyDefaults(true);
        plugin.saveConfig();

        messageCache.invalidateAll();
        listCache.invalidateAll();
    }

    public Component getMessage(String path) {
        return messageCache.get(path, this::loadMessage);
    }

    public Component getMessageWithText(String path, String text) {
        if (path.equals("messages.afk-enabled") || path.equals("messages.afk-updated")) {
            return getMessage(path);
        }

        String cacheKey = path + "|" + text;
        return messageCache.get(cacheKey, k -> {
            String message = config.getString(path, "");
            String formattedMessage = message.replace("<text>", text);
            return TextUtils.parseFormattedText(formattedMessage);
        });
    }

    private Component loadMessage(String path) {
        String message = config.getString(path, "");
        return TextUtils.parseFormattedText(message);
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

    public long getLong(String path) {
        return config.getLong(path);
    }

    public float getFloat(String path) {
        return (float) config.getDouble(path);
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