package com.app.backend.domain.attachment.service

import com.app.backend.domain.attachment.exception.FileErrorCode
import com.app.backend.domain.attachment.exception.FileException
import com.app.backend.domain.attachment.util.FileUtil
import org.springframework.beans.factory.annotation.Value
import org.springframework.scheduling.annotation.Async
import org.springframework.stereotype.Service
import org.springframework.web.multipart.MultipartFile
import java.io.File
import java.io.IOException
import java.nio.file.Files
import java.nio.file.Path
import java.nio.file.Paths
import java.time.LocalDate
import java.time.format.DateTimeFormatter

@Service
class LocalStorageFileServiceImpl(
    @Value("\${spring.file.base-dir}") private val BASE_DIR: String
) : FileService {

    override fun saveFile(file: MultipartFile): String {
        return try {
            val currentDate = LocalDate.now().format(DateTimeFormatter.ofPattern("yyyyMMdd"))
            val dateDir = Path.of(BASE_DIR, currentDate)

            Files.createDirectories(dateDir)

            val originalFileName = file.originalFilename ?: ""
            val ext = FileUtil.getExtension(originalFileName)

            val storedFilename = FileUtil.generateFileName(dateDir, ext)

            val filePath = dateDir.resolve(storedFilename)
            file.transferTo(filePath.toFile())

            "$currentDate/$storedFilename" // String.format() 대신 String Template 사용
        } catch (e: IOException) {
            throw FileException(FileErrorCode.FAILED_FILE_SAVE)
        }
    }

    override fun deleteFile(filePath: String) {
        try {
            val file = Path.of(filePath)
            Files.deleteIfExists(file)
        } catch (e: Exception) {
            println("파일 삭제 실패: $filePath")
        }
    }

    @Async
    override fun deleteFiles(filePaths: List<String>) {
        filePaths.forEach { deleteFile(it) }
    }
}
