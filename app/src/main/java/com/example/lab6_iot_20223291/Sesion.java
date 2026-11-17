package com.example.lab6_iot_20223291;

import com.google.firebase.firestore.DocumentId;

import java.util.Date;

/** Una sesión de estudio guardada en Cloud Firestore. */
public class Sesion {

    @DocumentId
    private String id;
    private String curso;
    private Date fecha;
    private double horas;
    private String tipo; // Teoría, Práctica o Laboratorio

    /** Constructor vacío: obligatorio para que Firestore pueda leer el documento. */
    public Sesion() {
    }

    public Sesion(String curso, Date fecha, double horas, String tipo) {
        this.curso = curso;
        this.fecha = fecha;
        this.horas = horas;
        this.tipo = tipo;
    }

    public String getId() { return id; }
    public void setId(String id) { this.id = id; }

    public String getCurso() { return curso; }
    public void setCurso(String curso) { this.curso = curso; }

    public Date getFecha() { return fecha; }
    public void setFecha(Date fecha) { this.fecha = fecha; }

    public double getHoras() { return horas; }
    public void setHoras(double horas) { this.horas = horas; }

    public String getTipo() { return tipo; }
    public void setTipo(String tipo) { this.tipo = tipo; }
}
