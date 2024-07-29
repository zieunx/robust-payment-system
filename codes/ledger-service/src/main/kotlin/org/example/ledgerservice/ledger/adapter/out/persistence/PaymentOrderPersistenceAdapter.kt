package org.example.ledgerservice.ledger.adapter.out.persistence

import org.example.ledgerservice.ledger.adapter.out.persistence.repository.PaymentOrderRepository
import org.example.ledgerservice.ledger.application.port.out.LoadPaymentOrderPort
import org.example.ledgerservice.ledger.domain.PaymentOrder
import org.example.walletservice.common.PersistentAdapter

@PersistentAdapter
class PaymentOrderPersistenceAdapter(
    private val paymentOrderRepository: PaymentOrderRepository,
) : LoadPaymentOrderPort {

    override fun getPaymentOrders(orderId: String): List<PaymentOrder> {
        return paymentOrderRepository.getPaymentOrders(orderId)
    }
}