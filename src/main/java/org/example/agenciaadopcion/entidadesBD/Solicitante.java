package org.example.agenciaadopcion.entidadesBD;

public class Solicitante {
    // 1. NUEVO CAMPO AGREGADO
    private String idSolicitante; // Ej: "SOL-0001"

    private String cedula;
    private String nombres;
    private String apellidos;
    private String telefono;
    private String email;
    private String idFamilia;
    private double ingreso;

    public Solicitante() {}

    // 2. CONSTRUCTOR ACTUALIZADO (Ahora recibe el ID al principio)
    public Solicitante(String idSolicitante, String cedula, String nombres, String apellidos, String telefono, String email, String idFamilia, double ingreso) {
        this.idSolicitante = idSolicitante; // <--- Nuevo
        this.cedula = cedula;
        this.nombres = nombres;
        this.apellidos = apellidos;
        this.telefono = telefono;
        this.email = email;
        this.idFamilia = idFamilia;
        this.ingreso = ingreso;
    }

    // 3. GETTER Y SETTER NUEVOS
    public String getIdSolicitante() { return idSolicitante; }
    public void setIdSolicitante(String idSolicitante) { this.idSolicitante = idSolicitante; }

    // (El resto de Getters y Setters queda IGUAL)
    public String getCedula() { return cedula; }
    public String getNombres() { return nombres; }
    public String getApellidos() { return apellidos; }
    public String getTelefono() { return telefono; }
    public String getEmail() { return email; }
    public String getIdFamilia() { return idFamilia; }
    public double getIngreso() { return ingreso; }

    public void setCedula(String cedula) { this.cedula = cedula; }
    public void setNombres(String nombres) { this.nombres = nombres; }
    public void setApellidos(String apellidos) { this.apellidos = apellidos; }
    public void setTelefono(String telefono) { this.telefono = telefono; }
    public void setEmail(String email) { this.email = email; }
    public void setIdFamilia(String idFamilia) {this.idFamilia = idFamilia; }
    public void setIngreso(double ingreso) { this.ingreso = ingreso; }
}