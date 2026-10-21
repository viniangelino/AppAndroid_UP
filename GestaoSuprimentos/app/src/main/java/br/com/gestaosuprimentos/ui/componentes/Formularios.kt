@file:OptIn(ExperimentalMaterial3Api::class)

package br.com.gestaosuprimentos.ui.componentes

import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Check
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import br.com.gestaosuprimentos.data.CATEGORIAS_FORNECEDOR
import br.com.gestaosuprimentos.data.Cliente
import br.com.gestaosuprimentos.data.Fornecedor
import br.com.gestaosuprimentos.util.Mascara
import br.com.gestaosuprimentos.util.padraoTelefone
import br.com.gestaosuprimentos.util.soDigitos

/*
 * Os formulários ficam aqui porque são usados em DOIS lugares:
 * na tela de cadastro e na edição dentro da tela de detalhe.
 */

// ===================== Cliente =====================

class ClienteFormState {
    var nome by mutableStateOf("")
    var cpf by mutableStateOf("")
    var telefone by mutableStateOf("")
    var nascimento by mutableStateOf("")
    var erros by mutableStateOf<Map<String, String>>(emptyMap())

    fun carregar(c: Cliente) {
        nome = c.nome; cpf = c.cpf; telefone = c.telefone; nascimento = c.nascimento
        erros = emptyMap()
    }

    fun limparErro(campo: String) {
        erros = erros - campo
    }
}

@Composable
fun FormularioCliente(form: ClienteFormState) {
    val mascaraCpf = remember { Mascara("###.###.###-##") }
    val mascaraData = remember { Mascara("##/##/####") }

    Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
        CampoFormulario(
            rotulo = "Nome",
            valor = form.nome,
            onValorChange = { form.nome = it; form.limparErro("nome") },
            erro = form.erros["nome"]
        )
        CampoFormulario(
            rotulo = "CPF",
            valor = form.cpf,
            onValorChange = { form.cpf = soDigitos(it, 11); form.limparErro("cpf") },
            teclado = KeyboardType.Number,
            transformacao = mascaraCpf,
            erro = form.erros["cpf"],
            dica = "000.000.000-00"
        )
        CampoFormulario(
            rotulo = "Telefone",
            valor = form.telefone,
            onValorChange = { form.telefone = soDigitos(it, 11); form.limparErro("telefone") },
            teclado = KeyboardType.Phone,
            transformacao = Mascara(padraoTelefone(form.telefone)),
            erro = form.erros["telefone"],
            dica = "(41) 99999-9999 — opcional"
        )
        CampoFormulario(
            rotulo = "Data de nascimento",
            valor = form.nascimento,
            onValorChange = { form.nascimento = soDigitos(it, 8); form.limparErro("nascimento") },
            teclado = KeyboardType.Number,
            transformacao = mascaraData,
            erro = form.erros["nascimento"],
            dica = "dd/mm/aaaa — opcional"
        )
    }
}

// ===================== Fornecedor =====================

class FornecedorFormState {
    var razaoSocial by mutableStateOf("")
    var cnpj by mutableStateOf("")
    var categoria by mutableStateOf(CATEGORIAS_FORNECEDOR.first())
    var prazo by mutableStateOf("")
    var observacoes by mutableStateOf("")
    var erros by mutableStateOf<Map<String, String>>(emptyMap())

    fun carregar(f: Fornecedor) {
        razaoSocial = f.razaoSocial; cnpj = f.cnpj; categoria = f.categoria
        prazo = f.prazoEntregaDias.toString(); observacoes = f.observacoes
        erros = emptyMap()
    }

    fun limparErro(campo: String) {
        erros = erros - campo
    }
}

@Composable
fun FormularioFornecedor(form: FornecedorFormState) {
    val mascaraCnpj = remember { Mascara("##.###.###/####-##") }

    Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
        CampoFormulario(
            rotulo = "Razão social",
            valor = form.razaoSocial,
            onValorChange = { form.razaoSocial = it; form.limparErro("razaoSocial") },
            erro = form.erros["razaoSocial"]
        )
        CampoFormulario(
            rotulo = "CNPJ",
            valor = form.cnpj,
            onValorChange = { form.cnpj = soDigitos(it, 14); form.limparErro("cnpj") },
            teclado = KeyboardType.Number,
            transformacao = mascaraCnpj,
            erro = form.erros["cnpj"],
            dica = "00.000.000/0000-00"
        )

        Column {
            Text(
                "CATEGORIA:",
                style = MaterialTheme.typography.labelMedium,
                fontWeight = FontWeight.Bold,
                modifier = Modifier.padding(start = 4.dp)
            )
            Row(
                modifier = Modifier.horizontalScroll(rememberScrollState()),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                CATEGORIAS_FORNECEDOR.forEach { cat ->
                    FilterChip(
                        selected = form.categoria == cat,
                        onClick = { form.categoria = cat },
                        label = { Text(cat) },
                        leadingIcon = if (form.categoria == cat) {
                            { Icon(Icons.Default.Check, contentDescription = null, modifier = Modifier.size(16.dp)) }
                        } else null
                    )
                }
            }
        }

        CampoFormulario(
            rotulo = "Prazo de entrega (dias úteis)",
            valor = form.prazo,
            onValorChange = { form.prazo = soDigitos(it, 3); form.limparErro("prazo") },
            teclado = KeyboardType.Number,
            erro = form.erros["prazo"],
            dica = "Ex.: 5"
        )
        CampoFormulario(
            rotulo = "Observações",
            valor = form.observacoes,
            onValorChange = { form.observacoes = it },
            linhas = 4,
            dica = "Condições, contato, pedido mínimo..."
        )
    }
}
