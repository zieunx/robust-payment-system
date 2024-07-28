package org.example.walletservice.wallet.application.service

import org.example.walletservice.common.UseCase
import org.example.walletservice.wallet.application.port.`in`.SettlementUseCase
import org.example.walletservice.wallet.application.port.out.DuplicateMessageFilterPort
import org.example.walletservice.wallet.application.port.out.LoadPaymentOrderPort
import org.example.walletservice.wallet.application.port.out.LoadWalletPort
import org.example.walletservice.wallet.application.port.out.SaveWalletPort
import org.example.walletservice.wallet.domain.*

@UseCase
class SettlementService(
    private val duplicateMessageFilterPort: DuplicateMessageFilterPort,
    private val loadPaymentOrderPort: LoadPaymentOrderPort,
    private val loadWalletPort: LoadWalletPort,
    private val saveWalletPort: SaveWalletPort,
) : SettlementUseCase {

    override fun processSettlement(paymentEventMessage: PaymentEventMessage): WalletEventMessage {
        if (duplicateMessageFilterPort.isAlreadyProcess(paymentEventMessage)) {
            /**
             * 이미 있음에도 WalletEventMessage 를 발행하는 이유:
             *
             * 정산처리를 완료한 이후에 갑작스럽게 어플리케이션이 크래시 나서 월렛 이벤트 메시지 발행에 실패할 가능성이 있기 때문.
             * 실패 상황을 대비해서 한번 더 발행하는 것.
             * 중복 메시지를 발행할 수는 있지만, 컨슘하는 쪽에서 중복처리를 잘 해놓았다면 문제가 없을것이다.
             */
            return createWalletEventMessage(paymentEventMessage)
        }

        val paymentOrders = loadPaymentOrderPort.getPaymentOrders(paymentEventMessage.orderId())
        val paymentOrdersGroupSellerId = paymentOrders.groupBy { it.sellerId }

        val updatedWallets = getUpdatedWallets(paymentOrdersGroupSellerId)

        saveWalletPort.save(updatedWallets)

        return createWalletEventMessage(paymentEventMessage)
    }

    private fun createWalletEventMessage(paymentEventMessage: PaymentEventMessage) =
        WalletEventMessage(
            type = WalletEventMessageType.SUCCESS,
            payload = mapOf(
                "orderId" to paymentEventMessage.orderId()
            )
        )

    private fun getUpdatedWallets(
        paymentOrdersGroupSellerId: Map<Long, List<PaymentOrder>>,
    ): List<Wallet> {
        val sellerIds = paymentOrdersGroupSellerId.keys

        val wallets = loadWalletPort.getWallets(sellerIds)

        return wallets.map { wallet ->
            wallet.calculateBalanceWith(paymentOrdersGroupSellerId[wallet.userId]!!)
        }
    }
}