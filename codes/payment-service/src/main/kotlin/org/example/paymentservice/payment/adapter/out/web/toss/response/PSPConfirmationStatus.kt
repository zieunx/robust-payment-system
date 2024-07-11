package org.example.paymentservice.payment.adapter.out.web.toss.response

enum class PSPConfirmationStatus(val description: String) {
    READY("결제를 생성한 초기 상태. 인증 전"),
    IN_PROGRESS("결제수단 인증 완료"),
    WAITING_FOR_DEPOSIT("가상계좌 입금 대기"),
    DONE("결제 승인 완료"),
    CANCELED("결제 취소"),
    PARTIAL_CANCELED("결제 부분 취소"),
    ABORTED("결제 승인 실패"),
    EXPIRED("결제 유효 시간 만료"),;

    companion object {
        fun get(status: String): PSPConfirmationStatus {
            return entries.find { it.name == status}
                ?: error("PSP 승인상태 (status: $status) 가 올바르지 않습니다.")
        }
    }
}