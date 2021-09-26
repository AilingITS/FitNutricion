package com.example.fitnutricion.firebase;

public class Pacientes{

    String p_Nombre, p_Correo, p_Edad;

    public Pacientes(String p_Nombre) {
        this.p_Nombre = p_Nombre;
    }

    public String getP_Nombre() {
        return p_Nombre;
    }

    public String getP_Correo() {
        return p_Correo;
    }

    public String getP_Edad() {
        return p_Edad;
    }

    @Override
    public String toString() {
        return p_Nombre;
    }
}
