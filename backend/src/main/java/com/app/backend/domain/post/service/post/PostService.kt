package com.app.backend.domain.post.service.post;

import com.app.backend.domain.attachment.entity.FileType;
import com.app.backend.domain.attachment.exception.FileErrorCode;
import com.app.backend.domain.attachment.exception.FileException;
import com.app.backend.domain.attachment.service.FileService;
import com.app.backend.domain.attachment.util.FileUtil;
import com.app.backend.domain.group.entity.GroupMembership;
import com.app.backend.domain.group.entity.GroupMembershipId;
import com.app.backend.domain.group.entity.GroupRole;
import com.app.backend.domain.group.entity.MembershipStatus;
import com.app.backend.domain.group.exception.GroupMembershipErrorCode;
import com.app.backend.domain.group.exception.GroupMembershipException;
import com.app.backend.domain.group.repository.GroupMembershipRepository;
import com.app.backend.domain.member.entity.Member;
import com.app.backend.domain.member.repository.MemberRepository;
import com.app.backend.domain.post.dto.req.PostReqDto;
import com.app.backend.domain.post.dto.resp.PostAttachmentRespDto;
import com.app.backend.domain.post.dto.resp.PostRespDto;
import com.app.backend.domain.post.entity.Post;
import com.app.backend.domain.post.entity.PostAttachment;
import com.app.backend.domain.post.entity.PostLike;
import com.app.backend.domain.post.entity.PostStatus;
import com.app.backend.domain.post.exception.PostErrorCode;
import com.app.backend.domain.post.exception.PostException;
import com.app.backend.domain.post.repository.post.PostLikeRepository;
import com.app.backend.domain.post.repository.post.PostRepository;
import com.app.backend.domain.post.repository.postAttachment.PostAttachmentRepository;
import com.app.backend.global.annotation.CustomCache;
import com.app.backend.global.annotation.CustomCacheDelete;
import com.app.backend.global.config.FileConfig;
import com.app.backend.global.error.exception.GlobalErrorCode;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

