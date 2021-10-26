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
import androidx.appcompat.app.AppCompatDelegate;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.FirebaseFirestore;

import org.jetbrains.annotations.NotNull;

import java.util.HashMap;
import java.util.Map;

public class RegisterActivity extends AppCompatActivity{

    private FirebaseAuth mAuth;
    private String userID;
    private FirebaseDatabase db;

    private EditText txtUser, txtMail, txtPassword, txtConfPassword;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        // checar condicion night mode en settings
        if(AppCompatDelegate.getDefaultNightMode() == AppCompatDelegate.MODE_NIGHT_YES){
            setTheme(R.style.ThemeDark_FitNutricion);
        } else {
            setTheme(R.style.ThemeLight_FitNutricion);
        }

        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_register);

        mAuth = FirebaseAuth.getInstance();
        db = FirebaseDatabase.getInstance();

        txtUser = findViewById(R.id.usuario_etxt);
        txtMail = findViewById(R.id.correo_etxt);
        txtPassword = findViewById(R.id.password_etxt);
        txtConfPassword = findViewById(R.id.confPassword_etxt);
    }

    public void onClick(View v){
        switch (v.getId()){
            case R.id.btnConfirmar:
                createuser();
                break;
        }
    }


    public void createuser(){
        //Obtenemos los datos que ingreso el usuario
        String name = txtUser.getText().toString();
        String mail = txtMail.getText().toString();
        String password = txtPassword.getText().toString();
        String confpassword = txtConfPassword.getText().toString();

        //Condiciones para verificar que los datos esten correctos
        if(TextUtils.isEmpty(name)){
            txtUser.setError(getString(R.string.stringIngreseUsuario));
            txtUser.requestFocus();
        } else if (TextUtils.isEmpty(mail)){
            txtMail.setError(getString(R.string.stringIngreseCorreo));
            txtMail.requestFocus();
        } else if(TextUtils.isEmpty(password)){
            txtPassword.setError(getString(R.string.stringIngreseContraseña));
            txtPassword.requestFocus();
        } else if(TextUtils.isEmpty(confpassword)){
            txtConfPassword.setError(getString(R.string.stringIngreseConfirmacion));
            txtConfPassword.requestFocus();
        } else if(!password.equals(confpassword)){
            txtConfPassword.setError(getString(R.string.stringContraseñasNoCoinciden));
            txtPassword.requestFocus();
        } else if (password.length() <= 5) {
            txtPassword.setError(getString(R.string.stringContraseñaCaracteres));
            txtPassword.requestFocus();
        } else {
            mAuth.createUserWithEmailAndPassword(mail, password).addOnCompleteListener(new OnCompleteListener<AuthResult>() {
                @Override
                public void onComplete(@NonNull @NotNull Task<AuthResult> task) {
                    if(task.isSuccessful()){
                        userID = mAuth.getCurrentUser().getUid();
                        DatabaseReference dbRef = FirebaseDatabase.getInstance().getReference("users").child(userID);
                        //Map para registrar a un usuario con sus datos
                        Map<String, Object> user = new HashMap<>();
                        user.put("Nombre", name);
                        user.put("Correo", mail);
                        user.put("Contraseña", password);

                        dbRef.setValue(user).addOnSuccessListener(new OnSuccessListener<Void>() {
                            @Override
                            public void onSuccess(Void unused) {
                                Log.d("TAG", getString(R.string.stringDatosRegistrados) + userID);
                            }
                        });
                        Toast.makeText(RegisterActivity.this, R.string.stringUsuarioRegistrado, Toast.LENGTH_SHORT).show();
                        startActivity(new Intent(RegisterActivity.this, LoginActivity.class));
                    } else {
                        Toast.makeText(RegisterActivity.this, getString(R.string.stringUsuarioNoRegistrado) + task.getException().getMessage(), Toast.LENGTH_SHORT).show();
                    }
                }
            });
        }
    }
}