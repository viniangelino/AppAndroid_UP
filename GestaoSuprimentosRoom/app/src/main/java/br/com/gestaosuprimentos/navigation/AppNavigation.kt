package br.com.gestaosuprimentos.navigation

import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.ShoppingCart
import androidx.compose.material3.Icon
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import br.com.gestaosuprimentos.data.GestaoViewModel
import br.com.gestaosuprimentos.ui.telas.AjudaScreen
import br.com.gestaosuprimentos.ui.telas.ClienteCadastroScreen
import br.com.gestaosuprimentos.ui.telas.ClienteDetalheScreen
import br.com.gestaosuprimentos.ui.telas.ClientesListaScreen
import br.com.gestaosuprimentos.ui.telas.CriarAcessoScreen
import br.com.gestaosuprimentos.ui.telas.FornecedorCadastroScreen
import br.com.gestaosuprimentos.ui.telas.FornecedorDetalheScreen
import br.com.gestaosuprimentos.ui.telas.FornecedoresListaScreen
import br.com.gestaosuprimentos.ui.telas.LoginScreen
import br.com.gestaosuprimentos.ui.telas.MenuScreen

@Composable
fun AppNavigation() {
    val navController = rememberNavController()
    val vm: GestaoViewModel = viewModel()

    // Rota da tela aberta agora — usada para esconder a barra no login e acender a aba certa
    val backStackEntry by navController.currentBackStackEntryAsState()
    val rotaAtual = backStackEntry?.destination?.route
    val mostrarBarra = rotaAtual != null && rotaAtual != Rotas.LOGIN && rotaAtual != Rotas.CRIAR_ACESSO

    Scaffold(
        bottomBar = { if (mostrarBarra) BarraInferior(navController, rotaAtual) }
    ) { innerPadding ->
        NavHost(
            navController = navController,
            startDestination = Rotas.LOGIN,
            modifier = Modifier.padding(innerPadding)
        ) {
            composable(Rotas.LOGIN) { LoginScreen(navController, vm) }
            composable(Rotas.CRIAR_ACESSO) { CriarAcessoScreen(navController, vm) }
            composable(Rotas.MENU) { MenuScreen(navController, vm) }

            // ---------- Clientes ----------
            composable(Rotas.CLIENTES) { ClientesListaScreen(navController) }
            composable(Rotas.CLIENTE_CADASTRO) { ClienteCadastroScreen(navController) }
            composable(Rotas.CLIENTE_DETALHE) { backStackEntry ->
                // Recebe o id que veio na rota "cliente_detalhe/{clienteId}"
                val id = backStackEntry.arguments?.getString(Rotas.ARG_CLIENTE_ID)
                ClienteDetalheScreen(navController, id?.toIntOrNull() ?: -1)
            }

            // ---------- Fornecedores ----------
            composable(Rotas.FORNECEDORES) { FornecedoresListaScreen(navController) }
            composable(Rotas.FORNECEDOR_CADASTRO) { FornecedorCadastroScreen(navController) }
            composable(Rotas.FORNECEDOR_DETALHE) { backStackEntry ->
                val id = backStackEntry.arguments?.getString(Rotas.ARG_FORNECEDOR_ID)
                FornecedorDetalheScreen(navController, id?.toIntOrNull() ?: -1)
            }

            composable(Rotas.AJUDA) { AjudaScreen(navController) }
        }
    }
}

/**
 * Troca de área (aba) sem empilhar telas repetidas:
 * volta até o Menu, guarda o estado da aba anterior e restaura o da nova.
 */
fun irParaArea(navController: NavController, rota: String) {
    navController.navigate(rota) {
        popUpTo(Rotas.MENU) { saveState = true }
        launchSingleTop = true
        restoreState = true
    }
}

private data class ItemBarra(val rota: String, val rotulo: String, val icone: ImageVector)

private val ITENS_BARRA = listOf(
    ItemBarra(Rotas.MENU, "Menu", Icons.Default.Home),
    ItemBarra(Rotas.CLIENTES, "Clientes", Icons.Default.Person),
    ItemBarra(Rotas.FORNECEDORES, "Fornecedores", Icons.Default.ShoppingCart),
    ItemBarra(Rotas.AJUDA, "Ajuda", Icons.Default.Info)
)

/** Descobre a aba de qualquer rota, para manter a aba acesa nas telas de cadastro/detalhe. */
private fun areaDaRota(rota: String?): String? = when {
    rota == null -> null
    rota.startsWith("cliente") -> Rotas.CLIENTES
    rota.startsWith("fornecedor") -> Rotas.FORNECEDORES
    rota == Rotas.AJUDA -> Rotas.AJUDA
    else -> Rotas.MENU
}

@Composable
private fun BarraInferior(navController: NavController, rotaAtual: String?) {
    val area = areaDaRota(rotaAtual)
    NavigationBar(containerColor = Color.White) {
        ITENS_BARRA.forEach { item ->
            NavigationBarItem(
                selected = area == item.rota,
                onClick = { irParaArea(navController, item.rota) },
                icon = { Icon(item.icone, contentDescription = item.rotulo) },
                label = { Text(item.rotulo) }
            )
        }
    }
}
