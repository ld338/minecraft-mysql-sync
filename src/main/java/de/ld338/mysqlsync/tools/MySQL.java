package de.ld338.mysqlsync.tools;

import de.ld338.mysqlsync.MySQLSync;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.util.Properties;

public class MySQL {
    private static Connection connection;

    public static void connect() {
        String host = MySQLSync.host;
        String database = MySQLSync.database;
        String username = MySQLSync.username;
        String password = MySQLSync.password;
        int port = MySQLSync.port;

        try {
            Properties properties = new Properties();
            properties.setProperty("user", username);
            properties.setProperty("password", password);
            properties.setProperty("autoReconnect", "true");
            properties.setProperty("maxReconnects", "3");

            connection = DriverManager.getConnection("jdbc:mysql://" + host + ":" + port + "/" + database, properties);
        } catch (SQLException e) {
            throw new RuntimeException("Could not connect to the database", e);
        }
    }

    public static Connection getConnection() {
        try {
            if (connection == null || connection.isClosed()) {
                connect();
            }
        } catch (SQLException e) {
            throw new RuntimeException("Failed to check or reconnect to the database", e);
        }
        return connection;
    }

    public static void closeConnection() {
        if (connection != null) {
            try {
                connection.close();
            } catch (SQLException e) {
                throw new RuntimeException("Could not close the database connection", e);
            }
        }
    }

    public static void createTable(String table, String columns) {
        try {
            getConnection().createStatement().execute("CREATE TABLE IF NOT EXISTS " + table + " (" + columns + ")");
        } catch (SQLException e) {
            throw new RuntimeException("Could not create table " + table, e);
        }
    }
}
