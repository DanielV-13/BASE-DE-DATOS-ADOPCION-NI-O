package org.example.agenciaadopcion;


import javafx.application.Application;
import javafx.application.Platform;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.effect.DropShadow;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.*;
import javafx.scene.paint.Color;
import javafx.scene.text.Font;
import javafx.scene.text.FontWeight;
import javafx.stage.FileChooser;
import javafx.stage.Stage;
import org.example.agenciaadopcion.entidadesBD.Nino;
import org.example.agenciaadopcion.entidadesBD.Solicitante;
import org.example.agenciaadopcion.reportes.GestorReportes;
import org.example.agenciaadopcion.reportes.ReporteAdopcion;
import org.example.agenciaadopcion.reportes.ReporteEstadoProceso;
import org.example.agenciaadopcion.reportes.ReporteMotivoCancelacion;
import javafx.stage.FileChooser;
import org.example.agenciaadopcion.reportes.*;


import java.io.File;

public class Aplicacion extends Application {

    private BorderPane mainLayout;
    private VBox sideMenu;
    private StackPane contentArea;
    private boolean menuExpanded = false;

    @Override
    public void start(Stage stage) {
        mainLayout = new BorderPane();
        createSideMenu();
        createContentArea();
        mainLayout.setLeft(sideMenu);
        mainLayout.setCenter(contentArea);

        Scene scene = new Scene(mainLayout, 1000, 700);
        try {
            Image icon = new Image(getClass().getResourceAsStream("/heart.png"));
            stage.getIcons().add(icon);
        } catch (Exception e) {
            System.out.println("No se pudo cargar el ícono de la app");
        }

        stage.setTitle("Agencia de Adopción");
        stage.setScene(scene);
        stage.setResizable(true);
        stage.show();
        showHomePage();
    }

    // --- MENÚ LATERAL ---
    private void createSideMenu() {
        sideMenu = new VBox(15);
        sideMenu.setPadding(new Insets(20, 10, 20, 10));
        sideMenu.setStyle("-fx-background-color: #34495E; -fx-min-width: 60px;");

        Label menuTitle = new Label("Menú");
        menuTitle.setFont(Font.font("Arial", FontWeight.BOLD, 18));
        menuTitle.setTextFill(Color.WHITE);
        menuTitle.setMaxWidth(Double.MAX_VALUE);
        menuTitle.setAlignment(Pos.CENTER);
        menuTitle.setVisible(false);
        menuTitle.managedProperty().bind(menuTitle.visibleProperty());

        Button btnAdopta = createMenuButton("/casa.png", "Adopta");
        Button btnSubir = createMenuButton("/archivo.png", "Subir Excel");
        Button btnSolicitantes = createMenuButton("/familia.png", "Solicitantes");
        Button btnNinos = createMenuButton("/chico.png", "Niños");

        Button btnReportes = createMenuButton("/reporte.png", "Reportes");
        btnReportes.setOnAction(e -> showReportesPage());


        btnAdopta.setOnAction(e -> showHomePage());
        btnSubir.setOnAction(e -> showUploadPage());
        btnSolicitantes.setOnAction(e -> showApplicantsPage());
        btnNinos.setOnAction(e -> showChildrenPage());

        sideMenu.getChildren().addAll(menuTitle, btnAdopta, btnSubir, btnSolicitantes, btnNinos);
        sideMenu.setOnMouseEntered(e -> expandMenu());
        sideMenu.setOnMouseExited(e -> collapseMenu());
        sideMenu.getChildren().add(btnReportes);

    }

    private Button createMenuButton(String imagePath, String text) {
        Button btn = new Button();
        try {
            ImageView imageView = new ImageView(new Image(getClass().getResourceAsStream(imagePath)));
            imageView.setFitWidth(24);
            imageView.setFitHeight(24);
            imageView.setPreserveRatio(true);
            btn.setGraphic(imageView);
            javafx.scene.effect.ColorAdjust colorAdjust = new javafx.scene.effect.ColorAdjust();
            colorAdjust.setBrightness(1.0);
            imageView.setEffect(colorAdjust);
        } catch (Exception e) { }

        btn.setMaxWidth(Double.MAX_VALUE);
        btn.setAlignment(Pos.CENTER);
        btn.setFont(Font.font("Arial", FontWeight.BOLD, 15));
        btn.setStyle("-fx-background-color: transparent; -fx-text-fill: white; -fx-cursor: hand; -fx-padding: 12px;");

        btn.setOnMouseEntered(e -> btn.setStyle("-fx-background-color: rgba(255, 255, 255, 0.15); -fx-text-fill: white; -fx-cursor: hand; -fx-padding: 12px; -fx-background-radius: 8px;"));
        btn.setOnMouseExited(e -> btn.setStyle("-fx-background-color: transparent; -fx-text-fill: white; -fx-cursor: hand; -fx-padding: 12px;"));

        btn.setUserData(text);
        return btn;
    }

