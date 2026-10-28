package br.com.gestaosuprimentos

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import br.com.gestaosuprimentos.navigation.AppNavigation
import br.com.gestaosuprimentos.ui.theme.GestaoTheme

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            GestaoTheme {
                AppNavigation()
            }
        }
    }
}
