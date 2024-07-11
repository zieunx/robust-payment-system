package org.example.paymentservice.payment.application.port.out

import reactor.core.publisher.Mono

interface PaymentStatusUpdatePort {

    fun updatePaymentStatusExecuting(orderId: String, paymentKey: String): Mono<Boolean>

    fun updatePaymentStatus(command: PaymentStatusUpdateCommand): Mono<Boolean>
}
