/**
 Auth Jeicob Murillo
 */


package com.example.signatext;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.os.Bundle;
import android.util.Log;
import android.widget.Toast;

import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.HashMap;

public class MainActivity extends AppCompatActivity {

    // Referencia a la base de datos de Firebase
    private DatabaseReference databaseReference;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        // Inicializa la referencia a la base de datos, apuntando a la raíz "Datos"
        databaseReference = FirebaseDatabase.getInstance().getReference("Datos");

        // Llamadas a las funciones para manejar datos en Firebase
        enviarDatos(); // Envía datos a la base de datos
        obtenerDatos(); // Recupera datos de la base de datos
        editarDatos("ID_DEL_NODO", "edad", "30"); // Actualiza datos en un nodo específico
        eliminarDatos("ID_DEL_NODO"); // Elimina un nodo específico
    }

    /**
     * Envía datos a Firebase Realtime Database.
     */
    private void enviarDatos() {
        // Crea un mapa con los datos a enviar
        HashMap<String, String> datos = new HashMap<>();
        datos.put("nombre", "Juan");
        datos.put("edad", "25");

        // Genera un ID único con push() y envía los datos con setValue()
        databaseReference.push().setValue(datos)
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful()) {
                        Log.d("Firebase", "Datos enviados correctamente.");
                        Toast.makeText(this, "Datos enviados correctamente.", Toast.LENGTH_SHORT).show();
                    } else {
                        Log.e("Firebase", "Error al enviar datos: " + task.getException().getMessage());
                        Toast.makeText(this, "Error al enviar datos.", Toast.LENGTH_SHORT).show();
                    }
                });
    }

    /**
     * Recupera datos de Firebase Realtime Database en tiempo real.
     */
    private void obtenerDatos() {
        // Configura un listener para escuchar cambios en la base de datos
        databaseReference.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                for (DataSnapshot data : snapshot.getChildren()) {
                    // Convierte cada nodo en un mapa para acceder a sus valores
                    HashMap<String, String> datos = (HashMap<String, String>) data.getValue();
                    String nombre = datos.get("nombre");
                    String edad = datos.get("edad");

                    // Imprime los datos en los logs
                    Log.d("Firebase", "Nombre: " + nombre + ", Edad: " + edad);
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                // Maneja errores al obtener los datos
                Log.e("Firebase", "Error al obtener datos: " + error.getMessage());
            }
        });
    }

    /**
     * Actualiza datos en un nodo específico de Firebase.
     * @param key El ID único del nodo que se va a actualizar.
     * @param campo El campo específico que se desea modificar.
     * @param nuevoValor El nuevo valor a asignar al campo.
     */
    private void editarDatos(String key, String campo, String nuevoValor) {
        // Apunta al nodo específico utilizando la clave única
        DatabaseReference nodo = databaseReference.child(key);

        // Crea un mapa con el campo y el nuevo valor a actualizar
        HashMap<String, Object> actualizacion = new HashMap<>();
        actualizacion.put(campo, nuevoValor);

        // Realiza la actualización en la base de datos
        nodo.updateChildren(actualizacion)
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful()) {
                        Log.d("Firebase", "Datos actualizados correctamente.");
                        Toast.makeText(this, "Datos actualizados correctamente.", Toast.LENGTH_SHORT).show();
                    } else {
                        Log.e("Firebase", "Error al actualizar datos: " + task.getException().getMessage());
                        Toast.makeText(this, "Error al actualizar datos.", Toast.LENGTH_SHORT).show();
                    }
                });
    }

    /**
     * Elimina un nodo específico de Firebase.
     * @param key El ID único del nodo que se desea eliminar.
     */
    private void eliminarDatos(String key) {
        // Apunta al nodo específico utilizando la clave única
        DatabaseReference nodo = databaseReference.child(key);

        // Elimina el nodo de la base de datos
        nodo.removeValue()
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful()) {
                        Log.d("Firebase", "Datos eliminados correctamente.");
                        Toast.makeText(this, "Datos eliminados correctamente.", Toast.LENGTH_SHORT).show();
                    } else {
                        Log.e("Firebase", "Error al eliminar datos: " + task.getException().getMessage());
                        Toast.makeText(this, "Error al eliminar datos.", Toast.LENGTH_SHORT).show();
                    }
                });
    }
}