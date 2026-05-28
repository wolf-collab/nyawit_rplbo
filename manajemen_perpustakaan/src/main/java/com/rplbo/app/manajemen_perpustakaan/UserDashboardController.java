package com.rplbo.app.manajemen_perpustakaan;

import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.collections.transformation.FilteredList;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Node;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;

import java.io.IOException;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.regex.Pattern;

public class UserDashboardController {

    @FXML private VBox pageKatalog, pageFormPeminjaman, pageBookmark, pagePinjaman, pageLapor;
    @FXML private Button btnMenuKatalog, btnMenuBookmark, btnMenuFormPinjam, btnMenuPinjaman, btnMenuLapor;

    @FXML private TextField searchField;
    @FXML private ComboBox<String> filterGenre;
    @FXML private Label labelHasilCari, labelJumlahBookmark;
    @FXML private TextArea txtKeluhan;

    // Komponen Formulir Peminjaman Lengkap
    @FXML private TextField txtNama, txtNik, txtNoHp, txtJudulBuku;
    @FXML private DatePicker dpPinjam, dpKembali;

    @FXML private TableView<BookModel> katalogTable;
    @FXML private TableColumn<BookModel, String> colBookmark, colJudul, colPengarang, colGenre, colStatus;
    @FXML private TableView<BookModel> bookmarkTable;
    @FXML private TableColumn<BookModel, String> colBmJudul, colBmPengarang, colBmGenre, colBmStatus;
    @FXML private TableView<BookModel> pinjamanTable;
    @FXML private TableColumn<BookModel, String> colPjJudul, colPjPengarang, colPjStatus;

    private ObservableList<BookModel> bookList = FXCollections.observableArrayList();
    private ObservableList<BookModel> bookmarkList = FXCollections.observableArrayList();
    private ObservableList<BookModel> pinjamanList = FXCollections.observableArrayList();
    private FilteredList<BookModel> filteredData;

    private Integer selectedBookId = null;

    @FXML
    public void initialize() {
        filterGenre.getItems().addAll("Semua", "Fiksi", "Non-Fiksi", "Komputer", "Sains", "Sejarah", "Sastra");
        filterGenre.getSelectionModel().selectFirst();

        setupTables();
        loadBooks();
        loadPinjaman();
        setupSearchAndFilter();
        showKatalog();
    }

    private void setupTables() {
        colBookmark.setCellValueFactory(new PropertyValueFactory<>("bookmarked"));
        colJudul.setCellValueFactory(new PropertyValueFactory<>("title"));
        colPengarang.setCellValueFactory(new PropertyValueFactory<>("author"));
        colGenre.setCellValueFactory(new PropertyValueFactory<>("genre"));
        colStatus.setCellValueFactory(new PropertyValueFactory<>("status"));

        colBmJudul.setCellValueFactory(new PropertyValueFactory<>("title"));
        colBmPengarang.setCellValueFactory(new PropertyValueFactory<>("author"));
        colBmGenre.setCellValueFactory(new PropertyValueFactory<>("genre"));
        colBmStatus.setCellValueFactory(new PropertyValueFactory<>("status"));

        colPjJudul.setCellValueFactory(new PropertyValueFactory<>("title"));
        colPjPengarang.setCellValueFactory(new PropertyValueFactory<>("author"));
        colPjStatus.setCellValueFactory(new PropertyValueFactory<>("status"));
    }

    private void loadBooks() {
        bookList.clear();
        String sql = "SELECT * FROM books ORDER BY title ASC";
        try (Connection conn = Database.connect(); PreparedStatement p = conn.prepareStatement(sql); ResultSet rs = p.executeQuery()) {
            while (rs.next()) bookList.add(new BookModel(rs.getInt("id"), rs.getString("title"), rs.getString("author"), rs.getString("publisher"), rs.getString("genre"), rs.getString("status"), false));
        } catch (SQLException e) { e.printStackTrace(); }
    }

