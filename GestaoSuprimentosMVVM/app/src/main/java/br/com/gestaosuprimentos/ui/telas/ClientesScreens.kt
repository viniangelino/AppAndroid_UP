package br.com.gestaosuprimentos.ui.telas

import android.widget.Toast
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import br.com.gestaosuprimentos.data.local.Cliente
import br.com.gestaosuprimentos.data.repository.ClienteRepository
import br.com.gestaosuprimentos.navigation.Rotas
import br.com.gestaosuprimentos.navigation.irParaArea
import br.com.gestaosuprimentos.ui.componentes.*
import br.com.gestaosuprimentos.ui.theme.CinzaCampo
import br.com.gestaosuprimentos.ui.theme.CinzaTexto
import br.com.gestaosuprimentos.ui.theme.VerdeOk
import br.com.gestaosuprimentos.util.*
import br.com.gestaosuprimentos.viewmodel.ClienteDetalheViewModel
import br.com.gestaosuprimentos.viewmodel.ClienteViewModelFactory
import br.com.gestaosuprimentos.viewmodel.ClientesViewModel

/*
 * Camada View: as telas só leem o uiState e chamam métodos do ViewModel.
 * Nada de DAO, CoroutineScope ou Dispatchers.IO aqui.
 */

// ======================= LISTA =======================

