package com.example.lab6_iot_20223291;

import android.graphics.Color;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.RecyclerView;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

public class SesionAdapter extends RecyclerView.Adapter<SesionAdapter.VH> {

    public interface Listener {
        void onEditar(Sesion s);

        void onEliminar(Sesion s);
    }

    private final List<Sesion> datos = new ArrayList<>();
    private final Listener listener;
    private final SimpleDateFormat fmt = new SimpleDateFormat("dd/MM/yyyy", new Locale("es", "PE"));

    public SesionAdapter(Listener listener) {
        this.listener = listener;
    }

    public void setDatos(List<Sesion> nuevos) {
        datos.clear();
        datos.addAll(nuevos);
        notifyDataSetChanged();
    }

    /** "2" si son horas exactas, "1.5" si tienen decimales. */
    public static String horasTexto(double h) {
        if (h == Math.floor(h)) return String.valueOf((long) h);
        return String.format(Locale.US, "%.1f", h);
    }

    @NonNull
    @Override
    public VH onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View v = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_sesion, parent, false);
        return new VH(v);
    }

    @Override
    public void onBindViewHolder(@NonNull VH h, int position) {
        Sesion s = datos.get(position);
        h.curso.setText(s.getCurso());
        h.detalle.setText(fmt.format(s.getFecha()) + " · " + horasTexto(s.getHoras()) + " h");

        // Etiqueta de color según el tipo de actividad
        h.tipo.setText(s.getTipo());
        int fondo, texto;
        if ("Laboratorio".equals(s.getTipo())) {
            fondo = R.drawable.bg_chip_lab;
            texto = R.color.chip_lab_texto;
        } else if ("Práctica".equals(s.getTipo())) {
            fondo = R.drawable.bg_chip_pra;
            texto = R.color.chip_pra_texto;
        } else {
            fondo = R.drawable.bg_chip_teo;
            texto = R.color.chip_teo_texto;
        }
        h.tipo.setBackgroundResource(fondo);
        h.tipo.setTextColor(ContextCompat.getColor(h.itemView.getContext(), texto));

        h.editar.setOnClickListener(v -> listener.onEditar(s));
        h.eliminar.setOnClickListener(v -> listener.onEliminar(s));
    }

    @Override
    public int getItemCount() {
        return datos.size();
    }

    static class VH extends RecyclerView.ViewHolder {
        final TextView curso, detalle, tipo;
        final ImageButton editar, eliminar;

        VH(@NonNull View v) {
            super(v);
            curso = v.findViewById(R.id.tvCurso);
            detalle = v.findViewById(R.id.tvDetalle);
            tipo = v.findViewById(R.id.tvTipo);
            editar = v.findViewById(R.id.btnEditar);
            eliminar = v.findViewById(R.id.btnEliminar);
        }
    }
}
