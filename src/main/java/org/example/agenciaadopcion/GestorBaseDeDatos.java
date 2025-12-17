package org.example.agenciaadopcion;


import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import org.example.agenciaadopcion.entidadesBD.Nino;
import org.example.agenciaadopcion.entidadesBD.Solicitante;


import java.sql.*;


public class GestorBaseDeDatos {


    // 1. INICIAR PROCESO
    public static String iniciarProceso(String idFamilia) {
        String sql = "SELECT iniciar_proceso(?)";
        try (Connection conn = ConexionDB.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {


            ps.setString(1, idFamilia);
            ResultSet rs = ps.executeQuery();
            if (rs.next()) return rs.getString(1);


        } catch (SQLException e) {
            System.err.println("Error al iniciar proceso: " + e.getMessage());
        }
        return null;
    }


    // 2. FINALIZAR PROCESO
    public static void finalizarProceso(String idProceso, String idNino) {
        String sql = "SELECT finalizar_proceso(?, ?)";
        try (Connection conn = ConexionDB.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {


            ps.setString(1, idProceso);
            ps.setString(2, idNino);
            ps.execute();


        } catch (SQLException e) {
            e.printStackTrace();
        }
    }


    // 3. DROPDOWN DE SOLICITANTES
    public static ObservableList<String> obtenerNombresDropdown() {
        ObservableList<String> lista = FXCollections.observableArrayList();
        String sql = "SELECT * FROM listar_solicitantes()";


        try (Connection conn = ConexionDB.getConnection();
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {


            while (rs.next()) {
                lista.add(
                        rs.getString("nombres") + " " +
                                rs.getString("apellidos") + " (" +
                                rs.getString("cedula") + ")"
                );
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return lista;
    }


    // 4. TODOS LOS SOLICITANTES (NO SE TOCA)
    public static ObservableList<Solicitante> obtenerTodosSolicitantes() {
        ObservableList<Solicitante> lista = FXCollections.observableArrayList();
        String sql = """
           SELECT id_solicitante, cedula, nombres, apellidos,
                  telefono, email, id_familia, ingreso_mensual::numeric
           FROM solicitante
       """;


        try (Connection conn = ConexionDB.getConnection();
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {


            while (rs.next()) {
                lista.add(new Solicitante(
                        rs.getString("id_solicitante"),
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


    // 5. BUSCAR SOLICITANTE POR CÉDULA (FUNCIÓN SQL)
    public static Solicitante buscarSolicitantePorCedula(String cedulaBuscada) {
        String sql = "SELECT * FROM buscar_solicitante_completo(?)";


        try (Connection conn = ConexionDB.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {


            ps.setString(1, cedulaBuscada);
            ResultSet rs = ps.executeQuery();


            if (rs.next()) {
                return new Solicitante(
                        rs.getString("id_solicitante"),
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
        return null;
    }


    // 6. BUSCAR PAREJA
    public static Solicitante buscarParejaDe(String idFamilia, String cedulaExcluir) {
        String sql = "SELECT * FROM buscar_pareja_familia(?, ?)";


        try (Connection conn = ConexionDB.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {


            ps.setString(1, idFamilia);
            ps.setString(2, cedulaExcluir);
            ResultSet rs = ps.executeQuery();


            if (rs.next()) {
                return new Solicitante(
                        rs.getString("id_solicitante"),
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
        return null;
    }


    // 7. ¿SOLICITANTE LIBRE? (BUG CORREGIDO)
    public static boolean checkSolicitanteLibre(String cedula) {
        String sql = "SELECT solicitante_proceso_adopcion FROM solicitante WHERE cedula = ?";


        try (Connection conn = ConexionDB.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {


            ps.setString(1, cedula);
            ResultSet rs = ps.executeQuery();


            if (rs.next()) {
                String estado = rs.getString("solicitante_proceso_adopcion");
                return estado != null && "Sin Asignar".equalsIgnoreCase(estado.trim());
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return false;
    }


    // 8. VALIDACIONES (YA ESTABAN BIEN)
    public static boolean checkEdad(String cedula) {
        return ejecutarCheckBooleano("SELECT validar_edad_solicitante(?)", obtenerIdPorCedula(cedula));
    }


    public static boolean checkIngresos(String cedula) {
        return ejecutarCheckBooleano("SELECT validar_ingreso_solicitante(?)", obtenerIdPorCedula(cedula));
    }


    public static boolean checkSalud(String cedula) {
        return ejecutarCheckBooleano("SELECT validar_enfermedades_solicitante(?)", obtenerIdPorCedula(cedula));
    }


    public static boolean checkAntecedentes(String cedula) {
        return ejecutarCheckBooleano("SELECT validar_antecedentes_penales(?)", obtenerIdPorCedula(cedula));
    }


    public static boolean checkDocumentosCompletos(String cedula) {
        return ejecutarCheckBooleano("SELECT validar_documentos_solicitante(?)", obtenerIdPorCedula(cedula));
    }


    private static boolean ejecutarCheckBooleano(String sql, String param) {
        try (Connection conn = ConexionDB.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {


            ps.setString(1, param);
            ResultSet rs = ps.executeQuery();
            return rs.next() && rs.getBoolean(1);


        } catch (SQLException e) {
            e.printStackTrace();
        }
        return false;
    }


    // 9. CREAR PROCESO
    public static String crearProceso(String idFamilia) {
        String sql = "SELECT crear_proceso_adopcion(?)";
        try (Connection conn = ConexionDB.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {


            ps.setString(1, idFamilia);
            ResultSet rs = ps.executeQuery();
            if (rs.next()) return rs.getString(1);


        } catch (SQLException e) {
            e.printStackTrace();
        }
        return null;
    }


    // 10. ASIGNAR NIÑO
    public static Nino asignarNinoAleatorio(String idProceso) {
        String sql = "SELECT * FROM asignar_nino_aleatorio(?)";


        try (Connection conn = ConexionDB.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {


            ps.setString(1, idProceso);
            ResultSet rs = ps.executeQuery();


            if (rs.next()) {
                return new Nino(
                        rs.getString("id_nino"),
                        rs.getString("nombres"),
                        rs.getString("apellidos"),
                        rs.getString("sexo"),
                        rs.getString("nivel_educacion")
                );
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return null;
    }


    // 11. COMPLETAR PROCESO (NO TOCAR)
    public static void completarProceso(String idProceso) {
        String sqlGetNino = "SELECT id_niño FROM proceso WHERE id_proceso = ?";
        String sqlFinalizar = "SELECT finalizar_proceso(?, ?)";


        try (Connection conn = ConexionDB.getConnection()) {


            String idNino = null;
            try (PreparedStatement ps1 = conn.prepareStatement(sqlGetNino)) {
                ps1.setString(1, idProceso);
                ResultSet rs = ps1.executeQuery();
                if (rs.next()) idNino = rs.getString("id_niño");
            }


            if (idNino != null) {
                try (PreparedStatement ps2 = conn.prepareStatement(sqlFinalizar)) {
                    ps2.setString(1, idProceso);
                    ps2.setString(2, idNino);
                    ps2.execute();
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }


    // 12. CANCELAR PROCESO
    public static void cancelarProceso(String idProceso, String motivo) {
        String sql = "SELECT cancelar_proceso(?, ?)";
        try (Connection conn = ConexionDB.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {


            ps.setString(1, idProceso);
            ps.setString(2, motivo);
            ps.execute();


        } catch (SQLException e) {
            e.printStackTrace();
        }
    }


    // 13. NIÑOS DISPONIBLES
    public static ObservableList<Nino> obtenerTodosNinos() {
        ObservableList<Nino> lista = FXCollections.observableArrayList();
        String sql = "SELECT * FROM listar_ninos_disponibles()";


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


    // AUXILIAR (SE DEJA COMO ESTABA)
    private static String obtenerIdPorCedula(String cedula) {
        String sql = "SELECT id_solicitante FROM solicitante WHERE cedula = ?";
        try (Connection conn = ConexionDB.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {


            ps.setString(1, cedula);
            ResultSet rs = ps.executeQuery();
            if (rs.next()) return rs.getString("id_solicitante");


        } catch (SQLException e) {
            e.printStackTrace();
        }
        return null;
    }


    // 14. REGISTRAR INTENTO FALLIDO
    public static void registrarIntentoFallido(String idFamilia, String motivo, String tipo) {
        String sql = "SELECT registrar_intento_fallido(?, ?, ?)";
        try (Connection conn = ConexionDB.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {


            ps.setString(1, idFamilia);
            ps.setString(2, motivo);
            ps.setString(3, tipo);
            ps.execute();


        } catch (SQLException e) {
            e.printStackTrace();
        }
    }
}

