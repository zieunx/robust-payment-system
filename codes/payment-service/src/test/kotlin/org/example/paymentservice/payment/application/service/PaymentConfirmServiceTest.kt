package org.example.paymentservice.payment.application.service

import io.mockk.every
import io.mockk.mockk
import org.assertj.core.api.Assertions.assertThat
import org.example.paymentservice.payment.adapter.out.web.toss.response.PSPConfirmationStatus
import org.example.paymentservice.payment.application.port.`in`.CheckoutCommand
import org.example.paymentservice.payment.application.port.`in`.CheckoutUseCase
import org.example.paymentservice.payment.application.port.`in`.PaymentConfirmCommand
import org.example.paymentservice.payment.application.port.out.PaymentExecutorPort
import org.example.paymentservice.payment.application.port.out.PaymentStatusUpdatePort
import org.example.paymentservice.payment.application.port.out.PaymentValidationPort
import org.example.paymentservice.payment.domain.*
import org.example.paymentservice.payment.test.PaymentDatabaseHelper
import org.example.paymentservice.payment.test.PaymentTestConfiguration
import org.junit.jupiter.api.BeforeEach
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.context.annotation.Import
import reactor.core.publisher.Mono
import java.time.LocalDateTime
import java.time.temporal.ChronoUnit
import java.util.UUID
import kotlin.test.Test
import kotlin.test.assertTrue

