package br.com.gestaosuprimentos.ui.telas

import android.widget.Toast
import androidx.compose.ui.platform.LocalContext
import androidx.navigation.NavController
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Check
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import br.com.gestaosuprimentos.data.GestaoViewModel
import br.com.gestaosuprimentos.navigation.Rotas
import br.com.gestaosuprimentos.navigation.irParaArea
import br.com.gestaosuprimentos.ui.componentes.BarraTopo
import br.com.gestaosuprimentos.ui.componentes.BotaoPrimario
import br.com.gestaosuprimentos.ui.componentes.CampoFormulario
import br.com.gestaosuprimentos.ui.componentes.LogoApp
import br.com.gestaosuprimentos.ui.theme.CinzaTexto

// ======================= LOGIN =======================

@Composable
fun LoginScreen(navController: NavController, vm: GestaoViewModel) {
    var login by rememberSaveable { mutableStateOf("") }
    var senha by rememberSaveable { mutableStateOf("") }
    var erro by remember { mutableStateOf<String?>(null) }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
            .verticalScroll(rememberScrollState())
            .padding(24.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Spacer(Modifier.height(56.dp))
        Row(verticalAlignment = Alignment.CenterVertically) {
            LogoApp(76.dp)
            Spacer(Modifier.width(16.dp))
            Column {
                Text("GESTÃO DE", letterSpacing = 4.sp, color = CinzaTexto, fontWeight = FontWeight.SemiBold)
                Text(
                    "SUPRIMENTOS",
                    fontSize = 30.sp,
                    fontWeight = FontWeight.ExtraBold,
                    color = MaterialTheme.colorScheme.primary
                )
            }
        }
        Spacer(Modifier.height(64.dp))

        CampoFormulario(rotulo = "Login", valor = login, onValorChange = { login = it; erro = null })
        Spacer(Modifier.height(16.dp))
        CampoFormulario(
            rotulo = "Senha",
            valor = senha,
            onValorChange = { senha = it; erro = null },
            teclado = KeyboardType.Password,
            transformacao = PasswordVisualTransformation()
        )
        erro?.let {
            Spacer(Modifier.height(8.dp))
            Text(it, color = MaterialTheme.colorScheme.error)
        }
        Spacer(Modifier.height(24.dp))

        BotaoPrimario(
            texto = "ENTRAR",
            onClick = {
                if (vm.entrar(login, senha)) {
                    // Tira o login da pilha: o "voltar" do Menu não retorna para cá
                    navController.navigate(Rotas.MENU) { popUpTo(Rotas.LOGIN) { inclusive = true } }
                } else {
                    erro = "Login ou senha inválidos"
                }
            },
            modifier = Modifier.width(180.dp)
        )
        TextButton(onClick = { navController.navigate(Rotas.CRIAR_ACESSO) }) {
            Text("CRIAR NOVO ACESSO", fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.onBackground)
        }
        Spacer(Modifier.height(32.dp))
        Text("Acesso de teste: admin / admin", style = MaterialTheme.typography.bodySmall, color = CinzaTexto)
    }
}

// ======================= CRIAR ACESSO =======================

@Composable
fun CriarAcessoScreen(navController: NavController, vm: GestaoViewModel) {
    val context = LocalContext.current
    var login by rememberSaveable { mutableStateOf("") }
    var senha by rememberSaveable { mutableStateOf("") }
    var confirmacao by rememberSaveable { mutableStateOf("") }
    var erro by remember { mutableStateOf<String?>(null) }

    Scaffold(topBar = { BarraTopo("Criar novo acesso", onVoltar = { navController.popBackStack() }) }) { padding ->
        Column(
            modifier = Modifier
                .padding(padding)
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
                .padding(24.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            LogoApp()
            Text(
                "Crie um login para outro integrante da equipe acessar o app.",
                textAlign = TextAlign.Center,
                color = CinzaTexto
            )
            CampoFormulario(rotulo = "Login", valor = login, onValorChange = { login = it; erro = null })
            CampoFormulario(
                rotulo = "Senha",
                valor = senha,
                onValorChange = { senha = it; erro = null },
                teclado = KeyboardType.Password,
                transformacao = PasswordVisualTransformation(),
                dica = "Mínimo de 4 caracteres"
            )
            CampoFormulario(
                rotulo = "Confirmar senha",
                valor = confirmacao,
                onValorChange = { confirmacao = it; erro = null },
                teclado = KeyboardType.Password,
                transformacao = PasswordVisualTransformation()
            )
            erro?.let { Text(it, color = MaterialTheme.colorScheme.error) }

            BotaoPrimario(
                texto = "CRIAR ACESSO",
                icone = Icons.Default.Check,
                onClick = {
                    erro = when {
                        senha.length < 4 -> "A senha precisa de pelo menos 4 caracteres"
                        senha != confirmacao -> "As senhas não conferem"
                        else -> vm.criarAcesso(login, senha)
                    }
                    if (erro == null) {
                        Toast.makeText(context, "Acesso criado! Entre com o novo login.", Toast.LENGTH_LONG).show()
                        navController.popBackStack()
                    }
                }
            )
        }
    }
}
