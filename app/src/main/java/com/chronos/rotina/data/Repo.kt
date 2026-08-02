package com.chronos.rotina.data

import android.content.Context
import com.chronos.rotina.MachittoApp

fun Context.appDb(): AppDatabase {
    val app = applicationContext as MachittoApp
    return app.db
}

suspend fun AppDatabase.perfilTemNome(): Boolean {
    val p = perfilDao().obter()
    return p != null && p.nome.isNotBlank()
}

suspend fun AppDatabase.salvarPerfil(nome: String, genero: String) {
    perfilDao().salvar(PerfilEntity(id = 1, nome = nome, genero = genero))
}

suspend fun AppDatabase.escalaNoMes(ano: Int, mes: Int): Set<String> {
    val prefixo = "%04d-%02d-".format(ano, mes)
    val de = prefixo + "01"
    val ate = prefixo + "31"
    return noIntervaloDatas(de, ate).toSet()
}

private suspend fun AppDatabase.noIntervaloDatas(de: String, ate: String): List<String> {
    return escalaDao().noIntervalo(de, ate)
}

suspend fun AppDatabase.salvarEscalaMes(ano: Int, mes: Int, dias: Set<String>) {
    val prefixo = "%04d-%02d-".format(ano, mes)
    val de = prefixo + "01"
    val ate = prefixo + "31"
    escalaDao().limparIntervalo(de, ate)
    for (d in dias) {
        escalaDao().inserir(EscalaEntity(workDate = d))
    }
}

suspend fun AppDatabase.passosOrdenados(): List<PassoEntity> = passoDao().todos()

suspend fun AppDatabase.rotinaVazia(): Boolean = passoDao().contar() == 0

suspend fun AppDatabase.aplicarMolde(chave: String) {
    if (passoDao().contar() > 0) return
    val passosMolde = moldeDao().passosDoMolde(chave)
    val tags = tagDao().todas().associateBy { it.tag }
    val novos = passosMolde.map { mp ->
        val info = tags[mp.tag]
        PassoEntity(
            tag = mp.tag,
            label = info?.label ?: mp.tag,
            emoji = info?.emoji ?: "",
            fireTime = mp.fireTime,
            dayOffset = mp.dayOffset,
            sortOrder = mp.sortOrder,
            ativo = true
        )
    }
    passoDao().inserirVarios(novos)
}

suspend fun AppDatabase.atualizarHorarioPasso(passo: PassoEntity, novoHorario: String) {
    passoDao().atualizar(passo.copy(fireTime = novoHorario))
}

suspend fun AppDatabase.alternarAtivoPasso(passo: PassoEntity) {
    passoDao().atualizar(passo.copy(ativo = !passo.ativo))
}

suspend fun AppDatabase.limparRotina() {
    passoDao().limpar()
}

suspend fun AppDatabase.moldesDisponiveis(): List<MoldeEntity> = moldeDao().ativos()

suspend fun AppDatabase.gerarRotina(r: RespostasRotina) {
    passoDao().limpar()
    val tags = tagDao().todas().associateBy { it.tag }
    val gerados = GeradorRotina.gerar(r, tags)
    val entidades = gerados.map { g ->
        PassoEntity(
            tag = g.tag, label = g.label, emoji = g.emoji,
            fireTime = g.fireTime, dayOffset = g.dayOffset,
            sortOrder = g.sortOrder, ativo = true,
            alarme = g.tag == "acordar",
            prioridade = "normal"
        )
    }
    passoDao().inserirVarios(entidades)
}

suspend fun AppDatabase.tagsParaEscolha(): List<TagEntity> = tagDao().ativas()

suspend fun AppDatabase.adicionarPasso(tag: TagEntity, horario: String) {
    val offset = if (horario >= "17:00") 1 else 0
    val ordem = passoDao().todos().size
    passoDao().inserir(
        PassoEntity(
            tag = tag.tag, label = tag.label, emoji = tag.emoji,
            fireTime = horario, dayOffset = offset, sortOrder = ordem,
            ativo = true, alarme = false, prioridade = "normal"
        )
    )
}

suspend fun AppDatabase.excluirPasso(passo: PassoEntity) {
    passoDao().apagar(passo)
}

suspend fun AppDatabase.alternarAlarme(passo: PassoEntity) {
    passoDao().atualizar(passo.copy(alarme = !passo.alarme))
}

