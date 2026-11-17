package com.example.lab6_iot_20223291;

import android.content.Intent;
import android.os.Bundle;
import android.widget.Toast;

import androidx.activity.result.ActivityResultLauncher;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.app.AppCompatDelegate;
import androidx.core.os.LocaleListCompat;

import com.firebase.ui.auth.AuthMethodPickerLayout;
import com.firebase.ui.auth.AuthUI;
import com.firebase.ui.auth.FirebaseAuthUIActivityResultContract;
import com.firebase.ui.auth.data.model.FirebaseAuthUIAuthenticationResult;
import com.google.firebase.auth.FirebaseAuth;

import java.util.Arrays;

/**
 * Pantalla 1: inicio de sesión con FirebaseUI y layout propio (Clase 10).
 * Correo/contraseña (iniciar sesión, registrarse y recuperar contraseña) y Google.
 */
public class LoginActivity extends AppCompatActivity {

    private final ActivityResultLauncher<Intent> signInLauncher = registerForActivityResult(
            new FirebaseAuthUIActivityResultContract(), this::onSignInResult);

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        // Todos los textos en español (también los de FirebaseUI y el calendario), aunque el celular esté en inglés.
        // Android guarda este idioma para la app; al cambiarlo recrea esta Activity y onCreate vuelve a correr.
        if (!"es".equals(idiomaActual())) {
            AppCompatDelegate.setApplicationLocales(LocaleListCompat.forLanguageTags("es"));
            return;
        }

        // Si el usuario ya inició sesión, va directo a la pantalla principal
        if (FirebaseAuth.getInstance().getCurrentUser() != null) {
            irAMain();
            return;
        }
        lanzarLogin();
    }

    private String idiomaActual() {
        LocaleListCompat locales = AppCompatDelegate.getApplicationLocales();
        return locales.isEmpty() || locales.get(0) == null ? "" : locales.get(0).getLanguage();
    }

    private void lanzarLogin() {
        // Layout propio con el logo y los botones; FirebaseUI los enlaza por id
        AuthMethodPickerLayout picker = new AuthMethodPickerLayout.Builder(R.layout.auth_picker)
                .setEmailButtonId(R.id.btnAuthEmail)
                .setGoogleButtonId(R.id.btnAuthGoogle)
                .build();

        Intent intent = AuthUI.getInstance()
                .createSignInIntentBuilder()
                .setAvailableProviders(Arrays.asList(
                        new AuthUI.IdpConfig.EmailBuilder().build(),
                        new AuthUI.IdpConfig.GoogleBuilder().build()))
                .setAuthMethodPickerLayout(picker)
                .setTheme(R.style.Theme_Lab6_Auth) // colores azules y sin barra con el nombre de la app
                .build();
        signInLauncher.launch(intent);
    }

    private void onSignInResult(FirebaseAuthUIAuthenticationResult result) {
        // Con Google, FirebaseUI 9.1.1 a veces devuelve "cancelado" aunque la sesión ya quedó iniciada
        if (result.getResultCode() == RESULT_OK
                || FirebaseAuth.getInstance().getCurrentUser() != null) {
            irAMain();
        } else {
            Toast.makeText(this, "Inicio de sesión cancelado", Toast.LENGTH_SHORT).show();
            finish();
        }
    }

    private void irAMain() {
        startActivity(new Intent(this, MainActivity.class));
        finish();
    }
}
