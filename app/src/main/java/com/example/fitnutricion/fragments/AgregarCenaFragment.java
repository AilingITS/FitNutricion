package com.example.fitnutricion.fragments;

import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatDelegate;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentTransaction;

import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import com.example.fitnutricion.R;
import com.example.fitnutricion.fragments.comidasRecetas.ComidaFragment;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

import org.jetbrains.annotations.NotNull;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.HashMap;
import java.util.Map;

public class AgregarCenaFragment extends Fragment {

    private static final String ARG_PARAM1 = "param1";
    private static final String ARG_PARAM2 = "param2";
    private String mParam1;
    private String mParam2;

    private View vista;
    private String userID;
    private FirebaseAuth mAuth;
    Button btn_añadirCena;
    private EditText cena_nombreComida, cena_ingredientes;
    private String comidaID, saveCurrentDate, saveCurrentTime;
    private DatabaseReference dbRef;

    public AgregarCenaFragment() {
        // Required empty public constructor
    }

    public static AgregarCenaFragment newInstance(String param1, String param2) {
        AgregarCenaFragment fragment = new AgregarCenaFragment();
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
        vista = inflater.inflate(R.layout.fragment_agregar_cena, container, false);

        mAuth = FirebaseAuth.getInstance();
        userID = mAuth.getCurrentUser().getUid();
        dbRef = FirebaseDatabase.getInstance().getReference().child("users").child(userID).child("cenas");

        cena_nombreComida = vista.findViewById(R.id.cena_nombreComida);
        cena_ingredientes = vista.findViewById(R.id.cena_ingredientes);

        btn_añadirCena = (Button) vista.findViewById(R.id.btn_añadirCena);
        btn_añadirCena.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                createComida();
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

    public void createComida(){
        //Obtenemos los datos que ingreso el usuario
        String nombre = cena_nombreComida.getText().toString();
        String ingredientes = cena_ingredientes.getText().toString();

        //Condiciones para verificar que los datos esten correctos
        if (TextUtils.isEmpty(nombre)){
            cena_nombreComida.setError("Ingrese el nombre de la comida");
            cena_nombreComida.requestFocus();
        } else if(TextUtils.isEmpty(ingredientes)){
            cena_ingredientes.setError("Ingrese los ingredientes de la comida");
            cena_ingredientes.requestFocus();
        } else {
            //Map para registrar a un usuario con sus datos
            Map<String, Object> comida = new HashMap<>();
            comida.put("f_tipo", "Cena");
            comida.put("f_nombrecomida", nombre);
            comida.put("f_ingredientes", ingredientes);

            Calendar calendar = Calendar.getInstance();

            //SimpleDateFormat currentDate = new SimpleDateFormat(" dd MM, yyyy");
            SimpleDateFormat currentDate = new SimpleDateFormat("MM dd, yyyy");
            saveCurrentDate = currentDate.format(calendar.getTime());

            SimpleDateFormat currentTime = new SimpleDateFormat("HH:mm:ss");
            saveCurrentTime = currentTime.format(calendar.getTime());

            comidaID = saveCurrentDate + saveCurrentTime;

            dbRef.child(comidaID).updateChildren(comida).addOnCompleteListener(new OnCompleteListener<Void>() {
                @Override
                public void onComplete(@NonNull @NotNull Task<Void> task) {
                    if(task.isSuccessful()){
                        Toast.makeText(getActivity(), "Comida agregada correctamente", Toast.LENGTH_SHORT).show();
                        replaceFragment(new ComidaFragment());
                    } else {
                        String message = task.getException().toString();
                        Toast.makeText(getActivity(), "Error: " + message, Toast.LENGTH_SHORT).show();
                    }
                }
            });
        }
    }
}