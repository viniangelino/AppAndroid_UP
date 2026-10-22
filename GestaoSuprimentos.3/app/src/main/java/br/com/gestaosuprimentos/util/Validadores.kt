package br.com.gestaosuprimentos.util

import java.time.DayOfWeek
import java.time.LocalDate
import java.time.Period
import java.time.format.DateTimeFormatter
import java.time.format.ResolverStyle
import java.time.temporal.ChronoUnit
import java.util.Locale

private val FORMATO_DATA = DateTimeFormatter.ofPattern("dd/MM/yyyy")
private val LEITURA_NASCIMENTO = DateTimeFormatter.ofPattern("ddMMuuuu").withResolverStyle(ResolverStyle.STRICT)
private val PT_BR = Locale("pt", "BR")

/** Mantém só números e corta no tamanho máximo. Usado em todos os campos numéricos. */
fun soDigitos(texto: String, max: Int) = texto.filter { it.isDigit() }.take(max)

// ---------- CPF / CNPJ ----------

fun cpfValido(cpf: String): Boolean {
    if (cpf.length != 11 || cpf.all { it == cpf[0] }) return false
    val d = cpf.map { it.digitToInt() }
    fun dv(n: Int): Int {
        val resto = (0 until n).sumOf { d[it] * (n + 1 - it) } % 11
        return if (resto < 2) 0 else 11 - resto
    }
    return d[9] == dv(9) && d[10] == dv(10)
}

fun cnpjValido(cnpj: String): Boolean {
    if (cnpj.length != 14 || cnpj.all { it == cnpj[0] }) return false
    val d = cnpj.map { it.digitToInt() }
    val pesos1 = listOf(5, 4, 3, 2, 9, 8, 7, 6, 5, 4, 3, 2)
    val pesos2 = listOf(6) + pesos1
    fun dv(pesos: List<Int>): Int {
        val resto = pesos.indices.sumOf { d[it] * pesos[it] } % 11
        return if (resto < 2) 0 else 11 - resto
    }
    return d[12] == dv(pesos1) && d[13] == dv(pesos2)
}

/** Os 8 primeiros dígitos do CNPJ identificam a empresa; os 4 seguintes, o estabelecimento. */
fun raizCnpj(cnpj: String) = cnpj.take(8)
fun numeroEstabelecimento(cnpj: String) = if (cnpj.length >= 12) cnpj.substring(8, 12).toInt() else 0
fun ehMatriz(cnpj: String) = numeroEstabelecimento(cnpj) == 1

// ---------- Formatação ----------

fun formatarCpf(cpf: String) = aplicarPadrao(cpf, "###.###.###-##")
fun formatarCnpj(cnpj: String) = aplicarPadrao(cnpj, "##.###.###/####-##")
fun formatarRaizCnpj(cnpj: String) = aplicarPadrao(raizCnpj(cnpj), "##.###.###")
fun padraoTelefone(tel: String) = if (tel.length <= 10) "(##) ####-####" else "(##) #####-####"
fun formatarTelefone(tel: String) = if (tel.isEmpty()) "Não informado" else aplicarPadrao(tel, padraoTelefone(tel))
fun formatarData(data: LocalDate): String = data.format(FORMATO_DATA)

fun formatarDataComDia(data: LocalDate): String {
    val dia = data.dayOfWeek.getDisplayName(java.time.format.TextStyle.FULL, PT_BR)
    return "${formatarData(data)} ($dia)"
}

fun iniciais(nome: String) =
    nome.split(" ").filter { it.isNotBlank() }.take(2).joinToString("") { it.first().uppercase() }

// ---------- Datas e cálculos ----------

fun lerNascimento(digitos: String): LocalDate? =
    if (digitos.length != 8) null
    else try {
        LocalDate.parse(digitos, LEITURA_NASCIMENTO)
    } catch (e: Exception) {
        null
    }

fun formatarNascimento(digitos: String) = lerNascimento(digitos)?.let { formatarData(it) } ?: "Não informado"

fun idade(nascimento: LocalDate) = Period.between(nascimento, LocalDate.now()).years

fun diasParaAniversario(nascimento: LocalDate): Long {
    val hoje = LocalDate.now()
    var proximo = nascimento.withYear(hoje.year)
    if (proximo.isBefore(hoje)) proximo = nascimento.withYear(hoje.year + 1)
    return ChronoUnit.DAYS.between(hoje, proximo)
}

fun diasDesde(data: LocalDate) = ChronoUnit.DAYS.between(data, LocalDate.now())

/** Soma dias úteis a partir de hoje, pulando sábado e domingo. */
fun previsaoEntrega(diasUteis: Int): LocalDate {
    var data = LocalDate.now()
    var restantes = diasUteis
    while (restantes > 0) {
        data = data.plusDays(1)
        if (data.dayOfWeek != DayOfWeek.SATURDAY && data.dayOfWeek != DayOfWeek.SUNDAY) restantes--
    }
    return data
}
