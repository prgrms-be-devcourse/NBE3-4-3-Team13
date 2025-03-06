package com.app.backend.domain.attachment.util

import com.app.backend.domain.attachment.entity.FileType
import com.app.backend.domain.attachment.exception.FileErrorCode
import com.app.backend.domain.attachment.exception.FileException
import java.nio.file.Path
import java.util.UUID

object FileUtil {

    fun generateFileName(dateDir: Path, ext: String): String {
        val currentDate = dateDir.fileName.toString() // yyyyMMdd 형식
        val shortUUID = UUID.randomUUID().toString().take(8)
        return "${currentDate}_$shortUUID.$ext"
    }

    fun getFileName(filePath: String): String {
        return filePath.substringAfterLast("/")
            .ifEmpty { throw FileException(FileErrorCode.FILE_NOT_FOUND) }
    }

    fun getExtension(fileName: String): String {
        return fileName.substringAfterLast(".", "")
            .lowercase()
            .ifEmpty { throw FileException(FileErrorCode.INVALID_FILE_EXTENSION) }
    }

    fun getFileType(fileName: String): FileType {
        return when (getExtension(fileName)) {
            "jpeg", "jpg", "png", "gif" -> FileType.IMAGE
            "pdf", "doc", "docx" -> FileType.DOCUMENT
            "mp4", "avi", "mkv" -> FileType.VIDEO
            else -> throw FileException(FileErrorCode.UNSUPPORTED_FILE_TYPE)
        }
    }
}
