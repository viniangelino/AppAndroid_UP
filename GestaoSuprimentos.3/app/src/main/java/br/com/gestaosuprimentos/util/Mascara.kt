package br.com.gestaosuprimentos.util

import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.input.OffsetMapping
import androidx.compose.ui.text.input.TransformedText
import androidx.compose.ui.text.input.VisualTransformation

/**
 * Máscara visual para campos numéricos. O estado guarda só os dígitos;
 * a máscara só muda o que aparece na tela. '#' = posição de um dígito.
 * Ex.: Mascara("###.###.###-##") para CPF, Mascara("##/##/####") para data.
 */
class Mascara(private val padrao: String) : VisualTransformation {

    private val maxDigitos = padrao.count { it == '#' }

    override fun filter(text: AnnotatedString): TransformedText {
        val digitos = text.text.take(maxDigitos)
        val saida = StringBuilder()
        val ehDigito = mutableListOf<Boolean>()
        val origParaTrans = IntArray(digitos.length + 1)

        var i = 0
        for (c in padrao) {
            if (i >= digitos.length) break
            if (c == '#') {
                origParaTrans[i] = saida.length
                saida.append(digitos[i])
                ehDigito.add(true)
                i++
            } else {
                saida.append(c)
                ehDigito.add(false)
            }
        }
        origParaTrans[digitos.length] = saida.length

        val transParaOrig = IntArray(saida.length + 1)
        var contagem = 0
        for (t in 0..saida.length) {
            transParaOrig[t] = contagem
            if (t < saida.length && ehDigito[t]) contagem++
        }

        val mapa = object : OffsetMapping {
            override fun originalToTransformed(offset: Int) = origParaTrans[offset.coerceIn(0, digitos.length)]
            override fun transformedToOriginal(offset: Int) = transParaOrig[offset.coerceIn(0, saida.length)]
        }
        return TransformedText(AnnotatedString(saida.toString()), mapa)
    }
}

/** Aplica o padrão a uma string de dígitos (para exibir fora de campos de texto). */
fun aplicarPadrao(digitos: String, padrao: String): String =
    Mascara(padrao).filter(AnnotatedString(digitos)).text.text
