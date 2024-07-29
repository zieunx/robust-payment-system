package org.example.ledgerservice.ledger.adapter.out.persistence

import org.example.ledgerservice.ledger.adapter.out.persistence.repository.AccountRepository
import org.example.ledgerservice.ledger.application.port.out.LoadAccountPort
import org.example.ledgerservice.ledger.domain.DoubleAccountsForLedger
import org.example.ledgerservice.ledger.domain.FinanceType
import org.example.walletservice.common.PersistentAdapter

@PersistentAdapter
class AccountPersistenceAdapter(
    private val accountRepository: AccountRepository,
) : LoadAccountPort {

    override fun getDoubleAccountsForLedger(financeType: FinanceType): DoubleAccountsForLedger {
        return accountRepository.getDoubleAccountsForLedger(financeType)
    }
}