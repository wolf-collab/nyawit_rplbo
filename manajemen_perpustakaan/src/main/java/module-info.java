module com.rplbo.app.manajemen_perpustakaan {
    requires javafx.controls;
    requires javafx.fxml;
    requires java.sql;

    opens com.rplbo.app.manajemen_perpustakaan to javafx.fxml;
    exports com.rplbo.app.manajemen_perpustakaan;
}