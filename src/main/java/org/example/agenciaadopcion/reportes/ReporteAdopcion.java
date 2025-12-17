package org.example.agenciaadopcion.reportes;


import java.time.LocalDate;


public class ReporteAdopcion {


    private String idProceso;
    private LocalDate fechaSolicitud;
    private String idFamilia;
    private String padres;
    private String nombreNino;     // nombre + apellido juntos (como viene en la vista)
    private String sexoNino;
    private String nivelEducacion;


    public ReporteAdopcion(String idProceso, LocalDate fechaSolicitud, String idFamilia,
                           String padres, String nombreNino, String sexoNino, String nivelEducacion) {
        this.idProceso = idProceso;
        this.fechaSolicitud = fechaSolicitud;
        this.idFamilia = idFamilia;
        this.padres = padres;
        this.nombreNino = nombreNino;
        this.sexoNino = sexoNino;
        this.nivelEducacion = nivelEducacion;
    }


    public String getIdProceso() { return idProceso; }
    public LocalDate getFechaSolicitud() { return fechaSolicitud; }
    public String getIdFamilia() { return idFamilia; }
    public String getPadres() { return padres; }
    public String getNombreNino() { return nombreNino; }
    public String getSexoNino() { return sexoNino; }
    public String getNivelEducacion() { return nivelEducacion; }
}