    private void expandMenu() {
        sideMenu.setStyle("-fx-background-color: #34495E; -fx-min-width: 220px;");
        Label menuTitle = (Label) sideMenu.getChildren().get(0);
        menuTitle.setVisible(true);
        for (int i = 1; i < sideMenu.getChildren().size(); i++) {
            Button btn = (Button) sideMenu.getChildren().get(i);
            btn.setText("  " + btn.getUserData());
            btn.setAlignment(Pos.CENTER_LEFT);
        }
        menuExpanded = true;
    }

    private void collapseMenu() {
        sideMenu.setStyle("-fx-background-color: #34495E; -fx-min-width: 60px;");
        Label menuTitle = (Label) sideMenu.getChildren().get(0);
        menuTitle.setVisible(false);
        for (int i = 1; i < sideMenu.getChildren().size(); i++) {
            Button btn = (Button) sideMenu.getChildren().get(i);
            btn.setText("");
            btn.setAlignment(Pos.CENTER);
        }
        menuExpanded = false;
    }

    private void createContentArea() {
        contentArea = new StackPane();
        contentArea.setStyle("-fx-background-color: #Fdfdfd;");
    }

    // --- PÁGINAS ---

    private void showHomePage() {
        BorderPane homeLayout = new BorderPane();
        VBox centerContent = new VBox(35);
        centerContent.setAlignment(Pos.CENTER);
        centerContent.setPadding(new Insets(20));

        VBox headerText = new VBox(8);
        headerText.setAlignment(Pos.CENTER);
        Label title = new Label("Bienvenido a nuestra agencia");
        title.setFont(Font.font("Segoe UI", FontWeight.NORMAL, 36));
        title.setTextFill(Color.web("#2c3e50"));
        Label subtitle = new Label("Seleccione su usuario para consultar o iniciar un proceso");
        subtitle.setFont(Font.font("Segoe UI", FontWeight.LIGHT, 18));
        subtitle.setTextFill(Color.web("#7f8c8d"));
        headerText.getChildren().addAll(title, subtitle);

        HBox selectionBox = new HBox(15);
        selectionBox.setAlignment(Pos.CENTER);
        selectionBox.setPadding(new Insets(30, 40, 30, 40));
        selectionBox.setMaxWidth(600);
        selectionBox.setStyle("-fx-background-color: white; -fx-background-radius: 20px; -fx-effect: dropshadow(three-pass-box, rgba(0,0,0,0.08), 30, 0, 0, 10);");

        ComboBox<String> solicitanteCombo = new ComboBox<>();
        solicitanteCombo.setPromptText("Seleccione su nombre (Cédula)");
        solicitanteCombo.setPrefWidth(300);
        solicitanteCombo.setPrefHeight(40);
        solicitanteCombo.setStyle("-fx-font-size: 14px; -fx-background-color: #f7f9fc; -fx-border-color: #e0e0e0; -fx-border-radius: 5;");
        solicitanteCombo.setItems(GestorBaseDeDatos.obtenerNombresDropdown());

        Button btnIniciar = new Button("Iniciar Proceso");
        btnIniciar.setPrefHeight(40);
        btnIniciar.setStyle("-fx-background-color: #e88188; -fx-text-fill: white; -fx-font-weight: bold; -fx-font-size: 14px; -fx-background-radius: 20; -fx-cursor: hand;");

        btnIniciar.setOnAction(e -> {
            String seleccion = solicitanteCombo.getValue();
            if (seleccion != null) {
                String cedula = seleccion.substring(seleccion.lastIndexOf("(") + 1, seleccion.lastIndexOf(")"));
                iniciarProcesoAdopcion(cedula);
            } else {
                mostrarAlerta("Atención", "Por favor seleccione un solicitante.");
            }
        });

        selectionBox.getChildren().addAll(solicitanteCombo, btnIniciar);
        centerContent.getChildren().addAll(headerText, selectionBox);
        homeLayout.setCenter(centerContent);

        try {
            ImageView illustration = new ImageView(new Image(getClass().getResourceAsStream("/ilustracion_niños.png")));
            illustration.setPreserveRatio(true);
            illustration.setFitHeight(200);
            VBox bottomContainer = new VBox(illustration);
            bottomContainer.setAlignment(Pos.BOTTOM_CENTER);
            homeLayout.setBottom(bottomContainer);
        } catch (Exception e) {}

        contentArea.getChildren().clear();
        contentArea.getChildren().add(homeLayout);
    }

    // --- FLUJO DE ADOPCIÓN (LÓGICA CORE) ---

