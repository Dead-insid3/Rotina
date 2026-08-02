package com.chronos.rotina.data

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "perfil")
data class PerfilEntity(
    @PrimaryKey val id: Int = 1,
    val nome: String = "",
    val genero: String = "nb"
)

@Entity(tableName = "preferencias")
data class PreferenciaEntity(
    @PrimaryKey val chave: String,
    val valor: String
)

@Entity(tableName = "tags")
data class TagEntity(
    @PrimaryKey val tag: String,
    val label: String,
    val emoji: String,
    val categoria: String,
    val padrao: Boolean = true,
    val ativa: Boolean = true
)

@Entity(tableName = "moldes")
data class MoldeEntity(
    @PrimaryKey val chave: String,
    val nome: String,
    val descricao: String,
    val ativo: Boolean = true
)

@Entity(tableName = "molde_passos")
data class MoldePassoEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val moldeChave: String,
    val tag: String,
    val fireTime: String,
    val dayOffset: Int,
    val sortOrder: Int
)

@Entity(tableName = "passos")
data class PassoEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val tag: String,
    val label: String,
    val emoji: String,
    val fireTime: String,
    val dayOffset: Int,
    val sortOrder: Int,
    val ativo: Boolean = true,
    val alarme: Boolean = false,
    val prioridade: String = "normal"
)

@Entity(tableName = "frases")
data class FraseEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val categoria: String,
    val textoM: String,
    val textoF: String,
    val textoN: String,
    val padrao: Boolean = true,
    val ativa: Boolean = true
)

@Entity(tableName = "escala")
data class EscalaEntity(
    @PrimaryKey val workDate: String,
    val nota: String? = null
)

@Entity(tableName = "categorias")
data class CategoriaEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val nome: String
)

@Entity(tableName = "contas")
data class ContaEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val label: String,
    val valor: Double,
    val diaVencimento: Int,
    val tipo: String,
    val categoria: String = "",
    val parcelaAtual: Int = 1,
    val parcelaTotal: Int = 1,
    val paga: Boolean = false,
    val ativa: Boolean = true,
    val porDiaUtil: Boolean = false
)

@Entity(tableName = "rendas")
data class RendaEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val label: String,
    val valor: Double,
    val diaPagamento: Int,
    val tipo: String,
    val ativa: Boolean = true,
    val porDiaUtil: Boolean = false
)

@Entity(tableName = "tarefas")
data class TarefaEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val titulo: String,
    val detalhe: String = "",
    val prazoMillis: Long,
    val insistencia: String = "presente",
    val concluida: Boolean = false,
    val criadaEm: Long = System.currentTimeMillis()
)
