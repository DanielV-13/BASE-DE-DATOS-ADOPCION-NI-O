package org.example.agenciaadopcion.entidadesBD;

public class Solicitante {
    private String cedula;
    private String nombres;
    private String apellidos;
    private String telefono;
    private String email;

    public Solicitante(String cedula, String nombres, String apellidos, String telefono, String email) {
        this.cedula = cedula;
        this.nombres = nombres;
        this.apellidos = apellidos;
        this.telefono = telefono;
        this.email = email;
    }

    // Getters (Necesarios para que la Tabla los lea)
    public String getCedula() { return cedula; }
    public String getNombres() { return nombres; }
    public String getApellidos() { return apellidos; }
    public String getTelefono() { return telefono; }
    public String getEmail() { return email; }
}