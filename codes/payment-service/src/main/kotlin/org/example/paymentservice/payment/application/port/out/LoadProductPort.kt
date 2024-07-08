package org.example.paymentservice.payment.application.port.out

import org.example.paymentservice.payment.domain.Product
import reactor.core.publisher.Flux

interface LoadProductPort {
    fun getProducts(cardId: Long, productIds: List<Long>): Flux<Product>
}
