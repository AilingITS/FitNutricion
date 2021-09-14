package com.example.fitnutricion;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

public class RegisterActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_register);

        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

    }

    public void onClick(View v){
        switch (v.getId()){
            case R.id.btnConfirmar:
                Intent intent = new Intent (RegisterActivity.this, com.example.fitnutricion.LoginActivity.class);
                startActivity(intent);
                Toast.makeText(this , "Cuenta creada correctamente.", Toast.LENGTH_SHORT).show();
                break;
        }
    }
}