@Import(PaymentTestConfiguration::class)
@SpringBootTest
class PaymentConfirmServiceTest(
    @Autowired private val checkoutUseCase: CheckoutUseCase,
    @Autowired private val paymentStatusUpdatePort: PaymentStatusUpdatePort,
    @Autowired private val paymentValidationPort: PaymentValidationPort,
    @Autowired private val paymentDatabaseHelper: PaymentDatabaseHelper,
) {

    private val mockPaymentExecutoerPort = mockk<PaymentExecutorPort>()

    @BeforeEach
    fun setup() {
        paymentDatabaseHelper.clean().block()
    }

    @Test
    fun `sould be mocked as SUCCESS if Payment Confirmation success is PSP`() {
        val orderId = UUID.randomUUID().toString()

        val ckeckoutCommand = CheckoutCommand(
            cartId = 1,
            buyerId = 1,
            productIds = setOf(1, 2, 3),
            idempotencyKey = orderId,
        )

        val checkoutResult = checkoutUseCase.checkout(ckeckoutCommand).block()!!

        val paymentConfirmCommand = PaymentConfirmCommand(
            paymentKey = UUID.randomUUID().toString(),
            orderId = orderId,
            amount = checkoutResult.amount,
        )

        val paymentConfirmService = PaymentConfirmService(
            paymentStatusUpdatePort = paymentStatusUpdatePort,
            paymentValidationPort = paymentValidationPort,
            paymentExecutorPort = mockPaymentExecutoerPort,
        )

        val paymentExecutionResult = PaymentExecutionResult(
            paymentKey = paymentConfirmCommand.paymentKey,
            orderId = paymentConfirmCommand.orderId,
            extraDetails = PaymentExtraDetails(
                type = PaymentType.NORMAL,
                method = PaymentMethod.EASY_PAY,
                totalAmount = paymentConfirmCommand.amount,
                orderName = "test_order_name",
                pspConfirmationStatus = PSPConfirmationStatus.DONE,
                approvalAt = LocalDateTime.now(),
                pspRawData = "{}"
            ),
            isSuccess = true,
            isRetryable = false,
            isUnknown = false,
            isFailure = false,
        )

        every { mockPaymentExecutoerPort.execution(paymentConfirmCommand) } returns Mono.just(paymentExecutionResult)

        val paymentConfirmationResult = paymentConfirmService.confirm(paymentConfirmCommand).block()!!

        val paymentEvent = paymentDatabaseHelper.getPayments(orderId)!!

        assertThat(paymentConfirmationResult.status).isEqualTo(PaymentStatus.SUCCESS)
        assertTrue(paymentEvent.paymentOrders.all { it.paymentStatus == PaymentStatus.SUCCESS })
        assertThat(paymentEvent.paymentType).isEqualTo(paymentExecutionResult.extraDetails!!.type)
        assertThat(paymentEvent.paymentMethod).isEqualTo(paymentExecutionResult.extraDetails!!.method)
        assertThat(paymentEvent.approvedAt!!.truncatedTo(ChronoUnit.MINUTES)).isEqualTo(paymentExecutionResult.extraDetails!!.approvalAt.truncatedTo(ChronoUnit.MINUTES))
    }

    @Test
    fun `sould be mocked as FAILURE if Payment Confirmation fails on PSP`() {
        val orderId = UUID.randomUUID().toString()

        val ckeckoutCommand = CheckoutCommand(
            cartId = 1,
            buyerId = 1,
            productIds = setOf(1, 2, 3),
            idempotencyKey = orderId,
        )

        val checkoutResult = checkoutUseCase.checkout(ckeckoutCommand).block()!!

        val paymentConfirmCommand = PaymentConfirmCommand(
            paymentKey = UUID.randomUUID().toString(),
            orderId = orderId,
            amount = checkoutResult.amount,
        )

        val paymentConfirmService = PaymentConfirmService(
            paymentStatusUpdatePort = paymentStatusUpdatePort,
            paymentValidationPort = paymentValidationPort,
            paymentExecutorPort = mockPaymentExecutoerPort,
        )

        val paymentExecutionResult = PaymentExecutionResult(
            paymentKey = paymentConfirmCommand.paymentKey,
            orderId = paymentConfirmCommand.orderId,
            extraDetails = PaymentExtraDetails(
                type = PaymentType.NORMAL,
                method = PaymentMethod.EASY_PAY,
                totalAmount = paymentConfirmCommand.amount,
                orderName = "test_order_name",
                pspConfirmationStatus = PSPConfirmationStatus.DONE,
                approvalAt = LocalDateTime.now(),
                pspRawData = "{}"
            ),
            failure = PaymentExecutionFailure(
                errorCode = "ERROR",
                message = "error.",
            ),
            isSuccess = false,
            isRetryable = false,
            isUnknown = false,
            isFailure = true,
        )

        every { mockPaymentExecutoerPort.execution(paymentConfirmCommand) } returns Mono.just(paymentExecutionResult)

        val paymentConfirmationResult = paymentConfirmService.confirm(paymentConfirmCommand).block()!!

        val paymentEvent = paymentDatabaseHelper.getPayments(orderId)!!

        assertThat(paymentConfirmationResult.status).isEqualTo(PaymentStatus.FAILURE)
        assertTrue(paymentEvent.paymentOrders.all { it.paymentStatus == PaymentStatus.FAILURE })
    }

    @Test
    fun `sould be mocked as UNKNOWN if Payment Confirmation UNKNOWN on PSP`() {
        val orderId = UUID.randomUUID().toString()

        val ckeckoutCommand = CheckoutCommand(
            cartId = 1,
            buyerId = 1,
            productIds = setOf(1, 2, 3),
            idempotencyKey = orderId,
        )

        val checkoutResult = checkoutUseCase.checkout(ckeckoutCommand).block()!!

        val paymentConfirmCommand = PaymentConfirmCommand(
            paymentKey = UUID.randomUUID().toString(),
            orderId = orderId,
            amount = checkoutResult.amount,
        )

        val paymentConfirmService = PaymentConfirmService(
            paymentStatusUpdatePort = paymentStatusUpdatePort,
            paymentValidationPort = paymentValidationPort,
            paymentExecutorPort = mockPaymentExecutoerPort,
        )

        val paymentExecutionResult = PaymentExecutionResult(
            paymentKey = paymentConfirmCommand.paymentKey,
            orderId = paymentConfirmCommand.orderId,
            extraDetails = PaymentExtraDetails(
                type = PaymentType.NORMAL,
                method = PaymentMethod.EASY_PAY,
                totalAmount = paymentConfirmCommand.amount,
                orderName = "test_order_name",
                pspConfirmationStatus = PSPConfirmationStatus.DONE,
                approvalAt = LocalDateTime.now(),
                pspRawData = "{}"
            ),
            isSuccess = false,
            isRetryable = false,
            isUnknown = true,
            isFailure = false,
        )

        every { mockPaymentExecutoerPort.execution(paymentConfirmCommand) } returns Mono.just(paymentExecutionResult)

        val paymentConfirmationResult = paymentConfirmService.confirm(paymentConfirmCommand).block()!!

        val paymentEvent = paymentDatabaseHelper.getPayments(orderId)!!

        assertThat(paymentConfirmationResult.status).isEqualTo(PaymentStatus.UNKNOWN)
        assertTrue(paymentEvent.paymentOrders.all { it.paymentStatus == PaymentStatus.UNKNOWN })
    }
}