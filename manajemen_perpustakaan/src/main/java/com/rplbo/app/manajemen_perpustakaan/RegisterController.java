package com.rplbo.app.manajemen_perpustakaan;

import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.Alert;
import javafx.scene.control.PasswordField;
import javafx.scene.control.TextField;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;

public class RegisterController {

    @FXML
    private TextField regUsernameField;

    @FXML
    private PasswordField regPasswordField;

    @FXML
    private PasswordField regConfirmPasswordField;

    @FXML
    void handleRegister(ActionEvent event) {
        String username = regUsernameField.getText();
        String password = regPasswordField.getText();
        String confirmPassword = regConfirmPasswordField.getText();

        if (username.isEmpty() || password.isEmpty() || confirmPassword.isEmpty()) {
            showAlert(Alert.AlertType.WARNING, "Peringatan", "Semua kolom wajib diisi!");
            return;
        }

        if (!password.equals(confirmPassword)) {
            showAlert(Alert.AlertType.ERROR, "Kesalahan", "Password dan Konfirmasi Password tidak cocok!");
            return;
        }

        // Proses simpan ke database dengan role default 'user'
        if (registerUser(username, password)) {
            showAlert(Alert.AlertType.INFORMATION, "Sukses", "Akun berhasil didaftarkan! Silakan login.");
            goToLogin(event); // Otomatis kembali ke halaman login setelah sukses
        } else {
            showAlert(Alert.AlertType.ERROR, "Gagal", "Username sudah digunakan atau terjadi kesalahan sistem.");
        }
    }

    private boolean registerUser(String username, String password) {
        // Query INSERT data dengan role 'user'
        String query = "INSERT INTO users (username, password, role) VALUES (?, ?, 'user')";

        try (Connection conn = Database.connect();
             PreparedStatement pstmt = conn.prepareStatement(query)) {

            pstmt.setString(1, username);
            pstmt.setString(2, password);

            pstmt.executeUpdate();
            return true;

        } catch (SQLException e) {
            System.out.println("Gagal mendaftar: " + e.getMessage());
            return false;
        }
    }

    @FXML
    void goToLogin(ActionEvent event) {
        try {
            javafx.fxml.FXMLLoader fxmlLoader = new javafx.fxml.FXMLLoader(getClass().getResource("hello-view.fxml"));
            javafx.scene.Scene scene = new javafx.scene.Scene(fxmlLoader.load(), 800, 500);
            javafx.stage.Stage stage = (javafx.stage.Stage) ((javafx.scene.Node) event.getSource()).getScene().getWindow();
            stage.setTitle("Perpustakaan Furab");
            stage.setScene(scene);
            stage.show();
        } catch (java.io.IOException e) {
            e.printStackTrace();
        }
    }

    private void showAlert(Alert.AlertType alertType, String title, String message) {
        Alert alert = new Alert(alertType);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }
}