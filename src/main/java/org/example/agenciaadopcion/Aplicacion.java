package org.example.agenciaadopcion;

import javafx.application.Application;
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
import javafx.stage.Stage;
import org.example.agenciaadopcion.entidadesBD.Nino;
import org.example.agenciaadopcion.entidadesBD.Solicitante;

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
        Scene scene = new Scene(mainLayout, 900, 650); // Un poco más ancha para que respire mejor

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

    private void createSideMenu() {
        sideMenu = new VBox(15);
        sideMenu.setPadding(new Insets(20, 10, 20, 10));
        sideMenu.setStyle("-fx-background-color: #34495E; -fx-min-width: 60px;");

        Label menuTitle = new Label("Menú");
        menuTitle.setFont(Font.font("Arial", FontWeight.BOLD, 18));
        menuTitle.setTextFill(Color.WHITE);

        // Centrar título del menú
        menuTitle.setMaxWidth(Double.MAX_VALUE);
        menuTitle.setAlignment(Pos.CENTER);

        menuTitle.setVisible(false);
        menuTitle.managedProperty().bind(menuTitle.visibleProperty());

        // Botones del menú
        Button btnAdopta = createMenuButton("/casa.png", "Adopta");
        Button btnSubir = createMenuButton("/archivo.png", "Subir desde archivo");
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

        } catch (Exception e) {
            // System.out.println("Info: No se encontró imagen para " + text);
        }

        btn.setMaxWidth(Double.MAX_VALUE);
        btn.setAlignment(Pos.CENTER);

        btn.setFont(Font.font("Arial", FontWeight.BOLD, 15));

        btn.setStyle("-fx-background-color: transparent; -fx-text-fill: white; " +
                "-fx-cursor: hand; -fx-padding: 12px;");

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
            String text = (String) btn.getUserData();
            btn.setText("  " + text);
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

    private void showHomePage() {
        BorderPane homeLayout = new BorderPane();
        homeLayout.setPadding(new Insets(0));

        // --- Zona Central ---
        VBox centerContent = new VBox(35);
        centerContent.setAlignment(Pos.CENTER);
        centerContent.setPadding(new Insets(20));

        // 1. Títulos
        VBox headerText = new VBox(8);
        headerText.setAlignment(Pos.CENTER);
        Label title = new Label("Bienvenido a nuestra agencia");
        title.setFont(Font.font("Segoe UI", FontWeight.NORMAL, 36));
        title.setTextFill(Color.web("#2c3e50"));

        Label subtitle = new Label("Seleccione su usuario para consultar o iniciar un proceso");
        subtitle.setFont(Font.font("Segoe UI", FontWeight.LIGHT, 18));
        subtitle.setTextFill(Color.web("#7f8c8d"));
        headerText.getChildren().addAll(title, subtitle);

        // 2. Tarjeta de selección (Solo Solicitante)
        HBox selectionBox = new HBox(15);
        selectionBox.setAlignment(Pos.CENTER);
        selectionBox.setPadding(new Insets(30, 40, 30, 40));
        selectionBox.setMaxWidth(600);
        selectionBox.setStyle("-fx-background-color: white; -fx-background-radius: 20px; -fx-effect: dropshadow(three-pass-box, rgba(0,0,0,0.08), 30, 0, 0, 10);");

        ComboBox<String> solicitanteCombo = new ComboBox<>();
        solicitanteCombo.setPromptText("Seleccione su nombre (Cédula)");
        solicitanteCombo.setPrefWidth(300);
        solicitanteCombo.setPrefHeight(40);
        // Estilo CSS inline para que se vea moderno
        solicitanteCombo.setStyle("-fx-font-size: 14px; -fx-background-color: #f7f9fc; -fx-border-color: #e0e0e0; -fx-border-radius: 5;");

        // Cargamos datos
        solicitanteCombo.setItems(GestorBaseDeDatos.obtenerNombresDropdown());

        Button btnIniciar = new Button("Iniciar Proceso");
        btnIniciar.setPrefHeight(40);
        btnIniciar.setStyle("-fx-background-color: #e88188; -fx-text-fill: white; -fx-font-weight: bold; -fx-font-size: 14px; -fx-background-radius: 20; -fx-cursor: hand;");

        // ACCIÓN DEL BOTÓN: Llamar a la lógica nueva
        btnIniciar.setOnAction(e -> {
            String seleccion = solicitanteCombo.getValue();
            if (seleccion != null) {
                // Extraemos la cédula del string "Nombre Apellido (CEDULA)"
                String cedula = seleccion.substring(seleccion.lastIndexOf("(") + 1, seleccion.lastIndexOf(")"));
                iniciarProceso(cedula);
            } else {
                mostrarAlerta("Atención", "Por favor seleccione un solicitante.");
            }
        });

        selectionBox.getChildren().addAll(solicitanteCombo, btnIniciar);
        centerContent.getChildren().addAll(headerText, selectionBox);
        homeLayout.setCenter(centerContent);

        // --- Zona Inferior (Imagen decorativa) ---
        ImageView illustration = new ImageView();
        try {
            illustration.setImage(new Image(getClass().getResourceAsStream("/ilustracion_niños.png")));
            illustration.setPreserveRatio(true);
            illustration.setFitHeight(200);
        } catch (Exception e) { /* Ignorar si no carga */ }

        VBox bottomContainer = new VBox(illustration);
        bottomContainer.setAlignment(Pos.BOTTOM_CENTER);
        homeLayout.setBottom(bottomContainer);

        contentArea.getChildren().clear();
        contentArea.getChildren().add(homeLayout);
    }

    private void showUploadPage() {
        VBox content = new VBox(25);
        content.setAlignment(Pos.CENTER);
        content.setPadding(new Insets(40));

        Label title = new Label("Cargar datos");
        title.setFont(Font.font("Segoe UI", FontWeight.NORMAL, 32));
        title.setTextFill(Color.web("#2c3e50"));

        Label subtitle = new Label("Sube archivos .csv para actualizar la base de datos");
        subtitle.setFont(Font.font("Segoe UI", FontWeight.LIGHT, 16));
        subtitle.setTextFill(Color.web("#7f8c8d"));

        VBox card = new VBox(20);
        card.setAlignment(Pos.CENTER);
        card.setPadding(new Insets(40));
        card.setMaxWidth(500);
        card.setStyle("-fx-background-color: white; -fx-background-radius: 20px;");

        DropShadow shadow = new DropShadow();
        shadow.setRadius(30);
        shadow.setOffsetY(10);
        shadow.setColor(Color.rgb(0, 0, 0, 0.08));
        card.setEffect(shadow);

        Button uploadBtn = new Button("Seleccionar archivo");
        uploadBtn.setStyle("-fx-background-color: #34495E; -fx-text-fill: white; " +
                "-fx-font-family: 'Segoe UI'; -fx-font-size: 16px; -fx-font-weight: bold; " +
                "-fx-padding: 12px 30px; -fx-background-radius: 25px; -fx-cursor: hand;");

        uploadBtn.setOnMouseEntered(e -> uploadBtn.setStyle("-fx-background-color: #2c3e50; -fx-text-fill: white; -fx-font-family: 'Segoe UI'; -fx-font-size: 16px; -fx-font-weight: bold; -fx-padding: 12px 30px; -fx-background-radius: 25px; -fx-cursor: hand;"));
        uploadBtn.setOnMouseExited(e -> uploadBtn.setStyle("-fx-background-color: #34495E; -fx-text-fill: white; -fx-font-family: 'Segoe UI'; -fx-font-size: 16px; -fx-font-weight: bold; -fx-padding: 12px 30px; -fx-background-radius: 25px; -fx-cursor: hand;"));

        card.getChildren().add(uploadBtn);
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

        // 1. Cambiamos TableView<String> por TableView<Solicitante>
        TableView<Solicitante> table = new TableView<>();

        // 2. Definimos las columnas
        TableColumn<Solicitante, String> colCedula = new TableColumn<>("Cédula");
        // "cedula" debe coincidir con getCedula() en la clase Solicitante
        colCedula.setCellValueFactory(new javafx.scene.control.cell.PropertyValueFactory<>("cedula"));

        TableColumn<Solicitante, String> colNombre = new TableColumn<>("Nombres");
        colNombre.setCellValueFactory(new javafx.scene.control.cell.PropertyValueFactory<>("nombres"));

        TableColumn<Solicitante, String> colApellido = new TableColumn<>("Apellidos");
        colApellido.setCellValueFactory(new javafx.scene.control.cell.PropertyValueFactory<>("apellidos"));

        TableColumn<Solicitante, String> colTel = new TableColumn<>("Teléfono");
        colTel.setCellValueFactory(new javafx.scene.control.cell.PropertyValueFactory<>("telefono"));

        TableColumn<Solicitante, String> colEmail = new TableColumn<>("Email");
        colEmail.setCellValueFactory(new javafx.scene.control.cell.PropertyValueFactory<>("email"));

        // 3. Añadimos columnas a la tabla
        table.getColumns().addAll(colCedula, colNombre, colApellido, colTel, colEmail);

        // 4. Llenamos los datos desde la BD
        table.setItems(GestorBaseDeDatos.obtenerTodosSolicitantes());

        // Estilos
        table.setColumnResizePolicy(TableView.CONSTRAINED_RESIZE_POLICY);
        table.setPlaceholder(new Label("No hay datos"));
        VBox.setVgrow(table, Priority.ALWAYS);

        content.getChildren().addAll(title, table);
        contentArea.getChildren().clear();
        contentArea.getChildren().add(content);
    }

    private void showChildrenPage() {
        VBox content = new VBox(20);
        content.setAlignment(Pos.TOP_LEFT);
        content.setPadding(new Insets(40));

        Label title = new Label("Niños en adopción");
        title.setFont(Font.font("Segoe UI", FontWeight.NORMAL, 28));
        title.setTextFill(Color.web("#2c3e50"));

        // 1. Usamos TableView<Nino>
        TableView<Nino> table = new TableView<>();

        // 2. Columnas
        TableColumn<Nino, String> colNombre = new TableColumn<>("Nombre");
        colNombre.setCellValueFactory(new javafx.scene.control.cell.PropertyValueFactory<>("nombre"));

        TableColumn<Nino, String> colApellido = new TableColumn<>("Apellido");
        colApellido.setCellValueFactory(new javafx.scene.control.cell.PropertyValueFactory<>("apellido"));

        TableColumn<Nino, String> colSexo = new TableColumn<>("Sexo");
        colSexo.setCellValueFactory(new javafx.scene.control.cell.PropertyValueFactory<>("sexo"));

        TableColumn<Nino, String> colEducacion = new TableColumn<>("Educación");
        colEducacion.setCellValueFactory(new javafx.scene.control.cell.PropertyValueFactory<>("nivelEducativo"));

        table.getColumns().addAll(colNombre, colApellido, colSexo, colEducacion);

        // 3. Datos desde BD
        table.setItems(GestorBaseDeDatos.obtenerTodosNinos());

        table.setColumnResizePolicy(TableView.CONSTRAINED_RESIZE_POLICY);
        VBox.setVgrow(table, Priority.ALWAYS);

        content.getChildren().addAll(title, table);
        contentArea.getChildren().clear();
        contentArea.getChildren().add(content);
    }

    public static void main(String[] args) {
        launch(args);
    }

    // Paso 1: Mostrar datos del Solicitante y Pareja (si tiene)
    private void iniciarProceso(String cedula) {
        // 1. Obtener datos completos desde BD
        Solicitante solicitante = GestorBaseDeDatos.buscarSolicitantePorCedula(cedula);
        Solicitante pareja = GestorBaseDeDatos.buscarParejaDe(solicitante.getIdFamilia()); // Necesitarás este método

        // 2. Crear el Popup (Dialog)
        Dialog<ButtonType> dialog = new Dialog<>();
        dialog.setTitle("Detalles de la Solicitud");
        dialog.setHeaderText("Verifique los datos de su familia antes de continuar");

        DialogPane dialogPane = dialog.getDialogPane();
        dialogPane.getButtonTypes().addAll(ButtonType.OK, ButtonType.CANCEL);

        // Layout del contenido
        VBox content = new VBox(10);
        content.setPadding(new Insets(20));

        content.getChildren().add(new Label("Solicitante Principal:"));
        content.getChildren().add(crearTarjetaInfo(solicitante));

        if (pareja != null) {
            content.getChildren().add(new Label("Cónyuge / Pareja:"));
            content.getChildren().add(crearTarjetaInfo(pareja));
        } else {
            content.getChildren().add(new Label("Tipo de Familia: Monoparental"));
        }

        dialogPane.setContent(content);

        // 3. Esperar respuesta del usuario
        dialog.showAndWait().ifPresent(response -> {
            if (response == ButtonType.OK) {
                // AQUÍ SE INSERTA EL PROCESO EN BD (Estado: 'En Curso')
                String idProceso = GestorBaseDeDatos.crearProcesoAdopcion(solicitante.getIdFamilia());

                if (idProceso != null) {
                    abrirVentanaVerificacion(idProceso, solicitante);
                }
            }
        });
    }

    // Paso 2: Ventana de Verificación de Requisitos
    private void abrirVentanaVerificacion(String idProceso, Solicitante sol) {
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle("Verificación de Elegibilidad");
        alert.setHeaderText("Validando requisitos legales...");

        // Simulamos un pequeño proceso visual
        VBox content = new VBox(10);
        Label lblEdad = new Label("⌛ Verificando edad mínima...");
        Label lblIngresos = new Label("⌛ Verificando ingresos económicos...");
        Label lblHistorial = new Label("⌛ Verificando historial...");

        Button btnVerificar = new Button("Ejecutar Verificación");
        btnVerificar.setOnAction(e -> {
            // AQUÍ LLAMAMOS A TUS FUNCIONES SQL
            boolean esApto = GestorBaseDeDatos.ejecutarVerificacionesSQL(sol.getCedula());

            if (esApto) {
                alert.setResult(ButtonType.NEXT); // Forzamos cierre para ir al siguiente paso
                alert.close();
                mostrarPropuestaNino(idProceso); // PASO 3
            } else {
                GestorBaseDeDatos.cancelarProceso(idProceso); // Update estado = Cancelado
                mostrarAlerta("Solicitud Rechazada", "Lo sentimos, no cumple con los requisitos legales en este momento.");
                alert.close();
            }
        });

        content.getChildren().addAll(lblEdad, lblIngresos, lblHistorial, new Separator(), btnVerificar);
        alert.getDialogPane().setContent(content);
        alert.showAndWait();
    }

    // Paso 3: Match con el Niño
    private void mostrarPropuestaNino(String idProceso) {
        // Llamada a la función SQL que busca niño random
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
                // Aquí podrías añadir enfermedades si las traes de la BD
        );

        matchAlert.getDialogPane().setContent(card);
        ButtonType btnAdoptar = new ButtonType("💙 Formalizar Adopción", ButtonBar.ButtonData.OK_DONE);
        matchAlert.getButtonTypes().setAll(btnAdoptar, ButtonType.CANCEL);

        matchAlert.showAndWait().ifPresent(type -> {
            if (type == btnAdoptar) {
                GestorBaseDeDatos.completarAdopcion(idProceso);
                mostrarAlerta("¡Felicidades!", "El proceso ha finalizado exitosamente. ¡Gracias por dar un hogar!");
                showHomePage(); // Volver al inicio
            }
        });
    }

    // Auxiliares
    private VBox crearTarjetaInfo(Solicitante s) {
        VBox box = new VBox(5);
        box.setStyle("-fx-border-color: #ccc; -fx-border-radius: 5; -fx-padding: 10;");
        box.getChildren().addAll(
                new Label("Nombre: " + s.getNombres() + " " + s.getApellidos()),
                new Label("Cédula: " + s.getCedula()),
                new Label("Ingresos: " + s.getIngreso()) // Asegúrate de tener este getter en Solicitante
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
}