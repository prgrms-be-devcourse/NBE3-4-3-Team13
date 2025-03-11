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

        fun <R> execute(block: () -> R): R =
            transactionTemplate.execute { status ->
                try {
                    block().also { return@execute it }
                } catch (e: Exception) {
                    status.setRollbackOnly()
                    throw e
                }
            } ?: throw IllegalStateException("Transaction execution failed")

        fun execute(block: () -> Unit) =
            transactionTemplate.execute { status ->
                try {
                    block()
                } catch (e: Exception) {
                    status.setRollbackOnly()
                    throw e
                }
            }
    }
}