    private void iniciarProcesoAdopcion(String cedula) {
        Solicitante solicitante = GestorBaseDeDatos.buscarSolicitantePorCedula(cedula);
        Solicitante pareja = GestorBaseDeDatos.buscarParejaDe(solicitante.getIdFamilia(), cedula);

        Dialog<ButtonType> dialog = new Dialog<>();
        dialog.setTitle("Fase 1: Identificación");
        dialog.setHeaderText("Verificación de Solicitantes");

        VBox content = new VBox(10);
        content.setPadding(new Insets(20));
        content.getChildren().add(new Label("Solicitante Principal:"));
        content.getChildren().add(crearTarjetaInfo(solicitante));

        if (pareja != null) {
            content.getChildren().add(new Label("Cónyuge / Pareja:"));
            content.getChildren().add(crearTarjetaInfo(pareja));
        }

        dialog.getDialogPane().setContent(content);
        ButtonType btnNext = new ButtonType("Verificar Requisitos", ButtonBar.ButtonData.OK_DONE);
        dialog.getDialogPane().getButtonTypes().addAll(btnNext, ButtonType.CANCEL);

        dialog.showAndWait().ifPresent(response -> {
            if (response == btnNext) {
                abrirVentanaValidacionAutomatica(solicitante);
            }
        });
    }
    private void abrirVentanaValidacionAutomatica(Solicitante sol) {// PASO 1: Creamos el proceso inmediatamente.
        // Esto pone a los padres en 'Asignado' en la BD.
        String idProcesoActivo = GestorBaseDeDatos.iniciarProceso(sol.getIdFamilia());

        if (idProcesoActivo == null) {
            mostrarAlerta("Error", "No se pudo iniciar el proceso.");
            return;
        }

        Alert alert = new Alert(Alert.AlertType.NONE);
        alert.setTitle("Fase 2: Análisis de Requisitos");
        alert.setHeaderText("Analizando Proceso: " + idProcesoActivo);


        VBox content = new VBox(15);
        content.setPadding(new Insets(20));
        content.setMinWidth(400);

        Label lblLibre    = new Label("⏳ Verificando disponibilidad de familia...");
        Label lblSalud    = new Label("⏳ Verificando salud...");
        Label lblIngresos = new Label("⏳ Verificando ingresos...");
        Label lblEdad     = new Label("⏳ Verificando edad...");
        Label lblDocs     = new Label("⏳ Verificando documentos...");
        Label lblPenal    = new Label("⏳ Verificando antecedentes...");

        content.getChildren().addAll(lblLibre, lblSalud, lblIngresos, lblEdad, lblDocs, lblPenal);
        alert.getDialogPane().setContent(content);

        ButtonType btnAsignarNino = new ButtonType("🔍 Buscar Niño", ButtonBar.ButtonData.OK_DONE);
        ButtonType btnCerrar = new ButtonType("Cerrar", ButtonBar.ButtonData.CANCEL_CLOSE);
        alert.getDialogPane().getButtonTypes().add(btnCerrar);

        new Thread(() -> {
            try {boolean libreOk = true;
                Platform.runLater(() -> updateLbl(lblLibre, libreOk));

                // PASO 2: Continuamos con las validaciones reales
                Thread.sleep(400); // Pequeña pausa para que el usuario vea el proceso
                boolean saludOk = GestorBaseDeDatos.checkSalud(sol.getCedula());
                Platform.runLater(() -> updateLbl(lblSalud, saludOk));

                Thread.sleep(400);
                boolean ingreOk = GestorBaseDeDatos.checkIngresos(sol.getCedula());
                Platform.runLater(() -> updateLbl(lblIngresos, ingreOk));

                Thread.sleep(400);
                boolean edadOk = GestorBaseDeDatos.checkEdad(sol.getCedula());
                Platform.runLater(() -> updateLbl(lblEdad, edadOk));

                Thread.sleep(400);
                boolean docsOk = GestorBaseDeDatos.checkDocumentosCompletos(sol.getCedula());
                Platform.runLater(() -> updateLbl(lblDocs, docsOk));

                Thread.sleep(400);
                boolean penalOk = GestorBaseDeDatos.checkAntecedentes(sol.getCedula());
                Platform.runLater(() -> updateLbl(lblPenal, penalOk));

                // PASO 3: TOMA DE DECISIÓN
                if (saludOk && ingreOk && edadOk && docsOk && penalOk) {
                    // TODO BIEN: El proceso ya está 'En Curso'
                    Platform.runLater(() -> {
                        alert.getDialogPane().getButtonTypes().add(btnAsignarNino);
                        alert.setHeaderText("✅ Requisitos Aprobados para " + idProcesoActivo);
                    });
                } else {
                    // FALLO: Cancelamos y reportamos
                    String motivo = determinarMotivo(libreOk, saludOk, ingreOk, edadOk, docsOk, penalOk);
                    String tipo = determinarTipo(libreOk, saludOk, ingreOk, edadOk, docsOk, penalOk);

                    // Cambiamos estado a cancelado en BD
                    GestorBaseDeDatos.cancelarProceso(idProcesoActivo, motivo);
                    // Registramos el motivo para el reporte
                    GestorBaseDeDatos.registrarIntentoFallido(sol.getIdFamilia(), motivo, tipo);

                    Platform.runLater(() -> {
                        alert.setHeaderText("❌ Proceso " + idProcesoActivo + " Cancelado.");
                        alert.setContentText("Motivo: " + motivo);
                    });
                }
            } catch (Exception e) {
                System.err.println("Error en el hilo de validación: " + e.getMessage());
                e.printStackTrace();
            }
        }).start();

        alert.showAndWait().ifPresent(r -> {
            if (r == btnAsignarNino) {
                mostrarPropuestaNino(idProcesoActivo);
            }
        });}

