package com.app.backend.global.init.file

import jakarta.annotation.PostConstruct
import org.springframework.beans.factory.annotation.Value
import org.springframework.stereotype.Component
import java.io.File

@Component
class FileInit(
    @Value("\${spring.file.base-dir}") private val BASE_DIR: String
) {
    @PostConstruct
    fun init() {
        val uploadDir = File(BASE_DIR)

        if (!uploadDir.exists()) {
            val created = uploadDir.mkdirs()
            if (created) {
                println("Upload directory created at: " + uploadDir.absolutePath)
            }
        }
    }
}