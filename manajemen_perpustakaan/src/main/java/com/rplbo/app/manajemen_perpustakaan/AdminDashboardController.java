package com.rplbo.app.manajemen_perpustakaan;

import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Node;
import javafx.scene.Scene;
import javafx.scene.chart.PieChart;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.VBox;
import javafx.stage.FileChooser;
import javafx.stage.Stage;

import java.io.File;
import java.sql.*;

public class AdminDashboardController {

    @FXML private Button btnMenuDashboard, btnMenuDataBuku, btnMenuAnggota, btnMenuPinjaman;
    @FXML private VBox pageDashboard, pageDataBuku, pageDataAnggota, pagePinjaman;
    @FXML private Label totalBukuLabel, totalAnggotaLabel, totalPinjamLabel;

    @FXML private PieChart chartBuku;
    @FXML private ListView<String> listKeluhan;

    @FXML private TextField titleField, authorField, publisherField, yearField, pagesField;
    @FXML private ComboBox<String> genreComboBox;
    @FXML private Label imagePathLabel;
    @FXML private ImageView previewImage;

    @FXML private TableView<BookModel> bookTable;
    @FXML private TableColumn<BookModel, String> colJudul, colPengarang, colPenerbit, colGenre;

    @FXML private TableView<UserModel> tableAnggota;
    @FXML private TableColumn<UserModel, Integer> colAnggotaId;
    @FXML private TableColumn<UserModel, String> colAnggotaUsername;

    @FXML private TableView<LoanModel> tablePinjamanAdmin;
    @FXML private TableColumn<LoanModel, Integer> colPinjamId;
    @FXML private TableColumn<LoanModel, String> colPinjamUser, colPinjamBuku, colPinjamTanggal, colPinjamStatus;

    private String selectedImagePath = "";
    private Integer editingBookId = null;

    private ObservableList<BookModel> bookList = FXCollections.observableArrayList();
    private ObservableList<UserModel> anggotaList = FXCollections.observableArrayList();
    private ObservableList<LoanModel> pinjamanList = FXCollections.observableArrayList();

    @FXML
    public void initialize() {
        genreComboBox.getItems().addAll("Fiksi", "Non-Fiksi", "Komputer", "Sains", "Sejarah", "Sastra");

        colJudul.setCellValueFactory(new PropertyValueFactory<>("title"));
        colPengarang.setCellValueFactory(new PropertyValueFactory<>("author"));
        colPenerbit.setCellValueFactory(new PropertyValueFactory<>("publisher"));
        colGenre.setCellValueFactory(new PropertyValueFactory<>("genre"));
        bookTable.getSelectionModel().selectedItemProperty().addListener((obs, old, selected) -> {
            if (selected != null) fillFormForEdit(selected);
        });

        colAnggotaId.setCellValueFactory(new PropertyValueFactory<>("id"));
        colAnggotaUsername.setCellValueFactory(new PropertyValueFactory<>("username"));

        colPinjamId.setCellValueFactory(new PropertyValueFactory<>("id"));
        colPinjamUser.setCellValueFactory(new PropertyValueFactory<>("username"));
        colPinjamBuku.setCellValueFactory(new PropertyValueFactory<>("bookTitle"));
        colPinjamTanggal.setCellValueFactory(new PropertyValueFactory<>("date"));
        colPinjamStatus.setCellValueFactory(new PropertyValueFactory<>("status"));

        showDashboard();
    }

    private void loadBooks() {
        bookList.clear();
        String sql = "SELECT * FROM books ORDER BY title ASC";
        try (Connection conn = Database.connect(); PreparedStatement p = conn.prepareStatement(sql); ResultSet rs = p.executeQuery()) {
            while (rs.next()) bookList.add(new BookModel(rs.getInt("id"), rs.getString("title"), rs.getString("author"), rs.getString("publisher"), rs.getString("genre"), rs.getString("status"), false));
            bookTable.setItems(bookList);
        } catch (SQLException e) { e.printStackTrace(); }
    }

    private void loadAnggota() {
        anggotaList.clear();
        String sql = "SELECT id, username FROM users WHERE role = 'user' ORDER BY id ASC";
        try (Connection conn = Database.connect(); PreparedStatement p = conn.prepareStatement(sql); ResultSet rs = p.executeQuery()) {
            while (rs.next()) anggotaList.add(new UserModel(rs.getInt("id"), rs.getString("username")));
            tableAnggota.setItems(anggotaList);
        } catch (SQLException e) { e.printStackTrace(); }
    }

