package org.example.agenciaadopcion.reportes;


import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import org.example.agenciaadopcion.ConexionDB;


import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.Statement;


public class GestorReportes {


    /* =========================
       REPORTE: ESTADOS DE PROCESO
       ========================= */
    public static ObservableList<ReporteEstadoProceso> obtenerReporteEstados() {


        ObservableList<ReporteEstadoProceso> lista = FXCollections.observableArrayList();


        String sql = "SELECT estado_proceso, cantidad FROM vista_estadisticas_procesos";


        try (Connection c = ConexionDB.getConnection();
             Statement st = c.createStatement();
             ResultSet rs = st.executeQuery(sql)) {


            while (rs.next()) {
                lista.add(new ReporteEstadoProceso(
                        rs.getString("estado_proceso"),
                        rs.getInt("cantidad")
                ));
            }


        } catch (Exception e) {
            e.printStackTrace();
        }


        return lista;
    }


    /* =========================
       REPORTE: MOTIVOS DE CANCELACIÓN
       ========================= */
    public static ObservableList<ReporteMotivoCancelacion> obtenerReporteMotivos() {


        ObservableList<ReporteMotivoCancelacion> lista = FXCollections.observableArrayList();


        String sql = "SELECT tipo_motivo, cantidad, ejemplos FROM vista_motivos_rechazo";


        try (Connection c = ConexionDB.getConnection();
             Statement st = c.createStatement();
             ResultSet rs = st.executeQuery(sql)) {


            while (rs.next()) {
                lista.add(new ReporteMotivoCancelacion(
                        rs.getString("tipo_motivo"),
                        rs.getInt("cantidad"),
                        rs.getString("ejemplos")
                ));
            }


        } catch (Exception e) {
            e.printStackTrace();
        }


        return lista;
    }


    /* =========================
   REPORTE: ADOPCIONES
   ========================= */
    public static ObservableList<ReporteAdopcion> obtenerReporteAdopciones() {


        ObservableList<ReporteAdopcion> lista = FXCollections.observableArrayList();


        String sql = "SELECT * FROM vista_reporte_adopciones";


        try (Connection c = ConexionDB.getConnection();
             Statement st = c.createStatement();
             ResultSet rs = st.executeQuery(sql)) {


            while (rs.next()) {
                lista.add(new ReporteAdopcion(
                        rs.getString("id_proceso"),
                        rs.getDate("fecha_solicitud").toLocalDate(),
                        rs.getString("id_familia"),
                        rs.getString("padres"),
                        rs.getString("nombre_nino"),     // ya viene "nombre apellido"
                        rs.getString("sexo_nino"),
                        rs.getString("nivel_educacion")
                ));
            }


        } catch (Exception e) {
            e.printStackTrace();
        }


        return lista;
    }








}

