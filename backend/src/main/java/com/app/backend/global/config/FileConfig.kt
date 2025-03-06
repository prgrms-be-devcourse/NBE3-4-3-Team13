package com.app.backend.global.config

import org.springframework.beans.factory.annotation.Value
import org.springframework.context.annotation.Configuration
import org.springframework.web.servlet.config.annotation.ResourceHandlerRegistry
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer

@Configuration
class FileConfig : WebMvcConfigurer {
    @Value("\${spring.file.base-dir}")
    private lateinit var bASE_DIR: String

    @Value("\${spring.file.img-dir}")
    private lateinit var iMAGE_DIR: String

    // 정적 리소스 방식
    override fun addResourceHandlers(registry: ResourceHandlerRegistry) {
        registry.addResourceHandler("/images/**")
            .addResourceLocations("file:///" + bASE_DIR + "/")
    }
}