    private void loadPinjaman() {
        pinjamanList.clear();
        String sql = "SELECT l.id, l.username, b.title, l.borrow_date, l.status FROM loans l JOIN books b ON l.book_id = b.id ORDER BY l.borrow_date DESC";
        try (Connection conn = Database.connect(); PreparedStatement p = conn.prepareStatement(sql); ResultSet rs = p.executeQuery()) {
            while (rs.next()) pinjamanList.add(new LoanModel(rs.getInt("id"), rs.getString("username"), rs.getString("title"), rs.getString("borrow_date"), rs.getString("status")));
            tablePinjamanAdmin.setItems(pinjamanList);
        } catch (SQLException e) { e.printStackTrace(); }
    }

    @FXML void showDashboard() { switchPage(pageDashboard, btnMenuDashboard); loadDashboardStatistics(); }
    @FXML void showDataBuku() { switchPage(pageDataBuku, btnMenuDataBuku); loadBooks(); resetForm(); }
    @FXML void showDataAnggota() { switchPage(pageDataAnggota, btnMenuAnggota); loadAnggota(); }
    @FXML void showPinjaman() { switchPage(pagePinjaman, btnMenuPinjaman); loadPinjaman(); }

    private void switchPage(VBox targetPage, Button targetButton) {
        pageDashboard.setVisible(false); pageDataBuku.setVisible(false);
        pageDataAnggota.setVisible(false); pagePinjaman.setVisible(false);
        targetPage.setVisible(true);

        String off = "-fx-background-color: transparent; -fx-text-fill: white; -fx-alignment: center-left;";
        btnMenuDashboard.setStyle(off); btnMenuDataBuku.setStyle(off);
        btnMenuAnggota.setStyle(off); btnMenuPinjaman.setStyle(off);
        targetButton.setStyle("-fx-background-color: #1e5646; -fx-text-fill: white; -fx-alignment: center-left;");
    }

    private void loadDashboardStatistics() {
        totalBukuLabel.setText(String.valueOf(count("SELECT COUNT(*) FROM books")));
        totalAnggotaLabel.setText(String.valueOf(count("SELECT COUNT(*) FROM users WHERE role = 'user'")));
        totalPinjamLabel.setText(String.valueOf(count("SELECT COUNT(*) FROM loans WHERE status = 'Dipinjam'")));

        // Load PieChart Data
        ObservableList<PieChart.Data> pieData = FXCollections.observableArrayList(
                new PieChart.Data("Tersedia", count("SELECT COUNT(*) FROM books WHERE status = 'Tersedia'")),
                new PieChart.Data("Dipinjam", count("SELECT COUNT(*) FROM books WHERE status = 'Dipinjam'"))
        );
        chartBuku.setData(pieData);

        // Load Laporan Keluhan
        ObservableList<String> keluhanList = FXCollections.observableArrayList();
        try (Connection conn = Database.connect(); PreparedStatement p = conn.prepareStatement("SELECT username, message, tanggal FROM complaints ORDER BY id DESC"); ResultSet rs = p.executeQuery()) {
            while (rs.next()) keluhanList.add("[" + rs.getString("tanggal") + "] " + rs.getString("username") + ":\n" + rs.getString("message"));
            listKeluhan.setItems(keluhanList);
        } catch (SQLException e) { e.printStackTrace(); }
    }

    private int count(String query) {
        try (Connection conn = Database.connect(); PreparedStatement p = conn.prepareStatement(query); ResultSet rs = p.executeQuery()) {
            if (rs.next()) return rs.getInt(1);
        } catch (SQLException e) { e.printStackTrace(); }
        return 0;
    }

    @FXML void handleSelectImage() {
        FileChooser fc = new FileChooser();
        fc.getExtensionFilters().add(new FileChooser.ExtensionFilter("Image Files", "*.png", "*.jpg", "*.jpeg"));
        File file = fc.showOpenDialog(null);
        if (file != null) {
            selectedImagePath = file.toURI().toString(); imagePathLabel.setText(file.getName()); previewImage.setImage(new Image(selectedImagePath));
        }
    }

