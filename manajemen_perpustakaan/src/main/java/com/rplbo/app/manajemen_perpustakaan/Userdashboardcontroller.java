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

import java.sql.*;
import java.util.HashSet;
import java.util.Set;

public class UserDashboardController {

    // ── Sidebar menu buttons ──────────────────────────────────────────────────
    @FXML private Button btnMenuKatalog;
    @FXML private Button btnMenuBookmark;
    @FXML private Button btnMenuPinjaman;

    // ── Halaman / Pages ───────────────────────────────────────────────────────
    @FXML private VBox pageKatalog;
    @FXML private VBox pageBookmark;
    @FXML private VBox pagePinjaman;

    // ── Katalog: search + tabel ───────────────────────────────────────────────
    @FXML private TextField searchField;
    @FXML private ComboBox<String> filterGenre;
    @FXML private TableView<BookModel> katalogTable;
    @FXML private TableColumn<BookModel, String> colBookmark;
    @FXML private TableColumn<BookModel, String> colJudul;
    @FXML private TableColumn<BookModel, String> colPengarang;
    @FXML private TableColumn<BookModel, String> colGenre;
    @FXML private TableColumn<BookModel, String> colStatus;
    @FXML private Label labelHasilCari;

    // ── Bookmark: tabel ───────────────────────────────────────────────────────
    @FXML private TableView<BookModel> bookmarkTable;
    @FXML private TableColumn<BookModel, String> colBmJudul;
    @FXML private TableColumn<BookModel, String> colBmPengarang;
    @FXML private TableColumn<BookModel, String> colBmGenre;
    @FXML private TableColumn<BookModel, String> colBmStatus;
    @FXML private Label labelJumlahBookmark;

    // ── Pinjaman: tabel ───────────────────────────────────────────────────────
    @FXML private TableView<BookModel> pinjamanTable;
    @FXML private TableColumn<BookModel, String> colPjJudul;
    @FXML private TableColumn<BookModel, String> colPjPengarang;
    @FXML private TableColumn<BookModel, String> colPjStatus;

    // ── State ─────────────────────────────────────────────────────────────────
    private String currentUsername = "user"; // default; diganti via setUsername()
    private ObservableList<BookModel> allBooks = FXCollections.observableArrayList();
    private FilteredList<BookModel> filteredBooks;
    private Set<Integer> bookmarkedIds = new HashSet<>();

    // ─────────────────────────────────────────────────────────────────────────
    // INIT
    // ─────────────────────────────────────────────────────────────────────────

    @FXML
    public void initialize() {
        setupKatalogTable();
        setupBookmarkTable();
        setupPinjamanTable();
        setupSearchAndFilter();
        showKatalog();
    }

    /** Dipanggil dari HelloController setelah login berhasil */
    public void setUsername(String username) {
        this.currentUsername = username;
        loadBookmarkedIds();
        loadAllBooks();
        loadBookmarkTable();
        loadPinjamanTable();
    }

    // ─────────────────────────────────────────────────────────────────────────
    // SETUP TABEL
    // ─────────────────────────────────────────────────────────────────────────

    private void setupKatalogTable() {
        colBookmark.setCellValueFactory(new PropertyValueFactory<>("bookmarked"));
        colJudul.setCellValueFactory(new PropertyValueFactory<>("title"));
        colPengarang.setCellValueFactory(new PropertyValueFactory<>("author"));
        colGenre.setCellValueFactory(new PropertyValueFactory<>("genre"));
        colStatus.setCellValueFactory(new PropertyValueFactory<>("status"));

        // Warna status
        colStatus.setCellFactory(col -> new TableCell<>() {
            @Override
            protected void updateItem(String item, boolean empty) {
                super.updateItem(item, empty);
                if (empty || item == null) { setText(null); setStyle(""); return; }
                setText(item);
                setStyle(item.equals("Tersedia")
                        ? "-fx-text-fill: #1e7d46; -fx-font-weight: bold;"
                        : "-fx-text-fill: #c0392b; -fx-font-weight: bold;");
            }
        });

        // Kolom bookmark bisa diklik
        colBookmark.setCellFactory(col -> new TableCell<>() {
            @Override
            protected void updateItem(String item, boolean empty) {
                super.updateItem(item, empty);
                if (empty || item == null) { setText(null); return; }
                setText(item);
                setStyle("-fx-cursor: hand; -fx-font-size: 16px; -fx-alignment: CENTER;");
                setOnMouseClicked(e -> {
                    BookModel book = getTableView().getItems().get(getIndex());
                    toggleBookmark(book);
                });
            }
        });
    }

    private void setupBookmarkTable() {
        colBmJudul.setCellValueFactory(new PropertyValueFactory<>("title"));
        colBmPengarang.setCellValueFactory(new PropertyValueFactory<>("author"));
        colBmGenre.setCellValueFactory(new PropertyValueFactory<>("genre"));
        colBmStatus.setCellValueFactory(new PropertyValueFactory<>("status"));
    }