@Service
@Transactional(readOnly = true)
class PostService(
    private val fileConfig: FileConfig,
    private val fileService: FileService,
    private val postRepository: PostRepository,
    private val memberRepository: MemberRepository,
    private val postLikeRepository: PostLikeRepository,
    private val postAttachmentRepository: PostAttachmentRepository,
    private val groupMembershipRepository: GroupMembershipRepository
) {
    private val MAX_FILE_SIZE = 10 * 1024 * 1024;

    @CustomCache(prefix = "post", key = "postid", id = "postId", viewCount = true, viewCountTtl = 10, history = true)
    fun getPost(postId: Long, memberId: Long): PostRespDto.GetPostDto {
        val post = getPostEntity(postId)

        if (post.postStatus != PostStatus.PUBLIC && getMemberShipEntity(
                post.groupId,
                memberId
            ).status != MembershipStatus.APPROVED
        ) {
            throw PostException(PostErrorCode.POST_UNAUTHORIZATION)
        }

        val member = getMemberEntity(memberId)

        val documents = postAttachmentRepository
            .findByPostIdAndFileTypeAndDisabledOrderByCreatedAtDesc(postId, FileType.DOCUMENT, false)
            .map { PostAttachmentRespDto.GetPostDocumentDto.from(it) }

        val images = postAttachmentRepository
            .findByPostIdAndFileTypeAndDisabledOrderByCreatedAtDesc(postId, FileType.IMAGE, false)
            .map { PostAttachmentRespDto.GetPostImageDto.from(it, fileConfig.basE_DIR) }

        return PostRespDto.GetPostDto.from(post, member.id!!, member.nickname, images, documents, true)
    }

    @CustomCache(prefix = "post", key = "groupid", id = "groupId", ttl = 2)
    fun getTopFivePosts(groupId: Long): kotlin.collections.List<PostRespDto.GetPostListDto> =
        postRepository.findPostsByGroupIdOrderByTodayViewsCountDesc(groupId, 5, false)
            .map { PostRespDto.GetPostListDto.from(it) }


    fun getPostsBySearch(
        groupId: Long,
        search: String,
        postStatus: PostStatus,
        pageable: Pageable
    ): Page<PostRespDto.GetPostListDto> = postRepository
        .findAllBySearchStatus(groupId, search, postStatus, false, pageable)
        .map { PostRespDto.GetPostListDto.from(it) }

    fun getPostsByUser(
        searchPost: PostReqDto.SearchPostDto,
        pageable: Pageable,
        memberId: Long
    ): Page<PostRespDto.GetPostListDto> = postRepository
        .findAllByUserAndSearchStatus(
            searchPost.groupId,
            memberId,
            searchPost.search,
            searchPost.postStatus,
            false,
            pageable
        )
        .map { PostRespDto.GetPostListDto.from(it) }


    @Transactional
    fun savePost(memberId: Long, savePost: PostReqDto.SavePostDto, files: Array<MultipartFile?>?): Post {
        val membership = getMemberShipEntity(savePost.groupId, memberId)

        if (membership.status != MembershipStatus.APPROVED) {
            throw PostException(PostErrorCode.POST_UNAUTHORIZATION)
        }

        if (savePost.postStatus == PostStatus.NOTICE && membership.groupRole != GroupRole.LEADER) {
            throw PostException(PostErrorCode.POST_UNAUTHORIZATION)
        }

        val member = getMemberEntity(memberId)

        System.out.println(member.nickname)

        val post = postRepository.save(savePost.toEntity(memberId, member.nickname))

        saveFiles(files, post)

        return post
    }

    @Transactional
    @CustomCacheDelete(prefix = "post", key = "postid", id = "postId")
    fun updatePost(
        memberId: Long,
        postId: Long,
        modifyPost: PostReqDto.ModifyPostDto,
        files: Array<MultipartFile?>
    ): Post {
        val membership = getMemberShipEntity(modifyPost.groupId, memberId)
        val post = getPostEntity(postId)

        if (membership.status != MembershipStatus.APPROVED) {
            throw PostException(PostErrorCode.POST_UNAUTHORIZATION)
        }

        if (post.memberId != memberId && membership.groupRole != GroupRole.LEADER) {
            throw PostException(PostErrorCode.POST_UNAUTHORIZATION)
        }

        checkFileSize(files, modifyPost.oldFileSize, MAX_FILE_SIZE.toLong())

        post.title = modifyPost.title
        post.content = modifyPost.content
        post.postStatus = modifyPost.postStatus

        saveFiles(files, post)

        modifyPost.removeIdList?.takeIf { it.isNotEmpty() }?.let {
            postAttachmentRepository.deleteByIdList(it)
        }

        return post
    }

    @Transactional
    @CustomCacheDelete(prefix = "post", key = "postid", id = "postId")
    fun deletePost(memberId: Long, postId: Long) {
        val post = getPostEntity(postId)
        val membership = getMemberShipEntity(post.groupId, memberId)

        if (membership.status != MembershipStatus.APPROVED) {
            throw PostException(PostErrorCode.POST_UNAUTHORIZATION)
        }

        if (post.memberId != memberId && membership.groupRole != GroupRole.LEADER) {
            throw PostException(PostErrorCode.POST_UNAUTHORIZATION)
        }

        postAttachmentRepository.deleteByPostId(postId)
        post.delete()
    }

    fun getMemberEntity(memberId: Long): Member =
        memberRepository.findById(memberId).orElseThrow { PostException(GlobalErrorCode.ENTITY_NOT_FOUND) }


    fun getPostEntity(postId: Long): Post =
        postRepository.findByIdAndDisabled(postId, false)?: throw PostException(PostErrorCode.POST_NOT_FOUND)


    fun getMemberShipEntity(groupId: Long, memberId: Long): GroupMembership =
        groupMembershipRepository.findByGroupIdAndMemberId(groupId, memberId)
            .orElseThrow { GroupMembershipException(GroupMembershipErrorCode.GROUP_MEMBERSHIP_NOT_FOUND) }

    // 파일 크기 체크
    fun checkFileSize(files: Array<MultipartFile?>, oldSize: Long, maxSize: Long) {
        val newSize = files?.sumOf { it?.size ?: 0 } ?: 0

        if (oldSize + newSize > maxSize) {
            throw FileException(FileErrorCode.FILE_SIZE_EXCEEDED)
        }
    }

    // 파일 저장 및 롤백
    fun saveFiles(files: Array<MultipartFile?>?, post: Post) {
        if (files.isNullOrEmpty()) return

        val attachments = mutableListOf<PostAttachment>()
        val filePaths = mutableListOf<String>()

        try {
            for (file in files) {
                if (file == null) continue

                val filePath = fileService.saveFile(file);
                val fileName = FileUtil.getFileName(filePath);
                filePaths.add(fileConfig.getBASE_DIR() + "/" + filePath);
                attachments.add(
                    PostAttachment(
                        file.getOriginalFilename(),
                        fileName,
                        filePath,
                        file.getSize(),
                        file.getContentType(),
                        FileUtil.getFileType(fileName),
                        post.id!!
                    )
                )
            }

            postAttachmentRepository.saveAll(attachments)
        } catch (e: Exception) {
            if (!attachments.isEmpty()) {
                fileService.deleteFiles(filePaths)
            }
            throw e
        }
    }

    @Transactional
    fun PostLike(postId: Long, memberId: Long) {
        val post = postRepository.findByIdWithLock(postId)?: throw PostException(PostErrorCode.POST_NOT_FOUND)

        val member = memberRepository.findById(memberId)
            .orElseThrow { PostException(GlobalErrorCode.ENTITY_NOT_FOUND) }

        val postLike = postLikeRepository.findByPostAndMember(post, member)

        if (postLike != null) {
            postLike.delete()
            post.removeLikeCount()
        } else {
            postLikeRepository.save(PostLike(null, member, post))
            post.addLikeCount()
        }
    }

    fun isLiked(postId: Long, memberId: Long): Boolean {
        val post = postRepository.findById(postId)
            .orElseThrow { PostException(PostErrorCode.POST_NOT_FOUND) }

        val member = memberRepository.findById(memberId)
            .orElseThrow { PostException(GlobalErrorCode.ENTITY_NOT_FOUND) }

        return postLikeRepository.findByPostAndMember(post, member)?.let { !it.disabled } ?: false
    }
}