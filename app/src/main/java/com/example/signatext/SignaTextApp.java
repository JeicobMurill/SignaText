package com.example.signatext;

import android.app.Application;
import com.google.firebase.FirebaseApp;

public class SignaTextApp extends Application {

    @Override
    public void onCreate() {
        super.onCreate();

        // Inicializar Firebase
        FirebaseApp.initializeApp(this);
    }
}