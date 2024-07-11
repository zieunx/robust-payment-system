package org.example.paymentservice.payment.adapter.out.web.toss.executor

import org.example.paymentservice.payment.adapter.out.web.toss.response.PSPConfirmationStatus
import org.example.paymentservice.payment.adapter.out.web.toss.response.TossPaymentConfirmResponse
import org.example.paymentservice.payment.application.port.`in`.PaymentConfirmCommand
import org.example.paymentservice.payment.domain.PaymentExecutionResult
import org.example.paymentservice.payment.domain.PaymentExtraDetails
import org.example.paymentservice.payment.domain.PaymentMethod
import org.example.paymentservice.payment.domain.PaymentType
import org.springframework.stereotype.Component
import org.springframework.web.reactive.function.client.WebClient
import reactor.core.publisher.Mono
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter

@Component
class TossPaymentExecutor(
    private val tossPaymentWebClient: WebClient,
    private val uri: String = "/v1/payments/confirm",
) : PaymentExecutor {

    override fun execute(command: PaymentConfirmCommand): Mono<PaymentExecutionResult> {
        return tossPaymentWebClient.post()
            .uri(uri)
            .header("Idempotency-Key", command.orderId)
            .bodyValue("""
            {
                "paymentKey": "${command.paymentKey}",
                "orderId": "${command.orderId}",
                "amount": "${command.amount}"
            }
            """.trimIndent())
            .retrieve()
            .bodyToMono(TossPaymentConfirmResponse::class.java)
            .map {
                PaymentExecutionResult(
                    paymentKey = command.paymentKey,
                    orderId = command.orderId,
                    extraDetails = PaymentExtraDetails(
                        type = PaymentType.get(it.type),
                        method = PaymentMethod.get(it.method),
                        approvalAt = LocalDateTime.parse(it.approvedAt, DateTimeFormatter.ISO_OFFSET_DATE_TIME),
                        orderName = it.orderName,
                        pspConfirmationStatus = PSPConfirmationStatus.get(it.status),
                        totalAmount = it.totalAmount.toLong(),
                        pspRawData = it.toString(),
                    ),
                    isSuccess = true,
                    isFailure = false,
                    isUnknown = false,
                    isRetryable = false,
                )
            }
    }
}