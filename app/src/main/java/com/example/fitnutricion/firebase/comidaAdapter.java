package com.example.fitnutricion.firebase;

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.RecyclerView;
import com.example.fitnutricion.R;
import com.example.fitnutricion.fragments.EditarComidaFragment;
import com.example.fitnutricion.fragments.EditarDesayunoFragment;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.squareup.picasso.Picasso;

import org.jetbrains.annotations.NotNull;
import java.util.ArrayList;

public class comidaAdapter extends RecyclerView.Adapter<comidaAdapter.foodsHolder> {

    Context context;
    ArrayList<Comida> list;

    String userID;
    FirebaseAuth mAuth;
    DatabaseReference dbRef;

    public comidaAdapter(Context context, ArrayList<Comida> list) {
        this.context = context;
        this.list = list;
    }

    @NonNull
    @NotNull
    @Override
    public comidaAdapter.foodsHolder onCreateViewHolder(@NonNull @NotNull ViewGroup parent, int viewType) {
        View v = LayoutInflater.from(context).inflate(R.layout.item_list_comidas,parent,false);
        return new comidaAdapter.foodsHolder(v);
    }

    @Override
    public void onBindViewHolder(@NonNull @NotNull comidaAdapter.foodsHolder holder, int position) {

        mAuth = FirebaseAuth.getInstance();
        userID = mAuth.getCurrentUser().getUid();

        dbRef = FirebaseDatabase.getInstance().getReference("users").child(userID).child("comidas");

        Comida foods = list.get(position);
        holder.f_tipo.setText(foods.getF_tipo());
        holder.f_nombrecomida.setText(foods.getF_nombrecomida());
        holder.f_ingredientes.setText(foods.getF_ingredientes());
        holder.f_calorias.setText(foods.getF_calorias());
        Picasso.get().load(foods.getF_image()).into(holder.f_image);

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
                            AppCompatActivity activity = (AppCompatActivity) context;
                            Fragment myFragment = new EditarComidaFragment(foods.getP_ID());
                            activity.getSupportFragmentManager().beginTransaction().replace(R.id.body_container, myFragment).addToBackStack(null).commit();
                        }
                        if(opciones[which] == "Borrar"){
                            list.clear();

                            dbRef.child(foods.getP_ID()).removeValue().addOnCompleteListener(new OnCompleteListener<Void>() {
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

    public static class foodsHolder extends RecyclerView.ViewHolder{

        TextView f_tipo, f_nombrecomida, f_ingredientes, f_calorias;
        ImageView f_image;

        public foodsHolder(View itemView){
            super(itemView);

            f_tipo = itemView.findViewById(R.id.item_tipo);
            f_nombrecomida = itemView.findViewById(R.id.item_nombrecomida);
            f_ingredientes = itemView.findViewById(R.id.item_ingredientes);
            f_calorias = itemView.findViewById(R.id.item_calorias);
            f_image = itemView.findViewById(R.id.item_imagen);
        }
    }
}