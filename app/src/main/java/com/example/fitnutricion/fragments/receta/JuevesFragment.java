package com.example.fitnutricion.fragments.receta;

import android.os.Bundle;

import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentTransaction;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;

import com.example.fitnutricion.R;
import com.example.fitnutricion.fragments.HomeFragment;

public class JuevesFragment extends Fragment {

    private static final String ARG_PARAM1 = "param1";
    private static final String ARG_PARAM2 = "param2";
    private String mParam1;
    private String mParam2;

    private View vista;
    Button btn_back_miercoles, btn_next_viernes, btn_cerrar_fragment;

    public JuevesFragment() {
        // Required empty public constructor
    }

    public static JuevesFragment newInstance(String param1, String param2) {
        JuevesFragment fragment = new JuevesFragment();
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
        vista = inflater.inflate(R.layout.fragment_jueves, container, false);

        btn_back_miercoles = (Button) vista.findViewById(R.id.btn_back_miercoles);
        btn_next_viernes = (Button) vista.findViewById(R.id.btn_next_viernes);
        btn_cerrar_fragment = (Button) vista.findViewById(R.id.btn_cerrar_fragment);

        btn_back_miercoles.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                replaceFragment(new MiercolesFragment());
            }
        });

        btn_next_viernes.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                replaceFragment(new ViernesFragment());
            }
        });

        btn_cerrar_fragment.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                replaceFragment(new HomeFragment());
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
}