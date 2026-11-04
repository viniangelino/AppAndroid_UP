package br.com.gestaosuprimentos

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import br.com.gestaosuprimentos.data.local.AppDatabase
import br.com.gestaosuprimentos.data.repository.ClienteRepository
import br.com.gestaosuprimentos.data.repository.FornecedorRepository
import br.com.gestaosuprimentos.navigation.AppNavigation
import br.com.gestaosuprimentos.ui.theme.GestaoTheme

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // Monta as camadas uma vez: Database (Singleton) -> DAO -> Repository
        val db = AppDatabase.getDatabase(this)
        val clienteRepository = ClienteRepository(db.clienteDao())
        val fornecedorRepository = FornecedorRepository(db.fornecedorDao())

        setContent {
            GestaoTheme {
                AppNavigation(clienteRepository, fornecedorRepository)
            }
        }
    }
}
