package com.example.fitnutricion.firebase.home_comidas_agregar;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.fitnutricion.R;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.squareup.picasso.Picasso;

import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

public class agregarComidaAdapter extends RecyclerView.Adapter<agregarComidaAdapter.agregarComidaHolder>{

    Context context;
    ArrayList<agregarComida> list;

    View view;
    private String userID;
    private FirebaseAuth mAuth;
    private DatabaseReference dbRef;

    public agregarComidaAdapter(Context context, View view, ArrayList<agregarComida> list) {
        this.context = context;
        this.view = view;
        this.list = list;
    }

    @NonNull
    @NotNull
    @Override
    public agregarComidaAdapter.agregarComidaHolder onCreateViewHolder(@NonNull @NotNull ViewGroup parent, int viewType) {
        View v = LayoutInflater.from(context).inflate(R.layout.item_list_agregar_comida_to_receta,parent,false);
        return new agregarComidaAdapter.agregarComidaHolder(v);
    }

    @Override
    public void onBindViewHolder(@NonNull @NotNull agregarComidaAdapter.agregarComidaHolder holder, int position) {

        mAuth = FirebaseAuth.getInstance();
        userID = mAuth.getCurrentUser().getUid();
        dbRef = FirebaseDatabase.getInstance().getReference().child("users").child(userID).child("recetas");

        agregarComida foods = list.get(position);
        holder.f_tipo.setText(foods.getF_tipo());
        holder.f_nombrecomida.setText(foods.getF_nombrecomida());
        Picasso.get().load(foods.getF_image()).into(holder.f_image);

        ArrayAdapter<CharSequence> adapter = ArrayAdapter.createFromResource(context,
                R.array.item_list_semana, android.R.layout.simple_spinner_item);
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        holder.item_list_spinner.setAdapter(adapter);

        holder.btn_f_agregarComida.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String f_id = "Comida";
                String f_dia_id = holder.item_list_spinner.getSelectedItem().toString();

                Map<String, Object> recetaMAP = new HashMap<>();
                recetaMAP.put("f_tipo", "Comida");
                recetaMAP.put("f_nombrecomida", foods.getF_nombrecomida());
                recetaMAP.put("f_ingredientes", foods.getF_ingredientes());
                recetaMAP.put("f_calorias", foods.getF_calorias());
                recetaMAP.put("f_image", foods.getF_image());
                recetaMAP.put("f_dia_id", f_dia_id);

                dbRef.child(f_dia_id).child(f_id).updateChildren(recetaMAP).addOnCompleteListener(new OnCompleteListener<Void>() {
                    @Override
                    public void onComplete(@NonNull @NotNull Task<Void> task) {
                        if(task.isSuccessful()){
                            Toast.makeText(context, "Comida agregada correctamente a la receta", Toast.LENGTH_SHORT).show();
                            /*AppCompatActivity activity = (AppCompatActivity) view.getContext();
                            Fragment myFragment = new PacienteDesayunoFragment();
                            activity.getSupportFragmentManager().beginTransaction().replace(R.id.body_container, myFragment).addToBackStack(null).commit();*/

                        } else {
                            String message = task.getException().toString();
                            Toast.makeText(context, "Error: " + message, Toast.LENGTH_SHORT).show();
                        }
                    }
                });
            }
        });
    }

    @Override
    public int getItemCount() {
        return list.size();
    }

    public static class agregarComidaHolder extends RecyclerView.ViewHolder{

        TextView f_tipo, f_nombrecomida, f_ingredientes, f_calorias;
        ImageView f_image;
        Spinner item_list_spinner;
        Button btn_f_agregarComida;

        public agregarComidaHolder(View itemView){
            super(itemView);

            f_tipo = itemView.findViewById(R.id.item_tipo);
            f_nombrecomida = itemView.findViewById(R.id.item_nombrecomida);
            f_ingredientes = itemView.findViewById(R.id.item_ingredientes);
            f_calorias = itemView.findViewById(R.id.item_calorias);
            f_image = itemView.findViewById(R.id.item_imagen);

            item_list_spinner = (Spinner) itemView.findViewById(R.id.item_list_spinner);
            btn_f_agregarComida = (Button) itemView.findViewById(R.id.btn_f_agregarComida);
        }
    }

}
