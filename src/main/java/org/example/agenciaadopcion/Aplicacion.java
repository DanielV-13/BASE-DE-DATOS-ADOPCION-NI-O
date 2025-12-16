package org.example.agenciaadopcion;

import javafx.application.Application;
import javafx.application.Platform;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.*;
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

import java.io.File;

public class Aplicacion extends Application {

    private BorderPane mainLayout;
    private VBox sideMenu;
    private StackPane contentArea;
    private boolean menuExpanded = false;

    @Override
    public void start(Stage stage) {
        // Layout principal
        mainLayout = new BorderPane();

        // Crear menú lateral
        createSideMenu();

        // Crear área de contenido
        createContentArea();

        // Agregar componentes al layout principal
        mainLayout.setLeft(sideMenu);
        mainLayout.setCenter(contentArea);

        // Crear escena
        Scene scene = new Scene(mainLayout, 1000, 700);

        // Configurar ventana
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



        // Mostrar página de inicio
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

        // Botones del menú
        Button btnAdopta = createMenuButton("/casa.png", "Adopta");
        Button btnSubir = createMenuButton("/archivo.png", "Subir Excel");
        Button btnSolicitantes = createMenuButton("/familia.png", "Solicitantes");
        Button btnNinos = createMenuButton("/chico.png", "Niños");

        btnAdopta.setOnAction(e -> showHomePage());
        btnSubir.setOnAction(e -> showUploadPage());
        btnSolicitantes.setOnAction(e -> showApplicantsPage());
        btnNinos.setOnAction(e -> showChildrenPage());

        sideMenu.getChildren().addAll(menuTitle, btnAdopta, btnSubir, btnSolicitantes, btnNinos);

        sideMenu.setOnMouseEntered(e -> expandMenu());
        sideMenu.setOnMouseExited(e -> collapseMenu());
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

        // Hover
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

        // Zona Central
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

        // Tarjeta de selección
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

        // LÓGICA DE INICIO
        btnIniciar.setOnAction(e -> {
            String seleccion = solicitanteCombo.getValue();
            if (seleccion != null) {
                // Extraer cédula del string "Nombre Apellido (CEDULA)"
                String cedula = seleccion.substring(seleccion.lastIndexOf("(") + 1, seleccion.lastIndexOf(")"));
                iniciarProcesoAdopcion(cedula); // <--- INICIO DEL FLUJO PRINCIPAL
            } else {
                mostrarAlerta("Atención", "Por favor seleccione un solicitante.");
            }
        });

        selectionBox.getChildren().addAll(solicitanteCombo, btnIniciar);
        centerContent.getChildren().addAll(headerText, selectionBox);
        homeLayout.setCenter(centerContent);

        // Imagen inferior
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

    // --- FLUJO DE ADOPCIÓN ---

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

        ButtonType btnNext = new ButtonType("Verificar Requisitos >", ButtonBar.ButtonData.OK_DONE);
        dialog.getDialogPane().getButtonTypes().addAll(btnNext, ButtonType.CANCEL);

        dialog.showAndWait().ifPresent(response -> {
            if (response == btnNext) {
                // CAMBIO: Ahora vamos a verificar requisitos ANTES de pedir documentos
                abrirVentanaRequisitosPrevios(solicitante);
            }
        });
    }

