package org.example.ledgerservice.ledger.adapter.`in`.stream

import org.example.ledgerservice.ledger.application.service.DoubleLedgerEntryRecordService
import org.example.ledgerservice.ledger.domain.PaymentEventMessage
import org.example.walletservice.common.StreamAdapter
import org.springframework.cloud.stream.function.StreamBridge
import org.springframework.context.annotation.Bean
import org.springframework.messaging.Message
import java.util.function.Consumer

@StreamAdapter
class PaymentEventMessageHandler(
    private val doubleLedgerEntryRecordService: DoubleLedgerEntryRecordService,
    private val streamBridge: StreamBridge,
) {

    @Bean
    fun consume(): Consumer<Message<PaymentEventMessage>> {
        return Consumer { message ->
            val ledgerEventMessage = doubleLedgerEntryRecordService.recordDoubleLedgerEntry(message.payload)
            streamBridge.send("ledger", ledgerEventMessage)
        }
    }
}