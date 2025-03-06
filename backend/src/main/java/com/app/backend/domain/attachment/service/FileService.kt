package com.app.backend.domain.attachment.service

import org.springframework.web.multipart.MultipartFile

interface FileService {
    fun saveFile(file: MultipartFile): String
    fun deleteFile(storedFilename: String)
    fun deleteFiles(filePaths: List<String>)
}