@Composable
fun ClientesListaScreen(
    navController: NavController,
    repository: ClienteRepository,
    vm: ClientesViewModel = viewModel(factory = ClienteViewModelFactory(repository))
) {
    val estado by vm.uiState.collectAsStateWithLifecycle()
    val mascaraCpf = remember { Mascara("###.###.###-##") }

    MostrarMensagem(estado.mensagem, vm::mensagemMostrada)

    Scaffold(
        topBar = { BarraTopo("Clientes", onMenu = { irParaArea(navController, Rotas.MENU) }) },
        floatingActionButton = {
            ExtendedFloatingActionButton(
                onClick = { navController.navigate(Rotas.CLIENTE_CADASTRO) },
                icon = { Icon(Icons.Default.Add, contentDescription = null) },
                text = { Text("Cadastrar") },
                containerColor = MaterialTheme.colorScheme.primary,
                contentColor = MaterialTheme.colorScheme.onPrimary
            )
        }
    ) { padding ->
        Column(
            Modifier
                .padding(padding)
                .fillMaxSize()
                .padding(horizontal = 16.dp)
        ) {
            Spacer(Modifier.height(12.dp))
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                CampoFormulario(
                    rotulo = "CPF",
                    valor = estado.buscaCpf,
                    onValorChange = { vm.alterarBuscaCpf(soDigitos(it, 11)) },
                    modifier = Modifier.weight(1f),
                    teclado = KeyboardType.Number,
                    transformacao = mascaraCpf,
                    dica = "Buscar"
                )
                CampoFormulario(
                    rotulo = "Nome",
                    valor = estado.buscaNome,
                    onValorChange = vm::alterarBuscaNome,
                    modifier = Modifier.weight(1.3f),
                    dica = "Buscar"
                )
            }
            Text(
                "${estado.filtrados.size} de ${estado.clientes.size} clientes · ${estado.ativos} ativos",
                style = MaterialTheme.typography.bodySmall,
                color = CinzaTexto,
                modifier = Modifier.padding(vertical = 8.dp)
            )

            when {
                estado.carregando -> Carregando()
                estado.filtrados.isEmpty() -> EstadoVazio(
                    if (estado.clientes.isEmpty()) "Nenhum cliente cadastrado ainda.\nToque em Cadastrar."
                    else "Nenhum cliente encontrado com esse filtro."
                )
                else -> LazyColumn(
                    verticalArrangement = Arrangement.spacedBy(8.dp),
                    contentPadding = PaddingValues(bottom = 88.dp)
                ) {
                    items(estado.filtrados, key = { it.id }) { cliente ->
                        ClienteCard(
                            cliente = cliente,
                            onClick = { navController.navigate(Rotas.clienteDetalhe(cliente.id)) },
                            onAlternarAtivo = { vm.alternarAtivo(cliente) },
                            onRemover = { vm.removerCliente(cliente) }
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun ClienteCard(
    cliente: Cliente,
    onClick: () -> Unit,
    onAlternarAtivo: () -> Unit,
    onRemover: () -> Unit
) {
    val corTexto = if (cliente.ativo) MaterialTheme.colorScheme.onSurface else CinzaTexto
    Card(
        onClick = onClick,
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(14.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White)
    ) {
        Row(
            modifier = Modifier.padding(vertical = 8.dp, horizontal = 4.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Checkbox marca o cliente como ativo/inativo (salva no banco via ViewModel)
            Checkbox(checked = cliente.ativo, onCheckedChange = { onAlternarAtivo() })
            Avatar(
                iniciais(cliente.nome),
                cor = if (cliente.ativo) MaterialTheme.colorScheme.secondary else CinzaCampo
            )
            Spacer(Modifier.width(12.dp))
            Column(Modifier.weight(1f)) {
                Text(
                    cliente.nome,
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    color = corTexto,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
                Text("CPF ${formatarCpf(cliente.cpf)}", style = MaterialTheme.typography.bodySmall, color = CinzaTexto)
                Spacer(Modifier.height(4.dp))
                if (cliente.ativo) Selo("ATIVO", VerdeOk.copy(alpha = 0.12f), VerdeOk)
                else Selo("INATIVO", CinzaCampo, CinzaTexto)
            }
            IconButton(onClick = onRemover) {
                Icon(Icons.Default.Delete, contentDescription = "Remover", tint = MaterialTheme.colorScheme.error)
            }
        }
    }
}

// ======================= CADASTRO =======================

@Composable
fun ClienteCadastroScreen(
    navController: NavController,
    repository: ClienteRepository,
    vm: ClienteDetalheViewModel = viewModel(factory = ClienteViewModelFactory(repository))
) {
    val estado by vm.uiState.collectAsStateWithLifecycle()

    MostrarMensagem(estado.mensagem, vm::mensagemMostrada)

    // Quando o ViewModel termina de salvar, abre o detalhe do cliente novo no lugar do formulário
    LaunchedEffect(estado.idCriado) {
        estado.idCriado?.let { id ->
            navController.navigate(Rotas.clienteDetalhe(id)) {
                popUpTo(Rotas.CLIENTE_CADASTRO) { inclusive = true }
            }
        }
    }

    Scaffold(topBar = { BarraTopo("Cadastrar cliente", onVoltar = { navController.popBackStack() }) }) { padding ->
        Column(
            modifier = Modifier
                .padding(padding)
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
                .padding(16.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            LogoApp()
            FormularioCliente(estado.form, vm::alterarForm)
            BotaoPrimario(
                texto = if (estado.salvando) "SALVANDO..." else "CADASTRAR CLIENTE",
                icone = Icons.Default.Check,
                onClick = vm::cadastrar
            )
        }
    }
}

// ======================= DETALHE =======================

@Composable
fun ClienteDetalheScreen(
    navController: NavController,
    repository: ClienteRepository,
    clienteId: Int,
    vm: ClienteDetalheViewModel = viewModel(factory = ClienteViewModelFactory(repository))
) {
    val estado by vm.uiState.collectAsStateWithLifecycle()
    val context = LocalContext.current

    // Pede ao ViewModel para buscar no banco o cliente do id que veio na rota
    LaunchedEffect(clienteId) { vm.carregar(clienteId) }

    MostrarMensagem(estado.mensagem, vm::mensagemMostrada)

    LaunchedEffect(estado.removido) {
        if (estado.removido) {
            Toast.makeText(context, "Cliente removido", Toast.LENGTH_SHORT).show()
            navController.popBackStack()
        }
    }

    // Cópia local para o Kotlin tratar como "não nulo" depois do if
    val cliente = estado.cliente

    Scaffold(
        topBar = {
            BarraTopo(
                titulo = if (estado.editando) "Editar cliente" else "Detalhe do cliente",
                onVoltar = { if (estado.editando) vm.cancelarEdicao() else navController.popBackStack() },
                acoes = {
                    if (cliente != null && !estado.editando) {
                        IconButton(onClick = vm::iniciarEdicao) {
                            Icon(Icons.Default.Edit, contentDescription = "Editar")
                        }
                    }
                }
            )
        }
    ) { padding ->
        if (estado.carregando) {
            Carregando(Modifier.padding(padding))
            return@Scaffold
        }
        if (cliente == null) {
            EstadoVazio("Cliente não encontrado.", Modifier.padding(padding))
            return@Scaffold
        }
        Column(
            modifier = Modifier
                .padding(padding)
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            if (estado.editando) {
                // ---- Edição no próprio detalhe ----
                FormularioCliente(estado.form, vm::alterarForm)
                Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                    OutlinedButton(
                        onClick = vm::cancelarEdicao,
                        modifier = Modifier.weight(1f).heightIn(min = 48.dp),
                        shape = RoundedCornerShape(10.dp)
                    ) { Text("CANCELAR") }
                    BotaoPrimario(
                        texto = "SALVAR",
                        icone = Icons.Default.Check,
                        modifier = Modifier.weight(1f),
                        onClick = vm::salvarEdicao
                    )
                }
            } else {
                // ---- Visualização ----
                CabecalhoDetalhe(
                    iniciais = iniciais(cliente.nome),
                    titulo = cliente.nome,
                    subtitulo = "Cliente nº ${cliente.id}"
                ) {
                    if (cliente.ativo) Selo("ATIVO", Color.White, VerdeOk)
                    else Selo("INATIVO", CinzaCampo, CinzaTexto)
                }

                SecaoCard("Dados cadastrais") {
                    LinhaInfo("CPF", formatarCpf(cliente.cpf))
                    LinhaInfo("Telefone", formatarTelefone(cliente.telefone))
                    LinhaInfo("Nascimento", formatarNascimento(cliente.nascimento))
                    LinhaInfo("Cadastrado em", formatarData(cliente.dataCadastro))
                }

                // Informações calculadas a partir dos dados do item
                SecaoCard("Informações calculadas") {
                    val valido = cpfValido(cliente.cpf)
                    LinhaInfo(
                        "Situação do CPF",
                        if (valido) "Válido ✓" else "Inválido ✗",
                        if (valido) VerdeOk else MaterialTheme.colorScheme.error
                    )
                    val nascimento = lerNascimento(cliente.nascimento)
                    if (nascimento != null) {
                        LinhaInfo("Idade", "${idade(nascimento)} anos")
                        val faltam = diasParaAniversario(nascimento)
                        LinhaInfo("Próximo aniversário", if (faltam == 0L) "É hoje! 🎉" else "em $faltam dias")
                    } else {
                        LinhaInfo("Idade", "Informe o nascimento")
                    }
                    val dias = diasDesde(cliente.dataCadastro)
                    LinhaInfo("Cliente há", if (dias == 0L) "Cadastrado hoje" else "$dias dias")
                }

                BotaoPrimario(
                    texto = if (cliente.ativo) "DESATIVAR CLIENTE" else "ATIVAR CLIENTE",
                    onClick = vm::alternarAtivo,
                    modifier = Modifier.fillMaxWidth()
                )
                OutlinedButton(
                    onClick = vm::pedirRemocao,
                    modifier = Modifier.fillMaxWidth().heightIn(min = 48.dp),
                    shape = RoundedCornerShape(10.dp),
                    colors = ButtonDefaults.outlinedButtonColors(contentColor = MaterialTheme.colorScheme.error)
                ) {
                    Icon(Icons.Default.Delete, contentDescription = null)
                    Spacer(Modifier.width(8.dp))
                    Text("REMOVER CLIENTE", fontWeight = FontWeight.Bold)
                }
            }
        }
    }

    if (estado.confirmandoRemocao && cliente != null) {
        AlertDialog(
            onDismissRequest = vm::cancelarRemocao,
            title = { Text("Remover cliente?") },
            text = { Text("${cliente.nome} será apagado do banco de dados.") },
            confirmButton = {
                TextButton(onClick = vm::remover) { Text("REMOVER", color = MaterialTheme.colorScheme.error) }
            },
            dismissButton = {
                TextButton(onClick = vm::cancelarRemocao) { Text("CANCELAR") }
            }
        )
    }
}
