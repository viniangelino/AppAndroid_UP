package br.com.gestaosuprimentos.viewmodel

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import br.com.gestaosuprimentos.data.repository.ClienteRepository
import br.com.gestaosuprimentos.data.repository.FornecedorRepository
import br.com.gestaosuprimentos.util.ehMatriz
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.launch

/** Números do resumo no topo do Menu. */
data class MenuUiState(
    val totalClientes: Int = 0,
    val clientesAtivos: Int = 0,
    val totalFornecedores: Int = 0,
    val matrizes: Int = 0
)

class MenuViewModel(
    clienteRepository: ClienteRepository,
    fornecedorRepository: FornecedorRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow(MenuUiState())
    val uiState: StateFlow<MenuUiState> = _uiState.asStateFlow()

    init {
        // Junta os dois Flows: qualquer mudança em clientes ou fornecedores atualiza o resumo
        viewModelScope.launch {
            combine(clienteRepository.buscarTodos(), fornecedorRepository.buscarTodos()) { clientes, fornecedores ->
                MenuUiState(
                    totalClientes = clientes.size,
                    clientesAtivos = clientes.count { it.ativo },
                    totalFornecedores = fornecedores.size,
                    matrizes = fornecedores.count { ehMatriz(it.cnpj) }
                )
            }
                .catch { e -> Log.e("MenuViewModel", "Erro ao carregar resumo", e) }
                .collect { resumo -> _uiState.value = resumo }
        }
    }
}