    private void loadPinjaman() {
        pinjamanList.clear();
        String sql = "SELECT b.title, b.author, l.status FROM loans l JOIN books b ON l.book_id = b.id WHERE l.username = 'user'";
        try (Connection conn = Database.connect(); PreparedStatement p = conn.prepareStatement(sql); ResultSet rs = p.executeQuery()) {
            while (rs.next()) pinjamanList.add(new BookModel(0, rs.getString("title"), rs.getString("author"), "", "", rs.getString("status"), false));
            pinjamanTable.setItems(pinjamanList);
        } catch (SQLException e) { e.printStackTrace(); }
    }

    private void setupSearchAndFilter() {
        filteredData = new FilteredList<>(bookList, p -> true);
        searchField.textProperty().addListener((obs, oldV, newV) -> filterData());
        filterGenre.valueProperty().addListener((obs, oldV, newV) -> filterData());
        katalogTable.setItems(filteredData);
    }

    private void filterData() {
        String filterText = searchField.getText();
        String genreFilter = filterGenre.getValue();

        filteredData.setPredicate(book -> {
            boolean matchesGenre = genreFilter.equals("Semua") || book.getGenre().equalsIgnoreCase(genreFilter);
            if (filterText == null || filterText.isEmpty()) return matchesGenre;

            Pattern pattern = Pattern.compile(Pattern.quote(filterText), Pattern.CASE_INSENSITIVE);
            boolean matchesSearch = pattern.matcher(book.getTitle()).find() || pattern.matcher(book.getAuthor()).find();
            return matchesGenre && matchesSearch;
        });
        labelHasilCari.setText("Ditemukan " + filteredData.size() + " buku.");
    }

    @FXML
    void handlePinjamBuku() {
        BookModel selected = katalogTable.getSelectionModel().getSelectedItem();
        if (selected == null) { showAlert(Alert.AlertType.WARNING, "Peringatan", "Pilih buku dari katalog terlebih dahulu!"); return; }
        if (!selected.getStatus().equalsIgnoreCase("Tersedia")) { showAlert(Alert.AlertType.ERROR, "Maaf", "Buku sedang dipinjam orang lain."); return; }

        selectedBookId = selected.getId();
        txtJudulBuku.setText(selected.getTitle());
        showFormPinjam();
    }

    @FXML
    void handleProsesPinjam() {
        String nama = txtNama.getText().trim();
        String nik = txtNik.getText().trim();
        String noHp = txtNoHp.getText().trim();

        if (selectedBookId == null || nama.isEmpty() || nik.isEmpty() || noHp.isEmpty() || dpPinjam.getValue() == null || dpKembali.getValue() == null) {
            showAlert(Alert.AlertType.WARNING, "Peringatan", "Semua kolom form wajib diisi!");
            return;
        }

        try (Connection conn = Database.connect()) {
            try (PreparedStatement p1 = conn.prepareStatement("INSERT INTO loans (username, book_id, borrow_date, status) VALUES ('user', ?, CURRENT_DATE, 'Dipinjam')")) {
                p1.setInt(1, selectedBookId); p1.executeUpdate();
            }
            try (PreparedStatement p2 = conn.prepareStatement("UPDATE books SET status = 'Dipinjam' WHERE id = ?")) {
                p2.setInt(1, selectedBookId); p2.executeUpdate();
            }

            showAlert(Alert.AlertType.INFORMATION, "Sukses!", "Buku berhasil dipinjam lewat formulir resmi!");
            handleBatalPinjam();
            loadBooks(); loadPinjaman(); showPinjaman();
        } catch (SQLException e) { e.printStackTrace(); }
    }

    @FXML
    void handleBatalPinjam() {
        txtNama.clear(); txtNik.clear(); txtNoHp.clear(); txtJudulBuku.clear();
        dpPinjam.setValue(null); dpKembali.setValue(null); selectedBookId = null;
    }

