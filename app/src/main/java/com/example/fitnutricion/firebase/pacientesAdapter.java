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
        holder.Nombre.setText(pacientes.getNombre());
        holder.Correo.setText(pacientes.getCorreo());
        holder.Contraseña.setText(pacientes.getContraseña());

    }

    @Override
    public int getItemCount() {
        return list.size();
    }

    public static class pacientesHolder extends RecyclerView.ViewHolder{

        TextView Nombre, Correo, Contraseña;

        public pacientesHolder(View itemView){
            super(itemView);

            Nombre = itemView.findViewById(R.id.item_name);
            Correo = itemView.findViewById(R.id.item_mail);
            Contraseña = itemView.findViewById(R.id.item_age);
        }
    }

}
