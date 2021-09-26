package com.example.fitnutricion.firebase;

public class SpinnerPaciente {

    String p_Nombre;

    public SpinnerPaciente(String p_Nombre) {
        this.p_Nombre = p_Nombre;
    }

    public String getP_Nombre() {
        return p_Nombre;
    }

    @Override
    public String toString() {
        return p_Nombre;
    }

}
