package org.example.agenciaadopcion;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;

public class ConexionDB { // 1. La clase abre aquí

    private static final String URL = "jdbc:postgresql://localhost:5432/PROYECTO BASE DE DATOS";
    private static final String USER = "postgres";
    private static final String PASS = "daniel13102005"; // Asegúrate que esta sea la contraseña real


    public static Connection getConnection() {
        try {
            return DriverManager.getConnection(URL, USER, PASS);
        } catch (SQLException e) {
            System.err.println("❌ Error SQL: " + e.getMessage());
            throw new RuntimeException("Error de conexión", e);
        }
    }

    public static void main(String[] args) {
        Connection conn = getConnection();
        if (conn != null) {
            System.out.println("✅ Conexión exitosa a la base de datos!");
        }
    }

} // 5. La clase cierra AQUÍ, al final del archivo
