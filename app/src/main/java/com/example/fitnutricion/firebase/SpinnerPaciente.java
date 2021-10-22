package com.example.fitnutricion.firebase;

public class SpinnerPaciente {

    String pacienteID, p_Nombre;

    public SpinnerPaciente(String pacienteID, String p_Nombre) {
        this.pacienteID = pacienteID;
        this.p_Nombre = p_Nombre;
    }

    public String getPacienteID() {
        return pacienteID;
    }

    public String getP_Nombre() {
        return p_Nombre;
    }

    @Override
    public String toString() {
        return p_Nombre;
    }
}
