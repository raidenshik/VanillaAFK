package com.rimzzy.vanillaAFK.managers;

import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.minimessage.MiniMessage;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.entity.ArmorStand;
import org.bukkit.entity.Player;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.scoreboard.Scoreboard;
import org.bukkit.scoreboard.Team;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

public class AFKManager {

    private final ConfigManager configManager;
    private final MiniMessage miniMessage;
    private final Map<UUID, Component> afkPlayers;
    private final Map<UUID, String> rawMessages;
    private final Map<UUID, ArmorStand> textArmorStands;
    private final Map<UUID, ArmorStand> sandclockArmorStands;
    private final Map<UUID, Location> afkLocations;
    private final Map<UUID, Integer> sandclockStates;
    private Team afkTeam;

    public AFKManager(JavaPlugin plugin, ConfigManager configManager) {
        this.configManager = configManager;
        this.miniMessage = MiniMessage.miniMessage();
        this.afkPlayers = new HashMap<>();
        this.rawMessages = new HashMap<>();
        this.textArmorStands = new HashMap<>();
        this.sandclockArmorStands = new HashMap<>();
        this.afkLocations = new HashMap<>();
        this.sandclockStates = new HashMap<>();
        setupAFKTeam();
    }

    private void setupAFKTeam() {
        Scoreboard scoreboard = Bukkit.getScoreboardManager().getMainScoreboard();
        afkTeam = scoreboard.getTeam("afk");

        if (afkTeam == null) {
            afkTeam = scoreboard.registerNewTeam("afk");
        }

        afkTeam.setOption(Team.Option.NAME_TAG_VISIBILITY, Team.OptionStatus.ALWAYS);
    }

    public boolean setAFK(Player player, String customMessage) {
        UUID playerId = player.getUniqueId();
        Component message;
        boolean wasAFK = isAFK(player);

        if (customMessage != null) {
            message = parseMessage(customMessage);
            rawMessages.put(playerId, customMessage);
        } else {
            message = configManager.getMessage("messages.default-afk-text");
            rawMessages.put(playerId, configManager.getString("messages.default-afk-text"));
        }

        afkPlayers.put(playerId, message);
        afkLocations.put(playerId, player.getLocation());
        sandclockStates.put(playerId, 0);
        afkTeam.addEntry(player.getName());

        if (wasAFK) {
            updateArmorStandText(player, message);
            updateSandclockText(player);
        } else {
            createArmorStands(player, message);
        }

        player.sendActionBar(configManager.getMessage("messages.action-bar"));

        return wasAFK;
    }

