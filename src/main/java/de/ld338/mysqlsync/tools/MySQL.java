package de.ld338.mysqlsync.tools;

import org.bukkit.Bukkit;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.Properties;
import java.util.logging.Level;
import java.util.logging.Logger;

public class MySQL {
    private static final Logger LOGGER = Bukkit.getLogger();
    private static Connection connection;
    private static String host;
    private static int port;
    private static String database;
    private static String username;
    private static String password;

    public static void connect() {
        try {
            Properties properties = new Properties();
            properties.setProperty("user", username);
            properties.setProperty("password", password);
            properties.setProperty("autoReconnect", "true");
            properties.setProperty("maxReconnects", "3");

            String url = String.format("jdbc:mysql://%s:%d/%s", host, port, database);
            connection = DriverManager.getConnection(url, properties);
        } catch (SQLException e) {
            LOGGER.log(Level.SEVERE, "Could not connect to the database", e);
            throw new RuntimeException("Could not connect to the database", e);
        }
    }

    public static Connection getConnection() {
        try {
            if (connection == null || connection.isClosed()) {
                connect();
            }
        } catch (SQLException e) {
            LOGGER.log(Level.SEVERE, "Failed to check or reconnect to the database", e);
            throw new RuntimeException("Failed to check or reconnect to the database", e);
        }
        return connection;
    }

    public static void closeConnection() {
        if (connection != null) {
            try {
                connection.close();
            } catch (SQLException e) {
                LOGGER.log(Level.SEVERE, "Could not close the database connection", e);
                throw new RuntimeException("Could not close the database connection", e);
            }
        }
    }

    public static void createTable(String table, String columns) {
        String sql = String.format("CREATE TABLE IF NOT EXISTS %s (%s)", table, columns);
        try (Statement statement = getConnection().createStatement()) {
            statement.execute(sql);
        } catch (SQLException e) {
            LOGGER.log(Level.SEVERE, "Could not create table " + table, e);
            throw new RuntimeException("Could not create table " + table, e);
        }
    }

    public static void setConfig(String host, int port, String database, String username, String password) {
        if (host == null || host.isEmpty() ||
                port <= 0 || database == null || database.isEmpty() ||
                username == null || username.isEmpty() || password == null || password.isEmpty()) {
            throw new IllegalArgumentException("Invalid MySQL configuration");
        }

        MySQL.host = host;
        MySQL.port = port;
        MySQL.database = database;
        MySQL.username = username;
        MySQL.password = password;
    }
}
