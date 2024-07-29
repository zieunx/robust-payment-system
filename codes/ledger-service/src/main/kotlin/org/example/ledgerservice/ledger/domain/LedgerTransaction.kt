package org.example.ledgerservice.ledger.domain

// 어떤 외부 도메인과 관련됐는지 설명하는 트랜잭션 도메인
data class LedgerTransaction (
  val referenceType: ReferenceType,
  val referenceId: Long,
  val orderId: String
)
