package com.app.backend.domain.post.service.scheduler

import com.app.backend.domain.attachment.service.FileService
import com.app.backend.domain.post.entity.Post
import com.app.backend.domain.post.entity.PostAttachment
import com.app.backend.domain.post.repository.post.PostRepository
import com.app.backend.domain.post.repository.postAttachment.PostAttachmentRepository
import com.app.backend.global.config.FileConfig
import jakarta.transaction.Transactional
import lombok.RequiredArgsConstructor
import lombok.extern.slf4j.Slf4j
import org.springframework.data.redis.core.RedisTemplate
import org.springframework.scheduling.annotation.Scheduled
import org.springframework.stereotype.Service
import java.time.LocalDate
import java.time.LocalDateTime
import java.util.function.Consumer

@Slf4j
@Service
@RequiredArgsConstructor
class PostScheduler {
    private val redisTemplate: RedisTemplate<String, Any>? = null

    private val fileConfig: FileConfig? = null
    private val fileService: FileService? = null
    private val postRepository: PostRepository? = null
    private val postAttachmentRepository: PostAttachmentRepository? = null

    @Transactional
    @Scheduled(fixedRate = 600000) // 10분
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
        val deleteDay = LocalDate.now().minusDays(deleteDays.toLong()).atStartOfDay()

        processDeletePosts(deleteDay)
        processDeleteFiles(deleteDay)
    }

    private fun processViewCountSave(typeKey: String, isReset: Boolean) {
        try {
            val updatedPostIds = redisTemplate!!.opsForSet().members(typeKey)

            if (updatedPostIds == null || updatedPostIds.isEmpty()) {
                PostScheduler.log.info("동기화 데이터가 존재하지 않습니다")
                return
            }

            val postIds = updatedPostIds.stream()
                .map { key: Any -> key.toString().substring(key.toString().lastIndexOf(":") + 1) }
                .map { s: String -> s.toLong() }
                .toList()

            val posts = postRepository!!.findAllById(postIds)

            posts.forEach(Consumer { post: Post ->
                val viewCountKey = VIEW_COUNT_PREFIX + post.id
                val viewCountValue = redisTemplate.opsForValue()[viewCountKey]
                if (viewCountValue != null) {
                    post.addTodayViewCount(viewCountValue.toString().toLong())
                    redisTemplate.delete(viewCountKey)
                }
                if (isReset) {
                    post.refreshViewCount()
                }
            })

            postRepository.saveAll(posts)
            redisTemplate.delete(POST_UPDATE)

            if (isReset) {
                redisTemplate.delete(POST_HISTORY)
            }

            PostScheduler.log.info("데이터 동기화를 완료했습니다")
        } catch (e: Exception) {
            PostScheduler.log.error("데이터 동기화에 실패했습니다")
        }
    }

    private fun processDeletePosts(deleteDay: LocalDateTime) {
        postRepository!!.deleteAllByModifiedAtAndDisabled(deleteDay, true)
    }

    private fun processDeleteFiles(deleteDay: LocalDateTime) {
        val files = postAttachmentRepository!!.findAllByModifiedAtAndDisabled(deleteDay, true)

        if (files.isEmpty()) {
            return
        }

        val deleteFilePaths: MutableList<String> = ArrayList()
        val deleteFileIds: MutableList<Long?> = ArrayList()

        files.forEach(Consumer<PostAttachment> { file: PostAttachment ->
            deleteFileIds.add(file.id)
            deleteFilePaths.add("%s/%s".formatted(fileConfig!!.basE_DIR, file.storeFilePath))
        })

        postAttachmentRepository.deleteByFileIdList(deleteFileIds)
        fileService!!.deleteFiles(deleteFilePaths)
    }

    companion object {
        private const val POST_UPDATE = "post:update"
        private const val POST_HISTORY = "post:history"
        private const val VIEW_COUNT_PREFIX = "viewCount:post:postid:"
        private const val deleteDays = 7
    }
}
