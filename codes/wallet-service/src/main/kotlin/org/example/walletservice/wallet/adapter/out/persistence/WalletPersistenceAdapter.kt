package org.example.walletservice.wallet.adapter.out.persistence

import org.example.walletservice.common.PersistentAdapter
import org.example.walletservice.wallet.adapter.out.persistence.repository.JpaWalletRepository
import org.example.walletservice.wallet.adapter.out.persistence.repository.JpaWalletTransactionRepository
import org.example.walletservice.wallet.application.port.out.DuplicateMessageFilterPort
import org.example.walletservice.wallet.application.port.out.LoadWalletPort
import org.example.walletservice.wallet.application.port.out.SaveWalletPort
import org.example.walletservice.wallet.domain.PaymentEventMessage
import org.example.walletservice.wallet.domain.Wallet

@PersistentAdapter
class WalletPersistenceAdapter(
    private val walletTransactionRepository: JpaWalletTransactionRepository,
    private val walletRepository: JpaWalletRepository,
) : DuplicateMessageFilterPort, LoadWalletPort, SaveWalletPort {

    override fun isAlreadyProcess(paymentEventMessage: PaymentEventMessage): Boolean {
        return walletTransactionRepository.isExist(paymentEventMessage)
    }

    override fun getWallets(sellerIds: Set<Long>): Set<Wallet> {
        return walletRepository.getWallets(sellerIds)
    }

    override fun save(wallets: List<Wallet>) {
        walletRepository.save(wallets)
    }
}