package com.app.backend.global.util

import io.github.oshai.kotlinlogging.KotlinLogging
import org.redisson.api.RLock
import org.redisson.api.RedissonClient
import org.redisson.client.RedisConnectionException
import org.springframework.stereotype.Component
import org.springframework.transaction.support.TransactionSynchronization
import org.springframework.transaction.support.TransactionSynchronizationManager
import java.util.concurrent.TimeUnit

@Component
class LockManager(private val redissonClient: RedissonClient) {
    companion object {
        private const val MAX_UNLOCK_RETRY_COUNT = 3
        private const val RETRY_DELAY = 100L
    }

    private val log = KotlinLogging.logger {}

    fun acquireLock(lockKey: String, maxWaitTime: Long, leaseTime: Long) =
        if (!isRedisRunning())
            throw IllegalStateException("Redis server is not available")
        else {
            val lock = redissonClient.getLock(lockKey)
            LockWrapper.of(
                lockKey,
                lock,
                tryRedissonLock(lock, maxWaitTime, leaseTime)
            )
        }

    fun releaseLock(lockWrapper: LockWrapper) {
        if (lockWrapper.locked)
            unlockRedissonLock(lockWrapper.lock, 0)
    }

    fun registerLockReleaseAfterTransaction(lockWrapper: LockWrapper) =
        TransactionSynchronizationManager.registerSynchronization(object : TransactionSynchronization {
            override fun afterCompletion(status: Int) {
                releaseLock(lockWrapper)
            }
        })

    private fun isRedisRunning() =
        try {
            redissonClient.keys.count()
            true
        } catch (e: RedisConnectionException) {
            log.warn(e) { "Redis server is not available. Switching to local lock" }
            false
        }

    private fun tryRedissonLock(lock: RLock, maxWaitTime: Long, leaseTime: Long): Boolean {
        var baseDelay = 100L
        var elapsedTime = 0L

        while (elapsedTime < maxWaitTime) {
            try {
                if (lock.tryLock(0, leaseTime, TimeUnit.MILLISECONDS))
                    return true
            } catch (e: InterruptedException) {
                Thread.currentThread().interrupt()
                throw RuntimeException("Redis lock acquisition interrupted", e)
            }

            log.info { "Redis lock acquisition failed, retrying after wait time: $baseDelay ms" }
            sleep(baseDelay)

            elapsedTime += baseDelay
            baseDelay = minOf(baseDelay * 2, maxWaitTime - elapsedTime)
        }
        return false
    }

    private fun unlockRedissonLock(lock: RLock, retryCount: Int) {
        if (lock.isLocked && lock.isHeldByCurrentThread)
            try {
                lock.unlock()
                log.info { "Redisson lock successfully unlocked" }
            } catch (e: Exception) {
                log.warn { "Failed to unlock redisson lock, retrying ${retryCount + 1}/${MAX_UNLOCK_RETRY_COUNT}" }
                if (retryCount < MAX_UNLOCK_RETRY_COUNT) {
                    sleep(RETRY_DELAY)
                    unlockRedissonLock(lock, retryCount + 1)
                } else
                    forceUnlockRedissonLock(lock)
            }
    }

    private fun forceUnlockRedissonLock(lock: RLock) {
        if (lock.isLocked && lock.isHeldByCurrentThread) {
            lock.forceUnlock()
            log.warn { "Redisson lock forcefully unlocked after max retries" }
        } else
            log.warn { "Skipping force unlock, lock is not held by current thread" }
    }

    private fun sleep(millis: Long) {
        try {
            Thread.sleep(millis)
        } catch (e: InterruptedException) {
            Thread.currentThread().interrupt()
            throw RuntimeException("Thread sleep interrupted", e)
        }
    }

    data class LockWrapper(
        val lockKey: String,
        val lock: RLock,
        val locked: Boolean
    ) {
        companion object {
            fun of(
                lockKey: String,
                lock: RLock,
                locked: Boolean
            ) = LockWrapper(lockKey, lock, locked)
        }
    }
}