    private Component parseMessage(String text) {
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
            return miniMessage.deserialize(miniMessageText);
        } catch (Exception e) {
            return Component.text(text);
        }
    }

    public void removeAFK(Player player) {
        UUID playerId = player.getUniqueId();
        afkPlayers.remove(playerId);
        rawMessages.remove(playerId);
        afkLocations.remove(playerId);
        sandclockStates.remove(playerId);
        afkTeam.removeEntry(player.getName());
        removeArmorStands(player);
    }

    private void createArmorStands(Player player, Component message) {
        Location baseLocation = player.getLocation();
        double offset = configManager.getDouble("settings.bubble-height-offset");

        Location textLocation = baseLocation.add(0, 2.2 + offset, 0);
        ArmorStand textArmorStand = createArmorStand(player, textLocation);
        textArmorStand.customName(message);
        textArmorStands.put(player.getUniqueId(), textArmorStand);

        if (configManager.getBoolean("settings.sandclock-enabled")) {
            Location sandclockLocation = baseLocation.add(0, 0.4, 0); // Выше текста
            ArmorStand sandclockArmorStand = createArmorStand(player, sandclockLocation);
            sandclockArmorStands.put(player.getUniqueId(), sandclockArmorStand);
            updateSandclockText(player); // Инициализируем текст
        }
    }

    private ArmorStand createArmorStand(Player player, Location location) {
        ArmorStand armorStand = player.getWorld().spawn(location, ArmorStand.class);
        armorStand.setCustomNameVisible(true);
        armorStand.setVisible(false);
        armorStand.setGravity(false);
        armorStand.setInvulnerable(true);
        armorStand.setSmall(true);
        armorStand.setMarker(true);
        armorStand.setCollidable(false);
        return armorStand;
    }

    private void updateArmorStandText(Player player, Component newMessage) {
        ArmorStand armorStand = textArmorStands.get(player.getUniqueId());
        if (armorStand != null && !armorStand.isDead()) {
            armorStand.customName(newMessage);
        }
    }

    private void updateSandclockText(Player player) {
        if (!configManager.getBoolean("settings.sandclock-enabled")) return;

        ArmorStand armorStand = sandclockArmorStands.get(player.getUniqueId());
        if (armorStand == null || armorStand.isDead()) return;

        UUID playerId = player.getUniqueId();
        String[] emojis = configManager.getStringList("settings.sandclock-emojis");
        int currentState = sandclockStates.getOrDefault(playerId, 0);
        int nextState = (currentState + 1) % emojis.length;
        sandclockStates.put(playerId, nextState);

        String emoji = emojis[nextState].trim();
        String rawMessage = rawMessages.get(playerId);

        Component sandclockMessage = parseMessage(rawMessage.replaceFirst("^(.*)$", emoji));
        armorStand.customName(sandclockMessage);
    }

    private void removeArmorStands(Player player) {
        UUID playerId = player.getUniqueId();

        ArmorStand textArmorStand = textArmorStands.remove(playerId);
        if (textArmorStand != null && !textArmorStand.isDead()) {
            textArmorStand.remove();
        }

        ArmorStand sandclockArmorStand = sandclockArmorStands.remove(playerId);
        if (sandclockArmorStand != null && !sandclockArmorStand.isDead()) {
            sandclockArmorStand.remove();
        }
    }

    public void updateArmorStandPositions(Player player) {
        Location baseLocation = player.getLocation();
        double offset = configManager.getDouble("settings.bubble-height-offset");

        ArmorStand textArmorStand = textArmorStands.get(player.getUniqueId());
        if (textArmorStand != null && !textArmorStand.isDead()) {
            Location textLocation = baseLocation.add(0, 2.2 + offset, 0);
            textArmorStand.teleport(textLocation);
        }

        if (configManager.getBoolean("settings.sandclock-enabled")) {
            ArmorStand sandclockArmorStand = sandclockArmorStands.get(player.getUniqueId());
            if (sandclockArmorStand != null && !sandclockArmorStand.isDead()) {
                Location sandclockLocation = baseLocation.add(0, 0.4, 0);
                sandclockArmorStand.teleport(sandclockLocation);
            }
        }
    }

    public void updateSandclock(Player player) {
        if (!isAFK(player) || !configManager.getBoolean("settings.sandclock-enabled")) return;
        updateSandclockText(player);
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
        UUID playerId = player.getUniqueId();
        Location originalLocation = afkLocations.get(playerId);
        if (originalLocation == null) return false;

        Location currentLocation = player.getLocation();
        return originalLocation.getBlockX() != currentLocation.getBlockX() ||
                originalLocation.getBlockY() != currentLocation.getBlockY() ||
                originalLocation.getBlockZ() != currentLocation.getBlockZ() ||
                originalLocation.getWorld() != currentLocation.getWorld();
    }

    public String getRawAFKMessage(Player player) {
        return rawMessages.get(player.getUniqueId());
    }

    public void cleanup() {
        for (UUID playerId : afkPlayers.keySet()) {
            Player player = Bukkit.getPlayer(playerId);
            if (player != null) {
                afkTeam.removeEntry(player.getName());
            }
        }

        for (ArmorStand armorStand : textArmorStands.values()) {
            if (!armorStand.isDead()) {
                armorStand.remove();
            }
        }

        for (ArmorStand armorStand : sandclockArmorStands.values()) {
            if (!armorStand.isDead()) {
                armorStand.remove();
            }
        }

        afkPlayers.clear();
        rawMessages.clear();
        textArmorStands.clear();
        sandclockArmorStands.clear();
        afkLocations.clear();
        sandclockStates.clear();
    }
}