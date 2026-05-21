package com.rplbo.app.manajemen_perpustakaan;

import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Node;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.VBox;
import javafx.stage.FileChooser;
import javafx.stage.Stage;

import java.io.File;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

public class AdminDashboardController {
    @FXML private Button btnMenuDashboard;
    @FXML private Button btnMenuDataBuku;
    @FXML private VBox pageDashboard;
    @FXML private VBox pageDataBuku;

    @FXML private Label totalBukuLabel;
    @FXML private Label totalAnggotaLabel;
    @FXML private Label totalPinjamLabel;

    @FXML private TextField titleField;
    @FXML private TextField authorField;
    @FXML private Label imagePathLabel;
    @FXML private ImageView previewImage;
    @FXML private TextField publisherField;
    @FXML private TextField yearField;
    @FXML private TextField pagesField;
    @FXML private ComboBox<String> genreComboBox;
    private String selectedImagePath = "";

    @FXML
    public void initialize() {
        showDashboard();
        genreComboBox.getItems().addAll("Fiksi", "Non-Fiksi", "Komputer", "Sains", "Sejarah", "Sastra");
    }

    @FXML
    void showDashboard() {
        pageDashboard.setVisible(true);
        pageDataBuku.setVisible(false);

        btnMenuDashboard.setStyle("-fx-background-color: #1e5646; -fx-text-fill: white; -fx-alignment: center-left;");
        btnMenuDataBuku.setStyle("-fx-background-color: transparent; -fx-text-fill: white; -fx-alignment: center-left;");

        loadDashboardStatistics();
    }

    @FXML
    void showDataBuku() {
        pageDashboard.setVisible(false);
        pageDataBuku.setVisible(true);

        btnMenuDashboard.setStyle("-fx-background-color: transparent; -fx-text-fill: white; -fx-alignment: center-left;");
        btnMenuDataBuku.setStyle("-fx-background-color: #1e5646; -fx-text-fill: white; -fx-alignment: center-left;");
    }

    private void loadDashboardStatistics() {
        totalBukuLabel.setText(String.valueOf(getCountFromDatabase("SELECT COUNT(*) FROM books")));
        totalAnggotaLabel.setText(String.valueOf(getCountFromDatabase("SELECT COUNT(*) FROM users WHERE role = 'user'")));
        totalPinjamLabel.setText(String.valueOf(getCountFromDatabase("SELECT COUNT(*) FROM loans WHERE status = 'Dipinjam'")));
    }

    private int getCountFromDatabase(String query) {
        try (Connection conn = Database.connect();
             PreparedStatement pstmt = conn.prepareStatement(query);
             ResultSet rs = pstmt.executeQuery()) {
            if (rs.next()) return rs.getInt(1);
        } catch (SQLException e) { e.printStackTrace(); }
        return 0;
    }

    @FXML
    void handleSelectImage() {
        FileChooser fileChooser = new FileChooser();
        fileChooser.getExtensionFilters().add(new FileChooser.ExtensionFilter("Image Files", "*.png", "*.jpg", "*.jpeg"));
        File file = fileChooser.showOpenDialog(null);

        if (file != null) {
            selectedImagePath = file.toURI().toString();
            imagePathLabel.setText(file.getName());
            previewImage.setImage(new Image(selectedImagePath));
        }
    }

    @FXML
    void handleAddBook() {
        String title = titleField.getText();
        String author = authorField.getText();
        String publisher = publisherField.getText();
        String yearText = yearField.getText();
        String pagesText = pagesField.getText();
        String genre = genreComboBox.getValue();

        if (title.isEmpty() || author.isEmpty()) {
            showAlert("Peringatan", "Judul dan Pengarang tidak boleh kosong!");
            return;
        }

        // Validasi angka untuk tahun dan halaman
        int year = 0, pages = 0;
        try {
            if (!yearText.isEmpty()) year = Integer.parseInt(yearText);
            if (!pagesText.isEmpty()) pages = Integer.parseInt(pagesText);
        } catch (NumberFormatException e) {
            showAlert("Peringatan", "Tahun dan Jumlah Halaman harus berupa angka!");
            return;
        }

        String sql = "INSERT INTO books (title, author, publisher, publish_year, page_count, genre, image_path) VALUES (?, ?, ?, ?, ?, ?, ?)";
        try (Connection conn = Database.connect();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {

            pstmt.setString(1, title);
            pstmt.setString(2, author);
            pstmt.setString(3, publisher);
            pstmt.setInt(4, year);
            pstmt.setInt(5, pages);
            pstmt.setString(6, genre);
            pstmt.setString(7, selectedImagePath);
            pstmt.executeUpdate();
            
            titleField.clear(); authorField.clear(); publisherField.clear();
            yearField.clear(); pagesField.clear(); genreComboBox.getSelectionModel().clearSelection();
            imagePathLabel.setText("Tidak ada"); previewImage.setImage(null); selectedImagePath = "";

            showAlert("Sukses", "Buku Berhasil Ditambahkan!");
        } catch (SQLException e) {
            e.printStackTrace();
            showAlert("Error", "Gagal menyimpan buku ke database.");
        }
    }

    private void showAlert(String title, String message) {
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }

    @FXML
    void handleLogout(ActionEvent event) {
        try {
            FXMLLoader fxmlLoader = new FXMLLoader(getClass().getResource("hello-view.fxml"));
            Scene scene = new Scene(fxmlLoader.load(), 800, 500);
            Stage stage = (Stage) ((Node) event.getSource()).getScene().getWindow();
            stage.setTitle("Furab💜 Manajemen Perpustakaan - Login");
            stage.setScene(scene);
        } catch (Exception e) { e.printStackTrace(); }
    }
}