    // --- MÉTODOS AUXILIARES PARA EL REPORTE ---

    private String determinarMotivo(boolean libre, boolean salud, boolean ingre, boolean edad, boolean docs, boolean penal) {
        if (!libre) return "La familia ya tiene un proceso activo";
        if (!salud) return "Problemas de salud imposibilitantes detectados";
        if (!ingre) return "Ingresos insuficientes (Menor a $800)";
        if (!edad)  return "Solicitante menor de la edad reglamentaria (25 años)";
        if (!docs)  return "Documentación incompleta o no presentada";
        if (!penal) return "Registra antecedentes penales";
        return "No cumple requisitos generales";
    }

    private String determinarTipo(boolean libre, boolean salud, boolean ingre, boolean edad, boolean docs, boolean penal) {
        if (!libre || !penal) return "LEGAL";
        if (!salud) return "SALUD";
        if (!ingre) return "ECONOMICO";
        if (!edad)  return "EDAD";
        if (!docs)  return "DOCUMENTOS";
        return "OTRO";
    }


    private void mostrarAlertaProcesoCreado(String idProceso) {
        Alert confirm = new Alert(Alert.AlertType.CONFIRMATION);
        confirm.setTitle("Expediente Abierto");
        confirm.setHeaderText("Proceso " + idProceso + " iniciado exitosamente");
        confirm.setContentText("El proceso se encuentra en estado 'En Curso'.\nNo se ha asignado ningún niño todavía.\n\n¿Desea buscar una coincidencia en el sistema ahora?");

        ButtonType btnBuscar = new ButtonType("🔍 Buscar Niño Aleatorio", ButtonBar.ButtonData.OK_DONE);
        ButtonType btnMasTarde = new ButtonType("Más tarde", ButtonBar.ButtonData.CANCEL_CLOSE);

        confirm.getButtonTypes().setAll(btnBuscar, btnMasTarde);

        confirm.showAndWait().ifPresent(resp -> {
            if (resp == btnBuscar) {
                // AQUI OCURRE EL PASO 2: UPDATE PROCESO (Asignar niño)
                mostrarPropuestaNino(idProceso);
            } else {
                // Si pone "Más tarde", el proceso se queda creado en BD pero sin niño.
                showHomePage();
            }
        });
    }

    private void updateLbl(Label l, boolean ok) {
        if (ok) {
            String texto = l.getText().replace("⏳ ", "");
            l.setText("✅ " + texto + ": OK");
            l.setStyle("-fx-text-fill: green; -fx-font-weight: bold;");
        } else {
            l.setText("❌ " + l.getText().replace("⏳ ", "") + ": FALLIDO");
            l.setStyle("-fx-text-fill: red; -fx-font-weight: bold;");
        }
    }

    private void crearProcesoYBuscar(Solicitante sol) {
        String idProceso = GestorBaseDeDatos.crearProceso(sol.getIdFamilia());
        if (idProceso != null) {
            mostrarPropuestaNino(idProceso);
        } else {
            mostrarAlerta("Error", "No se pudo iniciar el proceso. Verifique si la familia ya tiene uno activo.");
        }
    }
    private void mostrarPropuestaNino(String idProceso) {
        // Llamamos a la BD para asignar (UPDATE)
        Nino nino = GestorBaseDeDatos.asignarNinoAleatorio(idProceso);

        if (nino == null) {
            mostrarAlerta("Sin resultados", "No hay niños disponibles para asignación.");
            // Ojo: Si no hay niños, el proceso se queda 'En Curso' pero sin niño.
            // O podrías cancelarlo si prefieres.
            return;
        }

        Alert matchAlert = new Alert(Alert.AlertType.CONFIRMATION);
        matchAlert.setTitle("¡Tenemos una coincidencia!");
        matchAlert.setHeaderText("Propuesta de Adopción");

        VBox card = new VBox(10);
        card.setStyle("-fx-background-color: #f0f8ff; -fx-padding: 15; -fx-background-radius: 10; -fx-border-color: #bcdff1; -fx-border-radius: 10;");

        Label lblNombre = new Label("Nombre: " + nino.getNombre() + " " + nino.getApellido());
        lblNombre.setFont(Font.font("System", FontWeight.BOLD, 16));

        card.getChildren().addAll(
                lblNombre,
                new Label("Sexo: " + nino.getSexo()),
                new Label("Nivel Educativo: " + nino.getNivelEducativo())
        );

        matchAlert.getDialogPane().setContent(card);
        ButtonType btnAdoptar = new ButtonType("💙 Aceptar y Finalizar", ButtonBar.ButtonData.OK_DONE);
        ButtonType btnRechazar = new ButtonType("Rechazar Propuesta", ButtonBar.ButtonData.CANCEL_CLOSE);

        matchAlert.getButtonTypes().setAll(btnAdoptar, btnRechazar);

        matchAlert.showAndWait().ifPresent(type -> {
            if (type == btnAdoptar) {
                // PASO FINAL: Completado
                GestorBaseDeDatos.completarProceso(idProceso);
                mostrarAlerta("¡Felicidades!", "El proceso ha finalizado. El niño ha sido adoptado.");
                showHomePage();
            } else {
                // RECHAZO: Cancelado y liberamos al niño
                GestorBaseDeDatos.cancelarProceso(idProceso, "El solicitante rechazó la propuesta");
                mostrarAlerta("Proceso Cancelado", "Se ha registrado la cancelación.");
                showHomePage();
            }
        });
    }


