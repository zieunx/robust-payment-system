package org.example.ledgerservice.ledger.domain

data class DoubleLedgerEntry (
  val credit: LedgerEntry, // 입금된 쪽
  val debit: LedgerEntry, // 출금된 쪽
  val transaction: LedgerTransaction
) {
  init {
    // 출금금액과 입금금액은 같아야한다
    require(credit.amount == debit.amount) {
      "a double ledger entry require that the amounts for both the credit and debit are same."
    }
  }
}
