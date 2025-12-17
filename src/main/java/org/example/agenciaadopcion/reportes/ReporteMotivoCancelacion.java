package org.example.agenciaadopcion.reportes;


public class ReporteMotivoCancelacion {


    private String tipoMotivo;
    private int cantidad;
    private String ejemplos;


    public ReporteMotivoCancelacion(String tipoMotivo, int cantidad, String ejemplos) {
        this.tipoMotivo = tipoMotivo;
        this.cantidad = cantidad;
        this.ejemplos = ejemplos;
    }


    public String getTipoMotivo() {
        return tipoMotivo;
    }


    public int getCantidad() {
        return cantidad;
    }


    public String getEjemplos() {
        return ejemplos;
    }
}

