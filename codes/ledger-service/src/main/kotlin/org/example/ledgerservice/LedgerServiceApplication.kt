package org.example.ledgerservice

import org.springframework.boot.autoconfigure.SpringBootApplication
import org.springframework.boot.runApplication

@SpringBootApplication
class LedgerServiceApplication

fun main(args: Array<String>) {
    runApplication<LedgerServiceApplication>(*args)
}
