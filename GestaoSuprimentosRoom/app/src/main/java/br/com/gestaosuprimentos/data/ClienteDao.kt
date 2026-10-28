package br.com.gestaosuprimentos.data

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.Query
import androidx.room.Update

@Dao
interface ClienteDao {

    /** Retorna o id gerado pelo banco (usado para abrir o detalhe do cliente novo). */
    @Insert
    suspend fun inserir(cliente: Cliente): Long

    @Query("SELECT * FROM clientes ORDER BY nome COLLATE NOCASE")
    suspend fun buscarTodos(): List<Cliente>

    @Update
    suspend fun atualizar(cliente: Cliente)

    @Delete
    suspend fun deletar(cliente: Cliente)

    // ---------- Consultas filtradas ----------

    /** Usada pela tela de detalhe: busca só o cliente do id que veio na rota. */
    @Query("SELECT * FROM clientes WHERE id = :id")
    suspend fun buscarPorId(id: Int): Cliente?

    /** Usada no cadastro/edição para não deixar dois clientes com o mesmo CPF. */
    @Query("SELECT * FROM clientes WHERE cpf = :cpf LIMIT 1")
    suspend fun buscarPorCpf(cpf: String): Cliente?
}
