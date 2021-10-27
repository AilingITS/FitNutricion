package com.example.fitnutricion.fragments;

import android.Manifest;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Typeface;
import android.graphics.pdf.PdfDocument;
import android.os.Bundle;
import android.os.Environment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.Spinner;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatDelegate;
import androidx.core.app.ActivityCompat;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentTransaction;

import com.example.fitnutricion.R;
import com.example.fitnutricion.firebase.SpinnerPaciente;
import com.example.fitnutricion.fragments.receta.LunesFragment;
import com.example.fitnutricion.pacienteReceta.PacienteCenaFragment;
import com.example.fitnutricion.pacienteReceta.PacienteComidaFragment;
import com.example.fitnutricion.pacienteReceta.PacienteDesayunoFragment;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import org.jetbrains.annotations.NotNull;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;

public class HomeFragment extends Fragment {

    private static final String ARG_PARAM1 = "param1";
    private static final String ARG_PARAM2 = "param2";

    private String mParam1;
    private String mParam2;

    private View vista;
    private String userID;
    private FirebaseAuth mAuth;

    String pacienteID;
    private Spinner spinnerPacientes;
    Button btn_crear_pdf, desayunoHome, comidaHome, comidaCena, btn_eliminar_receta, btn_receta;
    Bitmap bmp, scaledbmp;
    int pageWidth = 1200;

    DatabaseReference dbRef;
    DatabaseReference dbRef_pdf;
    DatabaseReference dbRef_eliminarReceta;
    DatabaseReference dbRef_recetas;
    private FirebaseDatabase firebaseDatabase_pdf;

    public HomeFragment() {
        // Required empty public constructor
    }

    public static HomeFragment newInstance(String param1, String param2) {
        HomeFragment fragment = new HomeFragment();
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

        ActivityCompat.requestPermissions(getActivity(), new String[]{
                Manifest.permission.WRITE_EXTERNAL_STORAGE}, PackageManager.PERMISSION_GRANTED);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        vista = inflater.inflate(R.layout.fragment_home, container, false);

        mAuth = FirebaseAuth.getInstance();
        userID = mAuth.getCurrentUser().getUid();
        dbRef = FirebaseDatabase.getInstance().getReference("users").child(userID);
        dbRef_eliminarReceta = FirebaseDatabase.getInstance().getReference().child("users").child(userID);

        firebaseDatabase_pdf = FirebaseDatabase.getInstance();
        dbRef_pdf = firebaseDatabase_pdf.getReference();
        //spinnerComidas = vista.findViewById(R.id.spinnerComidas);
        spinnerPacientes = vista.findViewById(R.id.spinnerPacientes);

        desayunoHome = (Button) vista.findViewById(R.id.desayunoHome);
        comidaHome = (Button) vista.findViewById(R.id.comidaHome);
        comidaCena = (Button) vista.findViewById(R.id.comidaCena);
        btn_crear_pdf = (Button) vista.findViewById(R.id.btn_crear_pdf);
        btn_receta = (Button) vista.findViewById(R.id.btn_receta);
        btn_eliminar_receta = (Button) vista.findViewById(R.id.btn_eliminar_receta);
        bmp = BitmapFactory.decodeResource(getResources(), R.drawable.logo_degradado);
        scaledbmp = Bitmap.createScaledBitmap(bmp, 180, 180, false);

        //String [] opciones = {"Desayuno", "Comida", "Cena"};
        //ArrayAdapter <String> adapter = new ArrayAdapter<String>(getContext(), android.R.layout.simple_spinner_item, opciones);
        //spinnerComidas.setAdapter(adapter);
        //String seleccion = spinnerComidas.getSelectedItem().toString();

        loadNamePacientes();

        btn_crear_pdf.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                try {
                    createPDF();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        });

