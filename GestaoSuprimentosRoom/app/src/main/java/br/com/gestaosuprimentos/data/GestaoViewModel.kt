package br.com.gestaosuprimentos.data

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel

/**
 * No Trabalho 3 as listas de clientes e fornecedores foram para o banco (Room).
 * Aqui ficou só o login, que o enunciado diz para continuar em memória.
 */
class GestaoViewModel : ViewModel() {

    private val usuarios = mutableStateListOf(Usuario("admin", "admin"))

    var usuarioLogado by mutableStateOf<String?>(null)
        private set

    fun entrar(login: String, senha: String): Boolean {
        val ok = usuarios.any { it.login.equals(login.trim(), ignoreCase = true) && it.senha == senha }
        if (ok) usuarioLogado = login.trim()
        return ok
    }

    fun sair() {
        usuarioLogado = null
    }

    /** Retorna mensagem de erro ou null se deu certo. */
    fun criarAcesso(login: String, senha: String): String? {
        val l = login.trim()
        if (l.isBlank()) return "Informe um login"
        if (usuarios.any { it.login.equals(l, ignoreCase = true) }) return "Esse login já existe"
        usuarios.add(Usuario(l, senha))
        return null
    }
}
