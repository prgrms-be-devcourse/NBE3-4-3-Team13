package com.app.backend.global.util

import io.github.oshai.kotlinlogging.KotlinLogging
import jakarta.annotation.PreDestroy
import org.redisson.api.RLock
import org.redisson.api.RedissonClient
import org.redisson.client.RedisConnectionException
import org.springframework.stereotype.Component
import org.springframework.transaction.support.TransactionSynchronization
import org.springframework.transaction.support.TransactionSynchronizationManager
import java.util.*
import java.util.concurrent.*
import java.util.concurrent.locks.ReentrantLock

@Component
class LockManager(private val redissonClient: Optional<RedissonClient>) {
    private val log = KotlinLogging.logger {}
    private val MAX_UNLOCK_RETRY_COUNT = 3
    private val RETRY_DELAY = 100L

    private val localLockMap = ConcurrentHashMap<String, ReentrantLock>()

    private val executorService = Executors.newFixedThreadPool(10)
    private val scheduler = Executors.newScheduledThreadPool(1)

    fun acquireLock(lockKey: String, maxWaitTime: Long, leaseTime: Long): LockWrapper {
        val isRedisAvailable = isRedisRunning()

        val redisLock: RLock? = if (isRedisAvailable) redissonClient.get().getLock(lockKey) else null
        val localLock: ReentrantLock? = if (isRedisAvailable) null else getLock(lockKey)

        val locked = if (isRedisAvailable) tryRedissonLock(redisLock!!, maxWaitTime, leaseTime)
        else tryLocalLock(localLock!!, maxWaitTime)

        return LockWrapper.of(lockKey, redisLock, localLock, isRedisAvailable && locked, locked)
    }

    fun releaseLock(lockWrapper: LockWrapper) =
        if (lockWrapper.usingRedisLock)
            lockWrapper.redisLock?.let { unlockRedissonLock(it, 0) }
        else {
            lockWrapper.localLock?.let { unlockLocalLock(it) }
            releaseLock(lockWrapper.lockKey, lockWrapper.localLock!!)
        }

    fun registerLockReleaseAfterTransaction(lockWrapper: LockWrapper) =
        TransactionSynchronizationManager.registerSynchronization(object : TransactionSynchronization {
            override fun afterCompletion(status: Int) {
                releaseLock(lockWrapper)
            }
        })

    private fun isRedisRunning() = redissonClient.map {
        try {
            it.keys.count()
            true
        } catch (e: RedisConnectionException) {
            log.warn(e) { "Redis server is not available. Switching to local lock" }
            false
        }
    }.orElse(false)

    private fun tryRedissonLock(lock: RLock, maxWaitTime: Long, leaseTime: Long) =
        try {
            CompletableFuture.supplyAsync({
                var baseDelay = 100L
                var elapsedTime = 0L

                while (elapsedTime < maxWaitTime) {
                    try {
                        if (lock.tryLock(0, leaseTime, TimeUnit.MILLISECONDS)) return@supplyAsync true
                    } catch (e: InterruptedException) {
                        Thread.currentThread().interrupt()
                        throw RuntimeException("Redis lock acquisition interrupted", e)
                    }

                    log.info { "Redis lock acquisition failed, retrying after wait time: $baseDelay ms" }
                    sleep(baseDelay)

                    elapsedTime += baseDelay
                    baseDelay = minOf(baseDelay * 2, maxWaitTime - elapsedTime)
                }
                false
            }, executorService).get()
        } catch (e: InterruptedException) {
            log.error(e) { "Redisson lock acquisition interrupted" }
            Thread.currentThread().interrupt()
            false
        } catch (e: ExecutionException) {
            log.error(e) { "Execution exception occurred" }
            false
        }

