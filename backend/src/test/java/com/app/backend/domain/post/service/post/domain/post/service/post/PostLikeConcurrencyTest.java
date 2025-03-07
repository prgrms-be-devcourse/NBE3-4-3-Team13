package com.app.backend.domain.post.service.post.domain.post.service.post;

import com.app.backend.domain.member.entity.Member;
import com.app.backend.domain.member.repository.MemberRepository;
import com.app.backend.domain.post.entity.Post;
import com.app.backend.domain.post.entity.PostStatus;
import com.app.backend.domain.post.repository.post.PostLikeRepository;
import com.app.backend.domain.post.repository.post.PostRepository;
import com.app.backend.domain.post.service.post.PostService;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest
class PostLikeConcurrencyTest {

	@Autowired
	private PostService postService;

	@Autowired
	private PostRepository postRepository;

	@Autowired
	private MemberRepository memberRepository;

	@Autowired
	private PostLikeRepository postLikeRepository;

	@AfterEach
	void tearDown() {
		postLikeRepository.deleteAll();
		postRepository.deleteAll();
		memberRepository.deleteAll();
	}

	private Member testMember;
	private Post testPost;

	@BeforeEach
	void setUp() {
		testMember = memberRepository.save(Member.builder()
			.username("testUser")
			.password("password")
			.nickname("작성자")
			.role("ROLE_USER")
			.disabled(false)
			.build());

		testPost = postRepository.save(Post.builder()
			.title("테스트 게시글")
			.content("테스트 내용")
			.memberId(testMember.getId())
			.nickName(testMember.getNickname())
			.postStatus(PostStatus.PUBLIC)
			.groupId(1L)
			.build());
	}

	@Test
	@DisplayName("여러 사용자가 동시에 게시글 좋아요를 누를 때 정합성 테스트")
	void createMultiPostLike() throws InterruptedException {

		int numberOfUsers = 10;
		List<Member> users = new ArrayList<>();

		for (int i = 0; i < numberOfUsers; i++) {
			Member user = memberRepository.save(Member.builder()
				.username("testUser" + i)
				.password("password")
				.nickname("테스터" + i)
				.role("ROLE_USER")
				.disabled(false)
				.build());
			users.add(user);
		}

		CountDownLatch startSignal = new CountDownLatch(1);
		CountDownLatch endSignal = new CountDownLatch(numberOfUsers);
		ExecutorService executorService = Executors.newFixedThreadPool(numberOfUsers);

		for (Member user : users) {
			executorService.submit(() -> {
				try {
					startSignal.await();
					postService.PostLike(testPost.getId(), user.getId());
				} catch (Exception e) {
					System.err.println("Error: " + e.getMessage());
				} finally {
					endSignal.countDown();
				}
			});
		}

		startSignal.countDown();
		endSignal.await();
		executorService.shutdown();

		Post foundPost = postRepository.findById(testPost.getId()).get();
		assertThat(foundPost.getLikeCount()).isEqualTo(numberOfUsers);
		System.out.println("좋아요 수: " + foundPost.getLikeCount());
	}
}
