package com.example.fitnutricion.fragments;

import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;

import com.example.fitnutricion.LoginActivity;
import com.example.fitnutricion.MainActivity;
import com.example.fitnutricion.R;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import org.jetbrains.annotations.NotNull;

public class ProfileFragment extends Fragment {

    private static final String ARG_PARAM1 = "param1";
    private static final String ARG_PARAM2 = "param2";
    private String mParam1;
    private String mParam2;
    private View vista;
    private FirebaseAuth mAuth;
    private String userID;

    private TextView perfil_usuario, perfil_nombre, perfil_mail, perfil_password, perfil_edad, perfil_celular;
    private Button perfil_actualizar;
    private FirebaseDatabase firebaseDatabase;
    private DatabaseReference dbRef;

    public ProfileFragment() {
        // Required empty public constructor
    }

    public static ProfileFragment newInstance(String param1, String param2) {
        ProfileFragment fragment = new ProfileFragment();
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
        vista = inflater.inflate(R.layout.fragment_profile, container, false);

        firebaseDatabase = FirebaseDatabase.getInstance();
        mAuth = FirebaseAuth.getInstance();
        dbRef = firebaseDatabase.getReference();
        perfil_usuario = vista.findViewById(R.id.perfil_usuario);
        //perfil_nombre = vista.findViewById(R.id.perfil_nombre);
        perfil_mail = vista.findViewById(R.id.perfil_mail);
        //perfil_password = vista.findViewById(R.id.perfil_password);
        //perfil_edad = vista.findViewById(R.id.perfil_edad);
        //perfil_celular = vista.findViewById(R.id.perfil_celular);



        dbRef.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull @NotNull DataSnapshot snapshot) {
                if(snapshot.exists()){
                    userID = mAuth.getCurrentUser().getUid();
                    String usuario = snapshot.child("users").child(userID).child("Nombre").getValue().toString();
                    String mail = snapshot.child("users").child(userID).child("Correo").getValue().toString();
                    perfil_usuario.setText(usuario);
                    perfil_mail.setText(mail);

                }
            }

            @Override
            public void onCancelled(@NonNull @NotNull DatabaseError error) {

            }
        });

        perfil_actualizar = (Button) vista.findViewById(R.id.perfil_actualizar);
        perfil_actualizar.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

            }
        });


        return vista;
    }
}