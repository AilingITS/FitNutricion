package com.example.fitnutricion;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.app.AppCompatDelegate;

import android.content.Intent;
import android.os.Bundle;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;

public class IndexActivity extends AppCompatActivity {

    private FirebaseAuth mAuth;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        // checar condicion night mode en settings
        if(AppCompatDelegate.getDefaultNightMode() == AppCompatDelegate.MODE_NIGHT_YES){
            setTheme(R.style.ThemeDark_FitNutricion);
        } else {
            setTheme(R.style.ThemeLight_FitNutricion);
        }

        super.onCreate(savedInstanceState);
        mAuth = FirebaseAuth.getInstance();
    }

    protected void onStart() {
        super.onStart();

        //Checa si existe un usario, en dado caso que no te lleva a la interfaz de registro
        FirebaseUser user = mAuth.getCurrentUser();
        if(user == null){
            startActivity(new Intent(IndexActivity.this, LoginActivity.class));
        } else {
            startActivity(new Intent(IndexActivity.this, MainActivity.class));
        }
    }
}