package com.app.backend.domain.comment.Repository;

import static org.assertj.core.api.Assertions.*;

import java.util.ArrayList;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.transaction.annotation.Transactional;

import com.app.backend.domain.comment.dto.response.CommentResponse;
import com.app.backend.domain.comment.entity.Comment;
import com.app.backend.domain.comment.entity.CommentLike;
import com.app.backend.domain.comment.repository.CommentLikeRepository;
import com.app.backend.domain.comment.repository.CommentRepository;
import com.app.backend.domain.member.entity.Member;
import com.app.backend.domain.member.repository.MemberRepository;
import com.app.backend.domain.post.entity.Post;
import com.app.backend.domain.post.entity.PostStatus;
import com.app.backend.domain.post.repository.post.PostRepository;

@SpringBootTest
@AutoConfigureMockMvc
@Transactional
class CommentRepositoryTest {

	@Autowired
	private CommentRepository commentRepository;

	@Autowired
	private PostRepository postRepository;

	@Autowired
	private MemberRepository memberRepository;

	@Autowired
	private CommentLikeRepository commentLikeRepository;

	@Test
	@DisplayName("게시글의 댓글 목록을 조회할 수 있다(좋아요 개수, 좋아요 여부 포함)")
	void findCommentsWithLikeCountTest() {

		// 게시글 생성
		Post post = postRepository.save(Post.Companion.of(
			"테스트 게시글",
			"테스트 내용",
			PostStatus.PUBLIC,
			1L,
			1L,
			"작성자"
		));


		Member member = memberRepository.save(Member.create(
			"testUser",
			"password",
			"테스터",
			"ROLE_USER",
			false,
			null,
			null
		));

		Comment comment = commentRepository.save(new Comment(
			null,
			"테스트 댓글",
			post,
			member,
			null,
			new ArrayList<>()
		));

		// 댓글 좋아요 생성
		commentLikeRepository.save(new CommentLike(
			null,
			comment,
			member
		));


		// when
		Page<CommentResponse.CommentList> result = commentRepository
			.findCommentsWithLikeCount(post, member.getId(), PageRequest.of(0, 10));

		// then
		assertThat(result).isNotEmpty();
		CommentResponse.CommentList firstComment = result.getContent().get(0);
		assertThat(firstComment.getId()).isEqualTo(comment.getId());
		assertThat(firstComment.getContent()).isEqualTo(comment.getContent());
		assertThat(firstComment.getLikeCount()).isEqualTo(1);
		assertThat(firstComment.getLiked()).isTrue();
		assertThat(firstComment.getMemberId()).isEqualTo(member.getId());
		assertThat(firstComment.getNickname()).isEqualTo(member.getNickname());
	}
}