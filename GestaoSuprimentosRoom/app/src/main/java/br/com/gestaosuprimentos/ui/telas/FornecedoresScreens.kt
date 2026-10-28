@file:OptIn(ExperimentalFoundationApi::class)

package br.com.gestaosuprimentos.ui.telas

import android.util.Log
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
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import br.com.gestaosuprimentos.data.AppDatabase
import br.com.gestaosuprimentos.data.Fornecedor
import br.com.gestaosuprimentos.navigation.Rotas
import br.com.gestaosuprimentos.navigation.irParaArea
import br.com.gestaosuprimentos.ui.componentes.*
import br.com.gestaosuprimentos.ui.theme.CinzaCampo
import br.com.gestaosuprimentos.ui.theme.CinzaTexto
import br.com.gestaosuprimentos.ui.theme.VerdeOk
import br.com.gestaosuprimentos.util.*
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

private const val TAG = "FornecedoresScreens"

// ======================= LISTA =======================

@Composable
fun FornecedoresListaScreen(navController: NavController) {
    val context = LocalContext.current
    val dao = AppDatabase.getDatabase(context).fornecedorDao()

    val fornecedores = remember { mutableStateListOf<Fornecedor>() }
    var carregando by remember { mutableStateOf(true) }

    // Filtros (os campos CNPJ/RAZÃO SOCIAL do protótipo "Fornecedor")
    var buscaCnpj by rememberSaveable { mutableStateOf("") }
    var buscaRazao by rememberSaveable { mutableStateOf("") }
    var paraRemover by remember { mutableStateOf<Fornecedor?>(null) }
    val mascaraCnpj = remember { Mascara("##.###.###/####-##") }

    // Busca os fornecedores no banco sempre que a tela abre
    LaunchedEffect(Unit) {
        CoroutineScope(Dispatchers.IO).launch {
            try {
                val doBanco = dao.buscarTodos()
                withContext(Dispatchers.Main) {
                    fornecedores.clear()
                    fornecedores.addAll(doBanco)
                    carregando = false
                }
            } catch (e: Exception) {
                Log.e(TAG, "Erro ao buscar fornecedores", e)
                withContext(Dispatchers.Main) { carregando = false }
                avisar(context, "Erro ao carregar fornecedores")
            }
        }
    }

    fun remover(fornecedor: Fornecedor) {
        CoroutineScope(Dispatchers.IO).launch {
            try {
                dao.deletar(fornecedor)
                withContext(Dispatchers.Main) { fornecedores.remove(fornecedor) }
                avisar(context, "${fornecedor.razaoSocial} removido")
            } catch (e: Exception) {
                Log.e(TAG, "Erro ao remover fornecedor ${fornecedor.id}", e)
                avisar(context, "Erro ao remover fornecedor")
            }
        }
    }

    val filtrados = fornecedores.filter {
        it.cnpj.contains(buscaCnpj) &&
            (buscaRazao.isBlank() || it.razaoSocial.contains(buscaRazao.trim(), ignoreCase = true))
    }

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
                valor = buscaCnpj,
                onValorChange = { buscaCnpj = soDigitos(it, 14) },
                teclado = KeyboardType.Number,
                transformacao = mascaraCnpj,
                dica = "Buscar por CNPJ"
            )
            Spacer(Modifier.height(8.dp))
            CampoFormulario(
                rotulo = "Razão social",
                valor = buscaRazao,
                onValorChange = { buscaRazao = it },
                dica = "Buscar por nome"
            )
            Text(
                "${filtrados.size} de ${fornecedores.size} fornecedores · toque para ver, segure para remover",
                style = MaterialTheme.typography.bodySmall,
                color = CinzaTexto,
                modifier = Modifier.padding(vertical = 8.dp)
            )

            when {
                carregando -> Carregando()
                filtrados.isEmpty() -> EstadoVazio(
                    if (fornecedores.isEmpty()) "Nenhum fornecedor cadastrado ainda."
                    else "Nenhum fornecedor encontrado com esse filtro."
                )
                else -> LazyColumn(
                    verticalArrangement = Arrangement.spacedBy(8.dp),
                    contentPadding = PaddingValues(bottom = 88.dp)
                ) {
                    items(filtrados, key = { it.id }) { fornecedor ->
                        FornecedorCard(
                            fornecedor = fornecedor,
                            onClick = { navController.navigate(Rotas.fornecedorDetalhe(fornecedor.id)) },
                            onLongClick = { paraRemover = fornecedor }
                        )
                    }
                }
            }
        }
    }

    // Confirmação da remoção feita com clique longo
    paraRemover?.let { f ->
        AlertDialog(
            onDismissRequest = { paraRemover = null },
            title = { Text("Remover fornecedor?") },
            text = { Text("${f.razaoSocial} (${formatarCnpj(f.cnpj)}) será apagado do banco de dados.") },
            confirmButton = {
                TextButton(onClick = {
                    paraRemover = null
                    remover(f)
                }) {
                    Text("REMOVER", color = MaterialTheme.colorScheme.error)
                }
            },
            dismissButton = { TextButton(onClick = { paraRemover = null }) { Text("CANCELAR") } }
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
fun FornecedorCadastroScreen(navController: NavController) {
    val context = LocalContext.current
    val dao = AppDatabase.getDatabase(context).fornecedorDao()
    val form = remember { FornecedorFormState() }
    var salvando by remember { mutableStateOf(false) }

    fun salvar() {
        val erros = validarFornecedor(form.razaoSocial, form.cnpj, form.prazo)
        if (erros.isNotEmpty()) {
            form.erros = erros
            return
        }
        salvando = true
        val novo = Fornecedor(
            razaoSocial = form.razaoSocial.trim(),
            cnpj = form.cnpj,
            categoria = form.categoria,
            prazoEntregaDias = form.prazo.toInt(),
            observacoes = form.observacoes.trim()
        )
        CoroutineScope(Dispatchers.IO).launch {
            try {
                if (dao.buscarPorCnpj(novo.cnpj) != null) {
                    withContext(Dispatchers.Main) {
                        form.erros = mapOf("cnpj" to "CNPJ já cadastrado")
                        salvando = false
                    }
                } else {
                    val id = dao.inserir(novo).toInt()
                    withContext(Dispatchers.Main) {
                        Toast.makeText(context, "Fornecedor salvo no banco!", Toast.LENGTH_SHORT).show()
                        navController.navigate(Rotas.fornecedorDetalhe(id)) {
                            popUpTo(Rotas.FORNECEDOR_CADASTRO) { inclusive = true }
                        }
                    }
                }
            } catch (e: Exception) {
                Log.e(TAG, "Erro ao inserir fornecedor", e)
                withContext(Dispatchers.Main) { salvando = false }
                avisar(context, "Erro ao salvar fornecedor")
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
            FormularioFornecedor(form)
            BotaoPrimario(
                texto = if (salvando) "SALVANDO..." else "CADASTRAR FORNECEDOR",
                icone = Icons.Default.Check,
                onClick = { if (!salvando) salvar() }
            )
        }
    }
}

// ======================= DETALHE =======================

@Composable
fun FornecedorDetalheScreen(navController: NavController, fornecedorId: Int) {
    val context = LocalContext.current
    val dao = AppDatabase.getDatabase(context).fornecedorDao()

    var fornecedorDoBanco by remember { mutableStateOf<Fornecedor?>(null) }
    // Outros estabelecimentos da mesma empresa (consulta filtrada pela raiz do CNPJ)
    var mesmaEmpresa by remember { mutableStateOf<List<Fornecedor>>(emptyList()) }
    var carregando by remember { mutableStateOf(true) }
    var editando by remember { mutableStateOf(false) }
    var confirmarRemocao by remember { mutableStateOf(false) }
    val form = remember { FornecedorFormState() }

    /** Busca no banco o fornecedor do id e os outros estabelecimentos da mesma empresa. */
    suspend fun carregarDoBanco() {
        val f = dao.buscarPorId(fornecedorId)
        val outros = if (f == null) emptyList()
        else dao.buscarPorRaizCnpj(raizCnpj(f.cnpj)).filter { it.id != f.id }
        withContext(Dispatchers.Main) {
            fornecedorDoBanco = f
            mesmaEmpresa = outros
            carregando = false
        }
    }

    LaunchedEffect(fornecedorId) {
        CoroutineScope(Dispatchers.IO).launch {
            try {
                carregarDoBanco()
            } catch (e: Exception) {
                Log.e(TAG, "Erro ao buscar fornecedor $fornecedorId", e)
                withContext(Dispatchers.Main) { carregando = false }
                avisar(context, "Erro ao carregar fornecedor")
            }
        }
    }

    val fornecedor = fornecedorDoBanco

    fun salvarEdicao(atual: Fornecedor) {
        val erros = validarFornecedor(form.razaoSocial, form.cnpj, form.prazo)
        if (erros.isNotEmpty()) {
            form.erros = erros
            return
        }
        val atualizado = atual.copy(
            razaoSocial = form.razaoSocial.trim(), cnpj = form.cnpj,
            categoria = form.categoria, prazoEntregaDias = form.prazo.toInt(),
            observacoes = form.observacoes.trim()
        )
        CoroutineScope(Dispatchers.IO).launch {
            try {
                val outroComMesmoCnpj = dao.buscarPorCnpj(atualizado.cnpj)
                if (outroComMesmoCnpj != null && outroComMesmoCnpj.id != atual.id) {
                    withContext(Dispatchers.Main) { form.erros = mapOf("cnpj" to "CNPJ já cadastrado") }
                } else {
                    dao.atualizar(atualizado)
                    // Recarrega do banco: se o CNPJ mudou, a lista "mesma empresa" muda junto
                    carregarDoBanco()
                    withContext(Dispatchers.Main) { editando = false }
                    avisar(context, "Alterações salvas")
                }
            } catch (e: Exception) {
                Log.e(TAG, "Erro ao atualizar fornecedor ${atual.id}", e)
                avisar(context, "Erro ao salvar alterações")
            }
        }
    }

    fun remover(atual: Fornecedor) {
        CoroutineScope(Dispatchers.IO).launch {
            try {
                dao.deletar(atual)
                withContext(Dispatchers.Main) { navController.popBackStack() }
                avisar(context, "${atual.razaoSocial} removido")
            } catch (e: Exception) {
                Log.e(TAG, "Erro ao remover fornecedor ${atual.id}", e)
                avisar(context, "Erro ao remover fornecedor")
            }
        }
    }

    Scaffold(
        topBar = {
            BarraTopo(
                titulo = if (editando) "Editar fornecedor" else "Detalhe do fornecedor",
                onVoltar = { if (editando) editando = false else navController.popBackStack() },
                acoes = {
                    if (fornecedor != null && !editando) {
                        IconButton(onClick = { form.carregar(fornecedor); editando = true }) {
                            Icon(Icons.Default.Edit, contentDescription = "Editar")
                        }
                    }
                }
            )
        }
    ) { padding ->
        if (carregando) {
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
            if (editando) {
                FormularioFornecedor(form)
                Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                    OutlinedButton(
                        onClick = { editando = false },
                        modifier = Modifier.weight(1f).heightIn(min = 48.dp),
                        shape = RoundedCornerShape(10.dp)
                    ) { Text("CANCELAR") }
                    BotaoPrimario(
                        texto = "SALVAR",
                        icone = Icons.Default.Check,
                        modifier = Modifier.weight(1f),
                        onClick = { salvarEdicao(fornecedor) }
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

                // Informações calculadas a partir dos dados
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

                // Vem do banco: consulta buscarPorRaizCnpj + navegação secundária
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
                    onClick = { confirmarRemocao = true },
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

    if (confirmarRemocao && fornecedor != null) {
        AlertDialog(
            onDismissRequest = { confirmarRemocao = false },
            title = { Text("Remover fornecedor?") },
            text = { Text("${fornecedor.razaoSocial} será apagado do banco de dados.") },
            confirmButton = {
                TextButton(onClick = {
                    confirmarRemocao = false
                    remover(fornecedor)
                }) { Text("REMOVER", color = MaterialTheme.colorScheme.error) }
            },
            dismissButton = { TextButton(onClick = { confirmarRemocao = false }) { Text("CANCELAR") } }
        )
    }
}
