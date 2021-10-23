package com.example.fitnutricion.fragments;

import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentActivity;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentTransaction;

import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.Toast;

import com.example.fitnutricion.R;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.squareup.picasso.Picasso;

import org.jetbrains.annotations.NotNull;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.HashMap;
import java.util.Map;

public class EditarPacienteFragment extends Fragment {

    private static final String ARG_PARAM1 = "param1";
    private static final String ARG_PARAM2 = "param2";

    private String mParam1;
    private String mParam2;

    private View vista;
    private String userID;
    private FirebaseAuth mAuth;
    private DatabaseReference dbRef;

    Button editar_btn_editarPacientes;
    private EditText editar_paciente_nombreCompleto, editar_paciente_correo, editar_paciente_edad;
    Spinner editar_sexoSpinner;
    private String pacienteID;

     public EditarPacienteFragment(String pacienteID){
        this.pacienteID = pacienteID;
     }

    public EditarPacienteFragment() {
        // Required empty public constructor
    }

    public static EditarPacienteFragment newInstance(String param1, String param2) {
        EditarPacienteFragment fragment = new EditarPacienteFragment();
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
        vista = inflater.inflate(R.layout.fragment_editar_paciente, container, false);

        mAuth = FirebaseAuth.getInstance();
        userID = mAuth.getCurrentUser().getUid();
        dbRef = FirebaseDatabase.getInstance().getReference("users").child(userID).child("pacientes");

        editar_paciente_nombreCompleto = vista.findViewById(R.id.editar_paciente_nombreCompleto);
        editar_paciente_correo = vista.findViewById(R.id.editar_paciente_correo);
        editar_paciente_edad = vista.findViewById(R.id.editar_paciente_edad);

        editar_sexoSpinner = (Spinner) vista.findViewById(R.id.editar_sexoSpinner);
        ArrayAdapter<CharSequence> adapter = ArrayAdapter.createFromResource(getContext(),
                R.array.ag_sexoArray, android.R.layout.simple_spinner_item);
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        editar_sexoSpinner.setAdapter(adapter);

        dbRef.child(pacienteID).addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull @NotNull DataSnapshot snapshot) {
                if(snapshot.exists()){

                    String usuario = snapshot.child("p_Nombre").getValue().toString();
                    String correo = snapshot.child("p_Correo").getValue().toString();
                    String edad = snapshot.child("p_Edad").getValue().toString();
                    String sexo = snapshot.child("p_Sexo").getValue().toString();

                    editar_paciente_nombreCompleto.setText(usuario);
                    editar_paciente_correo.setText(correo);
                    editar_paciente_edad.setText(edad);

                    editar_sexoSpinner.setSelection(obtenerPosicionItem(editar_sexoSpinner, sexo));
                }
            }
            @Override
            public void onCancelled(@NonNull @NotNull DatabaseError error) {
            }
        });

        editar_btn_editarPacientes = (Button) vista.findViewById(R.id.editar_btn_editarPacientes);
        editar_btn_editarPacientes.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                editPaciente();
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

    public void editPaciente(){
        //Obtenemos los datos que ingreso el usuario

        String name = editar_paciente_nombreCompleto.getText().toString();
        String mail = editar_paciente_correo.getText().toString();
        String age = editar_paciente_edad.getText().toString();
        String sexo = editar_sexoSpinner.getSelectedItem().toString();

        //Condiciones para verificar que los datos esten correctos
        if(TextUtils.isEmpty(name)){
            editar_paciente_nombreCompleto.setError("Ingrese un nombre de usuario");
            editar_paciente_nombreCompleto.requestFocus();
        } else if (TextUtils.isEmpty(mail)){
            editar_paciente_correo.setError("Ingrese un correo");
            editar_paciente_correo.requestFocus();
        } else if(TextUtils.isEmpty(age)){
            editar_paciente_edad.setError("Ingrese una edad");
            editar_paciente_edad.requestFocus();
        } else {
            //Map para registrar a un usuario con sus datos
            Map<String, Object> pacienteMap = new HashMap<>();
            pacienteMap.put("p_Nombre", name);
            pacienteMap.put("p_Correo", mail);
            pacienteMap.put("p_Edad", age);
            pacienteMap.put("p_Sexo", sexo);

            dbRef.child(pacienteID).updateChildren(pacienteMap).addOnCompleteListener(new OnCompleteListener<Void>() {
                @Override
                public void onComplete(@NonNull @NotNull Task<Void> task) {
                    if(task.isSuccessful()){
                        Toast.makeText(getActivity(), "Cambios guardados correctamente", Toast.LENGTH_SHORT).show();
                        replaceFragment(new PacientesFragment());
                    } else {
                        String message = task.getException().toString();
                        Toast.makeText(getActivity(), "Error: " + message, Toast.LENGTH_SHORT).show();
                    }
                }
            });
        }
    }

    public static int obtenerPosicionItem(Spinner spinner, String sexo) {
        //Creamos la variable posicion y lo inicializamos en 0
        int posicion = 0;
        //Recorre el spinner en busca del ítem que coincida con el parametro `String sexo`
        //que lo pasaremos posteriormente
        for (int i = 0; i < spinner.getCount(); i++) {
            //Almacena la posición del ítem que coincida con la búsqueda
            if (spinner.getItemAtPosition(i).toString().equalsIgnoreCase(sexo)) {
                posicion = i;
            }
        }
        //Devuelve un valor entero (si encontro una coincidencia devuelve la
        // posición 0 o N, de lo contrario devuelve 0 = posición inicial)
        return posicion;
    }
}