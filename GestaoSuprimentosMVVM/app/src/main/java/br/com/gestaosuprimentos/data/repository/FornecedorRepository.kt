package br.com.gestaosuprimentos.data.repository

import br.com.gestaosuprimentos.data.local.Fornecedor
import br.com.gestaosuprimentos.data.local.FornecedorDao
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.withContext

/** Única fonte de verdade dos fornecedores. Só ele fala com o FornecedorDao. */
class FornecedorRepository(private val dao: FornecedorDao) {

    fun buscarTodos(): Flow<List<Fornecedor>> = dao.buscarTodos()

    suspend fun buscarPorId(id: Int): Fornecedor? = withContext(Dispatchers.IO) {
        dao.buscarPorId(id)
    }

    suspend fun buscarPorCnpj(cnpj: String): Fornecedor? = withContext(Dispatchers.IO) {
        dao.buscarPorCnpj(cnpj)
    }

    /** Matriz e filiais: fornecedores com a mesma raiz de CNPJ (8 primeiros dígitos). */
    suspend fun buscarPorRaizCnpj(raiz: String): List<Fornecedor> = withContext(Dispatchers.IO) {
        dao.buscarPorRaizCnpj(raiz)
    }

    suspend fun inserir(fornecedor: Fornecedor): Int = withContext(Dispatchers.IO) {
        dao.inserir(fornecedor).toInt()
    }

    suspend fun atualizar(fornecedor: Fornecedor) = withContext(Dispatchers.IO) {
        dao.atualizar(fornecedor)
    }

    suspend fun deletar(fornecedor: Fornecedor) = withContext(Dispatchers.IO) {
        dao.deletar(fornecedor)
    }
}