    // PASO 2: Verificación Visual (Edad, Ingresos, Salud)
    private void abrirVentanaRequisitosPrevios(Solicitante sol) {
        Alert alert = new Alert(Alert.AlertType.NONE); // Sin botones por defecto
        alert.setTitle("Fase 2: Requisitos Legales");
        alert.setHeaderText("Verificando cumplimiento de normativas...");

        VBox content = new VBox(15);
        content.setPadding(new Insets(20));
        content.setMinWidth(350);

        // Etiquetas de estado
        Label lblEdad = new Label("⏳ Verificando edad mínima (25 años)...");
        Label lblIngresos = new Label("⏳ Verificando solvencia económica...");
        Label lblSalud = new Label("⏳ Consultando historial médico...");
        Label lblPenal = new Label("⏳ Consultando antecedentes...");

        content.getChildren().addAll(lblEdad, lblIngresos, lblSalud, lblPenal);
        alert.getDialogPane().setContent(content);

        // Botones (Inicialmente deshabilitados)
        ButtonType btnContinuar = new ButtonType("Continuar a Documentación >", ButtonBar.ButtonData.OK_DONE);
        ButtonType btnCancelar = new ButtonType("Cancelar", ButtonBar.ButtonData.CANCEL_CLOSE);
        alert.getDialogPane().getButtonTypes().addAll(btnCancelar);

        // Tarea en segundo plano para dar efecto de "carga"
        new Thread(() -> {
            try {
                // Simular pequeña pausa para efecto visual
                Thread.sleep(800);
                boolean edadOk = GestorBaseDeDatos.checkEdad(sol.getCedula());
                Platform.runLater(() -> actualizarLabel(lblEdad, edadOk, "Edad: Apto", "Edad: Menor a 25 años"));

                Thread.sleep(500);
                boolean ingresosOk = GestorBaseDeDatos.checkIngresos(sol.getCedula());
                Platform.runLater(() -> actualizarLabel(lblIngresos, ingresosOk, "Ingresos: Suficientes", "Ingresos: Insuficientes (<$800)"));

                Thread.sleep(500);
                boolean saludOk = GestorBaseDeDatos.checkSalud(sol.getCedula());
                Platform.runLater(() -> actualizarLabel(lblSalud, saludOk, "Salud: Apto física y mentalmente", "Salud: Posee enfermedad inhabilitante"));

                Thread.sleep(500);
                boolean penalOk = GestorBaseDeDatos.checkAntecedentes(sol.getCedula()); // Asumiendo que TRUE es limpio
                Platform.runLater(() -> actualizarLabel(lblPenal, penalOk, "Antecedentes: Limpios", "Antecedentes: Registra antecedentes"));

                // Si todo está OK, habilitamos el botón continuar
                if (edadOk && ingresosOk && saludOk && penalOk) {
                    Platform.runLater(() -> {
                        alert.getDialogPane().getButtonTypes().add(btnContinuar);
                        alert.setHeaderText("✅ El solicitante cumple los requisitos previos.");
                        // Redimensionar para que quepa el botón
                        alert.getDialogPane().getScene().getWindow().sizeToScene();
                    });
                } else {
                    Platform.runLater(() -> {
                        alert.setHeaderText("❌ No se puede continuar el proceso.");
                        alert.setContentText("El solicitante no cumple con uno o más requisitos legales.");
                    });
                }

            } catch (InterruptedException e) { e.printStackTrace(); }
        }).start();

        alert.showAndWait().ifPresent(response -> {
            if (response == btnContinuar) {
                // Si pasó todo, vamos a los documentos
                mostrarChecklistDocumentos(sol);
            }
        });
    }

    // Auxiliar para pintar labels
    private void actualizarLabel(Label lbl, boolean ok, String textoOk, String textoError) {
        if (ok) {
            lbl.setText("✅ " + textoOk);
            lbl.setStyle("-fx-text-fill: green; -fx-font-weight: bold;");
        } else {
            lbl.setText("❌ " + textoError);
            lbl.setStyle("-fx-text-fill: red; -fx-font-weight: bold;");
        }
    }

    // PASO 3: Documentos (Checklist)
    private void mostrarChecklistDocumentos(Solicitante sol) {
        Dialog<ButtonType> dialog = new Dialog<>();
        dialog.setTitle("Fase 3: Documentación Física");
        dialog.setHeaderText("Marque los documentos físicos entregados por el solicitante:");

        VBox content = new VBox(15);
        content.setPadding(new Insets(20));

        // Nombres de documentos basados en tu DB
        CheckBox chk1 = new CheckBox("Cédula de Identidad (DOC-0001)");
        CheckBox chk2 = new CheckBox("Certificado de Nacimiento (DOC-0002)");
        CheckBox chk3 = new CheckBox("Certificado Laboral (DOC-0003)");
        CheckBox chk4 = new CheckBox("Récord Policial (DOC-0004)");
        CheckBox chk5 = new CheckBox("Certificado Médico (DOC-0006)"); // Ojo con el ID DOC-0005 que daba error antes

        content.getChildren().addAll(new Label("Documentos Requeridos:"), chk1, chk2, chk3, chk4, chk5);
        dialog.getDialogPane().setContent(content);

        ButtonType btnFinalizar = new ButtonType("Validar y Crear Proceso >", ButtonBar.ButtonData.OK_DONE);
        dialog.getDialogPane().getButtonTypes().addAll(btnFinalizar, ButtonType.CANCEL);

        dialog.showAndWait().ifPresent(response -> {
            if (response == btnFinalizar) {
                // 1. Guardar en BD
                GestorBaseDeDatos.registrarDocumento(sol.getCedula(), "DOC-0001", chk1.isSelected());
                GestorBaseDeDatos.registrarDocumento(sol.getCedula(), "DOC-0002", chk2.isSelected());
                GestorBaseDeDatos.registrarDocumento(sol.getCedula(), "DOC-0003", chk3.isSelected());
                GestorBaseDeDatos.registrarDocumento(sol.getCedula(), "DOC-0004", chk4.isSelected());
                GestorBaseDeDatos.registrarDocumento(sol.getCedula(), "DOC-0006", chk5.isSelected());

                // 2. Verificar si están completos en BD
                boolean docsCompletos = GestorBaseDeDatos.checkDocumentosCompletos(sol.getCedula());

                if (docsCompletos) {
                    // 3. CREAR PROCESO (Ahora sí)
                    String idProceso = GestorBaseDeDatos.crearProceso(sol.getIdFamilia());
                    if (idProceso != null) {
                        mostrarPropuestaNino(idProceso);
                    } else {
                        mostrarAlerta("Error", "Error al crear el proceso. Verifique si hay niños disponibles.");
                    }
                } else {
                    mostrarAlerta("Documentos Faltantes", "Faltan documentos obligatorios. No se puede crear el proceso.");
                }
            }
        });
    }

