package br.com.gestaosuprimentos.ui.telas

import android.util.Log
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
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import br.com.gestaosuprimentos.data.AppDatabase
import br.com.gestaosuprimentos.data.Cliente
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

private const val TAG = "ClientesScreens"

// ======================= LISTA =======================

@Composable
fun ClientesListaScreen(navController: NavController) {
    val context = LocalContext.current
    val dao = AppDatabase.getDatabase(context).clienteDao()

    // A lista da tela agora é só uma cópia do que está no banco
    val clientes = remember { mutableStateListOf<Cliente>() }
    var carregando by remember { mutableStateOf(true) }

    // Filtros (os campos CPF/NOME do protótipo "Cliente")
    var buscaCpf by rememberSaveable { mutableStateOf("") }
    var buscaNome by rememberSaveable { mutableStateOf("") }
    val mascaraCpf = remember { Mascara("###.###.###-##") }

    // Busca os clientes no banco sempre que a tela abre
    LaunchedEffect(Unit) {
        CoroutineScope(Dispatchers.IO).launch {
            try {
                val doBanco = dao.buscarTodos()
                withContext(Dispatchers.Main) {
                    clientes.clear()
                    clientes.addAll(doBanco)
                    carregando = false
                }
            } catch (e: Exception) {
                Log.e(TAG, "Erro ao buscar clientes", e)
                withContext(Dispatchers.Main) { carregando = false }
                avisar(context, "Erro ao carregar clientes")
            }
        }
    }

    fun alternarAtivo(cliente: Cliente) {
        val atualizado = cliente.copy(ativo = !cliente.ativo)
        CoroutineScope(Dispatchers.IO).launch {
            try {
                dao.atualizar(atualizado)
                withContext(Dispatchers.Main) {
                    val i = clientes.indexOfFirst { it.id == cliente.id }
                    if (i >= 0) clientes[i] = atualizado
                }
            } catch (e: Exception) {
                Log.e(TAG, "Erro ao atualizar cliente ${cliente.id}", e)
                avisar(context, "Erro ao salvar alteração")
            }
        }
    }

    fun remover(cliente: Cliente) {
        CoroutineScope(Dispatchers.IO).launch {
            try {
                dao.deletar(cliente)
                withContext(Dispatchers.Main) { clientes.remove(cliente) }
                avisar(context, "${cliente.nome} removido")
            } catch (e: Exception) {
                Log.e(TAG, "Erro ao remover cliente ${cliente.id}", e)
                avisar(context, "Erro ao remover cliente")
            }
        }
    }

    val filtrados = clientes.filter {
        it.cpf.contains(buscaCpf) && (buscaNome.isBlank() || it.nome.contains(buscaNome.trim(), ignoreCase = true))
    }

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
                    valor = buscaCpf,
                    onValorChange = { buscaCpf = soDigitos(it, 11) },
                    modifier = Modifier.weight(1f),
                    teclado = KeyboardType.Number,
                    transformacao = mascaraCpf,
                    dica = "Buscar"
                )
                CampoFormulario(
                    rotulo = "Nome",
                    valor = buscaNome,
                    onValorChange = { buscaNome = it },
                    modifier = Modifier.weight(1.3f),
                    dica = "Buscar"
                )
            }
            Text(
                "${filtrados.size} de ${clientes.size} clientes · ${clientes.count { it.ativo }} ativos",
                style = MaterialTheme.typography.bodySmall,
                color = CinzaTexto,
                modifier = Modifier.padding(vertical = 8.dp)
            )

            when {
                carregando -> Carregando()
                filtrados.isEmpty() -> EstadoVazio(
                    if (clientes.isEmpty()) "Nenhum cliente cadastrado ainda.\nToque em Cadastrar."
                    else "Nenhum cliente encontrado com esse filtro."
                )
                else -> LazyColumn(
                    verticalArrangement = Arrangement.spacedBy(8.dp),
                    contentPadding = PaddingValues(bottom = 88.dp)
                ) {
                    items(filtrados, key = { it.id }) { cliente ->
                        ClienteCard(
                            cliente = cliente,
                            onClick = { navController.navigate(Rotas.clienteDetalhe(cliente.id)) },
                            onAlternarAtivo = { alternarAtivo(cliente) },
                            onRemover = { remover(cliente) }
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
            // Checkbox marca o cliente como ativo/inativo (salva no banco)
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
fun ClienteCadastroScreen(navController: NavController) {
    val context = LocalContext.current
    val dao = AppDatabase.getDatabase(context).clienteDao()
    val form = remember { ClienteFormState() }
    var salvando by remember { mutableStateOf(false) }

    fun salvar() {
        val erros = validarCliente(form.nome, form.cpf, form.telefone, form.nascimento)
        if (erros.isNotEmpty()) {
            form.erros = erros
            return
        }
        salvando = true
        val novo = Cliente(
            nome = form.nome.trim(),
            cpf = form.cpf,
            telefone = form.telefone,
            nascimento = form.nascimento
        )
        CoroutineScope(Dispatchers.IO).launch {
            try {
                // Consulta filtrada: já existe alguém com esse CPF no banco?
                if (dao.buscarPorCpf(novo.cpf) != null) {
                    withContext(Dispatchers.Main) {
                        form.erros = mapOf("cpf" to "CPF já cadastrado")
                        salvando = false
                    }
                } else {
                    val id = dao.inserir(novo).toInt()
                    withContext(Dispatchers.Main) {
                        Toast.makeText(context, "Cliente salvo no banco!", Toast.LENGTH_SHORT).show()
                        // Abre o detalhe do cliente novo no lugar do formulário
                        navController.navigate(Rotas.clienteDetalhe(id)) {
                            popUpTo(Rotas.CLIENTE_CADASTRO) { inclusive = true }
                        }
                    }
                }
            } catch (e: Exception) {
                Log.e(TAG, "Erro ao inserir cliente", e)
                withContext(Dispatchers.Main) { salvando = false }
                avisar(context, "Erro ao salvar cliente")
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
            FormularioCliente(form)
            BotaoPrimario(
                texto = if (salvando) "SALVANDO..." else "CADASTRAR CLIENTE",
                icone = Icons.Default.Check,
                onClick = { if (!salvando) salvar() }
            )
        }
    }
}

// ======================= DETALHE =======================

@Composable
fun ClienteDetalheScreen(navController: NavController, clienteId: Int) {
    val context = LocalContext.current
    val dao = AppDatabase.getDatabase(context).clienteDao()

    var clienteDoBanco by remember { mutableStateOf<Cliente?>(null) }
    var carregando by remember { mutableStateOf(true) }
    var editando by remember { mutableStateOf(false) }
    var confirmarRemocao by remember { mutableStateOf(false) }
    val form = remember { ClienteFormState() }

    // Busca o cliente DIRETO no banco, pelo id que veio na rota
    LaunchedEffect(clienteId) {
        CoroutineScope(Dispatchers.IO).launch {
            try {
                val c = dao.buscarPorId(clienteId)
                withContext(Dispatchers.Main) {
                    clienteDoBanco = c
                    carregando = false
                }
            } catch (e: Exception) {
                Log.e(TAG, "Erro ao buscar cliente $clienteId", e)
                withContext(Dispatchers.Main) { carregando = false }
                avisar(context, "Erro ao carregar cliente")
            }
        }
    }

    // Cópia local para o Kotlin conseguir tratar como "não nulo" depois do if
    val cliente = clienteDoBanco

    fun salvarEdicao(atual: Cliente) {
        val erros = validarCliente(form.nome, form.cpf, form.telefone, form.nascimento)
        if (erros.isNotEmpty()) {
            form.erros = erros
            return
        }
        val atualizado = atual.copy(
            nome = form.nome.trim(), cpf = form.cpf,
            telefone = form.telefone, nascimento = form.nascimento
        )
        CoroutineScope(Dispatchers.IO).launch {
            try {
                val outroComMesmoCpf = dao.buscarPorCpf(atualizado.cpf)
                if (outroComMesmoCpf != null && outroComMesmoCpf.id != atual.id) {
                    withContext(Dispatchers.Main) { form.erros = mapOf("cpf" to "CPF já cadastrado") }
                } else {
                    dao.atualizar(atualizado)
                    withContext(Dispatchers.Main) {
                        clienteDoBanco = atualizado
                        editando = false
                    }
                    avisar(context, "Alterações salvas")
                }
            } catch (e: Exception) {
                Log.e(TAG, "Erro ao atualizar cliente ${atual.id}", e)
                avisar(context, "Erro ao salvar alterações")
            }
        }
    }

    fun alternarAtivo(atual: Cliente) {
        val atualizado = atual.copy(ativo = !atual.ativo)
        CoroutineScope(Dispatchers.IO).launch {
            try {
                dao.atualizar(atualizado)
                withContext(Dispatchers.Main) { clienteDoBanco = atualizado }
            } catch (e: Exception) {
                Log.e(TAG, "Erro ao atualizar cliente ${atual.id}", e)
                avisar(context, "Erro ao salvar alteração")
            }
        }
    }

    fun remover(atual: Cliente) {
        CoroutineScope(Dispatchers.IO).launch {
            try {
                dao.deletar(atual)
                withContext(Dispatchers.Main) { navController.popBackStack() }
                avisar(context, "${atual.nome} removido")
            } catch (e: Exception) {
                Log.e(TAG, "Erro ao remover cliente ${atual.id}", e)
                avisar(context, "Erro ao remover cliente")
            }
        }
    }

    Scaffold(
        topBar = {
            BarraTopo(
                titulo = if (editando) "Editar cliente" else "Detalhe do cliente",
                onVoltar = { if (editando) editando = false else navController.popBackStack() },
                acoes = {
                    if (cliente != null && !editando) {
                        IconButton(onClick = { form.carregar(cliente); editando = true }) {
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
            if (editando) {
                // ---- Edição no próprio detalhe ----
                FormularioCliente(form)
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
                        onClick = { salvarEdicao(cliente) }
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
                    onClick = { alternarAtivo(cliente) },
                    modifier = Modifier.fillMaxWidth()
                )
                OutlinedButton(
                    onClick = { confirmarRemocao = true },
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

    if (confirmarRemocao && cliente != null) {
        AlertDialog(
            onDismissRequest = { confirmarRemocao = false },
            title = { Text("Remover cliente?") },
            text = { Text("${cliente.nome} será apagado do banco de dados.") },
            confirmButton = {
                TextButton(onClick = {
                    confirmarRemocao = false
                    remover(cliente)
                }) { Text("REMOVER", color = MaterialTheme.colorScheme.error) }
            },
            dismissButton = {
                TextButton(onClick = { confirmarRemocao = false }) { Text("CANCELAR") }
            }
        )
    }
}
