package com.example.lab6_iot_20223291;

import android.os.Bundle;
import android.widget.Spinner;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.google.android.gms.tasks.Task;
import com.google.android.material.appbar.MaterialToolbar;
import com.google.android.material.button.MaterialButton;
import com.google.android.material.datepicker.MaterialDatePicker;
import com.google.android.material.textfield.TextInputEditText;
import com.google.android.material.textfield.TextInputLayout;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;
import java.util.TimeZone;

/** Pantalla 3: agregar o editar una sesión de estudio. */
public class SesionFormActivity extends AppCompatActivity {

    private TextInputLayout tilCurso, tilHoras;
    private TextInputEditText etCurso, etHoras;
    private Spinner spTipo;
    private MaterialButton btnFecha, btnGuardar;

    private String id;   // null = sesión nueva; con valor = se está editando
    private Date fecha;  // null = aún no se eligió
    private final SimpleDateFormat fmt = new SimpleDateFormat("dd/MM/yyyy", new Locale("es", "PE"));

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_sesion_form);

        MaterialToolbar toolbar = findViewById(R.id.toolbar);
        Ui.aplicarInsets(findViewById(R.id.main), toolbar);
        toolbar.setNavigationOnClickListener(v -> finish());

        tilCurso = findViewById(R.id.tilCurso);
        tilHoras = findViewById(R.id.tilHoras);
        etCurso = findViewById(R.id.etCurso);
        etHoras = findViewById(R.id.etHoras);
        spTipo = findViewById(R.id.spTipo);
        btnFecha = findViewById(R.id.btnFecha);
        btnGuardar = findViewById(R.id.btnGuardar);

        id = getIntent().getStringExtra("id");
        if (id != null) {
            toolbar.setTitle("Editar sesión");
            etCurso.setText(getIntent().getStringExtra("curso"));
            etHoras.setText(SesionAdapter.horasTexto(getIntent().getDoubleExtra("horas", 0)));
            fecha = new Date(getIntent().getLongExtra("fecha", 0));
            btnFecha.setText(fmt.format(fecha));

            String tipo = getIntent().getStringExtra("tipo");
            String[] tipos = getResources().getStringArray(R.array.tipos);
            for (int i = 0; i < tipos.length; i++) {
                if (tipos[i].equals(tipo)) spTipo.setSelection(i);
            }
        } else {
            toolbar.setTitle("Nueva sesión");
        }

        btnFecha.setOnClickListener(v -> elegirFecha());
        btnGuardar.setOnClickListener(v -> guardar());
    }

    private void elegirFecha() {
        MaterialDatePicker<Long> picker = MaterialDatePicker.Builder.datePicker()
                .setTitleText("Fecha de estudio")
                .build();
        picker.addOnPositiveButtonClickListener(utcMillis -> {
            // El selector devuelve la medianoche en UTC: se ajusta a la medianoche local
            long local = utcMillis - TimeZone.getDefault().getOffset(utcMillis);
            fecha = new Date(local);
            btnFecha.setText(fmt.format(fecha));
        });
        picker.show(getSupportFragmentManager(), "fecha");
    }

    private void guardar() {
        tilCurso.setError(null);
        tilHoras.setError(null);

        String curso = etCurso.getText() == null ? "" : etCurso.getText().toString().trim();
        String horasTxt = etHoras.getText() == null ? "" : etHoras.getText().toString().trim();

        if (curso.isEmpty()) {
            tilCurso.setError("Ingresa el curso");
            return;
        }
        if (fecha == null) {
            Toast.makeText(this, "Selecciona una fecha", Toast.LENGTH_SHORT).show();
            return;
        }
        double horas;
        try {
            horas = Double.parseDouble(horasTxt.replace(',', '.'));
        } catch (NumberFormatException e) {
            tilHoras.setError("Ingresa un número válido");
            return;
        }
        if (horas <= 0 || horas > 24) {
            tilHoras.setError("Debe estar entre 0 y 24 horas");
            return;
        }

        Sesion s = new Sesion(curso, fecha, horas, spTipo.getSelectedItem().toString());
        btnGuardar.setEnabled(false);
        btnGuardar.setText("Guardando…");

        Task<?> tarea = (id == null)
                ? Refs.sesiones().add(s)               // nueva: Firestore crea el id
                : Refs.sesiones().document(id).set(s); // existente: se reemplaza
        tarea.addOnSuccessListener(r -> finish())
                .addOnFailureListener(e -> {
                    btnGuardar.setEnabled(true);
                    btnGuardar.setText("Guardar");
                    Toast.makeText(this, "No se pudo guardar: " + e.getMessage(),
                            Toast.LENGTH_LONG).show();
                });
    }
}
