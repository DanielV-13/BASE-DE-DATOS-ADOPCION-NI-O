package org.example.agenciaadopcion;

import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import org.example.agenciaadopcion.entidadesBD.Nino;
import org.example.agenciaadopcion.entidadesBD.Solicitante;

import java.sql.*;

public class GestorBaseDeDatos {

    // 1. INICIAR (Llama a la función SQL iniciar_proceso)
    public static String iniciarProceso(String idFamilia) {
        String sql = "SELECT iniciar_proceso(?)";
        try (Connection conn = ConexionDB.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setString(1, idFamilia);
            ResultSet rs = ps.executeQuery();

            if (rs.next()) {
                String nuevoId = rs.getString(1);
                System.out.println("✅ Proceso iniciado con ID: " + nuevoId);
                return nuevoId;
            }
        } catch (SQLException e) {
            System.err.println("Error al iniciar proceso: " + e.getMessage());
        }
        return null;
    }

    // 2. FINALIZAR (Llama a finalizar_proceso)
    public static void finalizarProceso(String idProceso, String idNino) {
        String sql = "SELECT finalizar_proceso(?, ?)";
        try (Connection conn = ConexionDB.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setString(1, idProceso);
            ps.setString(2, idNino);
            ps.execute();

            System.out.println("🎉 Adopción formalizada correctamente en BD.");
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    // 1. OBTENER LISTA PARA EL DROPDOWN (Menú principal)
    public static ObservableList<String> obtenerNombresDropdown() {
        ObservableList<String> lista = FXCollections.observableArrayList();
        // Usamos la función que devuelve la tabla de solicitantes
        String sql = "SELECT * FROM listar_solicitantes()";

        try (Connection conn = ConexionDB.getConnection();
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {

            while (rs.next()) {
                // Formato: "Juan Perez (1712345678)"
                String texto = rs.getString("nombres") + " " +
                        rs.getString("apellidos") + " (" +
                        rs.getString("cedula") + ")";
                lista.add(texto);
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return lista;
    }

    // 2. OBTENER TODOS LOS SOLICITANTES (Para la tabla de Administración)
    public static ObservableList<Solicitante> obtenerTodosSolicitantes() {
        ObservableList<Solicitante> lista = FXCollections.observableArrayList();
        // CAMBIO CLAVE: ingreso_mensual::numeric
        String sql = "SELECT id_solicitante, cedula, nombres, apellidos, telefono, email, id_familia, ingreso_mensual::numeric FROM solicitante";

        // ... el resto sigue igual ...
        try (Connection conn = ConexionDB.getConnection();
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {

            while (rs.next()) {
                lista.add(new Solicitante(
                        rs.getString("id_solicitante"), // <--- Nuevo ID
                        rs.getString("cedula"),
                        rs.getString("nombres"),
                        rs.getString("apellidos"),
                        rs.getString("telefono"),
                        rs.getString("email"),
                        rs.getString("id_familia"),
                        rs.getDouble("ingreso_mensual")
                ));
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return lista;
    }

    // 3. BUSCAR SOLICITANTE POR CÉDULA
    public static Solicitante buscarSolicitantePorCedula(String cedulaBuscada) {
        Solicitante sol = null;
        // CAMBIO CLAVE: ingreso_mensual::numeric
        String sql = "SELECT id_solicitante, cedula, nombres, apellidos, telefono, email, id_familia, ingreso_mensual::numeric " +
                "FROM solicitante WHERE cedula = ?";

        try (Connection conn = ConexionDB.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {

            pstmt.setString(1, cedulaBuscada);
            ResultSet rs = pstmt.executeQuery();

            if (rs.next()) {
                sol = new Solicitante(
                        rs.getString("id_solicitante"),
                        rs.getString("cedula"),
                        rs.getString("nombres"),
                        rs.getString("apellidos"),
                        rs.getString("telefono"),
                        rs.getString("email"),
                        rs.getString("id_familia"),
                        rs.getDouble("ingreso_mensual") // ¡Ahora sí funcionará!
                );
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return sol;
    }

    // 4. BUSCAR PAREJA DE LA MISMA FAMILIA
    public static Solicitante buscarParejaDe(String idFamilia, String cedulaExcluir) {
        Solicitante pareja = null;
        // CAMBIO CLAVE: ingreso_mensual::numeric
        String sql = "SELECT id_solicitante, cedula, nombres, apellidos, telefono, email, id_familia, ingreso_mensual::numeric " +
                "FROM solicitante WHERE id_familia = ? AND cedula != ?";

        // ... el resto sigue igual ...
        try (Connection conn = ConexionDB.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {

            pstmt.setString(1, idFamilia);
            pstmt.setString(2, cedulaExcluir);
            ResultSet rs = pstmt.executeQuery();

            if (rs.next()) {
                pareja = new Solicitante(
                        rs.getString("id_solicitante"), // <--- Nuevo ID
                        rs.getString("cedula"),
                        rs.getString("nombres"),
                        rs.getString("apellidos"),
                        rs.getString("telefono"),
                        rs.getString("email"),
                        rs.getString("id_familia"),
                        rs.getDouble("ingreso_mensual")
                );
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return pareja;
    }

    // 5. VALIDACIÓN DE ESTADO: ¿TIENE PROCESO ACTIVO? (Para el "Semáforo")
    public static boolean checkSolicitanteLibre(String cedula) {
        // Esta consulta verifica si el campo solicitante_proceso_adopcion es 'Sin Asignar'
        String sql = "SELECT solicitante_proceso_adopcion FROM solicitante WHERE cedula = ?";

        try (Connection conn = ConexionDB.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setString(1, cedula);
            ResultSet rs = ps.executeQuery();

            if (rs.next()) {
                String estado = rs.getString("solicitante_proceso_adopcion");
                // Retorna TRUE solo si está libre ('Sin Asignar')
                return "Sin Asignar".equalsIgnoreCase(estado);
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return false; // Por defecto asumimos ocupado si falla
    }

    // 6. VALIDACIONES INDIVIDUALES (Llaman a funciones booleanas de SQL)
    public static boolean checkEdad(String cedula) {
        String id = obtenerIdPorCedula(cedula);
        return ejecutarCheckBooleano("SELECT validar_edad_solicitante(?)", id);
    }

    public static boolean checkIngresos(String cedula) {
        String id = obtenerIdPorCedula(cedula);
        return ejecutarCheckBooleano("SELECT validar_ingreso_solicitante(?)", id);
    }

    public static boolean checkSalud(String cedula) {
        String id = obtenerIdPorCedula(cedula);
        return ejecutarCheckBooleano("SELECT validar_enfermedades_solicitante(?)", id);
    }

    public static boolean checkAntecedentes(String cedula) {
        String id = obtenerIdPorCedula(cedula);
        return ejecutarCheckBooleano("SELECT validar_antecedentes_penales(?)", id);
    }

    public static boolean checkDocumentosCompletos(String cedula) {
        String id = obtenerIdPorCedula(cedula);
        return ejecutarCheckBooleano("SELECT validar_documentos_solicitante(?)", id);
    }

    // Método auxiliar para no repetir código de conexión
    private static boolean ejecutarCheckBooleano(String sql, String parametro) {
        try (Connection conn = ConexionDB.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, parametro);
            ResultSet rs = ps.executeQuery();
            if (rs.next()) {
                return rs.getBoolean(1); // Devuelve el TRUE/FALSE de la función SQL
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return false;
    }

    // 7. CREAR PROCESO (Retorna ID o NULL si falla)
    public static String crearProceso(String idFamilia) {
        String sql = "SELECT crear_proceso_adopcion(?)";
        try (Connection conn = ConexionDB.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, idFamilia);
            ResultSet rs = ps.executeQuery();
            if (rs.next()) return rs.getString(1);
        } catch (SQLException e) {
            System.err.println("Error creando proceso: " + e.getMessage());
        }
        return null;
    }

    // 8. ASIGNAR NIÑO ALEATORIO
    public static Nino asignarNinoAleatorio(String idProceso) {
        Nino nino = null;
        String sql = "SELECT * FROM asignar_nino_aleatorio(?)";

        try (Connection conn = ConexionDB.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {

            pstmt.setString(1, idProceso);
            ResultSet rs = pstmt.executeQuery();

            if (rs.next()) {
                nino = new Nino(
                        rs.getString("id_nino"), // OJO: id_nino (sin ñ) porque así lo definimos en la función SQL
                        rs.getString("nombres"),
                        rs.getString("apellidos"),
                        rs.getString("sexo"),
                        rs.getString("nivel_educacion")
                );
            }
        } catch (SQLException e) {
            System.err.println("Aviso BD (Asignación): " + e.getMessage());
        }
        return nino;
    }

    // 9. COMPLETAR PROCESO (Actualiza estado, fecha y libera padres)
    // 9. COMPLETAR PROCESO (VERSIÓN DEPURACIÓN CON ALERTAS)
    public static void completarProceso(String idProceso) {
        String sqlGetNino = "SELECT id_niño FROM proceso WHERE id_proceso = ?";
        String sqlFinalizar = "SELECT finalizar_proceso(?, ?)";

        try (Connection conn = ConexionDB.getConnection()) {

            // 1. Averiguar qué niño tiene asignado este proceso
            String idNino = null;
            try (PreparedStatement ps1 = conn.prepareStatement(sqlGetNino)) {
                ps1.setString(1, idProceso);
                ResultSet rs = ps1.executeQuery();
                if (rs.next()) {
                    idNino = rs.getString("id_niño");
                }
            }

            // 2. Si hay niño, llamamos a la función final
            if (idNino != null) {
                try (PreparedStatement ps2 = conn.prepareStatement(sqlFinalizar)) {
                    ps2.setString(1, idProceso);
                    ps2.setString(2, idNino);
                    ps2.execute();
                    System.out.println("✅ Adopción finalizada en BD.");
                }
            } else {
                // ERROR 1: EL NIÑO ES NULL
                mostrarError("Error Crítico", "El proceso " + idProceso + " no tiene un niño asignado en la BD.\nEl UPDATE anterior falló.");
            }

        } catch (SQLException e) {
            // ERROR 2: FALLO SQL (AQUÍ ESTÁ TU PROBLEMA SEGURAMENTE)
            e.printStackTrace();
            mostrarError("Error de Base de Datos", "No se pudo finalizar el proceso:\n" + e.getMessage());
        }
    }

    // Pequeño método auxiliar para mostrar errores desde aquí
    private static void mostrarError(String titulo, String mensaje) {
        javafx.application.Platform.runLater(() -> {
            javafx.scene.control.Alert alert = new javafx.scene.control.Alert(javafx.scene.control.Alert.AlertType.ERROR);
            alert.setTitle(titulo);
            alert.setHeaderText(null);
            alert.setContentText(mensaje);
            alert.showAndWait();
        });
    }

    // 10. CANCELAR PROCESO
    public static void cancelarProceso(String idProceso, String motivo) {
        String sql = "SELECT cancelar_proceso(?, ?)";
        try (Connection conn = ConexionDB.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setString(1, idProceso);
            pstmt.setString(2, motivo);
            pstmt.execute();
            System.out.println("⛔ Proceso cancelado. Motivo: " + motivo);
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    // 11. OBTENER NIÑOS DISPONIBLES (Para la tabla de "Niños")
    public static ObservableList<Nino> obtenerTodosNinos() {
        ObservableList<Nino> lista = FXCollections.observableArrayList();
        // Llamamos a la función que filtra solo los "Sin Asignar"
        String sql = "SELECT * FROM listar_ninos_disponibles()";

        try (Connection conn = ConexionDB.getConnection();
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {

            while (rs.next()) {
                lista.add(new Nino(
                        rs.getString("id_niño"), // Aquí sí es id_niño (con ñ) porque viene del TYPE TABLE
                        rs.getString("nombres"),
                        rs.getString("apellidos"),
                        rs.getString("sexo"),
                        rs.getString("nivel_educacion")
                ));
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return lista;
    }

    // AUXILIAR: Obtener ID (SOL-XXX) a partir de Cédula
    private static String obtenerIdPorCedula(String cedula) {
        String sql = "SELECT id_solicitante FROM solicitante WHERE cedula = ?";
        try (Connection conn = ConexionDB.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, cedula);
            ResultSet rs = ps.executeQuery();
            if (rs.next()) return rs.getString("id_solicitante");
        } catch (SQLException e) { e.printStackTrace(); }
        return null;
    }


    public static void registrarIntentoFallido(String idFamilia, String motivo, String tipo) {
        String sql = "SELECT registrar_intento_fallido(?, ?, ?)";
        try (Connection conn = ConexionDB.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setString(1, idFamilia);
            ps.setString(2, motivo); // Ej: "Ingresos insuficientes"
            ps.setString(3, tipo);   // Ej: "ECONOMICO"

            ps.execute();
            System.out.println("❌ Intento fallido registrado en BD: " + tipo);
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }
}