    // --- PÁGINAS ADICIONALES (Carga de Archivos y Tablas) ---
    private void showReporteEstados() {


        VBox content = new VBox(20);
        content.setPadding(new Insets(40));
        content.setAlignment(Pos.TOP_LEFT);


        Label title = new Label("Reporte: Estados de Procesos");
        title.setFont(Font.font("Segoe UI", FontWeight.NORMAL, 28));
        title.setTextFill(Color.web("#2c3e50"));


        Label subtitle = new Label("Cantidad de procesos por estado");
        subtitle.setFont(Font.font("Segoe UI", FontWeight.LIGHT, 16));
        subtitle.setTextFill(Color.web("#7f8c8d"));


        TableView<ReporteEstadoProceso> table = new TableView<>();


        Button btnPDF = new Button("📄 Exportar a PDF");
        btnPDF.setStyle(
                "-fx-background-color: #2c3e50;" +
                        "-fx-text-fill: white;" +
                        "-fx-font-weight: bold;" +
                        "-fx-padding: 8 16;" +
                        "-fx-background-radius: 8;"
        );


        btnPDF.setOnAction(e -> {


            FileChooser fileChooser = new FileChooser();
            fileChooser.setTitle("Guardar Reporte de Estados");
            fileChooser.getExtensionFilters().add(
                    new FileChooser.ExtensionFilter("PDF (*.pdf)", "*.pdf")
            );
            fileChooser.setInitialFileName("Reporte_Estados_Procesos.pdf");


            File archivo = fileChooser.showSaveDialog(null);


            if (archivo != null) {
                try {
                    ReporteEstadosPDF.generar(
                            GestorReportes.obtenerReporteEstados(),
                            archivo.getAbsolutePath()
                    );


                    mostrarAlerta(
                            "PDF generado",
                            "El reporte se guardó correctamente."
                    );


                } catch (Exception ex) {
                    ex.printStackTrace();
                    mostrarAlerta(
                            "Error",
                            "No se pudo generar el PDF."
                    );
                }
            }
        });


















        TableColumn<ReporteEstadoProceso, String> colEstado =
                new TableColumn<>("Estado del Proceso");
        colEstado.setCellValueFactory(
                new PropertyValueFactory<>("estadoProceso")
        );


        TableColumn<ReporteEstadoProceso, Integer> colCantidad =
                new TableColumn<>("Cantidad");
        colCantidad.setCellValueFactory(
                new PropertyValueFactory<>("cantidad")
        );


        table.setItems(GestorReportes.obtenerReporteEstados());


        table.getColumns().addAll(colEstado, colCantidad);
        table.setColumnResizePolicy(TableView.CONSTRAINED_RESIZE_POLICY);




        content.getChildren().addAll(title, subtitle, table, btnPDF);


        contentArea.getChildren().clear();
        contentArea.getChildren().add(content);
    }




