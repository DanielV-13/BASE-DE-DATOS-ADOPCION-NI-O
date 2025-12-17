package org.example.agenciaadopcion.reportes;


public class ReporteEstadoProceso {


    private String estadoProceso;
    private int cantidad;


    public ReporteEstadoProceso(String estadoProceso, int cantidad) {
        this.estadoProceso = estadoProceso;
        this.cantidad = cantidad;
    }


    public String getEstadoProceso() {
        return estadoProceso;
    }


    public int getCantidad() {
        return cantidad;
    }
}

