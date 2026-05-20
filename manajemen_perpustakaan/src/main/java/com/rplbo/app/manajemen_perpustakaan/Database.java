package com.rplbo.app.manajemen_perpustakaan;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.sql.Statement;

public class Database {
    private static final String URL = "jdbc:sqlite:perpustakaan.db";

    public static Connection connect() {
        Connection conn = null;
        try {
            conn = DriverManager.getConnection(URL);
        } catch (SQLException e) {
            System.out.println("Gagal koneksi ke SQLite: " + e.getMessage());
        }
        return conn;
    }

    public static void initializeDB() {
        String sql = "CREATE TABLE IF NOT EXISTS users (\n"
                + " id INTEGER PRIMARY KEY AUTOINCREMENT,\n"
                + " username TEXT NOT NULL UNIQUE,\n"
                + " password TEXT NOT NULL,\n"
                + " role TEXT NOT NULL\n"
                + ");";

        try (Connection conn = connect();
             Statement stmt = conn.createStatement()) {

            stmt.execute(sql);
            System.out.println("Tabel users berhasil disiapkan.");

            String insertAdmin = "INSERT OR IGNORE INTO users (username, password, role) "
                    + "VALUES ('admin', 'admin123', 'admin');";
            stmt.execute(insertAdmin);

            String insertUser = "INSERT OR IGNORE INTO users (username, password, role) "
                    + "VALUES ('user', 'user123', 'user');";
            stmt.execute(insertUser);

            System.out.println("Data dummy admin & user berhasil ditambahkan.");

        } catch (SQLException e) {
            System.out.println("Gagal menyiapkan database: " + e.getMessage());
        }
    }
}