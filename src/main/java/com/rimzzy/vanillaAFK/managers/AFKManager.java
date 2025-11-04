package com.rimzzy.vanillaAFK.managers;

import com.rimzzy.vanillaAFK.utils.TextUtils;
import net.kyori.adventure.platform.bukkit.BukkitAudiences;
import net.kyori.adventure.text.Component;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.Sound;
import org.bukkit.World;
import org.bukkit.entity.Display;
import org.bukkit.entity.Player;
import org.bukkit.entity.TextDisplay;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.scoreboard.Scoreboard;
import org.bukkit.scoreboard.Team;
import org.bukkit.util.Transformation;
import org.joml.Vector3f;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

public class AFKManager {

    private final JavaPlugin plugin;
    private final ConfigManager configManager;
    private final BukkitAudiences adventure;
    private final Map<UUID, AFKPlayerData> afkPlayers;
    private Team afkTeam;

    public AFKManager(JavaPlugin plugin, ConfigManager configManager, BukkitAudiences adventure) {
        this.plugin = plugin;
        this.configManager = configManager;
        this.adventure = adventure;
        this.afkPlayers = new ConcurrentHashMap<>();
        setupAFKTeam();
    }

    private void setupAFKTeam() {
        Scoreboard scoreboard = Bukkit.getScoreboardManager().getMainScoreboard();
        afkTeam = scoreboard.getTeam("vanillaafk");

        if (afkTeam == null) {
            afkTeam = scoreboard.registerNewTeam("vanillaafk");
        }

        afkTeam.setOption(Team.Option.NAME_TAG_VISIBILITY, Team.OptionStatus.ALWAYS);
    }

    public boolean setAFK(Player player, String customMessage) {
        UUID playerId = player.getUniqueId();
        boolean wasAFK = isAFK(player);

        Component message;
        String rawMessage;

        if (customMessage != null && !customMessage.isEmpty()) {
            message = TextUtils.parseFormattedText(customMessage);
            rawMessage = customMessage;
        } else {
            message = Component.empty();
            rawMessage = "";
        }

        if (wasAFK) {
            AFKPlayerData data = afkPlayers.get(playerId);
            data.setMessage(message);
            data.setRawMessage(rawMessage);

            if (data.getTextDisplay() != null && !data.getTextDisplay().isDead()) {
                data.getTextDisplay().text(message);
            }

            if (player.hasPermission(configManager.getString("permissions.customtext")) && customMessage != null && !customMessage.isEmpty()) {
                sendMessage(player, "messages.afk-updated-with-text", rawMessage);
            } else {
                sendMessage(player, "messages.afk-updated");
            }
        } else {
            AFKPlayerData data = new AFKPlayerData(message, rawMessage, player.getLocation());
            afkPlayers.put(playerId, data);
            afkTeam.addEntry(player.getName());

            createAllPlayerDisplays(player, data);
            data.setAfkStartTime(System.currentTimeMillis());

            if (player.hasPermission(configManager.getString("permissions.customtext")) && customMessage != null && !customMessage.isEmpty()) {
                sendMessage(player, "messages.afk-enabled-with-text", rawMessage);
            } else {
                sendMessage(player, "messages.afk-enabled");
            }
        }

        player.sendActionBar(getAFKTimeComponent(player));
        return wasAFK;
    }

    private void createAllPlayerDisplays(Player player, AFKPlayerData data) {
        Location baseLocation = player.getLocation();
        double textOffset = configManager.getDouble("settings.text-height-offset");
        double sandclockOffset = configManager.getDouble("settings.sandclock-height-offset");

        TextDisplay textDisplay = createTextDisplay(player,
                baseLocation.clone().add(0, textOffset, 0), plugin);
        textDisplay.text(data.getMessage());
        data.setTextDisplay(textDisplay);

        if (configManager.getBoolean("settings.sandclock-enabled")) {
            TextDisplay sandclockDisplay = createTextDisplay(player,
                    baseLocation.clone().add(0, sandclockOffset, 0), plugin);

            String[] emojis = configManager.getStringList("settings.sandclock-emojis");
            if (emojis.length > 0) {
                String initialEmoji = emojis[0];
                String formattedEmoji = applySameFormatting(data.getRawMessage(), initialEmoji);
                sandclockDisplay.text(TextUtils.parseFormattedText(formattedEmoji));
            }

            data.setSandclockDisplay(sandclockDisplay);
            data.setSandclockState(0);
        }

        updateDisplayBillboard(player, data);
    }

