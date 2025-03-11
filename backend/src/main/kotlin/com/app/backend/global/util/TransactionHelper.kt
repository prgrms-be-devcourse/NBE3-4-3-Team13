package com.app.backend.global.util

import org.springframework.stereotype.Component
import org.springframework.transaction.support.TransactionTemplate

@Component
class TransactionHelper(
    _transactionTemplate: TransactionTemplate
) {
    init {
        transactionTemplate = _transactionTemplate
    }

    companion object {
        private lateinit var transactionTemplate: TransactionTemplate

        fun <R> execute(block: () -> R) {
            transactionTemplate.execute { status ->
                kotlin.runCatching { block() }
                    .onSuccess { status.isCompleted }
                    .onFailure { status.setRollbackOnly() }
                    .getOrThrow()
            } ?: throw IllegalStateException("Transaction execution failed")
        }
    }
}