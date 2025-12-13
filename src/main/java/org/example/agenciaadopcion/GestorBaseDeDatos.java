package org.example.agenciaadopcion;

import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import org.example.agenciaadopcion.entidadesBD.Nino;
import org.example.agenciaadopcion.entidadesBD.Solicitante;

import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.Statement;
import java.sql.SQLException;
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
                        rs.getString("email")
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
}

