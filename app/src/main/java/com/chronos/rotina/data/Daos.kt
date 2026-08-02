package com.chronos.rotina.data

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update

@Dao
interface PerfilDao {
    @Query("SELECT * FROM perfil WHERE id = 1")
    suspend fun obter(): PerfilEntity?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun salvar(perfil: PerfilEntity)
}

@Dao
interface PreferenciaDao {
    @Query("SELECT valor FROM preferencias WHERE chave = :chave")
    suspend fun obter(chave: String): String?

    @Query("SELECT * FROM preferencias")
    suspend fun todas(): List<PreferenciaEntity>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun definir(pref: PreferenciaEntity)
}

@Dao
interface TagDao {
    @Query("SELECT * FROM tags WHERE ativa = 1 ORDER BY tag")
    suspend fun ativas(): List<TagEntity>

    @Query("SELECT * FROM tags ORDER BY tag")
    suspend fun todas(): List<TagEntity>

    @Query("SELECT * FROM tags WHERE tag = :tag")
    suspend fun obter(tag: String): TagEntity?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun inserir(tags: List<TagEntity>)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun salvar(tag: TagEntity)

    @Delete
    suspend fun apagar(tag: TagEntity)

    @Query("SELECT COUNT(*) FROM tags")
    suspend fun contar(): Int
}

@Dao
interface MoldeDao {
    @Query("SELECT * FROM moldes WHERE ativo = 1")
    suspend fun ativos(): List<MoldeEntity>

    @Query("SELECT * FROM molde_passos WHERE moldeChave = :chave ORDER BY sortOrder")
    suspend fun passosDoMolde(chave: String): List<MoldePassoEntity>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun inserirMoldes(moldes: List<MoldeEntity>)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun inserirPassos(passos: List<MoldePassoEntity>)

    @Query("SELECT COUNT(*) FROM moldes")
    suspend fun contar(): Int
}

@Dao
interface PassoDao {
    @Query("SELECT * FROM passos ORDER BY sortOrder, fireTime")
    suspend fun todos(): List<PassoEntity>

    @Query("SELECT * FROM passos WHERE ativo = 1 ORDER BY sortOrder, fireTime")
    suspend fun ativos(): List<PassoEntity>

    @Insert
    suspend fun inserir(passo: PassoEntity): Long

    @Insert
    suspend fun inserirVarios(passos: List<PassoEntity>)

    @Update
    suspend fun atualizar(passo: PassoEntity)

    @Delete
    suspend fun apagar(passo: PassoEntity)

    @Query("DELETE FROM passos")
    suspend fun limpar()

    @Query("SELECT COUNT(*) FROM passos")
    suspend fun contar(): Int
}

@Dao
interface FraseDao {
    @Query("SELECT * FROM frases WHERE categoria = :categoria AND ativa = 1")
    suspend fun daCategoria(categoria: String): List<FraseEntity>

    @Query("SELECT * FROM frases ORDER BY categoria")
    suspend fun todas(): List<FraseEntity>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun inserir(frases: List<FraseEntity>)

    @Update
    suspend fun atualizar(frase: FraseEntity)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun inserirUma(frase: FraseEntity): Long

    @Delete
    suspend fun apagar(frase: FraseEntity)

    @Query("SELECT COUNT(*) FROM frases")
    suspend fun contar(): Int
}

@Dao
interface EscalaDao {
    @Query("SELECT * FROM escala ORDER BY workDate")
    suspend fun todas(): List<EscalaEntity>

    @Query("SELECT * FROM escala WHERE workDate = :data")
    suspend fun obter(data: String): EscalaEntity?

    @Query("SELECT COUNT(*) FROM escala WHERE workDate = :data")
    suspend fun ehDiaDeTrabalho(data: String): Int

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun inserir(dia: EscalaEntity)

    @Query("DELETE FROM escala WHERE workDate = :data")
    suspend fun apagar(data: String)

    @Query("DELETE FROM escala WHERE workDate BETWEEN :de AND :ate")
    suspend fun limparIntervalo(de: String, ate: String)

    @Query("SELECT workDate FROM escala WHERE workDate BETWEEN :de AND :ate ORDER BY workDate")
    suspend fun noIntervalo(de: String, ate: String): List<String>
}

@Dao
interface CategoriaDao {
    @Query("SELECT * FROM categorias ORDER BY nome")
    suspend fun todas(): List<CategoriaEntity>

    @Insert
    suspend fun inserir(categoria: CategoriaEntity): Long

    @Insert(onConflict = OnConflictStrategy.IGNORE)
    suspend fun inserirVarias(categorias: List<CategoriaEntity>)

    @Delete
    suspend fun apagar(categoria: CategoriaEntity)
}

@Dao
interface ContaDao {
    @Query("SELECT * FROM contas ORDER BY diaVencimento")
    suspend fun todas(): List<ContaEntity>

    @Query("SELECT * FROM contas WHERE ativa = 1 ORDER BY diaVencimento")
    suspend fun ativas(): List<ContaEntity>

    @Insert
    suspend fun inserir(conta: ContaEntity): Long

    @Update
    suspend fun atualizar(conta: ContaEntity)

    @Delete
    suspend fun apagar(conta: ContaEntity)
}

@Dao
interface RendaDao {
    @Query("SELECT * FROM rendas ORDER BY diaPagamento")
    suspend fun todas(): List<RendaEntity>

    @Query("SELECT * FROM rendas WHERE ativa = 1 ORDER BY diaPagamento")
    suspend fun ativas(): List<RendaEntity>

    @Insert
    suspend fun inserir(renda: RendaEntity): Long

    @Update
    suspend fun atualizar(renda: RendaEntity)

    @Delete
    suspend fun apagar(renda: RendaEntity)
}

@Dao
interface TarefaDao {
    @Query("SELECT * FROM tarefas ORDER BY concluida ASC, prazoMillis ASC")
    suspend fun todas(): List<TarefaEntity>

    @Query("SELECT * FROM tarefas WHERE concluida = 0 ORDER BY prazoMillis ASC")
    suspend fun pendentes(): List<TarefaEntity>

    @Query("SELECT * FROM tarefas WHERE id = :id LIMIT 1")
    suspend fun porId(id: Long): TarefaEntity?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun inserir(tarefa: TarefaEntity): Long

    @Update
    suspend fun atualizar(tarefa: TarefaEntity)

    @Delete
    suspend fun apagar(tarefa: TarefaEntity)

    @Query("DELETE FROM tarefas WHERE concluida = 1")
    suspend fun limparConcluidas()
}
