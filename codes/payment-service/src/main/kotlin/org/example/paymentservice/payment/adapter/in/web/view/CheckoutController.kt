package org.example.paymentservice.payment.adapter.`in`.web.view

import org.example.paymentservice.common.IdempotencyCreator
import org.example.paymentservice.common.WebAdapter
import org.example.paymentservice.payment.adapter.`in`.web.request.CheckoutRequest
import org.example.paymentservice.payment.application.port.`in`.CheckoutCommand
import org.example.paymentservice.payment.application.port.`in`.CheckoutUseCase
import org.springframework.stereotype.Controller
import org.springframework.ui.Model
import org.springframework.web.bind.annotation.GetMapping
import reactor.core.publisher.Mono

@WebAdapter
@Controller
class CheckoutController(
    private val checkoutUseCase: CheckoutUseCase,
) {

    @GetMapping
    fun checkoutPage(request: CheckoutRequest, model: Model): Mono<String> {
        val command = CheckoutCommand(
            cartId = request.cartId,
            buyerId = request.buyerId,
            productIds = request.productId.toSet(),
            idempotencyKey = IdempotencyCreator.create(request.seed), // 개발편의상 seed를 넣었지만 제대로 하려면 요청데이터 그 자체로 해야함.
        )
        return checkoutUseCase.checkout(command)
            .map {
                model.addAttribute("orderId", it.orderId)
                model.addAttribute("orderName", it.orderName)
                model.addAttribute("amount", it.amount)
                "checkout"
            }
    }
}