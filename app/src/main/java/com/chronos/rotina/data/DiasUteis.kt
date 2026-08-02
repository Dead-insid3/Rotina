package com.chronos.rotina.data

import java.util.Calendar

object DiasUteis {

    private fun d2(n: Int) = if (n < 10) "0$n" else "$n"

    private fun chave(ano: Int, mes: Int, dia: Int) = "$ano-${d2(mes)}-${d2(dia)}"

    private fun pascoa(ano: Int): Triple<Int, Int, Int> {
        val a = ano % 19
        val b = ano / 100
        val c = ano % 100
        val d = b / 4
        val e = b % 4
        val f = (b + 8) / 25
        val g = (b - f + 1) / 3
        val h = (19 * a + b - d - g + 15) % 30
        val i = c / 4
        val k = c % 4
        val l = (32 + 2 * e + 2 * i - h - k) % 7
        val m = (a + 11 * h + 22 * l) / 451
        val mes = (h + l - 7 * m + 114) / 31
        val dia = ((h + l - 7 * m + 114) % 31) + 1
        return Triple(ano, mes, dia)
    }

    private fun somarDias(ano: Int, mes: Int, dia: Int, n: Int): String {
        val cal = Calendar.getInstance()
        cal.set(ano, mes - 1, dia, 12, 0, 0)
        cal.set(Calendar.MILLISECOND, 0)
        cal.add(Calendar.DAY_OF_YEAR, n)
        return chave(
            cal.get(Calendar.YEAR),
            cal.get(Calendar.MONTH) + 1,
            cal.get(Calendar.DAY_OF_MONTH)
        )
    }

    fun feriadosNacionais(ano: Int): Set<String> {
        val fixos = listOf(
            1 to 1,
            4 to 21,
            5 to 1,
            9 to 7,
            10 to 12,
            11 to 2,
            11 to 15,
            11 to 20,
            12 to 25
        ).map { (m, d) -> chave(ano, m, d) }

        val (pa, pm, pd) = pascoa(ano)
        val moveis = listOf(
            somarDias(pa, pm, pd, -48),
            somarDias(pa, pm, pd, -47),
            somarDias(pa, pm, pd, -2),
            somarDias(pa, pm, pd, 60)
        )

        return (fixos + moveis).toSet()
    }

    fun ehDiaUtil(ano: Int, mes: Int, dia: Int, feriados: Set<String>): Boolean {
        val cal = Calendar.getInstance()
        cal.set(ano, mes - 1, dia, 12, 0, 0)
        val dow = cal.get(Calendar.DAY_OF_WEEK)
        if (dow == Calendar.SATURDAY || dow == Calendar.SUNDAY) return false
        return !feriados.contains(chave(ano, mes, dia))
    }

    fun diaDoMes(ano: Int, mes: Int, enesimoDiaUtil: Int): Int {
        val feriados = feriadosNacionais(ano)
        val cal = Calendar.getInstance()
        cal.set(ano, mes - 1, 1)
        val ultimoDia = cal.getActualMaximum(Calendar.DAY_OF_MONTH)

        var contador = 0
        for (dia in 1..ultimoDia) {
            if (ehDiaUtil(ano, mes, dia, feriados)) {
                contador += 1
                if (contador == enesimoDiaUtil) return dia
            }
        }
        return ultimoDia
    }

    fun diaDoMesAtual(enesimoDiaUtil: Int): Int {
        val hoje = Calendar.getInstance()
        return diaDoMes(
            hoje.get(Calendar.YEAR),
            hoje.get(Calendar.MONTH) + 1,
            enesimoDiaUtil
        )
    }
}
