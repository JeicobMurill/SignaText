package com.example.signatext;

import androidx.appcompat.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;

import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

public class MainActivity extends AppCompatActivity {

    private static final String TAG = "FirebaseTest";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        // Referencia a la base de datos
        FirebaseDatabase database = FirebaseDatabase.getInstance();
        DatabaseReference myRef = database.getReference("message");

        // Escribir un dato en la base de datos
        myRef.setValue("Hola, Firebase!")
                .addOnSuccessListener(aVoid -> Log.d(TAG, "Dato enviado correctamente"))
                .addOnFailureListener(e -> Log.e(TAG, "Error al enviar el dato", e));
    }
}