package com.chronos.rotina

import android.content.Intent
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.result.contract.ActivityResultContracts
import android.Manifest
import android.os.Build
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.foundation.layout.Box
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import com.chronos.rotina.data.appDb
import com.chronos.rotina.data.perfilTemNome
import com.chronos.rotina.data.salvarPerfil
import com.chronos.rotina.ui.BoasVindasScreen
import com.chronos.rotina.ui.MachittoTemaWrapper
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

private val Base = Color(0xFF1E1E2E)

class AberturaActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        pedirPermissoes()
        setContent {
            MachittoTemaWrapper {
            var estado by remember { mutableStateOf("carregando") }

            LaunchedEffect(Unit) {
                val temNome = withContext(Dispatchers.IO) { appDb().perfilTemNome() }
                estado = if (temNome) "principal" else "boasvindas"
            }

            when (estado) {
                "carregando" -> Box(
                    Modifier.fillMaxSize().background(Base),
                    contentAlignment = Alignment.Center
                ) { CircularProgressIndicator(color = Color(0xFFB4BEFE)) }

                "boasvindas" -> BoasVindasScreen(
                    aoConcluir = { nome, genero ->
                        (application as MachittoApp).scopeApp.launch(Dispatchers.IO) {
                            appDb().salvarPerfil(nome, genero)
                        }
                        irParaPrincipal()
                    }
                )

                "principal" -> {
                    irParaPrincipal()
                }
            }
            }
        }
    }

    private val pedirNotif = registerForActivityResult(ActivityResultContracts.RequestPermission()) { }

    private fun pedirPermissoes() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            pedirNotif.launch(Manifest.permission.POST_NOTIFICATIONS)
        }
    }

    private fun irParaPrincipal() {
        startActivity(Intent(this, PrincipalActivity::class.java))
        @Suppress("DEPRECATION")
        overridePendingTransition(android.R.anim.fade_in, android.R.anim.fade_out)
        finish()
    }
}
