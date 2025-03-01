package com.example.fitnutricion.fragments;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatDelegate;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentTransaction;

import com.example.fitnutricion.LoginActivity;
import com.example.fitnutricion.MainActivity;
import com.example.fitnutricion.R;
import com.google.android.gms.tasks.Continuation;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;
import com.squareup.picasso.Picasso;

import org.jetbrains.annotations.NotNull;

import java.util.HashMap;
import java.util.Map;

public class ProfileFragment extends Fragment {

    private static final String ARG_PARAM1 = "param1";
    private static final String ARG_PARAM2 = "param2";
    private String mParam1;
    private String mParam2;

    private View vista;
    private FirebaseAuth mAuth;
    private String userID;

    private ImageView fotoperfil;
    private static final int GalleryPick = 1;
    private static final int RESULT_OK = -1;
    private Uri ImageUri;
    private String downloadImageUrl;

    private EditText perfil_usuario, perfil_mail;
    private Button perfil_actualizar;
    private FirebaseDatabase firebaseDatabase;
    private DatabaseReference dbRef;
    private StorageReference ImagesRef;

    public ProfileFragment() {
        // Required empty public constructor
    }

    public static ProfileFragment newInstance(String param1, String param2) {
        ProfileFragment fragment = new ProfileFragment();
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
        // Declaramos la vista del fragment para retornarlo al final
        vista = inflater.inflate(R.layout.fragment_profile, container, false);

        firebaseDatabase = FirebaseDatabase.getInstance();
        mAuth = FirebaseAuth.getInstance();
        dbRef = firebaseDatabase.getReference();
        ImagesRef = FirebaseStorage.getInstance().getReference().child("images");

        perfil_usuario = vista.findViewById(R.id.perfil_usuario);
        fotoperfil = vista.findViewById(R.id.fotoperfil);
        perfil_mail = vista.findViewById(R.id.perfil_mail);

        //Al momento de cargar el fragment perfil verifica si ya existen datos del usuario para cargarlos
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
                    String mail = snapshot.child("users").child(userID).child("Correo").getValue().toString();
                    perfil_usuario.setText(usuario);
                    perfil_mail.setText(mail);
                }
            }
            @Override
            public void onCancelled(@NonNull @NotNull DatabaseError error) {
            }
        });

        perfil_actualizar = (Button) vista.findViewById(R.id.perfil_actualizar);
        perfil_actualizar.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                ValidateProductData();
            }
        });

        fotoperfil.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                OpenGallery();
            }
        });

        return vista;
    }

    //Función que sirve para cambiar de fragmento en fragmento
    private void replaceFragment(Fragment fragment) {
        FragmentManager fragmentManager = getFragmentManager();
        FragmentTransaction fragmentTransaction = fragmentManager.beginTransaction();
        fragmentTransaction.replace(R.id.body_container,fragment);
        fragmentTransaction.commit();
    }

    //Función cuando el usuario da clic en el boton actualizar datos
    private void ValidateProductData() {
        String usuario = perfil_usuario.getText().toString();
        String mail = perfil_mail.getText().toString();

        if(ImageUri == null){ //En caso que el usuario modifico datos pero no su imagen se llama a la sig función solo para actualizar datos
            SaveInfoToDatabasewithoutImage();
        } else if (TextUtils.isEmpty(usuario)){
            perfil_usuario.setError("Ingrese un nombre de usuario");
            perfil_usuario.requestFocus();
        } else if (TextUtils.isEmpty(mail)) {
            perfil_mail.setError("Ingrese un correo electronico");
            perfil_mail.requestFocus();
        }else if(!mail.contains("@")){
            perfil_mail.setError("Ingrese un correo electronico valido");
            perfil_mail.requestFocus();
        } else if(!mail.contains(".com")){
            perfil_mail.setError("Ingrese un correo electronico valido");
            perfil_mail.requestFocus();
        }else { //Si el usuario si agrego una imagen de perfil entra en este else
            userID = mAuth.getCurrentUser().getUid();
            StorageReference fileRef = ImagesRef.child(userID + ".jpg");
            final UploadTask uploadTask = fileRef.putFile(ImageUri);

            uploadTask.addOnFailureListener(new OnFailureListener() {
                @Override
                public void onFailure(@NonNull @NotNull Exception e) {
                    String message = e.toString();
                    Toast.makeText(getActivity(), R.string.stringError + message, Toast.LENGTH_SHORT).show();
                }
            }).addOnSuccessListener(new OnSuccessListener<UploadTask.TaskSnapshot>() {
                @Override
                public void onSuccess(UploadTask.TaskSnapshot taskSnapshot) {
                    Task<Uri> urlTask = uploadTask.continueWithTask(new Continuation<UploadTask.TaskSnapshot, Task<Uri>>() {
                        @Override
                        public Task<Uri> then(@NonNull @NotNull Task<UploadTask.TaskSnapshot> task) throws Exception {
                            if (!task.isSuccessful()){
                                throw task.getException();
                            }
                            downloadImageUrl = fileRef.getDownloadUrl().toString();
                            return fileRef.getDownloadUrl();
                        }
                    }).addOnCompleteListener(new OnCompleteListener<Uri>() {
                        @Override
                        public void onComplete(@NonNull @NotNull Task<Uri> task) {
                            if(task.isSuccessful()){
                                downloadImageUrl = task.getResult().toString();
                                SaveInfoToDatabase(); //Función para actualizar datos e imagen de perfil
                            }
                        }
                    });
                }
            });
        }
    }

    // Actualiza los datos menos la foto de perfil
    private void SaveInfoToDatabasewithoutImage() {
        HashMap<String, Object> infoMap = new HashMap<>();
        String usuario = perfil_usuario.getText().toString();
        String mail = perfil_mail.getText().toString();
        infoMap.put("Nombre", usuario);
        infoMap.put("Correo", mail);

        if (TextUtils.isEmpty(usuario)){
            perfil_usuario.setError("Ingrese un nombre de usuario");
            perfil_usuario.requestFocus();
        } else if (TextUtils.isEmpty(mail)){
            perfil_mail.setError("Ingrese un correo electronico");
            perfil_mail.requestFocus();
        }else if(!mail.contains("@")){
            perfil_mail.setError("Ingrese un correo electronico valido");
            perfil_mail.requestFocus();
        } else if(!mail.contains(".com")){
            perfil_mail.setError("Ingrese un correo electronico valido");
            perfil_mail.requestFocus();
        }else {

            userID = mAuth.getCurrentUser().getUid();
            dbRef.child("users").child(userID).updateChildren(infoMap).addOnCompleteListener(new OnCompleteListener<Void>() {
                @Override
                public void onComplete(@NonNull @NotNull Task<Void> task) {
                    if (task.isSuccessful()) {
                        Toast.makeText(getActivity(), R.string.stringCambiosGuardadosCorrectamente, Toast.LENGTH_SHORT).show();
                        replaceFragment(new SettingsFragment());
                    } else {
                        String message = task.getException().toString();
                        Toast.makeText(getActivity(), R.string.stringError + message, Toast.LENGTH_SHORT).show();
                    }
                }
            });
        }
    }

    //Guardar información de perfil con imagen de perfil
    private void SaveInfoToDatabase() {
        HashMap<String, Object> infoMap = new HashMap<>();
        String usuario = perfil_usuario.getText().toString();
        String mail = perfil_mail.getText().toString();
        infoMap.put("Nombre", usuario);
        infoMap.put("Correo", mail);
        infoMap.put("image", downloadImageUrl);

        if (TextUtils.isEmpty(usuario)){
            perfil_usuario.setError("Ingrese un nombre de usuario");
            perfil_usuario.requestFocus();
        } else if (TextUtils.isEmpty(mail)){
            perfil_mail.setError("Ingrese un correo electronico");
            perfil_mail.requestFocus();
        }else if(!mail.contains("@")){
            perfil_mail.setError("Ingrese un correo electronico valido");
            perfil_mail.requestFocus();
        } else if(!mail.contains(".com")){
        perfil_mail.setError("Ingrese un correo electronico valido");
        perfil_mail.requestFocus();
    }else {

            userID = mAuth.getCurrentUser().getUid();
            dbRef.child("users").child(userID).updateChildren(infoMap).addOnCompleteListener(new OnCompleteListener<Void>() {
                @Override
                public void onComplete(@NonNull @NotNull Task<Void> task) {
                    if (task.isSuccessful()) {
                        Toast.makeText(getActivity(), R.string.stringCambiosGuardadosCorrectamente, Toast.LENGTH_SHORT).show();
                        replaceFragment(new SettingsFragment());
                    } else {
                        String message = task.getException().toString();
                        Toast.makeText(getActivity(), R.string.stringError + message, Toast.LENGTH_SHORT).show();
                    }
                }
            });
        }
    }

    //Función para abrir la galeria cuando da clic en la imagen
    private void OpenGallery() {
        Intent galleryIntent = new Intent();
        galleryIntent.setAction(Intent.ACTION_GET_CONTENT);
        galleryIntent.setType("image/*");
        startActivityForResult(galleryIntent, GalleryPick);
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, @Nullable @org.jetbrains.annotations.Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (requestCode==GalleryPick && resultCode==RESULT_OK && data!=null){
            ImageUri = data.getData();
            fotoperfil.setImageURI(ImageUri);
        }
    }
}