package br.com.gestaosuprimentos.data

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import br.com.gestaosuprimentos.util.cnpjValido
import br.com.gestaosuprimentos.util.cpfValido
import br.com.gestaosuprimentos.util.lerNascimento
import br.com.gestaosuprimentos.util.raizCnpj
import java.time.LocalDate

/**
 * Único ViewModel do app. Guarda as listas reativas (mutableStateListOf) em memória
 * e é compartilhado por todas as telas a partir do AppNavigation.
 */
class GestaoViewModel : ViewModel() {

    val clientes = mutableStateListOf<Cliente>()
    val fornecedores = mutableStateListOf<Fornecedor>()
    private val usuarios = mutableStateListOf(Usuario("admin", "admin"))

    var usuarioLogado by mutableStateOf<String?>(null)
        private set

    private var proximoIdCliente = 1
    private var proximoIdFornecedor = 1

    init {
        val hoje = LocalDate.now()
        adicionarCliente("Maria Souza", "52998224725", "41998765432", "15031990", hoje.minusDays(120))
        adicionarCliente("João Pereira", "11144477735", "4133334444", "02111985", hoje.minusDays(45))
        val ana = adicionarCliente("Ana Lima", "12345678909", "", "", hoje.minusDays(3))
        alternarAtivo(ana)

        adicionarFornecedor("Embalagens Paraná Ltda", "11222333000181", "Embalagens", 5,
            "Caixas de papelão e fitas adesivas.", hoje.minusDays(200))
        adicionarFornecedor("Madeireira Araucária Ltda", "11444777000161", "Matéria-prima", 10,
            "Pinus tratado. Pedido mínimo de 2 m³.", hoje.minusDays(90))
        adicionarFornecedor("Madeireira Araucária - Filial Curitiba", "11444777000242", "Matéria-prima", 3,
            "Retirada no balcão disponível.", hoje.minusDays(15))
    }

    // ---------- Acesso ----------

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

    // ---------- Clientes ----------

    fun buscarCliente(id: Int) = clientes.find { it.id == id }

    fun adicionarCliente(
        nome: String, cpf: String, telefone: String, nascimento: String,
        dataCadastro: LocalDate = LocalDate.now()
    ): Int {
        val novo = Cliente(proximoIdCliente++, nome.trim(), cpf, telefone, nascimento, true, dataCadastro)
        clientes.add(novo)
        return novo.id
    }

    fun atualizarCliente(cliente: Cliente) {
        val i = clientes.indexOfFirst { it.id == cliente.id }
        if (i >= 0) clientes[i] = cliente
    }

    fun alternarAtivo(id: Int) {
        buscarCliente(id)?.let { atualizarCliente(it.copy(ativo = !it.ativo)) }
    }

    fun removerCliente(id: Int) {
        clientes.removeAll { it.id == id }
    }

    /** Valida o formulário de cliente. Mapa vazio = tudo certo. */
    fun validarCliente(
        nome: String, cpf: String, telefone: String, nascimento: String, idIgnorado: Int? = null
    ): Map<String, String> {
        val erros = mutableMapOf<String, String>()
        if (nome.isBlank()) erros["nome"] = "Informe o nome"
        when {
            cpf.length != 11 -> erros["cpf"] = "O CPF precisa ter 11 dígitos"
            !cpfValido(cpf) -> erros["cpf"] = "CPF inválido (dígito verificador não confere)"
            clientes.any { it.cpf == cpf && it.id != idIgnorado } -> erros["cpf"] = "CPF já cadastrado"
        }
        if (telefone.isNotEmpty() && telefone.length < 10) erros["telefone"] = "Telefone incompleto"
        if (nascimento.isNotEmpty()) {
            val data = lerNascimento(nascimento)
            when {
                data == null -> erros["nascimento"] = "Data inválida"
                data.isAfter(LocalDate.now()) -> erros["nascimento"] = "A data não pode ser no futuro"
            }
        }
        return erros
    }

    // ---------- Fornecedores ----------

    fun buscarFornecedor(id: Int) = fornecedores.find { it.id == id }

    fun adicionarFornecedor(
        razaoSocial: String, cnpj: String, categoria: String, prazoEntregaDias: Int, observacoes: String,
        dataCadastro: LocalDate = LocalDate.now()
    ): Int {
        val novo = Fornecedor(
            proximoIdFornecedor++, razaoSocial.trim(), cnpj, categoria, prazoEntregaDias,
            observacoes.trim(), dataCadastro
        )
        fornecedores.add(novo)
        return novo.id
    }

    fun atualizarFornecedor(fornecedor: Fornecedor) {
        val i = fornecedores.indexOfFirst { it.id == fornecedor.id }
        if (i >= 0) fornecedores[i] = fornecedor
    }

    fun removerFornecedor(id: Int) {
        fornecedores.removeAll { it.id == id }
    }

    /** Outros fornecedores com a mesma raiz de CNPJ (mesma empresa: matriz + filiais). */
    fun estabelecimentosDaMesmaEmpresa(fornecedor: Fornecedor) =
        fornecedores.filter { it.id != fornecedor.id && raizCnpj(it.cnpj) == raizCnpj(fornecedor.cnpj) }

    fun validarFornecedor(
        razaoSocial: String, cnpj: String, prazo: String, idIgnorado: Int? = null
    ): Map<String, String> {
        val erros = mutableMapOf<String, String>()
        if (razaoSocial.isBlank()) erros["razaoSocial"] = "Informe a razão social"
        when {
            cnpj.length != 14 -> erros["cnpj"] = "O CNPJ precisa ter 14 dígitos"
            !cnpjValido(cnpj) -> erros["cnpj"] = "CNPJ inválido (dígito verificador não confere)"
            fornecedores.any { it.cnpj == cnpj && it.id != idIgnorado } -> erros["cnpj"] = "CNPJ já cadastrado"
        }
        val dias = prazo.toIntOrNull()
        when {
            dias == null -> erros["prazo"] = "Informe o prazo em dias"
            dias > 365 -> erros["prazo"] = "Prazo máximo de 365 dias"
        }
        return erros
    }
}
