package com.rplbo.app.manajemen_perpustakaan;

import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.stage.Stage;

import java.io.IOException;

public class MainApp extends Application {
    @Override
    public void start(Stage stage) throws IOException {
        // Menjalankan pengecekan tabel database saat aplikasi dibuka
        Database.initializeDB();

        // Langsung meload halaman peminjaman buku
        FXMLLoader fxmlLoader = new FXMLLoader(MainApp.class.getResource("Peminjaman_view.fxml"));
        Scene scene = new Scene(fxmlLoader.load(), 1080, 720);

        stage.setTitle("Aplikasi Perpustakaan Furab - Form Peminjaman");
        stage.setScene(scene);
        stage.setResizable(true); // Memperbolehkan ukuran aplikasi di-resize
        stage.show();
    }

    public static void main(String[] args) {
        launch();
    }
}