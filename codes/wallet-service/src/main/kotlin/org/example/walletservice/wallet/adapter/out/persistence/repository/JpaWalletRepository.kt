package org.example.walletservice.wallet.adapter.out.persistence.repository

import org.example.walletservice.wallet.adapter.out.persistence.entity.JpaWalletEntity
import org.example.walletservice.wallet.adapter.out.persistence.entity.JpaWalletMapper
import org.example.walletservice.wallet.domain.Wallet
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.stereotype.Repository

@Repository
class JpaWalletRepository(
    private val springDataJpaWalletRepository: SpringDataJpaWalletRepository,
    private val jpaWalletMapper: JpaWalletMapper,
    private val walletTransactionRepository: WalletTransactionRepository,
) : WalletRepository {

    override fun getWallet(sellerId: Set<Long>): Set<Wallet> {
        return springDataJpaWalletRepository.findByUserIdIn(sellerId)
            .map { jpaWalletMapper.mapToDomainEntity(it) }
            .toSet()
    }

    override fun save(wallets: List<Wallet>) {
        springDataJpaWalletRepository.saveAll(wallets.map { jpaWalletMapper.mapToJpaEntity(it) })
        walletTransactionRepository.save(wallets.flatMap { it.walletTransaction })
    }
}

interface SpringDataJpaWalletRepository : JpaRepository<JpaWalletEntity, Long> {

    fun findByUserIdIn(userIDs: Set<Long>) : List<JpaWalletEntity>
}