    private void showReporteMotivos() {


        VBox content = new VBox(20);
        content.setPadding(new Insets(40));
        content.setAlignment(Pos.TOP_LEFT);


        Label title = new Label("Reporte: Motivos de Cancelación");
        title.setFont(Font.font("Segoe UI", FontWeight.NORMAL, 28));
        title.setTextFill(Color.web("#2c3e50"));


        Label subtitle = new Label("Causas más frecuentes de cancelación de procesos");
        subtitle.setFont(Font.font("Segoe UI", FontWeight.LIGHT, 16));
        subtitle.setTextFill(Color.web("#7f8c8d"));


        TableView<ReporteMotivoCancelacion> table = new TableView<>();


        Button btnPDF = new Button("📄 Exportar a PDF");
        btnPDF.setStyle(
                "-fx-background-color: #2c3e50;" +
                        "-fx-text-fill: white;" +
                        "-fx-font-weight: bold;" +
                        "-fx-padding: 8 16;" +
                        "-fx-background-radius: 8;"
        );




        btnPDF.setOnAction(e -> {


            FileChooser fileChooser = new FileChooser();
            fileChooser.setTitle("Guardar Reporte de Motivos de Cancelación");
            fileChooser.getExtensionFilters().add(
                    new FileChooser.ExtensionFilter("PDF (*.pdf)", "*.pdf")
            );
            fileChooser.setInitialFileName("Reporte_Motivos_Cancelacion.pdf");


            File archivo = fileChooser.showSaveDialog(null);


            if (archivo != null) {
                try {
                    ReporteMotivosPDF.generar(
                            GestorReportes.obtenerReporteMotivos(),
                            archivo.getAbsolutePath()
                    );


                    mostrarAlerta(
                            "PDF generado",
                            "El reporte se guardó correctamente."
                    );


                } catch (Exception ex) {
                    ex.printStackTrace();
                    mostrarAlerta(
                            "Error",
                            "No se pudo generar el PDF."
                    );
                }
            }
        });












        TableColumn<ReporteMotivoCancelacion, String> colMotivo =
                new TableColumn<>("Tipo de Motivo");
        colMotivo.setCellValueFactory(
                new PropertyValueFactory<>("tipoMotivo")
        );


        TableColumn<ReporteMotivoCancelacion, Integer> colCantidad =
                new TableColumn<>("Cantidad");
        colCantidad.setCellValueFactory(
                new PropertyValueFactory<>("cantidad")
        );


        TableColumn<ReporteMotivoCancelacion, String> colEjemplos =
                new TableColumn<>("Ejemplos / Comentarios");
        colEjemplos.setCellValueFactory(
                new PropertyValueFactory<>("ejemplos")
        );


        table.getColumns().addAll(colMotivo, colCantidad, colEjemplos);
        table.setColumnResizePolicy(TableView.CONSTRAINED_RESIZE_POLICY);


        // 🔥 AQUÍ ESTÁ LA CLAVE
        table.setItems(GestorReportes.obtenerReporteMotivos());




        content.getChildren().addAll(title, subtitle, table, btnPDF);


        contentArea.getChildren().clear();
        contentArea.getChildren().add(content);
    }














    private void showReporteAdopciones() {


        VBox content = new VBox(20);
        content.setPadding(new Insets(40));
        content.setAlignment(Pos.TOP_LEFT);


        Label title = new Label("Reporte: Adopciones Completadas");
        title.setFont(Font.font("Segoe UI", FontWeight.NORMAL, 28));
        title.setTextFill(Color.web("#2c3e50"));


        Label subtitle = new Label("Listado de procesos completados con familia y niño asignado");
        subtitle.setFont(Font.font("Segoe UI", FontWeight.LIGHT, 16));
        subtitle.setTextFill(Color.web("#7f8c8d"));


        TableView<ReporteAdopcion> table = new TableView<>();


        Button btnPDF = new Button("📄 Exportar a PDF");
        btnPDF.setStyle(
                "-fx-background-color: #2c3e50;" +
                        "-fx-text-fill: white;" +
                        "-fx-font-weight: bold;" +
                        "-fx-padding: 8 16;" +
                        "-fx-background-radius: 8;"
        );


        btnPDF.setOnAction(e -> {


            FileChooser fileChooser = new FileChooser();
            fileChooser.setTitle("Guardar Reporte de Adopciones");
            fileChooser.getExtensionFilters().add(
                    new FileChooser.ExtensionFilter("PDF (*.pdf)", "*.pdf")
            );
            fileChooser.setInitialFileName("Reporte_Adopciones_Completadas.pdf");


            File archivo = fileChooser.showSaveDialog(null);


            if (archivo != null) {
                try {
                    ReporteAdopcionesPDF.generar(
                            GestorReportes.obtenerReporteAdopciones(),
                            archivo.getAbsolutePath()
                    );


                    mostrarAlerta(
                            "PDF generado",
                            "El reporte se guardó correctamente."
                    );


                } catch (Exception ex) {
                    ex.printStackTrace();
                    mostrarAlerta(
                            "Error",
                            "No se pudo generar el PDF."
                    );
                }
            }
        });




        TableColumn<ReporteAdopcion, String> colProceso = new TableColumn<>("Proceso");
        colProceso.setCellValueFactory(new PropertyValueFactory<>("idProceso"));


        TableColumn<ReporteAdopcion, String> colPadres = new TableColumn<>("Padres");
        colPadres.setCellValueFactory(new PropertyValueFactory<>("padres"));


        TableColumn<ReporteAdopcion, String> colNino = new TableColumn<>("Niño");
        colNino.setCellValueFactory(new PropertyValueFactory<>("nombreNino"));


        TableColumn<ReporteAdopcion, String> colSexo = new TableColumn<>("Sexo");
        colSexo.setCellValueFactory(new PropertyValueFactory<>("sexoNino"));


        TableColumn<ReporteAdopcion, String> colEducacion = new TableColumn<>("Educación");
        colEducacion.setCellValueFactory(new PropertyValueFactory<>("nivelEducacion"));


        TableColumn<ReporteAdopcion, String> colFecha = new TableColumn<>("Fecha");
        colFecha.setCellValueFactory(cell ->
                new javafx.beans.property.SimpleStringProperty(
                        cell.getValue().getFechaSolicitud() != null ? cell.getValue().getFechaSolicitud().toString() : ""
                )
        );


        table.getColumns().addAll(colProceso, colPadres, colNino, colSexo, colEducacion, colFecha);
        table.setColumnResizePolicy(TableView.CONSTRAINED_RESIZE_POLICY);


        table.setItems(GestorReportes.obtenerReporteAdopciones());






        content.getChildren().addAll(title, subtitle, table, btnPDF);


        contentArea.getChildren().clear();
        contentArea.getChildren().add(content);
    }










