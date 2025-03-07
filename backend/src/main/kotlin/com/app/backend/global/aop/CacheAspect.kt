package com.app.backend.global.aop

import com.app.backend.domain.member.entity.MemberDetails
import com.app.backend.global.annotation.CustomCache
import com.app.backend.global.annotation.CustomCacheDelete
import org.aspectj.lang.ProceedingJoinPoint
import org.aspectj.lang.annotation.Around
import org.aspectj.lang.annotation.Aspect
import org.aspectj.lang.reflect.MethodSignature
import org.springframework.data.redis.core.RedisTemplate
import org.springframework.security.core.Authentication
import org.springframework.security.core.context.SecurityContextHolder
import org.springframework.stereotype.Component

@Aspect
@Component
class CacheAspect(private val redisTemplate: RedisTemplate<String, Any>) {

    companion object {
        private const val UPDATE_KEY = "update"
        private const val HISTORY_KEY = "history"
        private const val VIEW_COUNT_PREFIX = "viewCount"
    }

    @Around("@annotation(customCache)")
    @Throws(Throwable::class)
    fun aroundG(joinPoint: ProceedingJoinPoint, customCache: CustomCache): Any? {
        val cacheKey = generateKey(customCache.prefix, customCache.key, customCache.id, getParams(joinPoint))
        val viewCountKey = "$VIEW_COUNT_PREFIX:$cacheKey"
        val limitUserKey = "$cacheKey:user:${getUserID()}"
        val updateKeyList = "${customCache.prefix}:$UPDATE_KEY"
        val historyKey = "${customCache.prefix}:$HISTORY_KEY"

        return try {
            // 조회수 증가
            if (customCache.viewCount && !redisTemplate.hasKey(limitUserKey)) {
                redisTemplate.opsForValue().increment(viewCountKey)
                redisTemplate.opsForValue().set(limitUserKey, true, customCache.viewCountTtl, customCache.viewCountTtlUnit)
                redisTemplate.opsForSet().add(updateKeyList, viewCountKey)
            }

            val cachedData = redisTemplate.opsForValue().get(cacheKey)

            // 조회 기록 저장
            if (customCache.history) {
                redisTemplate.opsForSet().add(historyKey, cacheKey)
            }

            cachedData ?: joinPoint.proceed().also {
                redisTemplate.opsForValue().set(cacheKey, it, customCache.ttl, customCache.ttlUnit)
            }
        } catch (e: Exception) {
            joinPoint.proceed()
        }
    }

    @Around("@annotation(customCacheDelete)")
    @Throws(Throwable::class)
    fun aroundD(joinPoint: ProceedingJoinPoint, customCacheDelete: CustomCacheDelete): Any? {
        val cacheKey = generateKey(customCacheDelete.prefix, customCacheDelete.key, customCacheDelete.id, getParams(joinPoint))

        return try {
            if (redisTemplate.hasKey(cacheKey)) {
                redisTemplate.delete(cacheKey)
            }
            joinPoint.proceed()
        } catch (e: Exception) {
            joinPoint.proceed()
        }
    }

    private fun getUserID(): Long {
        val authentication: Authentication = SecurityContextHolder.getContext().authentication
        val memberDetails = authentication.principal as MemberDetails
        return memberDetails.id!!
    }

    private fun getParams(joinPoint: ProceedingJoinPoint): Map<String, Any> {
        val methodSignature = joinPoint.signature as MethodSignature
        val paramsName = methodSignature.parameterNames
        val args = joinPoint.args

        return paramsName.zip(args).toMap()
    }

    private fun generateKey(prefix: String, key: String, id: String, params: Map<String, Any>): String {
        val newKey = StringBuilder(prefix)

        if (key.isNotEmpty()) {
            newKey.append(":").append(key)
        }

        if (id.isNotEmpty() && params.containsKey(id)) {
            newKey.append(":").append(params[id])
            return newKey.toString()
        }

        params.values.forEach { newKey.append(":").append(it) }

        return newKey.toString()
    }
}
