package org.example.paymentservice.payment.domain

enum class PaymentStatus(description: String) {
    NOT_STARTED("결제 승인 시작 전"),
    EXECUTING("결제 승인 중"),
    SUCCESS("결제 승인 성공"),
    FAILURE("결제 승인 실패"),
    UNKNOWN("결제 승인 알수 없음 상태"),;

    companion object {
        fun get(value: String): PaymentStatus {
            return entries.find { it.name == value } ?: throw IllegalArgumentException("올바르지 않은 값입니다. $value")
        }
    }
}