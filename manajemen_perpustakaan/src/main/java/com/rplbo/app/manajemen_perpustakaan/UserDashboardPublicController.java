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

public class UserDashboardPublicController {

    @FXML private VBox pageKatalog;
    @FXML private Button btnMenuKatalog;

    @FXML private TextField searchField;
    @FXML private ComboBox<String> filterGenre;
    @FXML private Label labelHasilCari;

    @FXML private TableView<BookModel> katalogTable;
    @FXML private TableColumn<BookModel, String> colJudul, colPengarang, colGenre, colStatus;

    private ObservableList<BookModel> bookList = FXCollections.observableArrayList();
    private FilteredList<BookModel> filteredData;

    @FXML
    public void initialize() {
        filterGenre.getItems().addAll("Semua", "Fiksi", "Non-Fiksi", "Komputer", "Sains", "Sejarah", "Sastra");
        filterGenre.getSelectionModel().selectFirst();

        setupTables();
        loadBooks();
        setupSearchAndFilter();
        showKatalog();
    }

    private void setupTables() {
        colJudul.setCellValueFactory(new PropertyValueFactory<>("title"));
        colPengarang.setCellValueFactory(new PropertyValueFactory<>("author"));
        colGenre.setCellValueFactory(new PropertyValueFactory<>("genre"));
        colStatus.setCellValueFactory(new PropertyValueFactory<>("status"));
    }

    private void loadBooks() {
        bookList.clear();
        String sql = "SELECT * FROM books ORDER BY title ASC";
        try (Connection conn = Database.connect(); PreparedStatement p = conn.prepareStatement(sql); ResultSet rs = p.executeQuery()) {
            while (rs.next()) bookList.add(new BookModel(rs.getInt("id"), rs.getString("title"), rs.getString("author"), rs.getString("publisher"), rs.getString("genre"), rs.getString("status"), false));
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

    @FXML void handleResetFilter() { searchField.clear(); filterGenre.getSelectionModel().selectFirst(); }
    @FXML void showKatalog() { pageKatalog.setVisible(true); }

    private void showAlert(Alert.AlertType type, String title, String msg) {
        Alert alert = new Alert(type); alert.setTitle(title); alert.setHeaderText(null); alert.setContentText(msg); alert.showAndWait();
    }
}
