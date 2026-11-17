package com.example.lab6_iot_20223291;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.FirebaseFirestore;

/**
 * Ruta de las sesiones: usuarios/{uid}/sesiones/{id}.
 * Al colgar las sesiones del uid, cada usuario solo ve las suyas (y las reglas de Firestore lo exigen).
 */
public class Refs {

    private Refs() {
    }

    public static CollectionReference sesiones() {
        String uid = FirebaseAuth.getInstance().getCurrentUser().getUid();
        return FirebaseFirestore.getInstance()
                .collection("usuarios").document(uid).collection("sesiones");
    }
}
