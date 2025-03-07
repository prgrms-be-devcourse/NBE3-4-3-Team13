package com.app.backend.domain.post.service.post;

import static org.assertj.core.api.AssertionsForClassTypes.*;

import com.app.backend.domain.member.repository.MemberRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import com.app.backend.domain.member.entity.Member;
import com.app.backend.domain.post.entity.Post;
import com.app.backend.domain.post.entity.PostStatus;
import com.app.backend.domain.post.exception.PostErrorCode;
import com.app.backend.domain.post.exception.PostException;
import com.app.backend.domain.post.repository.post.PostRepository;
import org.springframework.transaction.annotation.Transactional;

@SpringBootTest
@Transactional
class PostLikeServiceTest {
	@Autowired
	private PostService postService;

	@Autowired
	private PostRepository postRepository;

	@Autowired
	private MemberRepository memberRepository;

	private Member testMember;
	private Post testPost;

	@BeforeEach
	void setUp() {
		testMember = memberRepository.save(Member.create("testUser","password","작성자","ROLE_USER",false, null,null));

		testPost = postRepository.save(Post.Companion.of("테스트 게시글", "테스트 내용", PostStatus.PUBLIC, 1L, testMember.getId(), testMember.getNickname()));
	}

	@Test
	@DisplayName("게시글 좋아요 추가 성공")
	void createPostLike() {
		// when
		postService.PostLike(testPost.getId(), testMember.getId());

		// then
		Post foundPost = postRepository.findById(testPost.getId()).get();
		assertThat(foundPost.getLikeCount()).isEqualTo(1);
		assertThat(postService.isLiked(testPost.getId(), testMember.getId())).isTrue();
	}

	@Test
	@DisplayName("삭제된 게시글에 좋아요 시도 시 실패")
	void createPostLike2() {
		// given
		testPost.delete();
		postRepository.save(testPost);

		// when & then
		assertThatThrownBy(() -> postService.PostLike(testPost.getId(), testMember.getId()))
			.isInstanceOf(PostException.class)
			.hasFieldOrPropertyWithValue("domainErrorCode", PostErrorCode.POST_NOT_FOUND);
	}

	@Test
	@DisplayName("여러 사용자의 좋아요 정합성 테스트")
	void createPostLike3() {
		// given
		int numberOfUsers = 10;
		for (int i = 0; i < numberOfUsers; i++) {
			Member user = memberRepository.save(Member.create("testUser" + i,"password","테스터" + i,"ROLE_USER",false, null,null));
			Member savedUser = memberRepository.save(user);

			// when
			postService.PostLike(testPost.getId(), savedUser.getId());
		}

		// then
		Post foundPost = postRepository.findById(testPost.getId()).get();
		assertThat(foundPost.getLikeCount()).isEqualTo(numberOfUsers);
	}

	@Test
	@DisplayName("동일 사용자의 연속 좋아요 정합성")
	void createPostLike4() {
		// given
		int toggleCount = 2;

		// when
		for (int i = 0; i < toggleCount; i++) {
			postService.PostLike(testPost.getId(), testMember.getId());
		}

		// then
		Post foundPost = postRepository.findById(testPost.getId()).get();
		assertThat(foundPost.getLikeCount()).isEqualTo(0);
		assertThat(postService.isLiked(testPost.getId(), testMember.getId())).isFalse();
	}


}
