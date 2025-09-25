package com.rimzzy.vanillaAFK.managers;

import net.kyori.adventure.platform.bukkit.BukkitAudiences;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.serializer.legacy.LegacyComponentSerializer;
import org.bukkit.Bukkit;
import org.bukkit.Location;
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

        Component message = customMessage != null ?
                LegacyComponentSerializer.legacyAmpersand().deserialize(customMessage) :
                configManager.getMessage("messages.default-afk-text");

        String rawMessage = customMessage != null ? customMessage : configManager.getString("messages.default-afk-text");

        if (wasAFK) {
            AFKPlayerData data = afkPlayers.get(playerId);
            data.setMessage(message);
            data.setRawMessage(rawMessage);

            if (data.getTextDisplay() != null && !data.getTextDisplay().isDead()) {
                data.getTextDisplay().text(message);
            }

            sendMessage(player, "messages.afk-updated", rawMessage);
        } else {
            AFKPlayerData data = new AFKPlayerData(message, rawMessage, player.getLocation());
            afkPlayers.put(playerId, data);
            afkTeam.addEntry(player.getName());
            createPlayerDisplays(player, data);
            sendMessage(player, "messages.afk-enabled", rawMessage);
        }

        player.sendActionBar(configManager.getMessage("messages.action-bar"));
        return wasAFK;
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
            if (sendMessage) {
                sendMessage(player, "messages.afk-disabled");
            }
        }
    }

    private void createPlayerDisplays(Player player, AFKPlayerData data) {
        Location baseLocation = player.getLocation();
        double textOffset = configManager.getDouble("settings.text-height-offset");
        double sandclockOffset = configManager.getDouble("settings.sandclock-height-offset");

        TextDisplay textDisplay = createTextDisplay(player,
                baseLocation.clone().add(0, textOffset, 0));
        textDisplay.text(data.getMessage());
        data.setTextDisplay(textDisplay);

        if (configManager.getBoolean("settings.sandclock-enabled")) {
            TextDisplay sandclockDisplay = createTextDisplay(player,
                    baseLocation.clone().add(0, sandclockOffset, 0));
            updateSandclockText(data);
            data.setSandclockDisplay(sandclockDisplay);
        }

        updateDisplayBillboard(player, data);
    }

    private TextDisplay createTextDisplay(Player player, Location location) {
        TextDisplay display = player.getWorld().spawn(location, TextDisplay.class);

        display.setBillboard(org.bukkit.entity.Display.Billboard.CENTER);
        display.setAlignment(TextDisplay.TextAlignment.CENTER);
        display.setSeeThrough(false);
        display.setShadowed(true);
        display.setBackgroundColor(org.bukkit.Color.fromARGB(0));
        display.setTextOpacity((byte) -1);
        display.setDisplayWidth(20f);
        display.setDisplayHeight(4f);
        display.setViewRange(128f);
        display.setShadowStrength(1.0f);
        display.setLineWidth(1000);

        Transformation transformation = display.getTransformation();
        transformation.getScale().set(new Vector3f(1f, 1f, 1f));
        display.setTransformation(transformation);

        return display;
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
        data.getSandclockDisplay().text(LegacyComponentSerializer.legacyAmpersand().deserialize(emoji));
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
        if (data.getTextDisplay() != null) {
            if (!data.getTextDisplay().isDead()) {
                data.getTextDisplay().remove();
            }
            data.setTextDisplay(null);
        }
        if (data.getSandclockDisplay() != null) {
            if (!data.getSandclockDisplay().isDead()) {
                data.getSandclockDisplay().remove();
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
            player.sendActionBar(configManager.getMessage("messages.action-bar"));
        }
    }

    public boolean isAFK(Player player) {
        return afkPlayers.containsKey(player.getUniqueId());
    }

    public boolean hasMoved(Player player) {
        AFKPlayerData data = afkPlayers.get(player.getUniqueId());
        if (data == null) return false;

        Location originalLocation = data.getOriginalLocation();
        Location currentLocation = player.getLocation();

        return originalLocation.getBlockX() != currentLocation.getBlockX() ||
                originalLocation.getBlockZ() != currentLocation.getBlockZ() ||
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

    private static class AFKPlayerData {
        private final UUID playerId;
        private Component message;
        private String rawMessage;
        private final Location originalLocation;
        private TextDisplay textDisplay;
        private TextDisplay sandclockDisplay;
        private int sandclockState = 0;

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
    }
}