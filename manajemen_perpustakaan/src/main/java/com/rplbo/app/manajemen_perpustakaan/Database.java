package com.rplbo.app.manajemen_perpustakaan;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;

public class Database {

    public static Connection connect() {
        try {
            Class.forName("org.sqlite.JDBC");
            return DriverManager.getConnection("jdbc:sqlite:perpustakaan.db");
        } catch (Exception e) {
            System.out.println("Koneksi gagal: " + e.getMessage());
            return null;
        }
    }

    public static void initializeDB() {
        String sqlUsers = "CREATE TABLE IF NOT EXISTS users ("
                + " id INTEGER PRIMARY KEY AUTOINCREMENT,"
                + " username TEXT NOT NULL UNIQUE,"
                + " password TEXT NOT NULL,"
                + " role TEXT NOT NULL"
                + ");";

        String sqlBooks = "CREATE TABLE IF NOT EXISTS books ("
                + " id INTEGER PRIMARY KEY AUTOINCREMENT,"
                + " title TEXT NOT NULL,"
                + " author TEXT NOT NULL,"
                + " publisher TEXT,"
                + " publish_year INTEGER,"
                + " page_count INTEGER,"
                + " genre TEXT,"
                + " image_path TEXT,"
                + " status TEXT DEFAULT 'Tersedia'"
                + ");";

        String sqlLoans = "CREATE TABLE IF NOT EXISTS loans ("
                + " id INTEGER PRIMARY KEY AUTOINCREMENT,"
                + " username TEXT NOT NULL,"
                + " book_id INTEGER,"
                + " borrow_date DATE DEFAULT CURRENT_DATE,"
                + " status TEXT DEFAULT 'Dipinjam'"
                + ");";

        String sqlComplaints = "CREATE TABLE IF NOT EXISTS complaints ("
                + " id INTEGER PRIMARY KEY AUTOINCREMENT,"
                + " username TEXT NOT NULL,"
                + " message TEXT NOT NULL,"
                + " tanggal DATE DEFAULT CURRENT_DATE"
                + ");";

        try (Connection conn = connect();
             Statement stmt = conn.createStatement()) {

            stmt.execute(sqlUsers);
            stmt.execute(sqlBooks);
            stmt.execute(sqlLoans);
            stmt.execute(sqlComplaints);

            // Insert dummy users
            stmt.execute("INSERT OR IGNORE INTO users (username, password, role) VALUES ('admin', 'admin123', 'admin');");
            stmt.execute("INSERT OR IGNORE INTO users (username, password, role) VALUES ('user', 'user123', 'user');");

            System.out.println("Database dan tabel berhasil diinisialisasi.");

        } catch (SQLException e) {
            System.out.println("Gagal menyiapkan database: " + e.getMessage());
        }
    }
}