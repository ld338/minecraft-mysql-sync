package de.ld338.mysqlsync.tools;

import org.bukkit.Bukkit;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.logging.Level;
import java.util.logging.Logger;

public class PlayerUtil {
    private static final Logger LOGGER = Bukkit.getLogger();

    private static void loadData(org.bukkit.entity.Player player, String table, DataDecoder decoder) {
        String query = String.format("SELECT CONTENTS FROM %s WHERE player_uuid = ?", table);
        try (Connection conn = MySQL.getConnection();
             PreparedStatement statement = conn.prepareStatement(query)) {

            statement.setString(1, player.getUniqueId().toString());
            try (ResultSet resultSet = statement.executeQuery()) {
                if (resultSet.next()) {
                    String data = resultSet.getString("CONTENTS");
                    decoder.decode(player, data);
                }
            }
        } catch (SQLException e) {
            LOGGER.log(Level.SEVERE, "SQL error while loading data from " + table, e);
        } catch (Exception e) {
            throw new RuntimeException("Unexpected error while loading data from " + table, e);
        }
    }

    private static void saveData(org.bukkit.entity.Player player, String table, DataEncoder encoder) {
        String query = String.format(
                "INSERT INTO %s (player_uuid, CONTENTS) VALUES (?, ?) ON DUPLICATE KEY UPDATE CONTENTS = ?",
                table);
        try (Connection conn = MySQL.getConnection();
             PreparedStatement statement = conn.prepareStatement(query)) {

            String data = encoder.encode(player);
            if (data == null) {
                return;
            }

            statement.setString(1, player.getUniqueId().toString());
            statement.setString(2, data);
            statement.setString(3, data);
            statement.executeUpdate();
        } catch (SQLException e) {
            throw new RuntimeException("SQL error while saving data to " + table, e);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    public static void loadInv(org.bukkit.entity.Player player) {
        loadData(player, "inventory", Base64Util::decodeInventoryFromBase64);
    }

    public static void saveInv(org.bukkit.entity.Player player) {
        saveData(player, "inventory", Base64Util::encodeInventoryToBase64);
    }

    public static void loadEnderchest(org.bukkit.entity.Player player) {
        loadData(player, "enderchest", Base64Util::decodeEnderChestFromBase64);
    }

    public static void saveEnderchest(org.bukkit.entity.Player player) {
        saveData(player, "enderchest", Base64Util::encodeEnderChestToBase64);
    }

    public static void loadArmor(org.bukkit.entity.Player player) {
        loadData(player, "armor", Base64Util::decodeArmorFromBase64);
    }

    public static void saveArmor(org.bukkit.entity.Player player) {
        saveData(player, "armor", Base64Util::encodeArmorToBase64);
    }

    public static void loadXP(org.bukkit.entity.Player player) {
        loadData(player, "xp", (p, data) -> p.setTotalExperience(Integer.parseInt(data)));
    }

    public static void saveXP(org.bukkit.entity.Player player) {
        saveData(player, "xp", p -> String.valueOf(p.getTotalExperience()));
    }

    public static void loadAchievements(org.bukkit.entity.Player player) {
        loadData(player, "achievements", Base64Util::decodeAchievementsFromBase64);
    }

    public static void saveAchievements(org.bukkit.entity.Player player) {
        saveData(player, "achievements", Base64Util::encodeAchievementsToBase64);
    }

    public static void loadStats(org.bukkit.entity.Player player) {
        loadData(player, "stats", Base64Util::decodeStatsFromBase64);
    }

    public static void saveStats(org.bukkit.entity.Player player) {
        saveData(player, "stats", Base64Util::encodeStatsToBase64);
    }

    public static void savePlayerState(org.bukkit.entity.Player player) {
        saveData(player, "player_state", p -> {
            int hunger = p.getFoodLevel();
            double health = p.getHealth();
            return hunger + "," + health;
        });
    }

    public static void loadPlayerState(org.bukkit.entity.Player player) {
        loadData(player, "player_state", (p, data) -> {
            String[] parts = data.split(",");
            p.setFoodLevel(Integer.parseInt(parts[0]));
            p.setHealth(Double.parseDouble(parts[1]));
        });
    }

    public static void loadEffects(org.bukkit.entity.Player player) {
        loadData(player, "player_effects", (p, data) -> Base64Util.decodeEffectsFromBase64(player, data));
    }

    public static void saveEffects(org.bukkit.entity.Player player) {
        saveData(player, "player_effects", Base64Util::encodeEffectsToBase64);
    }

    @FunctionalInterface
    private interface DataDecoder {
        void decode(org.bukkit.entity.Player player, String data) throws Exception;
    }

    @FunctionalInterface
    private interface DataEncoder {
        String encode(org.bukkit.entity.Player player) throws Exception;
    }

    public static void saveData(org.bukkit.entity.Player player) {
        saveInv(player);
        saveEnderchest(player);
        saveArmor(player);
        saveXP(player);
        saveAchievements(player);
        saveStats(player);
        saveEffects(player);
        savePlayerState(player);
    }

    public static void loadData(org.bukkit.entity.Player player) {
        loadInv(player);
        loadEnderchest(player);
        loadArmor(player);
        loadXP(player);
        loadAchievements(player);
        loadStats(player);
        loadEffects(player);
        loadPlayerState(player);
    }
}
