package com.app.backend.global.util

import org.aspectj.lang.ProceedingJoinPoint
import org.aspectj.lang.reflect.MethodSignature
import org.springframework.expression.ExpressionParser
import org.springframework.expression.spel.standard.SpelExpressionParser
import org.springframework.expression.spel.support.StandardEvaluationContext
import java.time.LocalDateTime
import java.util.*

class LockKeyGenerator {
    companion object {
        private val parser: ExpressionParser = SpelExpressionParser()

        fun generateLockKey(joinPoint: ProceedingJoinPoint, spelExpression: String): String {
            val signature = joinPoint.signature as MethodSignature
            val args = joinPoint.args
            val method = signature.method
            val parameterNames = signature.parameterNames

            val context = StandardEvaluationContext()
            parameterNames.forEachIndexed { index, parameterName -> context.setVariable(parameterName, args[index]) }

            val value = parser.parseExpression(spelExpression).getValue(context)
                ?: throw IllegalArgumentException("Lock key cannot be null")

            return "${method.name}:${convertToKey(value)}"
        }

        fun generateLockKey(keyParam: String) = "${
            Thread.currentThread().stackTrace.first {
                it.methodName != "generateLockKey"
            }.methodName
        }:$keyParam"

        private fun convertToKey(value: Any): String = when (value) {
            is String -> value
            is Number, is Boolean -> value.toString()
            is Enum<*> -> value.name
            is LocalDateTime -> AppUtil.localDateTimeToString(value)
            is Date -> AppUtil.DateToString(value)
            is Array<*> -> arrayToString(value)
            is Collection<*> -> collectionToString(value)
            is Map<*, *> -> mapToString(value)
            else -> value.toString()
        }

        private fun arrayToString(array: Any): String =
            (0 until java.lang.reflect.Array.getLength(array)).joinToString(",", prefix = "[", postfix = "]") {
                convertToKey(java.lang.reflect.Array.get(array, it))
            }

        private fun collectionToString(collection: Collection<*>): String =
            collection.joinToString(",", prefix = "[", postfix = "]") { convertToKey(it!!) }

        private fun mapToString(map: Map<*, *>): String =
            map.entries.joinToString(
                ",",
                prefix = "[",
                postfix = "]"
            ) { "${convertToKey(it.key!!)}=${convertToKey(it.value!!)}" }

    }
}