    //REPORTES REPORTES PAGEE
    private void showReportesPage() {
        VBox content = new VBox(20);
        content.setPadding(new Insets(40));
        content.setAlignment(Pos.TOP_LEFT);


        Label title = new Label("Reportes del Sistema");
        title.setFont(Font.font("Segoe UI", FontWeight.NORMAL, 28));
        title.setTextFill(Color.web("#2c3e50"));




        Button btnReporteEstados = new Button("📊 Estados de Procesos");
        Button btnReporteMotivos = new Button("❌ Motivos de Cancelación");
        Button btnReporteAdopciones = new Button("📄 Adopciones Completadas");


        btnReporteAdopciones.setOnAction(e -> showReporteAdopciones());
        btnReporteEstados.setOnAction(e -> showReporteEstados());
        btnReporteMotivos.setOnAction(e -> showReporteMotivos());


        content.getChildren().addAll(
                title,
                btnReporteAdopciones,
                btnReporteEstados,
                btnReporteMotivos
        );


        contentArea.getChildren().clear();
        contentArea.getChildren().add(content);
    }













    private void showUploadPage() {
        VBox content = new VBox(25);
        content.setAlignment(Pos.CENTER);
        content.setPadding(new Insets(40));

        Label title = new Label("Cargar datos");
        title.setFont(Font.font("Segoe UI", FontWeight.NORMAL, 32));
        title.setTextFill(Color.web("#2c3e50"));
        Label subtitle = new Label("Sube el archivo Excel (.xlsx) para actualizar la base de datos");
        subtitle.setFont(Font.font("Segoe UI", FontWeight.LIGHT, 16));
        subtitle.setTextFill(Color.web("#7f8c8d"));

        VBox card = new VBox(20);
        card.setAlignment(Pos.CENTER);
        card.setPadding(new Insets(40));
        card.setMaxWidth(500);
        card.setStyle("-fx-background-color: white; -fx-background-radius: 20px;");
        card.setEffect(new DropShadow(30, Color.rgb(0, 0, 0, 0.08)));

        TextArea txtLog = new TextArea();
        txtLog.setEditable(false);
        txtLog.setPrefHeight(150);
        txtLog.setWrapText(true);
        txtLog.setStyle("-fx-background-color: transparent; -fx-control-inner-background: #f9f9f9;");
        txtLog.setVisible(false);
        txtLog.setManaged(false);

        Button uploadBtn = new Button("Seleccionar archivo Excel");
        uploadBtn.setStyle("-fx-background-color: #34495E; -fx-text-fill: white; -fx-font-family: 'Segoe UI'; -fx-font-size: 16px; -fx-font-weight: bold; -fx-padding: 12px 30px; -fx-background-radius: 25px; -fx-cursor: hand;");

        // --- LÓGICA DE CARGA MODIFICADA PARA USAR VALIDACIÓN ---
        uploadBtn.setOnAction(e -> {
            FileChooser fileChooser = new FileChooser();
            fileChooser.setTitle("Seleccionar Excel de Base de Datos");
            fileChooser.getExtensionFilters().add(new FileChooser.ExtensionFilter("Archivos Excel", "*.xlsx"));
            File archivo = fileChooser.showOpenDialog(null);

            if (archivo != null) {
                txtLog.setVisible(true);
                txtLog.setManaged(true);
                txtLog.setText("⏳ Analizando archivo... Por favor espere.");

                // Usamos Hilo para no congelar la UI mientras lee
                new Thread(() -> {
                    // PASO 1: VALIDACIÓN
                    String reporteValidacion = LectorDeArchivos.validarIntegridad(archivo);

                    Platform.runLater(() -> {
                        txtLog.setText(reporteValidacion); // Mostramos el reporte

                        // PASO 2: CONFIRMACIÓN (Solo si no hay errores fatales)
                        if (!reporteValidacion.contains("❌ EL ARCHIVO TIENE ERRORES")) {
                            Alert confirm = new Alert(Alert.AlertType.CONFIRMATION);
                            confirm.setTitle("Confirmar Importación");
                            confirm.setHeaderText("Validación Exitosa");
                            confirm.setContentText("El archivo parece correcto. ¿Desea proceder a guardar los datos en la base de datos?");

                            confirm.showAndWait().ifPresent(response -> {
                                if (response == ButtonType.OK) {
                                    txtLog.appendText("\n\n⏳ Guardando en PostgreSQL...");
                                    new Thread(() -> {
                                        // PASO 3: EJECUCIÓN REAL
                                        String resultadoFinal = LectorDeArchivos.importarExcelBD(archivo);
                                        Platform.runLater(() -> txtLog.setText(resultadoFinal));
                                    }).start();
                                }
                            });
                        }
                    });
                }).start();
            }
        });

        card.getChildren().addAll(uploadBtn, txtLog);
        content.getChildren().addAll(title, subtitle, card);
        contentArea.getChildren().clear();
        contentArea.getChildren().add(content);
    }

