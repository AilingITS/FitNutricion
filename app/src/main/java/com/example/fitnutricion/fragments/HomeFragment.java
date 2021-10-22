package com.example.fitnutricion.fragments;

import android.Manifest;
import android.app.Activity;
import android.content.ContextWrapper;
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

import com.example.fitnutricion.R;
import com.example.fitnutricion.firebase.Pacientes;
import com.example.fitnutricion.firebase.SpinnerPaciente;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.squareup.picasso.Picasso;

import org.jetbrains.annotations.NotNull;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.util.ArrayList;
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
    private Spinner spinnerComidas, spinnerPacientes;
    Button btn_crear_pdf;
    Bitmap bmp, scaledbmp;
    int pageWidth = 1200;

    DatabaseReference dbRef;
    DatabaseReference dbRef_pdf;
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
        firebaseDatabase_pdf = FirebaseDatabase.getInstance();
        dbRef_pdf = firebaseDatabase_pdf.getReference();
        //spinnerComidas = vista.findViewById(R.id.spinnerComidas);
        spinnerPacientes = vista.findViewById(R.id.spinnerPacientes);

        btn_crear_pdf = (Button) vista.findViewById(R.id.btn_crear_pdf);
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

        //Poner esto cuando se cree el pdf para saber el tipo de comida
        /*if(seleccion.equals("Desayuno")){

        } else if(seleccion.equals("Comida")){

        } else if(seleccion.equals("Cena")){

        }*/

        return vista;
    }

    private void createPDF() throws IOException {

        dbRef_pdf.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull @NotNull DataSnapshot snapshot) {
                if(snapshot.exists()){

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
                    File file = new File(pdfPath, "FitNutricion.pdf");
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
                    titlePaint.setTextSize(40);
                    titlePaint.setColor(Color.rgb(110, 184, 245));
                    canvas.drawText("Dr.: " + nombre_nutri_pdf, 50, 75, titlePaint);

                    titlePaint.setTextAlign(Paint.Align.LEFT);
                    titlePaint.setTextSize(40);
                    titlePaint.setColor(Color.rgb(110, 184, 245));
                    canvas.drawText(correo_nutri_pdf, 50, 110, titlePaint);
                    /* -------------------- */

                    /* FECHA Y DATOS DEL PACIENTE */
                    titlePaint.setTextAlign(Paint.Align.CENTER);
                    titlePaint.setTypeface(Typeface.create(Typeface.DEFAULT, Typeface.BOLD_ITALIC));
                    titlePaint.setTextSize(65);
                    titlePaint.setColor(Color.rgb(0,0,0));
                    canvas.drawText("RECETA SEMANAL", pageWidth/2, 220, titlePaint);

                    titlePaint.setTextAlign(Paint.Align.LEFT);
                    titlePaint.setTypeface(Typeface.create(Typeface.DEFAULT, Typeface.BOLD));
                    titlePaint.setTextSize(45);
                    titlePaint.setColor(Color.rgb(0,0,0));
                    canvas.drawText("Fecha:", 50, 350, titlePaint);

                    // FECHA
                    titlePaint.setTextAlign(Paint.Align.LEFT);
                    titlePaint.setTypeface(Typeface.create(Typeface.DEFAULT, Typeface.NORMAL));
                    titlePaint.setTextSize(45);
                    titlePaint.setColor(Color.rgb(0,0,0));
                    canvas.drawText("20 de octubre de 2021", 188, 350, titlePaint);

                    titlePaint.setTextAlign(Paint.Align.LEFT);
                    titlePaint.setTypeface(Typeface.create(Typeface.DEFAULT, Typeface.BOLD));
                    titlePaint.setTextSize(45);
                    titlePaint.setColor(Color.rgb(0,0,0));
                    canvas.drawText("Nombre del paciente:", 50, 410, titlePaint);

                    // NOMBRE DEL PACIENTE
                    titlePaint.setTextAlign(Paint.Align.LEFT);
                    titlePaint.setTypeface(Typeface.create(Typeface.DEFAULT, Typeface.NORMAL));
                    titlePaint.setTextSize(45);
                    titlePaint.setColor(Color.rgb(0,0,0));
                    canvas.drawText(nombre_p_pdf, 490, 410, titlePaint);

                    titlePaint.setTextAlign(Paint.Align.LEFT);
                    titlePaint.setTypeface(Typeface.create(Typeface.DEFAULT, Typeface.BOLD));
                    titlePaint.setTextSize(45);
                    titlePaint.setColor(Color.rgb(0,0,0));
                    canvas.drawText("Edad:", 50, 470, titlePaint);

                    //Edad
                    titlePaint.setTextAlign(Paint.Align.LEFT);
                    titlePaint.setTypeface(Typeface.create(Typeface.DEFAULT, Typeface.NORMAL));
                    titlePaint.setTextSize(45);
                    titlePaint.setColor(Color.rgb(0,0,0));
                    canvas.drawText(Edad_p_pdf + " años", 165, 470, titlePaint);

                    titlePaint.setTextAlign(Paint.Align.LEFT);
                    titlePaint.setTypeface(Typeface.create(Typeface.DEFAULT, Typeface.BOLD));
                    titlePaint.setTextSize(45);
                    titlePaint.setColor(Color.rgb(0,0,0));
                    canvas.drawText("Sexo:", 50, 530, titlePaint);

                    //Sexo
                    titlePaint.setTextAlign(Paint.Align.LEFT);
                    titlePaint.setTypeface(Typeface.create(Typeface.DEFAULT, Typeface.NORMAL));
                    titlePaint.setTextSize(45);
                    titlePaint.setColor(Color.rgb(0,0,0));
                    canvas.drawText(Sexo_p_pdf, 170, 530, titlePaint);
                    /* -------------------------- */

                    /* LISTA DE COMIDAS - TABLA */
                    titlePaint.setTextAlign(Paint.Align.CENTER);
                    titlePaint.setTypeface(Typeface.create(Typeface.DEFAULT, Typeface.BOLD_ITALIC));
                    titlePaint.setTextSize(55);
                    titlePaint.setColor(Color.rgb(0,0,0));
                    canvas.drawText("Lista de alimentos", pageWidth/2, 660, titlePaint);

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
                    canvas2.drawBitmap(scaledbmp, 425, 1800, myPaint2);

                    titlePaint2.setTextAlign(Paint.Align.CENTER);
                    titlePaint2.setTypeface(Typeface.create(Typeface.DEFAULT, Typeface.BOLD));
                    titlePaint2.setTextSize(70);
                    canvas2.drawText("RECETA SEMANAL", pageWidth/2, 100, titlePaint2);

                    titlePaint2.setTextAlign(Paint.Align.CENTER);
                    titlePaint2.setTextSize(50);
                    titlePaint2.setColor(Color.rgb(122, 119, 119));
                    canvas2.drawText("Nutriólogo: " + nombre_nutri_pdf, pageWidth/2, 160, titlePaint2);

                    titlePaint2.setTextAlign(Paint.Align.CENTER);
                    titlePaint2.setTextSize(50);
                    titlePaint2.setColor(Color.rgb(122, 119, 119));
                    canvas2.drawText("Correo: " + correo_nutri_pdf, pageWidth/2, 220, titlePaint2);

                    document.finishPage(page2);

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

        /*String pdfPath = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS).toString();
        File file = new File(pdfPath, "FitNutricion.pdf");
        OutputStream outputStream = new FileOutputStream(file);

           DECLARANDO DOCUMENTO

        PdfDocument document = new PdfDocument();

           INICIO DE PRIMERA PAGINA

        PdfDocument.PageInfo pageInfo = new PdfDocument.PageInfo.Builder(1200, 2010, 1).create();
        PdfDocument.Page page = document.startPage(pageInfo);

        Canvas canvas = page.getCanvas();
        Paint myPaint = new Paint();
        Paint titlePaint = new Paint();
        canvas.drawBitmap(scaledbmp, 0, 0, myPaint);

        titlePaint.setTextAlign(Paint.Align.CENTER);
        titlePaint.setTypeface(Typeface.create(Typeface.DEFAULT, Typeface.BOLD));
        titlePaint.setTextSize(70);
        canvas.drawText("RECETA", pageWidth/2, 30, titlePaint);

        titlePaint.setTextAlign(Paint.Align.CENTER);
        titlePaint.setTypeface(Typeface.create(Typeface.DEFAULT, Typeface.ITALIC));
        titlePaint.setTextSize(70);
        canvas.drawText("Lunes", pageWidth/2, 500, titlePaint);

        document.finishPage(page);

            INICIO DE SEGUNDA PAGINA

        PdfDocument.PageInfo pageInfo2 = new PdfDocument.PageInfo.Builder(1200, 2010, 1).create();
        PdfDocument.Page page2 = document.startPage(pageInfo2);

        Canvas canvas2 = page2.getCanvas();
        Paint myPaint2 = new Paint();
        Paint titlePaint2 = new Paint();
        canvas2.drawBitmap(scaledbmp, 0, 0, myPaint2);

        titlePaint2.setTextAlign(Paint.Align.CENTER);
        titlePaint2.setTypeface(Typeface.create(Typeface.DEFAULT, Typeface.BOLD));
        titlePaint2.setTextSize(70);
        canvas2.drawText("RECETA", pageWidth/2, 270, titlePaint2);

        titlePaint2.setTextAlign(Paint.Align.CENTER);
        titlePaint2.setTypeface(Typeface.create(Typeface.DEFAULT, Typeface.ITALIC));
        titlePaint2.setTextSize(70);
        canvas2.drawText("Martes", pageWidth/2, 500, titlePaint2);

        document.finishPage(page2);

        document.writeTo(outputStream);
        document.close();
        Toast.makeText(getActivity(), "PDF generado correctamente", Toast.LENGTH_SHORT).show();*/

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

    /*private String getFilePath(){
        ContextWrapper contextWrapper = new ContextWrapper(getContext());
        File file = new File(Environment.getExternalStorageDirectory(), "/FitNutricion.pdf");
        return file.getPath();
    }*/
}