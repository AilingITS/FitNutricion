package com.example.fitnutricion.fragments;

import android.content.Intent;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatDelegate;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentTransaction;

import android.text.TextUtils;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import com.example.fitnutricion.LoginActivity;
import com.example.fitnutricion.R;
import com.example.fitnutricion.RegisterActivity;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

import org.jetbrains.annotations.NotNull;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.HashMap;
import java.util.Map;
import java.util.Calendar;

public class AgregarPacienteFragment extends Fragment {

    private static final String ARG_PARAM1 = "param1";
    private static final String ARG_PARAM2 = "param2";

    private String mParam1;
    private String mParam2;

    private View vista;
    private String userID;
    private FirebaseAuth mAuth;
    Button btn_añadirPacientes;
    private EditText paciente_nombreCompleto, paciente_correo, paciente_edad;

    private String pacienteID, saveCurrentDate, saveCurrentTime;
    private DatabaseReference dbRef;

    public AgregarPacienteFragment() {
        // Required empty public constructor
    }

    public static AgregarPacienteFragment newInstance(String param1, String param2) {
        AgregarPacienteFragment fragment = new AgregarPacienteFragment();
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
        vista = inflater.inflate(R.layout.fragment_agregar_paciente, container, false);
        //dbRef = FirebaseDatabase.getInstance().getReference().child("pacientes");
        mAuth = FirebaseAuth.getInstance();
        userID = mAuth.getCurrentUser().getUid();
        //dbRef = FirebaseDatabase.getInstance().getReference("pacientes");
        dbRef = FirebaseDatabase.getInstance().getReference("users").child(userID).child("pacientes");

        paciente_nombreCompleto = vista.findViewById(R.id.paciente_nombreCompleto);
        paciente_correo = vista.findViewById(R.id.paciente_correo);
        paciente_edad = vista.findViewById(R.id.paciente_edad);

        btn_añadirPacientes = (Button) vista.findViewById(R.id.btn_añadirPacientes);
        btn_añadirPacientes.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                createPaciente();
            }
        });

        return vista;
    }

    private void replaceFragment(Fragment fragment) {
        FragmentManager fragmentManager = getFragmentManager();
        FragmentTransaction fragmentTransaction = fragmentManager.beginTransaction();
        fragmentTransaction.replace(R.id.body_container,fragment);
        fragmentTransaction.commit();
    }

    public void createPaciente(){
        //Obtenemos los datos que ingreso el usuario
        String name = paciente_nombreCompleto.getText().toString();
        String mail = paciente_correo.getText().toString();
        String age = paciente_edad.getText().toString();

        //Condiciones para verificar que los datos esten correctos
        if(TextUtils.isEmpty(name)){
            paciente_nombreCompleto.setError("Ingrese un nombre de usuario");
            paciente_nombreCompleto.requestFocus();
        } else if (TextUtils.isEmpty(mail)){
            paciente_correo.setError("Ingrese un correo");
            paciente_correo.requestFocus();
        } else if(TextUtils.isEmpty(age)){
            paciente_edad.setError("Ingrese una edad");
            paciente_edad.requestFocus();
        } else {
            //Map para registrar a un usuario con sus datos
            Map<String, Object> paciente = new HashMap<>();
            paciente.put("p_Nombre", name);
            paciente.put("p_Correo", mail);
            paciente.put("p_Edad", age);

            Calendar calendar = Calendar.getInstance();

            //SimpleDateFormat currentDate = new SimpleDateFormat(" dd MM, yyyy");
            SimpleDateFormat currentDate = new SimpleDateFormat("MM dd, yyyy");
            saveCurrentDate = currentDate.format(calendar.getTime());

            SimpleDateFormat currentTime = new SimpleDateFormat("HH:mm:ss");
            saveCurrentTime = currentTime.format(calendar.getTime());

            pacienteID = saveCurrentDate + saveCurrentTime;

            dbRef.child(pacienteID).updateChildren(paciente).addOnCompleteListener(new OnCompleteListener<Void>() {
                @Override
                public void onComplete(@NonNull @NotNull Task<Void> task) {
                    if(task.isSuccessful()){
                        Toast.makeText(getActivity(), "Paciente agregado correctamente", Toast.LENGTH_SHORT).show();
                        replaceFragment(new PacientesFragment());
                    } else {
                        String message = task.getException().toString();
                        Toast.makeText(getActivity(), "Error: " + message, Toast.LENGTH_SHORT).show();
                    }
                }
            });
        }
    }
}