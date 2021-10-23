package com.example.fitnutricion.pacienteReceta;

import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.example.fitnutricion.R;
import com.example.fitnutricion.firebase.Desayuno;
import com.example.fitnutricion.firebase.desayunoAdapter;
import com.example.fitnutricion.firebase.home_comidas_agregar.agregarComida;
import com.example.fitnutricion.firebase.home_comidas_agregar.agregarComidaAdapter;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;

public class PacienteDesayunoFragment extends Fragment {

    private static final String ARG_PARAM1 = "param1";
    private static final String ARG_PARAM2 = "param2";
    private String mParam1;
    private String mParam2;

    private View vista;

    private String userID;
    private FirebaseAuth mAuth;
    DatabaseReference dbRef;

    RecyclerView recyclerView;
    agregarComidaAdapter myAdapter;
    ArrayList<agregarComida> list;

    public PacienteDesayunoFragment() {
        // Required empty public constructor
    }

    public static PacienteDesayunoFragment newInstance(String param1, String param2) {
        PacienteDesayunoFragment fragment = new PacienteDesayunoFragment();
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
        // Inflate the layout for this fragment
        vista = inflater.inflate(R.layout.fragment_paciente_desayuno, container, false);

        mAuth = FirebaseAuth.getInstance();
        userID = mAuth.getCurrentUser().getUid();
        dbRef = FirebaseDatabase.getInstance().getReference().child("users").child(userID).child("desayunos");

        recyclerView = vista.findViewById(R.id.agregardesayunoList);
        recyclerView.setHasFixedSize(true);
        recyclerView.setLayoutManager(new LinearLayoutManager(getContext()));

        list = new ArrayList<>();
        myAdapter = new agregarComidaAdapter(getContext(),list);
        recyclerView.setAdapter(myAdapter);

        dbRef.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull @NotNull DataSnapshot snapshot) {

                for(DataSnapshot dataSnapshot : snapshot.getChildren()){
                    agregarComida agregar_comida = dataSnapshot.getValue(agregarComida.class);
                    list.add(agregar_comida);
                }
                myAdapter.notifyDataSetChanged();
            }

            @Override
            public void onCancelled(@NonNull @NotNull DatabaseError error) { }
        });

        return vista;
    }
}