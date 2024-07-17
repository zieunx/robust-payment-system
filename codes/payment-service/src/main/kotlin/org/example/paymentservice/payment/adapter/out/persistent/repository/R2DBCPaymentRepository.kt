package org.example.paymentservice.payment.adapter.out.persistent.repository

import org.example.paymentservice.payment.adapter.out.persistent.util.MySQLDateTimeFormatter
import org.example.paymentservice.payment.domain.PaymentEvent
import org.example.paymentservice.payment.domain.PaymentStatus
import org.example.paymentservice.payment.domain.PendingPaymentEvent
import org.example.paymentservice.payment.domain.PendingPaymentOrder
import org.springframework.r2dbc.core.DatabaseClient
import org.springframework.stereotype.Repository
import org.springframework.transaction.reactive.TransactionalOperator
import reactor.core.publisher.Flux
import reactor.core.publisher.Mono
import java.math.BigDecimal
import java.math.BigInteger
import java.time.LocalDateTime

@Repository
class R2DBCPaymentRepository(
    private val databaseClient: DatabaseClient,
    private val transactionalOperator: TransactionalOperator,
) : PaymentRepository {
    override fun save(paymentEvent: PaymentEvent): Mono<Void> {
        return insertPaymentEvent(paymentEvent)
            .flatMap { selectPaymentEventId() }
            .flatMap { paymentEventId -> insertPaymentOrders(paymentEvent, paymentEventId) }
            .`as`(transactionalOperator::transactional)
            .then()
    }

    override fun getPendingPayments(): Flux<PendingPaymentEvent> {
        return databaseClient.sql(SELECT_PENDING_PAYMENT_QUERY)
            .bind("updatedAt", LocalDateTime.now().format(MySQLDateTimeFormatter))
            .fetch()
            .all()
            .groupBy { it["payment_event_id"] as Long }
            .flatMap { groupedFlux ->
                groupedFlux.collectList().map { results ->
                    PendingPaymentEvent(
                        paymentEventId = groupedFlux.key(),
                        paymentKey = results.first()["payment_key"] as String,
                        orderId = results.first()["order_id"] as String,
                        pendingPaymentOrders = results.map {
                            PendingPaymentOrder(
                                paymentOrderId = it["payment_order_id"] as Long,
                                status = PaymentStatus.get(it["payment_order_status"] as String),
                                amount = (it["amount"] as BigDecimal).toLong(),
                                failedCount = it["failed_count"] as Byte,
                                threshold = it["threshold"] as Byte
                            )
                        }
                    )
                }
            }
    }

    private fun insertPaymentOrders(
        paymentEvent: PaymentEvent,
        paymentEventId: Long?
    ): Mono<Long> {
        val values = paymentEvent.paymentOrders.joinToString(", ") { paymentOrder ->
            "($paymentEventId, ${paymentOrder.sellerId}, '${paymentOrder.orderId}', ${paymentOrder.productId}, ${paymentOrder.amount}, '${paymentOrder.paymentStatus}')"
        }
        return databaseClient.sql(INSERT_PAYMENT_ORDER_QUERY(values))
            .fetch()
            .rowsUpdated()
    }

    private fun selectPaymentEventId() = databaseClient.sql(LAST_INSERT_ID_QUERY)
        .fetch()
        .first()
        .map { (it["LAST_INSERT_ID()"] as BigInteger).toLong() }

    private fun insertPaymentEvent(paymentEvent: PaymentEvent): Mono<Long> {
        return databaseClient.sql(INSERT_PAYMENT_EVENT_QUERY)
            .bind("buyer_id", paymentEvent.buyerId)
            .bind("order_name", paymentEvent.orderName)
            .bind("order_id", paymentEvent.orderId)
            .fetch()
            .rowsUpdated()
    }

    companion object {
        val INSERT_PAYMENT_EVENT_QUERY = """
            INSERT INTO payment_events (buyer_id, order_name, order_id)
            VALUES (:buyer_id, :order_name, :order_id)
        """.trimIndent()

        val LAST_INSERT_ID_QUERY = """
            SELECT LAST_INSERT_ID()
        """.trimIndent()

        val INSERT_PAYMENT_ORDER_QUERY = fun (valueClauses: String) = """
            INSERT INTO payment_orders (payment_event_id, seller_id, order_id, product_id, amount, payment_order_status)
            VALUES $valueClauses
        """.trimIndent()

        val SELECT_PENDING_PAYMENT_QUERY = """
            SELECT
                pe.id as payment_event_id,
                 pe.payment_key,
                 pe.order_id,
                 po.id as payment_order_id,
                 po.payment_order_status,
                 po.amount,
                 po.failed_count,
                 po.threshold
            FROM payment_events pe
            INNER JOIN payment_orders po ON pe.id = po.payment_event_id
            WHERE (po.payment_order_status = 'UNKNOWN' OR (po.payment_order_status = 'EXECUTING' AND po.updated_at <= :updatedAt - INTERVAL 3 MINUTE ))
            AND po.failed_count < po.threshold
            LIMIT 10
        """.trimIndent()
    }
}