    private fun tryLocalLock(lock: ReentrantLock, maxWaitTime: Long) =
        try {
            CompletableFuture.supplyAsync {
                var baseDelay = 100L
                var elapsedTime = 0L

                while (elapsedTime < maxWaitTime) {
                    try {
                        if (lock.tryLock(0, TimeUnit.MILLISECONDS)) return@supplyAsync true
                    } catch (e: InterruptedException) {
                        Thread.currentThread().interrupt()
                        throw RuntimeException("Local lock acquisition interrupted", e)
                    }

                    log.info { "Local lock acquisition failed, retrying after wait time: $baseDelay ms" }
                    sleep(baseDelay)

                    elapsedTime += baseDelay
                    baseDelay = minOf(baseDelay * 2, maxWaitTime - elapsedTime)
                }
                false
            }.get()
        } catch (e: InterruptedException) {
            log.error(e) { "Local lock acquisition interrupted" }
            Thread.currentThread().interrupt()
            false
        } catch (e: ExecutionException) {
            log.error(e) { "Execution exception occurred" }
            false
        }

    private fun unlockRedissonLock(lock: RLock, retryCount: Int) {
        if (lock.isLocked && lock.isHeldByCurrentThread) {
            lock.unlockAsync().whenComplete { _, throwable ->
                if (throwable != null) {
                    log.warn { "Failed to unlock redisson lock, retrying ${retryCount + 1}/${MAX_UNLOCK_RETRY_COUNT}" }
                    if (retryCount < MAX_UNLOCK_RETRY_COUNT) {
                        scheduler.schedule(
                            { unlockRedissonLock(lock, retryCount + 1) },
                            RETRY_DELAY,
                            TimeUnit.MILLISECONDS
                        )
                    } else
                        forceUnlockRedissonLock(lock)
                } else
                    log.info { "Redisson lock successfully unlocked" }
            }
        }
    }

    private fun forceUnlockRedissonLock(lock: RLock) {
        if (lock.isLocked && lock.isHeldByCurrentThread) {
            lock.forceUnlock()
            log.warn { "Redisson lock forcefully unlocked after max retries" }
        } else
            log.warn { "Skipping force unlock, lock is not held by current thread" }
    }

    private fun unlockLocalLock(lock: ReentrantLock) {
        if (lock.isHeldByCurrentThread) lock.unlock()
    }

    private fun sleep(millis: Long) {
        try {
            Thread.sleep(millis)
        } catch (e: InterruptedException) {
            Thread.currentThread().interrupt()
            throw RuntimeException("Thread sleep interrupted", e)
        }
    }

    private fun getLock(key: String): ReentrantLock {
        return localLockMap.computeIfAbsent(key) { ReentrantLock() }
    }

    private fun releaseLock(key: String, lock: ReentrantLock) {
        if (!lock.hasQueuedThreads())
            localLockMap.remove(key)
    }

    @PreDestroy
    private fun shutdownExecutors() {
        log.info { "Shutting down executor services..." }
        executorService.shutdown()
        scheduler.shutdown()

        try {
            if (!executorService.awaitTermination(5, TimeUnit.SECONDS)) {
                log.warn { "ExecutorService did not terminate in the specified time." }
                executorService.shutdownNow()
            }
            if (!scheduler.awaitTermination(5, TimeUnit.SECONDS)) {
                log.warn { "ScheduledExecutorService did not terminate in the specified time." }
                scheduler.shutdownNow()
            }
        } catch (e: InterruptedException) {
            log.error(e) { "Shutdown interrupted" }
            executorService.shutdownNow()
            scheduler.shutdownNow()
            Thread.currentThread().interrupt()
        }
    }

    data class LockWrapper(
        val lockKey: String,
        val redisLock: RLock?,
        val localLock: ReentrantLock?,
        val usingRedisLock: Boolean,
        val locked: Boolean
    ) {
        companion object {
            fun of(
                lockKey: String,
                redisLock: RLock?,
                localLock: ReentrantLock?,
                usingRedisLock: Boolean,
                locked: Boolean
            ) = LockWrapper(lockKey, redisLock, localLock, usingRedisLock, locked)
        }
    }
}