    private String applySameFormatting(String originalText, String emoji) {
        if (originalText == null || originalText.isEmpty()) {
            return emoji;
        }
        StringBuilder formatting = new StringBuilder();

        // Ищем все & коды в оригинальном тексте
        for (int i = 0; i < originalText.length() - 1; i++) {
            if (originalText.charAt(i) == '&') {
                char code = originalText.charAt(i + 1);
                if ("0123456789abcdefklmnor".indexOf(code) != -1) {
                    formatting.append("&").append(code);
                    i++; // Пропускаем следующий символ
                }
            }
        }

        // Ищем все MiniMessage теги в оригинальном тексте
        String[] miniMessageTags = {"<black>", "<dark_blue>", "<dark_green>", "<dark_aqua>", "<dark_red>",
                "<dark_purple>", "<gold>", "<gray>", "<dark_gray>", "<blue>", "<green>",
                "<aqua>", "<red>", "<light_purple>", "<yellow>", "<white>", "<obfuscated>",
                "<bold>", "<strikethrough>", "<underlined>", "<italic>", "<reset>"};

        for (String tag : miniMessageTags) {
            if (originalText.contains(tag)) {
                formatting.append(tag);
            }
        }

        return formatting.toString() + emoji;
    }

    private TextDisplay createTextDisplay(Player player, Location location, JavaPlugin plugin) {
        World world = player.getWorld();
        TextDisplay display = world.spawn(location, TextDisplay.class);

        display.setBillboard(Display.Billboard.CENTER);
        display.setAlignment(TextDisplay.TextAlignment.CENTER);
        display.setSeeThrough(false);
        display.setShadowed(true);
        display.setBackgroundColor(org.bukkit.Color.fromARGB(0));
        display.setTextOpacity((byte) -1);
        display.setDisplayWidth(10f);
        display.setDisplayHeight(2f);
        display.setViewRange(128f);
        display.setShadowStrength(1.0f);
        display.setLineWidth(1000);

        float targetScale = configManager.getFloat("settings.text-scale");
        float startScale = configManager.getFloat("settings.text-startscale");
        int interpolationTicks = configManager.getInt("settings.scale-interpolation");
        int interpolationDelay = configManager.getInt("settings.scale-interpolation-delay");
        long runTaskLaterDelay = configManager.getLong("settings.scale-run-task-later-delay");

        display.setInterpolationDuration(interpolationTicks);
        display.setInterpolationDelay(interpolationDelay);

        Transformation transformation = display.getTransformation();
        transformation.getScale().set(new Vector3f(startScale, startScale, startScale));
        display.setTransformation(transformation);

        Bukkit.getScheduler().runTaskLater(plugin, () -> {
            Transformation t = display.getTransformation();
            t.getScale().set(new Vector3f(targetScale, targetScale, targetScale));
            display.setTransformation(t);
        }, runTaskLaterDelay);

        return display;
    }

    private void shrinkTextDisplay(TextDisplay display, int interpolationTicks) {
        display.setInterpolationDuration(interpolationTicks);
        display.setInterpolationDelay(0);

        Transformation transformation = display.getTransformation();
        transformation.getScale().set(new Vector3f(0, 0, 0));
        display.setTransformation(transformation);

        Bukkit.getScheduler().runTaskLater(plugin, display::remove, interpolationTicks);
    }

    private void updateSandclockText(AFKPlayerData data) {
        if (!configManager.getBoolean("settings.sandclock-enabled") ||
                data.getSandclockDisplay() == null ||
                data.getSandclockDisplay().isDead()) {
            return;
        }

        String[] emojis = configManager.getStringList("settings.sandclock-emojis");
        if (emojis.length == 0) return;

        int nextState = (data.getSandclockState() + 1) % emojis.length;
        data.setSandclockState(nextState);

        String emoji = emojis[nextState];
        String formattedEmoji = applySameFormatting(data.getRawMessage(), emoji);
        data.getSandclockDisplay().text(TextUtils.parseFormattedText(formattedEmoji));
    }

