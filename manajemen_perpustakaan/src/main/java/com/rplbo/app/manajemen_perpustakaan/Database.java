package com.rplbo.app.manajemen_perpustakaan;

import java.sql.*;

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

        try (Connection conn = connect();
             Statement stmt = conn.createStatement()) {

            // 1. Eksekusi pembuatan tabel
            stmt.execute(sqlUsers);
            stmt.execute(sqlBooks);
            stmt.execute(sqlLoans);

            // 2. Insert dummy users (pakai INSERT OR IGNORE supaya tidak error kalau sudah ada)
            stmt.execute("INSERT OR IGNORE INTO users (username, password, role) VALUES ('admin', 'admin123', 'admin');");
            stmt.execute("INSERT OR IGNORE INTO users (username, password, role) VALUES ('user', 'user123', 'user');");

            // 3. Masukkan 3 buku dummy ke tabel books jika tabelnya masih kosong
            ResultSet rsBooks = stmt.executeQuery("SELECT COUNT(*) FROM books");
            if (rsBooks.next() && rsBooks.getInt(1) == 0) {
                stmt.execute("INSERT INTO books (title, author) VALUES ('Pemrograman Java', 'Budi Raharjo')");
                stmt.execute("INSERT INTO books (title, author) VALUES ('Belajar UI/UX', 'Siska')");
                stmt.execute("INSERT INTO books (title, author) VALUES ('Database SQLite', 'Andi')");
            }

            // 4. Masukkan 1 peminjaman dummy ke tabel loans jika tabelnya masih kosong
            ResultSet rsLoans = stmt.executeQuery("SELECT COUNT(*) FROM loans");
            if (rsLoans.next() && rsLoans.getInt(1) == 0) {
                stmt.execute("INSERT INTO loans (username, book_id) VALUES ('user', 1)");
            }

            System.out.println("Database dan tabel berhasil diinisialisasi.");

        } catch (SQLException e) {
            System.out.println("Gagal menyiapkan database: " + e.getMessage());
        }
    }
}