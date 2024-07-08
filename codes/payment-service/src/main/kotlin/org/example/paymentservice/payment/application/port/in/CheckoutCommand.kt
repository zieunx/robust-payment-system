package org.example.paymentservice.payment.application.port.`in`

data class CheckoutCommand(
    val cartId: Long,
    val buyerId: Long,
    val productIds: Set<Long>,
    val idempotencyKey: String,
)
