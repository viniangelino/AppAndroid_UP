package br.com.gestaosuprimentos.data

import androidx.room.Entity
import androidx.room.PrimaryKey
import java.time.LocalDate

/**
 * Tabela "clientes". Mesma data class do Trabalho 2, agora com @Entity.
 * CPF, telefone e nascimento guardados só com dígitos.
 */
@Entity(tableName = "clientes")
data class Cliente(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val nome: String,
    val cpf: String,
    val telefone: String,
    val nascimento: String,          // ddMMaaaa (ou vazio)
    val ativo: Boolean = true,
    // O Room não salva LocalDate direto: guardamos o dia como número (LocalDate.toEpochDay)
    val dataCadastro: Long = LocalDate.now().toEpochDay()
)

/** Tabela "fornecedores". CNPJ guardado só com dígitos. */
@Entity(tableName = "fornecedores")
data class Fornecedor(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val razaoSocial: String,
    val cnpj: String,
    val categoria: String,
    val prazoEntregaDias: Int,       // dias úteis
    val observacoes: String,
    val dataCadastro: Long = LocalDate.now().toEpochDay()
)

/** Login continua só em memória: o enunciado diz que login não entra neste trabalho. */
data class Usuario(val login: String, val senha: String)

val CATEGORIAS_FORNECEDOR = listOf("Matéria-prima", "Embalagens", "Transporte", "Serviços", "Outros")
