package org.example.agenciaadopcion.entidadesBD;

public class Nino {
    private String idNino;
    private String nombre;
    private String apellido;
    private String sexo;
    private String nivelEducativo;

    public Nino(String idNino,String nombre, String apellido, String sexo, String nivelEducativo) {
        this.idNino = idNino;
        this.nombre = nombre;
        this.apellido = apellido;
        this.sexo = sexo;
        this.nivelEducativo = nivelEducativo;
    }
    public String getIdNino() {return idNino;}
    public String getNombre() { return nombre; }
    public String getApellido() { return apellido; }
    public String getSexo() { return sexo; }
    public String getNivelEducativo() { return nivelEducativo; }
}