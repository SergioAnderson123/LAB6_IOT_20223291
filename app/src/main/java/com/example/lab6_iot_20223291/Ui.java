package com.example.lab6_iot_20223291;

import android.view.View;

import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

/** Utilidad edge-to-edge: la barra azul queda debajo del reloj y el contenido sobre la barra de navegación. */
public class Ui {

    private Ui() {
    }

    /**
     * @param raiz  vista raíz de la pantalla (recibe el margen inferior)
     * @param barra barra azul superior (recibe el margen del reloj/notch, sumado a su padding original)
     */
    public static void aplicarInsets(View raiz, View barra) {
        final int pl = barra.getPaddingLeft();
        final int pt = barra.getPaddingTop();
        final int pr = barra.getPaddingRight();
        final int pb = barra.getPaddingBottom();

        ViewCompat.setOnApplyWindowInsetsListener(raiz, (v, insets) -> {
            Insets b = insets.getInsets(
                    WindowInsetsCompat.Type.systemBars() | WindowInsetsCompat.Type.displayCutout());
            barra.setPadding(pl + b.left, pt + b.top, pr + b.right, pb);
            v.setPadding(0, 0, 0, b.bottom);
            return insets;
        });
    }
}
