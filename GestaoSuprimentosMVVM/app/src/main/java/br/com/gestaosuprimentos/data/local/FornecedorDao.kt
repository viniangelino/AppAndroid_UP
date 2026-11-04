package br.com.gestaosuprimentos.data.local

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.Query
import androidx.room.Update
import kotlinx.coroutines.flow.Flow

@Dao
interface FornecedorDao {

    @Insert
    suspend fun inserir(fornecedor: Fornecedor): Long

    /**
     * Flow: o Room avisa sozinho sempre que a tabela muda (inserir, atualizar, deletar).
     * Não é suspend porque o Flow já é assíncrono por natureza.
     */
    @Query("SELECT * FROM fornecedores ORDER BY razaoSocial COLLATE NOCASE")
    fun buscarTodos(): Flow<List<Fornecedor>>

    @Update
    suspend fun atualizar(fornecedor: Fornecedor)

    @Delete
    suspend fun deletar(fornecedor: Fornecedor)

    // ---------- Consultas filtradas ----------

    @Query("SELECT * FROM fornecedores WHERE id = :id")
    suspend fun buscarPorId(id: Int): Fornecedor?

    @Query("SELECT * FROM fornecedores WHERE cnpj = :cnpj LIMIT 1")
    suspend fun buscarPorCnpj(cnpj: String): Fornecedor?

    /**
     * Matriz e filiais da mesma empresa: os 8 primeiros dígitos do CNPJ (a "raiz") são iguais.
     * Ex.: raiz = "11444777" encontra 11444777000161 e 11444777000242.
     */
    @Query("SELECT * FROM fornecedores WHERE cnpj LIKE :raiz || '%' ORDER BY cnpj")
    suspend fun buscarPorRaizCnpj(raiz: String): List<Fornecedor>
}