    private void updateDisplayBillboard(Player player, AFKPlayerData data) {
        Location playerLocation = player.getLocation();
        double textOffset = configManager.getDouble("settings.text-height-offset");
        double sandclockOffset = configManager.getDouble("settings.sandclock-height-offset");

        if (data.getTextDisplay() != null && !data.getTextDisplay().isDead()) {
            Location textLocation = playerLocation.clone().add(0, textOffset, 0);
            data.getTextDisplay().teleport(textLocation);
        }

        if (configManager.getBoolean("settings.sandclock-enabled") &&
                data.getSandclockDisplay() != null && !data.getSandclockDisplay().isDead()) {
            Location sandclockLocation = playerLocation.clone().add(0, sandclockOffset, 0);
            data.getSandclockDisplay().teleport(sandclockLocation);
        }
    }

    private void removePlayerDisplays(AFKPlayerData data) {
        TextDisplay textDisplay = data.getTextDisplay();
        TextDisplay sandclockDisplay = data.getSandclockDisplay();

        int interpolationTicks = configManager.getInt("settings.scale-interpolation");

        if (textDisplay != null) {
            if (!textDisplay.isDead()) {
                shrinkTextDisplay(textDisplay, interpolationTicks);
            }
            data.setTextDisplay(null);
        }
        if (sandclockDisplay != null) {
            if (!sandclockDisplay.isDead()) {
                shrinkTextDisplay(sandclockDisplay, interpolationTicks);
            }
            data.setSandclockDisplay(null);
        }
    }

    public void updateDisplayPositions(Player player) {
        AFKPlayerData data = afkPlayers.get(player.getUniqueId());
        if (data != null) {
            updateDisplayBillboard(player, data);
        }
    }

    public void updateSandclock(Player player) {
        AFKPlayerData data = afkPlayers.get(player.getUniqueId());
        if (data != null) {
            updateSandclockText(data);
        }
    }

    public void updateActionBar(Player player) {
        if (isAFK(player)) {
            player.sendActionBar(getAFKTimeComponent(player));
        }
    }

    public void playAFKSound(Player player) {
        if (!configManager.getBoolean("settings.sound.enabled") || !isAFK(player)) {
            return;
        }

        try {
            String soundKey = configManager.getString("settings.sound.sound");
            org.bukkit.NamespacedKey key = org.bukkit.NamespacedKey.fromString(soundKey.toLowerCase());
            Sound sound = org.bukkit.Registry.SOUNDS.get(key);

            if (sound != null) {
                float volume = (float) configManager.getDouble("settings.sound.volume");
                float pitch = (float) configManager.getDouble("settings.sound.pitch");
                player.playSound(player.getLocation(), sound, volume, pitch);
            } else {
                plugin.getLogger().warning("Звук не найден: " + soundKey);
            }
        } catch (Exception e) {
            plugin.getLogger().warning("Неверное название звука в конфиге: " + configManager.getString("settings.sound.sound") + " - " + e.getMessage());
        }
    }

    private Component getAFKTimeComponent(Player player) {
        AFKPlayerData data = afkPlayers.get(player.getUniqueId());
        if (data == null) {
            return configManager.getMessage("messages.action-bar");
        }

        long afkTime = System.currentTimeMillis() - data.getAfkStartTime();
        String timeFormatted = formatAFKTime(afkTime);

        String actionBarText = configManager.getString("messages.action-bar")
                .replace("{time}", timeFormatted);

        return TextUtils.parseFormattedText(actionBarText);
    }

    private String formatAFKTime(long millis) {
        long seconds = millis / 1000;
        long hours = seconds / 3600;
        long minutes = (seconds % 3600) / 60;
        long secs = seconds % 60;

        String formatKey;
        if (seconds < 60) {
            formatKey = "messages.afk-time-formats.less-than-minute";
        } else if (hours < 1) {
            formatKey = "messages.afk-time-formats.less-than-hour";
        } else {
            formatKey = "messages.afk-time-formats.hour-or-more";
        }

        String format = configManager.getString(formatKey);
        return format.replace("{hours}", String.valueOf(hours))
                .replace("{minutes}", String.valueOf(minutes))
                .replace("{seconds}", String.valueOf(secs));
    }