    private void showApplicantsPage() {
        VBox content = new VBox(20);
        content.setAlignment(Pos.TOP_LEFT);
        content.setPadding(new Insets(40));

        Label title = new Label("Listado de Solicitantes");
        title.setFont(Font.font("Segoe UI", FontWeight.NORMAL, 28));
        title.setTextFill(Color.web("#2c3e50"));

        TableView<Solicitante> table = new TableView<>();

        TableColumn<Solicitante, String> colCedula = new TableColumn<>("Cédula");
        colCedula.setCellValueFactory(new javafx.scene.control.cell.PropertyValueFactory<>("cedula"));

        TableColumn<Solicitante, String> colNombre = new TableColumn<>("Nombres");
        colNombre.setCellValueFactory(new javafx.scene.control.cell.PropertyValueFactory<>("nombres"));

        TableColumn<Solicitante, String> colApellido = new TableColumn<>("Apellidos");
        colApellido.setCellValueFactory(new javafx.scene.control.cell.PropertyValueFactory<>("apellidos"));

        TableColumn<Solicitante, String> colEmail = new TableColumn<>("Email");
        colEmail.setCellValueFactory(new javafx.scene.control.cell.PropertyValueFactory<>("email"));

        TableColumn<Solicitante, String> colIngreso = new TableColumn<>("Ingreso Mensual");
        colIngreso.setCellValueFactory(cell ->
                new javafx.beans.property.SimpleStringProperty("$ " + cell.getValue().getIngreso())
        );

        table.getColumns().addAll(colCedula, colNombre, colApellido, colEmail, colIngreso);
        table.setItems(GestorBaseDeDatos.obtenerTodosSolicitantes());
        table.setColumnResizePolicy(TableView.CONSTRAINED_RESIZE_POLICY);
        VBox.setVgrow(table, Priority.ALWAYS);

        content.getChildren().addAll(title, table);
        contentArea.getChildren().clear();
        contentArea.getChildren().add(content);
    }

    private void showChildrenPage() {
        VBox content = new VBox(20);
        content.setAlignment(Pos.TOP_LEFT);
        content.setPadding(new Insets(40));

        Label title = new Label("Niños en adopción (Disponibles)");
        title.setFont(Font.font("Segoe UI", FontWeight.NORMAL, 28));
        title.setTextFill(Color.web("#2c3e50"));

        TableView<Nino> table = new TableView<>();

        TableColumn<Nino, String> colNombre = new TableColumn<>("Nombre");
        colNombre.setCellValueFactory(new javafx.scene.control.cell.PropertyValueFactory<>("nombre"));

        TableColumn<Nino, String> colApellido = new TableColumn<>("Apellido");
        colApellido.setCellValueFactory(new javafx.scene.control.cell.PropertyValueFactory<>("apellido"));

        TableColumn<Nino, String> colSexo = new TableColumn<>("Sexo");
        colSexo.setCellValueFactory(new javafx.scene.control.cell.PropertyValueFactory<>("sexo"));

        TableColumn<Nino, String> colEducacion = new TableColumn<>("Educación");
        colEducacion.setCellValueFactory(new javafx.scene.control.cell.PropertyValueFactory<>("nivelEducativo"));

        table.getColumns().addAll(colNombre, colApellido, colSexo, colEducacion);
        table.setItems(GestorBaseDeDatos.obtenerTodosNinos());
        table.setColumnResizePolicy(TableView.CONSTRAINED_RESIZE_POLICY);
        VBox.setVgrow(table, Priority.ALWAYS);

        content.getChildren().addAll(title, table);
        contentArea.getChildren().clear();
        contentArea.getChildren().add(content);
    }

    private VBox crearTarjetaInfo(Solicitante s) {
        VBox box = new VBox(5);
        box.setStyle("-fx-border-color: #ccc; -fx-border-radius: 5; -fx-padding: 10; -fx-background-color: #f9f9f9;");
        box.getChildren().addAll(
                new Label("Nombre: " + s.getNombres() + " " + s.getApellidos()),
                new Label("Cédula: " + s.getCedula()),
                new Label("Email: " + s.getEmail()),
                new Label("Ingresos: $ " + s.getIngreso())
        );
        return box;
    }

    private void mostrarAlerta(String titulo, String mensaje) {
        Alert a = new Alert(Alert.AlertType.INFORMATION);
        a.setTitle(titulo);
        a.setHeaderText(null);
        a.setContentText(mensaje);
        a.showAndWait();
    }

    public static void main(String[] args) {
        launch(args);
    }



}