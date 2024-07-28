package org.example.walletservice.wallet.adapter.out.persistence.entity

import jakarta.persistence.*
import java.math.BigDecimal

@Entity
@Table(name = "payment_orders")
class JpaPaymentOrderEntity (
    @Id
    var id: Long? = null,

    @Column(name = "seller_id")
    var sellerId: Long,

    @Column(name = "amount")
    val amount: BigDecimal,

    @Column(name = "order_id")
    val orderId: String,
)