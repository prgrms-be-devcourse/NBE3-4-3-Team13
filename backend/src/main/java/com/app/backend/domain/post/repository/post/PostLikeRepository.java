package com.app.backend.domain.post.repository.post;

import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import com.app.backend.domain.member.entity.Member;
import com.app.backend.domain.post.entity.Post;
import com.app.backend.domain.post.entity.PostLike;

import jakarta.persistence.LockModeType;

public interface PostLikeRepository extends JpaRepository<PostLike, Long> {

	@Lock(LockModeType.PESSIMISTIC_WRITE)
	Optional<PostLike> findByPostAndMember(Post post, Member member);


}
