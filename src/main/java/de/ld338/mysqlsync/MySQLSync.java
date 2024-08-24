package de.ld338.mysqlsync;

import de.ld338.mysqlsync.commands.SyncCommand;
import de.ld338.mysqlsync.tools.MySQL;
import de.ld338.mysqlsync.tools.PlayerUtil;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerMoveEvent;
import org.bukkit.event.player.PlayerQuitEvent;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.inventory.InventoryDragEvent;
import org.bukkit.plugin.java.JavaPlugin;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.sql.Time;
import java.util.HashMap;

public final class MySQLSync extends JavaPlugin implements Listener {
    public static String host, database, username, password;
    public static int port;
    Connection connection;

    private HashMap<Player, Boolean> frozen = new HashMap<>();

    @Override
    public void onEnable() {
        Bukkit.getPluginManager().registerEvents(this, this);
        getCommand("sync-mysql").setExecutor(new SyncCommand());
        loadConfig();
        connection = MySQL.getConnection();
        if (getConfig().getBoolean("recreatetables")) {
            try (PreparedStatement statement = connection.prepareStatement("DROP TABLE IF EXISTS inventory, enderchest, armor, achievements, xp, stats, player_state, player_effects")) {
                statement.execute();
            } catch (Exception e) {
                e.printStackTrace();
            }
        }

        MySQL.createTable("inventory", "player_uuid VARCHAR(36) PRIMARY KEY, CONTENTS LONGTEXT");
        MySQL.createTable("enderchest", "player_uuid VARCHAR(36) PRIMARY KEY, CONTENTS LONGTEXT");
        MySQL.createTable("armor", "player_uuid VARCHAR(36) PRIMARY KEY, CONTENTS LONGTEXT");
        MySQL.createTable("achievements", "player_uuid VARCHAR(36) PRIMARY KEY, CONTENTS LONGTEXT");
        MySQL.createTable("xp", "player_uuid VARCHAR(36) PRIMARY KEY, CONTENTS LONGTEXT");
        MySQL.createTable("stats", "player_uuid VARCHAR(36) PRIMARY KEY, CONTENTS LONGTEXT");
        MySQL.createTable("player_state", "player_uuid VARCHAR(36) PRIMARY KEY, CONTENTS LONGTEXT");
        MySQL.createTable("player_effects", "player_uuid VARCHAR(36) PRIMARY KEY, CONTENTS LONGTEXT");
    }

    @Override
    public void onDisable() {
        Bukkit.getOnlinePlayers().forEach(PlayerUtil::saveData);
        MySQL.closeConnection();
    }

    private void loadConfig() {
        getConfig().options().copyDefaults(true);
        saveConfig();

        host = getConfig().getString("host");
        port = getConfig().getInt("port");
        database = getConfig().getString("database");
        username = getConfig().getString("username");
        password = getConfig().getString("password");
    }

    @EventHandler
    public void onPlayerJoin(PlayerJoinEvent event) {
        Player player = event.getPlayer();
        player.getInventory().clear();
        player.getEnderChest().clear();
        player.getInventory().setArmorContents(null);
        player.sendTitle("§c§lMySQLSync", "§7Loading your data...", 10, 70, 20);
        freezePlayer(player);
        Time time = new Time(System.currentTimeMillis());
        PlayerUtil.loadData(player);
        Time time2 = new Time(System.currentTimeMillis());
        System.out.println("Loaded data for " + player.getName() + " in " + (time2.getTime() - time.getTime()) + "ms");
        unfreezePlayer(player);
        player.sendTitle("§a§lMySQLSync", "§7Data loaded!", 10, 70, 20);
    }

    @EventHandler
    public void onPlayerQuit(PlayerQuitEvent event) {
        Player player = event.getPlayer();
        if (frozen.containsKey(player)) {
            frozen.remove(player);
        } else {
            Time time = new Time(System.currentTimeMillis());
            PlayerUtil.saveData(player);
            Time time2 = new Time(System.currentTimeMillis());
            System.out.println("Saved data for " + player.getName() + " in " + (time2.getTime() - time.getTime()) + "ms");
        }
    }

    @EventHandler
    public void onPlayerMove(PlayerMoveEvent event) {
        Player player = event.getPlayer();
        if (frozen.containsKey(player)) {
            event.setCancelled(true);
        }
    }

    @EventHandler
    public void onPlayerInteract(PlayerInteractEvent event) {
        Player player = event.getPlayer();
        if (frozen.containsKey(player)) {
            event.setCancelled(true);
        }
    }

    @EventHandler
    public void onInventoryClick(InventoryClickEvent event) {
        Player player = (Player) event.getWhoClicked();
        if (frozen.containsKey(player)) {
            event.setCancelled(true);
        }
    }

    @EventHandler
    public void onInventoryDrag(InventoryDragEvent event) {
        Player player = (Player) event.getWhoClicked();
        if (frozen.containsKey(player)) {
            event.setCancelled(true);
        }
    }

    public void unfreezePlayer(Player player) {
        frozen.remove(player);
    }

    public void freezePlayer(Player player) {
        frozen.put(player, true);
    }
}
