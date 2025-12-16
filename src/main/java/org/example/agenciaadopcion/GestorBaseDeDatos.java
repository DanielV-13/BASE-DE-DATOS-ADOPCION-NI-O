package org.example.agenciaadopcion;

import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import org.example.agenciaadopcion.entidadesBD.Nino;
import org.example.agenciaadopcion.entidadesBD.Solicitante;

import java.sql.*;

public class GestorBaseDeDatos {

    // 1. OBTENER LISTA PARA EL DROPDOWN
    public static ObservableList<String> obtenerNombresDropdown() {
        ObservableList<String> lista = FXCollections.observableArrayList();
        String sql = "SELECT nombres, apellidos, cedula FROM listar_solicitantes()";

        try (Connection conn = ConexionDB.getConnection();
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {

            while (rs.next()) {
                lista.add(rs.getString("nombres") + " " +
                        rs.getString("apellidos") + " (" +
                        rs.getString("cedula") + ")");
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return lista;
    }

    // 2. OBTENER TODOS LOS SOLICITANTES
    public static ObservableList<Solicitante> obtenerTodosSolicitantes() {
        ObservableList<Solicitante> lista = FXCollections.observableArrayList();
        String sql = "SELECT * FROM listar_solicitantes()";

        try (Connection conn = ConexionDB.getConnection();
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {

            while (rs.next()) {
                lista.add(new Solicitante(
                        rs.getString("cedula"),
                        rs.getString("nombres"),
                        rs.getString("apellidos"),
                        rs.getString("telefono"),
                        rs.getString("email"),
                        rs.getString("id_familia"),
                        rs.getDouble("ingreso")
                ));
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return lista;
    }

    // 3. BUSCAR UN SOLICITANTE POR CÉDULA
    public static Solicitante buscarSolicitantePorCedula(String cedulaBuscada) {
        Solicitante sol = null;
        String sql = "SELECT * FROM obtener_info_solicitante(?)";

        try (Connection conn = ConexionDB.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {

            pstmt.setString(1, cedulaBuscada);
            ResultSet rs = pstmt.executeQuery();

            if (rs.next()) {
                sol = new Solicitante();
                sol.setCedula(rs.getString("cedula"));
                sol.setNombres(rs.getString("nombres"));
                sol.setApellidos(rs.getString("apellidos"));
                sol.setTelefono(rs.getString("telefono"));
                sol.setEmail(rs.getString("email"));
                sol.setIdFamilia(rs.getString("id_familia"));
                sol.setIngreso(rs.getDouble("ingreso"));
            }
        } catch (SQLException e) {
            System.err.println("Error al buscar solicitante: " + e.getMessage());
        }
        return sol;
    }

    // 4. BUSCAR PAREJA
    public static Solicitante buscarParejaDe(String idFamilia, String cedulaActual) {
        Solicitante pareja = null;
        String sql = "SELECT * FROM buscar_pareja_familia(?, ?)";

        try (Connection conn = ConexionDB.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {

            pstmt.setString(1, idFamilia);
            pstmt.setString(2, cedulaActual);
            ResultSet rs = pstmt.executeQuery();

            if (rs.next()) {
                pareja = new Solicitante();
                pareja.setCedula(rs.getString("cedula"));
                pareja.setNombres(rs.getString("nombres"));
                pareja.setApellidos(rs.getString("apellidos"));
                pareja.setIngreso(rs.getDouble("ingreso"));
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return pareja;
    }

    // 5. CREAR PROCESO
    public static String crearProceso(String idFamilia) {
        String sql = "SELECT crear_proceso_adopcion(?)";
        try (Connection conn = ConexionDB.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, idFamilia);
            ResultSet rs = ps.executeQuery();
            if (rs.next()) return rs.getString(1);
        } catch (SQLException e) { e.printStackTrace(); }
        return null;
    }

    // 6. REGISTRAR DOCUMENTOS
    public static void registrarDocumento(String cedulaSolicitante, String idTipoDoc, boolean presentado) {
        String sql = "UPDATE verif_doc_solicitante " +
                "SET presentado = ? " +
                "WHERE id_solicitante = (SELECT id_solicitante FROM solicitante WHERE cedula = ?) " +
                "AND id_tipodocumento = ?";

        try (Connection conn = ConexionDB.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {

            pstmt.setBoolean(1, presentado);
            pstmt.setString(2, cedulaSolicitante);
            pstmt.setString(3, idTipoDoc);
            pstmt.executeUpdate();

        } catch (SQLException e) {
            System.err.println("Error al registrar documento: " + e.getMessage());
        }
    }

    // 7. VERIFICACIONES INDIVIDUALES (Usadas por tu interfaz animada)

    public static boolean checkEdad(String cedula) {
        String id = obtenerIdPorCedula(cedula);
        String sql = "SELECT validar_edad_solicitante(?)";
        return ejecutarCheckBooleano(sql, id);
    }

    public static boolean checkIngresos(String cedula) {
        String id = obtenerIdPorCedula(cedula);
        String sql = "SELECT validar_ingreso_solicitante(?)";
        return ejecutarCheckBooleano(sql, id);
    }

    public static boolean checkSalud(String cedula) {
        String id = obtenerIdPorCedula(cedula);
        String sql = "SELECT validar_enfermedades_solicitante(?)";
        return ejecutarCheckBooleano(sql, id);
    }

    public static boolean checkAntecedentes(String cedula) {
        String id = obtenerIdPorCedula(cedula);
        String sql = "SELECT validar_antecedentes_penales(?)";
        return ejecutarCheckBooleano(sql, id);
    }

    public static boolean checkDocumentosCompletos(String cedula) {
        String id = obtenerIdPorCedula(cedula);
        String sql = "SELECT validar_documentos_solicitante(?)";
        return ejecutarCheckBooleano(sql, id);
    }

    // Método auxiliar para no repetir código de conexión en los checks
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

    // 8. ASIGNAR NIÑO ALEATORIO (LOGICA MANUAL CORREGIDA)
    // Esta función busca un niño libre y actualiza la BD, vital porque quitamos los triggers.
    public static Nino asignarNinoAleatorio(String idProceso) {
        Nino nino = null;

        // 1. Buscar niño disponible
        String sqlBuscar = "SELECT * FROM niño WHERE Niño_Proceso_Adopcion = 'Sin Asignar' ORDER BY random() LIMIT 1";
        // 2. Vincular al proceso
        String sqlUpdateProceso = "UPDATE proceso SET id_niño = ? WHERE id_proceso = ?";
        // 3. Marcar niño como ocupado
        String sqlUpdateNino = "UPDATE niño SET Niño_Proceso_Adopcion = 'Asignado' WHERE id_niño = ?";

        try (Connection conn = ConexionDB.getConnection()) {
            // A. Encontrar al niño
            try (Statement stmt = conn.createStatement();
                 ResultSet rs = stmt.executeQuery(sqlBuscar)) {
                if (rs.next()) {
                    nino = new Nino(
                            rs.getString("id_niño"),
                            rs.getString("nombres"),
                            rs.getString("apellidos"),
                            rs.getString("sexo"),
                            rs.getString("nivel_educacion")
                    );
                }
            }

            if (nino != null) {
                // B. Actualizar Proceso
                try (PreparedStatement ps = conn.prepareStatement(sqlUpdateProceso)) {
                    ps.setString(1, nino.getIdNino());
                    ps.setString(2, idProceso);
                    ps.executeUpdate();
                }
                // C. Actualizar Estado Niño
                try (PreparedStatement ps = conn.prepareStatement(sqlUpdateNino)) {
                    ps.setString(1, nino.getIdNino());
                    ps.executeUpdate();
                }
            }

        } catch (SQLException e) {
            e.printStackTrace();
        }
        return nino;
    }

    // 9. COMPLETAR ADOPCIÓN
    public static void completarProceso(String idProceso) {
        String sql = "SELECT completar_proceso(?)";
        try (Connection conn = ConexionDB.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setString(1, idProceso);
            pstmt.execute();
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    // 10. CANCELAR PROCESO
    public static void cancelarProceso(String idProceso, String motivo) {
        String sql = "SELECT cancelar_proceso(?, ?)";
        try (Connection conn = ConexionDB.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setString(1, idProceso);
            pstmt.setString(2, motivo);
            pstmt.execute();
            System.out.println("⛔ Proceso " + idProceso + " cancelado. Motivo: " + motivo);
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    // 11. OBTENER NIÑOS (Para la tabla de "Mostrar Niños")
    public static ObservableList<Nino> obtenerTodosNinos() {
        ObservableList<Nino> lista = FXCollections.observableArrayList();
        String sql = "SELECT id_niño, nombres, apellidos, sexo, nivel_educacion FROM niño WHERE Niño_Proceso_Adopcion = 'Sin Asignar'";

        try (Connection conn = ConexionDB.getConnection();
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {

            while (rs.next()) {
                lista.add(new Nino(
                        rs.getString("id_niño"),
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

    // AUXILIAR
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
}