suspend fun AppDatabase.definirPrioridade(passo: PassoEntity, prioridade: String) {
    passoDao().atualizar(passo.copy(prioridade = prioridade))
}

suspend fun AppDatabase.contasAtivas(): List<ContaEntity> = contaDao().ativas()
suspend fun AppDatabase.rendasAtivas(): List<RendaEntity> = rendaDao().ativas()
suspend fun AppDatabase.categoriasTodas(): List<CategoriaEntity> = categoriaDao().todas()

suspend fun AppDatabase.adicionarConta(conta: ContaEntity) { contaDao().inserir(conta) }
suspend fun AppDatabase.editarConta(conta: ContaEntity) { contaDao().atualizar(conta) }
suspend fun AppDatabase.removerConta(conta: ContaEntity) { contaDao().apagar(conta) }

suspend fun AppDatabase.adicionarRenda(renda: RendaEntity) { rendaDao().inserir(renda) }
suspend fun AppDatabase.editarRenda(renda: RendaEntity) { rendaDao().atualizar(renda) }
suspend fun AppDatabase.removerRenda(renda: RendaEntity) { rendaDao().apagar(renda) }

suspend fun AppDatabase.adicionarCategoria(nome: String) {
    if (nome.isBlank()) return
    categoriaDao().inserir(CategoriaEntity(nome = nome.trim()))
}
suspend fun AppDatabase.removerCategoria(cat: CategoriaEntity) { categoriaDao().apagar(cat) }

suspend fun AppDatabase.marcarPaga(conta: ContaEntity, paga: Boolean) {
    contaDao().atualizar(conta.copy(paga = paga))
}

suspend fun AppDatabase.modoPagamento(): String =
    preferenciaDao().obter("modo_pagamento") ?: "vencimento"

suspend fun AppDatabase.definirModoPagamento(modo: String) {
    preferenciaDao().definir(PreferenciaEntity("modo_pagamento", modo))
}

data class ResumoFinanceiro(
    val totalRenda: Double,
    val totalGastos: Double,
    val livre: Double,
    val contasEmAberto: List<ContaEntity>
)

suspend fun AppDatabase.resumoDoMes(diaAtual: Int): ResumoFinanceiro {
    val rendas = rendaDao().ativas()
    val contas = contaDao().ativas()
    val modo = modoPagamento()

    val totalRenda = rendas.sumOf { it.valor }

    val totalGastos = contas.filter { c ->
        if (modo == "pago") !c.paga else true
    }.sumOf { it.valor }

    val emAberto = contas.filter { !it.paga }

    return ResumoFinanceiro(
        totalRenda = totalRenda,
        totalGastos = totalGastos,
        livre = totalRenda - totalGastos,
        contasEmAberto = emAberto
    )
}

suspend fun AppDatabase.temaSalvo(): String =
    preferenciaDao().obter("tema_nome") ?: "Catppuccin"

suspend fun AppDatabase.corPersonalizada(): Pair<Long, Long>? {
    val fundo = preferenciaDao().obter("tema_fundo")?.toLongOrNull()
    val principal = preferenciaDao().obter("tema_principal")?.toLongOrNull()
    return if (fundo != null && principal != null) fundo to principal else null
}

suspend fun AppDatabase.salvarTemaNome(nome: String) {
    preferenciaDao().definir(PreferenciaEntity("tema_nome", nome))
}

suspend fun AppDatabase.salvarTemaPersonalizado(fundo: Long, principal: Long) {
    preferenciaDao().definir(PreferenciaEntity("tema_nome", "Personalizado"))
    preferenciaDao().definir(PreferenciaEntity("tema_fundo", fundo.toString()))
    preferenciaDao().definir(PreferenciaEntity("tema_principal", principal.toString()))
}

suspend fun AppDatabase.todasAsTags(): List<TagEntity> = tagDao().todas()

suspend fun AppDatabase.salvarTag(tag: TagEntity) { tagDao().salvar(tag) }

suspend fun AppDatabase.removerTag(tag: TagEntity) { tagDao().apagar(tag) }

suspend fun AppDatabase.todasAsFrases(): List<FraseEntity> = fraseDao().todas()

suspend fun AppDatabase.salvarFrase(frase: FraseEntity) {
    if (frase.id == 0L) fraseDao().inserirUma(frase) else fraseDao().atualizar(frase)
}

