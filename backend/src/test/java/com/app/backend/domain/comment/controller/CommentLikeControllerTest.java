package com.app.backend.domain.comment.controller;

import static org.assertj.core.api.Assertions.*;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import com.app.backend.domain.comment.dto.response.CommentResponse;
import com.app.backend.domain.comment.entity.Comment;
import com.app.backend.domain.comment.repository.CommentRepository;
import com.app.backend.domain.comment.service.CommentService;
import com.app.backend.domain.member.entity.Member;
import com.app.backend.domain.member.entity.MemberDetails;
import com.app.backend.domain.member.repository.MemberRepository;
import com.app.backend.domain.post.entity.Post;
import com.app.backend.domain.post.entity.PostStatus;
import com.app.backend.domain.post.repository.post.PostRepository;
import com.app.backend.global.annotation.CustomWithMockUser;

@SpringBootTest
@AutoConfigureMockMvc
@Transactional
class CommentLikeControllerTest {

	@Autowired
	private MockMvc mockMvc;

	@Autowired
	private CommentRepository commentRepository;

	@Autowired
	private MemberRepository memberRepository;

	@Autowired
	private PostRepository postRepository;

	@Autowired
	private CommentService commentService;

	private Member testMember;
	private Post testPost;
	private Comment testComment;
	private MemberDetails memberDetails;

	@BeforeEach
	void setUp() {

		testMember = Member.create(
			"testUser",
			"password",
			"테스터",
			"USER",
			false,
			null,
			null
		);
		testMember = memberRepository.save(testMember);

		testPost = Post.Companion.of(
			"테스트 게시글",
			"테스트 내용",
			PostStatus.PUBLIC,
			1L,
			testMember.getId(),
			"테스트 닉"
		);
		testPost = postRepository.save(testPost);

		testComment = new Comment(
			null,
			"테스트 댓글",
			testPost,
			testMember,
			null,
			new ArrayList<>()
		);
		testComment = commentRepository.save(testComment);

		memberDetails = new MemberDetails(testMember);
	}

	@Test
	@DisplayName("댓글 좋아요 추가 성공")
	void createCommentLike() throws Exception {

		mockMvc.perform(post("/api/v1/comment/{id}/like", testComment.getId())
				.with(user(memberDetails)))
			.andDo(print())
			.andExpect(status().isOk())
			.andExpect(jsonPath("$.isSuccess").value(true))
			.andExpect(jsonPath("$.message").value("댓글 좋아요가 추가되었습니다."));


		Page<CommentResponse.CommentList> comments = commentService.getComments(testPost.getId(),
			testMember.getId(), PageRequest.of(0, 10));
		assertThat(comments.getContent().get(0).getLikeCount()).isEqualTo(1);
	}

	@Test
	@DisplayName("삭제된 댓글에 좋아요 시도 시 실패")
	void createCommentLike2() throws Exception {

		testComment.delete();
		commentRepository.save(testComment);


		mockMvc.perform(post("/api/v1/comment/{id}/like", testComment.getId())
				.with(user(memberDetails)))
			.andDo(print())
			.andExpect(status().isNotFound())
			.andExpect(jsonPath("$.isSuccess").value(false))
			.andExpect(jsonPath("$.code").value("CM001"));
	}


	@Test
	@DisplayName("여러 사용자의 좋아요 정합성 테스트")
	@CustomWithMockUser(role="USER")
	void testMultipleUserLikes() throws Exception {

		Comment testComment = new Comment(
			null,
			"테스트 댓글",
			testPost,
			testMember,
			null,
			new ArrayList<>()
		);
		commentRepository.save(testComment);


		int numberOfUsers = 10;
		for (int i = 0; i < numberOfUsers; i++) {
			Member user = Member.create(
				"testUser" + i,
				null,
				"테스터" + i,
				"ROLE_USER",
				false,
				null,
				null
			);
			memberRepository.save(user);
		}


		mockMvc.perform(get("/api/v1/comment/" + testPost.getId())
				.with(user(memberDetails)))
			.andExpect(status().isOk())
			.andExpect(jsonPath("$.data.content[0].likeCount").value(numberOfUsers));
	}


	@Test
	@DisplayName("동일 사용자의 연속 좋아요 정합성")
	void getCommentLikeCount3() throws Exception {

		int toggleCount = 2;

		for (int i = 0; i < toggleCount; i++) {
			mockMvc.perform(post("/api/v1/comment/{id}/like", testComment.getId())
					.with(user(memberDetails)))
				.andExpect(status().isOk())
				.andExpect(jsonPath("$.isSuccess").value(true));
		}


		Page<CommentResponse.CommentList> comments = commentService.getComments(testPost.getId(),
			testMember.getId(), PageRequest.of(0, 10));
		assertThat(comments.getContent().get(0).getLikeCount()).isEqualTo(0);
	}

	@Test
	@DisplayName("여러 사용자가 동시에 좋아요를 누를 때 정합성 테스트")
	@Transactional(propagation = Propagation.NOT_SUPPORTED)
	void getCommentLikeCount4() throws Exception {

		int numberOfUsers = 3;
		List<Member> users = new ArrayList<>();


		for (int i = 0; i < numberOfUsers; i++) {
			Member user = Member.create(
				"testUser" + i,
				"password",
				"테스터" + i,
				"ROLE_USER",
				false,
				null,
				null
			);
			users.add(memberRepository.save(user));
		}

		CountDownLatch startSignal = new CountDownLatch(1);
		CountDownLatch endSignal = new CountDownLatch(numberOfUsers);
		ExecutorService executorService = Executors.newFixedThreadPool(numberOfUsers);


		for (Member user : users) {
			executorService.submit(() -> {
				try {
					startSignal.await();
					mockMvc.perform(post("/api/v1/comment/{id}/like", testComment.getId())
							.with(user(new MemberDetails(user))))
						.andExpect(status().isOk());
				} catch (Exception e) {
					System.out.println("에러: " + e.getMessage());
				} finally {
					endSignal.countDown();
				}
			});
		}

		startSignal.countDown();
		endSignal.await();
		executorService.shutdown();

		Page<CommentResponse.CommentList> comments = commentService.getComments(testPost.getId(),
			testMember.getId(), PageRequest.of(0, 10));
		assertThat(comments.getContent().get(0).getLikeCount()).isEqualTo(numberOfUsers);
		System.out.println("최종 좋아요 수: " + comments.getContent().get(0).getLikeCount());
	}

}