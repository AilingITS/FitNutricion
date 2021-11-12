package com.example.fitnutricion.fragments;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatDelegate;
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
import com.example.fitnutricion.fragments.comidasRecetas.ComidaFragment;
import com.example.fitnutricion.fragments.comidasRecetas.DesayunoFragment;
import com.google.android.gms.tasks.Continuation;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;

import org.jetbrains.annotations.NotNull;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.HashMap;
import java.util.Map;

public class AgregarComidaFragment extends Fragment {

    private static final String ARG_PARAM1 = "param1";
    private static final String ARG_PARAM2 = "param2";
    private String mParam1;
    private String mParam2;

    private View vista;
    private String userID;
    private FirebaseAuth mAuth;

    Button btn_añadirComida;
    private EditText comida_nombreComida, comida_ingredientes, comida_calorias;
    private String comidaID, saveCurrentDate, saveCurrentTime;
    private DatabaseReference dbRef;
    private StorageReference ImagesRef;

    private ImageView btn_agregar_comida;
    private static final int GalleryPick = 1;
    private static final int RESULT_OK = -1;
    private Uri ImageUri;
    private String downloadImageUrl;


    public AgregarComidaFragment() {
        // Required empty public constructor
    }

    public static AgregarComidaFragment newInstance(String param1, String param2) {
        AgregarComidaFragment fragment = new AgregarComidaFragment();
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
        // Inflate the layout for this fragment
        vista = inflater.inflate(R.layout.fragment_agregar_comida, container, false);

        mAuth = FirebaseAuth.getInstance();
        userID = mAuth.getCurrentUser().getUid();
        dbRef = FirebaseDatabase.getInstance().getReference().child("users").child(userID).child("comidas");
        ImagesRef = FirebaseStorage.getInstance().getReference().child("comidas");

        comida_nombreComida = vista.findViewById(R.id.comida_nombreComida);
        comida_ingredientes = vista.findViewById(R.id.comida_ingredientes);
        comida_calorias = vista.findViewById(R.id.comida_calorias);
        btn_agregar_comida = vista.findViewById(R.id.btn_agregar_comida);

        btn_añadirComida = (Button) vista.findViewById(R.id.btn_añadirComida);
        btn_añadirComida.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                ValidateProductData();
            }
        });

        btn_agregar_comida.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                OpenGallery();
            }
        });

        return vista;
    }

    private void ValidateProductData() {
        //Obtenemos los datos que ingreso el usuario
        String nombre = comida_nombreComida.getText().toString();
        String ingredientes = comida_ingredientes.getText().toString();
        String calorias = comida_calorias.getText().toString();

        //Condiciones para verificar que los datos esten correctos
        if (ImageUri == null) { //En caso que el usuario modifico datos pero no su imagen se llama a la sig función solo para actualizar datos
            SaveInfoToDatabasewithoutImage();
        } else if (TextUtils.isEmpty(nombre)){
            comida_nombreComida.setError("Ingrese el nombre de la comida");
            comida_nombreComida.requestFocus();
        } else if (TextUtils.isEmpty(ingredientes)) {
            comida_ingredientes.setError("Ingrese los ingredientes de la comida");
            comida_ingredientes.requestFocus();
        }else if(TextUtils.isEmpty(calorias)){
            comida_calorias.setError("Ingrese las calorias de la comida");
            comida_calorias.requestFocus();
        }else {
            Calendar calendar = Calendar.getInstance();
            //SimpleDateFormat currentDate = new SimpleDateFormat(" dd MM, yyyy");
            SimpleDateFormat currentDate = new SimpleDateFormat("MM dd, yyyy");
            saveCurrentDate = currentDate.format(calendar.getTime());
            SimpleDateFormat currentTime = new SimpleDateFormat("HH:mm:ss");
            saveCurrentTime = currentTime.format(calendar.getTime());
            comidaID = saveCurrentDate + saveCurrentTime;

            StorageReference fileRef = ImagesRef.child(comidaID + ".jpg");
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

    private void SaveInfoToDatabasewithoutImage () {
        Calendar calendar = Calendar.getInstance();
        //SimpleDateFormat currentDate = new SimpleDateFormat(" dd MM, yyyy");
        SimpleDateFormat currentDate = new SimpleDateFormat("MM dd, yyyy");
        saveCurrentDate = currentDate.format(calendar.getTime());
        SimpleDateFormat currentTime = new SimpleDateFormat("HH:mm:ss");
        saveCurrentTime = currentTime.format(calendar.getTime());
        comidaID = saveCurrentDate + saveCurrentTime;
        //Obtenemos los datos que ingreso el usuario
        String nombre = comida_nombreComida.getText().toString();
        String ingredientes = comida_ingredientes.getText().toString();
        String calorias = comida_calorias.getText().toString();

        //Condiciones para verificar que los datos esten correctos
        if (TextUtils.isEmpty(nombre)){
            comida_nombreComida.setError("Ingrese el nombre de la comida");
            comida_nombreComida.requestFocus();
        } else if(TextUtils.isEmpty(ingredientes)){
            comida_ingredientes.setError("Ingrese los ingredientes de la comida");
            comida_ingredientes.requestFocus();
        } else if(TextUtils.isEmpty(calorias)){
            comida_calorias.setError("Ingrese las calorias de la comida");
            comida_calorias.requestFocus();
        } else {
            //Map para registrar a un usuario con sus datos
            Map<String, Object> comida = new HashMap<>();
            comida.put("p_ID", comidaID);
            comida.put("f_tipo", "Comida");
            comida.put("f_nombrecomida", nombre);
            comida.put("f_ingredientes", ingredientes);
            comida.put("f_calorias", calorias);

            dbRef.child(comidaID).updateChildren(comida).addOnCompleteListener(new OnCompleteListener<Void>() {
                @Override
                public void onComplete(@NonNull @NotNull Task<Void> task) {
                    if(task.isSuccessful()){
                        Toast.makeText(getActivity(), R.string.stringCambiosGuardadosCorrectamente, Toast.LENGTH_SHORT).show();
                        replaceFragment(new ComidaFragment());
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
        //Obtenemos los datos que ingreso el usuario
        String nombre = comida_nombreComida.getText().toString();
        String ingredientes = comida_ingredientes.getText().toString();
        String calorias = comida_calorias.getText().toString();

        //Condiciones para verificar que los datos esten correctos
        if (TextUtils.isEmpty(nombre)){
            comida_nombreComida.setError("Ingrese el nombre de la comida");
            comida_nombreComida.requestFocus();
        } else if(TextUtils.isEmpty(ingredientes)){
            comida_ingredientes.setError("Ingrese los ingredientes de la comida");
            comida_ingredientes.requestFocus();
        } else if(TextUtils.isEmpty(calorias)){
            comida_calorias.setError("Ingrese las calorias de la comida");
            comida_calorias.requestFocus();
        } else {
            //Map para registrar a un usuario con sus datos
            Map<String, Object> comida = new HashMap<>();
            comida.put("p_ID", comidaID);
            comida.put("f_tipo", "Comida");
            comida.put("f_nombrecomida", nombre);
            comida.put("f_ingredientes", ingredientes);
            comida.put("f_calorias", calorias);
            comida.put("f_image", downloadImageUrl);

            dbRef.child(comidaID).updateChildren(comida).addOnCompleteListener(new OnCompleteListener<Void>() {
                @Override
                public void onComplete(@NonNull @NotNull Task<Void> task) {
                    if(task.isSuccessful()){
                        Toast.makeText(getActivity(), R.string.stringCambiosGuardadosCorrectamente, Toast.LENGTH_SHORT).show();
                        replaceFragment(new ComidaFragment());
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
            btn_agregar_comida.setImageURI(ImageUri);
        }
    }
    private void replaceFragment(Fragment fragment) {
        FragmentManager fragmentManager = getFragmentManager();
        FragmentTransaction fragmentTransaction = fragmentManager.beginTransaction();
        fragmentTransaction.replace(R.id.body_container,fragment);
        fragmentTransaction.commit();
    }
}