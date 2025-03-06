package com.app.backend.global.util

import org.aspectj.lang.ProceedingJoinPoint
import org.aspectj.lang.reflect.MethodSignature
import org.springframework.expression.ExpressionParser
import org.springframework.expression.spel.standard.SpelExpressionParser
import org.springframework.expression.spel.support.StandardEvaluationContext
import java.lang.reflect.Array
import java.time.LocalDateTime
import java.util.*

object LockKeyGenerator {
    private val PARSER: ExpressionParser = SpelExpressionParser()

    @JvmStatic
    fun generateLockKey(joinPoint: ProceedingJoinPoint, spelExpression: String): String {
        val signature = joinPoint.signature as MethodSignature
        val args = joinPoint.args
        val method = signature.method
        val parameterNames = signature.parameterNames

        val context = StandardEvaluationContext()
        parameterNames?.forEachIndexed { index, paramName ->
            context.setVariable(paramName, args[index])
        }

        val value = PARSER.parseExpression(spelExpression).getValue(context)
            ?: throw IllegalArgumentException("Lock key cannot be null")

        return "${method.name}:${convertToKey(value)}"
    }

    private fun convertToKey(value: Any): String = when (value) {
        is String -> value
        is Number, is Boolean -> value.toString()
        is Enum<*> -> value.name
        is LocalDateTime -> AppUtil.localDateTimeToString(value)
        is Date -> AppUtil.DateToString(value)
        else -> when {
            value.javaClass.isArray -> arrayToString(value)
            value is Collection<*> -> collectionToString(value)
            value is Map<*, *> -> mapToString(value)
            else -> value.toString()
        }
    }

    private fun arrayToString(array: Any): String {
        val elements = mutableListOf<String>()
        for (i in 0 until Array.getLength(array)) {
            elements.add(convertToKey(Array.get(array, i)))
        }
        return "[${elements.joinToString(",")}]"
    }

    private fun collectionToString(collection: Collection<*>): String {
        val elements = collection.map { convertToKey(it ?: "null") }
        return "[${elements.joinToString(",")}]"
    }

    private fun mapToString(map: Map<*, *>): String {
        val entries = map.entries.map {
            "${it.key?.let { it1 -> convertToKey(it1) }}=${it.value?.let { it1 -> convertToKey(it1) }}"
        }
        return "{${entries.joinToString(",")}}"
    }
}