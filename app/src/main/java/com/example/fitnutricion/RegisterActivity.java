package com.example.fitnutricion;

import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.widget.EditText;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.FirebaseFirestore;

import org.jetbrains.annotations.NotNull;

import java.util.HashMap;
import java.util.Map;

public class RegisterActivity extends AppCompatActivity {

    private FirebaseAuth mAuth;
    private String userID;
    private FirebaseFirestore db;

    private EditText txtUser, txtMail, txtPassword, txtConfPassword;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_register);

        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        mAuth = FirebaseAuth.getInstance();
        db = FirebaseFirestore.getInstance();

        txtUser = findViewById(R.id.usuario_etxt);
        txtMail = findViewById(R.id.correo_etxt);
        txtPassword = findViewById(R.id.password_etxt);
        txtConfPassword = findViewById(R.id.confPassword_etxt);

    }

    public void onClick(View v){
        switch (v.getId()){
            case R.id.btnConfirmar:
                //Intent intent = new Intent (RegisterActivity.this, com.example.fitnutricion.LoginActivity.class);
                //startActivity(intent);
                //Toast.makeText(this , "Cuenta creada correctamente.", Toast.LENGTH_SHORT).show();
                createuser();
                break;
        }
    }

    public void createuser(){
        String name = txtUser.getText().toString();
        String mail = txtMail.getText().toString();
        String password = txtPassword.getText().toString();
        String confpassword = txtConfPassword.getText().toString();

        if(TextUtils.isEmpty(name)){
            txtUser.setError("Ingrese un nombre de usuario");
            txtUser.requestFocus();
        } else if (TextUtils.isEmpty(mail)){
            txtMail.setError("Ingrese un correo");
            txtMail.requestFocus();
        } else if(TextUtils.isEmpty(password)){
            txtPassword.setError("Ingrese una contraseña");
            txtPassword.requestFocus();
        } else if(TextUtils.isEmpty(confpassword)){
            txtConfPassword.setError("Ingrese la confrimación de su contraseña");
            txtConfPassword.requestFocus();
        } else if(!password.equals(confpassword)){
            txtConfPassword.setError("Las contraseñas no coinciden");
            txtPassword.requestFocus();
        } else {
            mAuth.createUserWithEmailAndPassword(mail, password).addOnCompleteListener(new OnCompleteListener<AuthResult>() {
                @Override
                public void onComplete(@NonNull @NotNull Task<AuthResult> task) {
                    if(task.isSuccessful()){
                        userID = mAuth.getCurrentUser().getUid();
                        DocumentReference documentReference = db.collection("users").document(userID);

                        Map<String, Object> user = new HashMap<>();
                        user.put("Nombre", name);
                        user.put("Correo", mail);
                        user.put("Contraseña", password);

                        documentReference.set(user).addOnSuccessListener(new OnSuccessListener<Void>() {
                            @Override
                            public void onSuccess(Void unused) {
                                Log.d("TAG", "onSuccess: Datos registrados " + userID);
                            }
                        });
                        Toast.makeText(RegisterActivity.this, "Usuario registrado", Toast.LENGTH_SHORT).show();
                        startActivity(new Intent(RegisterActivity.this, LoginActivity.class));
                    } else {
                        Toast.makeText(RegisterActivity.this, "Usuario no registrado" + task.getException().getMessage(), Toast.LENGTH_SHORT).show();
                    }
                }
            });
        }
    }
}