package com.app.backend.domain.attachment.controller

import com.google.auth.oauth2.GoogleCredentials
import com.google.cloud.storage.Blob
import com.google.cloud.storage.BlobInfo
import com.google.cloud.storage.HttpMethod
import com.google.cloud.storage.Storage
import com.google.cloud.storage.StorageOptions
import org.springframework.core.io.ClassPathResource
import org.springframework.http.HttpStatus
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RequestParam
import org.springframework.web.bind.annotation.RestController
import java.io.InputStream
import java.net.URL
import java.util.concurrent.TimeUnit

@RestController
@RequestMapping("/api/v1/")
class FileUploadController {

	private lateinit var storage: Storage
	private val bucketName = "bucket-get-started_linkus-453401" // 구글 클라우드 스토리지 버킷 이름

	init {
		// resources에서 서비스 계정 키 로드
		val resource = ClassPathResource("linkus-453401.json")
		val serviceAccountStream: InputStream = resource.inputStream

		// 인증 정보로 GCS 클라이언트 생성
		val credentials = GoogleCredentials.fromStream(serviceAccountStream)
		storage = StorageOptions.newBuilder()
			.setCredentials(credentials)
			.build()
			.service
	}

	@GetMapping("/generate-signed-url")
	fun generateSignedUrl(@RequestParam filename: String): ResponseEntity<String> {
		// 스토리지에서 파일 찾기
		val blob: Blob? = storage[bucketName, filename]

		// 파일이 존재하면 에러 반환
		if (blob != null) {
			return ResponseEntity.status(HttpStatus.CONFLICT).body("이미 존재하는 파일입니다.")
		}

		// 새 파일을 업로드할 수 있도록 Signed URL 생성
		val blobInfo = BlobInfo.newBuilder(bucketName, filename).build()
		val signedUrl: URL = storage.signUrl(
			blobInfo,
			15, TimeUnit.MINUTES, // URL 유효 기간
			Storage.SignUrlOption.withV4Signature(), // V4 서명 사용
			Storage.SignUrlOption.httpMethod(HttpMethod.PUT) // 파일 업로드용 URL
		)

		return ResponseEntity.ok(signedUrl.toString()) // 생성된 Signed URL 반환
	}
}