suspend fun AppDatabase.removerFrase(frase: FraseEntity) { fraseDao().apagar(frase) }

suspend fun AppDatabase.categoriasDeFrases(): List<String> =
    fraseDao().todas().map { it.categoria }.distinct().sorted()

data class PadraoEscala(
    val nome: String,
    val diasTrabalho: Int,
    val diasFolga: Int,
    val descricao: String
)

val PadroesEscala = listOf(
    PadraoEscala("12x36", 1, 1, "Plantão: trabalha um dia, folga o outro"),
    PadraoEscala("6x1", 6, 1, "Seis dias de trabalho, um de folga"),
    PadraoEscala("5x1", 5, 1, "Cinco dias de trabalho, um de folga"),
    PadraoEscala("5x2", 5, 2, "Comercial: cinco dias, folga dois"),
    PadraoEscala("4x2", 4, 2, "Quatro dias de trabalho, dois de folga"),
    PadraoEscala("24x48", 1, 2, "Trabalha um dia, folga dois")
)

suspend fun AppDatabase.gerarEscalaPorPadrao(
    padrao: PadraoEscala,
    anoInicio: Int,
    mesInicio: Int,
    diaInicio: Int,
    mesesAdiante: Int
) {
    val ciclo = padrao.diasTrabalho + padrao.diasFolga
    if (ciclo <= 0) return

    val cal = java.util.Calendar.getInstance()
    cal.set(anoInicio, mesInicio - 1, diaInicio, 12, 0, 0)
    cal.set(java.util.Calendar.MILLISECOND, 0)

    val fim = java.util.Calendar.getInstance()
    fim.time = cal.time
    fim.add(java.util.Calendar.MONTH, mesesAdiante)

    val porMes = mutableMapOf<Pair<Int, Int>, MutableSet<String>>()
    var i = 0
    while (cal.timeInMillis < fim.timeInMillis) {
        if (i % ciclo < padrao.diasTrabalho) {
            val a = cal.get(java.util.Calendar.YEAR)
            val m = cal.get(java.util.Calendar.MONTH) + 1
            val d = cal.get(java.util.Calendar.DAY_OF_MONTH)
            val chave = a to m
            porMes.getOrPut(chave) { mutableSetOf() }.add("%04d-%02d-%02d".format(a, m, d))
        }
        cal.add(java.util.Calendar.DAY_OF_YEAR, 1)
        i++
    }

    for ((chave, dias) in porMes) {
        salvarEscalaMes(chave.first, chave.second, dias)
    }
}

suspend fun AppDatabase.limparEscalaAdiante(anoInicio: Int, mesInicio: Int, mesesAdiante: Int) {
    val cal = java.util.Calendar.getInstance()
    cal.set(anoInicio, mesInicio - 1, 1)
    for (k in 0 until mesesAdiante) {
        salvarEscalaMes(cal.get(java.util.Calendar.YEAR), cal.get(java.util.Calendar.MONTH) + 1, emptySet())
        cal.add(java.util.Calendar.MONTH, 1)
    }
}

fun ContaEntity.diaEfetivo(ano: Int, mes: Int): Int =
    if (porDiaUtil) DiasUteis.diaDoMes(ano, mes, diaVencimento) else diaVencimento

fun RendaEntity.diaEfetivo(ano: Int, mes: Int): Int =
    if (porDiaUtil) DiasUteis.diaDoMes(ano, mes, diaPagamento) else diaPagamento

fun ContaEntity.diaEfetivoAtual(): Int {
    val cal = java.util.Calendar.getInstance()
    return diaEfetivo(cal.get(java.util.Calendar.YEAR), cal.get(java.util.Calendar.MONTH) + 1)
}

fun RendaEntity.diaEfetivoAtual(): Int {
    val cal = java.util.Calendar.getInstance()
    return diaEfetivo(cal.get(java.util.Calendar.YEAR), cal.get(java.util.Calendar.MONTH) + 1)
}

fun ContaEntity.descricaoDia(): String =
    if (porDiaUtil) "${diaVencimento}º dia útil" else "dia $diaVencimento"

fun RendaEntity.descricaoDia(): String =
    if (porDiaUtil) "${diaPagamento}º dia útil" else "dia $diaPagamento"

