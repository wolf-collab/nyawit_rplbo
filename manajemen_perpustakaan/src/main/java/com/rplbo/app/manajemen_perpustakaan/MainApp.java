package com.rplbo.app.manajemen_perpustakaan;

import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.stage.Stage;
import java.io.IOException;

public class MainApp extends Application {
    @Override
    public void start(Stage stage) throws IOException {
        Database.initializeDB();

        FXMLLoader fxmlLoader = new FXMLLoader(MainApp.class.getResource("hello-view.fxml"));
        Scene scene = new Scene(fxmlLoader.load(), 800, 500);

        stage.setTitle("Furab💜 Aplikasi Manajemen Perpustakaan");
        stage.setScene(scene);
        // INI KUNCINYA BIAR BISA FULLSCREEN:
        stage.setResizable(true);
        stage.show();
    }
    public static void main(String[] args) { launch(); }
}