package com.example.lab6_iot_20223291;

import android.app.Application;

import com.google.firebase.auth.FirebaseAuth;

/** Application: idioma de los correos de Firebase (Clase 10, diapositiva 36). */
public class App extends Application {

    @Override
    public void onCreate() {
        super.onCreate();
        // Idioma de los correos de Firebase (verificación y recuperar contraseña): español de Latinoamérica
        FirebaseAuth.getInstance().setLanguageCode("es-419");
        // El idioma de las pantallas se fuerza en LoginActivity (ahí ya existe una Activity)
    }
}
