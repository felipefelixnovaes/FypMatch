package com.ideiassertiva.FypMatch.models

import java.util.UUID

data class Report(
    val reportId: String = UUID.randomUUID().toString(),
    val reporterUserId: String,
    val reportedUserId: String,
    val reason: ReportReason,
    val description: String = "",
    val timestamp: Long = System.currentTimeMillis(),
    val status: ReportStatus = ReportStatus.PENDING
)

enum class ReportReason(val displayName: String) {
    HARASSMENT("Assedio ou intimidacao"),
    INAPPROPRIATE_CONTENT("Conteudo inapropriado"),
    FAKE_PROFILE("Perfil falso"),
    UNDERAGE("Menor de idade"),
    SPAM("Spam ou propaganda"),
    OTHER("Outro motivo")
}

enum class ReportStatus(val displayName: String) {
    PENDING("Pendente"),
    REVIEWED("Em revisao"),
    RESOLVED("Resolvido")
}