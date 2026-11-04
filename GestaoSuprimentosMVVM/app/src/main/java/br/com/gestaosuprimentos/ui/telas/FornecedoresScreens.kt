@file:OptIn(ExperimentalFoundationApi::class)

package br.com.gestaosuprimentos.ui.telas

import android.widget.Toast
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.KeyboardArrowRight
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.ShoppingCart
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import br.com.gestaosuprimentos.data.local.Fornecedor
import br.com.gestaosuprimentos.data.repository.FornecedorRepository
import br.com.gestaosuprimentos.navigation.Rotas
import br.com.gestaosuprimentos.navigation.irParaArea
import br.com.gestaosuprimentos.ui.componentes.*
import br.com.gestaosuprimentos.ui.theme.CinzaCampo
import br.com.gestaosuprimentos.ui.theme.CinzaTexto
import br.com.gestaosuprimentos.ui.theme.VerdeOk
import br.com.gestaosuprimentos.util.*
import br.com.gestaosuprimentos.viewmodel.FornecedorDetalheViewModel
import br.com.gestaosuprimentos.viewmodel.FornecedorViewModelFactory
import br.com.gestaosuprimentos.viewmodel.FornecedoresViewModel

// ======================= LISTA =======================

@Composable
fun FornecedoresListaScreen(
    navController: NavController,
    repository: FornecedorRepository,
    vm: FornecedoresViewModel = viewModel(factory = FornecedorViewModelFactory(repository))
) {
    val estado by vm.uiState.collectAsStateWithLifecycle()
    val mascaraCnpj = remember { Mascara("##.###.###/####-##") }

    MostrarMensagem(estado.mensagem, vm::mensagemMostrada)

    Scaffold(
        topBar = { BarraTopo("Fornecedores", onMenu = { irParaArea(navController, Rotas.MENU) }) },
        floatingActionButton = {
            ExtendedFloatingActionButton(
                onClick = { navController.navigate(Rotas.FORNECEDOR_CADASTRO) },
                icon = { Icon(Icons.Default.Add, contentDescription = null) },
                text = { Text("Novo fornecedor") },
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
            CampoFormulario(
                rotulo = "CNPJ",
                valor = estado.buscaCnpj,
                onValorChange = { vm.alterarBuscaCnpj(soDigitos(it, 14)) },
                teclado = KeyboardType.Number,
                transformacao = mascaraCnpj,
                dica = "Buscar por CNPJ"
            )
            Spacer(Modifier.height(8.dp))
            CampoFormulario(
                rotulo = "Razão social",
                valor = estado.buscaRazao,
                onValorChange = vm::alterarBuscaRazao,
                dica = "Buscar por nome"
            )
            Text(
                "${estado.filtrados.size} de ${estado.fornecedores.size} fornecedores · toque para ver, segure para remover",
                style = MaterialTheme.typography.bodySmall,
                color = CinzaTexto,
                modifier = Modifier.padding(vertical = 8.dp)
            )

            when {
                estado.carregando -> Carregando()
                estado.filtrados.isEmpty() -> EstadoVazio(
                    if (estado.fornecedores.isEmpty()) "Nenhum fornecedor cadastrado ainda."
                    else "Nenhum fornecedor encontrado com esse filtro."
                )
                else -> LazyColumn(
                    verticalArrangement = Arrangement.spacedBy(8.dp),
                    contentPadding = PaddingValues(bottom = 88.dp)
                ) {
                    items(estado.filtrados, key = { it.id }) { fornecedor ->
                        FornecedorCard(
                            fornecedor = fornecedor,
                            onClick = { navController.navigate(Rotas.fornecedorDetalhe(fornecedor.id)) },
                            onLongClick = { vm.pedirRemocao(fornecedor) }
                        )
                    }
                }
            }
        }
    }

    // Confirmação da remoção feita com clique longo
    estado.paraRemover?.let { f ->
        AlertDialog(
            onDismissRequest = vm::cancelarRemocao,
            title = { Text("Remover fornecedor?") },
            text = { Text("${f.razaoSocial} (${formatarCnpj(f.cnpj)}) será apagado do banco de dados.") },
            confirmButton = {
                TextButton(onClick = vm::confirmarRemocao) {
                    Text("REMOVER", color = MaterialTheme.colorScheme.error)
                }
            },
            dismissButton = { TextButton(onClick = vm::cancelarRemocao) { Text("CANCELAR") } }
        )
    }
}

@Composable
private fun FornecedorCard(fornecedor: Fornecedor, onClick: () -> Unit, onLongClick: () -> Unit) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(14.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .combinedClickable(onClick = onClick, onLongClick = onLongClick)
                .padding(12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier
                    .size(44.dp)
                    .clip(RoundedCornerShape(12.dp))
                    .background(MaterialTheme.colorScheme.primary),
                contentAlignment = Alignment.Center
            ) {
                Icon(Icons.Default.ShoppingCart, contentDescription = null, tint = MaterialTheme.colorScheme.secondary)
            }
            Spacer(Modifier.width(12.dp))
            Column(Modifier.weight(1f)) {
                Text(
                    fornecedor.razaoSocial,
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
                Text(formatarCnpj(fornecedor.cnpj), style = MaterialTheme.typography.bodySmall, color = CinzaTexto)
                Spacer(Modifier.height(4.dp))
                Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                    Selo(
                        fornecedor.categoria.uppercase(),
                        MaterialTheme.colorScheme.secondaryContainer,
                        MaterialTheme.colorScheme.onSecondaryContainer
                    )
                    Selo(
                        if (ehMatriz(fornecedor.cnpj)) "MATRIZ" else "FILIAL",
                        MaterialTheme.colorScheme.primaryContainer,
                        MaterialTheme.colorScheme.onPrimaryContainer
                    )
                }
            }
            Column(horizontalAlignment = Alignment.End) {
                Text("${fornecedor.prazoEntregaDias}", style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.ExtraBold)
                Text("dias úteis", style = MaterialTheme.typography.labelSmall, color = CinzaTexto)
            }
        }
    }
}

