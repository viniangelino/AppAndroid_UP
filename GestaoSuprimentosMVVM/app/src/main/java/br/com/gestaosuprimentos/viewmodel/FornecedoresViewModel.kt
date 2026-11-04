package br.com.gestaosuprimentos.viewmodel

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import br.com.gestaosuprimentos.data.local.Fornecedor
import br.com.gestaosuprimentos.data.repository.FornecedorRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

private const val TAG = "FornecedoresViewModel"

/** Tudo o que a tela de lista de fornecedores precisa para se desenhar. */
data class FornecedoresUiState(
    val fornecedores: List<Fornecedor> = emptyList(),
    val carregando: Boolean = true,
    val buscaCnpj: String = "",
    val buscaRazao: String = "",
    val paraRemover: Fornecedor? = null,   // fornecedor do diálogo de confirmação (clique longo)
    val mensagem: String? = null
) {
    val filtrados: List<Fornecedor>
        get() = fornecedores.filter {
            it.cnpj.contains(buscaCnpj) &&
                (buscaRazao.isBlank() || it.razaoSocial.contains(buscaRazao.trim(), ignoreCase = true))
        }
}

class FornecedoresViewModel(private val repository: FornecedorRepository) : ViewModel() {

    private val _uiState = MutableStateFlow(FornecedoresUiState())
    val uiState: StateFlow<FornecedoresUiState> = _uiState.asStateFlow()

    init {
        viewModelScope.launch {
            repository.buscarTodos()
                .catch { e ->
                    Log.e(TAG, "Erro ao buscar fornecedores", e)
                    _uiState.update { it.copy(carregando = false, mensagem = "Erro ao carregar fornecedores") }
                }
                .collect { lista ->
                    _uiState.update { it.copy(fornecedores = lista, carregando = false) }
                }
        }
    }

    fun alterarBuscaCnpj(valor: String) = _uiState.update { it.copy(buscaCnpj = valor) }

    fun alterarBuscaRazao(valor: String) = _uiState.update { it.copy(buscaRazao = valor) }

    fun pedirRemocao(fornecedor: Fornecedor) = _uiState.update { it.copy(paraRemover = fornecedor) }

    fun cancelarRemocao() = _uiState.update { it.copy(paraRemover = null) }

    fun confirmarRemocao() {
        val f = _uiState.value.paraRemover ?: return
        _uiState.update { it.copy(paraRemover = null) }
        viewModelScope.launch {
            try {
                repository.deletar(f)
                _uiState.update { it.copy(mensagem = "${f.razaoSocial} removido") }
            } catch (e: Exception) {
                Log.e(TAG, "Erro ao remover fornecedor ${f.id}", e)
                _uiState.update { it.copy(mensagem = "Erro ao remover fornecedor") }
            }
        }
    }

    fun mensagemMostrada() = _uiState.update { it.copy(mensagem = null) }
}
