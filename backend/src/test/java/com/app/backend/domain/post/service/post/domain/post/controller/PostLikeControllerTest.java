package com.app.backend.domain.post.service.post.domain.post.controller;

import com.app.backend.domain.member.entity.Member;
import com.app.backend.domain.member.entity.MemberDetails;
import com.app.backend.domain.member.repository.MemberRepository;
import com.app.backend.domain.post.entity.Post;
import com.app.backend.domain.post.entity.PostStatus;
import com.app.backend.domain.post.repository.post.PostLikeRepository;
import com.app.backend.domain.post.repository.post.PostRepository;
import com.app.backend.domain.post.service.post.PostService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.transaction.annotation.Transactional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.user;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
@Transactional
public class PostLikeControllerTest {

	@Autowired
	private MockMvc mockMvc;

	@Autowired
	private PostRepository postRepository;

	@Autowired
	private MemberRepository memberRepository;

	@Autowired
	private PostLikeRepository postLikeRepository;

	@Autowired
	private PostService postService;

	private Member testMember;
	private Post testPost;
	private MemberDetails memberDetails;
	private static final String BASE_URL = "/api/v1/post";

	@BeforeEach
	void setUp() {
		testMember = Member.builder()
			.username("testUser")
			.password("password")
			.nickname("테스터")
			.role("ROLE_USER")
			.disabled(false)
			.build();
		testMember = memberRepository.save(testMember);

		testPost = Post.builder()
			.title("테스트 게시글")
			.content("테스트 내용")
			.memberId(testMember.getId())
			.nickName(testMember.getNickname())
			.postStatus(PostStatus.PUBLIC)
			.groupId(1L)
			.build();
		testPost = postRepository.save(testPost);

		memberDetails = new MemberDetails(testMember);
	}

	@Test
	@DisplayName("게시글 좋아요 토글 성공")
	void createPostLike() throws Exception {
		mockMvc.perform(post(BASE_URL + "/{postId}/like", testPost.getId())
				.with(user(memberDetails))
				.contentType(MediaType.APPLICATION_JSON))
			.andDo(print())
			.andExpect(status().isOk())
			.andExpect(jsonPath("$.message").value("게시글 좋아요 토글 성공"));

		boolean isLiked = postService.isLiked(testPost.getId(), testMember.getId());
		assertThat(isLiked).isTrue();
	}

	@Test
	@DisplayName("삭제된 게시글 좋아요 시도 실패")
	void createPostLike2() throws Exception {
		postRepository.delete(testPost);

		mockMvc.perform(post(BASE_URL + "/{postId}/like", testPost.getId())
				.with(user(memberDetails))
				.contentType(MediaType.APPLICATION_JSON))
			.andDo(print())
			.andExpect(status().isNotFound())
			.andExpect(jsonPath("$.message").value("게시물 정보가 존재하지 않음"));
	}


	@Test
	@DisplayName("좋아요 상태 확인 성공")
	void getPostLike() throws Exception {
		postService.PostLike(testPost.getId(), testMember.getId());

		mockMvc.perform(get(BASE_URL + "/{postId}/like", testPost.getId())
				.with(user(memberDetails))
				.contentType(MediaType.APPLICATION_JSON))
			.andDo(print())
			.andExpect(status().isOk())
			.andExpect(jsonPath("$.message").value("좋아요 여부 확인 성공"))
			.andExpect(jsonPath("$.data").value(true));
	}

}