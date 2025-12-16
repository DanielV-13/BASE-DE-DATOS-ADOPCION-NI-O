module org.example.agenciaadopcion {
    // JavaFX y UI
    requires javafx.controls;
    requires javafx.fxml;
    requires javafx.web;
    requires javafx.swing; // Necesario para SwingFXUtils si lo usas

    // Librerías de Terceros UI
    requires org.controlsfx.controls;
    requires com.dlsc.formsfx;
    requires net.synedra.validatorfx;
    requires org.kordamp.ikonli.javafx;
    requires org.kordamp.bootstrapfx.core;
    requires eu.hansolo.tilesfx;
    requires com.almasb.fxgl.all;

    // Java Core
    requires java.desktop;
    requires java.sql;

    // --- AQUÍ ESTÁ EL ARREGLO ---

    // 1. Para Excel (Apache POI)
    requires org.apache.poi.poi;
    requires org.apache.poi.ooxml;

    // 2. Para PDF (iText)
    requires itextpdf;

    // Exportaciones
    opens org.example.agenciaadopcion to javafx.fxml;
    exports org.example.agenciaadopcion;
    opens org.example.agenciaadopcion.entidadesBD to javafx.base;
}
