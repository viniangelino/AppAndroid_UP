package br.com.gestaosuprimentos.data.repository

import br.com.gestaosuprimentos.data.local.Cliente
import br.com.gestaosuprimentos.data.local.ClienteDao
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.withContext

/**
 * Única fonte de verdade dos clientes.
 * Só o Repository fala com o DAO; o ViewModel fala só com o Repository.
 * Se um dia os dados vierem de uma API, só esta classe muda.
 */
class ClienteRepository(private val dao: ClienteDao) {

    fun buscarTodos(): Flow<List<Cliente>> = dao.buscarTodos()

    suspend fun buscarPorId(id: Int): Cliente? = withContext(Dispatchers.IO) {
        dao.buscarPorId(id)
    }

    suspend fun buscarPorCpf(cpf: String): Cliente? = withContext(Dispatchers.IO) {
        dao.buscarPorCpf(cpf)
    }

    /** Retorna o id gerado pelo banco. */
    suspend fun inserir(cliente: Cliente): Int = withContext(Dispatchers.IO) {
        dao.inserir(cliente).toInt()
    }

    suspend fun atualizar(cliente: Cliente) = withContext(Dispatchers.IO) {
        dao.atualizar(cliente)
    }

    suspend fun deletar(cliente: Cliente) = withContext(Dispatchers.IO) {
        dao.deletar(cliente)
    }
}
