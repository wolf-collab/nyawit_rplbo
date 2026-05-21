package com.rplbo.app.manajemen_perpustakaan;

import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.Alert;
import javafx.scene.control.Alert.AlertType;
import javafx.scene.control.DatePicker;
import javafx.scene.control.TextField;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.time.format.DateTimeFormatter;

public class PeminjamanController {

    @FXML private TextField txtNama;
    @FXML private TextField txtNik;
    @FXML private TextField txtNoHp;
    @FXML private TextField txtJudulBuku;
    @FXML private DatePicker dpPinjam;
    @FXML private DatePicker dpKembali;

    @FXML
    void handlePinjam(ActionEvent event) {
        String nama = txtNama.getText().trim();
        String nik = txtNik.getText().trim();
        String noHp = txtNoHp.getText().trim();
        String judul = txtJudulBuku.getText().trim();


        if (nama.isEmpty() || nik.isEmpty() || noHp.isEmpty() || judul.isEmpty() || dpPinjam.getValue() == null || dpKembali.getValue() == null) {
            showMsg("Peringatan", "Semua kolom bertanda * wajib diisi!");
            return;
        }

        // Mengubah objek tanggal DatePicker ke String format hari/bulan/tahun
        String tglPinjam = dpPinjam.getValue().format(DateTimeFormatter.ofPattern("dd/MM/yyyy"));
        String tglKembali = dpKembali.getValue().format(DateTimeFormatter.ofPattern("dd/MM/yyyy"));

        String sql = "INSERT INTO peminjaman(nama, nik, no_hp, judul_buku, tgl_pinjam, tgl_kembali) VALUES(?,?,?,?,?,?)";

        try (Connection conn = Database.connect();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {

            pstmt.setString(1, nama);
            pstmt.setString(2, nik);
            pstmt.setString(3, noHp);
            pstmt.setString(4, judul);
            pstmt.setString(5, tglPinjam);
            pstmt.setString(6, tglKembali);

            pstmt.executeUpdate();
            showMsg("Sukses", "Data Peminjaman Berhasil Disimpan Ke Database!");
            handleBatal(null); // Otomatis reset kolom form setelah sukses

        } catch (SQLException e) {
            showMsg("Database Error", e.getMessage());
        }
    }

    @FXML
    void handleBatal(ActionEvent event) {
        txtNama.clear();
        txtNik.clear();
        txtNoHp.clear();
        txtJudulBuku.clear();
        dpPinjam.setValue(null);
        dpKembali.setValue(null);
    }

    private void showMsg(String title, String content) {
        Alert alert = new Alert(AlertType.INFORMATION);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(content);
        alert.showAndWait();
    }
}