data class ConfigSono(
    val ativo: Boolean,
    val horaAcordar: String,
    val horasSono: Int,
    val minutosAntes: Int
)

suspend fun AppDatabase.configSono(): ConfigSono {
    val ativo = preferenciaDao().obter("sono_ativo") == "1"
    val acordar = preferenciaDao().obter("sono_acordar") ?: "08:00"
    val horas = preferenciaDao().obter("sono_horas")?.toIntOrNull() ?: 8
    val antes = preferenciaDao().obter("sono_antes")?.toIntOrNull() ?: 30
    return ConfigSono(ativo, acordar, horas, antes)
}

suspend fun AppDatabase.salvarConfigSono(config: ConfigSono) {
    preferenciaDao().definir(PreferenciaEntity("sono_ativo", if (config.ativo) "1" else "0"))
    preferenciaDao().definir(PreferenciaEntity("sono_acordar", config.horaAcordar))
    preferenciaDao().definir(PreferenciaEntity("sono_horas", config.horasSono.toString()))
    preferenciaDao().definir(PreferenciaEntity("sono_antes", config.minutosAntes.toString()))
}

fun calcularHoraDormir(horaAcordar: String, horasSono: Int): String {
    val partes = horaAcordar.split(":")
    if (partes.size != 2) return "23:00"
    val h = partes[0].toIntOrNull() ?: 8
    val m = partes[1].toIntOrNull() ?: 0
    var minutos = h * 60 + m - horasSono * 60
    minutos = ((minutos % 1440) + 1440) % 1440
    return "%02d:%02d".format(minutos / 60, minutos % 60)
}

suspend fun AppDatabase.passosDeDormirAtivos(): List<PassoEntity> =
    passoDao().todos().filter { it.ativo && it.tag == "dormir" }

data class NivelInsistencia(
    val chave: String,
    val emoji: String,
    val nome: String,
    val descricao: String,
    val intervaloMin: Int,
    val janelaHoras: Int
)

val NiveisInsistencia = listOf(
    NivelInsistencia("tranquilo", "😌", "Tranquilo", "Um aviso só, 1h antes", 0, 1),
    NivelInsistencia("presente", "🙂", "Presente", "A cada 2h nas últimas 8h", 120, 8),
    NivelInsistencia("cutucador", "😼", "Cutucador", "De hora em hora nas últimas 6h", 60, 6),
    NivelInsistencia("chatissimo", "😾", "Chatíssimo", "A cada 30min nas últimas 4h", 30, 4)
)

fun nivelPorChave(chave: String): NivelInsistencia =
    NiveisInsistencia.firstOrNull { it.chave == chave } ?: NiveisInsistencia[1]

fun horariosDeAviso(prazoMillis: Long, agoraMillis: Long, chaveNivel: String): List<Long> {
    val nivel = nivelPorChave(chaveNivel)
    val res = mutableListOf<Long>()
    val janelaMs = nivel.janelaHoras * 3_600_000L

    if (nivel.intervaloMin == 0) {
        val t = prazoMillis - janelaMs
        if (t > agoraMillis) res.add(t)
    } else {
        var t = prazoMillis - janelaMs
        val passo = nivel.intervaloMin * 60_000L
        while (t < prazoMillis) {
            if (t > agoraMillis) res.add(t)
            t += passo
        }
    }

    if (res.isEmpty() && prazoMillis > agoraMillis + 60_000L) {
        res.add((agoraMillis + prazoMillis) / 2)
    }
    return res
}

suspend fun AppDatabase.tarefasTodas(): List<TarefaEntity> = tarefaDao().todas()
suspend fun AppDatabase.tarefasPendentes(): List<TarefaEntity> = tarefaDao().pendentes()

suspend fun AppDatabase.salvarTarefa(tarefa: TarefaEntity): Long =
    if (tarefa.id == 0L) tarefaDao().inserir(tarefa)
    else { tarefaDao().atualizar(tarefa); tarefa.id }

suspend fun AppDatabase.removerTarefa(tarefa: TarefaEntity) = tarefaDao().apagar(tarefa)

suspend fun AppDatabase.concluirTarefa(tarefa: TarefaEntity, concluida: Boolean) {
    tarefaDao().atualizar(tarefa.copy(concluida = concluida))
}

suspend fun AppDatabase.limparTarefasConcluidas() = tarefaDao().limparConcluidas()
