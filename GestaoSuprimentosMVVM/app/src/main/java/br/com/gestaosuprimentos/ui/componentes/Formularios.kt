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
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import br.com.gestaosuprimentos.data.local.CATEGORIAS_FORNECEDOR
import br.com.gestaosuprimentos.util.Mascara
import br.com.gestaosuprimentos.util.padraoTelefone
import br.com.gestaosuprimentos.util.soDigitos
import br.com.gestaosuprimentos.viewmodel.ClienteForm
import br.com.gestaosuprimentos.viewmodel.FornecedorForm

/*
 * Formulários "burros": recebem o texto atual (que mora no UiState do ViewModel)
 * e avisam cada mudança com onChange. Não guardam estado nenhum.
 * São usados na tela de cadastro e na edição dentro da tela de detalhe.
 */

// ===================== Cliente =====================

@Composable
fun FormularioCliente(form: ClienteForm, onChange: (ClienteForm) -> Unit) {
    val mascaraCpf = remember { Mascara("###.###.###-##") }
    val mascaraData = remember { Mascara("##/##/####") }

    Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
        CampoFormulario(
            rotulo = "Nome",
            valor = form.nome,
            onValorChange = { onChange(form.copy(nome = it, erros = form.erros - "nome")) },
            erro = form.erros["nome"]
        )
        CampoFormulario(
            rotulo = "CPF",
            valor = form.cpf,
            onValorChange = { onChange(form.copy(cpf = soDigitos(it, 11), erros = form.erros - "cpf")) },
            teclado = KeyboardType.Number,
            transformacao = mascaraCpf,
            erro = form.erros["cpf"],
            dica = "000.000.000-00"
        )
        CampoFormulario(
            rotulo = "Telefone",
            valor = form.telefone,
            onValorChange = { onChange(form.copy(telefone = soDigitos(it, 11), erros = form.erros - "telefone")) },
            teclado = KeyboardType.Phone,
            transformacao = Mascara(padraoTelefone(form.telefone)),
            erro = form.erros["telefone"],
            dica = "(41) 99999-9999 — opcional"
        )
        CampoFormulario(
            rotulo = "Data de nascimento",
            valor = form.nascimento,
            onValorChange = { onChange(form.copy(nascimento = soDigitos(it, 8), erros = form.erros - "nascimento")) },
            teclado = KeyboardType.Number,
            transformacao = mascaraData,
            erro = form.erros["nascimento"],
            dica = "dd/mm/aaaa — opcional"
        )
    }
}

// ===================== Fornecedor =====================

@Composable
fun FormularioFornecedor(form: FornecedorForm, onChange: (FornecedorForm) -> Unit) {
    val mascaraCnpj = remember { Mascara("##.###.###/####-##") }

    Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
        CampoFormulario(
            rotulo = "Razão social",
            valor = form.razaoSocial,
            onValorChange = { onChange(form.copy(razaoSocial = it, erros = form.erros - "razaoSocial")) },
            erro = form.erros["razaoSocial"]
        )
        CampoFormulario(
            rotulo = "CNPJ",
            valor = form.cnpj,
            onValorChange = { onChange(form.copy(cnpj = soDigitos(it, 14), erros = form.erros - "cnpj")) },
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
                        onClick = { onChange(form.copy(categoria = cat)) },
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
            onValorChange = { onChange(form.copy(prazo = soDigitos(it, 3), erros = form.erros - "prazo")) },
            teclado = KeyboardType.Number,
            erro = form.erros["prazo"],
            dica = "Ex.: 5"
        )
        CampoFormulario(
            rotulo = "Observações",
            valor = form.observacoes,
            onValorChange = { onChange(form.copy(observacoes = it)) },
            linhas = 4,
            dica = "Condições, contato, pedido mínimo..."
        )
    }
}