    // PASO 4: Asignación de Niño y Finalización
    private void mostrarPropuestaNino(String idProceso) {
        // SQL asigna un niño random (o recupera el asignado por el trigger)
        Nino nino = GestorBaseDeDatos.asignarNinoAleatorio(idProceso);

        if (nino == null) {
            mostrarAlerta("Sin resultados", "No hay niños disponibles para adopción en este momento.");
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
        ButtonType btnAdoptar = new ButtonType("Formalizar Adopción", ButtonBar.ButtonData.OK_DONE);
        matchAlert.getButtonTypes().setAll(btnAdoptar, ButtonType.CANCEL);

        matchAlert.showAndWait().ifPresent(type -> {
            if (type == btnAdoptar) {
                // COMPLETAR PROCESO -> UPDATE SQL 'Completado'
                GestorBaseDeDatos.completarProceso(idProceso);
                mostrarAlerta("¡Felicidades!", "El proceso ha finalizado exitosamente. ¡Gracias por dar un hogar!");
                showHomePage();
            } else {
                // RECHAZO -> UPDATE SQL 'Cancelado'
                GestorBaseDeDatos.cancelarProceso(idProceso, "El solicitante rechazó la asignación del niño propuesto");
                mostrarAlerta("Proceso Cancelado", "Se ha cancelado el proceso de adopción.");
                showHomePage();
            }
        });
    }

    // --- PÁGINAS ADICIONALES (Carga de Archivos y Tablas) ---

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

        // Área de Log
        TextArea txtLog = new TextArea();
        txtLog.setEditable(false);
        txtLog.setPrefHeight(150);
        txtLog.setWrapText(true);
        txtLog.setStyle("-fx-background-color: transparent; -fx-control-inner-background: #f9f9f9;");
        txtLog.setVisible(false);
        txtLog.setManaged(false);

        Button uploadBtn = new Button("Seleccionar archivo Excel");
        uploadBtn.setStyle("-fx-background-color: #34495E; -fx-text-fill: white; -fx-font-family: 'Segoe UI'; -fx-font-size: 16px; -fx-font-weight: bold; -fx-padding: 12px 30px; -fx-background-radius: 25px; -fx-cursor: hand;");

        uploadBtn.setOnAction(e -> {
            FileChooser fileChooser = new FileChooser();
            fileChooser.setTitle("Seleccionar Excel de Base de Datos");
            fileChooser.getExtensionFilters().add(new FileChooser.ExtensionFilter("Archivos Excel", "*.xlsx"));
            File archivo = fileChooser.showOpenDialog(null);

            if (archivo != null) {
                txtLog.setVisible(true);
                txtLog.setManaged(true);
                txtLog.setText("⏳ Procesando archivo... Por favor espere.");

                new Thread(() -> {
                    String resultado = LectorDeArchivos.importarExcelCompleto(archivo);
                    Platform.runLater(() -> txtLog.setText(resultado));
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

        // Ingresos formateados
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

    // --- UTILIDADES ---

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