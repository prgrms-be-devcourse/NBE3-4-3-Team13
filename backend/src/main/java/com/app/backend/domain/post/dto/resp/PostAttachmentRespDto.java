package com.app.backend.domain.post.dto.resp;

import com.app.backend.domain.attachment.entity.FileType;
import com.app.backend.domain.post.entity.PostAttachment;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;

public class PostAttachmentRespDto {

    @Getter
    @Builder(access = AccessLevel.PRIVATE)
    @AllArgsConstructor(access = AccessLevel.PRIVATE)
    public static class GetPostImageDto {
        private final Long attachmentId;
        private final String fileName;
        private final FileType fileType;
        private final String filePath;
        private final Long fileSize;
    }

    public static GetPostImageDto GetPostImage(final PostAttachment postAttachment,
                                               final String basePath)
    {
        return GetPostImageDto.builder()
                .attachmentId(postAttachment.getId())
                .fileName(postAttachment.getOriginalFileName())
                .fileType(postAttachment.getFileType())
                .filePath(basePath + "/" + postAttachment.getStoreFilePath())
                .fileSize(postAttachment.getFileSize())
                .build();
    }

    @Getter
    @Builder(access = AccessLevel.PRIVATE)
    @AllArgsConstructor(access = AccessLevel.PRIVATE)
    public static class GetPostDocumentDto {
        private final Long attachmentId;
        private final String fileName;
        private final FileType fileType;
        private final Long fileSize;
    }

    public static GetPostDocumentDto getPostDocument(final PostAttachment postAttachment){
        return GetPostDocumentDto.builder()
                .attachmentId(postAttachment.getId())
                .fileName(postAttachment.getOriginalFileName())
                .fileType(postAttachment.getFileType())
                .fileSize(postAttachment.getFileSize())
                .build();
    }
}

