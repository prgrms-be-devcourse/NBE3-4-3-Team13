package com.app.backend.global.util

import io.github.oshai.kotlinlogging.KotlinLogging
import org.redisson.api.RedissonClient
import org.redisson.client.RedisConnectionException
import org.springframework.stereotype.Component
import org.springframework.transaction.support.TransactionSynchronization
import org.springframework.transaction.support.TransactionSynchronizationManager

@Component
class LockManager(private val redissonClient: RedissonClient, private val lockUtil: LockUtil) {
    private val log = KotlinLogging.logger {}

    fun acquireLock(lockKey: String, maxWaitTime: Long, leaseTime: Long) =
        if (!isRedisRunning())
            throw IllegalStateException("Redis server is not available")
        else {
            val lock = redissonClient.getLock(lockKey)
            LockUtil.LockWrapper.of(
                lockKey,
                lock,
                lockUtil.lockWithRetry(lock, maxWaitTime, leaseTime)
            )
        }

    fun releaseLock(lockWrapper: LockUtil.LockWrapper) {
        if (lockWrapper.locked)
            lockUtil.unlockWithRetry(lockWrapper.lock, 0)
    }

    fun registerLockReleaseAfterTransaction(lockWrapper: LockUtil.LockWrapper) =
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
}
