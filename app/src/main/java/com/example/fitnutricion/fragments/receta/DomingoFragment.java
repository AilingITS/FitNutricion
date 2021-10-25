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

public class DomingoFragment extends Fragment {

    private static final String ARG_PARAM1 = "param1";
    private static final String ARG_PARAM2 = "param2";
    private String mParam1;
    private String mParam2;

    private View vista;
    Button btn_back_sabado, btn_next_lunes, btn_cerrar_fragment;

    public DomingoFragment() {
        // Required empty public constructor
    }

    public static DomingoFragment newInstance(String param1, String param2) {
        DomingoFragment fragment = new DomingoFragment();
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
        vista = inflater.inflate(R.layout.fragment_domingo, container, false);

        btn_back_sabado = (Button) vista.findViewById(R.id.btn_back_sabado);
        btn_next_lunes = (Button) vista.findViewById(R.id.btn_next_lunes);
        btn_cerrar_fragment = (Button) vista.findViewById(R.id.btn_cerrar_fragment);

        btn_back_sabado.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                replaceFragment(new SabadoFragment());
            }
        });

        btn_next_lunes.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                replaceFragment(new LunesFragment());
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