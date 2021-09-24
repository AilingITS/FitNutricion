package com.example.fitnutricion;

import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.FirebaseFirestore;

import org.jetbrains.annotations.NotNull;

public class LoginActivity extends AppCompatActivity {

    private FirebaseAuth mAuth;

    private EditText txtMail, txtPassword;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);

        mAuth = FirebaseAuth.getInstance();

        txtMail = findViewById(R.id.correo_etxt);
        txtPassword = findViewById(R.id.password_etxt);

    }

    public void onClick(View v){
        switch (v.getId()){
            case R.id.btniniciarSesion:
                Toast.makeText(this , "Verificando datos", Toast.LENGTH_SHORT).show();
                //Intent intent = new Intent (LoginActivity.this, MainActivity.class);
                //startActivity(intent);
                userLogin();
                break;
            case R.id.btnRegistrarse:
                Intent intent2 = new Intent (LoginActivity.this, RegisterActivity.class);
                startActivity(intent2);
                Toast.makeText(this , "Cargando...", Toast.LENGTH_SHORT).show();
                break;
        }
    }

    public void userLogin(){
        String mail = txtMail.getText().toString();
        String password = txtPassword.getText().toString();

        if(TextUtils.isEmpty(mail)){
            txtMail.setError("Ingrese un correo");
            txtMail.requestFocus();
        } else if (TextUtils.isEmpty(password)){
            Toast.makeText(this, "Ingrese una contraseña", Toast.LENGTH_SHORT).show();
            txtPassword.requestFocus();
        } else {

            mAuth.signInWithEmailAndPassword(mail, password).addOnCompleteListener(new OnCompleteListener<AuthResult>() {
                @Override
                public void onComplete(@NonNull @NotNull Task<AuthResult> task) {
                    if(task.isSuccessful()){
                        Toast.makeText(LoginActivity.this, "Bienvenid@ a FitNutricion", Toast.LENGTH_SHORT).show();
                        startActivity(new Intent(LoginActivity.this, MainActivity.class));
                        finish();
                    } else {
                        Log.w("TAG", "Error:", task.getException());
                    }
                }
            });
        }
    }
}