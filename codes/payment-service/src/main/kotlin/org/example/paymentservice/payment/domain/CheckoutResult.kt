package org.example.paymentservice.payment.domain

import java.math.BigDecimal

data class CheckoutResult(
    val amount: Long,
    val orderId: String,
    val orderName: String,
)