    private void setupPinjamanTable() {
        colPjJudul.setCellValueFactory(new PropertyValueFactory<>("title"));
        colPjPengarang.setCellValueFactory(new PropertyValueFactory<>("author"));
        colPjStatus.setCellValueFactory(new PropertyValueFactory<>("status"));
    }

    // ─────────────────────────────────────────────────────────────────────────
    // SETUP SEARCH & FILTER
    // ─────────────────────────────────────────────────────────────────────────

    private void setupSearchAndFilter() {
        // Isi dropdown genre
        filterGenre.getItems().add("Semua Genre");
        filterGenre.getItems().addAll("Fiksi", "Non-Fiksi", "Komputer", "Sains", "Sejarah", "Sastra");
        filterGenre.setValue("Semua Genre");

        // FilteredList menempel ke allBooks
        filteredBooks = new FilteredList<>(allBooks, b -> true);
        katalogTable.setItems(filteredBooks);

        // Listener: search field
        searchField.textProperty().addListener((obs, oldVal, newVal) -> applyFilter());

        // Listener: filter genre
        filterGenre.valueProperty().addListener((obs, oldVal, newVal) -> applyFilter());
    }

    private void applyFilter() {
        String keyword = searchField.getText().toLowerCase().trim();
        String genre   = filterGenre.getValue();

        filteredBooks.setPredicate(book -> {
            // Filter genre
            boolean genreOk = genre == null
                    || genre.equals("Semua Genre")
                    || book.getGenre().equalsIgnoreCase(genre);

            // Filter keyword: cocok ke judul atau pengarang
            boolean keywordOk = keyword.isEmpty()
                    || book.getTitle().toLowerCase().contains(keyword)
                    || book.getAuthor().toLowerCase().contains(keyword);

            return genreOk && keywordOk;
        });

        // Update label hasil
        int total    = allBooks.size();
        int tampil   = filteredBooks.size();
        if (keyword.isEmpty() && (genre == null || genre.equals("Semua Genre"))) {
            labelHasilCari.setText("Menampilkan semua " + total + " koleksi");
        } else {
            labelHasilCari.setText("Ditemukan " + tampil + " dari " + total + " koleksi");
        }
    }

    // ─────────────────────────────────────────────────────────────────────────
    // LOAD DATA DARI DATABASE
    // ─────────────────────────────────────────────────────────────────────────

    private void loadBookmarkedIds() {
        bookmarkedIds.clear();
        String sql = "SELECT book_id FROM bookmarks WHERE username = ?";
        try (Connection conn = Database.connect();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setString(1, currentUsername);
            ResultSet rs = pstmt.executeQuery();
            while (rs.next()) bookmarkedIds.add(rs.getInt("book_id"));
        } catch (SQLException e) { e.printStackTrace(); }
    }

    private void loadAllBooks() {
        allBooks.clear();
        String sql = "SELECT id, title, author, genre, status FROM books ORDER BY title ASC";
        try (Connection conn = Database.connect();
             PreparedStatement pstmt = conn.prepareStatement(sql);
             ResultSet rs = pstmt.executeQuery()) {
            while (rs.next()) {
                allBooks.add(new BookModel(
                        rs.getInt("id"),
                        rs.getString("title"),
                        rs.getString("author"),
                        rs.getString("genre"),
                        rs.getString("status"),
                        bookmarkedIds.contains(rs.getInt("id"))
                ));
            }
        } catch (SQLException e) { e.printStackTrace(); }
        applyFilter();
    }

    private void loadBookmarkTable() {
        ObservableList<BookModel> bookmarks = FXCollections.observableArrayList();
        String sql = """
                SELECT b.id, b.title, b.author, b.genre, b.status
                FROM books b
                JOIN bookmarks bm ON b.id = bm.book_id
                WHERE bm.username = ?
                ORDER BY bm.created_at DESC
                """;
        try (Connection conn = Database.connect();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setString(1, currentUsername);
            ResultSet rs = pstmt.executeQuery();
            while (rs.next()) {
                bookmarks.add(new BookModel(
                        rs.getInt("id"),
                        rs.getString("title"),
                        rs.getString("author"),
                        rs.getString("genre"),
                        rs.getString("status"),
                        true
                ));
            }
        } catch (SQLException e) { e.printStackTrace(); }
        bookmarkTable.setItems(bookmarks);
        labelJumlahBookmark.setText(bookmarks.size() + " buku dalam favorit");
    }

    private void loadPinjamanTable() {
        ObservableList<BookModel> pinjaman = FXCollections.observableArrayList();
        String sql = """
                SELECT b.id, b.title, b.author, b.genre, l.status
                FROM loans l
                JOIN books b ON l.book_id = b.id
                WHERE l.username = ?
                ORDER BY l.borrow_date DESC
                """;
        try (Connection conn = Database.connect();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setString(1, currentUsername);
            ResultSet rs = pstmt.executeQuery();
            while (rs.next()) {
                pinjaman.add(new BookModel(
                        rs.getInt("id"),
                        rs.getString("title"),
                        rs.getString("author"),
                        rs.getString("genre"),
                        rs.getString("status"),
                        false
                ));
            }
        } catch (SQLException e) { e.printStackTrace(); }
        pinjamanTable.setItems(pinjaman);
    }

