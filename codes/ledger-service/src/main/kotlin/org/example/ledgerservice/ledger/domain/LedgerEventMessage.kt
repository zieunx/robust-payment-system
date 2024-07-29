package org.example.ledgerservice.ledger.domain

data class LedgerEventMessage(
    val type: LedgerEventMessageType,
    val payload: Map<String, Any?> = emptyMap(),
    val metadata: Map<String, Any?> = emptyMap(),
)

enum class LedgerEventMessageType(val description: String) {
    SUCCESS("정부 기입 성공")
}