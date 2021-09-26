package com.example.fitnutricion.fragments;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.Spinner;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;

import com.example.fitnutricion.R;
import com.example.fitnutricion.firebase.Pacientes;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.List;

public class HomeFragment extends Fragment {

    private static final String ARG_PARAM1 = "param1";
    private static final String ARG_PARAM2 = "param2";

    private String mParam1;
    private String mParam2;

    private View vista;
    private Spinner spinnerComidas, spinnerPacientes;

    DatabaseReference mDatabase;

    public HomeFragment() {
        // Required empty public constructor
    }

    public static HomeFragment newInstance(String param1, String param2) {
        HomeFragment fragment = new HomeFragment();
        Bundle args = new Bundle();
        args.putString(ARG_PARAM1, param1);
        args.putString(ARG_PARAM2, param2);
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (getArguments() != null) {
            mParam1 = getArguments().getString(ARG_PARAM1);
            mParam2 = getArguments().getString(ARG_PARAM2);
        }
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        vista = inflater.inflate(R.layout.fragment_home, container, false);

        mDatabase = FirebaseDatabase.getInstance().getReference();
        spinnerComidas = vista.findViewById(R.id.spinnerComidas);
        spinnerPacientes = vista.findViewById(R.id.spinnerPacientes);

        String [] opciones = {"Desayuno", "Comida", "Cena"};

        ArrayAdapter <String> adapter = new ArrayAdapter<String>(getContext(), android.R.layout.simple_spinner_item, opciones);
        spinnerComidas.setAdapter(adapter);

        String seleccion = spinnerComidas.getSelectedItem().toString();
        loadNamePacientes();

        //Poner esto cuando se cree el pdf para saber el tipo de comida
        /*if(seleccion.equals("Desayuno")){

        } else if(seleccion.equals("Comida")){

        } else if(seleccion.equals("Cena")){

        }*/

        return vista;
    }

    public void loadNamePacientes(){
        List<Pacientes> pacientesList = new ArrayList<>();
        mDatabase.child("pacientes").addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull @NotNull DataSnapshot snapshot) {
                if(snapshot.exists()){
                    for(DataSnapshot ds: snapshot.getChildren()){
                        String p_Nombre = ds.child("p_Nombre").getValue().toString();
                        pacientesList.add(new Pacientes(p_Nombre));
                    }

                    ArrayAdapter<Pacientes> arrayAdapter = new ArrayAdapter<>(getActivity(), android.R.layout.simple_dropdown_item_1line, pacientesList);
                    spinnerPacientes.setAdapter(arrayAdapter);
                }
            }

            @Override
            public void onCancelled(@NonNull @NotNull DatabaseError error) {

            }
        });
    }
}