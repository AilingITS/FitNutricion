package com.example.fitnutricion.fragments.comidasRecetas;

import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatDelegate;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentTransaction;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;

import com.example.fitnutricion.R;
import com.example.fitnutricion.firebase.Desayuno;
import com.example.fitnutricion.firebase.desayunoAdapter;
import com.example.fitnutricion.fragments.AgregarDesayunoFragment;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;

public class DesayunoFragment extends Fragment {

    private static final String ARG_PARAM1 = "param1";
    private static final String ARG_PARAM2 = "param2";

    private String mParam1;
    private String mParam2;

    private View vista;

    RecyclerView recyclerView;
    DatabaseReference dbRef;
    desayunoAdapter myAdapter;
    ArrayList<Desayuno> list;

    Button btn_agregarComida;

    public DesayunoFragment() {
        // Required empty public constructor
    }

    public static DesayunoFragment newInstance(String param1, String param2) {
        DesayunoFragment fragment = new DesayunoFragment();
        Bundle args = new Bundle();
        args.putString(ARG_PARAM1, param1);
        args.putString(ARG_PARAM2, param2);
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        // checar condicion night mode en settings
        if(AppCompatDelegate.getDefaultNightMode() == AppCompatDelegate.MODE_NIGHT_YES){
            getActivity().setTheme(R.style.ThemeDark_FitNutricion);
        } else {
            getActivity().setTheme(R.style.ThemeLight_FitNutricion);
        }

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
        vista = inflater.inflate(R.layout.fragment_desayuno, container, false);

        btn_agregarComida = (Button) vista.findViewById(R.id.btn_agregarComida);

        recyclerView = vista.findViewById(R.id.desayunoList);
        dbRef = FirebaseDatabase.getInstance().getReference("desayunos");
        recyclerView.setHasFixedSize(true);
        recyclerView.setLayoutManager(new LinearLayoutManager(getContext()));

        list = new ArrayList<>();
        myAdapter = new desayunoAdapter(getContext(),list);
        recyclerView.setAdapter(myAdapter);

        btn_agregarComida.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                replaceFragment(new AgregarDesayunoFragment());
            }
        });

        dbRef.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull @NotNull DataSnapshot snapshot) {

                for(DataSnapshot dataSnapshot : snapshot.getChildren()){
                    Desayuno foods = dataSnapshot.getValue(Desayuno.class);
                    list.add(foods);
                }
                myAdapter.notifyDataSetChanged();
            }

            @Override
            public void onCancelled(@NonNull @NotNull DatabaseError error) { }
        });

        return vista;
    }

    private void replaceFragment(Fragment fragment) {
        FragmentManager fragmentManager = getFragmentManager();
        FragmentTransaction fragmentTransaction = fragmentManager.beginTransaction();
        fragmentTransaction.replace(R.id.body_container,fragment);
        fragmentTransaction.commit();
    }
}