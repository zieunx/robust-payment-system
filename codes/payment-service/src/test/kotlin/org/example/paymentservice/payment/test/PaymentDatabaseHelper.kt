package org.example.paymentservice.payment.test

import org.example.paymentservice.payment.domain.PaymentEvent

interface PaymentDatabaseHelper {

    fun getPayments(orderId: String): PaymentEvent?
}