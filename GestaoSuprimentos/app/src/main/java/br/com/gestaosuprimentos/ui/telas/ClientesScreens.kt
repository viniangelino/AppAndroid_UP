package br.com.gestaosuprimentos.ui.telas

import android.widget.Toast
import androidx.compose.ui.platform.LocalContext
import androidx.navigation.NavController
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
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import br.com.gestaosuprimentos.data.Cliente
import br.com.gestaosuprimentos.data.GestaoViewModel
import br.com.gestaosuprimentos.navigation.Rotas
import br.com.gestaosuprimentos.navigation.irParaArea
import br.com.gestaosuprimentos.ui.componentes.*
import br.com.gestaosuprimentos.ui.theme.CinzaCampo
import br.com.gestaosuprimentos.ui.theme.CinzaTexto
import br.com.gestaosuprimentos.ui.theme.VerdeOk
import br.com.gestaosuprimentos.util.*

// ======================= LISTA =======================

@Composable
fun ClientesListaScreen(navController: NavController, vm: GestaoViewModel) {
    val context = LocalContext.current
    // Filtros (os campos CPF/NOME do protótipo "Cliente")
    var buscaCpf by rememberSaveable { mutableStateOf("") }
    var buscaNome by rememberSaveable { mutableStateOf("") }
    val mascaraCpf = remember { Mascara("###.###.###-##") }

    val filtrados = vm.clientes.filter {
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
                "${filtrados.size} de ${vm.clientes.size} clientes · ${vm.clientes.count { it.ativo }} ativos",
                style = MaterialTheme.typography.bodySmall,
                color = CinzaTexto,
                modifier = Modifier.padding(vertical = 8.dp)
            )

            if (filtrados.isEmpty()) {
                EstadoVazio(
                    if (vm.clientes.isEmpty()) "Nenhum cliente cadastrado ainda.\nToque em Cadastrar."
                    else "Nenhum cliente encontrado com esse filtro."
                )
            } else {
                LazyColumn(
                    verticalArrangement = Arrangement.spacedBy(8.dp),
                    contentPadding = PaddingValues(bottom = 88.dp)
                ) {
                    items(filtrados, key = { it.id }) { cliente ->
                        ClienteCard(
                            cliente = cliente,
                            onClick = { navController.navigate(Rotas.clienteDetalhe(cliente.id)) },
                            onAlternarAtivo = { vm.alternarAtivo(cliente.id) },
                            onRemover = {
                                vm.removerCliente(cliente.id)
                                Toast.makeText(context, "${cliente.nome} removido", Toast.LENGTH_SHORT).show()
                            }
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
            // Checkbox marca o cliente como ativo/inativo
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
fun ClienteCadastroScreen(navController: NavController, vm: GestaoViewModel) {
    val context = LocalContext.current
    val form = remember { ClienteFormState() }

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
                texto = "CADASTRAR CLIENTE",
                icone = Icons.Default.Check,
                onClick = {
                    val erros = vm.validarCliente(form.nome, form.cpf, form.telefone, form.nascimento)
                    if (erros.isEmpty()) {
                        val id = vm.adicionarCliente(form.nome, form.cpf, form.telefone, form.nascimento)
                        Toast.makeText(context, "Cliente cadastrado!", Toast.LENGTH_SHORT).show()
                        // Abre o detalhe do cliente novo no lugar do formulário
                        navController.navigate(Rotas.clienteDetalhe(id)) {
                            popUpTo(Rotas.CLIENTE_CADASTRO) { inclusive = true }
                        }
                    } else {
                        form.erros = erros
                    }
                }
            )
        }
    }
}

// ======================= DETALHE =======================

@Composable
fun ClienteDetalheScreen(navController: NavController, vm: GestaoViewModel, clienteId: Int) {
    val context = LocalContext.current
    // Busca o item certo na lista usando o id que veio pela rota
    val cliente = vm.buscarCliente(clienteId)
    var editando by remember { mutableStateOf(false) }
    var confirmarRemocao by remember { mutableStateOf(false) }
    val form = remember { ClienteFormState() }

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
                        onClick = {
                            val erros = vm.validarCliente(
                                form.nome, form.cpf, form.telefone, form.nascimento, idIgnorado = cliente.id
                            )
                            if (erros.isEmpty()) {
                                vm.atualizarCliente(
                                    cliente.copy(
                                        nome = form.nome.trim(), cpf = form.cpf,
                                        telefone = form.telefone, nascimento = form.nascimento
                                    )
                                )
                                editando = false
                                Toast.makeText(context, "Alterações salvas", Toast.LENGTH_SHORT).show()
                            } else {
                                form.erros = erros
                            }
                        }
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

                // Complexidade extra: informações calculadas a partir dos dados do item
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
                    onClick = { vm.alternarAtivo(cliente.id) },
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
            text = { Text("${cliente.nome} será removido da lista.") },
            confirmButton = {
                TextButton(onClick = {
                    confirmarRemocao = false
                    navController.popBackStack()
                    vm.removerCliente(cliente.id)
                    Toast.makeText(context, "${cliente.nome} removido", Toast.LENGTH_SHORT).show()
                }) { Text("REMOVER", color = MaterialTheme.colorScheme.error) }
            },
            dismissButton = {
                TextButton(onClick = { confirmarRemocao = false }) { Text("CANCELAR") }
            }
        )
    }
}
