package com.example.fitnutricion.firebase.home_comidas_agregar;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.Spinner;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.fitnutricion.R;
import com.example.fitnutricion.firebase.Desayuno;
import com.example.fitnutricion.firebase.desayunoAdapter;
import com.squareup.picasso.Picasso;

import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;

public class agregarComidaAdapter extends RecyclerView.Adapter<agregarComidaAdapter.agregarComidaHolder>{

    Context context;
    ArrayList<agregarComida> list;

    public agregarComidaAdapter(Context context, ArrayList<agregarComida> list) {
        this.context = context;
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
        agregarComida foods = list.get(position);
        holder.f_tipo.setText(foods.getF_tipo());
        holder.f_nombrecomida.setText(foods.getF_nombrecomida());

        ArrayAdapter<CharSequence> adapter = ArrayAdapter.createFromResource(context,
                R.array.item_list_semana, android.R.layout.simple_spinner_item);
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        holder.item_list_spinner.setAdapter(adapter);

        Picasso.get().load(foods.getF_image()).into(holder.f_image);
    }

    @Override
    public int getItemCount() {
        return list.size();
    }

    public static class agregarComidaHolder extends RecyclerView.ViewHolder{

        TextView f_tipo, f_nombrecomida;
                //, f_ingredientes, f_calorias;
        ImageView f_image;
        Spinner item_list_spinner;

        public agregarComidaHolder(View itemView){
            super(itemView);

            f_tipo = itemView.findViewById(R.id.item_tipo);
            f_nombrecomida = itemView.findViewById(R.id.item_nombrecomida);
            //f_ingredientes = itemView.findViewById(R.id.item_ingredientes);
            //f_calorias = itemView.findViewById(R.id.item_calorias);
            f_image = itemView.findViewById(R.id.item_imagen);
            item_list_spinner = (Spinner) itemView.findViewById(R.id.item_list_spinner);
        }
    }
}
