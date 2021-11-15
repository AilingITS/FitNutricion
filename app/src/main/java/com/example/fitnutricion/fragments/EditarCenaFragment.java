package com.example.fitnutricion.fragments;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentTransaction;

import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.Toast;

import com.example.fitnutricion.R;
import com.example.fitnutricion.fragments.comidasRecetas.CenaFragment;
import com.example.fitnutricion.fragments.comidasRecetas.DesayunoFragment;
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

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.HashMap;
import java.util.Map;

public class EditarCenaFragment extends Fragment {
    private static final String ARG_PARAM1 = "param1";
    private static final String ARG_PARAM2 = "param2";
    private String mParam1;
    private String mParam2;

    private View vista;
    private String userID;
    private FirebaseAuth mAuth;
    private DatabaseReference dbRef;
    private StorageReference ImagesRef;

    Button btn_editarDesayuno;
    private EditText editar_paciente_nombre, editar_Ingredientes, editar_calorias;
    private String cenaID;

    private ImageView btn_editar_img;
    private static final int GalleryPick = 1;
    private static final int RESULT_OK = -1;
    private Uri ImageUri;
    private String downloadImageUrl;

    public EditarCenaFragment(String cenaID) {
        this.cenaID = cenaID;
    }

    public EditarCenaFragment() {
        // Required empty public constructor
    }

    public static EditarCenaFragment newInstance(String param1, String param2) {
        EditarCenaFragment fragment = new EditarCenaFragment();
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
        vista = inflater.inflate(R.layout.fragment_editar_cena, container, false);

        mAuth = FirebaseAuth.getInstance();
        userID = mAuth.getCurrentUser().getUid();
        dbRef = FirebaseDatabase.getInstance().getReference("users").child(userID).child("cenas");
        ImagesRef = FirebaseStorage.getInstance().getReference().child("cenas");

        editar_paciente_nombre = vista.findViewById(R.id.editar_paciente_nombre);
        editar_Ingredientes = vista.findViewById(R.id.editar_Ingredientes);
        editar_calorias = vista.findViewById(R.id.editar_calorias);
        btn_editar_img = vista.findViewById(R.id.btn_editar_img);

        dbRef.child(cenaID).addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull @NotNull DataSnapshot snapshot) {
                if (snapshot.exists()) {

                    if (snapshot.child("f_image").exists()) {
                        String image = snapshot.child("f_image").getValue().toString();
                        Picasso.get().load(image).into(btn_editar_img);
                    }

                    String nombre = snapshot.child("f_nombrecomida").getValue().toString();
                    String ingredientes = snapshot.child("f_ingredientes").getValue().toString();
                    String calorias = snapshot.child("f_calorias").getValue().toString();

                    editar_paciente_nombre.setText(nombre);
                    editar_Ingredientes.setText(ingredientes);
                    editar_calorias.setText(calorias);

                }
            }

