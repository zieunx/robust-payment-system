package org.example.walletservice.wallet.adapter.out.persistence.entity

import jakarta.persistence.*
import java.math.BigDecimal

@Entity
@Table(name = "wallets")
data class JpaWalletEntity (
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    val id: Long? = null,

    @Column(name = "user_id")
    val userId: Long,

    @Column(name = "balance")
    val balance: BigDecimal,

    @Version // 동시성 방지.
    val version: Int,
) {
    fun addBalance(bigDecimal: BigDecimal): JpaWalletEntity {
        return copy(
            balance = balance,
        )
    }
}