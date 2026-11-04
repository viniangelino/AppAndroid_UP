package br.com.gestaosuprimentos.ui.telas

import androidx.navigation.NavController
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ExitToApp
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.ShoppingCart
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import br.com.gestaosuprimentos.data.repository.ClienteRepository
import br.com.gestaosuprimentos.data.repository.FornecedorRepository
import br.com.gestaosuprimentos.navigation.Rotas
import br.com.gestaosuprimentos.navigation.irParaArea
import br.com.gestaosuprimentos.ui.componentes.BarraTopo
import br.com.gestaosuprimentos.ui.componentes.LogoApp
import br.com.gestaosuprimentos.viewmodel.AcessoViewModel
import br.com.gestaosuprimentos.viewmodel.MenuViewModel
import br.com.gestaosuprimentos.viewmodel.MenuViewModelFactory

private data class OpcaoMenu(val rotulo: String, val rota: String)

// Rotas que são abas da barra inferior (as outras são telas comuns)
private val AREAS = listOf(Rotas.CLIENTES, Rotas.FORNECEDORES, Rotas.AJUDA)

// Só entram no menu as opções que realmente funcionam nesta versão
private val OPCOES = listOf(
    OpcaoMenu("FORNECEDORES", Rotas.FORNECEDORES),
    OpcaoMenu("CADASTRO FORNECEDOR", Rotas.FORNECEDOR_CADASTRO),
    OpcaoMenu("CLIENTES", Rotas.CLIENTES),
    OpcaoMenu("CADASTRO CLIENTE", Rotas.CLIENTE_CADASTRO),
    OpcaoMenu("AJUDA", Rotas.AJUDA)
)

@Composable
fun MenuScreen(
    navController: NavController,
    acessoVm: AcessoViewModel,
    clienteRepository: ClienteRepository,
    fornecedorRepository: FornecedorRepository,
    vm: MenuViewModel = viewModel(factory = MenuViewModelFactory(clienteRepository, fornecedorRepository))
) {
    // Resumo vindo do banco (o MenuViewModel junta os Flows das duas tabelas)
    val resumo by vm.uiState.collectAsStateWithLifecycle()
    val acesso by acessoVm.uiState.collectAsStateWithLifecycle()

    fun abrir(rota: String) {
        if (rota in AREAS) irParaArea(navController, rota) else navController.navigate(rota)
    }

    Scaffold(
        topBar = {
            BarraTopo("Menu", acoes = {
                IconButton(onClick = {
                    acessoVm.sair()
                    // Limpa toda a pilha e volta para o login
                    navController.navigate(Rotas.LOGIN) { popUpTo(0) { inclusive = true } }
                }) {
                    Icon(Icons.AutoMirrored.Filled.ExitToApp, contentDescription = "Sair")
                }
            })
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .padding(padding)
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
                .padding(16.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            LogoApp(80.dp)
            Text(
                "Olá, ${acesso.usuarioLogado ?: ""}!",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold
            )

            // Resumo: números vindos do banco de dados
            Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                CardResumo(
                    titulo = "Clientes",
                    valor = resumo.totalClientes.toString(),
                    detalhe = "${resumo.clientesAtivos} ativos",
                    icone = Icons.Default.Person,
                    onClick = { abrir(Rotas.CLIENTES) },
                    modifier = Modifier.weight(1f)
                )
                CardResumo(
                    titulo = "Fornecedores",
                    valor = resumo.totalFornecedores.toString(),
                    detalhe = "${resumo.matrizes} matrizes",
                    icone = Icons.Default.ShoppingCart,
                    onClick = { abrir(Rotas.FORNECEDORES) },
                    modifier = Modifier.weight(1f)
                )
            }

            Text(
                "MENU",
                style = MaterialTheme.typography.headlineSmall,
                fontWeight = FontWeight.ExtraBold,
                modifier = Modifier.fillMaxWidth().padding(top = 8.dp)
            )
            OPCOES.forEach { opcao ->
                Button(
                    onClick = { abrir(opcao.rota) },
                    modifier = Modifier.fillMaxWidth().height(44.dp),
                    shape = RoundedCornerShape(50)
                ) {
                    Text(opcao.rotulo, fontWeight = FontWeight.Bold)
                }
            }
        }
    }
}

@Composable
private fun CardResumo(
    titulo: String,
    valor: String,
    detalhe: String,
    icone: ImageVector,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Card(
        onClick = onClick,
        modifier = modifier,
        shape = RoundedCornerShape(14.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.secondaryContainer,
            contentColor = MaterialTheme.colorScheme.onSecondaryContainer
        )
    ) {
        Column(Modifier.padding(16.dp)) {
            Icon(icone, contentDescription = null)
            Spacer(Modifier.height(8.dp))
            Text(valor, style = MaterialTheme.typography.headlineMedium, fontWeight = FontWeight.ExtraBold)
            Text(titulo, fontWeight = FontWeight.Bold)
            Text(detalhe, style = MaterialTheme.typography.bodySmall)
        }
    }
}
