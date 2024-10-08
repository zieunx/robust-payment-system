package org.example.paymentservice.payment.application.service

import org.assertj.core.api.Assertions.assertThat
import org.example.paymentservice.payment.application.port.`in`.CheckoutCommand
import org.example.paymentservice.payment.application.port.`in`.CheckoutUseCase
import org.example.paymentservice.payment.test.PaymentDatabaseHelper
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.assertThrows
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.dao.DataIntegrityViolationException
import reactor.test.StepVerifier
import java.util.UUID
import kotlin.test.Test
import kotlin.test.assertFalse

@SpringBootTest
class CheckoutServiceTest(
    @Autowired private val checkoutUseCase: CheckoutUseCase,
    @Autowired private val paymentDatabaseHelper: PaymentDatabaseHelper,
) {

    @BeforeEach
    fun setup() {
        paymentDatabaseHelper.clean().block()
    }

    @Test
    fun `should save PaymentEvent and PaymentOrder successfully`() {
        val orderId = UUID.randomUUID().toString()
        val checkoutCommand = CheckoutCommand(
            cartId = 1,
            buyerId = 1,
            productIds = setOf(1,2,3),
            idempotencyKey = orderId,
        )

        StepVerifier.create(checkoutUseCase.checkout(checkoutCommand))
            .expectNextMatches {
                it.amount.toInt() == 6000 && it.orderId == orderId
            }
            .verifyComplete()

        val paymentEvent = paymentDatabaseHelper.getPayments(orderId)!!

        assertThat(paymentEvent.orderId).isEqualTo(orderId)
        assertThat(paymentEvent.totalAmount()).isEqualTo(6000)
        assertThat(paymentEvent.paymentOrders.size).isEqualTo(checkoutCommand.productIds.size)
        assertFalse(paymentEvent.isPaymentDone())
        assertThat(paymentEvent.paymentOrders.all { it.isLedgerUpdated() })
        assertThat(paymentEvent.paymentOrders.all { it.isWalletUpdated() })
    }

    @Test
    fun `should fail to save PaymentEvent and PaymentOrder when trying to save second time`() {
        val orderId = UUID.randomUUID().toString()
        val checkoutCommand = CheckoutCommand(
            cartId = 1,
            buyerId = 1,
            productIds = setOf(1,2,3),
            idempotencyKey = orderId,
        )

        checkoutUseCase.checkout(checkoutCommand).block()

        assertThrows<DataIntegrityViolationException> {
            checkoutUseCase.checkout(checkoutCommand).block()
        }
    }
}