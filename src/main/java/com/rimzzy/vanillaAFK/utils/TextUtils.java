package com.rimzzy.vanillaAFK.utils;

import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.minimessage.MiniMessage;

public class TextUtils {

    private static final MiniMessage MINI_MESSAGE = MiniMessage.miniMessage();

    public static String removeColorCodes(String text) {
        return text.replaceAll("&[0-9a-fk-or]", "");
    }

    public static Component parseFormattedText(String text) {
        if (text == null || text.isEmpty()) {
            return Component.empty();
        }

        String miniMessageText = text.replace("&0", "<black>")
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

        try {
            return MINI_MESSAGE.deserialize(miniMessageText);
        } catch (Exception e) {
            return MINI_MESSAGE.deserialize("<white>" + text);
        }
    }
}