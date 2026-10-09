package com.example.pokedex.views.util

import android.view.View
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.core.view.updatePadding

/**
 * Com edge-to-edge (padrão no Android 15+), o conteúdo pode ficar sob a barra de navegação.
 * Aplicamos apenas o espaçamento lateral e inferior; o topo já é tratado pela ActionBar.
 */
fun View.applySystemBarsPadding() {
    ViewCompat.setOnApplyWindowInsetsListener(this) { view, insets ->
        val bars = insets.getInsets(
            WindowInsetsCompat.Type.systemBars() or WindowInsetsCompat.Type.displayCutout()
        )
        view.updatePadding(left = bars.left, right = bars.right, bottom = bars.bottom)
        insets
    }
}
