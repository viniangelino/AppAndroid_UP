package br.com.gestaosuprimentos.navigation

/** Todas as rotas do app em um só lugar. */
object Rotas {
    const val LOGIN = "login"
    const val CRIAR_ACESSO = "criar_acesso"
    const val MENU = "menu"

    const val CLIENTES = "clientes"
    const val CLIENTE_CADASTRO = "cliente_cadastro"
    const val ARG_CLIENTE_ID = "clienteId"
    const val CLIENTE_DETALHE = "cliente_detalhe/{$ARG_CLIENTE_ID}"

    const val FORNECEDORES = "fornecedores"
    const val FORNECEDOR_CADASTRO = "fornecedor_cadastro"
    const val ARG_FORNECEDOR_ID = "fornecedorId"
    const val FORNECEDOR_DETALHE = "fornecedor_detalhe/{$ARG_FORNECEDOR_ID}"

    const val AJUDA = "ajuda"

    // Montam a rota de detalhe com o id real do item
    fun clienteDetalhe(id: Int) = "cliente_detalhe/$id"
    fun fornecedorDetalhe(id: Int) = "fornecedor_detalhe/$id"
}
