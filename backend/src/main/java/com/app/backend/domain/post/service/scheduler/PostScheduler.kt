package com.app.backend.domain.post.service.scheduler

import com.app.backend.domain.attachment.service.FileService
import com.app.backend.domain.post.repository.post.PostRepository
import com.app.backend.domain.post.repository.postAttachment.PostAttachmentRepository
import com.app.backend.global.config.FileConfig
import org.slf4j.Logger
import org.slf4j.LoggerFactory
import org.springframework.data.redis.core.RedisTemplate
import org.springframework.scheduling.annotation.Scheduled
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import java.time.LocalDate
import java.time.LocalDateTime

@Service
@Transactional(readOnly = true)
class PostScheduler(
    private val redisTemplate: RedisTemplate<String, Any>,
    private val fileConfig: FileConfig,
    private val fileService: FileService,
    private val postRepository: PostRepository,
    private val postAttachmentRepository: PostAttachmentRepository
) {

    private val log: Logger = LoggerFactory.getLogger(PostScheduler::class.java)

    companion object {
        private const val POST_UPDATE = "post:update"
        private const val POST_HISTORY = "post:history"
        private const val VIEW_COUNT_PREFIX = "viewCount:post:postid:"
        private const val DELETE_DAYS = 7
    }

    @Transactional
    @Scheduled(fixedRate = 600_000) // 10분
    fun viewCountsRedisToRDB() {
        processViewCountSave(POST_UPDATE, false)
    }

    @Transactional
    @Scheduled(cron = "0 0 0 * * ?")
    fun refreshViewCount() {
        processViewCountSave(POST_HISTORY, true)
    }

    @Transactional
    @Scheduled(cron = "0 0 4 * * ?")
    fun deletePosts() {
        val deleteDay = LocalDate.now().minusDays(DELETE_DAYS.toLong()).atStartOfDay()
        processDeletePosts(deleteDay)
        processDeleteFiles(deleteDay)
    }

    private fun processViewCountSave(typeKey: String, isReset: Boolean) {
        try {
            val updatedPostIds: Set<String>? = redisTemplate.opsForSet().members(typeKey) as? Set<String>

            if (updatedPostIds.isNullOrEmpty()) {
                log.info("동기화 데이터가 존재하지 않습니다")
                return
            }

            val postIds = updatedPostIds.map { it.substringAfterLast(":").toLong() }
            val posts = postRepository.findAllById(postIds)

            posts.forEach { post ->
                val viewCountKey = "$VIEW_COUNT_PREFIX${post.id}"
                val viewCountValue = redisTemplate.opsForValue().get(viewCountKey) as? String
                viewCountValue?.toLongOrNull()?.let {
                    post.addTodayViewCount(it)
                    redisTemplate.delete(viewCountKey)
                }
                if (isReset) {
                    post.refreshViewCount()
                }
            }

            postRepository.saveAll(posts)
            redisTemplate.delete(POST_UPDATE)

            if (isReset) {
                redisTemplate.delete(POST_HISTORY)
            }

            log.info("데이터 동기화를 완료했습니다")
        } catch (e: Exception) {
            log.error("데이터 동기화에 실패했습니다", e)
        }
    }

    private fun processDeletePosts(deleteDay: LocalDateTime) {
        postRepository.deleteAllByModifiedAtAndDisabled(deleteDay, true)
    }

    private fun processDeleteFiles(deleteDay: LocalDateTime) {
        val files = postAttachmentRepository.findAllByModifiedAtAndDisabled(deleteDay, true)

        if (files.isNotEmpty()) {
            val deleteFilePaths = files.map { "${fileConfig.getBaseDir()}/${it.storeFilePath}" }
            val deleteFileIds = files.mapNotNull { it.id }

            postAttachmentRepository.deleteByFileIdList(deleteFileIds)
            fileService.deleteFiles(deleteFilePaths)
        }
    }
}

