package com.app.backend.global.util

import org.redisson.api.RedissonClient
import org.springframework.stereotype.Component
import java.util.concurrent.TimeUnit

@Component
class LockHelper(private val redissonClient: RedissonClient, private val lockUtil: LockUtil) {
    fun <R> executeWithLock(
        lockKey: String,
        maxWaitTime: Long = 1000L,
        leaseTime: Long = 5000L,
        timeUnit: TimeUnit = TimeUnit.MILLISECONDS,
        block: () -> R
    ): R {
        val lock = redissonClient.getLock(lockKey)
        return if (lockUtil.lockWithRetry(lock, timeUnit.toMillis(maxWaitTime), timeUnit.toMillis(leaseTime)))
            try {
                block()
            } finally {
                if (lock.isHeldByCurrentThread)
                    lock.unlock()
            }
        else
            throw RuntimeException("Failed to acquire lock: $lockKey")
    }

    fun executeWithLock(
        lockKey: String,
        maxWaitTime: Long = 1000L,
        leaseTime: Long = 5000L,
        timeUnit: TimeUnit = TimeUnit.MILLISECONDS,
        block: () -> Unit
    ) {
        executeWithLock(lockKey, maxWaitTime, leaseTime, timeUnit) {
            block()
            Unit
        }
    }
}