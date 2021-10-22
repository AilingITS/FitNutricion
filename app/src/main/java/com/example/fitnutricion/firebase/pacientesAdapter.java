package com.example.fitnutricion.firebase;

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.fitnutricion.R;
import com.google.firebase.firestore.DocumentSnapshot;

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

            itemView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    AlertDialog.Builder dialogo1 = new AlertDialog.Builder(itemView.getContext());
                    dialogo1.setCancelable(true);

                    final CharSequence[] opciones = new CharSequence[2];
                    opciones[0] = "Editar";
                    opciones[1] = "Borrar";
                    dialogo1.setTitle("Seleccione una opción");
                    dialogo1.setItems(opciones, new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            if(opciones[which] == "Editar"){
                                Toast.makeText(itemView.getContext(), "Se ha editado correctamente", Toast.LENGTH_SHORT).show();
                            }
                            if(opciones[which] == "Borrar"){
                                Toast.makeText(itemView.getContext(), "Se ha borrado correctamente", Toast.LENGTH_SHORT).show();
                            }
                        }
                    });
                    dialogo1.setNegativeButton("Cancelar", new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            dialog.dismiss();
                        }
                    });
                    dialogo1.show();
                }
            });

        }
    }
}