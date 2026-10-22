@file:OptIn(ExperimentalMaterial3Api::class)

package br.com.gestaosuprimentos.ui.componentes

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.RowScope
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.Menu
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextField
import androidx.compose.material3.TextFieldDefaults
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import br.com.gestaosuprimentos.ui.theme.CinzaCampo
import br.com.gestaosuprimentos.ui.theme.CinzaTexto

/** Logo do protótipo: quadrado verde arredondado com uma caixa. */
@Composable
fun LogoApp(tamanho: Dp = 72.dp) {
    Box(
        modifier = Modifier
            .size(tamanho)
            .clip(RoundedCornerShape(tamanho * 0.3f))
            .background(MaterialTheme.colorScheme.primary),
        contentAlignment = Alignment.Center
    ) {
        Text("📦", fontSize = (tamanho.value * 0.45f).sp)
    }
}

/** TopAppBar padrão do app: seta de voltar OU ícone de menu (hambúrguer do Figma). */
@Composable
fun BarraTopo(
    titulo: String,
    onVoltar: (() -> Unit)? = null,
    onMenu: (() -> Unit)? = null,
    acoes: @Composable RowScope.() -> Unit = {}
) {
    TopAppBar(
        title = { Text(titulo, fontWeight = FontWeight.Bold) },
        navigationIcon = {
            when {
                onVoltar != null -> IconButton(onClick = onVoltar) {
                    Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Voltar")
                }
                onMenu != null -> IconButton(onClick = onMenu) {
                    Icon(Icons.Default.Menu, contentDescription = "Ir para o menu")
                }
            }
        },
        actions = acoes,
        colors = TopAppBarDefaults.topAppBarColors(
            containerColor = MaterialTheme.colorScheme.primary,
            titleContentColor = MaterialTheme.colorScheme.onPrimary,
            navigationIconContentColor = MaterialTheme.colorScheme.onPrimary,
            actionIconContentColor = MaterialTheme.colorScheme.onPrimary
        )
    )
}

/** Campo no estilo do Figma: rótulo em cima e caixa cinza arredondada sem linha. */
@Composable
fun CampoFormulario(
    rotulo: String,
    valor: String,
    onValorChange: (String) -> Unit,
    modifier: Modifier = Modifier,
    teclado: KeyboardType = KeyboardType.Text,
    linhas: Int = 1,
    transformacao: VisualTransformation = VisualTransformation.None,
    erro: String? = null,
    dica: String? = null
) {
    Column(modifier.fillMaxWidth()) {
        Text(
            text = "${rotulo.uppercase()}:",
            style = MaterialTheme.typography.labelMedium,
            fontWeight = FontWeight.Bold,
            modifier = Modifier.padding(start = 4.dp, bottom = 4.dp)
        )
        TextField(
            value = valor,
            onValueChange = onValorChange,
            modifier = Modifier.fillMaxWidth(),
            singleLine = linhas == 1,
            minLines = linhas,
            keyboardOptions = KeyboardOptions(keyboardType = teclado),
            visualTransformation = transformacao,
            isError = erro != null,
            placeholder = dica?.let { texto -> @Composable { Text(texto, color = CinzaTexto) } },
            supportingText = erro?.let { texto -> @Composable { Text(texto) } },
            shape = RoundedCornerShape(10.dp),
            colors = TextFieldDefaults.colors(
                focusedContainerColor = CinzaCampo,
                unfocusedContainerColor = CinzaCampo,
                errorContainerColor = CinzaCampo,
                focusedIndicatorColor = Color.Transparent,
                unfocusedIndicatorColor = Color.Transparent,
                errorIndicatorColor = Color.Transparent,
                disabledIndicatorColor = Color.Transparent
            )
        )
    }
}

@Composable
fun BotaoPrimario(
    texto: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    icone: ImageVector? = null
) {
    Button(
        onClick = onClick,
        modifier = modifier.heightIn(min = 48.dp),
        shape = RoundedCornerShape(10.dp)
    ) {
        if (icone != null) {
            Icon(icone, contentDescription = null, modifier = Modifier.size(18.dp))
            Spacer(Modifier.width(8.dp))
        }
        Text(texto, fontWeight = FontWeight.Bold)
    }
}

/** Círculo com as iniciais do nome. */
@Composable
fun Avatar(texto: String, tamanho: Dp = 44.dp, cor: Color = MaterialTheme.colorScheme.secondary) {
    Box(
        modifier = Modifier.size(tamanho).clip(CircleShape).background(cor),
        contentAlignment = Alignment.Center
    ) {
        Text(
            texto,
            fontWeight = FontWeight.Bold,
            fontSize = (tamanho.value * 0.38f).sp,
            color = MaterialTheme.colorScheme.onSecondary
        )
    }
}

/** Etiqueta pequena e arredondada (status, categoria...). */
@Composable
fun Selo(texto: String, fundo: Color, conteudo: Color) {
    Surface(shape = RoundedCornerShape(50), color = fundo) {
        Text(
            texto,
            color = conteudo,
            style = MaterialTheme.typography.labelSmall,
            fontWeight = FontWeight.Bold,
            modifier = Modifier.padding(horizontal = 8.dp, vertical = 3.dp)
        )
    }
}

/** Card branco com título, usado para agrupar informações nas telas de detalhe. */
@Composable
fun SecaoCard(titulo: String, conteudo: @Composable ColumnScope.() -> Unit) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        shape = RoundedCornerShape(14.dp)
    ) {
        Column(Modifier.padding(16.dp)) {
            Text(
                titulo.uppercase(),
                style = MaterialTheme.typography.labelLarge,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.primary
            )
            HorizontalDivider(Modifier.padding(vertical = 8.dp))
            conteudo()
        }
    }
}

/** Linha "rótulo ........ valor". */
@Composable
fun LinhaInfo(rotulo: String, valor: String, corValor: Color = Color.Unspecified) {
    Row(
        modifier = Modifier.fillMaxWidth().padding(vertical = 5.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(rotulo, style = MaterialTheme.typography.bodyMedium, color = CinzaTexto)
        Spacer(Modifier.width(12.dp))
        Text(
            valor,
            style = MaterialTheme.typography.bodyMedium,
            fontWeight = FontWeight.SemiBold,
            color = corValor,
            textAlign = TextAlign.End,
            maxLines = 2,
            overflow = TextOverflow.Ellipsis
        )
    }
}

/** Cabeçalho das telas de detalhe: avatar, nome, subtítulo e selo de status. */
@Composable
fun CabecalhoDetalhe(
    iniciais: String,
    titulo: String,
    subtitulo: String,
    selo: @Composable () -> Unit
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.primary),
        shape = RoundedCornerShape(14.dp)
    ) {
        Row(Modifier.padding(16.dp), verticalAlignment = Alignment.CenterVertically) {
            Avatar(iniciais, tamanho = 56.dp)
            Spacer(Modifier.width(16.dp))
            Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                Text(
                    titulo,
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onPrimary
                )
                Text(subtitulo, color = MaterialTheme.colorScheme.onPrimary.copy(alpha = 0.8f))
                selo()
            }
        }
    }
}

@Composable
fun EstadoVazio(mensagem: String, modifier: Modifier = Modifier) {
    Column(
        modifier = modifier.fillMaxWidth().padding(32.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Icon(Icons.Default.Info, contentDescription = null, tint = CinzaTexto, modifier = Modifier.size(48.dp))
        Spacer(Modifier.padding(4.dp))
        Text(mensagem, color = CinzaTexto, textAlign = TextAlign.Center)
    }
}
