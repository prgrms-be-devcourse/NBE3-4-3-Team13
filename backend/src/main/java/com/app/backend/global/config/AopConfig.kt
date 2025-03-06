package com.app.backend.global.config

import com.app.backend.global.aop.AppAspect.Companion.LockAspect
import com.app.backend.global.aop.AppAspect.Companion.PageJsonSerializerAspect
import com.app.backend.global.util.LockManager
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration

@Configuration
class AopConfig {
    @Bean
    fun pageJsonSerializerAspect() = PageJsonSerializerAspect()

    @Bean
    fun redissonLockAspect(lockManager: LockManager) = LockAspect(lockManager)
}
