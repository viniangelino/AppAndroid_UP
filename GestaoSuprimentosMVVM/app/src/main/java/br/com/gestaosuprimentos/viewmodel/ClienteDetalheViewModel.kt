package br.com.gestaosuprimentos.viewmodel

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import br.com.gestaosuprimentos.data.local.Cliente
import br.com.gestaosuprimentos.data.repository.ClienteRepository
import br.com.gestaosuprimentos.util.validarCliente
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

private const val TAG = "ClienteDetalheViewModel"

/** Texto dos campos do formulário de cliente (CPF, telefone e nascimento só com dígitos). */
data class ClienteForm(
    val nome: String = "",
    val cpf: String = "",
    val telefone: String = "",
    val nascimento: String = "",
    val erros: Map<String, String> = emptyMap()
)

/** Estado das telas de cadastro e de detalhe/edição de cliente. */
data class ClienteDetalheUiState(
    val cliente: Cliente? = null,          // cliente lido do banco (detalhe)
    val carregando: Boolean = true,
    val editando: Boolean = false,
    val form: ClienteForm = ClienteForm(),
    val salvando: Boolean = false,
    val confirmandoRemocao: Boolean = false,
    val idCriado: Int? = null,             // cadastro concluído: a tela abre o detalhe deste id
    val removido: Boolean = false,         // remoção concluída: a tela volta
    val mensagem: String? = null
)

/** Usado pela tela de cadastro e pela tela de detalhe (que também edita). */
class ClienteDetalheViewModel(private val repository: ClienteRepository) : ViewModel() {

    private val _uiState = MutableStateFlow(ClienteDetalheUiState())
    val uiState: StateFlow<ClienteDetalheUiState> = _uiState.asStateFlow()

    /** Busca o cliente direto no banco pelo id que veio na rota. */
    fun carregar(id: Int) {
        viewModelScope.launch {
            try {
                val cliente = repository.buscarPorId(id)
                _uiState.update { it.copy(cliente = cliente, carregando = false) }
            } catch (e: Exception) {
                Log.e(TAG, "Erro ao buscar cliente $id", e)
                _uiState.update { it.copy(carregando = false, mensagem = "Erro ao carregar cliente") }
            }
        }
    }

    fun alterarForm(form: ClienteForm) = _uiState.update { it.copy(form = form) }

    // ---------- Cadastro ----------

    fun cadastrar() {
        val form = _uiState.value.form
        if (_uiState.value.salvando || !formValido(form)) return
        _uiState.update { it.copy(salvando = true) }

        viewModelScope.launch {
            try {
                if (repository.buscarPorCpf(form.cpf) != null) {
                    erroNoCampo("cpf", "CPF já cadastrado")
                } else {
                    val id = repository.inserir(
                        Cliente(
                            nome = form.nome.trim(),
                            cpf = form.cpf,
                            telefone = form.telefone,
                            nascimento = form.nascimento
                        )
                    )
                    _uiState.update { it.copy(idCriado = id, mensagem = "Cliente salvo no banco!") }
                }
            } catch (e: Exception) {
                Log.e(TAG, "Erro ao inserir cliente", e)
                _uiState.update { it.copy(salvando = false, mensagem = "Erro ao salvar cliente") }
            }
        }
    }

    // ---------- Edição no detalhe ----------

    fun iniciarEdicao() {
        val c = _uiState.value.cliente ?: return
        _uiState.update {
            it.copy(
                editando = true,
                form = ClienteForm(nome = c.nome, cpf = c.cpf, telefone = c.telefone, nascimento = c.nascimento)
            )
        }
    }

    fun cancelarEdicao() = _uiState.update { it.copy(editando = false) }

    fun salvarEdicao() {
        val atual = _uiState.value.cliente ?: return
        val form = _uiState.value.form
        if (!formValido(form)) return
        val atualizado = atual.copy(
            nome = form.nome.trim(), cpf = form.cpf,
            telefone = form.telefone, nascimento = form.nascimento
        )

        viewModelScope.launch {
            try {
                val outro = repository.buscarPorCpf(atualizado.cpf)
                if (outro != null && outro.id != atual.id) {
                    erroNoCampo("cpf", "CPF já cadastrado")
                } else {
                    repository.atualizar(atualizado)
                    _uiState.update {
                        it.copy(cliente = atualizado, editando = false, mensagem = "Alterações salvas")
                    }
                }
            } catch (e: Exception) {
                Log.e(TAG, "Erro ao atualizar cliente ${atual.id}", e)
                _uiState.update { it.copy(mensagem = "Erro ao salvar alterações") }
            }
        }
    }

    fun alternarAtivo() {
        val atual = _uiState.value.cliente ?: return
        val atualizado = atual.copy(ativo = !atual.ativo)
        viewModelScope.launch {
            try {
                repository.atualizar(atualizado)
                _uiState.update { it.copy(cliente = atualizado) }
            } catch (e: Exception) {
                Log.e(TAG, "Erro ao atualizar cliente ${atual.id}", e)
                _uiState.update { it.copy(mensagem = "Erro ao salvar alteração") }
            }
        }
    }

    // ---------- Remoção ----------

    fun pedirRemocao() = _uiState.update { it.copy(confirmandoRemocao = true) }

    fun cancelarRemocao() = _uiState.update { it.copy(confirmandoRemocao = false) }

    fun remover() {
        val atual = _uiState.value.cliente ?: return
        _uiState.update { it.copy(confirmandoRemocao = false) }
        viewModelScope.launch {
            try {
                repository.deletar(atual)
                _uiState.update { it.copy(removido = true) }
            } catch (e: Exception) {
                Log.e(TAG, "Erro ao remover cliente ${atual.id}", e)
                _uiState.update { it.copy(mensagem = "Erro ao remover cliente") }
            }
        }
    }

    fun mensagemMostrada() = _uiState.update { it.copy(mensagem = null) }

    // ---------- Apoio ----------

    /** Regra de negócio: confere o formato dos campos. Se tiver erro, coloca no estado. */
    private fun formValido(form: ClienteForm): Boolean {
        val erros = validarCliente(form.nome, form.cpf, form.telefone, form.nascimento)
        if (erros.isNotEmpty()) _uiState.update { it.copy(form = form.copy(erros = erros)) }
        return erros.isEmpty()
    }

    private fun erroNoCampo(campo: String, mensagem: String) = _uiState.update {
        it.copy(salvando = false, form = it.form.copy(erros = mapOf(campo to mensagem)))
    }
}
