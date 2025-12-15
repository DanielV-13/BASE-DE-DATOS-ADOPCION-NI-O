package org.example.agenciaadopcion;

import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import org.example.agenciaadopcion.entidadesBD.Nino;
import org.example.agenciaadopcion.entidadesBD.Solicitante;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class GestorBaseDeDatos {

    public static void mostrarSolicitantes() {
        // Usamos un try-with-resources. Esto cierra automáticamente la conexión y el statement al terminar.
        // Fíjate cómo llamamos a tu clase ConexionDB aquí:
        try (Connection conn = ConexionDB.getConnection();
             Statement stmt = conn.createStatement()) {

            // Recuerda: Al ser una función que retorna tabla, usamos SELECT
            String sql = "SELECT * FROM listar_solicitantes()";

            System.out.println("⏳ Consultando base de datos...");
            ResultSet rs = stmt.executeQuery(sql);

            System.out.println("\n--- LISTADO DE SOLICITANTES ---");
            System.out.printf("%-15s %-20s %-20s %-15s%n", "CEDULA", "NOMBRE", "APELLIDO", "TELEFONO");
            System.out.println("----------------------------------------------------------------------");

            // Iteramos sobre los resultados
            while (rs.next()) {
                String cedula = rs.getString("cedula");
                String nombres = rs.getString("nombres");
                String apellidos = rs.getString("apellidos");
                String telefono = rs.getString("telefono");

                // Imprimimos con formato bonito
                System.out.printf("%-15s %-20s %-20s %-15s%n", cedula, nombres, apellidos, telefono);
            }

        } catch (SQLException e) {
            System.err.println("❌ Error al ejecutar el procedimiento: " + e.getMessage());
            e.printStackTrace();
        }
    }

    // Un main pequeñito para probar solo esta funcionalidad
    public static void main(String[] args) {
        mostrarSolicitantes();
    }

    public static List<String> obtenerNombresSolicitantes() {
        List<String> listaNombres = new ArrayList<>();

        try (Connection conn = ConexionDB.getConnection();
             Statement stmt = conn.createStatement()) {

            // Llamamos a tu función de SQL
            String sql = "SELECT nombres, apellidos FROM listar_solicitantes()";
            ResultSet rs = stmt.executeQuery(sql);

            while (rs.next()) {
                String nombre = rs.getString("nombres");
                String apellido = rs.getString("apellidos");
                // Guardamos "Nombre Apellido" en la lista
                listaNombres.add(nombre + " " + apellido);
            }

        } catch (SQLException e) {
            System.err.println("❌ Error al cargar lista: " + e.getMessage());
        }

        return listaNombres;
    }


    // 1. PARA EL DROPDOWN (Modificado con Cédula)
    public static ObservableList<String> obtenerNombresDropdown() {
        ObservableList<String> lista = FXCollections.observableArrayList();

        try (Connection conn = ConexionDB.getConnection();
             Statement stmt = conn.createStatement()) {

            // Pedimos también la cédula
            ResultSet rs = stmt.executeQuery("SELECT nombres, apellidos, cedula FROM listar_solicitantes()");

            while (rs.next()) {
                String nombre = rs.getString("nombres");
                String apellido = rs.getString("apellidos");
                String cedula = rs.getString("cedula");
                // AQUÍ EL FORMATO QUE PEDISTE:
                lista.add(nombre + " " + apellido + " (" + cedula + ")");
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return lista;
    }

    // 2. PARA LA TABLA DE SOLICITANTES (Devuelve objetos completos)
    public static ObservableList<Solicitante> obtenerTodosSolicitantes() {
        ObservableList<Solicitante> lista = FXCollections.observableArrayList();

        try (Connection conn = ConexionDB.getConnection();
             Statement stmt = conn.createStatement()) {

            // Usamos tu función almacenada
            ResultSet rs = stmt.executeQuery("SELECT * FROM listar_solicitantes()");

            while (rs.next()) {
                // Creamos el objeto y lo metemos a la lista
                lista.add(new Solicitante(
                        rs.getString("cedula"),
                        rs.getString("nombres"),
                        rs.getString("apellidos"),
                        rs.getString("telefono"),
                        rs.getString("email"),
                        rs.getInt("id_familia"),    // <--- Agrega esto
                        rs.getDouble("ingreso")     // <--- Y esto
                ));
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return lista;
    }

    // 3. PARA LA TABLA DE NIÑOS
    public static ObservableList<Nino> obtenerTodosNinos() {
        ObservableList<Nino> lista = FXCollections.observableArrayList();

        try (Connection conn = ConexionDB.getConnection();
             Statement stmt = conn.createStatement()) {

            // Asumiendo que harás un "SELECT * FROM nino" o crearás una función stored procedure similar
            ResultSet rs = stmt.executeQuery("SELECT * from listar_ninos()");

            while (rs.next()) {
                lista.add(new Nino(
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

    public static Solicitante buscarSolicitantePorCedula(String cedulaBuscada) {
        Solicitante sol = null;
        String sql = "SELECT * FROM solicitante WHERE cedula = ?";

        try (Connection conn = ConexionDB.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {

            pstmt.setString(1, cedulaBuscada);
            ResultSet rs = pstmt.executeQuery();

            if (rs.next()) {
                sol = new Solicitante(
                        rs.getString("cedula"),
                        rs.getString("nombres"),
                        rs.getString("apellidos"),
                        rs.getString("telefono"),
                        rs.getString("email"),
                        rs.getInt("id_familia"),    // Nuevo campo
                        rs.getDouble("ingreso")     // Nuevo campo
                );
            }
        } catch (SQLException e) {
            System.err.println("Error al buscar solicitante: " + e.getMessage());
        }
        return sol;
    }

    // 5. BUSCAR LA PAREJA (Mismo ID Familia, diferente Cédula)
    public static Solicitante buscarParejaDe(int idFamilia, String cedulaSolicitante) {
        Solicitante pareja = null;
        // Buscamos a alguien de la misma familia que NO sea el solicitante actual
        String sql = "SELECT * FROM solicitante WHERE id_familia = ? AND cedula != ?";

        try (Connection conn = ConexionDB.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {

            pstmt.setInt(1, idFamilia);
            pstmt.setString(2, cedulaSolicitante);
            ResultSet rs = pstmt.executeQuery();

            if (rs.next()) {
                pareja = new Solicitante(
                        rs.getString("cedula"),
                        rs.getString("nombres"),
                        rs.getString("apellidos"),
                        rs.getString("telefono"),
                        rs.getString("email"),
                        rs.getInt("id_familia"),
                        rs.getDouble("ingreso")
                );
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return pareja;
    }

    // Sobrecarga para facilitar el llamado si solo tenemos el ID Familia (trae el primero que encuentre)
    public static Solicitante buscarParejaDe(int idFamilia) {
        // Nota: Este es un método auxiliar simple. Para ser exactos deberíamos pasar la cédula del original.
        // Pero para que compile tu código actual:
        return null;
        // Si quieres implementarlo bien, necesitas cambiar la llamada en Aplicacion.java para pasar la cédula
        // O dejarlo retornar null si es monoparental.
    }

    // 6. CREAR PROCESO DE ADOPCIÓN (INSERT)
    public static String crearProcesoAdopcion(int idFamilia) {
        String idGenerado = null;
        // Usamos RETURNING id_proceso para obtener el ID generado automáticamente por la BD
        String sql = "INSERT INTO proceso_adopcion (id_familia, fecha_solicitud, estado_proceso) " +
                "VALUES (?, NOW(), 'En Curso') RETURNING id_proceso";

        try (Connection conn = ConexionDB.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {

            pstmt.setInt(1, idFamilia);
            ResultSet rs = pstmt.executeQuery();

            if (rs.next()) {
                idGenerado = rs.getString("id_proceso");
                System.out.println("✅ Proceso creado con ID: " + idGenerado);
            }

        } catch (SQLException e) {
            System.err.println("Error al crear proceso: " + e.getMessage());
        }
        return idGenerado;
    }

    // 7. EJECUTAR VERIFICACIONES (Llama a las funciones SQL)
    public static boolean ejecutarVerificacionesSQL(String idSolicitante) {
        boolean esApto = false;
        // Llamamos a las funciones que creamos en SQL
        // Nota: Ajusta los nombres de funciones si en tu SQL los llamaste diferente
        String sql = "SELECT verificar_edad(?) AS edad_ok, " +
                "verificar_ingresos(?) AS ingresos_ok";
        // Puedes agregar más: verificar_proceso_activo(?) etc.

        try (Connection conn = ConexionDB.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {

            // Asumiendo que idSolicitante es el ID (o cédula si tu función SQL usa cédula)
            // Si tus funciones SQL reciben ID (ej. 'SOL-001'), asegúrate de pasar eso.
            // Si reciben cédula, pasa la cédula.
            // Aquí asumiré que pasamos el ID del solicitante
            String idReal = obtenerIdSolicitantePorCedula(idSolicitante); // Método auxiliar abajo

            pstmt.setString(1, idReal);
            pstmt.setString(2, idReal);

            ResultSet rs = pstmt.executeQuery();

            if (rs.next()) {
                boolean edadOk = rs.getBoolean("edad_ok");
                boolean ingresosOk = rs.getBoolean("ingresos_ok");

                System.out.println("Verificación - Edad: " + edadOk + ", Ingresos: " + ingresosOk);
                esApto = edadOk && ingresosOk;
            }

        } catch (SQLException e) {
            System.err.println("Error en verificaciones: " + e.getMessage());
            // Si falla SQL, asumimos falso por seguridad
            return false;
        }
        return esApto;
    }

    // Auxiliar para conseguir el ID (SOL-XXX) usando la cédula
    private static String obtenerIdSolicitantePorCedula(String cedula) {
        String id = "";
        try (Connection conn = ConexionDB.getConnection();
             PreparedStatement ps = conn.prepareStatement("SELECT id_solicitante FROM solicitante WHERE cedula = ?")) {
            ps.setString(1, cedula);
            ResultSet rs = ps.executeQuery();
            if (rs.next()) id = rs.getString("id_solicitante");
        } catch (SQLException e) { e.printStackTrace(); }
        return id;
    }

    // 8. ASIGNAR NIÑO ALEATORIO
    public static Nino asignarNinoAleatorio(String idProceso) {
        Nino nino = null;
        // Llamamos a la función compleja de SQL
        String sql = "SELECT * FROM asignar_nino_aleatorio(?)";

        try (Connection conn = ConexionDB.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {

            pstmt.setString(1, idProceso);
            ResultSet rs = pstmt.executeQuery();

            if (rs.next()) {
                String idNino = rs.getString("nino_id");

                // Si la función SQL devolvió un niño (no null)
                if (idNino != null) {
                    // Ahora consultamos los detalles completos de ese niño para mostrarlos
                    nino = obtenerDetallesNino(idNino);
                }
            }

        } catch (SQLException e) {
            e.printStackTrace();
        }
        return nino;
    }

    private static Nino obtenerDetallesNino(String idNino) {
        Nino n = null;
        try (Connection conn = ConexionDB.getConnection();
             PreparedStatement ps = conn.prepareStatement("SELECT * FROM \"niño\" WHERE \"id_niño\" = ?")) { // Ojo con las comillas en "niño"
            ps.setString(1, idNino);
            ResultSet rs = ps.executeQuery();
            if (rs.next()) {
                n = new Nino(
                        rs.getString("nombres"),
                        rs.getString("apellidos"),
                        rs.getString("sexo"),
                        rs.getString("nivel_educacion")
                );
            }
        } catch (SQLException e) { e.printStackTrace(); }
        return n;
    }

    // 9. COMPLETAR ADOPCIÓN (UPDATE FINAL)
    public static void completarAdopcion(String idProceso) {
        String sql = "UPDATE proceso_adopcion SET estado_proceso = 'Completado', fecha_finalizacion = NOW() WHERE id_proceso = ?";

        try (Connection conn = ConexionDB.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {

            pstmt.setString(1, idProceso);
            pstmt.executeUpdate();
            System.out.println("✅ Adopción completada exitosamente.");

        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    // 10. CANCELAR PROCESO
    public static void cancelarProceso(String idProceso) {
        String sql = "UPDATE proceso_adopcion SET estado_proceso = 'Cancelado', fecha_finalizacion = NOW() WHERE id_proceso = ?";

        try (Connection conn = ConexionDB.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {

            pstmt.setString(1, idProceso);
            pstmt.executeUpdate();
            System.out.println("⛔ Proceso cancelado.");

        } catch (SQLException e) {
            e.printStackTrace();
        }
    }
}

