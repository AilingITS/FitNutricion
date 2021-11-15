package com.example.fitnutricion.fragments;

import android.app.Activity;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.res.Configuration;
import android.content.res.Resources;
import android.net.Uri;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.CompoundButton;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatDelegate;
import androidx.appcompat.widget.SwitchCompat;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentTransaction;

import com.example.fitnutricion.Language.LocaleHelper;
import com.example.fitnutricion.LoginActivity;
import com.example.fitnutricion.R;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.mahfa.dnswitch.DayNightSwitch;
import com.squareup.picasso.Picasso;

import org.jetbrains.annotations.NotNull;

import java.util.Locale;

public class SettingsFragment extends Fragment{
    private static final String ARG_PARAM1 = "param1";
    private static final String ARG_PARAM2 = "param2";
    private String mParam1;
    private String mParam2;

    View vista;
    TextView settings, perfil_usuario;
    Button btncerrarSesion, btn_modificar_datos, btn_cambiar_idioma;

    private ImageView fotoperfil;
    private static final int GalleryPick = 1;
    private static final int RESULT_OK = -1;
    private Uri ImageUri;
    private String downloadImageUrl;

    private DatabaseReference dbRef;
    private FirebaseDatabase firebaseDatabase;
    private FirebaseAuth mAuth;
    private String userID;

    //SwitchCompat settings_theme_night;
    int lang_selected;

    Context context;
    Resources resources;

    public SettingsFragment() {
        // Required empty public constructor
    }

    public static SettingsFragment newInstance(String param1, String param2) {
        SettingsFragment fragment = new SettingsFragment();
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

        mAuth = FirebaseAuth.getInstance();

        if(LocaleHelper.getLanguage(getActivity()).equalsIgnoreCase("values")){
            context = LocaleHelper.setLocale(getActivity(),"values");
            resources =context.getResources();
            lang_selected = 0;

        } else if(LocaleHelper.getLanguage(getActivity()).equalsIgnoreCase("en")){
            context = LocaleHelper.setLocale(getActivity(),"en");
            resources =context.getResources();
            lang_selected = 1;

        } else if(LocaleHelper.getLanguage(getActivity()).equalsIgnoreCase("de")){
            context = LocaleHelper.setLocale(getActivity(),"de");
            resources =context.getResources();
            lang_selected = 2;

        }
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        vista = inflater.inflate(R.layout.fragment_settings, container, false);

        settings = (TextView) vista.findViewById(R.id.settings);

        firebaseDatabase = FirebaseDatabase.getInstance();
        dbRef = firebaseDatabase.getReference();
        mAuth = FirebaseAuth.getInstance();

        btncerrarSesion = (Button) vista.findViewById(R.id.btncerrarSesion);
        btn_modificar_datos = (Button) vista.findViewById(R.id.btn_modificar_datos);

        perfil_usuario = vista.findViewById(R.id.perfil_usuario);
        fotoperfil = vista.findViewById(R.id.fotoperfil);

        dbRef.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull @NotNull DataSnapshot snapshot) {
                if(snapshot.exists()){
                    userID = mAuth.getCurrentUser().getUid();

                    if(snapshot.child("users").child(userID).child("image").exists()){
                        String image = snapshot.child("users").child(userID).child("image").getValue().toString();
                        Picasso.get().load(image).into(fotoperfil);
                    }

                    String usuario = snapshot.child("users").child(userID).child("Nombre").getValue().toString();
                    perfil_usuario.setText(usuario);
                }
            }
            @Override
            public void onCancelled(@NonNull @NotNull DatabaseError error) {
            }
        });

        //btn_cambiar_idioma = (Button) vista.findViewById(R.id.btn_cambiar_idioma);
        //settings_theme_night = (SwitchCompat) vista.findViewById(R.id.settings_theme_night);

        /*if(loadState() == true){
            settings_theme_night.setChecked(true);
        }*/

        /*settings_theme_night.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                if(isChecked){
                    AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_YES);
                    saveState(true);
                } else {
                    AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_NO);
                    saveState(false);
                }
            }
        });*/

        btncerrarSesion.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                mAuth.signOut();
                Intent intent = new Intent (getContext(), LoginActivity.class);
                startActivity(intent);
                getActivity().finish();
                Toast.makeText(getContext(), R.string.stringCerrandoSesion, Toast.LENGTH_SHORT).show();
            }
        });

        btn_modificar_datos.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                replaceFragment(new ProfileFragment());
            }
        });

        /*btn_cambiar_idioma.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                final String[] Language ={"Español", "English", "German"};

                final AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());

                builder.setTitle("Select a language")
                        .setSingleChoiceItems(Language, lang_selected, new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                // IdLenguaje en el que esta la aplicacion  lenguage_dialog.setText(languege[which]);
                                if(Language[which].equals("Español")){
                                    context = LocaleHelper.setLocale(getActivity(), "values");
                                    resources = context.getResources();
                                    lang_selected = 0;

                                    setString();
                                }

                                if(Language[which].equals("English")){
                                    context = LocaleHelper.setLocale(getActivity(), "en");
                                    resources = context.getResources();
                                    lang_selected = 1;

                                    setString();
                                }

                                if(Language[which].equals("German")){
                                    context = LocaleHelper.setLocale(getActivity(), "de");
                                    resources = context.getResources();
                                    lang_selected = 2;

                                    setString();
                                }
                            }
                        }).setPositiveButton("OK", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        dialog.dismiss();
                    }
                });
                builder.create().show();
            }
        });*/

        return vista;
    }

    // Cambiar de fragment
    private void replaceFragment(Fragment fragment) {
        FragmentManager fragmentManager = getFragmentManager();
        FragmentTransaction fragmentTransaction = fragmentManager.beginTransaction();
        fragmentTransaction.replace(R.id.body_container,fragment);
        fragmentTransaction.commit();
    }

    /*private void saveState(Boolean state){
        SharedPreferences sharedPreferences = getActivity().getSharedPreferences("ABHOPositive", Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = sharedPreferences.edit();
        editor.putBoolean("NightMode", state);
        editor.apply();
    }

    private Boolean loadState(){
        SharedPreferences sharedPreferences = requireContext().getSharedPreferences("ABHOPositive", Context.MODE_PRIVATE);
        Boolean state = sharedPreferences.getBoolean("NightMode", false);
        return state;
    }

    void setString(){
        settings.setText(resources.getString(R.string.setting_text));
    }*/
}