package com.chronos.rotina.data

fun tagsSemente(): List<TagEntity> = listOf(
    TagEntity(tag = "dormir", label = "Hora de dormir", emoji = "😴", categoria = "hora_dormir"),
    TagEntity(tag = "acordar", label = "Acordar e levantar", emoji = "⏰", categoria = "hora_acordar"),
    TagEntity(tag = "higiene", label = "Banho e café", emoji = "🚿", categoria = "hora_acordar"),
    TagEntity(tag = "livre", label = "Tempo livre", emoji = "☕", categoria = "generico"),
    TagEntity(tag = "preparar_sair", label = "Preparar para sair", emoji = "🎒", categoria = "fretado"),
    TagEntity(tag = "sair", label = "Sair de casa", emoji = "🚌", categoria = "fretado"),
    TagEntity(tag = "inicio_expediente", label = "Início do expediente", emoji = "💼", categoria = "inicio_expediente"),
    TagEntity(tag = "fim_expediente", label = "Fim do expediente", emoji = "🏁", categoria = "fim_expediente"),
    TagEntity(tag = "voltar", label = "Voltar para casa", emoji = "🏠", categoria = "fim_expediente"),
    TagEntity(tag = "chegada", label = "Chegada, banho e almoço", emoji = "🛁", categoria = "generico"),
    TagEntity(tag = "preparar_dormir", label = "Preparação para dormir", emoji = "🛏️", categoria = "hora_dormir")
)

fun moldesSemente(): List<MoldeEntity> = listOf(
    MoldeEntity(chave = "madrugada", nome = "Madrugada",
        descricao = "Turno que começa de madrugada (ex: 03h). A rotina começa na véspera."),
    MoldeEntity(chave = "diurno", nome = "Diurno",
        descricao = "Turno comercial (ex: 08h às 17h)."),
    MoldeEntity(chave = "noturno", nome = "Noturno",
        descricao = "Turno da noite (ex: 22h às 06h). O expediente termina no dia seguinte.")
)

fun moldePassosSemente(): List<MoldePassoEntity> = listOf(
    // Madrugada — rotina real do Henrique (trabalha 03h)
    MoldePassoEntity(moldeChave = "madrugada", tag = "dormir", fireTime = "17:00", dayOffset = 1, sortOrder = 0),
    MoldePassoEntity(moldeChave = "madrugada", tag = "acordar", fireTime = "23:40", dayOffset = 1, sortOrder = 1),
    MoldePassoEntity(moldeChave = "madrugada", tag = "higiene", fireTime = "00:00", dayOffset = 0, sortOrder = 2),
    MoldePassoEntity(moldeChave = "madrugada", tag = "livre", fireTime = "01:00", dayOffset = 0, sortOrder = 3),
    MoldePassoEntity(moldeChave = "madrugada", tag = "sair", fireTime = "01:20", dayOffset = 0, sortOrder = 4),
    MoldePassoEntity(moldeChave = "madrugada", tag = "inicio_expediente", fireTime = "03:00", dayOffset = 0, sortOrder = 5),
    MoldePassoEntity(moldeChave = "madrugada", tag = "fim_expediente", fireTime = "12:45", dayOffset = 0, sortOrder = 6),
    MoldePassoEntity(moldeChave = "madrugada", tag = "chegada", fireTime = "14:15", dayOffset = 0, sortOrder = 7),
    MoldePassoEntity(moldeChave = "madrugada", tag = "livre", fireTime = "15:00", dayOffset = 0, sortOrder = 8),
    MoldePassoEntity(moldeChave = "madrugada", tag = "preparar_dormir", fireTime = "16:40", dayOffset = 0, sortOrder = 9),

    // Diurno — comercial 08h
    MoldePassoEntity(moldeChave = "diurno", tag = "dormir", fireTime = "23:00", dayOffset = 1, sortOrder = 0),
    MoldePassoEntity(moldeChave = "diurno", tag = "acordar", fireTime = "06:00", dayOffset = 0, sortOrder = 1),
    MoldePassoEntity(moldeChave = "diurno", tag = "higiene", fireTime = "06:20", dayOffset = 0, sortOrder = 2),
    MoldePassoEntity(moldeChave = "diurno", tag = "preparar_sair", fireTime = "06:50", dayOffset = 0, sortOrder = 3),
    MoldePassoEntity(moldeChave = "diurno", tag = "sair", fireTime = "07:10", dayOffset = 0, sortOrder = 4),
    MoldePassoEntity(moldeChave = "diurno", tag = "inicio_expediente", fireTime = "08:00", dayOffset = 0, sortOrder = 5),
    MoldePassoEntity(moldeChave = "diurno", tag = "fim_expediente", fireTime = "17:00", dayOffset = 0, sortOrder = 6),
    MoldePassoEntity(moldeChave = "diurno", tag = "voltar", fireTime = "17:15", dayOffset = 0, sortOrder = 7),
    MoldePassoEntity(moldeChave = "diurno", tag = "chegada", fireTime = "18:00", dayOffset = 0, sortOrder = 8),
    MoldePassoEntity(moldeChave = "diurno", tag = "livre", fireTime = "19:00", dayOffset = 0, sortOrder = 9),

    // Noturno — 22h às 06h (termina no dia seguinte)
    MoldePassoEntity(moldeChave = "noturno", tag = "acordar", fireTime = "15:00", dayOffset = 0, sortOrder = 0),
    MoldePassoEntity(moldeChave = "noturno", tag = "higiene", fireTime = "19:30", dayOffset = 0, sortOrder = 1),
    MoldePassoEntity(moldeChave = "noturno", tag = "preparar_sair", fireTime = "20:30", dayOffset = 0, sortOrder = 2),
    MoldePassoEntity(moldeChave = "noturno", tag = "sair", fireTime = "21:00", dayOffset = 0, sortOrder = 3),
    MoldePassoEntity(moldeChave = "noturno", tag = "inicio_expediente", fireTime = "22:00", dayOffset = 0, sortOrder = 4),
    MoldePassoEntity(moldeChave = "noturno", tag = "fim_expediente", fireTime = "06:00", dayOffset = 1, sortOrder = 5),
    MoldePassoEntity(moldeChave = "noturno", tag = "voltar", fireTime = "06:15", dayOffset = 1, sortOrder = 6),
    MoldePassoEntity(moldeChave = "noturno", tag = "chegada", fireTime = "07:00", dayOffset = 1, sortOrder = 7),
    MoldePassoEntity(moldeChave = "noturno", tag = "dormir", fireTime = "08:00", dayOffset = 1, sortOrder = 8)
)
