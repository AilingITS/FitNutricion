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
import com.squareup.picasso.Picasso;

import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;

public class agregarCenaAdapter extends RecyclerView.Adapter<agregarCenaAdapter.agregarCenaHolder>{
    Context context;
    ArrayList<agregarCena> list;

    public agregarCenaAdapter(Context context, ArrayList<agregarCena> list) {
        this.context = context;
        this.list = list;
    }

    @NonNull
    @NotNull
    @Override
    public agregarCenaAdapter.agregarCenaHolder onCreateViewHolder(@NonNull @NotNull ViewGroup parent, int viewType) {
        View v = LayoutInflater.from(context).inflate(R.layout.item_list_agregar_comida_to_receta,parent,false);
        return new agregarCenaAdapter.agregarCenaHolder(v);
    }

    @Override
    public void onBindViewHolder(@NonNull @NotNull agregarCenaAdapter.agregarCenaHolder holder, int position) {
        agregarCena foods = list.get(position);
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

    public static class agregarCenaHolder extends RecyclerView.ViewHolder{

        TextView f_tipo, f_nombrecomida;
        ImageView f_image;
        Spinner item_list_spinner;

        public agregarCenaHolder(View itemView){
            super(itemView);

            f_tipo = itemView.findViewById(R.id.item_tipo);
            f_nombrecomida = itemView.findViewById(R.id.item_nombrecomida);
            f_image = itemView.findViewById(R.id.item_imagen);
            item_list_spinner = (Spinner) itemView.findViewById(R.id.item_list_spinner);
        }
    }
}
