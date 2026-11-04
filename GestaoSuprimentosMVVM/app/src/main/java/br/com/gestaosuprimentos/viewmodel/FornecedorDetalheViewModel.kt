package br.com.gestaosuprimentos.viewmodel

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import br.com.gestaosuprimentos.data.local.CATEGORIAS_FORNECEDOR
import br.com.gestaosuprimentos.data.local.Fornecedor
import br.com.gestaosuprimentos.data.repository.FornecedorRepository
import br.com.gestaosuprimentos.util.raizCnpj
import br.com.gestaosuprimentos.util.validarFornecedor
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

private const val TAG = "FornecedorDetalheVM"

/** Texto dos campos do formulário de fornecedor (CNPJ e prazo só com dígitos). */
data class FornecedorForm(
    val razaoSocial: String = "",
    val cnpj: String = "",
    val categoria: String = CATEGORIAS_FORNECEDOR.first(),
    val prazo: String = "",
    val observacoes: String = "",
    val erros: Map<String, String> = emptyMap()
)

/** Estado das telas de cadastro e de detalhe/edição de fornecedor. */
data class FornecedorDetalheUiState(
    val fornecedor: Fornecedor? = null,
    val mesmaEmpresa: List<Fornecedor> = emptyList(),   // outros estabelecimentos (mesma raiz de CNPJ)
    val carregando: Boolean = true,
    val editando: Boolean = false,
    val form: FornecedorForm = FornecedorForm(),
    val salvando: Boolean = false,
    val confirmandoRemocao: Boolean = false,
    val idCriado: Int? = null,
    val removido: Boolean = false,
    val mensagem: String? = null
)

class FornecedorDetalheViewModel(private val repository: FornecedorRepository) : ViewModel() {

    private val _uiState = MutableStateFlow(FornecedorDetalheUiState())
    val uiState: StateFlow<FornecedorDetalheUiState> = _uiState.asStateFlow()

    /** Busca o fornecedor pelo id e, junto, os outros estabelecimentos da mesma empresa. */
    fun carregar(id: Int) {
        viewModelScope.launch {
            try {
                buscarNoBanco(id)
            } catch (e: Exception) {
                Log.e(TAG, "Erro ao buscar fornecedor $id", e)
                _uiState.update { it.copy(carregando = false, mensagem = "Erro ao carregar fornecedor") }
            }
        }
    }

    fun alterarForm(form: FornecedorForm) = _uiState.update { it.copy(form = form) }

    // ---------- Cadastro ----------

    fun cadastrar() {
        val form = _uiState.value.form
        if (_uiState.value.salvando || !formValido(form)) return
        _uiState.update { it.copy(salvando = true) }

        viewModelScope.launch {
            try {
                if (repository.buscarPorCnpj(form.cnpj) != null) {
                    erroNoCampo("cnpj", "CNPJ já cadastrado")
                } else {
                    val id = repository.inserir(
                        Fornecedor(
                            razaoSocial = form.razaoSocial.trim(),
                            cnpj = form.cnpj,
                            categoria = form.categoria,
                            prazoEntregaDias = form.prazo.toInt(),
                            observacoes = form.observacoes.trim()
                        )
                    )
                    _uiState.update { it.copy(idCriado = id, mensagem = "Fornecedor salvo no banco!") }
                }
            } catch (e: Exception) {
                Log.e(TAG, "Erro ao inserir fornecedor", e)
                _uiState.update { it.copy(salvando = false, mensagem = "Erro ao salvar fornecedor") }
            }
        }
    }

    // ---------- Edição no detalhe ----------

    fun iniciarEdicao() {
        val f = _uiState.value.fornecedor ?: return
        _uiState.update {
            it.copy(
                editando = true,
                form = FornecedorForm(
                    razaoSocial = f.razaoSocial, cnpj = f.cnpj, categoria = f.categoria,
                    prazo = f.prazoEntregaDias.toString(), observacoes = f.observacoes
                )
            )
        }
    }

    fun cancelarEdicao() = _uiState.update { it.copy(editando = false) }

    fun salvarEdicao() {
        val atual = _uiState.value.fornecedor ?: return
        val form = _uiState.value.form
        if (!formValido(form)) return
        val atualizado = atual.copy(
            razaoSocial = form.razaoSocial.trim(), cnpj = form.cnpj,
            categoria = form.categoria, prazoEntregaDias = form.prazo.toInt(),
            observacoes = form.observacoes.trim()
        )

        viewModelScope.launch {
            try {
                val outro = repository.buscarPorCnpj(atualizado.cnpj)
                if (outro != null && outro.id != atual.id) {
                    erroNoCampo("cnpj", "CNPJ já cadastrado")
                } else {
                    repository.atualizar(atualizado)
                    // Recarrega: se o CNPJ mudou, a lista "mesma empresa" muda junto
                    buscarNoBanco(atual.id)
                    _uiState.update { it.copy(editando = false, mensagem = "Alterações salvas") }
                }
            } catch (e: Exception) {
                Log.e(TAG, "Erro ao atualizar fornecedor ${atual.id}", e)
                _uiState.update { it.copy(mensagem = "Erro ao salvar alterações") }
            }
        }
    }

    // ---------- Remoção ----------

    fun pedirRemocao() = _uiState.update { it.copy(confirmandoRemocao = true) }

    fun cancelarRemocao() = _uiState.update { it.copy(confirmandoRemocao = false) }

    fun remover() {
        val atual = _uiState.value.fornecedor ?: return
        _uiState.update { it.copy(confirmandoRemocao = false) }
        viewModelScope.launch {
            try {
                repository.deletar(atual)
                _uiState.update { it.copy(removido = true) }
            } catch (e: Exception) {
                Log.e(TAG, "Erro ao remover fornecedor ${atual.id}", e)
                _uiState.update { it.copy(mensagem = "Erro ao remover fornecedor") }
            }
        }
    }

    fun mensagemMostrada() = _uiState.update { it.copy(mensagem = null) }

    // ---------- Apoio ----------

    private suspend fun buscarNoBanco(id: Int) {
        val f = repository.buscarPorId(id)
        val outros = if (f == null) emptyList()
        else repository.buscarPorRaizCnpj(raizCnpj(f.cnpj)).filter { it.id != f.id }
        _uiState.update { it.copy(fornecedor = f, mesmaEmpresa = outros, carregando = false) }
    }

    private fun formValido(form: FornecedorForm): Boolean {
        val erros = validarFornecedor(form.razaoSocial, form.cnpj, form.prazo)
        if (erros.isNotEmpty()) _uiState.update { it.copy(form = form.copy(erros = erros)) }
        return erros.isEmpty()
    }

    private fun erroNoCampo(campo: String, mensagem: String) = _uiState.update {
        it.copy(salvando = false, form = it.form.copy(erros = mapOf(campo to mensagem)))
    }
}
