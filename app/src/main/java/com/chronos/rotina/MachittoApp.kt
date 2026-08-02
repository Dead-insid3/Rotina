package com.chronos.rotina

import android.app.Application
import android.util.Log
import com.chronos.rotina.data.AppDatabase
import com.chronos.rotina.data.corPersonalizada
import com.chronos.rotina.data.temaSalvo
import com.chronos.rotina.ui.TemaAtivo
import com.chronos.rotina.ui.paletaPersonalizada
import androidx.compose.ui.graphics.Color
import com.chronos.rotina.notif.Agendador
import com.chronos.rotina.notif.Canais
import com.chronos.rotina.notif.LembreteFinanceiro
import com.chronos.rotina.notif.CronogramaAgendador
import com.chronos.rotina.notif.SonoAgendador
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.launch

class MachittoApp : Application() {
    val scopeApp = CoroutineScope(SupervisorJob())

    val db by lazy { AppDatabase.obter(this, scopeApp) }

    override fun onCreate() {
        super.onCreate()
        val banco = db
        Canais.criar(this)
        scopeApp.launch(Dispatchers.IO) {
            AppDatabase.popularSeVazio(banco)
            val nomeTema = banco.temaSalvo()
            if (nomeTema == "Personalizado") {
                banco.corPersonalizada()?.let { (fundo, principal) ->
                    TemaAtivo.aplicarPersonalizado(Color(fundo.toULong()), Color(principal.toULong()))
                }
            } else {
                TemaAtivo.aplicarPorNome(nomeTema)
            }
            val tags = banco.tagDao().contar()
            val frases = banco.fraseDao().contar()
            val moldes = banco.moldeDao().contar()
            Log.i("Machitto", "Banco pronto -> tags=$tags frases=$frases moldes=$moldes")
            Agendador.reagendarTodos(this@MachittoApp)
            LembreteFinanceiro.reagendarTodos(this@MachittoApp)
            SonoAgendador.reagendar(this@MachittoApp)
            CronogramaAgendador.reagendarTodas(this@MachittoApp)
        }
    }
}
