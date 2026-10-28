package br.com.gestaosuprimentos.util

import android.content.Context
import android.widget.Toast
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

/**
 * Mostra um Toast a partir de uma coroutine que está rodando no Dispatchers.IO.
 * Toast (e qualquer coisa de tela) só pode ser feito na thread principal (Dispatchers.Main).
 */
suspend fun avisar(context: Context, mensagem: String) = withContext(Dispatchers.Main) {
    Toast.makeText(context, mensagem, Toast.LENGTH_SHORT).show()
}
