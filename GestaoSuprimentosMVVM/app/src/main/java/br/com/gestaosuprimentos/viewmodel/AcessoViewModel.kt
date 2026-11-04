package br.com.gestaosuprimentos.viewmodel

import androidx.lifecycle.ViewModel
import br.com.gestaosuprimentos.data.local.Usuario
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update

/** Estado das telas de Login e Criar acesso (login continua em memória, fora do banco). */
data class AcessoUiState(
    val usuarioLogado: String? = null,
    // Tela de login
    val login: String = "",
    val senha: String = "",
    val erroLogin: String? = null,
    // Tela de criar acesso
    val novoLogin: String = "",
    val novaSenha: String = "",
    val confirmacao: String = "",
    val erroCriar: String? = null,
    val acessoCriado: Boolean = false
)

class AcessoViewModel : ViewModel() {

    private val _uiState = MutableStateFlow(AcessoUiState())
    val uiState: StateFlow<AcessoUiState> = _uiState.asStateFlow()

    private val usuarios = mutableListOf(Usuario("admin", "admin"))

    // ---------- Login ----------

    fun alterarLogin(valor: String) = _uiState.update { it.copy(login = valor, erroLogin = null) }

    fun alterarSenha(valor: String) = _uiState.update { it.copy(senha = valor, erroLogin = null) }

    /** Retorna true se entrou (a tela então navega para o Menu). */
    fun entrar(): Boolean {
        val estado = _uiState.value
        val ok = usuarios.any {
            it.login.equals(estado.login.trim(), ignoreCase = true) && it.senha == estado.senha
        }
        _uiState.update {
            if (ok) it.copy(usuarioLogado = estado.login.trim(), senha = "", erroLogin = null)
            else it.copy(erroLogin = "Login ou senha inválidos")
        }
        return ok
    }

    fun sair() = _uiState.update { AcessoUiState() }

    // ---------- Criar acesso ----------

    fun alterarNovoLogin(valor: String) = _uiState.update { it.copy(novoLogin = valor, erroCriar = null) }

    fun alterarNovaSenha(valor: String) = _uiState.update { it.copy(novaSenha = valor, erroCriar = null) }

    fun alterarConfirmacao(valor: String) = _uiState.update { it.copy(confirmacao = valor, erroCriar = null) }

    fun criarAcesso() {
        val estado = _uiState.value
        val l = estado.novoLogin.trim()
        val erro = when {
            l.isBlank() -> "Informe um login"
            estado.novaSenha.length < 4 -> "A senha precisa de pelo menos 4 caracteres"
            estado.novaSenha != estado.confirmacao -> "As senhas não conferem"
            usuarios.any { it.login.equals(l, ignoreCase = true) } -> "Esse login já existe"
            else -> null
        }
        if (erro != null) {
            _uiState.update { it.copy(erroCriar = erro) }
            return
        }
        usuarios.add(Usuario(l, estado.novaSenha))
        _uiState.update {
            it.copy(acessoCriado = true, login = l, novoLogin = "", novaSenha = "", confirmacao = "")
        }
    }

    /** A tela já voltou para o login depois de criar o acesso. */
    fun acessoCriadoTratado() = _uiState.update { it.copy(acessoCriado = false) }
}