            @Override
            public void onCancelled(@NonNull @NotNull DatabaseError error) {
            }
        });

        btn_editar_img.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                OpenGallery();
            }
        });


        btn_editarDesayuno = (Button) vista.findViewById(R.id.btn_editarDesayuno);
        btn_editarDesayuno.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                ValidateProductData();
            }
        });

        return vista;
    }

    private void replaceFragment(Fragment fragment) {
        FragmentManager fragmentManager = getFragmentManager();
        FragmentTransaction fragmentTransaction = fragmentManager.beginTransaction();
        fragmentTransaction.replace(R.id.body_container, fragment);
        fragmentTransaction.commit();
    }

    //Función cuando el usuario da clic en el boton actualizar datos
    private void ValidateProductData() {
        String nombre = editar_paciente_nombre.getText().toString();
        String ingredientes = editar_Ingredientes.getText().toString();
        String calorias = editar_calorias.getText().toString();

        if (ImageUri == null) { //En caso que el usuario modifico datos pero no su imagen se llama a la sig función solo para actualizar datos
            SaveInfoToDatabasewithoutImage();
        } else if (TextUtils.isEmpty(nombre)) {
            editar_paciente_nombre.setError("Ingrese el nombre de la comida");
            editar_paciente_nombre.requestFocus();
        } else if (TextUtils.isEmpty(ingredientes)) {
            editar_Ingredientes.setError("Ingrese los ingredientes");
            editar_Ingredientes.requestFocus();
        } else if (TextUtils.isEmpty(calorias)) {
            editar_calorias.setError("Ingrese las calorías");
            editar_calorias.requestFocus();
        } else { //Si el usuario si agrego una imagen de perfil entra en este else
            userID = mAuth.getCurrentUser().getUid();

            Calendar calendar = Calendar.getInstance();
            //SimpleDateFormat currentDate = new SimpleDateFormat(" dd MM, yyyy");
            SimpleDateFormat currentDate = new SimpleDateFormat("MM dd, yyyy");
            String saveCurrentDate = currentDate.format(calendar.getTime());
            SimpleDateFormat currentTime = new SimpleDateFormat("HH:mm:ss");
            String saveCurrentTime = currentTime.format(calendar.getTime());
            String imgID = saveCurrentDate + saveCurrentTime;

            StorageReference fileRef = ImagesRef.child(userID).child(imgID + ".jpg");
            final UploadTask uploadTask = fileRef.putFile(ImageUri);

            uploadTask.addOnFailureListener(new OnFailureListener() {
                @Override
                public void onFailure(@NonNull @NotNull Exception e) {
                    String message = e.toString();
                    Toast.makeText(getActivity(), getString(R.string.stringError) + message, Toast.LENGTH_SHORT).show();
                }
            }).addOnSuccessListener(new OnSuccessListener<UploadTask.TaskSnapshot>() {
                @Override
                public void onSuccess(UploadTask.TaskSnapshot taskSnapshot) {
                    Task<Uri> urlTask = uploadTask.continueWithTask(new Continuation<UploadTask.TaskSnapshot, Task<Uri>>() {
                        @Override
                        public Task<Uri> then(@NonNull @NotNull Task<UploadTask.TaskSnapshot> task) throws Exception {
                            if (!task.isSuccessful()) {
                                throw task.getException();
                            }
                            downloadImageUrl = fileRef.getDownloadUrl().toString();
                            return fileRef.getDownloadUrl();
                        }
                    }).addOnCompleteListener(new OnCompleteListener<Uri>() {
                        @Override
                        public void onComplete(@NonNull @NotNull Task<Uri> task) {
                            if (task.isSuccessful()) {
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
    private void SaveInfoToDatabasewithoutImage () {
        HashMap<String, Object> infoMap = new HashMap<>();
        String nombre = editar_paciente_nombre.getText().toString();
        String ingredientes = editar_Ingredientes.getText().toString();
        String calorias = editar_calorias.getText().toString();
        infoMap.put("f_nombrecomida", nombre);
        infoMap.put("f_ingredientes", ingredientes);
        infoMap.put("f_calorias", calorias);

        dbRef.child(cenaID).updateChildren(infoMap).addOnCompleteListener(new OnCompleteListener<Void>() {
            @Override
            public void onComplete(@NonNull @NotNull Task<Void> task) {
                if (task.isSuccessful()) {
                    Toast.makeText(getActivity(), R.string.stringCambiosGuardadosCorrectamente, Toast.LENGTH_SHORT).show();
                    replaceFragment(new CenaFragment());
                } else {
                    String message = task.getException().toString();
                    Toast.makeText(getActivity(), R.string.stringError + message, Toast.LENGTH_SHORT).show();
                }
            }
        });
    }

    public void SaveInfoToDatabase () {
        //Obtenemos los datos que ingreso el usuario

        String nombre = editar_paciente_nombre.getText().toString();
        String ingredientes = editar_Ingredientes.getText().toString();
        String calorias = editar_calorias.getText().toString();

        //Condiciones para verificar que los datos esten correctos
        if (TextUtils.isEmpty(nombre)) {
            editar_paciente_nombre.setError("Ingrese el nombre de la comida");
            editar_paciente_nombre.requestFocus();
        } else if (TextUtils.isEmpty(ingredientes)) {
            editar_Ingredientes.setError("Ingrese los ingredientes");
            editar_Ingredientes.requestFocus();
        } else if (TextUtils.isEmpty(calorias)) {
            editar_calorias.setError("Ingrese las calorías");
            editar_calorias.requestFocus();
        } else {
            //Map para registrar a un usuario con sus datos
            Map<String, Object> desayunoMap = new HashMap<>();
            desayunoMap.put("f_nombrecomida", nombre);
            desayunoMap.put("f_ingredientes", ingredientes);
            desayunoMap.put("f_calorias", calorias);
            desayunoMap.put("f_image", downloadImageUrl);

            dbRef.child(cenaID).updateChildren(desayunoMap).addOnCompleteListener(new OnCompleteListener<Void>() {
                @Override
                public void onComplete(@NonNull @NotNull Task<Void> task) {
                    if (task.isSuccessful()) {
                        Toast.makeText(getActivity(),R.string.stringCambiosGuardadosCorrectamente, Toast.LENGTH_SHORT).show();
                        replaceFragment(new CenaFragment());
                    } else {
                        String message = task.getException().toString();
                        Toast.makeText(getActivity(), R.string.stringError + message, Toast.LENGTH_SHORT).show();
                    }
                }
            });
        }
    }

    //Función para abrir la galeria cuando da clic en la imagen
    private void OpenGallery () {
        Intent galleryIntent = new Intent();
        galleryIntent.setAction(Intent.ACTION_GET_CONTENT);
        galleryIntent.setType("image/*");
        startActivityForResult(galleryIntent, GalleryPick);
    }

    @Override
    public void onActivityResult ( int requestCode, int resultCode,
                                   @Nullable @org.jetbrains.annotations.Nullable Intent data){
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == GalleryPick && resultCode == RESULT_OK && data != null) {
            ImageUri = data.getData();
            btn_editar_img.setImageURI(ImageUri);
        }
    }
}