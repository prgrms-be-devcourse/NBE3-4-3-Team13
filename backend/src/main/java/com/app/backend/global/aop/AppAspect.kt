package com.app.backend.global.aop

import com.app.backend.global.annotation.CustomLock
import com.app.backend.global.annotation.CustomPageJsonSerializer
import com.app.backend.global.dto.response.ApiResponse
import com.app.backend.global.util.LockKeyGenerator
import com.app.backend.global.util.LockManager
import com.app.backend.global.util.PageUtil
import io.github.oshai.kotlinlogging.KLogger
import io.github.oshai.kotlinlogging.KotlinLogging
import org.aspectj.lang.ProceedingJoinPoint
import org.aspectj.lang.annotation.Around
import org.aspectj.lang.annotation.Aspect
import org.aspectj.lang.reflect.MethodSignature
import org.springframework.data.domain.Page
import org.springframework.http.ResponseEntity
import org.springframework.transaction.support.TransactionSynchronizationManager

class AppAspect {
    companion object {
        private val log: KLogger = KotlinLogging.logger { }

        @Aspect
        class PageJsonSerializerAspect {
            @Around("@annotation(com.app.backend.global.annotation.CustomPageJsonSerializer)")
            @Throws(Throwable::class)
            fun execute(joinPoint: ProceedingJoinPoint): Any? {
                val signature = joinPoint.signature as MethodSignature
                val method = signature.method ?: return joinPoint.proceed()
                val annotation = method.getAnnotation(CustomPageJsonSerializer::class.java)
                    ?: return joinPoint.proceed()

                return when (val result = joinPoint.proceed()) {
                    is Page<*> -> PageUtil.processPageJson(result, annotation)
                    is ApiResponse<*> -> PageUtil.processApiResponse(result, annotation)
                    is ResponseEntity<*> -> PageUtil.processResponseEntity(result, annotation)
                    else -> result
                }
            }
        }

        @Aspect
        class LockAspect(private val lockManager: LockManager) {
            @Around("@annotation(customLock)")
            @Throws(Throwable::class)
            fun execute(joinPoint: ProceedingJoinPoint, customLock: CustomLock): Any? {
                val lockKey = LockKeyGenerator.generateLockKey(joinPoint, customLock.key)
                val lockWrapper = lockManager.acquireLock(
                    lockKey,
                    customLock.timeUnit.toMillis(customLock.maxWaitTime),
                    customLock.timeUnit.toMillis(customLock.leaseTime)
                )

                if (!lockWrapper.locked)
                    throw RuntimeException("Failed to acquire lock: $lockKey")

                try {
                    val result = joinPoint.proceed()

                    if (TransactionSynchronizationManager.isActualTransactionActive())
                        lockManager.registerLockReleaseAfterTransaction(lockWrapper)
                    else lockManager.releaseLock(lockWrapper)

                    return result
                } catch (e: Throwable) {
                    lockManager.releaseLock(lockWrapper)
                    throw e
                }
            }
        }
    }
}