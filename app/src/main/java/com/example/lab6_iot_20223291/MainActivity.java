package com.example.lab6_iot_20223291;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.firebase.ui.auth.AuthUI;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.ListenerRegistration;
import com.google.firebase.firestore.Query;

import java.util.ArrayList;
import java.util.List;

/** Pantalla 2: lista de sesiones de estudio del usuario, con actualización en tiempo real. */
public class MainActivity extends AppCompatActivity implements SesionAdapter.Listener {

    private SesionAdapter adapter;
    private TextView tvVacio, tvConteo;
    private ListenerRegistration registro;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        if (FirebaseAuth.getInstance().getCurrentUser() == null) {
            irALogin();
            return;
        }

        setContentView(R.layout.activity_main);
        Ui.aplicarInsets(findViewById(R.id.main), findViewById(R.id.toolbar));

        tvVacio = findViewById(R.id.tvVacio);
        tvConteo = findViewById(R.id.tvConteo);
        tvConteo.setText("0 sesiones registradas");

        RecyclerView rv = findViewById(R.id.rv);
        rv.setLayoutManager(new LinearLayoutManager(this));
        adapter = new SesionAdapter(this);
        rv.setAdapter(adapter);

        findViewById(R.id.fab).setOnClickListener(v ->
                startActivity(new Intent(this, SesionFormActivity.class)));
        findViewById(R.id.btnGraficos).setOnClickListener(v ->
                startActivity(new Intent(this, GraficosActivity.class)));
        // Clase 10, diapositiva 43: cerrar sesión con AuthUI.signOut()
        findViewById(R.id.btnLogout).setOnClickListener(v ->
                AuthUI.getInstance().signOut(this).addOnCompleteListener(task -> irALogin()));
    }

    @Override
    protected void onStart() {
        super.onStart();
        if (FirebaseAuth.getInstance().getCurrentUser() == null || adapter == null) return;

        // Escucha en tiempo real: la lista se actualiza sola al agregar, editar o eliminar
        registro = Refs.sesiones()
                .orderBy("fecha", Query.Direction.DESCENDING)
                .addSnapshotListener((snap, e) -> {
                    if (e != null || snap == null) {
                        Toast.makeText(this, "No se pudieron cargar las sesiones: "
                                + (e != null ? e.getMessage() : ""), Toast.LENGTH_LONG).show();
                        return;
                    }
                    List<Sesion> lista = new ArrayList<>();
                    for (DocumentSnapshot doc : snap.getDocuments()) {
                        Sesion s = doc.toObject(Sesion.class);
                        if (s != null && s.getFecha() != null) lista.add(s);
                    }
                    adapter.setDatos(lista);
                    tvVacio.setVisibility(lista.isEmpty() ? View.VISIBLE : View.GONE);
                    tvConteo.setText(lista.size() == 1
                            ? "1 sesión registrada"
                            : lista.size() + " sesiones registradas");
                });
    }

    @Override
    protected void onStop() {
        super.onStop();
        if (registro != null) {
            registro.remove();
            registro = null;
        }
    }

    // ---- Acciones de cada fila ----

    @Override
    public void onEditar(Sesion s) {
        Intent i = new Intent(this, SesionFormActivity.class);
        i.putExtra("id", s.getId());
        i.putExtra("curso", s.getCurso());
        i.putExtra("fecha", s.getFecha().getTime());
        i.putExtra("horas", s.getHoras());
        i.putExtra("tipo", s.getTipo());
        startActivity(i);
    }

    @Override
    public void onEliminar(Sesion s) {
        new AlertDialog.Builder(this)
                .setTitle("Eliminar sesión")
                .setMessage("¿Eliminar la sesión de " + s.getCurso() + "?")
                .setPositiveButton("Eliminar", (d, w) ->
                        Refs.sesiones().document(s.getId()).delete()
                                .addOnFailureListener(e -> Toast.makeText(this,
                                        "No se pudo eliminar: " + e.getMessage(), Toast.LENGTH_LONG).show()))
                .setNegativeButton("Cancelar", null)
                .show();
    }

    private void irALogin() {
        Intent i = new Intent(this, LoginActivity.class);
        i.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        startActivity(i);
        finish();
    }
}