    public boolean isAFK(Player player) {
        return afkPlayers.containsKey(player.getUniqueId());
    }

    public boolean hasMoved(Player player) {
        AFKPlayerData data = afkPlayers.get(player.getUniqueId());
        if (data == null) return false;

        Location originalLocation = data.getOriginalLocation();
        Location currentLocation = player.getLocation();

        return Math.abs(originalLocation.getX() - currentLocation.getX()) > 0.000001 ||
                Math.abs(originalLocation.getZ() - currentLocation.getZ()) > 0.000001 ||
                originalLocation.getWorld() != currentLocation.getWorld();
    }

    public String getRawAFKMessage(Player player) {
        AFKPlayerData data = afkPlayers.get(player.getUniqueId());
        return data != null ? data.getRawMessage() : "";
    }

    private void sendMessage(Player player, String messageKey, String text) {
        Component message = configManager.getMessageWithText(messageKey, text);
        adventure.player(player).sendMessage(message);
    }

    private void sendMessage(Player player, String messageKey) {
        Component message = configManager.getMessage(messageKey);
        adventure.player(player).sendMessage(message);
    }

    public void cleanup() {
        for (AFKPlayerData data : afkPlayers.values()) {
            Player player = Bukkit.getPlayer(data.getPlayerId());
            if (player != null) {
                afkTeam.removeEntry(player.getName());
            }
            removePlayerDisplays(data);
        }

        afkPlayers.clear();

        if (afkTeam != null && afkTeam.getEntries().isEmpty()) {
            afkTeam.unregister();
        }
    }

    public Collection<UUID> getAFKPlayers() {
        return Collections.unmodifiableCollection(afkPlayers.keySet());
    }

    public JavaPlugin getPlugin() {
        return plugin;
    }

    public int getSoundInterval() {
        return configManager.getInt("settings.sound.interval");
    }

    public void removeAFK(Player player) {
        removeAFK(player, true);
    }

    public void removeAFKSilently(Player player) {
        removeAFK(player, false);
    }

    private void removeAFK(Player player, boolean sendMessage) {
        AFKPlayerData data = afkPlayers.remove(player.getUniqueId());
        if (data != null) {
            afkTeam.removeEntry(player.getName());
            removePlayerDisplays(data);
            if (configManager.getBoolean("settings.fast-actionbar-removing")) {
                player.sendActionBar(Component.text(" "));
            }

            if (sendMessage) {
                sendMessage(player, "messages.afk-disabled");
            }
        }
    }

    private static class AFKPlayerData {
        private final UUID playerId;
        private Component message;
        private String rawMessage;
        private final Location originalLocation;
        private TextDisplay textDisplay;
        private TextDisplay sandclockDisplay;
        private int sandclockState = 0;
        private long afkStartTime;

        public AFKPlayerData(Component message, String rawMessage, Location originalLocation) {
            this.playerId = UUID.randomUUID();
            this.message = message;
            this.rawMessage = rawMessage;
            this.originalLocation = originalLocation.clone();
        }

        public UUID getPlayerId() { return playerId; }
        public Component getMessage() { return message; }
        public void setMessage(Component message) { this.message = message; }
        public String getRawMessage() { return rawMessage; }
        public void setRawMessage(String rawMessage) { this.rawMessage = rawMessage; }
        public Location getOriginalLocation() { return originalLocation.clone(); }
        public TextDisplay getTextDisplay() { return textDisplay; }
        public void setTextDisplay(TextDisplay textDisplay) { this.textDisplay = textDisplay; }
        public TextDisplay getSandclockDisplay() { return sandclockDisplay; }
        public void setSandclockDisplay(TextDisplay sandclockDisplay) { this.sandclockDisplay = sandclockDisplay; }
        public int getSandclockState() { return sandclockState; }
        public void setSandclockState(int sandclockState) { this.sandclockState = sandclockState; }
        public long getAfkStartTime() { return afkStartTime; }
        public void setAfkStartTime(long afkStartTime) { this.afkStartTime = afkStartTime; }
    }
}