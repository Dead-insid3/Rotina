package com.chronos.rotina.data

data class PassoGerado(
    val tag: String,
    val label: String,
    val emoji: String,
    val fireTime: String,
    val dayOffset: Int,
    val sortOrder: Int
)

data class RespostasRotina(
    val entrada: String,
    val saida: String,
    val deslocMin: Int,
    val preparoMin: Int,
    val sonoMin: Int,
    val folgaMin: Int = 10
)

object GeradorRotina {

    private fun toMin(hhmm: String): Int {
        val p = hhmm.split(":")
        return p[0].toInt() * 60 + p[1].toInt()
    }

    private fun hm(minAbs: Int): String {
        val m = ((minAbs % 1440) + 1440) % 1440
        return "%02d:%02d".format(m / 60, m % 60)
    }

    private fun offsetDe(min: Int): Int = if (min < 0) 1 else 0

    fun gerar(r: RespostasRotina, tags: Map<String, TagEntity>): List<PassoGerado> {
        val ent = toMin(r.entrada)
        val sai = toMin(r.saida)
        val sairCasa = ent - r.deslocMin - r.folgaMin
        val acordar = sairCasa - r.preparoMin
        val dormir = acordar - r.sonoMin

        data class Base(val tag: String, val min: Int)
        val bases = listOf(
            Base("dormir", dormir),
            Base("acordar", acordar),
            Base("higiene", acordar + 10),
            Base("sair", sairCasa),
            Base("inicio_expediente", ent),
            Base("fim_expediente", sai)
        )

        return bases.mapIndexed { i, b ->
            val info = tags[b.tag]
            PassoGerado(
                tag = b.tag,
                label = info?.label ?: b.tag,
                emoji = info?.emoji ?: "",
                fireTime = hm(b.min),
                dayOffset = offsetDe(b.min),
                sortOrder = i
            )
        }
    }
}