    @FXML void handleAddBook() {
        String title = titleField.getText().trim();
        String author = authorField.getText().trim();
        if (title.isEmpty() || author.isEmpty()) { showAlert("Peringatan", "Judul dan Pengarang tidak boleh kosong!"); return; }

        int year = 0, pages = 0;
        try {
            if (!yearField.getText().isEmpty()) year = Integer.parseInt(yearField.getText().trim());
            if (!pagesField.getText().isEmpty()) pages = Integer.parseInt(pagesField.getText().trim());
        } catch (NumberFormatException e) { showAlert("Peringatan", "Tahun dan Halaman harus angka!"); return; }
        String genre = genreComboBox.getValue() != null ? genreComboBox.getValue() : "Lainnya";

        String sql = editingBookId == null
                ? "INSERT INTO books (title, author, publisher, publish_year, page_count, genre, image_path) VALUES (?, ?, ?, ?, ?, ?, ?)"
                : "UPDATE books SET title=?, author=?, publisher=?, publish_year=?, page_count=?, genre=?, image_path=? WHERE id=" + editingBookId;

        try (Connection conn = Database.connect(); PreparedStatement p = conn.prepareStatement(sql)) {
            p.setString(1, title); p.setString(2, author); p.setString(3, publisherField.getText().trim());
            p.setInt(4, year); p.setInt(5, pages); p.setString(6, genre); p.setString(7, selectedImagePath);
            p.executeUpdate();
            showAlert("Sukses", editingBookId == null ? "Buku ditambahkan!" : "Buku diperbarui!");
            resetForm(); loadBooks();
        } catch (SQLException e) { e.printStackTrace(); }
    }

    private void resetForm() {
        editingBookId = null; titleField.clear(); authorField.clear(); publisherField.clear();
        yearField.clear(); pagesField.clear(); genreComboBox.getSelectionModel().clearSelection();
        imagePathLabel.setText("Tidak ada"); previewImage.setImage(null); selectedImagePath = "";
        bookTable.getSelectionModel().clearSelection();
    }

    private void fillFormForEdit(BookModel book) {
        editingBookId = book.getId();
        try (Connection conn = Database.connect(); PreparedStatement p = conn.prepareStatement("SELECT * FROM books WHERE id = ?")) {
            p.setInt(1, book.getId()); ResultSet rs = p.executeQuery();
            if (rs.next()) {
                titleField.setText(rs.getString("title")); authorField.setText(rs.getString("author"));
                publisherField.setText(rs.getString("publisher")); yearField.setText(String.valueOf(rs.getInt("publish_year")));
                pagesField.setText(String.valueOf(rs.getInt("page_count"))); genreComboBox.setValue(rs.getString("genre"));
                selectedImagePath = rs.getString("image_path") != null ? rs.getString("image_path") : "";
                if (!selectedImagePath.isEmpty()) previewImage.setImage(new Image(selectedImagePath));
            }
        } catch (SQLException e) { e.printStackTrace(); }
    }

    @FXML void bukaFormPeminjaman(ActionEvent event) {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("Peminjaman_view.fxml"));
            Stage stage = new Stage();
            stage.setTitle("Form Peminjaman Manual / Offline");
            stage.setScene(new Scene(loader.load(), 1080, 720));
            stage.show();
        } catch (Exception e) { e.printStackTrace(); }
    }

    @FXML void handleLogout(ActionEvent event) {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("hello-view.fxml"));
            Stage stage = (Stage) ((Node) event.getSource()).getScene().getWindow();
            stage.setTitle("Furab💜 - Login"); stage.setScene(new Scene(loader.load(), 800, 500));
        } catch (Exception e) { e.printStackTrace(); }
    }

    private void showAlert(String title, String message) {
        Alert alert = new Alert(Alert.AlertType.INFORMATION); alert.setTitle(title); alert.setHeaderText(null); alert.setContentText(message); alert.showAndWait();
    }

    public static class UserModel {
        private final int id; private final String username;
        public UserModel(int id, String username) { this.id = id; this.username = username; }
        public int getId() { return id; } public String getUsername() { return username; }
    }

    public static class LoanModel {
        private final int id; private final String username, bookTitle, date, status;
        public LoanModel(int id, String username, String bookTitle, String date, String status) {
            this.id = id; this.username = username; this.bookTitle = bookTitle; this.date = date; this.status = status;
        }
        public int getId() { return id; } public String getUsername() { return username; }
        public String getBookTitle() { return bookTitle; } public String getDate() { return date; }
        public String getStatus() { return status; }
    }
}