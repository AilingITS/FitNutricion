package com.example.fitnutricion.firebase;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.Context;
import android.content.ContextWrapper;
import android.content.DialogInterface;
import android.content.Intent;
import android.transition.TransitionManager;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentTransaction;
import androidx.recyclerview.widget.RecyclerView;

import com.example.fitnutricion.R;
import com.example.fitnutricion.fragments.EditarPacienteFragment;
import com.example.fitnutricion.fragments.HomeFragment;
import com.example.fitnutricion.fragments.PacientesFragment;
import com.example.fitnutricion.fragments.ProfileFragment;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.firestore.DocumentSnapshot;

import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

public class pacientesAdapter  extends RecyclerView.Adapter<pacientesAdapter.pacientesHolder> {

    Context context;
    View view;
    ArrayList<Pacientes> list;

    String userID;
    FirebaseAuth mAuth;
    DatabaseReference dbRef;

    public pacientesAdapter(Context context, View view, ArrayList<Pacientes> list) {
        this.context = context;
        this.view = view;
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

        mAuth = FirebaseAuth.getInstance();
        userID = mAuth.getCurrentUser().getUid();

        dbRef = FirebaseDatabase.getInstance().getReference("users").child(userID).child("pacientes");

        Pacientes pacientes = list.get(position);
        holder.p_Nombre.setText(pacientes.getP_Nombre());
        holder.p_Correo.setText(pacientes.getP_Correo());
        holder.p_Edad.setText(pacientes.getP_Edad());
        holder.p_Sexo.setText(pacientes.getP_Sexo());

        holder.itemView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                AlertDialog.Builder dialogo1 = new AlertDialog.Builder(context);
                dialogo1.setCancelable(true);

                final CharSequence[] opciones = new CharSequence[2];
                opciones[0] = "Editar";
                opciones[1] = "Borrar";
                dialogo1.setTitle("Seleccione una opción");
                dialogo1.setItems(opciones, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        if(opciones[which] == "Editar"){
                            AppCompatActivity activity = (AppCompatActivity) view.getContext();
                            Fragment myFragment = new EditarPacienteFragment(pacientes.getP_ID());
                            activity.getSupportFragmentManager().beginTransaction().replace(R.id.body_container, myFragment).addToBackStack(null).commit();

                        }
                        if(opciones[which] == "Borrar"){
                            list.clear();

                            dbRef.child(pacientes.getP_ID()).removeValue().addOnCompleteListener(new OnCompleteListener<Void>() {
                                @Override
                                public void onComplete(@NonNull @NotNull Task<Void> task) {
                                    Toast.makeText(context, "Se ha borrado correctamente", Toast.LENGTH_SHORT).show();
                                }
                            });
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
