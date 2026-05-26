package com.rplbo.app.manajemen_perpustakaan;

import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Node;
import javafx.scene.Scene;
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

    // ── Sidebar ───────────────────────────────────────────────────────────────
    @FXML private Button btnMenuDashboard;
    @FXML private Button btnMenuDataBuku;
    @FXML private Button btnMenuAnggota;
    @FXML private Button btnMenuLaporan;

    // ── Pages ─────────────────────────────────────────────────────────────────
    @FXML private VBox pageDashboard;
    @FXML private VBox pageDataBuku;

    // ── Dashboard stats ───────────────────────────────────────────────────────
    @FXML private Label totalBukuLabel;
    @FXML private Label totalAnggotaLabel;
    @FXML private Label totalPinjamLabel;

    // ── Form tambah/edit buku ─────────────────────────────────────────────────
    @FXML private TextField titleField;
    @FXML private TextField authorField;
    @FXML private TextField publisherField;
    @FXML private TextField yearField;
    @FXML private TextField pagesField;
    @FXML private ComboBox<String> genreComboBox;
    @FXML private Label imagePathLabel;
    @FXML private ImageView previewImage;
    @FXML private Button btnSimpan;
    @FXML private Label labelFormMode; // "Tambah Buku Baru" atau "Edit Buku"

    // ── Tabel buku ────────────────────────────────────────────────────────────
    @FXML private TableView<BookModel> tableBuku;
    @FXML private TableColumn<BookModel, Integer> colId;
    @FXML private TableColumn<BookModel, String>  colJudul;
    @FXML private TableColumn<BookModel, String>  colPengarang;
    @FXML private TableColumn<BookModel, String>  colGenre;
    @FXML private TableColumn<BookModel, String>  colStatus;

    private String selectedImagePath = "";
    private Integer editingBookId    = null; // null = mode tambah, ada nilai = mode edit
    private ObservableList<BookModel> bookList = FXCollections.observableArrayList();

    // ─────────────────────────────────────────────────────────────────────────
    @FXML
    public void initialize() {
        genreComboBox.getItems().addAll("Fiksi", "Non-Fiksi", "Komputer", "Sains", "Sejarah", "Sastra");
        setupTabelBuku();
        showDashboard();
    }

    // ── Setup tabel ───────────────────────────────────────────────────────────
    private void setupTabelBuku() {
        colId.setCellValueFactory(new PropertyValueFactory<>("id"));
        colJudul.setCellValueFactory(new PropertyValueFactory<>("title"));
        colPengarang.setCellValueFactory(new PropertyValueFactory<>("author"));
        colGenre.setCellValueFactory(new PropertyValueFactory<>("genre"));
        colStatus.setCellValueFactory(new PropertyValueFactory<>("status"));

        // Warna status
        colStatus.setCellFactory(col -> new TableCell<>() {
            @Override protected void updateItem(String item, boolean empty) {
                super.updateItem(item, empty);
                if (empty || item == null) { setText(null); setStyle(""); return; }
                setText(item);
                setStyle(item.equals("Tersedia")
                        ? "-fx-text-fill: #1e7d46; -fx-font-weight: bold;"
                        : "-fx-text-fill: #c0392b; -fx-font-weight: bold;");
            }
        });

        // Klik baris → isi form untuk edit
        tableBuku.getSelectionModel().selectedItemProperty().addListener((obs, old, selected) -> {
            if (selected != null) fillFormForEdit(selected);
        });

        tableBuku.setItems(bookList);
    }

    // ── Load data buku ────────────────────────────────────────────────────────
    private void loadBooks() {
        bookList.clear();
        String sql = "SELECT id, title, author, genre, status FROM books ORDER BY title ASC";
        try (Connection conn = Database.connect();
             PreparedStatement pstmt = conn.prepareStatement(sql);
             ResultSet rs = pstmt.executeQuery()) {
            while (rs.next()) {
                bookList.add(new BookModel(
                        rs.getInt("id"),
                        rs.getString("title"),
                        rs.getString("author"),
                        rs.getString("genre"),
                        rs.getString("status"),
                        false
                ));
            }
        } catch (SQLException e) { e.printStackTrace(); }
    }

    // ── Navigasi halaman ──────────────────────────────────────────────────────
    @FXML void showDashboard() {
        pageDashboard.setVisible(true);
        pageDataBuku.setVisible(false);
        setActiveMenu(btnMenuDashboard);
        loadDashboardStatistics();
    }

    @FXML void showDataBuku() {
        pageDashboard.setVisible(false);
        pageDataBuku.setVisible(true);
        setActiveMenu(btnMenuDataBuku);
        loadBooks();
        resetForm();
    }

    @FXML void showManajemenAnggota(ActionEvent event) {
        navigateTo("manajemen-anggota.fxml", "Manajemen Anggota - Furab💜", event);
    }

    @FXML void showLaporan(ActionEvent event) {
        navigateTo("laporan-kondisi.fxml", "Laporan Koleksi - Furab💜", event);
    }

    private void setActiveMenu(Button active) {
        String on  = "-fx-background-color: #1e5646; -fx-text-fill: white; -fx-alignment: center-left;";
        String off = "-fx-background-color: transparent; -fx-text-fill: white; -fx-alignment: center-left;";
        for (Button b : new Button[]{btnMenuDashboard, btnMenuDataBuku, btnMenuAnggota, btnMenuLaporan})
            if (b != null) b.setStyle(off);
        active.setStyle(on);
    }

    // ── Dashboard stats ───────────────────────────────────────────────────────
    private void loadDashboardStatistics() {
        totalBukuLabel.setText(String.valueOf(count("SELECT COUNT(*) FROM books")));
        totalAnggotaLabel.setText(String.valueOf(count("SELECT COUNT(*) FROM users WHERE role = 'user'")));
        totalPinjamLabel.setText(String.valueOf(count("SELECT COUNT(*) FROM loans WHERE status = 'Dipinjam'")));
    }

    private int count(String query) {
        try (Connection conn = Database.connect();
             PreparedStatement pstmt = conn.prepareStatement(query);
             ResultSet rs = pstmt.executeQuery()) {
            if (rs.next()) return rs.getInt(1);
        } catch (SQLException e) { e.printStackTrace(); }
        return 0;
    }

    // ── Form: pilih gambar ────────────────────────────────────────────────────
    @FXML void handleSelectImage() {
        FileChooser fc = new FileChooser();
        fc.getExtensionFilters().add(new FileChooser.ExtensionFilter("Image Files", "*.png", "*.jpg", "*.jpeg"));
        File file = fc.showOpenDialog(null);
        if (file != null) {
            selectedImagePath = file.toURI().toString();
            imagePathLabel.setText(file.getName());
            previewImage.setImage(new Image(selectedImagePath));
        }
    }

    // ── Simpan (tambah atau edit) ─────────────────────────────────────────────
    @FXML void handleSimpanBuku() {
        String title  = titleField.getText().trim();
        String author = authorField.getText().trim();
        if (title.isEmpty() || author.isEmpty()) {
            showAlert("Peringatan", "Judul dan Pengarang tidak boleh kosong!"); return;
        }
        int year = 0, pages = 0;
        try {
            if (!yearField.getText().isEmpty())  year  = Integer.parseInt(yearField.getText().trim());
            if (!pagesField.getText().isEmpty()) pages = Integer.parseInt(pagesField.getText().trim());
        } catch (NumberFormatException e) {
            showAlert("Peringatan", "Tahun dan Jumlah Halaman harus berupa angka!"); return;
        }

        if (editingBookId == null) {
            insertBook(title, author, publisherField.getText().trim(), year, pages,
                    genreComboBox.getValue(), selectedImagePath);
        } else {
            updateBook(editingBookId, title, author, publisherField.getText().trim(),
                    year, pages, genreComboBox.getValue(), selectedImagePath);
        }
    }

    private void insertBook(String title, String author, String publisher,
                            int year, int pages, String genre, String imgPath) {
        String sql = "INSERT INTO books (title, author, publisher, publish_year, page_count, genre, image_path) VALUES (?, ?, ?, ?, ?, ?, ?)";
        try (Connection conn = Database.connect();
             PreparedStatement p = conn.prepareStatement(sql)) {
            p.setString(1, title); p.setString(2, author); p.setString(3, publisher);
            p.setInt(4, year);     p.setInt(5, pages);     p.setString(6, genre);
            p.setString(7, imgPath);
            p.executeUpdate();
            showAlert("Sukses", "Buku berhasil ditambahkan!");
            resetForm(); loadBooks();
        } catch (SQLException e) { e.printStackTrace(); showAlert("Error", "Gagal menyimpan buku."); }
    }

    private void updateBook(int id, String title, String author, String publisher,
                            int year, int pages, String genre, String imgPath) {
        String sql = "UPDATE books SET title=?, author=?, publisher=?, publish_year=?, page_count=?, genre=?, image_path=? WHERE id=?";
        try (Connection conn = Database.connect();
             PreparedStatement p = conn.prepareStatement(sql)) {
            p.setString(1, title); p.setString(2, author); p.setString(3, publisher);
            p.setInt(4, year);     p.setInt(5, pages);     p.setString(6, genre);
            p.setString(7, imgPath); p.setInt(8, id);
            p.executeUpdate();
            showAlert("Sukses", "Buku berhasil diperbarui!");
            resetForm(); loadBooks();
        } catch (SQLException e) { e.printStackTrace(); showAlert("Error", "Gagal mengupdate buku."); }
    }

    // ── Hapus buku ────────────────────────────────────────────────────────────
    @FXML void handleHapusBuku() {
        BookModel selected = tableBuku.getSelectionModel().getSelectedItem();
        if (selected == null) { showAlert("Peringatan", "Pilih buku yang ingin dihapus!"); return; }

        Alert confirm = new Alert(Alert.AlertType.CONFIRMATION);
        confirm.setTitle("Konfirmasi Hapus");
        confirm.setHeaderText(null);
        confirm.setContentText("Yakin ingin menghapus buku \"" + selected.getTitle() + "\"?");
        confirm.showAndWait().ifPresent(resp -> {
            if (resp == ButtonType.OK) {
                String sql = "DELETE FROM books WHERE id = ?";
                try (Connection conn = Database.connect();
                     PreparedStatement p = conn.prepareStatement(sql)) {
                    p.setInt(1, selected.getId());
                    p.executeUpdate();
                    showAlert("Sukses", "Buku berhasil dihapus!");
                    resetForm(); loadBooks();
                } catch (SQLException e) { e.printStackTrace(); }
            }
        });
    }

    // ── Reset form ke mode tambah ─────────────────────────────────────────────
    @FXML void handleResetForm() { resetForm(); }

    private void resetForm() {
        editingBookId = null;
        titleField.clear(); authorField.clear(); publisherField.clear();
        yearField.clear();  pagesField.clear();
        genreComboBox.getSelectionModel().clearSelection();
        imagePathLabel.setText("Tidak ada");
        previewImage.setImage(null);
        selectedImagePath = "";
        if (labelFormMode != null) labelFormMode.setText("Tambah Buku Baru");
        if (btnSimpan != null) btnSimpan.setText("Tambah Buku");
        tableBuku.getSelectionModel().clearSelection();
    }

    // ── Isi form saat baris tabel diklik (mode edit) ──────────────────────────
    private void fillFormForEdit(BookModel book) {
        editingBookId = book.getId();
        // Ambil data lengkap dari DB (tabel mungkin tidak punya publisher dll)
        String sql = "SELECT * FROM books WHERE id = ?";
        try (Connection conn = Database.connect();
             PreparedStatement p = conn.prepareStatement(sql)) {
            p.setInt(1, book.getId());
            ResultSet rs = p.executeQuery();
            if (rs.next()) {
                titleField.setText(rs.getString("title"));
                authorField.setText(rs.getString("author"));
                publisherField.setText(rs.getString("publisher") != null ? rs.getString("publisher") : "");
                yearField.setText(rs.getInt("publish_year") != 0 ? String.valueOf(rs.getInt("publish_year")) : "");
                pagesField.setText(rs.getInt("page_count") != 0 ? String.valueOf(rs.getInt("page_count")) : "");
                genreComboBox.setValue(rs.getString("genre"));
                selectedImagePath = rs.getString("image_path") != null ? rs.getString("image_path") : "";
                if (!selectedImagePath.isEmpty()) {
                    previewImage.setImage(new Image(selectedImagePath));
                    imagePathLabel.setText("Gambar tersimpan");
                }
            }
        } catch (SQLException e) { e.printStackTrace(); }

        if (labelFormMode != null) labelFormMode.setText("Edit Buku (ID: " + book.getId() + ")");
        if (btnSimpan != null) btnSimpan.setText("Simpan Perubahan");
    }

    // ── Logout ────────────────────────────────────────────────────────────────
    @FXML void handleLogout(ActionEvent event) {
        navigateTo("hello-view.fxml", "Furab💜 - Login", event);
    }

    // ── Helpers ───────────────────────────────────────────────────────────────
    private void navigateTo(String fxml, String title, ActionEvent event) {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource(fxml));
            Scene scene = new Scene(loader.load(), 900, 600);
            Stage stage = (Stage) ((Node) event.getSource()).getScene().getWindow();
            stage.setTitle(title);
            stage.setScene(scene);
        } catch (Exception e) { e.printStackTrace(); }
    }

    private void showAlert(String title, String message) {
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle(title); alert.setHeaderText(null);
        alert.setContentText(message); alert.showAndWait();
    }
}