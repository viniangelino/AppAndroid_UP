package br.com.gestaosuprimentos.ui.telas

import androidx.navigation.NavController
import br.com.gestaosuprimentos.navigation.Rotas
import br.com.gestaosuprimentos.navigation.irParaArea
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.KeyboardArrowDown
import androidx.compose.material.icons.filled.KeyboardArrowUp
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import br.com.gestaosuprimentos.ui.componentes.BarraTopo
import br.com.gestaosuprimentos.ui.componentes.LogoApp
import br.com.gestaosuprimentos.ui.theme.CinzaTexto

private val PERGUNTAS = listOf(
    "Como cadastro um cliente?" to
        "Na aba Clientes, toque em \"Cadastrar\". Nome e CPF são obrigatórios; telefone e nascimento são opcionais. O CPF é conferido pelo dígito verificador.",
    "Como marco um cliente como inativo?" to
        "Na lista de clientes, desmarque a caixa ao lado do nome. Também dá para ativar/desativar pela tela de detalhe.",
    "Como removo um cliente ou fornecedor?" to
        "Clientes: toque na lixeira do card. Fornecedores: segure o dedo sobre o card. Os dois também têm o botão Remover na tela de detalhe.",
    "Como edito um cadastro?" to
        "Abra o detalhe do item e toque no lápis no canto superior direito.",
    "O que é matriz e filial?" to
        "Os 8 primeiros dígitos do CNPJ identificam a empresa. Se os 4 seguintes forem 0001, é a matriz; senão, é uma filial. O detalhe do fornecedor mostra os outros estabelecimentos da mesma empresa.",
    "Como é calculada a previsão de entrega?" to
        "A partir de hoje, somando o prazo do fornecedor em dias úteis (sábado e domingo não contam).",
    "Os dados ficam salvos?" to
        "Nesta versão os dados ficam só na memória do app e somem ao fechar. O salvamento permanente entra na próxima versão."
)

@Composable
fun AjudaScreen(navController: NavController) {
    Scaffold(topBar = { BarraTopo("Ajuda", onMenu = { irParaArea(navController, Rotas.MENU) }) }) { padding ->
        LazyColumn(
            modifier = Modifier.padding(padding).fillMaxSize(),
            contentPadding = PaddingValues(16.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            item {
                Column(Modifier.fillMaxWidth(), horizontalAlignment = Alignment.CenterHorizontally) {
                    LogoApp()
                    Spacer(Modifier.height(8.dp))
                    Text("Perguntas frequentes", style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Bold)
                    Text("Toque numa pergunta para ver a resposta", color = CinzaTexto)
                    Spacer(Modifier.height(8.dp))
                }
            }
            items(PERGUNTAS) { (pergunta, resposta) ->
                ItemAjuda(pergunta, resposta)
            }
            item {
                Text(
                    "Gestão de Suprimentos · versão 1.0 (MAF)",
                    style = MaterialTheme.typography.bodySmall,
                    color = CinzaTexto,
                    textAlign = TextAlign.Center,
                    modifier = Modifier.fillMaxWidth().padding(top = 16.dp)
                )
            }
        }
    }
}

@Composable
private fun ItemAjuda(pergunta: String, resposta: String) {
    var aberto by remember { mutableStateOf(false) }
    Card(
        onClick = { aberto = !aberto },
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(14.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White)
    ) {
        Column(Modifier.padding(16.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Text(pergunta, fontWeight = FontWeight.Bold, modifier = Modifier.weight(1f))
                Icon(
                    if (aberto) Icons.Default.KeyboardArrowUp else Icons.Default.KeyboardArrowDown,
                    contentDescription = if (aberto) "Fechar" else "Abrir"
                )
            }
            AnimatedVisibility(visible = aberto) {
                Text(resposta, color = CinzaTexto, modifier = Modifier.padding(top = 8.dp))
            }
        }
    }
}
