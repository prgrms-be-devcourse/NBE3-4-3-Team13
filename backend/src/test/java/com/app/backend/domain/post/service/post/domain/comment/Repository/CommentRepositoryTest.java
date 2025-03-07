package com.app.backend.domain.post.service.post.domain.comment.Repository;

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
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.transaction.annotation.Transactional;

import static org.assertj.core.api.Assertions.assertThat;

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
		// given

		// 게시글 생성
		Post post = postRepository.save(Post.builder()
			.title("테스트 게시글")
			.content("테스트 내용")
			.memberId(1L)
			.nickName("작성자")
			.postStatus(PostStatus.PUBLIC)
			.groupId(1L)
			.build());

		// 댓글 생성
		Member member = memberRepository.save(Member.builder()
			.username("testUser")
			.password("password")
			.nickname("테스터")
			.role("ROLE_USER")
			.disabled(false)
			.build());

		Comment comment = commentRepository.save(Comment.builder()
			.post(post)
			.member(member)
			.content("테스트 댓글")
			.build());

		// 댓글 좋아요 생성
		commentLikeRepository.save(CommentLike.builder()
			.comment(comment)
			.member(member)
			.build());


		// when
		Page<CommentResponse.CommentList> result = commentRepository
			.findCommentsWithLikeCount(post, member.getId(), PageRequest.of(0, 10));

		// then
		assertThat(result).isNotEmpty();
		CommentResponse.CommentList firstComment = result.getContent().get(0);
		assertThat(firstComment.getId()).isEqualTo(comment.getId());
		assertThat(firstComment.getContent()).isEqualTo(comment.getContent());
		assertThat(firstComment.getLikeCount()).isEqualTo(1);
		assertThat(firstComment.isLiked()).isTrue();
		assertThat(firstComment.getMemberId()).isEqualTo(member.getId());
		assertThat(firstComment.getNickname()).isEqualTo(member.getNickname());
	}
}