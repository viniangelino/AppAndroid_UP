package br.com.gestaosuprimentos.data

import java.time.LocalDate

/** Item da primeira lista do app. CPF, telefone e nascimento guardados só com dígitos. */
data class Cliente(
    val id: Int,
    val nome: String,
    val cpf: String,
    val telefone: String,
    val nascimento: String,          // ddMMaaaa (ou vazio)
    val ativo: Boolean = true,
    val dataCadastro: LocalDate = LocalDate.now()
)

/** Item da segunda lista do app. CNPJ guardado só com dígitos. */
data class Fornecedor(
    val id: Int,
    val razaoSocial: String,
    val cnpj: String,
    val categoria: String,
    val prazoEntregaDias: Int,       // dias úteis
    val observacoes: String,
    val dataCadastro: LocalDate = LocalDate.now()
)

data class Usuario(val login: String, val senha: String)

val CATEGORIAS_FORNECEDOR = listOf("Matéria-prima", "Embalagens", "Transporte", "Serviços", "Outros")