    // ─────────────────────────────────────────────────────────────────────────
    // TOGGLE BOOKMARK
    // ─────────────────────────────────────────────────────────────────────────

    private void toggleBookmark(BookModel book) {
        boolean isCurrentlyBookmarked = bookmarkedIds.contains(book.getId());

        if (isCurrentlyBookmarked) {
            // Hapus dari DB
            String sql = "DELETE FROM bookmarks WHERE username = ? AND book_id = ?";
            try (Connection conn = Database.connect();
                 PreparedStatement pstmt = conn.prepareStatement(sql)) {
                pstmt.setString(1, currentUsername);
                pstmt.setInt(2, book.getId());
                pstmt.executeUpdate();
                bookmarkedIds.remove(book.getId());
                book.setBookmarked(false);
                showToast("Bookmark dihapus: " + book.getTitle());
            } catch (SQLException e) { e.printStackTrace(); }
        } else {
            // Tambah ke DB
            String sql = "INSERT OR IGNORE INTO bookmarks (username, book_id) VALUES (?, ?)";
            try (Connection conn = Database.connect();
                 PreparedStatement pstmt = conn.prepareStatement(sql)) {
                pstmt.setString(1, currentUsername);
                pstmt.setInt(2, book.getId());
                pstmt.executeUpdate();
                bookmarkedIds.add(book.getId());
                book.setBookmarked(true);
                showToast("★ Ditambahkan ke favorit: " + book.getTitle());
            } catch (SQLException e) { e.printStackTrace(); }
        }

        // Refresh tabel bookmark
        katalogTable.refresh();
        loadBookmarkTable();
    }

    // ─────────────────────────────────────────────────────────────────────────
    // NAVIGASI HALAMAN
    // ─────────────────────────────────────────────────────────────────────────

    @FXML
    void showKatalog() {
        pageKatalog.setVisible(true);
        pageBookmark.setVisible(false);
        pagePinjaman.setVisible(false);
        setActiveMenu(btnMenuKatalog);
    }

    @FXML
    void showBookmark() {
        pageKatalog.setVisible(false);
        pageBookmark.setVisible(true);
        pagePinjaman.setVisible(false);
        setActiveMenu(btnMenuBookmark);
        loadBookmarkTable(); // refresh saat dibuka
    }

    @FXML
    void showPinjaman() {
        pageKatalog.setVisible(false);
        pageBookmark.setVisible(false);
        pagePinjaman.setVisible(true);
        setActiveMenu(btnMenuPinjaman);
        loadPinjamanTable(); // refresh saat dibuka
    }

    private void setActiveMenu(Button active) {
        String activeStyle      = "-fx-background-color: #1e5646; -fx-text-fill: white; -fx-alignment: center-left;";
        String inactiveStyle    = "-fx-background-color: transparent; -fx-text-fill: white; -fx-alignment: center-left;";
        btnMenuKatalog.setStyle(inactiveStyle);
        btnMenuBookmark.setStyle(inactiveStyle);
        btnMenuPinjaman.setStyle(inactiveStyle);
        active.setStyle(activeStyle);
    }

    // ─────────────────────────────────────────────────────────────────────────
    // TOMBOL AKSI
    // ─────────────────────────────────────────────────────────────────────────

    /** Tombol "Toggle Bookmark" di toolbar katalog */
    @FXML
    void handleToggleBookmark() {
        BookModel selected = katalogTable.getSelectionModel().getSelectedItem();
        if (selected == null) {
            showAlert("Pilih dulu buku yang ingin di-bookmark!");
            return;
        }
        toggleBookmark(selected);
    }

    /** Hapus bookmark yang dipilih dari halaman Bookmark */
    @FXML
    void handleHapusBookmark() {
        BookModel selected = bookmarkTable.getSelectionModel().getSelectedItem();
        if (selected == null) {
            showAlert("Pilih buku yang ingin dihapus dari favorit!");
            return;
        }
        toggleBookmark(selected);
    }

    /** Reset search field dan filter */
    @FXML
    void handleResetFilter() {
        searchField.clear();
        filterGenre.setValue("Semua Genre");
    }

    @FXML
    void handleLogout(ActionEvent event) {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("hello-view.fxml"));
            Scene scene = new Scene(loader.load(), 800, 500);
            Stage stage = (Stage) ((Node) event.getSource()).getScene().getWindow();
            stage.setTitle("Furab💜 Manajemen Perpustakaan - Login");
            stage.setScene(scene);
        } catch (Exception e) { e.printStackTrace(); }
    }

    // ─────────────────────────────────────────────────────────────────────────
    // HELPERS
    // ─────────────────────────────────────────────────────────────────────────

    private void showAlert(String message) {
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle("Info");
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }

    /** Tampilkan notifikasi kecil di label hasil cari (tidak pakai popup) */
    private void showToast(String message) {
        labelHasilCari.setText(message);
    }
}