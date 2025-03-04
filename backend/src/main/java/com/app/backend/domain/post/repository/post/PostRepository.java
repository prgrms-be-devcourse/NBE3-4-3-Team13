package com.app.backend.domain.post.repository.post;

import com.app.backend.domain.post.entity.Post;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;

import java.util.Optional;

import jakarta.persistence.LockModeType;

public interface PostRepository extends JpaRepository<Post, Long>, PostRepositoryCustom {

    Optional<Post> findByIdAndDisabled(Long id, Boolean disabled);


    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("SELECT p FROM Post p WHERE p.id = :postId AND p.disabled = false")
    Optional<Post> findByIdWithLock(Long postId);

}
