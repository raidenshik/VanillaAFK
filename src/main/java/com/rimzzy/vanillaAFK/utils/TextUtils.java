package com.rimzzy.vanillaAFK.utils;

import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.minimessage.MiniMessage;
import net.kyori.adventure.text.serializer.legacy.LegacyComponentSerializer;

import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class TextUtils {

    private static final MiniMessage MINI_MESSAGE = MiniMessage.miniMessage();

    private static final Map<Character, String> LEGACY_TO_MINI = Map.ofEntries(
            Map.entry('0', "black"),
            Map.entry('1', "dark_blue"),
            Map.entry('2', "dark_green"),
            Map.entry('3', "dark_aqua"),
            Map.entry('4', "dark_red"),
            Map.entry('5', "dark_purple"),
            Map.entry('6', "gold"),
            Map.entry('7', "gray"),
            Map.entry('8', "dark_gray"),
            Map.entry('9', "blue"),
            Map.entry('a', "green"),
            Map.entry('b', "aqua"),
            Map.entry('c', "red"),
            Map.entry('d', "light_purple"),
            Map.entry('e', "yellow"),
            Map.entry('f', "white"),
            Map.entry('k', "obfuscated"),
            Map.entry('l', "bold"),
            Map.entry('m', "strikethrough"),
            Map.entry('n', "underlined"),
            Map.entry('o', "italic"),
            Map.entry('r', "reset")
    );

    private static final Pattern LEGACY_PATTERN = Pattern.compile("&([0-9a-fk-or])", Pattern.CASE_INSENSITIVE);

    public static Component parseFormattedText(String text) {
        if (text == null || text.isEmpty()) return Component.empty();

        try {
            String miniMessageText = convertLegacyToMiniMessage(text);
            return MINI_MESSAGE.deserialize(miniMessageText);
        } catch (Exception e) {
            // Fallback
            return LegacyComponentSerializer.legacyAmpersand().deserialize(text);
        }
    }

    public static String removeColorCodes(String text) {
        if (text == null) return "";
        // Убираем legacy-коды (&x, §x) и MiniMessage-теги (<tag>)
        return text.replaceAll("(?i)&[0-9a-fk-or]", "")
                .replaceAll("§[0-9a-fk-or]", "")
                .replaceAll("<[^>]*>", "");
    }

    private static String convertLegacyToMiniMessage(String text) {
        Matcher matcher = LEGACY_PATTERN.matcher(text);
        StringBuilder stringBuilder = new StringBuilder();
        while (matcher.find()) {
            char code = Character.toLowerCase(matcher.group(1).charAt(0));
            String replacement = LEGACY_TO_MINI.getOrDefault(code, "white");
            matcher.appendReplacement(stringBuilder, "<" + replacement + ">");
        }
        matcher.appendTail(stringBuilder);
        return stringBuilder.toString();
    }
}