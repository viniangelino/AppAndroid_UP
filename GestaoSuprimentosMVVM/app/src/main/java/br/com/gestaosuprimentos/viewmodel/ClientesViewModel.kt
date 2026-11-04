package br.com.gestaosuprimentos.viewmodel

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import br.com.gestaosuprimentos.data.local.Cliente
import br.com.gestaosuprimentos.data.repository.ClienteRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

private const val TAG = "ClientesViewModel"

/** Tudo o que a tela de lista de clientes precisa para se desenhar. */
data class ClientesUiState(
    val clientes: List<Cliente> = emptyList(),
    val carregando: Boolean = true,
    val buscaCpf: String = "",
    val buscaNome: String = "",
    val mensagem: String? = null          // Toast a mostrar (null = nenhum)
) {
    /** Lista já filtrada pelos campos de busca. */
    val filtrados: List<Cliente>
        get() = clientes.filter {
            it.cpf.contains(buscaCpf) &&
                (buscaNome.isBlank() || it.nome.contains(buscaNome.trim(), ignoreCase = true))
        }

    val ativos: Int get() = clientes.count { it.ativo }
}

class ClientesViewModel(private val repository: ClienteRepository) : ViewModel() {

    private val _uiState = MutableStateFlow(ClientesUiState())
    val uiState: StateFlow<ClientesUiState> = _uiState.asStateFlow()

    init {
        // O Flow do Room entrega a lista nova sozinho a cada inserir/atualizar/deletar
        viewModelScope.launch {
            repository.buscarTodos()
                .catch { e ->
                    Log.e(TAG, "Erro ao buscar clientes", e)
                    _uiState.update { it.copy(carregando = false, mensagem = "Erro ao carregar clientes") }
                }
                .collect { lista ->
                    _uiState.update { it.copy(clientes = lista, carregando = false) }
                }
        }
    }

    fun alterarBuscaCpf(valor: String) = _uiState.update { it.copy(buscaCpf = valor) }

    fun alterarBuscaNome(valor: String) = _uiState.update { it.copy(buscaNome = valor) }

    fun alternarAtivo(cliente: Cliente) {
        viewModelScope.launch {
            try {
                repository.atualizar(cliente.copy(ativo = !cliente.ativo))
            } catch (e: Exception) {
                Log.e(TAG, "Erro ao atualizar cliente ${cliente.id}", e)
                _uiState.update { it.copy(mensagem = "Erro ao salvar alteração") }
            }
        }
    }

    fun removerCliente(cliente: Cliente) {
        viewModelScope.launch {
            try {
                repository.deletar(cliente)
                _uiState.update { it.copy(mensagem = "${cliente.nome} removido") }
            } catch (e: Exception) {
                Log.e(TAG, "Erro ao remover cliente ${cliente.id}", e)
                _uiState.update { it.copy(mensagem = "Erro ao remover cliente") }
            }
        }
    }

    fun mensagemMostrada() = _uiState.update { it.copy(mensagem = null) }
}
