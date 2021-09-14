package com.example.fitnutricion;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

public class LoginActivity extends AppCompatActivity {

    //Button btniniciarSesion, btnRegistrarse;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);

    }

    public void onClick(View v){
        switch (v.getId()){
            case R.id.btniniciarSesion:
                Intent intent = new Intent (LoginActivity.this, MainActivity.class);
                startActivity(intent);
                Toast.makeText(this , "Verificando datos", Toast.LENGTH_SHORT).show();
                break;
            case R.id.btnRegistrarse:
                Intent intent2 = new Intent (LoginActivity.this, RegisterActivity.class);
                startActivity(intent2);
                Toast.makeText(this , "Cargando...", Toast.LENGTH_SHORT).show();
                break;
        }
    }
}