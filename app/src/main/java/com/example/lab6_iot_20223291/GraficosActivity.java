package com.example.lab6_iot_20223291;

import android.graphics.Color;
import android.os.Bundle;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Spinner;

import androidx.appcompat.app.AppCompatActivity;

import com.github.mikephil.charting.charts.BarChart;
import com.github.mikephil.charting.charts.PieChart;
import com.github.mikephil.charting.components.XAxis;
import com.github.mikephil.charting.data.BarData;
import com.github.mikephil.charting.data.BarDataSet;
import com.github.mikephil.charting.data.BarEntry;
import com.github.mikephil.charting.data.PieData;
import com.github.mikephil.charting.data.PieDataSet;
import com.github.mikephil.charting.data.PieEntry;
import com.github.mikephil.charting.formatter.IndexAxisValueFormatter;
import com.github.mikephil.charting.formatter.PercentFormatter;
import com.github.mikephil.charting.utils.ColorTemplate;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.ListenerRegistration;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.TreeMap;
import java.util.TreeSet;

/**
 * Pantalla 4: gráficos con las sesiones guardadas.
 * Barras: horas por mes. Torta: proporción de horas por tipo de actividad. Ambos filtrables por curso.
 */
public class GraficosActivity extends AppCompatActivity {

    private static final String TODOS = "Todos los cursos";

    private BarChart barChart;
    private PieChart pieChart;
    private Spinner spCurso;
    private ArrayAdapter<String> cursoAdapter;

    private final List<Sesion> todas = new ArrayList<>();
    private String filtro = TODOS;
    private ListenerRegistration registro;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_graficos);
        View toolbar = findViewById(R.id.toolbar);
        Ui.aplicarInsets(findViewById(R.id.main), toolbar);
        ((com.google.android.material.appbar.MaterialToolbar) toolbar)
                .setNavigationOnClickListener(v -> finish());

        barChart = findViewById(R.id.barChart);
        pieChart = findViewById(R.id.pieChart);
        spCurso = findViewById(R.id.spCurso);

        cursoAdapter = new ArrayAdapter<>(this, android.R.layout.simple_spinner_dropdown_item,
                new ArrayList<>(Arrays.asList(TODOS)));
        spCurso.setAdapter(cursoAdapter);
        spCurso.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                filtro = cursoAdapter.getItem(position);
                dibujar();
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {
            }
        });

        // Los gráficos se actualizan solos cuando cambian las sesiones
        registro = Refs.sesiones().addSnapshotListener((snap, e) -> {
            if (e != null || snap == null) return;
            todas.clear();
            for (DocumentSnapshot doc : snap.getDocuments()) {
                Sesion s = doc.toObject(Sesion.class);
                if (s != null && s.getFecha() != null) todas.add(s);
            }
            actualizarCursos();
            dibujar();
        });
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (registro != null) registro.remove();
    }

    /** Llena el filtro con "Todos los cursos" y los cursos que existen en las sesiones. */
    private void actualizarCursos() {
        TreeSet<String> cursos = new TreeSet<>(String.CASE_INSENSITIVE_ORDER);
        for (Sesion s : todas) cursos.add(s.getCurso());

        List<String> items = new ArrayList<>();
        items.add(TODOS);
        items.addAll(cursos);

        cursoAdapter.clear();
        cursoAdapter.addAll(items);

        int idx = items.indexOf(filtro);
        if (idx < 0) {
            filtro = TODOS;
            idx = 0;
        }
        spCurso.setSelection(idx);
    }

    private void dibujar() {
        // 1) Aplicar el filtro por curso
        List<Sesion> datos = new ArrayList<>();
        for (Sesion s : todas) {
            if (filtro.equals(TODOS) || s.getCurso().equalsIgnoreCase(filtro)) datos.add(s);
        }

        dibujarBarras(datos);
        dibujarTorta(datos);
    }

    /** Barras: horas por mes (clave yyyy-MM para que el TreeMap las ordene cronológicamente). */
    private void dibujarBarras(List<Sesion> datos) {
        TreeMap<String, Float> porMes = new TreeMap<>();
        SimpleDateFormat clave = new SimpleDateFormat("yyyy-MM", Locale.US);
        for (Sesion s : datos) {
            porMes.merge(clave.format(s.getFecha()), (float) s.getHoras(), Float::sum);
        }

        List<BarEntry> barras = new ArrayList<>();
        List<String> etiquetas = new ArrayList<>();
        SimpleDateFormat mostrar = new SimpleDateFormat("MMM yy", new Locale("es", "PE"));
        int i = 0;
        for (Map.Entry<String, Float> en : porMes.entrySet()) {
            barras.add(new BarEntry(i++, en.getValue()));
            try {
                etiquetas.add(mostrar.format(clave.parse(en.getKey())));
            } catch (Exception ex) {
                etiquetas.add(en.getKey());
            }
        }

        if (barras.isEmpty()) {
            barChart.clear();
            barChart.setNoDataText("Sin datos para mostrar");
            barChart.invalidate();
            return;
        }

        BarDataSet set = new BarDataSet(barras, "Horas de estudio");
        set.setColor(Color.rgb(57, 73, 171));
        set.setValueTextSize(11f);
        barChart.setData(new BarData(set));

        XAxis x = barChart.getXAxis();
        x.setValueFormatter(new IndexAxisValueFormatter(etiquetas));
        x.setPosition(XAxis.XAxisPosition.BOTTOM);
        x.setGranularity(1f);
        x.setDrawGridLines(false);

        barChart.getAxisRight().setEnabled(false);
        barChart.getAxisLeft().setAxisMinimum(0f);
        barChart.getDescription().setEnabled(false);
        barChart.animateY(400);
        barChart.invalidate();
    }

    /** Torta: proporción de horas por tipo de actividad (Teoría, Práctica, Laboratorio). */
    private void dibujarTorta(List<Sesion> datos) {
        Map<String, Float> porTipo = new LinkedHashMap<>();
        for (Sesion s : datos) {
            porTipo.merge(s.getTipo(), (float) s.getHoras(), Float::sum);
        }

        List<PieEntry> porciones = new ArrayList<>();
        for (Map.Entry<String, Float> en : porTipo.entrySet()) {
            porciones.add(new PieEntry(en.getValue(), en.getKey()));
        }

        if (porciones.isEmpty()) {
            pieChart.clear();
            pieChart.setNoDataText("Sin datos para mostrar");
            pieChart.invalidate();
            return;
        }

        PieDataSet set = new PieDataSet(porciones, "");
        set.setColors(ColorTemplate.MATERIAL_COLORS);
        set.setValueTextSize(13f);
        set.setValueTextColor(Color.WHITE);

        PieData pie = new PieData(set);
        pie.setValueFormatter(new PercentFormatter(pieChart));
        pieChart.setUsePercentValues(true);
        pieChart.setData(pie);
        pieChart.getDescription().setEnabled(false);
        pieChart.setEntryLabelColor(Color.WHITE);
        pieChart.animateY(400);
        pieChart.invalidate();
    }
}