// ======================= CADASTRO =======================

@Composable
fun FornecedorCadastroScreen(
    navController: NavController,
    repository: FornecedorRepository,
    vm: FornecedorDetalheViewModel = viewModel(factory = FornecedorViewModelFactory(repository))
) {
    val estado by vm.uiState.collectAsStateWithLifecycle()

    MostrarMensagem(estado.mensagem, vm::mensagemMostrada)

    LaunchedEffect(estado.idCriado) {
        estado.idCriado?.let { id ->
            navController.navigate(Rotas.fornecedorDetalhe(id)) {
                popUpTo(Rotas.FORNECEDOR_CADASTRO) { inclusive = true }
            }
        }
    }

    Scaffold(topBar = { BarraTopo("Novo fornecedor", onVoltar = { navController.popBackStack() }) }) { padding ->
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
            FormularioFornecedor(estado.form, vm::alterarForm)
            BotaoPrimario(
                texto = if (estado.salvando) "SALVANDO..." else "CADASTRAR FORNECEDOR",
                icone = Icons.Default.Check,
                onClick = vm::cadastrar
            )
        }
    }
}

// ======================= DETALHE =======================

@Composable
fun FornecedorDetalheScreen(
    navController: NavController,
    repository: FornecedorRepository,
    fornecedorId: Int,
    vm: FornecedorDetalheViewModel = viewModel(factory = FornecedorViewModelFactory(repository))
) {
    val estado by vm.uiState.collectAsStateWithLifecycle()
    val context = LocalContext.current

    LaunchedEffect(fornecedorId) { vm.carregar(fornecedorId) }

    MostrarMensagem(estado.mensagem, vm::mensagemMostrada)

    LaunchedEffect(estado.removido) {
        if (estado.removido) {
            Toast.makeText(context, "Fornecedor removido", Toast.LENGTH_SHORT).show()
            navController.popBackStack()
        }
    }

    val fornecedor = estado.fornecedor
    val mesmaEmpresa = estado.mesmaEmpresa

    Scaffold(
        topBar = {
            BarraTopo(
                titulo = if (estado.editando) "Editar fornecedor" else "Detalhe do fornecedor",
                onVoltar = { if (estado.editando) vm.cancelarEdicao() else navController.popBackStack() },
                acoes = {
                    if (fornecedor != null && !estado.editando) {
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
        if (fornecedor == null) {
            EstadoVazio("Fornecedor não encontrado.", Modifier.padding(padding))
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
                FormularioFornecedor(estado.form, vm::alterarForm)
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
                CabecalhoDetalhe(
                    iniciais = iniciais(fornecedor.razaoSocial),
                    titulo = fornecedor.razaoSocial,
                    subtitulo = fornecedor.categoria
                ) {
                    Selo(
                        if (ehMatriz(fornecedor.cnpj)) "MATRIZ" else "FILIAL",
                        Color.White,
                        MaterialTheme.colorScheme.primary
                    )
                }

                SecaoCard("Dados cadastrais") {
                    LinhaInfo("CNPJ", formatarCnpj(fornecedor.cnpj))
                    LinhaInfo("Categoria", fornecedor.categoria)
                    LinhaInfo("Prazo de entrega", "${fornecedor.prazoEntregaDias} dias úteis")
                    LinhaInfo("Cadastrado em", formatarData(fornecedor.dataCadastro))
                    if (fornecedor.observacoes.isNotBlank()) {
                        Spacer(Modifier.height(8.dp))
                        Text("Observações", style = MaterialTheme.typography.bodyMedium, color = CinzaTexto)
                        Text(fornecedor.observacoes, style = MaterialTheme.typography.bodyMedium)
                    }
                }

                SecaoCard("Informações calculadas") {
                    val valido = cnpjValido(fornecedor.cnpj)
                    LinhaInfo(
                        "Situação do CNPJ",
                        if (valido) "Válido ✓" else "Inválido ✗",
                        if (valido) VerdeOk else MaterialTheme.colorScheme.error
                    )
                    val n = numeroEstabelecimento(fornecedor.cnpj)
                    LinhaInfo("Estabelecimento", if (n == 1) "Matriz" else "Filial nº $n")
                    LinhaInfo("Se pedir hoje, chega em", formatarDataComDia(previsaoEntrega(fornecedor.prazoEntregaDias)))
                    LinhaInfo("Fornecedor há", "${diasDesde(fornecedor.dataCadastro)} dias")
                }

                // Vem do UiState (o ViewModel buscou com buscarPorRaizCnpj) + navegação secundária
                SecaoCard("Mesma empresa · raiz ${formatarRaizCnpj(fornecedor.cnpj)}") {
                    if (mesmaEmpresa.isEmpty()) {
                        Text(
                            "Nenhum outro estabelecimento desta empresa cadastrado.",
                            style = MaterialTheme.typography.bodyMedium,
                            color = CinzaTexto
                        )
                    } else {
                        mesmaEmpresa.forEach { outro ->
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .clip(RoundedCornerShape(10.dp))
                                    .clickable { navController.navigate(Rotas.fornecedorDetalhe(outro.id)) }
                                    .padding(vertical = 8.dp, horizontal = 4.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Avatar(iniciais(outro.razaoSocial), tamanho = 36.dp, cor = CinzaCampo)
                                Spacer(Modifier.width(12.dp))
                                Column(Modifier.weight(1f)) {
                                    Text(outro.razaoSocial, fontWeight = FontWeight.Bold, maxLines = 1, overflow = TextOverflow.Ellipsis)
                                    Text(
                                        "${formatarCnpj(outro.cnpj)} · ${if (ehMatriz(outro.cnpj)) "Matriz" else "Filial"} · ${outro.prazoEntregaDias} dias",
                                        style = MaterialTheme.typography.bodySmall,
                                        color = CinzaTexto
                                    )
                                }
                                Icon(Icons.AutoMirrored.Filled.KeyboardArrowRight, contentDescription = "Abrir")
                            }
                        }
                        val maisRapido = (mesmaEmpresa + fornecedor).minBy { it.prazoEntregaDias }
                        Spacer(Modifier.height(4.dp))
                        Text(
                            "Entrega mais rápida desta empresa: ${maisRapido.razaoSocial} (${maisRapido.prazoEntregaDias} dias úteis)",
                            style = MaterialTheme.typography.bodySmall,
                            fontWeight = FontWeight.SemiBold,
                            color = MaterialTheme.colorScheme.primary
                        )
                    }
                }

                OutlinedButton(
                    onClick = vm::pedirRemocao,
                    modifier = Modifier.fillMaxWidth().heightIn(min = 48.dp),
                    shape = RoundedCornerShape(10.dp),
                    colors = ButtonDefaults.outlinedButtonColors(contentColor = MaterialTheme.colorScheme.error)
                ) {
                    Icon(Icons.Default.Delete, contentDescription = null)
                    Spacer(Modifier.width(8.dp))
                    Text("REMOVER FORNECEDOR", fontWeight = FontWeight.Bold)
                }
            }
        }
    }

    if (estado.confirmandoRemocao && fornecedor != null) {
        AlertDialog(
            onDismissRequest = vm::cancelarRemocao,
            title = { Text("Remover fornecedor?") },
            text = { Text("${fornecedor.razaoSocial} será apagado do banco de dados.") },
            confirmButton = {
                TextButton(onClick = vm::remover) { Text("REMOVER", color = MaterialTheme.colorScheme.error) }
            },
            dismissButton = { TextButton(onClick = vm::cancelarRemocao) { Text("CANCELAR") } }
        )
    }
}