        btn_eliminar_receta.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                dbRef_eliminarReceta.child("recetas").removeValue();
                Toast.makeText(getContext(), "Receta eliminada correctamente", Toast.LENGTH_SHORT).show();
            }
        });

        btn_receta.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                replaceFragment(new LunesFragment());
                /*Intent intent = new Intent(getActivity(), VisualizarRecetaActivity.class);
                startActivity(intent);*/
            }
        });

        desayunoHome.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                replaceFragment(new PacienteDesayunoFragment());
            }
        });

        comidaHome.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                replaceFragment(new PacienteComidaFragment());
            }
        });

        comidaCena.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                replaceFragment(new PacienteCenaFragment());
            }
        });

        //Poner esto cuando se cree el pdf para saber el tipo de comida
        /*if(seleccion.equals("Desayuno")){

        } else if(seleccion.equals("Comida")){

        } else if(seleccion.equals("Cena")){

        }*/

        return vista;
    }

    private void createPDF() throws IOException {

        dbRef_pdf.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull @NotNull DataSnapshot snapshot) {
                if(snapshot.exists()){

                    Calendar calendar = Calendar.getInstance();
                    //SimpleDateFormat currentDate = new SimpleDateFormat(" dd MM, yyyy");
                    SimpleDateFormat currentDate = new SimpleDateFormat("dd/MM/yyyy");
                    String saveCurrentDate = currentDate.format(calendar.getTime());
                    String Fecha = saveCurrentDate;

                    SimpleDateFormat currentDate1 = new SimpleDateFormat("dd MM yyyy");
                    String nombrepdfFecha = currentDate1.format(calendar.getTime());
                    SimpleDateFormat currentTime = new SimpleDateFormat("HH mm ss");
                    String nombrepdfHora = currentTime.format(calendar.getTime());

                    /* DATOS DEL NUTRIOLOGO - PDF */
                    userID = mAuth.getCurrentUser().getUid();
                    String nombre_nutri_pdf = snapshot.child("users").child(userID).child("Nombre").getValue().toString();
                    String correo_nutri_pdf = snapshot.child("users").child(userID).child("Correo").getValue().toString();

                    /* DATOS DEL PACIENTE - PDF */
                    String nombre_p_pdf = snapshot.child("users").child(userID).child("pacientes").child(pacienteID).child("p_Nombre").getValue().toString();
                    String Correo_p_pdf = snapshot.child("users").child(userID).child("pacientes").child(pacienteID).child("p_Correo").getValue().toString();
                    String Edad_p_pdf = snapshot.child("users").child(userID).child("pacientes").child(pacienteID).child("p_Edad").getValue().toString();
                    String Sexo_p_pdf = snapshot.child("users").child(userID).child("pacientes").child(pacienteID).child("p_Sexo").getValue().toString();

                    String pdfPath = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS).toString();
                    File file = new File(pdfPath, "FitNutricion " + nombrepdfFecha + " " + nombrepdfHora + ".pdf");
                    OutputStream outputStream = null;
                    try {
                        outputStream = new FileOutputStream(file);
                    } catch (FileNotFoundException e) {
                        e.printStackTrace();
                    }

                    /* DECLARANDO DOCUMENTO */

                    PdfDocument document = new PdfDocument();

                    /* INICIO DE PRIMERA PAGINA */

                    PdfDocument.PageInfo pageInfo = new PdfDocument.PageInfo.Builder(1200, 2010, 1).create();
                    PdfDocument.Page page = document.startPage(pageInfo);

                    Canvas canvas = page.getCanvas();
                    Paint myPaint = new Paint();
                    Paint titlePaint = new Paint();

                    /* DATOS DEL NUTRIOLOGO */
                    titlePaint.setTextAlign(Paint.Align.LEFT);
                    titlePaint.setTextSize(20);
                    titlePaint.setColor(Color.rgb(110, 184, 245));
                    canvas.drawText("Dr.: " + nombre_nutri_pdf, 50, 75, titlePaint);

                    titlePaint.setTextAlign(Paint.Align.LEFT);
                    titlePaint.setTextSize(20);
                    titlePaint.setColor(Color.rgb(110, 184, 245));
                    canvas.drawText(correo_nutri_pdf, 50, 110, titlePaint);
                    /* -------------------- */

                    /* FECHA Y DATOS DEL PACIENTE */
                    titlePaint.setTextAlign(Paint.Align.CENTER);
                    titlePaint.setTypeface(Typeface.create(Typeface.DEFAULT, Typeface.BOLD_ITALIC));
                    titlePaint.setTextSize(45);
                    titlePaint.setColor(Color.rgb(0,0,0));
                    canvas.drawText("RECETA SEMANAL", pageWidth/2, 220, titlePaint);

                    titlePaint.setTextAlign(Paint.Align.LEFT);
                    titlePaint.setTypeface(Typeface.create(Typeface.DEFAULT, Typeface.NORMAL));
                    titlePaint.setTextSize(25);
                    titlePaint.setColor(Color.rgb(0,0,0));
                    canvas.drawText("Fecha: " + Fecha, 50, 330, titlePaint);

                    titlePaint.setTextAlign(Paint.Align.LEFT);
                    titlePaint.setTypeface(Typeface.create(Typeface.DEFAULT, Typeface.NORMAL));
                    titlePaint.setTextSize(25);
                    titlePaint.setColor(Color.rgb(0,0,0));
                    canvas.drawText("Nombre del paciente: " + nombre_p_pdf, 50, 400, titlePaint);

                    titlePaint.setTextAlign(Paint.Align.LEFT);
                    titlePaint.setTypeface(Typeface.create(Typeface.DEFAULT, Typeface.NORMAL));
                    titlePaint.setTextSize(25);
                    titlePaint.setColor(Color.rgb(0,0,0));
                    canvas.drawText("Edad: " + Edad_p_pdf + " años", 50, 470, titlePaint);

                    titlePaint.setTextAlign(Paint.Align.LEFT);
                    titlePaint.setTypeface(Typeface.create(Typeface.DEFAULT, Typeface.NORMAL));
                    titlePaint.setTextSize(25);
                    titlePaint.setColor(Color.rgb(0,0,0));
                    canvas.drawText("Sexo: " + Sexo_p_pdf, 50, 550, titlePaint);
                    /* -------------------------- */

                    /* LISTA DE COMIDAS - TABLA */
                    titlePaint.setTextAlign(Paint.Align.CENTER);
                    titlePaint.setTypeface(Typeface.create(Typeface.DEFAULT, Typeface.BOLD_ITALIC));
                    titlePaint.setTextSize(35);
                    titlePaint.setColor(Color.rgb(0,0,0));
                    canvas.drawText("Lista de alimentos", pageWidth/2, 660, titlePaint);

                    titlePaint.setTextAlign(Paint.Align.CENTER);
                    titlePaint.setTypeface(Typeface.create(Typeface.DEFAULT, Typeface.BOLD));
                    titlePaint.setTextSize(30);
                    titlePaint.setColor(Color.rgb(0,0,0));
                    canvas.drawText("Lunes", 220, 800, titlePaint);

                    if(snapshot.child("users").child(userID).child("recetas").child("Lunes").child("A_Desayuno").exists()){
                        String lunesA = snapshot.child("users").child(userID).child("recetas").child("Lunes").child("A_Desayuno").child("f_nombrecomida").getValue().toString();
                        titlePaint.setTextAlign(Paint.Align.CENTER);
                        titlePaint.setTypeface(Typeface.create(Typeface.DEFAULT, Typeface.NORMAL));
                        titlePaint.setTextSize(20);
                        titlePaint.setColor(Color.rgb(0,0,0));
                        canvas.drawText(lunesA, 220, 865, titlePaint);
                    } else {
                        titlePaint.setTextAlign(Paint.Align.CENTER);
                        titlePaint.setTypeface(Typeface.create(Typeface.DEFAULT, Typeface.NORMAL));
                        titlePaint.setTextSize(20);
                        titlePaint.setColor(Color.rgb(0,0,0));
                        canvas.drawText("Sin desayuno", 220, 865, titlePaint);
                    }

                    if(snapshot.child("users").child(userID).child("recetas").child("Lunes").child("B_Comida").exists()){
                        String lunesB = snapshot.child("users").child(userID).child("recetas").child("Lunes").child("B_Comida").child("f_nombrecomida").getValue().toString();
                        titlePaint.setTextAlign(Paint.Align.CENTER);
                        titlePaint.setTypeface(Typeface.create(Typeface.DEFAULT, Typeface.NORMAL));
                        titlePaint.setTextSize(20);
                        titlePaint.setColor(Color.rgb(0,0,0));
                        canvas.drawText(lunesB, 220, 915, titlePaint);
                    } else {
                        titlePaint.setTextAlign(Paint.Align.CENTER);
                        titlePaint.setTypeface(Typeface.create(Typeface.DEFAULT, Typeface.NORMAL));
                        titlePaint.setTextSize(20);
                        titlePaint.setColor(Color.rgb(0,0,0));
                        canvas.drawText("Sin comida", 220, 915, titlePaint);
                    }

                    if(snapshot.child("users").child(userID).child("recetas").child("Lunes").child("C_Cena").exists()){
                        String lunesC = snapshot.child("users").child(userID).child("recetas").child("Lunes").child("C_Cena").child("f_nombrecomida").getValue().toString();
                        titlePaint.setTextAlign(Paint.Align.CENTER);
                        titlePaint.setTypeface(Typeface.create(Typeface.DEFAULT, Typeface.NORMAL));
                        titlePaint.setTextSize(20);
                        titlePaint.setColor(Color.rgb(0,0,0));
                        canvas.drawText(lunesC, 220, 965, titlePaint);
                    } else {
                        titlePaint.setTextAlign(Paint.Align.CENTER);
                        titlePaint.setTypeface(Typeface.create(Typeface.DEFAULT, Typeface.NORMAL));
                        titlePaint.setTextSize(20);
                        titlePaint.setColor(Color.rgb(0,0,0));
                        canvas.drawText("Sin cena", 220, 965, titlePaint);
                    }

                    titlePaint.setTextAlign(Paint.Align.CENTER);
                    titlePaint.setTypeface(Typeface.create(Typeface.DEFAULT, Typeface.BOLD));
                    titlePaint.setTextSize(30);
                    titlePaint.setColor(Color.rgb(0,0,0));
                    canvas.drawText("Martes", 600, 800, titlePaint);

                    if(snapshot.child("users").child(userID).child("recetas").child("Martes").child("A_Desayuno").exists()){
                        String lunesA = snapshot.child("users").child(userID).child("recetas").child("Martes").child("A_Desayuno").child("f_nombrecomida").getValue().toString();
                        titlePaint.setTextAlign(Paint.Align.CENTER);
                        titlePaint.setTypeface(Typeface.create(Typeface.DEFAULT, Typeface.NORMAL));
                        titlePaint.setTextSize(20);
                        titlePaint.setColor(Color.rgb(0,0,0));
                        canvas.drawText(lunesA, 600, 865, titlePaint);
                    } else {
                        titlePaint.setTextAlign(Paint.Align.CENTER);
                        titlePaint.setTypeface(Typeface.create(Typeface.DEFAULT, Typeface.NORMAL));
                        titlePaint.setTextSize(20);
                        titlePaint.setColor(Color.rgb(0,0,0));
                        canvas.drawText("Sin desayuno", 600, 865, titlePaint);
                    }

                    if(snapshot.child("users").child(userID).child("recetas").child("Martes").child("B_Comida").exists()){
                        String lunesB = snapshot.child("users").child(userID).child("recetas").child("Martes").child("B_Comida").child("f_nombrecomida").getValue().toString();
                        titlePaint.setTextAlign(Paint.Align.CENTER);
                        titlePaint.setTypeface(Typeface.create(Typeface.DEFAULT, Typeface.NORMAL));
                        titlePaint.setTextSize(20);
                        titlePaint.setColor(Color.rgb(0,0,0));
                        canvas.drawText(lunesB, 600, 915, titlePaint);
                    } else {
                        titlePaint.setTextAlign(Paint.Align.CENTER);
                        titlePaint.setTypeface(Typeface.create(Typeface.DEFAULT, Typeface.NORMAL));
                        titlePaint.setTextSize(20);
                        titlePaint.setColor(Color.rgb(0,0,0));
                        canvas.drawText("Sin comida", 600, 915, titlePaint);
                    }

                    if(snapshot.child("users").child(userID).child("recetas").child("Martes").child("C_Cena").exists()){
                        String lunesC = snapshot.child("users").child(userID).child("recetas").child("Martes").child("C_Cena").child("f_nombrecomida").getValue().toString();
                        titlePaint.setTextAlign(Paint.Align.CENTER);
                        titlePaint.setTypeface(Typeface.create(Typeface.DEFAULT, Typeface.NORMAL));
                        titlePaint.setTextSize(20);
                        titlePaint.setColor(Color.rgb(0,0,0));
                        canvas.drawText(lunesC, 600, 965, titlePaint);
                    } else {
                        titlePaint.setTextAlign(Paint.Align.CENTER);
                        titlePaint.setTypeface(Typeface.create(Typeface.DEFAULT, Typeface.NORMAL));
                        titlePaint.setTextSize(20);
                        titlePaint.setColor(Color.rgb(0,0,0));
                        canvas.drawText("Sin cena", 600, 965, titlePaint);
                    }

                    titlePaint.setTextAlign(Paint.Align.CENTER);
                    titlePaint.setTypeface(Typeface.create(Typeface.DEFAULT, Typeface.BOLD));
                    titlePaint.setTextSize(30);
                    titlePaint.setColor(Color.rgb(0,0,0));
                    canvas.drawText("Miercoles", 980, 800, titlePaint);

                    if(snapshot.child("users").child(userID).child("recetas").child("Miercoles").child("A_Desayuno").exists()){
                        String lunesA = snapshot.child("users").child(userID).child("recetas").child("Miercoles").child("A_Desayuno").child("f_nombrecomida").getValue().toString();
                        titlePaint.setTextAlign(Paint.Align.CENTER);
                        titlePaint.setTypeface(Typeface.create(Typeface.DEFAULT, Typeface.NORMAL));
                        titlePaint.setTextSize(20);
                        titlePaint.setColor(Color.rgb(0,0,0));
                        canvas.drawText(lunesA, 980, 865, titlePaint);
                    } else {
                        titlePaint.setTextAlign(Paint.Align.CENTER);
                        titlePaint.setTypeface(Typeface.create(Typeface.DEFAULT, Typeface.NORMAL));
                        titlePaint.setTextSize(20);
                        titlePaint.setColor(Color.rgb(0,0,0));
                        canvas.drawText("Sin desayuno", 980, 865, titlePaint);
                    }

                    if(snapshot.child("users").child(userID).child("recetas").child("Miercoles").child("B_Comida").exists()){
                        String lunesB = snapshot.child("users").child(userID).child("recetas").child("Miercoles").child("B_Comida").child("f_nombrecomida").getValue().toString();
                        titlePaint.setTextAlign(Paint.Align.CENTER);
                        titlePaint.setTypeface(Typeface.create(Typeface.DEFAULT, Typeface.NORMAL));
                        titlePaint.setTextSize(20);
                        titlePaint.setColor(Color.rgb(0,0,0));
                        canvas.drawText(lunesB, 980, 915, titlePaint);
                    } else {
                        titlePaint.setTextAlign(Paint.Align.CENTER);
                        titlePaint.setTypeface(Typeface.create(Typeface.DEFAULT, Typeface.NORMAL));
                        titlePaint.setTextSize(20);
                        titlePaint.setColor(Color.rgb(0,0,0));
                        canvas.drawText("Sin comida", 980, 915, titlePaint);
                    }

                    if(snapshot.child("users").child(userID).child("recetas").child("Miercoles").child("C_Cena").exists()){
                        String lunesC = snapshot.child("users").child(userID).child("recetas").child("Miercoles").child("C_Cena").child("f_nombrecomida").getValue().toString();
                        titlePaint.setTextAlign(Paint.Align.CENTER);
                        titlePaint.setTypeface(Typeface.create(Typeface.DEFAULT, Typeface.NORMAL));
                        titlePaint.setTextSize(20);
                        titlePaint.setColor(Color.rgb(0,0,0));
                        canvas.drawText(lunesC, 980, 965, titlePaint);
                    } else {
                        titlePaint.setTextAlign(Paint.Align.CENTER);
                        titlePaint.setTypeface(Typeface.create(Typeface.DEFAULT, Typeface.NORMAL));
                        titlePaint.setTextSize(20);
                        titlePaint.setColor(Color.rgb(0,0,0));
                        canvas.drawText("Sin cena", 980, 965, titlePaint);
                    }

                    titlePaint.setTextAlign(Paint.Align.CENTER);
                    titlePaint.setTypeface(Typeface.create(Typeface.DEFAULT, Typeface.BOLD));
                    titlePaint.setTextSize(30);
                    titlePaint.setColor(Color.rgb(0,0,0));
                    canvas.drawText("Jueves", 220, 1050, titlePaint);

                    if(snapshot.child("users").child(userID).child("recetas").child("Jueves").child("A_Desayuno").exists()){
                        String lunesA = snapshot.child("users").child(userID).child("recetas").child("Jueves").child("A_Desayuno").child("f_nombrecomida").getValue().toString();
                        titlePaint.setTextAlign(Paint.Align.CENTER);
                        titlePaint.setTypeface(Typeface.create(Typeface.DEFAULT, Typeface.NORMAL));
                        titlePaint.setTextSize(20);
                        titlePaint.setColor(Color.rgb(0,0,0));
                        canvas.drawText(lunesA, 220, 1115, titlePaint);
                    } else {
                        titlePaint.setTextAlign(Paint.Align.CENTER);
                        titlePaint.setTypeface(Typeface.create(Typeface.DEFAULT, Typeface.NORMAL));
                        titlePaint.setTextSize(20);
                        titlePaint.setColor(Color.rgb(0,0,0));
                        canvas.drawText("Sin desayuno", 220, 1115, titlePaint);
                    }

                    if(snapshot.child("users").child(userID).child("recetas").child("Jueves").child("B_Comida").exists()){
                        String lunesB = snapshot.child("users").child(userID).child("recetas").child("Jueves").child("B_Comida").child("f_nombrecomida").getValue().toString();
                        titlePaint.setTextAlign(Paint.Align.CENTER);
                        titlePaint.setTypeface(Typeface.create(Typeface.DEFAULT, Typeface.NORMAL));
                        titlePaint.setTextSize(20);
                        titlePaint.setColor(Color.rgb(0,0,0));
                        canvas.drawText(lunesB, 220, 1165, titlePaint);
                    } else {
                        titlePaint.setTextAlign(Paint.Align.CENTER);
                        titlePaint.setTypeface(Typeface.create(Typeface.DEFAULT, Typeface.NORMAL));
                        titlePaint.setTextSize(20);
                        titlePaint.setColor(Color.rgb(0,0,0));
                        canvas.drawText("Sin comida", 220, 1165, titlePaint);
                    }

                    if(snapshot.child("users").child(userID).child("recetas").child("Jueves").child("C_Cena").exists()){
                        String lunesC = snapshot.child("users").child(userID).child("recetas").child("Jueves").child("C_Cena").child("f_nombrecomida").getValue().toString();
                        titlePaint.setTextAlign(Paint.Align.CENTER);
                        titlePaint.setTypeface(Typeface.create(Typeface.DEFAULT, Typeface.NORMAL));
                        titlePaint.setTextSize(20);
                        titlePaint.setColor(Color.rgb(0,0,0));
                        canvas.drawText(lunesC, 220, 1215, titlePaint);
                    } else {
                        titlePaint.setTextAlign(Paint.Align.CENTER);
                        titlePaint.setTypeface(Typeface.create(Typeface.DEFAULT, Typeface.NORMAL));
                        titlePaint.setTextSize(20);
                        titlePaint.setColor(Color.rgb(0,0,0));
                        canvas.drawText("Sin cena", 220, 1215, titlePaint);
                    }

                    titlePaint.setTextAlign(Paint.Align.CENTER);
                    titlePaint.setTypeface(Typeface.create(Typeface.DEFAULT, Typeface.BOLD));
                    titlePaint.setTextSize(30);
                    titlePaint.setColor(Color.rgb(0,0,0));
                    canvas.drawText("Viernes", 600, 1050, titlePaint);

                    if(snapshot.child("users").child(userID).child("recetas").child("Viernes").child("A_Desayuno").exists()){
                        String lunesA = snapshot.child("users").child(userID).child("recetas").child("Viernes").child("A_Desayuno").child("f_nombrecomida").getValue().toString();
                        titlePaint.setTextAlign(Paint.Align.CENTER);
                        titlePaint.setTypeface(Typeface.create(Typeface.DEFAULT, Typeface.NORMAL));
                        titlePaint.setTextSize(20);
                        titlePaint.setColor(Color.rgb(0,0,0));
                        canvas.drawText(lunesA, 600, 1115, titlePaint);
                    } else {
                        titlePaint.setTextAlign(Paint.Align.CENTER);
                        titlePaint.setTypeface(Typeface.create(Typeface.DEFAULT, Typeface.NORMAL));
                        titlePaint.setTextSize(20);
                        titlePaint.setColor(Color.rgb(0,0,0));
                        canvas.drawText("Sin desayuno", 600, 1115, titlePaint);
                    }

                    if(snapshot.child("users").child(userID).child("recetas").child("Viernes").child("B_Comida").exists()){
                        String lunesB = snapshot.child("users").child(userID).child("recetas").child("Viernes").child("B_Comida").child("f_nombrecomida").getValue().toString();
                        titlePaint.setTextAlign(Paint.Align.CENTER);
                        titlePaint.setTypeface(Typeface.create(Typeface.DEFAULT, Typeface.NORMAL));
                        titlePaint.setTextSize(20);
                        titlePaint.setColor(Color.rgb(0,0,0));
                        canvas.drawText(lunesB, 600, 1165, titlePaint);
                    } else {
                        titlePaint.setTextAlign(Paint.Align.CENTER);
                        titlePaint.setTypeface(Typeface.create(Typeface.DEFAULT, Typeface.NORMAL));
                        titlePaint.setTextSize(20);
                        titlePaint.setColor(Color.rgb(0,0,0));
                        canvas.drawText("Sin comida", 600, 1165, titlePaint);
                    }

                    if(snapshot.child("users").child(userID).child("recetas").child("Viernes").child("C_Cena").exists()){
                        String lunesC = snapshot.child("users").child(userID).child("recetas").child("Viernes").child("C_Cena").child("f_nombrecomida").getValue().toString();
                        titlePaint.setTextAlign(Paint.Align.CENTER);
                        titlePaint.setTypeface(Typeface.create(Typeface.DEFAULT, Typeface.NORMAL));
                        titlePaint.setTextSize(20);
                        titlePaint.setColor(Color.rgb(0,0,0));
                        canvas.drawText(lunesC, 600, 1215, titlePaint);
                    } else {
                        titlePaint.setTextAlign(Paint.Align.CENTER);
                        titlePaint.setTypeface(Typeface.create(Typeface.DEFAULT, Typeface.NORMAL));
                        titlePaint.setTextSize(20);
                        titlePaint.setColor(Color.rgb(0,0,0));
                        canvas.drawText("Sin cena", 600, 1215, titlePaint);
                    }

                    titlePaint.setTextAlign(Paint.Align.CENTER);
                    titlePaint.setTypeface(Typeface.create(Typeface.DEFAULT, Typeface.BOLD));
                    titlePaint.setTextSize(30);
                    titlePaint.setColor(Color.rgb(0,0,0));
                    canvas.drawText("Sabado", 980, 1050, titlePaint);

                    if(snapshot.child("users").child(userID).child("recetas").child("Sabado").child("A_Desayuno").exists()){
                        String lunesA = snapshot.child("users").child(userID).child("recetas").child("Sabado").child("A_Desayuno").child("f_nombrecomida").getValue().toString();
                        titlePaint.setTextAlign(Paint.Align.CENTER);
                        titlePaint.setTypeface(Typeface.create(Typeface.DEFAULT, Typeface.NORMAL));
                        titlePaint.setTextSize(20);
                        titlePaint.setColor(Color.rgb(0,0,0));
                        canvas.drawText(lunesA, 980, 1115, titlePaint);
                    } else {
                        titlePaint.setTextAlign(Paint.Align.CENTER);
                        titlePaint.setTypeface(Typeface.create(Typeface.DEFAULT, Typeface.NORMAL));
                        titlePaint.setTextSize(20);
                        titlePaint.setColor(Color.rgb(0,0,0));
                        canvas.drawText("Sin desayuno", 980, 1115, titlePaint);
                    }

                    if(snapshot.child("users").child(userID).child("recetas").child("Sabado").child("B_Comida").exists()){
                        String lunesB = snapshot.child("users").child(userID).child("recetas").child("Sabado").child("B_Comida").child("f_nombrecomida").getValue().toString();
                        titlePaint.setTextAlign(Paint.Align.CENTER);
                        titlePaint.setTypeface(Typeface.create(Typeface.DEFAULT, Typeface.NORMAL));
                        titlePaint.setTextSize(20);
                        titlePaint.setColor(Color.rgb(0,0,0));
                        canvas.drawText(lunesB, 980, 1165, titlePaint);
                    } else {
                        titlePaint.setTextAlign(Paint.Align.CENTER);
                        titlePaint.setTypeface(Typeface.create(Typeface.DEFAULT, Typeface.NORMAL));
                        titlePaint.setTextSize(20);
                        titlePaint.setColor(Color.rgb(0,0,0));
                        canvas.drawText("Sin comida", 980, 1165, titlePaint);
                    }

                    if(snapshot.child("users").child(userID).child("recetas").child("Sabado").child("C_Cena").exists()){
                        String lunesC = snapshot.child("users").child(userID).child("recetas").child("Sabado").child("C_Cena").child("f_nombrecomida").getValue().toString();
                        titlePaint.setTextAlign(Paint.Align.CENTER);
                        titlePaint.setTypeface(Typeface.create(Typeface.DEFAULT, Typeface.NORMAL));
                        titlePaint.setTextSize(20);
                        titlePaint.setColor(Color.rgb(0,0,0));
                        canvas.drawText(lunesC, 980, 1215, titlePaint);
                    } else {
                        titlePaint.setTextAlign(Paint.Align.CENTER);
                        titlePaint.setTypeface(Typeface.create(Typeface.DEFAULT, Typeface.NORMAL));
                        titlePaint.setTextSize(20);
                        titlePaint.setColor(Color.rgb(0,0,0));
                        canvas.drawText("Sin cena", 980, 1215, titlePaint);
                    }

                    titlePaint.setTextAlign(Paint.Align.CENTER);
                    titlePaint.setTypeface(Typeface.create(Typeface.DEFAULT, Typeface.BOLD));
                    titlePaint.setTextSize(30);
                    titlePaint.setColor(Color.rgb(0,0,0));
                    canvas.drawText("Domingo", 220, 1300, titlePaint);

                    if(snapshot.child("users").child(userID).child("recetas").child("Domingo").child("A_Desayuno").exists()){
                        String lunesA = snapshot.child("users").child(userID).child("recetas").child("Domingo").child("A_Desayuno").child("f_nombrecomida").getValue().toString();
                        titlePaint.setTextAlign(Paint.Align.CENTER);
                        titlePaint.setTypeface(Typeface.create(Typeface.DEFAULT, Typeface.NORMAL));
                        titlePaint.setTextSize(20);
                        titlePaint.setColor(Color.rgb(0,0,0));
                        canvas.drawText(lunesA, 220, 1365, titlePaint);
                    } else {
                        titlePaint.setTextAlign(Paint.Align.CENTER);
                        titlePaint.setTypeface(Typeface.create(Typeface.DEFAULT, Typeface.NORMAL));
                        titlePaint.setTextSize(20);
                        titlePaint.setColor(Color.rgb(0,0,0));
                        canvas.drawText("Sin desayuno", 220, 1365, titlePaint);
                    }

                    if(snapshot.child("users").child(userID).child("recetas").child("Domingo").child("B_Comida").exists()){
                        String lunesB = snapshot.child("users").child(userID).child("recetas").child("Domingo").child("B_Comida").child("f_nombrecomida").getValue().toString();
                        titlePaint.setTextAlign(Paint.Align.CENTER);
                        titlePaint.setTypeface(Typeface.create(Typeface.DEFAULT, Typeface.NORMAL));
                        titlePaint.setTextSize(20);
                        titlePaint.setColor(Color.rgb(0,0,0));
                        canvas.drawText(lunesB, 220, 1415, titlePaint);
                    } else {
                        titlePaint.setTextAlign(Paint.Align.CENTER);
                        titlePaint.setTypeface(Typeface.create(Typeface.DEFAULT, Typeface.NORMAL));
                        titlePaint.setTextSize(20);
                        titlePaint.setColor(Color.rgb(0,0,0));
                        canvas.drawText("Sin comida", 220, 1415, titlePaint);
                    }

                    if(snapshot.child("users").child(userID).child("recetas").child("Domingo").child("C_Cena").exists()){
                        String lunesC = snapshot.child("users").child(userID).child("recetas").child("Domingo").child("C_Cena").child("f_nombrecomida").getValue().toString();
                        titlePaint.setTextAlign(Paint.Align.CENTER);
                        titlePaint.setTypeface(Typeface.create(Typeface.DEFAULT, Typeface.NORMAL));
                        titlePaint.setTextSize(20);
                        titlePaint.setColor(Color.rgb(0,0,0));
                        canvas.drawText(lunesC, 220, 1465, titlePaint);
                    } else {
                        titlePaint.setTextAlign(Paint.Align.CENTER);
                        titlePaint.setTypeface(Typeface.create(Typeface.DEFAULT, Typeface.NORMAL));
                        titlePaint.setTextSize(20);
                        titlePaint.setColor(Color.rgb(0,0,0));
                        canvas.drawText("Sin cena", 220, 1465, titlePaint);
                    }

                    // L-M-M
                    titlePaint.setStyle(Paint.Style.STROKE);
                    titlePaint.setStrokeWidth(2);
                    // Linea horizontal Arriba y abajo
                    canvas.drawRect(30, 750, 1170, 1000, titlePaint);
                    // 2 Lineas Verticales del centro
                    canvas.drawLine(410, 750, 410, 1000, titlePaint);
                    canvas.drawLine(790, 750, 790, 1000, titlePaint);

                    // J-V-S
                    // Linea horizontal
                    canvas.drawLine(30, 1250, 1170, 1250, titlePaint);
                    // Linea Vertical
                    canvas.drawLine(30, 1000, 30, 1250, titlePaint);
                    canvas.drawLine(410, 1000, 410, 1250, titlePaint);
                    canvas.drawLine(790, 1000, 790, 1250, titlePaint);
                    canvas.drawLine(1170, 1000, 1170, 1250, titlePaint);
                    titlePaint.setStrokeWidth(0);
                    titlePaint.setStyle(Paint.Style.FILL);

                    // D
                    // Linea horizontal
                    canvas.drawLine(30, 1500, 410, 1500, titlePaint);
                    // Linea Vertical
                    canvas.drawLine(30, 1250, 30, 1500, titlePaint);
                    canvas.drawLine(410, 1250, 410, 1500, titlePaint);
                    titlePaint.setStrokeWidth(0);
                    titlePaint.setStyle(Paint.Style.FILL);
                    /* ----------------------------- */

                    /* LOGO PARTE DE ABAJO DEL PDF */
                    canvas.drawBitmap(scaledbmp, 515, 1800, myPaint);
                    /* --------------------------- */

                    document.finishPage(page);

                    /* INICIO DE SEGUNDA PAGINA */

                    PdfDocument.PageInfo pageInfo2 = new PdfDocument.PageInfo.Builder(1200, 2010, 1).create();
                    PdfDocument.Page page2 = document.startPage(pageInfo2);

                    Canvas canvas2 = page2.getCanvas();
                    Paint myPaint2 = new Paint();
                    Paint titlePaint2 = new Paint();

                    /* DATOS DEL NUTRIOLOGO */
                    titlePaint2.setTextAlign(Paint.Align.LEFT);
                    titlePaint2.setTextSize(20);
                    titlePaint2.setColor(Color.rgb(110, 184, 245));
                    canvas2.drawText("Dr.: " + nombre_nutri_pdf, 50, 75, titlePaint2);

                    titlePaint2.setTextAlign(Paint.Align.LEFT);
                    titlePaint2.setTextSize(20);
                    titlePaint2.setColor(Color.rgb(110, 184, 245));
                    canvas2.drawText(correo_nutri_pdf, 50, 110, titlePaint2);
                    /* -------------------- */

                    /* ALIMENTOS LUNES */
                    titlePaint2.setTextAlign(Paint.Align.CENTER);
                    titlePaint2.setTypeface(Typeface.create(Typeface.DEFAULT, Typeface.BOLD));
                    titlePaint2.setTextSize(45);
                    titlePaint2.setColor(Color.rgb(0,0,0));
                    canvas2.drawText("Lunes", pageWidth/2, 260, titlePaint2);

                    titlePaint2.setStyle(Paint.Style.STROKE);
                    titlePaint2.setStrokeWidth(2);
                    // Tablas Desayuno, comida y cena
                    canvas2.drawRect(60, 380, 1140, 780, titlePaint2);
                    canvas2.drawLine(60, 440, 1140, 440, titlePaint);

                    canvas2.drawRect(60, 830, 1140, 1230, titlePaint2);
                    canvas2.drawLine(60, 890, 1140, 890, titlePaint);

                    canvas2.drawRect(60, 1280, 1140, 1680, titlePaint2);
                    canvas2.drawLine(60, 1340, 1140, 1340, titlePaint);

                    titlePaint2.setStrokeWidth(0);
                    titlePaint2.setStyle(Paint.Style.FILL);

                    //Desayuno
                    titlePaint2.setTextAlign(Paint.Align.CENTER);
                    titlePaint2.setTypeface(Typeface.create(Typeface.DEFAULT, Typeface.BOLD));
                    titlePaint2.setTextSize(30);
                    titlePaint2.setColor(Color.rgb(0,0,0));
                    canvas2.drawText("Tipo de alimento: Desayuno", pageWidth/2, 420, titlePaint2);

                    //Lunes d_NOMBRE Firebase
                    if(snapshot.child("users").child(userID).child("recetas").child("Lunes").child("A_Desayuno").exists()){
                        String lunesDesayuno = snapshot.child("users").child(userID).child("recetas").child("Lunes").child("A_Desayuno").child("f_nombrecomida").getValue().toString();
                        titlePaint2.setTextAlign(Paint.Align.LEFT);
                        titlePaint2.setTypeface(Typeface.create(Typeface.DEFAULT, Typeface.NORMAL));
                        titlePaint2.setTextSize(25);
                        titlePaint2.setColor(Color.rgb(0,0,0));
                        canvas2.drawText("Nombre: " + lunesDesayuno, 100, 500, titlePaint2);
                    } else {
                        titlePaint2.setTextAlign(Paint.Align.LEFT);
                        titlePaint2.setTypeface(Typeface.create(Typeface.DEFAULT, Typeface.NORMAL));
                        titlePaint2.setTextSize(25);
                        titlePaint2.setColor(Color.rgb(0,0,0));
                        canvas2.drawText("Nombre: Sin desayuno", 280, 500, titlePaint2);
                    }

                    if(snapshot.child("users").child(userID).child("recetas").child("Lunes").child("A_Desayuno").exists()){
                        String lunesIngredientes = snapshot.child("users").child(userID).child("recetas").child("Lunes").child("A_Desayuno").child("f_ingredientes").getValue().toString();

                        titlePaint2.setTextAlign(Paint.Align.LEFT);
                        titlePaint2.setTypeface(Typeface.create(Typeface.DEFAULT, Typeface.NORMAL));
                        titlePaint2.setTextSize(25);
                        titlePaint2.setColor(Color.rgb(0,0,0));
                        canvas2.drawText("Ingredientes:", 100, 560, titlePaint2);

                        imprimirIngredientes(100, 590, lunesIngredientes, titlePaint2, canvas2);

                    }

                    if(snapshot.child("users").child(userID).child("recetas").child("Lunes").child("A_Desayuno").exists()){
                        String lunesCalorias = snapshot.child("users").child(userID).child("recetas").child("Lunes").child("A_Desayuno").child("f_calorias").getValue().toString();
                        titlePaint2.setTextAlign(Paint.Align.LEFT);
                        titlePaint2.setTypeface(Typeface.create(Typeface.DEFAULT, Typeface.NORMAL));
                        titlePaint2.setTextSize(20);
                        titlePaint2.setColor(Color.rgb(0,0,0));
                        canvas2.drawText("Contenido calórico aproximado: " + lunesCalorias, 100, 760, titlePaint2);
                    }

                    //Comida
                    titlePaint2.setTextAlign(Paint.Align.CENTER);
                    titlePaint2.setTypeface(Typeface.create(Typeface.DEFAULT, Typeface.BOLD));
                    titlePaint2.setTextSize(30);
                    titlePaint2.setColor(Color.rgb(0,0,0));
                    canvas2.drawText("Tipo de alimento: Comida", pageWidth/2, 870, titlePaint2);

                    if(snapshot.child("users").child(userID).child("recetas").child("Lunes").child("B_Comida").exists()){
                        String lunesComida = snapshot.child("users").child(userID).child("recetas").child("Lunes").child("B_Comida").child("f_nombrecomida").getValue().toString();
                        titlePaint2.setTextAlign(Paint.Align.LEFT);
                        titlePaint2.setTypeface(Typeface.create(Typeface.DEFAULT, Typeface.NORMAL));
                        titlePaint2.setTextSize(25);
                        titlePaint2.setColor(Color.rgb(0,0,0));
                        canvas2.drawText("Nombre: " + lunesComida, 100, 950, titlePaint2);
                    } else {
                        titlePaint2.setTextAlign(Paint.Align.LEFT);
                        titlePaint2.setTypeface(Typeface.create(Typeface.DEFAULT, Typeface.NORMAL));
                        titlePaint2.setTextSize(25);
                        titlePaint2.setColor(Color.rgb(0,0,0));
                        canvas2.drawText("Nombre: Sin comida", 280, 950, titlePaint2);
                    }

                    if(snapshot.child("users").child(userID).child("recetas").child("Lunes").child("B_Comida").exists()){
                        String lunesIngredientes = snapshot.child("users").child(userID).child("recetas").child("Lunes").child("B_Comida").child("f_ingredientes").getValue().toString();

                        titlePaint2.setTextAlign(Paint.Align.LEFT);
                        titlePaint2.setTypeface(Typeface.create(Typeface.DEFAULT, Typeface.NORMAL));
                        titlePaint2.setTextSize(25);
                        titlePaint2.setColor(Color.rgb(0,0,0));
                        canvas2.drawText("Ingredientes:", 100, 1010, titlePaint2);

                        imprimirIngredientes(100, 1040, lunesIngredientes, titlePaint2, canvas2);

                    }

                    if(snapshot.child("users").child(userID).child("recetas").child("Lunes").child("B_Comida").exists()){
                        String lunesCalorias = snapshot.child("users").child(userID).child("recetas").child("Lunes").child("B_Comida").child("f_calorias").getValue().toString();
                        titlePaint2.setTextAlign(Paint.Align.LEFT);
                        titlePaint2.setTypeface(Typeface.create(Typeface.DEFAULT, Typeface.NORMAL));
                        titlePaint2.setTextSize(20);
                        titlePaint2.setColor(Color.rgb(0,0,0));
                        canvas2.drawText("Contenido calórico aproximado: " + lunesCalorias, 100, 1210, titlePaint2);
                    }

                    //Cena
                    titlePaint2.setTextAlign(Paint.Align.CENTER);
                    titlePaint2.setTypeface(Typeface.create(Typeface.DEFAULT, Typeface.BOLD));
                    titlePaint2.setTextSize(30);
                    titlePaint2.setColor(Color.rgb(0,0,0));
                    canvas2.drawText("Tipo de alimento: Cena", pageWidth/2, 1320, titlePaint2);

                    if(snapshot.child("users").child(userID).child("recetas").child("Lunes").child("C_Cena").exists()){
                        String lunesCena = snapshot.child("users").child(userID).child("recetas").child("Lunes").child("C_Cena").child("f_nombrecomida").getValue().toString();
                        titlePaint2.setTextAlign(Paint.Align.LEFT);
                        titlePaint2.setTypeface(Typeface.create(Typeface.DEFAULT, Typeface.NORMAL));
                        titlePaint2.setTextSize(25);
                        titlePaint2.setColor(Color.rgb(0,0,0));
                        canvas2.drawText("Nombre: " + lunesCena, 100, 1400, titlePaint2);
                    } else {
                        titlePaint2.setTextAlign(Paint.Align.LEFT);
                        titlePaint2.setTypeface(Typeface.create(Typeface.DEFAULT, Typeface.NORMAL));
                        titlePaint2.setTextSize(25);
                        titlePaint2.setColor(Color.rgb(0,0,0));
                        canvas2.drawText("Nombre: Sin cena", 280, 1400, titlePaint2);
                    }

                    if(snapshot.child("users").child(userID).child("recetas").child("Lunes").child("C_Cena").exists()){
                        String lunesIngredientes = snapshot.child("users").child(userID).child("recetas").child("Lunes").child("C_Cena").child("f_ingredientes").getValue().toString();

                        titlePaint2.setTextAlign(Paint.Align.LEFT);
                        titlePaint2.setTypeface(Typeface.create(Typeface.DEFAULT, Typeface.NORMAL));
                        titlePaint2.setTextSize(25);
                        titlePaint2.setColor(Color.rgb(0,0,0));
                        canvas2.drawText("Ingredientes:", 100, 1450, titlePaint2);

                        imprimirIngredientes(100, 1480, lunesIngredientes, titlePaint2, canvas2);
                    }

                    if(snapshot.child("users").child(userID).child("recetas").child("Lunes").child("C_Cena").exists()){
                        String lunesCalorias = snapshot.child("users").child(userID).child("recetas").child("Lunes").child("C_Cena").child("f_calorias").getValue().toString();
                        titlePaint2.setTextAlign(Paint.Align.LEFT);
                        titlePaint2.setTypeface(Typeface.create(Typeface.DEFAULT, Typeface.NORMAL));
                        titlePaint2.setTextSize(20);
                        titlePaint2.setColor(Color.rgb(0,0,0));
                        canvas2.drawText("Contenido calórico aproximado: " + lunesCalorias, 100, 1660, titlePaint2);
                    }
                    /* ---------------- */

                    /* LOGO PARTE DE ABAJO DEL PDF */
                    canvas2.drawBitmap(scaledbmp, 515, 1800, myPaint2);
                    /* --------------------------- */

                    document.finishPage(page2);




                    /* INICIO DE TERCERA PAGINA (MARTES) */



                    PdfDocument.PageInfo pageInfo3 = new PdfDocument.PageInfo.Builder(1200, 2010, 1).create();
                    PdfDocument.Page page3 = document.startPage(pageInfo3);

                    Canvas canvas3 = page3.getCanvas();
                    Paint myPaint3 = new Paint();
                    Paint titlePaint3 = new Paint();

                    /* DATOS DEL NUTRIOLOGO */
                    titlePaint3.setTextAlign(Paint.Align.LEFT);
                    titlePaint3.setTextSize(20);
                    titlePaint3.setColor(Color.rgb(110, 184, 245));
                    canvas3.drawText("Dr.: " + nombre_nutri_pdf, 50, 75, titlePaint3);

                    titlePaint3.setTextAlign(Paint.Align.LEFT);
                    titlePaint3.setTextSize(20);
                    titlePaint3.setColor(Color.rgb(110, 184, 245));
                    canvas3.drawText(correo_nutri_pdf, 50, 110, titlePaint3);
                    /* -------------------- */

                    /* ALIMENTOS MARTES */
                    titlePaint3.setTextAlign(Paint.Align.CENTER);
                    titlePaint3.setTypeface(Typeface.create(Typeface.DEFAULT, Typeface.BOLD));
                    titlePaint3.setTextSize(45);
                    titlePaint3.setColor(Color.rgb(0,0,0));
                    canvas3.drawText("Martes", pageWidth/2, 260, titlePaint3);

                    titlePaint3.setStyle(Paint.Style.STROKE);
                    titlePaint3.setStrokeWidth(2);
                    // Tablas Desayuno, comida y cena
                    canvas3.drawRect(60, 380, 1140, 780, titlePaint3);
                    canvas3.drawLine(60, 440, 1140, 440, titlePaint3);

                    canvas3.drawRect(60, 830, 1140, 1230, titlePaint3);
                    canvas3.drawLine(60, 890, 1140, 890, titlePaint3);

                    canvas3.drawRect(60, 1280, 1140, 1680, titlePaint3);
                    canvas3.drawLine(60, 1340, 1140, 1340, titlePaint3);

                    titlePaint3.setStrokeWidth(0);
                    titlePaint3.setStyle(Paint.Style.FILL);

                    //Desayuno
                    titlePaint3.setTextAlign(Paint.Align.CENTER);
                    titlePaint3.setTypeface(Typeface.create(Typeface.DEFAULT, Typeface.BOLD));
                    titlePaint3.setTextSize(30);
                    titlePaint3.setColor(Color.rgb(0,0,0));
                    canvas3.drawText("Tipo de alimento: Desayuno", pageWidth/2, 420, titlePaint3);

                    //Martes d_NOMBRE Firebase
                    if(snapshot.child("users").child(userID).child("recetas").child("Martes").child("A_Desayuno").exists()){
                        String martesDesayuno = snapshot.child("users").child(userID).child("recetas").child("Martes").child("A_Desayuno").child("f_nombrecomida").getValue().toString();
                        titlePaint3.setTextAlign(Paint.Align.LEFT);
                        titlePaint3.setTypeface(Typeface.create(Typeface.DEFAULT, Typeface.NORMAL));
                        titlePaint3.setTextSize(25);
                        titlePaint3.setColor(Color.rgb(0,0,0));
                        canvas3.drawText("Nombre: " + martesDesayuno, 100, 500, titlePaint3);
                    } else {
                        titlePaint3.setTextAlign(Paint.Align.LEFT);
                        titlePaint3.setTypeface(Typeface.create(Typeface.DEFAULT, Typeface.NORMAL));
                        titlePaint3.setTextSize(25);
                        titlePaint3.setColor(Color.rgb(0,0,0));
                        canvas3.drawText("Nombre: Sin desayuno", 280, 500, titlePaint3);
                    }

                    if(snapshot.child("users").child(userID).child("recetas").child("Martes").child("A_Desayuno").exists()){
                        String martesIngredientes = snapshot.child("users").child(userID).child("recetas").child("Martes").child("A_Desayuno").child("f_ingredientes").getValue().toString();

                        titlePaint3.setTextAlign(Paint.Align.LEFT);
                        titlePaint3.setTypeface(Typeface.create(Typeface.DEFAULT, Typeface.NORMAL));
                        titlePaint3.setTextSize(25);
                        titlePaint3.setColor(Color.rgb(0,0,0));
                        canvas3.drawText("Ingredientes:", 100, 560, titlePaint3);

                        imprimirIngredientes(100, 590, martesIngredientes, titlePaint3, canvas3);

                    }

                    if(snapshot.child("users").child(userID).child("recetas").child("Martes").child("A_Desayuno").exists()){
                        String martesCalorias = snapshot.child("users").child(userID).child("recetas").child("Martes").child("A_Desayuno").child("f_calorias").getValue().toString();
                        titlePaint3.setTextAlign(Paint.Align.LEFT);
                        titlePaint3.setTypeface(Typeface.create(Typeface.DEFAULT, Typeface.NORMAL));
                        titlePaint3.setTextSize(20);
                        titlePaint3.setColor(Color.rgb(0,0,0));
                        canvas3.drawText("Contenido calórico aproximado: " + martesCalorias, 100, 760, titlePaint3);
                    }

                    //Comida
                    titlePaint3.setTextAlign(Paint.Align.CENTER);
                    titlePaint3.setTypeface(Typeface.create(Typeface.DEFAULT, Typeface.BOLD));
                    titlePaint3.setTextSize(30);
                    titlePaint3.setColor(Color.rgb(0,0,0));
                    canvas3.drawText("Tipo de alimento: Comida", pageWidth/2, 870, titlePaint3);

                    if(snapshot.child("users").child(userID).child("recetas").child("Martes").child("B_Comida").exists()){
                        String martesComida = snapshot.child("users").child(userID).child("recetas").child("Martes").child("B_Comida").child("f_nombrecomida").getValue().toString();
                        titlePaint3.setTextAlign(Paint.Align.LEFT);
                        titlePaint3.setTypeface(Typeface.create(Typeface.DEFAULT, Typeface.NORMAL));
                        titlePaint3.setTextSize(25);
                        titlePaint3.setColor(Color.rgb(0,0,0));
                        canvas3.drawText("Nombre: " + martesComida, 100, 950, titlePaint3);
                    } else {
                        titlePaint3.setTextAlign(Paint.Align.LEFT);
                        titlePaint3.setTypeface(Typeface.create(Typeface.DEFAULT, Typeface.NORMAL));
                        titlePaint3.setTextSize(25);
                        titlePaint3.setColor(Color.rgb(0,0,0));
                        canvas3.drawText("Nombre: Sin comida", 280, 950, titlePaint3);
                    }

                    if(snapshot.child("users").child(userID).child("recetas").child("Martes").child("B_Comida").exists()){
                        String martesIngredientes = snapshot.child("users").child(userID).child("recetas").child("Martes").child("B_Comida").child("f_ingredientes").getValue().toString();

                        titlePaint3.setTextAlign(Paint.Align.LEFT);
                        titlePaint3.setTypeface(Typeface.create(Typeface.DEFAULT, Typeface.NORMAL));
                        titlePaint3.setTextSize(25);
                        titlePaint3.setColor(Color.rgb(0,0,0));
                        canvas3.drawText("Ingredientes:", 100, 1010, titlePaint3);

                        imprimirIngredientes(100, 1040, martesIngredientes, titlePaint3, canvas3);

                    }

                    if(snapshot.child("users").child(userID).child("recetas").child("Martes").child("B_Comida").exists()){
                        String martesCalorias = snapshot.child("users").child(userID).child("recetas").child("Martes").child("B_Comida").child("f_calorias").getValue().toString();
                        titlePaint3.setTextAlign(Paint.Align.LEFT);
                        titlePaint3.setTypeface(Typeface.create(Typeface.DEFAULT, Typeface.NORMAL));
                        titlePaint3.setTextSize(20);
                        titlePaint3.setColor(Color.rgb(0,0,0));
                        canvas3.drawText("Contenido calórico aproximado: " + martesCalorias, 100, 1210, titlePaint3);
                    }

                    //Cena
                    titlePaint3.setTextAlign(Paint.Align.CENTER);
                    titlePaint3.setTypeface(Typeface.create(Typeface.DEFAULT, Typeface.BOLD));
                    titlePaint3.setTextSize(30);
                    titlePaint3.setColor(Color.rgb(0,0,0));
                    canvas3.drawText("Tipo de alimento: Cena", pageWidth/2, 1320, titlePaint3);

                    if(snapshot.child("users").child(userID).child("recetas").child("Martes").child("C_Cena").exists()){
                        String martesCena = snapshot.child("users").child(userID).child("recetas").child("Martes").child("C_Cena").child("f_nombrecomida").getValue().toString();
                        titlePaint3.setTextAlign(Paint.Align.LEFT);
                        titlePaint3.setTypeface(Typeface.create(Typeface.DEFAULT, Typeface.NORMAL));
                        titlePaint3.setTextSize(25);
                        titlePaint3.setColor(Color.rgb(0,0,0));
                        canvas3.drawText("Nombre: " + martesCena, 100, 1400, titlePaint3);
                    } else {
                        titlePaint3.setTextAlign(Paint.Align.LEFT);
                        titlePaint3.setTypeface(Typeface.create(Typeface.DEFAULT, Typeface.NORMAL));
                        titlePaint3.setTextSize(25);
                        titlePaint3.setColor(Color.rgb(0,0,0));
                        canvas3.drawText("Nombre: Sin cena", 280, 1400, titlePaint3);
                    }

                    if(snapshot.child("users").child(userID).child("recetas").child("Martes").child("C_Cena").exists()){
                        String martesIngredientes = snapshot.child("users").child(userID).child("recetas").child("Martes").child("C_Cena").child("f_ingredientes").getValue().toString();

                        titlePaint3.setTextAlign(Paint.Align.LEFT);
                        titlePaint3.setTypeface(Typeface.create(Typeface.DEFAULT, Typeface.NORMAL));
                        titlePaint3.setTextSize(25);
                        titlePaint3.setColor(Color.rgb(0,0,0));
                        canvas3.drawText("Ingredientes:", 100, 1450, titlePaint3);

                        imprimirIngredientes(100, 1480, martesIngredientes, titlePaint3, canvas3);
                    }

                    if(snapshot.child("users").child(userID).child("recetas").child("Martes").child("C_Cena").exists()){
                        String martesCalorias = snapshot.child("users").child(userID).child("recetas").child("Martes").child("C_Cena").child("f_calorias").getValue().toString();
                        titlePaint3.setTextAlign(Paint.Align.LEFT);
                        titlePaint3.setTypeface(Typeface.create(Typeface.DEFAULT, Typeface.NORMAL));
                        titlePaint3.setTextSize(20);
                        titlePaint3.setColor(Color.rgb(0,0,0));
                        canvas3.drawText("Contenido calórico aproximado: " + martesCalorias, 100, 1660, titlePaint3);
                    }
                    /* ---------------- */

                    /* LOGO PARTE DE ABAJO DEL PDF */
                    canvas3.drawBitmap(scaledbmp, 515, 1800, myPaint3);
                    /* --------------------------- */

                    document.finishPage(page3);
                    /* BORRAR DESDE AQUÍ */


                    /* INICIO DE CUARTA PAGINA (MIERCOLES) */



                    PdfDocument.PageInfo pageInfo4 = new PdfDocument.PageInfo.Builder(1200, 2010, 1).create();
                    PdfDocument.Page page4 = document.startPage(pageInfo4);

                    Canvas canvas4 = page4.getCanvas();
                    Paint myPaint4 = new Paint();
                    Paint titlePaint4 = new Paint();

                    /* DATOS DEL NUTRIOLOGO */
                    titlePaint4.setTextAlign(Paint.Align.LEFT);
                    titlePaint4.setTextSize(20);
                    titlePaint4.setColor(Color.rgb(110, 184, 245));
                    canvas4.drawText("Dr.: " + nombre_nutri_pdf, 50, 75, titlePaint4);

                    titlePaint4.setTextAlign(Paint.Align.LEFT);
                    titlePaint4.setTextSize(20);
                    titlePaint4.setColor(Color.rgb(110, 184, 245));
                    canvas4.drawText(correo_nutri_pdf, 50, 110, titlePaint4);
                    /* -------------------- */

                    /* ALIMENTOS MIERCOLES */
                    titlePaint4.setTextAlign(Paint.Align.CENTER);
                    titlePaint4.setTypeface(Typeface.create(Typeface.DEFAULT, Typeface.BOLD));
                    titlePaint4.setTextSize(45);
                    titlePaint4.setColor(Color.rgb(0,0,0));
                    canvas4.drawText("Miércoles", pageWidth/2, 260, titlePaint4);

                    titlePaint4.setStyle(Paint.Style.STROKE);
                    titlePaint4.setStrokeWidth(2);

                    // Tablas Desayuno, comida y cena
                    canvas4.drawRect(60, 380, 1140, 780, titlePaint4);
                    canvas4.drawLine(60, 440, 1140, 440, titlePaint4);

                    canvas4.drawRect(60, 830, 1140, 1230, titlePaint4);
                    canvas4.drawLine(60, 890, 1140, 890, titlePaint4);

                    canvas4.drawRect(60, 1280, 1140, 1680, titlePaint4);
                    canvas4.drawLine(60, 1340, 1140, 1340, titlePaint4);

                    titlePaint4.setStrokeWidth(0);
                    titlePaint4.setStyle(Paint.Style.FILL);

                    //Desayuno
                    titlePaint4.setTextAlign(Paint.Align.CENTER);
                    titlePaint4.setTypeface(Typeface.create(Typeface.DEFAULT, Typeface.BOLD));
                    titlePaint4.setTextSize(30);
                    titlePaint4.setColor(Color.rgb(0,0,0));
                    canvas4.drawText("Tipo de alimento: Desayuno", pageWidth/2, 420, titlePaint4);

                    //Miercoles d_NOMBRE Firebase
                    if(snapshot.child("users").child(userID).child("recetas").child("Miercoles").child("A_Desayuno").exists()){
                        String miercolesDesayuno = snapshot.child("users").child(userID).child("recetas").child("Miercoles").child("A_Desayuno").child("f_nombrecomida").getValue().toString();
                        titlePaint4.setTextAlign(Paint.Align.LEFT);
                        titlePaint4.setTypeface(Typeface.create(Typeface.DEFAULT, Typeface.NORMAL));
                        titlePaint4.setTextSize(25);
                        titlePaint4.setColor(Color.rgb(0,0,0));
                        canvas4.drawText("Nombre: " + miercolesDesayuno, 100, 500, titlePaint4);
                    } else {
                        titlePaint4.setTextAlign(Paint.Align.LEFT);
                        titlePaint4.setTypeface(Typeface.create(Typeface.DEFAULT, Typeface.NORMAL));
                        titlePaint4.setTextSize(25);
                        titlePaint4.setColor(Color.rgb(0,0,0));
                        canvas4.drawText("Nombre: Sin desayuno", 280, 500, titlePaint4);
                    }

                    if(snapshot.child("users").child(userID).child("recetas").child("Miercoles").child("A_Desayuno").exists()){
                        String miercolesIngredientes = snapshot.child("users").child(userID).child("recetas").child("Miercoles").child("A_Desayuno").child("f_ingredientes").getValue().toString();

                        titlePaint4.setTextAlign(Paint.Align.LEFT);
                        titlePaint4.setTypeface(Typeface.create(Typeface.DEFAULT, Typeface.NORMAL));
                        titlePaint4.setTextSize(25);
                        titlePaint4.setColor(Color.rgb(0,0,0));
                        canvas4.drawText("Ingredientes:", 100, 560, titlePaint4);

                        imprimirIngredientes(100, 590, miercolesIngredientes, titlePaint4, canvas4);
                    }

                    if(snapshot.child("users").child(userID).child("recetas").child("Miercoles").child("A_Desayuno").exists()){
                        String miercolesCalorias = snapshot.child("users").child(userID).child("recetas").child("Miercoles").child("A_Desayuno").child("f_calorias").getValue().toString();
                        titlePaint4.setTextAlign(Paint.Align.LEFT);
                        titlePaint4.setTypeface(Typeface.create(Typeface.DEFAULT, Typeface.NORMAL));
                        titlePaint4.setTextSize(20);
                        titlePaint4.setColor(Color.rgb(0,0,0));
                        canvas4.drawText("Contenido calórico aproximado: " + miercolesCalorias, 100, 760, titlePaint4);
                    }

                    //Comida
                    titlePaint4.setTextAlign(Paint.Align.CENTER);
                    titlePaint4.setTypeface(Typeface.create(Typeface.DEFAULT, Typeface.BOLD));
                    titlePaint4.setTextSize(30);
                    titlePaint4.setColor(Color.rgb(0,0,0));
                    canvas4.drawText("Tipo de alimento: Comida", pageWidth/2, 870, titlePaint4);

                    if(snapshot.child("users").child(userID).child("recetas").child("Miercoles").child("B_Comida").exists()){
                        String miercolesComida = snapshot.child("users").child(userID).child("recetas").child("Miercoles").child("B_Comida").child("f_nombrecomida").getValue().toString();
                        titlePaint4.setTextAlign(Paint.Align.LEFT);
                        titlePaint4.setTypeface(Typeface.create(Typeface.DEFAULT, Typeface.NORMAL));
                        titlePaint4.setTextSize(25);
                        titlePaint4.setColor(Color.rgb(0,0,0));
                        canvas4.drawText("Nombre: " + miercolesComida, 100, 950, titlePaint4);
                    } else {
                        titlePaint4.setTextAlign(Paint.Align.LEFT);
                        titlePaint4.setTypeface(Typeface.create(Typeface.DEFAULT, Typeface.NORMAL));
                        titlePaint4.setTextSize(25);
                        titlePaint4.setColor(Color.rgb(0,0,0));
                        canvas4.drawText("Nombre: Sin comida", 280, 950, titlePaint4);
                    }

                    if(snapshot.child("users").child(userID).child("recetas").child("Miercoles").child("B_Comida").exists()){
                        String miercolesIngredientes = snapshot.child("users").child(userID).child("recetas").child("Miercoles").child("B_Comida").child("f_ingredientes").getValue().toString();

                        titlePaint4.setTextAlign(Paint.Align.LEFT);
                        titlePaint4.setTypeface(Typeface.create(Typeface.DEFAULT, Typeface.NORMAL));
                        titlePaint4.setTextSize(25);
                        titlePaint4.setColor(Color.rgb(0,0,0));
                        canvas4.drawText("Ingredientes:", 100, 1010, titlePaint4);

                        imprimirIngredientes(100, 1040, miercolesIngredientes, titlePaint4, canvas4);

                    }

                    if(snapshot.child("users").child(userID).child("recetas").child("Miercoles").child("B_Comida").exists()){
                        String miercolesCalorias = snapshot.child("users").child(userID).child("recetas").child("Miercoles").child("B_Comida").child("f_calorias").getValue().toString();
                        titlePaint4.setTextAlign(Paint.Align.LEFT);
                        titlePaint4.setTypeface(Typeface.create(Typeface.DEFAULT, Typeface.NORMAL));
                        titlePaint4.setTextSize(20);
                        titlePaint4.setColor(Color.rgb(0,0,0));
                        canvas4.drawText("Contenido calórico aproximado: " + miercolesCalorias, 100, 1210, titlePaint4);
                    }

                    //Cena
                    titlePaint4.setTextAlign(Paint.Align.CENTER);
                    titlePaint4.setTypeface(Typeface.create(Typeface.DEFAULT, Typeface.BOLD));
                    titlePaint4.setTextSize(30);
                    titlePaint4.setColor(Color.rgb(0,0,0));
                    canvas4.drawText("Tipo de alimento: Cena", pageWidth/2, 1320, titlePaint4);

                    if(snapshot.child("users").child(userID).child("recetas").child("Miercoles").child("C_Cena").exists()){
                        String miercolesCena = snapshot.child("users").child(userID).child("recetas").child("Miercoles").child("C_Cena").child("f_nombrecomida").getValue().toString();
                        titlePaint4.setTextAlign(Paint.Align.LEFT);
                        titlePaint4.setTypeface(Typeface.create(Typeface.DEFAULT, Typeface.NORMAL));
                        titlePaint4.setTextSize(25);
                        titlePaint4.setColor(Color.rgb(0,0,0));
                        canvas4.drawText("Nombre: " + miercolesCena, 100, 1400, titlePaint4);
                    } else {
                        titlePaint4.setTextAlign(Paint.Align.LEFT);
                        titlePaint4.setTypeface(Typeface.create(Typeface.DEFAULT, Typeface.NORMAL));
                        titlePaint4.setTextSize(25);
                        titlePaint4.setColor(Color.rgb(0,0,0));
                        canvas4.drawText("Nombre: Sin cena", 280, 1400, titlePaint4);
                    }

                    if(snapshot.child("users").child(userID).child("recetas").child("Miercoles").child("C_Cena").exists()){
                        String miercolesIngredientes = snapshot.child("users").child(userID).child("recetas").child("Miercoles").child("C_Cena").child("f_ingredientes").getValue().toString();

                        titlePaint4.setTextAlign(Paint.Align.LEFT);
                        titlePaint4.setTypeface(Typeface.create(Typeface.DEFAULT, Typeface.NORMAL));
                        titlePaint4.setTextSize(25);
                        titlePaint4.setColor(Color.rgb(0,0,0));
                        canvas4.drawText("Ingredientes:", 100, 1450, titlePaint4);

                        imprimirIngredientes(100, 1480, miercolesIngredientes, titlePaint4, canvas4);
                    }

                    if(snapshot.child("users").child(userID).child("recetas").child("Miercoles").child("C_Cena").exists()){
                        String miercolesCalorias = snapshot.child("users").child(userID).child("recetas").child("Miercoles").child("C_Cena").child("f_calorias").getValue().toString();
                        titlePaint4.setTextAlign(Paint.Align.LEFT);
                        titlePaint4.setTypeface(Typeface.create(Typeface.DEFAULT, Typeface.NORMAL));
                        titlePaint4.setTextSize(20);
                        titlePaint4.setColor(Color.rgb(0,0,0));
                        canvas4.drawText("Contenido calórico aproximado: " + miercolesCalorias, 100, 1660, titlePaint4);
                    }
                    /* ---------------- */

                    /* LOGO PARTE DE ABAJO DEL PDF */
                    canvas4.drawBitmap(scaledbmp, 515, 1800, myPaint4);
                    /* --------------------------- */

                    document.finishPage(page4);

                    /* INICIO DE QUINTA PAGINA (JUEVES) */



                    PdfDocument.PageInfo pageInfo5 = new PdfDocument.PageInfo.Builder(1200, 2010, 1).create();
                    PdfDocument.Page page5 = document.startPage(pageInfo5);

                    Canvas canvas5 = page5.getCanvas();
                    Paint myPaint5 = new Paint();
                    Paint titlePaint5 = new Paint();

                    /* DATOS DEL NUTRIOLOGO */
                    titlePaint5.setTextAlign(Paint.Align.LEFT);
                    titlePaint5.setTextSize(20);
                    titlePaint5.setColor(Color.rgb(110, 184, 245));
                    canvas5.drawText("Dr.: " + nombre_nutri_pdf, 50, 75, titlePaint5);

                    titlePaint5.setTextAlign(Paint.Align.LEFT);
                    titlePaint5.setTextSize(20);
                    titlePaint5.setColor(Color.rgb(110, 184, 245));
                    canvas5.drawText(correo_nutri_pdf, 50, 110, titlePaint5);
                    /* -------------------- */

                    /* ALIMENTOS JUEVES */
                    titlePaint5.setTextAlign(Paint.Align.CENTER);
                    titlePaint5.setTypeface(Typeface.create(Typeface.DEFAULT, Typeface.BOLD));
                    titlePaint5.setTextSize(45);
                    titlePaint5.setColor(Color.rgb(0,0,0));
                    canvas5.drawText("Jueves", pageWidth/2, 260, titlePaint5);

                    titlePaint5.setStyle(Paint.Style.STROKE);
                    titlePaint5.setStrokeWidth(2);

                    // Tablas Desayuno, comida y cena
                    canvas5.drawRect(60, 380, 1140, 780, titlePaint5);
                    canvas5.drawLine(60, 440, 1140, 440, titlePaint5);

                    canvas5.drawRect(60, 830, 1140, 1230, titlePaint5);
                    canvas5.drawLine(60, 890, 1140, 890, titlePaint5);

                    canvas5.drawRect(60, 1280, 1140, 1680, titlePaint5);
                    canvas5.drawLine(60, 1340, 1140, 1340, titlePaint5);

                    titlePaint5.setStrokeWidth(0);
                    titlePaint5.setStyle(Paint.Style.FILL);

                    //Desayuno
                    titlePaint5.setTextAlign(Paint.Align.CENTER);
                    titlePaint5.setTypeface(Typeface.create(Typeface.DEFAULT, Typeface.BOLD));
                    titlePaint5.setTextSize(30);
                    titlePaint5.setColor(Color.rgb(0,0,0));
                    canvas5.drawText("Tipo de alimento: Desayuno", pageWidth/2, 420, titlePaint5);

                    //Jueves d_NOMBRE Firebase
                    if(snapshot.child("users").child(userID).child("recetas").child("Jueves").child("A_Desayuno").exists()){
                        String juevesDesayuno = snapshot.child("users").child(userID).child("recetas").child("Jueves").child("A_Desayuno").child("f_nombrecomida").getValue().toString();
                        titlePaint5.setTextAlign(Paint.Align.LEFT);
                        titlePaint5.setTypeface(Typeface.create(Typeface.DEFAULT, Typeface.NORMAL));
                        titlePaint5.setTextSize(25);
                        titlePaint5.setColor(Color.rgb(0,0,0));
                        canvas5.drawText("Nombre: " + juevesDesayuno, 100, 500, titlePaint5);
                    } else {
                        titlePaint5.setTextAlign(Paint.Align.LEFT);
                        titlePaint5.setTypeface(Typeface.create(Typeface.DEFAULT, Typeface.NORMAL));
                        titlePaint5.setTextSize(25);
                        titlePaint5.setColor(Color.rgb(0,0,0));
                        canvas5.drawText("Nombre: Sin desayuno", 280, 500, titlePaint5);
                    }

                    if(snapshot.child("users").child(userID).child("recetas").child("Jueves").child("A_Desayuno").exists()){
                        String juevesIngredientes = snapshot.child("users").child(userID).child("recetas").child("Jueves").child("A_Desayuno").child("f_ingredientes").getValue().toString();

                        titlePaint5.setTextAlign(Paint.Align.LEFT);
                        titlePaint5.setTypeface(Typeface.create(Typeface.DEFAULT, Typeface.NORMAL));
                        titlePaint5.setTextSize(25);
                        titlePaint5.setColor(Color.rgb(0,0,0));
                        canvas5.drawText("Ingredientes:", 100, 560, titlePaint4);

                        imprimirIngredientes(100, 590, juevesIngredientes, titlePaint5, canvas5);
                    }

                    if(snapshot.child("users").child(userID).child("recetas").child("Jueves").child("A_Desayuno").exists()){
                        String juevesCalorias = snapshot.child("users").child(userID).child("recetas").child("Jueves").child("A_Desayuno").child("f_calorias").getValue().toString();
                        titlePaint5.setTextAlign(Paint.Align.LEFT);
                        titlePaint5.setTypeface(Typeface.create(Typeface.DEFAULT, Typeface.NORMAL));
                        titlePaint5.setTextSize(20);
                        titlePaint5.setColor(Color.rgb(0,0,0));
                        canvas5.drawText("Contenido calórico aproximado: " + juevesCalorias, 100, 760, titlePaint5);
                    }

                    //Comida
                    titlePaint5.setTextAlign(Paint.Align.CENTER);
                    titlePaint5.setTypeface(Typeface.create(Typeface.DEFAULT, Typeface.BOLD));
                    titlePaint5.setTextSize(30);
                    titlePaint5.setColor(Color.rgb(0,0,0));
                    canvas5.drawText("Tipo de alimento: Comida", pageWidth/2, 870, titlePaint5);

                    if(snapshot.child("users").child(userID).child("recetas").child("Jueves").child("B_Comida").exists()){
                        String juevesComida = snapshot.child("users").child(userID).child("recetas").child("Jueves").child("B_Comida").child("f_nombrecomida").getValue().toString();
                        titlePaint5.setTextAlign(Paint.Align.LEFT);
                        titlePaint5.setTypeface(Typeface.create(Typeface.DEFAULT, Typeface.NORMAL));
                        titlePaint5.setTextSize(25);
                        titlePaint5.setColor(Color.rgb(0,0,0));
                        canvas5.drawText("Nombre: " + juevesComida, 100, 950, titlePaint5);
                    } else {
                        titlePaint5.setTextAlign(Paint.Align.LEFT);
                        titlePaint5.setTypeface(Typeface.create(Typeface.DEFAULT, Typeface.NORMAL));
                        titlePaint5.setTextSize(25);
                        titlePaint5.setColor(Color.rgb(0,0,0));
                        canvas5.drawText("Nombre: Sin comida", 280, 950, titlePaint5);
                    }

                    if(snapshot.child("users").child(userID).child("recetas").child("Jueves").child("B_Comida").exists()){
                        String juevesIngredientes = snapshot.child("users").child(userID).child("recetas").child("Jueves").child("B_Comida").child("f_ingredientes").getValue().toString();

                        titlePaint5.setTextAlign(Paint.Align.LEFT);
                        titlePaint5.setTypeface(Typeface.create(Typeface.DEFAULT, Typeface.NORMAL));
                        titlePaint5.setTextSize(25);
                        titlePaint5.setColor(Color.rgb(0,0,0));
                        canvas5.drawText("Ingredientes:", 100, 1010, titlePaint5);

                        imprimirIngredientes(100, 1040, juevesIngredientes, titlePaint5, canvas5);

                    }

                    if(snapshot.child("users").child(userID).child("recetas").child("Jueves").child("B_Comida").exists()){
                        String juevesCalorias = snapshot.child("users").child(userID).child("recetas").child("Jueves").child("B_Comida").child("f_calorias").getValue().toString();
                        titlePaint5.setTextAlign(Paint.Align.LEFT);
                        titlePaint5.setTypeface(Typeface.create(Typeface.DEFAULT, Typeface.NORMAL));
                        titlePaint5.setTextSize(20);
                        titlePaint5.setColor(Color.rgb(0,0,0));
                        canvas5.drawText("Contenido calórico aproximado: " + juevesCalorias, 100, 1210, titlePaint5);
                    }

                    //Cena
                    titlePaint5.setTextAlign(Paint.Align.CENTER);
                    titlePaint5.setTypeface(Typeface.create(Typeface.DEFAULT, Typeface.BOLD));
                    titlePaint5.setTextSize(30);
                    titlePaint5.setColor(Color.rgb(0,0,0));
                    canvas5.drawText("Tipo de alimento: Cena", pageWidth/2, 1320, titlePaint5);

                    if(snapshot.child("users").child(userID).child("recetas").child("Jueves").child("C_Cena").exists()){
                        String juevesCena = snapshot.child("users").child(userID).child("recetas").child("Jueves").child("C_Cena").child("f_nombrecomida").getValue().toString();
                        titlePaint5.setTextAlign(Paint.Align.LEFT);
                        titlePaint5.setTypeface(Typeface.create(Typeface.DEFAULT, Typeface.NORMAL));
                        titlePaint5.setTextSize(25);
                        titlePaint5.setColor(Color.rgb(0,0,0));
                        canvas5.drawText("Nombre: " + juevesCena, 100, 1400, titlePaint5);
                    } else {
                        titlePaint5.setTextAlign(Paint.Align.LEFT);
                        titlePaint5.setTypeface(Typeface.create(Typeface.DEFAULT, Typeface.NORMAL));
                        titlePaint5.setTextSize(25);
                        titlePaint5.setColor(Color.rgb(0,0,0));
                        canvas5.drawText("Nombre: Sin cena", 280, 1400, titlePaint5);
                    }

                    if(snapshot.child("users").child(userID).child("recetas").child("Jueves").child("C_Cena").exists()){
                        String juevesIngredientes = snapshot.child("users").child(userID).child("recetas").child("Jueves").child("C_Cena").child("f_ingredientes").getValue().toString();

                        titlePaint5.setTextAlign(Paint.Align.LEFT);
                        titlePaint5.setTypeface(Typeface.create(Typeface.DEFAULT, Typeface.NORMAL));
                        titlePaint5.setTextSize(25);
                        titlePaint5.setColor(Color.rgb(0,0,0));
                        canvas5.drawText("Ingredientes:", 100, 1450, titlePaint5);

                        imprimirIngredientes(100, 1480, juevesIngredientes, titlePaint5, canvas5);
                    }

                    if(snapshot.child("users").child(userID).child("recetas").child("Miercoles").child("C_Cena").exists()){
                        String juevesCalorias = snapshot.child("users").child(userID).child("recetas").child("Miercoles").child("C_Cena").child("f_calorias").getValue().toString();
                        titlePaint5.setTextAlign(Paint.Align.LEFT);
                        titlePaint5.setTypeface(Typeface.create(Typeface.DEFAULT, Typeface.NORMAL));
                        titlePaint5.setTextSize(20);
                        titlePaint5.setColor(Color.rgb(0,0,0));
                        canvas5.drawText("Contenido calórico aproximado: " + juevesCalorias, 100, 1660, titlePaint5);
                    }
                    /* ---------------- */

                    /* LOGO PARTE DE ABAJO DEL PDF */
                    canvas5.drawBitmap(scaledbmp, 515, 1800, myPaint5);
                    /* --------------------------- */

                    document.finishPage(page5);

                    /* INICIO DE SEXTA PAGINA (VIERNES) */



                    PdfDocument.PageInfo pageInfo6 = new PdfDocument.PageInfo.Builder(1200, 2010, 1).create();
                    PdfDocument.Page page6 = document.startPage(pageInfo6);

                    Canvas canvas6 = page6.getCanvas();
                    Paint myPaint6 = new Paint();
                    Paint titlePaint6 = new Paint();

                    /* DATOS DEL NUTRIOLOGO */
                    titlePaint6.setTextAlign(Paint.Align.LEFT);
                    titlePaint6.setTextSize(20);
                    titlePaint6.setColor(Color.rgb(110, 184, 245));
                    canvas6.drawText("Dr.: " + nombre_nutri_pdf, 50, 75, titlePaint6);

                    titlePaint6.setTextAlign(Paint.Align.LEFT);
                    titlePaint6.setTextSize(20);
                    titlePaint6.setColor(Color.rgb(110, 184, 245));
                    canvas6.drawText(correo_nutri_pdf, 50, 110, titlePaint6);
                    /* -------------------- */

                    /* ALIMENTOS VIERNES */
                    titlePaint6.setTextAlign(Paint.Align.CENTER);
                    titlePaint6.setTypeface(Typeface.create(Typeface.DEFAULT, Typeface.BOLD));
                    titlePaint6.setTextSize(45);
                    titlePaint6.setColor(Color.rgb(0,0,0));
                    canvas6.drawText("Viernes", pageWidth/2, 260, titlePaint6);

                    titlePaint6.setStyle(Paint.Style.STROKE);
                    titlePaint6.setStrokeWidth(2);

                    // Tablas Desayuno, comida y cena
                    canvas6.drawRect(60, 380, 1140, 780, titlePaint6);
                    canvas6.drawLine(60, 440, 1140, 440, titlePaint6);

                    canvas6.drawRect(60, 830, 1140, 1230, titlePaint6);
                    canvas6.drawLine(60, 890, 1140, 890, titlePaint6);

                    canvas6.drawRect(60, 1280, 1140, 1680, titlePaint6);
                    canvas6.drawLine(60, 1340, 1140, 1340, titlePaint6);

                    titlePaint6.setStrokeWidth(0);
                    titlePaint6.setStyle(Paint.Style.FILL);

                    //Desayuno
                    titlePaint6.setTextAlign(Paint.Align.CENTER);
                    titlePaint6.setTypeface(Typeface.create(Typeface.DEFAULT, Typeface.BOLD));
                    titlePaint6.setTextSize(30);
                    titlePaint6.setColor(Color.rgb(0,0,0));
                    canvas6.drawText("Tipo de alimento: Desayuno", pageWidth/2, 420, titlePaint6);

                    //Jueves d_NOMBRE Firebase
                    if(snapshot.child("users").child(userID).child("recetas").child("Viernes").child("A_Desayuno").exists()){
                        String viernesDesayuno = snapshot.child("users").child(userID).child("recetas").child("Viernes").child("A_Desayuno").child("f_nombrecomida").getValue().toString();
                        titlePaint6.setTextAlign(Paint.Align.LEFT);
                        titlePaint6.setTypeface(Typeface.create(Typeface.DEFAULT, Typeface.NORMAL));
                        titlePaint6.setTextSize(25);
                        titlePaint6.setColor(Color.rgb(0,0,0));
                        canvas6.drawText("Nombre: " + viernesDesayuno, 100, 500, titlePaint6);
                    } else {
                        titlePaint6.setTextAlign(Paint.Align.LEFT);
                        titlePaint6.setTypeface(Typeface.create(Typeface.DEFAULT, Typeface.NORMAL));
                        titlePaint6.setTextSize(25);
                        titlePaint6.setColor(Color.rgb(0,0,0));
                        canvas6.drawText("Nombre: Sin desayuno", 280, 500, titlePaint6);
                    }

                    if(snapshot.child("users").child(userID).child("recetas").child("Viernes").child("A_Desayuno").exists()){
                        String viernesIngredientes = snapshot.child("users").child(userID).child("recetas").child("Viernes").child("A_Desayuno").child("f_ingredientes").getValue().toString();

                        titlePaint6.setTextAlign(Paint.Align.LEFT);
                        titlePaint6.setTypeface(Typeface.create(Typeface.DEFAULT, Typeface.NORMAL));
                        titlePaint6.setTextSize(25);
                        titlePaint6.setColor(Color.rgb(0,0,0));
                        canvas6.drawText("Ingredientes:", 100, 560, titlePaint4);

                        imprimirIngredientes(100, 590, viernesIngredientes, titlePaint6, canvas6);
                    }

                    if(snapshot.child("users").child(userID).child("recetas").child("Viernes").child("A_Desayuno").exists()){
                        String viernesCalorias = snapshot.child("users").child(userID).child("recetas").child("Viernes").child("A_Desayuno").child("f_calorias").getValue().toString();
                        titlePaint6.setTextAlign(Paint.Align.LEFT);
                        titlePaint6.setTypeface(Typeface.create(Typeface.DEFAULT, Typeface.NORMAL));
                        titlePaint6.setTextSize(20);
                        titlePaint6.setColor(Color.rgb(0,0,0));
                        canvas6.drawText("Contenido calórico aproximado: " + viernesCalorias, 100, 760, titlePaint6);
                    }

                    //Comida
                    titlePaint6.setTextAlign(Paint.Align.CENTER);
                    titlePaint6.setTypeface(Typeface.create(Typeface.DEFAULT, Typeface.BOLD));
                    titlePaint6.setTextSize(30);
                    titlePaint6.setColor(Color.rgb(0,0,0));
                    canvas6.drawText("Tipo de alimento: Comida", pageWidth/2, 870, titlePaint6);

                    if(snapshot.child("users").child(userID).child("recetas").child("Viernes").child("B_Comida").exists()){
                        String viernesComida = snapshot.child("users").child(userID).child("recetas").child("Viernes").child("B_Comida").child("f_nombrecomida").getValue().toString();
                        titlePaint6.setTextAlign(Paint.Align.LEFT);
                        titlePaint6.setTypeface(Typeface.create(Typeface.DEFAULT, Typeface.NORMAL));
                        titlePaint6.setTextSize(25);
                        titlePaint6.setColor(Color.rgb(0,0,0));
                        canvas6.drawText("Nombre: " + viernesComida, 100, 950, titlePaint6);
                    } else {
                        titlePaint6.setTextAlign(Paint.Align.LEFT);
                        titlePaint6.setTypeface(Typeface.create(Typeface.DEFAULT, Typeface.NORMAL));
                        titlePaint6.setTextSize(25);
                        titlePaint6.setColor(Color.rgb(0,0,0));
                        canvas6.drawText("Nombre: Sin comida", 280, 950, titlePaint6);
                    }

                    if(snapshot.child("users").child(userID).child("recetas").child("Viernes").child("B_Comida").exists()){
                        String viernesIngredientes = snapshot.child("users").child(userID).child("recetas").child("Viernes").child("B_Comida").child("f_ingredientes").getValue().toString();

                        titlePaint6.setTextAlign(Paint.Align.LEFT);
                        titlePaint6.setTypeface(Typeface.create(Typeface.DEFAULT, Typeface.NORMAL));
                        titlePaint6.setTextSize(25);
                        titlePaint6.setColor(Color.rgb(0,0,0));
                        canvas6.drawText("Ingredientes:", 100, 1010, titlePaint6);

                        imprimirIngredientes(100, 1040, viernesIngredientes, titlePaint6, canvas6);

                    }

                    if(snapshot.child("users").child(userID).child("recetas").child("Viernes").child("B_Comida").exists()){
                        String viernesCalorias = snapshot.child("users").child(userID).child("recetas").child("Viernes").child("B_Comida").child("f_calorias").getValue().toString();
                        titlePaint6.setTextAlign(Paint.Align.LEFT);
                        titlePaint6.setTypeface(Typeface.create(Typeface.DEFAULT, Typeface.NORMAL));
                        titlePaint6.setTextSize(20);
                        titlePaint6.setColor(Color.rgb(0,0,0));
                        canvas6.drawText("Contenido calórico aproximado: " + viernesCalorias, 100, 1210, titlePaint6);
                    }

                    //Cena
                    titlePaint6.setTextAlign(Paint.Align.CENTER);
                    titlePaint6.setTypeface(Typeface.create(Typeface.DEFAULT, Typeface.BOLD));
                    titlePaint6.setTextSize(30);
                    titlePaint6.setColor(Color.rgb(0,0,0));
                    canvas6.drawText("Tipo de alimento: Cena", pageWidth/2, 1320, titlePaint6);

                    if(snapshot.child("users").child(userID).child("recetas").child("Viernes").child("C_Cena").exists()){
                        String viernesCena = snapshot.child("users").child(userID).child("recetas").child("Viernes").child("C_Cena").child("f_nombrecomida").getValue().toString();
                        titlePaint6.setTextAlign(Paint.Align.LEFT);
                        titlePaint6.setTypeface(Typeface.create(Typeface.DEFAULT, Typeface.NORMAL));
                        titlePaint6.setTextSize(25);
                        titlePaint6.setColor(Color.rgb(0,0,0));
                        canvas6.drawText("Nombre: " + viernesCena, 100, 1400, titlePaint6);
                    } else {
                        titlePaint6.setTextAlign(Paint.Align.LEFT);
                        titlePaint6.setTypeface(Typeface.create(Typeface.DEFAULT, Typeface.NORMAL));
                        titlePaint6.setTextSize(25);
                        titlePaint6.setColor(Color.rgb(0,0,0));
                        canvas6.drawText("Nombre: Sin cena", 280, 1400, titlePaint6);
                    }

                    if(snapshot.child("users").child(userID).child("recetas").child("Viernes").child("C_Cena").exists()){
                        String viernesIngredientes = snapshot.child("users").child(userID).child("recetas").child("Viernes").child("C_Cena").child("f_ingredientes").getValue().toString();

                        titlePaint6.setTextAlign(Paint.Align.LEFT);
                        titlePaint6.setTypeface(Typeface.create(Typeface.DEFAULT, Typeface.NORMAL));
                        titlePaint6.setTextSize(25);
                        titlePaint6.setColor(Color.rgb(0,0,0));
                        canvas6.drawText("Ingredientes:", 100, 1450, titlePaint6);

                        imprimirIngredientes(100, 1480, viernesIngredientes, titlePaint6, canvas6);
                    }

                    if(snapshot.child("users").child(userID).child("recetas").child("Viernes").child("C_Cena").exists()){
                        String viernesCalorias = snapshot.child("users").child(userID).child("recetas").child("Viernes").child("C_Cena").child("f_calorias").getValue().toString();
                        titlePaint6.setTextAlign(Paint.Align.LEFT);
                        titlePaint6.setTypeface(Typeface.create(Typeface.DEFAULT, Typeface.NORMAL));
                        titlePaint6.setTextSize(20);
                        titlePaint6.setColor(Color.rgb(0,0,0));
                        canvas6.drawText("Contenido calórico aproximado: " + viernesCalorias, 100, 1660, titlePaint6);
                    }
                    /* ---------------- */

                    /* LOGO PARTE DE ABAJO DEL PDF */
                    canvas6.drawBitmap(scaledbmp, 515, 1800, myPaint6);
                    /* --------------------------- */

                    document.finishPage(page6);

                    /* INICIO DE SEPTIMA PAGINA (SABADO) */

                    PdfDocument.PageInfo pageInfo7 = new PdfDocument.PageInfo.Builder(1200, 2010, 1).create();
                    PdfDocument.Page page7 = document.startPage(pageInfo7);

                    Canvas canvas7 = page7.getCanvas();
                    Paint myPaint7 = new Paint();
                    Paint titlePaint7 = new Paint();

                    /* DATOS DEL NUTRIOLOGO */
                    titlePaint7.setTextAlign(Paint.Align.LEFT);
                    titlePaint7.setTextSize(20);
                    titlePaint7.setColor(Color.rgb(110, 184, 245));
                    canvas7.drawText("Dr.: " + nombre_nutri_pdf, 50, 75, titlePaint7);

                    titlePaint7.setTextAlign(Paint.Align.LEFT);
                    titlePaint7.setTextSize(20);
                    titlePaint7.setColor(Color.rgb(110, 184, 245));
                    canvas7.drawText(correo_nutri_pdf, 50, 110, titlePaint7);
                    /* -------------------- */

                    /* ALIMENTOS SABADO */
                    titlePaint7.setTextAlign(Paint.Align.CENTER);
                    titlePaint7.setTypeface(Typeface.create(Typeface.DEFAULT, Typeface.BOLD));
                    titlePaint7.setTextSize(45);
                    titlePaint7.setColor(Color.rgb(0,0,0));
                    canvas7.drawText("Sabado", pageWidth/2, 260, titlePaint7);

                    titlePaint7.setStyle(Paint.Style.STROKE);
                    titlePaint7.setStrokeWidth(2);

                    // Tablas Desayuno, comida y cena
                    canvas7.drawRect(60, 380, 1140, 780, titlePaint7);
                    canvas7.drawLine(60, 440, 1140, 440, titlePaint7);

                    canvas7.drawRect(60, 830, 1140, 1230, titlePaint7);
                    canvas7.drawLine(60, 890, 1140, 890, titlePaint7);

                    canvas7.drawRect(60, 1280, 1140, 1680, titlePaint7);
                    canvas7.drawLine(60, 1340, 1140, 1340, titlePaint7);

                    titlePaint7.setStrokeWidth(0);
                    titlePaint7.setStyle(Paint.Style.FILL);

                    //Desayuno
                    titlePaint7.setTextAlign(Paint.Align.CENTER);
                    titlePaint7.setTypeface(Typeface.create(Typeface.DEFAULT, Typeface.BOLD));
                    titlePaint7.setTextSize(30);
                    titlePaint7.setColor(Color.rgb(0,0,0));
                    canvas7.drawText("Tipo de alimento: Desayuno", pageWidth/2, 420, titlePaint7);

                    //Jueves d_NOMBRE Firebase
                    if(snapshot.child("users").child(userID).child("recetas").child("Sabado").child("A_Desayuno").exists()){
                        String sabadoDesayuno = snapshot.child("users").child(userID).child("recetas").child("Sabado").child("A_Desayuno").child("f_nombrecomida").getValue().toString();
                        titlePaint7.setTextAlign(Paint.Align.LEFT);
                        titlePaint7.setTypeface(Typeface.create(Typeface.DEFAULT, Typeface.NORMAL));
                        titlePaint7.setTextSize(25);
                        titlePaint7.setColor(Color.rgb(0,0,0));
                        canvas7.drawText("Nombre: " + sabadoDesayuno, 100, 500, titlePaint7);
                    } else {
                        titlePaint7.setTextAlign(Paint.Align.LEFT);
                        titlePaint7.setTypeface(Typeface.create(Typeface.DEFAULT, Typeface.NORMAL));
                        titlePaint7.setTextSize(25);
                        titlePaint7.setColor(Color.rgb(0,0,0));
                        canvas7.drawText("Nombre: Sin desayuno", 280, 500, titlePaint7);
                    }

                    if(snapshot.child("users").child(userID).child("recetas").child("Sabado").child("A_Desayuno").exists()){
                        String sabadoIngredientes = snapshot.child("users").child(userID).child("recetas").child("Sabado").child("A_Desayuno").child("f_ingredientes").getValue().toString();

                        titlePaint7.setTextAlign(Paint.Align.LEFT);
                        titlePaint7.setTypeface(Typeface.create(Typeface.DEFAULT, Typeface.NORMAL));
                        titlePaint7.setTextSize(25);
                        titlePaint7.setColor(Color.rgb(0,0,0));
                        canvas7.drawText("Ingredientes:", 100, 560, titlePaint4);

                        imprimirIngredientes(100, 590, sabadoIngredientes, titlePaint7, canvas7);
                    }

                    if(snapshot.child("users").child(userID).child("recetas").child("Sabado").child("A_Desayuno").exists()){
                        String sabadoCalorias = snapshot.child("users").child(userID).child("recetas").child("Sabado").child("A_Desayuno").child("f_calorias").getValue().toString();
                        titlePaint7.setTextAlign(Paint.Align.LEFT);
                        titlePaint7.setTypeface(Typeface.create(Typeface.DEFAULT, Typeface.NORMAL));
                        titlePaint7.setTextSize(20);
                        titlePaint7.setColor(Color.rgb(0,0,0));
                        canvas7.drawText("Contenido calórico aproximado: " + sabadoCalorias, 100, 760, titlePaint7);
                    }

                    //Comida
                    titlePaint7.setTextAlign(Paint.Align.CENTER);
                    titlePaint7.setTypeface(Typeface.create(Typeface.DEFAULT, Typeface.BOLD));
                    titlePaint7.setTextSize(30);
                    titlePaint7.setColor(Color.rgb(0,0,0));
                    canvas7.drawText("Tipo de alimento: Comida", pageWidth/2, 870, titlePaint7);

                    if(snapshot.child("users").child(userID).child("recetas").child("Sabado").child("B_Comida").exists()){
                        String sabadoComida = snapshot.child("users").child(userID).child("recetas").child("Sabado").child("B_Comida").child("f_nombrecomida").getValue().toString();
                        titlePaint7.setTextAlign(Paint.Align.LEFT);
                        titlePaint7.setTypeface(Typeface.create(Typeface.DEFAULT, Typeface.NORMAL));
                        titlePaint7.setTextSize(25);
                        titlePaint7.setColor(Color.rgb(0,0,0));
                        canvas7.drawText("Nombre: " + sabadoComida, 100, 950, titlePaint7);
                    } else {
                        titlePaint7.setTextAlign(Paint.Align.LEFT);
                        titlePaint7.setTypeface(Typeface.create(Typeface.DEFAULT, Typeface.NORMAL));
                        titlePaint7.setTextSize(25);
                        titlePaint7.setColor(Color.rgb(0,0,0));
                        canvas7.drawText("Nombre: Sin comida", 280, 950, titlePaint7);
                    }

                    if(snapshot.child("users").child(userID).child("recetas").child("Sabado").child("B_Comida").exists()){
                        String sabadoIngredientes = snapshot.child("users").child(userID).child("recetas").child("Sabado").child("B_Comida").child("f_ingredientes").getValue().toString();

                        titlePaint7.setTextAlign(Paint.Align.LEFT);
                        titlePaint7.setTypeface(Typeface.create(Typeface.DEFAULT, Typeface.NORMAL));
                        titlePaint7.setTextSize(25);
                        titlePaint7.setColor(Color.rgb(0,0,0));
                        canvas7.drawText("Ingredientes:", 100, 1010, titlePaint7);

                        imprimirIngredientes(100, 1040, sabadoIngredientes, titlePaint7, canvas7);

                    }

                    if(snapshot.child("users").child(userID).child("recetas").child("Sabado").child("B_Comida").exists()){
                        String sabadoCalorias = snapshot.child("users").child(userID).child("recetas").child("Sabado").child("B_Comida").child("f_calorias").getValue().toString();
                        titlePaint7.setTextAlign(Paint.Align.LEFT);
                        titlePaint7.setTypeface(Typeface.create(Typeface.DEFAULT, Typeface.NORMAL));
                        titlePaint7.setTextSize(20);
                        titlePaint7.setColor(Color.rgb(0,0,0));
                        canvas7.drawText("Contenido calórico aproximado: " + sabadoCalorias, 100, 1210, titlePaint7);
                    }

                    //Cena
                    titlePaint7.setTextAlign(Paint.Align.CENTER);
                    titlePaint7.setTypeface(Typeface.create(Typeface.DEFAULT, Typeface.BOLD));
                    titlePaint7.setTextSize(30);
                    titlePaint7.setColor(Color.rgb(0,0,0));
                    canvas7.drawText("Tipo de alimento: Cena", pageWidth/2, 1320, titlePaint7);

                    if(snapshot.child("users").child(userID).child("recetas").child("Sabado").child("C_Cena").exists()){
                        String sabadoCena = snapshot.child("users").child(userID).child("recetas").child("Sabado").child("C_Cena").child("f_nombrecomida").getValue().toString();
                        titlePaint7.setTextAlign(Paint.Align.LEFT);
                        titlePaint7.setTypeface(Typeface.create(Typeface.DEFAULT, Typeface.NORMAL));
                        titlePaint7.setTextSize(25);
                        titlePaint7.setColor(Color.rgb(0,0,0));
                        canvas7.drawText("Nombre: " + sabadoCena, 100, 1400, titlePaint7);
                    } else {
                        titlePaint7.setTextAlign(Paint.Align.LEFT);
                        titlePaint7.setTypeface(Typeface.create(Typeface.DEFAULT, Typeface.NORMAL));
                        titlePaint7.setTextSize(25);
                        titlePaint7.setColor(Color.rgb(0,0,0));
                        canvas7.drawText("Nombre: Sin cena", 280, 1400, titlePaint7);
                    }

                    if(snapshot.child("users").child(userID).child("recetas").child("Sabado").child("C_Cena").exists()){
                        String sabadoIngredientes = snapshot.child("users").child(userID).child("recetas").child("Sabado").child("C_Cena").child("f_ingredientes").getValue().toString();

                        titlePaint7.setTextAlign(Paint.Align.LEFT);
                        titlePaint7.setTypeface(Typeface.create(Typeface.DEFAULT, Typeface.NORMAL));
                        titlePaint7.setTextSize(25);
                        titlePaint7.setColor(Color.rgb(0,0,0));
                        canvas7.drawText("Ingredientes:", 100, 1450, titlePaint7);

                        imprimirIngredientes(100, 1480, sabadoIngredientes, titlePaint7, canvas7);
                    }

                    if(snapshot.child("users").child(userID).child("recetas").child("Sabado").child("C_Cena").exists()){
                        String sabadoCalorias = snapshot.child("users").child(userID).child("recetas").child("Sabado").child("C_Cena").child("f_calorias").getValue().toString();
                        titlePaint7.setTextAlign(Paint.Align.LEFT);
                        titlePaint7.setTypeface(Typeface.create(Typeface.DEFAULT, Typeface.NORMAL));
                        titlePaint7.setTextSize(20);
                        titlePaint7.setColor(Color.rgb(0,0,0));
                        canvas7.drawText("Contenido calórico aproximado: " + sabadoCalorias, 100, 1660, titlePaint7);
                    }
                    /* ---------------- */

                    /* LOGO PARTE DE ABAJO DEL PDF */
                    canvas7.drawBitmap(scaledbmp, 515, 1800, myPaint7);
                    /* --------------------------- */

                    document.finishPage(page7);


                    /* INICIO DE OCTAVA PAGINA (DOMINGO) */

                    PdfDocument.PageInfo pageInfo8 = new PdfDocument.PageInfo.Builder(1200, 2010, 1).create();
                    PdfDocument.Page page8 = document.startPage(pageInfo8);

                    Canvas canvas8 = page8.getCanvas();
                    Paint myPaint8 = new Paint();
                    Paint titlePaint8 = new Paint();

                    /* DATOS DEL NUTRIOLOGO */
                    titlePaint8.setTextAlign(Paint.Align.LEFT);
                    titlePaint8.setTextSize(20);
                    titlePaint8.setColor(Color.rgb(110, 184, 245));
                    canvas8.drawText("Dr.: " + nombre_nutri_pdf, 50, 75, titlePaint8);

                    titlePaint8.setTextAlign(Paint.Align.LEFT);
                    titlePaint8.setTextSize(20);
                    titlePaint8.setColor(Color.rgb(110, 184, 245));
                    canvas8.drawText(correo_nutri_pdf, 50, 110, titlePaint8);
                    /* -------------------- */

                    /* ALIMENTOS DOMINGO */
                    titlePaint8.setTextAlign(Paint.Align.CENTER);
                    titlePaint8.setTypeface(Typeface.create(Typeface.DEFAULT, Typeface.BOLD));
                    titlePaint8.setTextSize(45);
                    titlePaint8.setColor(Color.rgb(0,0,0));
                    canvas8.drawText("Domingo", pageWidth/2, 260, titlePaint8);

                    titlePaint8.setStyle(Paint.Style.STROKE);
                    titlePaint8.setStrokeWidth(2);

                    // Tablas Desayuno, comida y cena
                    canvas8.drawRect(60, 380, 1140, 780, titlePaint8);
                    canvas8.drawLine(60, 440, 1140, 440, titlePaint8);

                    canvas8.drawRect(60, 830, 1140, 1230, titlePaint8);
                    canvas8.drawLine(60, 890, 1140, 890, titlePaint8);

                    canvas8.drawRect(60, 1280, 1140, 1680, titlePaint8);
                    canvas8.drawLine(60, 1340, 1140, 1340, titlePaint8);

                    titlePaint8.setStrokeWidth(0);
                    titlePaint8.setStyle(Paint.Style.FILL);

                    //Desayuno
                    titlePaint8.setTextAlign(Paint.Align.CENTER);
                    titlePaint8.setTypeface(Typeface.create(Typeface.DEFAULT, Typeface.BOLD));
                    titlePaint8.setTextSize(30);
                    titlePaint8.setColor(Color.rgb(0,0,0));
                    canvas8.drawText("Tipo de alimento: Desayuno", pageWidth/2, 420, titlePaint8);

                    //Jueves d_NOMBRE Firebase
                    if(snapshot.child("users").child(userID).child("recetas").child("Domingo").child("A_Desayuno").exists()){
                        String domingoDesayuno = snapshot.child("users").child(userID).child("recetas").child("Domingo").child("A_Desayuno").child("f_nombrecomida").getValue().toString();
                        titlePaint8.setTextAlign(Paint.Align.LEFT);
                        titlePaint8.setTypeface(Typeface.create(Typeface.DEFAULT, Typeface.NORMAL));
                        titlePaint8.setTextSize(25);
                        titlePaint8.setColor(Color.rgb(0,0,0));
                        canvas8.drawText("Nombre: " + domingoDesayuno, 100, 500, titlePaint8);
                    } else {
                        titlePaint8.setTextAlign(Paint.Align.LEFT);
                        titlePaint8.setTypeface(Typeface.create(Typeface.DEFAULT, Typeface.NORMAL));
                        titlePaint8.setTextSize(25);
                        titlePaint8.setColor(Color.rgb(0,0,0));
                        canvas8.drawText("Nombre: Sin desayuno", 280, 500, titlePaint8);
                    }

                    if(snapshot.child("users").child(userID).child("recetas").child("Domingo").child("A_Desayuno").exists()){
                        String domingoIngredientes = snapshot.child("users").child(userID).child("recetas").child("Domingo").child("A_Desayuno").child("f_ingredientes").getValue().toString();

                        titlePaint8.setTextAlign(Paint.Align.LEFT);
                        titlePaint8.setTypeface(Typeface.create(Typeface.DEFAULT, Typeface.NORMAL));
                        titlePaint8.setTextSize(25);
                        titlePaint8.setColor(Color.rgb(0,0,0));
                        canvas8.drawText("Ingredientes:", 100, 560, titlePaint8);

                        imprimirIngredientes(100, 590, domingoIngredientes, titlePaint8, canvas8);
                    }

                    if(snapshot.child("users").child(userID).child("recetas").child("Domingo").child("A_Desayuno").exists()){
                        String domingoCalorias = snapshot.child("users").child(userID).child("recetas").child("Domingo").child("A_Desayuno").child("f_calorias").getValue().toString();
                        titlePaint8.setTextAlign(Paint.Align.LEFT);
                        titlePaint8.setTypeface(Typeface.create(Typeface.DEFAULT, Typeface.NORMAL));
                        titlePaint8.setTextSize(20);
                        titlePaint8.setColor(Color.rgb(0,0,0));
                        canvas8.drawText("Contenido calórico aproximado: " + domingoCalorias, 100, 760, titlePaint8);
                    }

                    //Comida
                    titlePaint8.setTextAlign(Paint.Align.CENTER);
                    titlePaint8.setTypeface(Typeface.create(Typeface.DEFAULT, Typeface.BOLD));
                    titlePaint8.setTextSize(30);
                    titlePaint8.setColor(Color.rgb(0,0,0));
                    canvas8.drawText("Tipo de alimento: Comida", pageWidth/2, 870, titlePaint8);

                    if(snapshot.child("users").child(userID).child("recetas").child("Domingo").child("B_Comida").exists()){
                        String domingoComida = snapshot.child("users").child(userID).child("recetas").child("Domingo").child("B_Comida").child("f_nombrecomida").getValue().toString();
                        titlePaint8.setTextAlign(Paint.Align.LEFT);
                        titlePaint8.setTypeface(Typeface.create(Typeface.DEFAULT, Typeface.NORMAL));
                        titlePaint8.setTextSize(25);
                        titlePaint8.setColor(Color.rgb(0,0,0));
                        canvas8.drawText("Nombre: " + domingoComida, 100, 950, titlePaint8);
                    } else {
                        titlePaint8.setTextAlign(Paint.Align.LEFT);
                        titlePaint8.setTypeface(Typeface.create(Typeface.DEFAULT, Typeface.NORMAL));
                        titlePaint8.setTextSize(25);
                        titlePaint8.setColor(Color.rgb(0,0,0));
                        canvas8.drawText("Nombre: Sin comida", 280, 950, titlePaint8);
                    }

                    if(snapshot.child("users").child(userID).child("recetas").child("Domingo").child("B_Comida").exists()){
                        String domingoIngredientes = snapshot.child("users").child(userID).child("recetas").child("Domingo").child("B_Comida").child("f_ingredientes").getValue().toString();

                        titlePaint8.setTextAlign(Paint.Align.LEFT);
                        titlePaint8.setTypeface(Typeface.create(Typeface.DEFAULT, Typeface.NORMAL));
                        titlePaint8.setTextSize(25);
                        titlePaint8.setColor(Color.rgb(0,0,0));
                        canvas8.drawText("Ingredientes:", 100, 1010, titlePaint8);

                        imprimirIngredientes(100, 1040, domingoIngredientes, titlePaint8, canvas8);

                    }

                    if(snapshot.child("users").child(userID).child("recetas").child("Domingo").child("B_Comida").exists()){
                        String domingoCalorias = snapshot.child("users").child(userID).child("recetas").child("Domingo").child("B_Comida").child("f_calorias").getValue().toString();
                        titlePaint8.setTextAlign(Paint.Align.LEFT);
                        titlePaint8.setTypeface(Typeface.create(Typeface.DEFAULT, Typeface.NORMAL));
                        titlePaint8.setTextSize(20);
                        titlePaint8.setColor(Color.rgb(0,0,0));
                        canvas8.drawText("Contenido calórico aproximado: " + domingoCalorias, 100, 1210, titlePaint8);
                    }

                    //Cena
                    titlePaint8.setTextAlign(Paint.Align.CENTER);
                    titlePaint8.setTypeface(Typeface.create(Typeface.DEFAULT, Typeface.BOLD));
                    titlePaint8.setTextSize(30);
                    titlePaint8.setColor(Color.rgb(0,0,0));
                    canvas8.drawText("Tipo de alimento: Cena", pageWidth/2, 1320, titlePaint8);

                    if(snapshot.child("users").child(userID).child("recetas").child("Domingo").child("C_Cena").exists()){
                        String domingoCena = snapshot.child("users").child(userID).child("recetas").child("Domingo").child("C_Cena").child("f_nombrecomida").getValue().toString();
                        titlePaint8.setTextAlign(Paint.Align.LEFT);
                        titlePaint8.setTypeface(Typeface.create(Typeface.DEFAULT, Typeface.NORMAL));
                        titlePaint8.setTextSize(25);
                        titlePaint8.setColor(Color.rgb(0,0,0));
                        canvas8.drawText("Nombre: " + domingoCena, 100, 1400, titlePaint8);
                    } else {
                        titlePaint8.setTextAlign(Paint.Align.LEFT);
                        titlePaint8.setTypeface(Typeface.create(Typeface.DEFAULT, Typeface.NORMAL));
                        titlePaint8.setTextSize(25);
                        titlePaint8.setColor(Color.rgb(0,0,0));
                        canvas8.drawText("Nombre: Sin cena", 280, 1400, titlePaint8);
                    }

                    if(snapshot.child("users").child(userID).child("recetas").child("Domingo").child("C_Cena").exists()){
                        String domingoIngredientes = snapshot.child("users").child(userID).child("recetas").child("Domingo").child("C_Cena").child("f_ingredientes").getValue().toString();

                        titlePaint8.setTextAlign(Paint.Align.LEFT);
                        titlePaint8.setTypeface(Typeface.create(Typeface.DEFAULT, Typeface.NORMAL));
                        titlePaint8.setTextSize(25);
                        titlePaint8.setColor(Color.rgb(0,0,0));
                        canvas8.drawText("Ingredientes:", 100, 1450, titlePaint8);

                        imprimirIngredientes(100, 1480, domingoIngredientes, titlePaint8, canvas8);
                    }

                    if(snapshot.child("users").child(userID).child("recetas").child("Domingo").child("C_Cena").exists()){
                        String domingoCalorias = snapshot.child("users").child(userID).child("recetas").child("Domingo").child("C_Cena").child("f_calorias").getValue().toString();
                        titlePaint8.setTextAlign(Paint.Align.LEFT);
                        titlePaint8.setTypeface(Typeface.create(Typeface.DEFAULT, Typeface.NORMAL));
                        titlePaint8.setTextSize(20);
                        titlePaint8.setColor(Color.rgb(0,0,0));
                        canvas8.drawText("Contenido calórico aproximado: " + domingoCalorias, 100, 1660, titlePaint8);
                    }
                    /* ---------------- */

                    /* LOGO PARTE DE ABAJO DEL PDF */
                    canvas8.drawBitmap(scaledbmp, 515, 1800, myPaint8);
                    /* --------------------------- */

                    document.finishPage(page8);


                    try {
                        document.writeTo(outputStream);
                    } catch (IOException e) {
                        e.printStackTrace();
                    }

                    document.close();
                    Toast.makeText(getActivity(), "PDF generado correctamente", Toast.LENGTH_SHORT).show();
                }
            }
            @Override
            public void onCancelled(@NonNull @NotNull DatabaseError error) {
            }
        });

    }

    public void loadNamePacientes(){
        List<SpinnerPaciente> pacientesList = new ArrayList<>();
        dbRef.child("pacientes").addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull @NotNull DataSnapshot snapshot) {
                if(snapshot.exists()){
                    for(DataSnapshot ds: snapshot.getChildren()){
                        String p_Nombre = ds.child("p_Nombre").getValue().toString();
                        String pacienteID = ds.getKey();
                        pacientesList.add(new SpinnerPaciente(pacienteID, p_Nombre));
                    }

                    if (getActivity() != null) {
                        ArrayAdapter<SpinnerPaciente> arrayAdapter = new ArrayAdapter<>(getActivity(), android.R.layout.simple_dropdown_item_1line, pacientesList);
                        spinnerPacientes.setAdapter(arrayAdapter);
                        spinnerPacientes.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
                            @Override
                            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                                pacienteID = pacientesList.get(position).getPacienteID();
                            }

                            @Override
                            public void onNothingSelected(AdapterView<?> parent) {

                            }
                        });
                    }
                }
            }

            @Override
            public void onCancelled(@NonNull @NotNull DatabaseError error) {

            }
        });
    }

    private void replaceFragment(Fragment fragment) {
        FragmentManager fragmentManager = getFragmentManager();
        FragmentTransaction fragmentTransaction = fragmentManager.beginTransaction();
        fragmentTransaction.replace(R.id.body_container,fragment);
        fragmentTransaction.commit();
    }

    private void imprimirIngredientes(int xi, int yi, String ingredientes, Paint titlePaint, Canvas canvas){
        char coma = ',';
        int y = yi;
        int x = xi;
        int salto =  0;
        String palabra = " ";
        for(int i=0; i<ingredientes.length(); i++){
            char letra = ingredientes.charAt(i);
            String letraS = String.valueOf(letra);

            if(coma == letra){
                titlePaint.setTextAlign(Paint.Align.LEFT);
                titlePaint.setTypeface(Typeface.create(Typeface.DEFAULT, Typeface.NORMAL));
                titlePaint.setTextSize(25);
                titlePaint.setColor(Color.rgb(0,0,0));
                canvas.drawText(palabra, x, y, titlePaint);
                salto++;
                y = y + 30;
                palabra = "";
            } else {
                palabra += letraS;
            }

            if(salto == 4){
                y = yi;
                x = x + 280;
                salto = 0;
            }
        }
    }
    /*public void imprimirTexto(int textSize, Paint.Align Align, Typeface default1, Typeface textType, String textoAImprimir, Paint titlepaint, Canvas canvas, int R, int G, int B, int x, int y){
        titlepaint.setTextAlign(Align);
        titlepaint.setTypeface(Typeface.create(default1, textType));
        titlepaint.setTextSize(textSize);
        titlepaint.setColor(Color.rgb(R,G,B);
        canvas.drawText(textoAImprimir, x, y, titlepaint);
    }*/
}