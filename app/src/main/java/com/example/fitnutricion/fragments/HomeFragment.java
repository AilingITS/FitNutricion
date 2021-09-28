package com.example.fitnutricion.fragments;

import android.Manifest;
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

    private Spinner spinnerComidas, spinnerPacientes;
    Button btn_crear_pdf;
    Bitmap bmp, scaledbmp;
    int pageWidth = 1200;

    DatabaseReference dbRef;

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
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        vista = inflater.inflate(R.layout.fragment_home, container, false);

        mAuth = FirebaseAuth.getInstance();
        userID = mAuth.getCurrentUser().getUid();
        //dbRef = FirebaseDatabase.getInstance().getReference();
        dbRef = FirebaseDatabase.getInstance().getReference("users").child(userID);
        spinnerComidas = vista.findViewById(R.id.spinnerComidas);
        spinnerPacientes = vista.findViewById(R.id.spinnerPacientes);

        btn_crear_pdf = (Button) vista.findViewById(R.id.btn_crear_pdf);
        bmp = BitmapFactory.decodeResource(getResources(), R.drawable.logo);
        scaledbmp = Bitmap.createScaledBitmap(bmp, 1200, 518, false);

        String [] opciones = {"Desayuno", "Comida", "Cena"};

        ArrayAdapter <String> adapter = new ArrayAdapter<String>(getContext(), android.R.layout.simple_spinner_item, opciones);
        spinnerComidas.setAdapter(adapter);

        String seleccion = spinnerComidas.getSelectedItem().toString();
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
        String pdfPath = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS).toString();
        File file = new File(pdfPath, "FitNutricion.pdf");
        OutputStream outputStream = new FileOutputStream(file);

        PdfDocument document = new PdfDocument();
        PdfDocument.PageInfo pageInfo = new PdfDocument.PageInfo.Builder(1200, 2010, 1).create();
        PdfDocument.Page page = document.startPage(pageInfo);

        Canvas canvas = page.getCanvas();
        Paint titlePaint = new Paint();

        titlePaint.setTextAlign(Paint.Align.CENTER);
        titlePaint.setTypeface(Typeface.create(Typeface.DEFAULT, Typeface.ITALIC));
        titlePaint.setTextSize(70);
        canvas.drawText("Datos", pageWidth/2, 500, titlePaint);

        document.finishPage(page);

        document.writeTo(outputStream);
        document.close();
        Toast.makeText(getActivity(), "PDF generado correctamente", Toast.LENGTH_SHORT).show();

    }

    public void loadNamePacientes(){
        List<SpinnerPaciente> pacientesList = new ArrayList<>();
        dbRef.child("pacientes").addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull @NotNull DataSnapshot snapshot) {
                if(snapshot.exists()){
                    for(DataSnapshot ds: snapshot.getChildren()){
                        String p_Nombre = ds.child("p_Nombre").getValue().toString();
                        pacientesList.add(new SpinnerPaciente(p_Nombre));
                    }

                    if (getActivity() != null) {
                        ArrayAdapter<SpinnerPaciente> arrayAdapter = new ArrayAdapter<>(getActivity(), android.R.layout.simple_dropdown_item_1line, pacientesList);
                        spinnerPacientes.setAdapter(arrayAdapter);
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