    @FXML
    void kirimLaporan() {
        String keluhan = txtKeluhan.getText().trim();
        if (keluhan.isEmpty()) { showAlert(Alert.AlertType.WARNING, "Peringatan", "Keluhan tidak boleh kosong!"); return; }

        String sql = "INSERT INTO complaints (username, message) VALUES ('user', ?)";
        try (Connection conn = Database.connect(); PreparedStatement p = conn.prepareStatement(sql)) {
            p.setString(1, keluhan); p.executeUpdate();
            showAlert(Alert.AlertType.INFORMATION, "Berhasil", "Laporan dikirim ke Admin!");
            txtKeluhan.clear();
        } catch (SQLException e) { e.printStackTrace(); }
    }

    @FXML
    void handleToggleBookmark() {
        BookModel selected = katalogTable.getSelectionModel().getSelectedItem();
        if (selected == null) return;
        if (selected.getBookmarked().equals("☆")) {
            selected.setBookmarked(true);
            if(!bookmarkList.contains(selected)) bookmarkList.add(selected);
        } else {
            selected.setBookmarked(false); bookmarkList.remove(selected);
        }
        katalogTable.refresh(); labelJumlahBookmark.setText(bookmarkList.size() + " buku dalam favorit");
    }

    @FXML
    void handleHapusBookmark() {
        BookModel selected = bookmarkTable.getSelectionModel().getSelectedItem();
        if (selected != null) {
            selected.setBookmarked(false); bookmarkList.remove(selected);
            katalogTable.refresh(); labelJumlahBookmark.setText(bookmarkList.size() + " buku dalam favorit");
        }
    }

    @FXML void handleResetFilter() { searchField.clear(); filterGenre.getSelectionModel().selectFirst(); }
    @FXML void showKatalog() { switchPage(pageKatalog, btnMenuKatalog); }
    @FXML void showFormPinjam() { switchPage(pageFormPeminjaman, btnMenuFormPinjam); }
    @FXML void showBookmark() { switchPage(pageBookmark, btnMenuBookmark); bookmarkTable.setItems(bookmarkList); }
    @FXML void showPinjaman() { switchPage(pagePinjaman, btnMenuPinjaman); }
    @FXML void showLapor() { switchPage(pageLapor, btnMenuLapor); }

    private void switchPage(VBox targetPage, Button targetBtn) {
        pageKatalog.setVisible(false); pageFormPeminjaman.setVisible(false);
        pageBookmark.setVisible(false); pagePinjaman.setVisible(false); pageLapor.setVisible(false);
        targetPage.setVisible(true);

        // Warna menu kalau lagi TIDAK diklik (teksnya abu-abu gelap #555)
        String off = "-fx-background-color: transparent; -fx-text-fill: #555; -fx-alignment: center-left; -fx-padding: 12 15; -fx-font-weight: bold; -fx-cursor: hand;";
        btnMenuKatalog.setStyle(off);
        btnMenuBookmark.setStyle(off);
        btnMenuFormPinjam.setStyle(off);
        btnMenuPinjaman.setStyle(off);
        btnMenuLapor.setStyle(off);

        // Warna menu kalau LAGI DIKLIK (background tosca, teks hijau gelap)
        targetBtn.setStyle("-fx-background-color: #e0f2f1; -fx-text-fill: #00695c; -fx-alignment: center-left; -fx-padding: 12 15; -fx-font-weight: bold; -fx-background-radius: 8; -fx-cursor: hand;");
    }

    @FXML void handleLogout(ActionEvent event) {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("hello-view.fxml"));
            Stage stage = (Stage) ((Node) event.getSource()).getScene().getWindow();
            stage.setTitle("Furab💜 - Login"); stage.setScene(new Scene(loader.load(), 800, 500));
        } catch (IOException e) { e.printStackTrace(); }
    }

    private void showAlert(Alert.AlertType type, String title, String msg) {
        Alert alert = new Alert(type); alert.setTitle(title); alert.setHeaderText(null); alert.setContentText(msg); alert.showAndWait();
    }
}