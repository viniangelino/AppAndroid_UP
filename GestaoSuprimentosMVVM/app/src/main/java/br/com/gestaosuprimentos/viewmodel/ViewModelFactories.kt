package br.com.gestaosuprimentos.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import br.com.gestaosuprimentos.data.repository.ClienteRepository
import br.com.gestaosuprimentos.data.repository.FornecedorRepository

/*
 * O Android não sabe criar sozinho um ViewModel que recebe parâmetro no construtor.
 * Cada Factory recebe o Repository certo e entrega para o ViewModel na hora da criação.
 * Na tela: viewModel(factory = ClienteViewModelFactory(repository))
 */

/** Cria os ViewModels de cliente (lista e detalhe), passando o ClienteRepository. */
class ClienteViewModelFactory(private val repository: ClienteRepository) : ViewModelProvider.Factory {
    @Suppress("UNCHECKED_CAST")
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        return when {
            modelClass.isAssignableFrom(ClientesViewModel::class.java) ->
                ClientesViewModel(repository) as T
            modelClass.isAssignableFrom(ClienteDetalheViewModel::class.java) ->
                ClienteDetalheViewModel(repository) as T
            else -> throw IllegalArgumentException("ViewModel desconhecido: ${modelClass.name}")
        }
    }
}

/** Cria os ViewModels de fornecedor (lista e detalhe), passando o FornecedorRepository. */
class FornecedorViewModelFactory(private val repository: FornecedorRepository) : ViewModelProvider.Factory {
    @Suppress("UNCHECKED_CAST")
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        return when {
            modelClass.isAssignableFrom(FornecedoresViewModel::class.java) ->
                FornecedoresViewModel(repository) as T
            modelClass.isAssignableFrom(FornecedorDetalheViewModel::class.java) ->
                FornecedorDetalheViewModel(repository) as T
            else -> throw IllegalArgumentException("ViewModel desconhecido: ${modelClass.name}")
        }
    }
}

/** O Menu mostra números das duas tabelas, então recebe os dois Repository. */
class MenuViewModelFactory(
    private val clienteRepository: ClienteRepository,
    private val fornecedorRepository: FornecedorRepository
) : ViewModelProvider.Factory {
    @Suppress("UNCHECKED_CAST")
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(MenuViewModel::class.java)) {
            return MenuViewModel(clienteRepository, fornecedorRepository) as T
        }
        throw IllegalArgumentException("ViewModel desconhecido: ${modelClass.name}")
    }
}
