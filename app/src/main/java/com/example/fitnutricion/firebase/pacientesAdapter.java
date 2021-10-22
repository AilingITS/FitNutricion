package com.example.fitnutricion.firebase;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.fitnutricion.R;

import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;

public class pacientesAdapter  extends RecyclerView.Adapter<pacientesAdapter.pacientesHolder> {

    Context context;

    ArrayList<Pacientes> list;

    public pacientesAdapter(Context context, ArrayList<Pacientes> list) {
        this.context = context;
        this.list = list;
    }

    @NonNull
    @NotNull
    @Override
    public pacientesHolder onCreateViewHolder(@NonNull @NotNull ViewGroup parent, int viewType) {
        View v = LayoutInflater.from(context).inflate(R.layout.item_list_pacientes,parent,false);
        return new pacientesHolder(v);
    }

    @Override
    public void onBindViewHolder(@NonNull @NotNull pacientesAdapter.pacientesHolder holder, int position) {

        Pacientes pacientes = list.get(position);
        holder.p_Nombre.setText(pacientes.getP_Nombre());
        holder.p_Correo.setText(pacientes.getP_Correo());
        holder.p_Edad.setText(pacientes.getP_Edad());
        holder.p_Sexo.setText(pacientes.getP_Sexo());

    }

    @Override
    public int getItemCount() {
        return list.size();
    }

    public static class pacientesHolder extends RecyclerView.ViewHolder{

        TextView p_Nombre, p_Correo, p_Edad, p_Sexo;

        public pacientesHolder(View itemView){
            super(itemView);

            p_Nombre = itemView.findViewById(R.id.item_name);
            p_Correo = itemView.findViewById(R.id.item_mail);
            p_Edad = itemView.findViewById(R.id.item_age);
            p_Sexo = itemView.findViewById(R.id.item_sexo);
        }
    }

}