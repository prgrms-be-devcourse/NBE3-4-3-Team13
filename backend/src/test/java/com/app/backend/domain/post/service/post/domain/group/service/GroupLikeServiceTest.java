package com.app.backend.domain.post.service.post.domain.group.service;

import com.app.backend.domain.category.entity.Category;
import com.app.backend.domain.category.repository.CategoryRepository;
import com.app.backend.domain.group.entity.Group;
import com.app.backend.domain.group.entity.RecruitStatus;
import com.app.backend.domain.group.repository.GroupLikeRepository;
import com.app.backend.domain.group.repository.GroupRepository;
import com.app.backend.domain.group.service.GroupLikeService;
import com.app.backend.domain.member.entity.Member;
import com.app.backend.domain.member.repository.MemberRepository;
import jakarta.transaction.Transactional;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest
@Transactional
public class GroupLikeServiceTest {

    @Autowired
    private GroupLikeService groupLikeService;

    @Autowired
    private GroupRepository groupRepository;

    @Autowired
    private MemberRepository memberRepository;

    @Autowired
    private GroupLikeRepository groupLikeRepository;

    @Autowired
    private CategoryRepository categoryRepository;

    @Test
    @DisplayName("그룹 좋아요 추가")
    void likeGroupTest() {
        // given
        Category category = categoryRepository.save(Category.builder().name("카테고리").build());
        Group group = groupRepository.save(Group.builder()
                .name("test group")
                .province("test province")
                .city("test city")
                .town("test town")
                .description("test description")
                .recruitStatus(RecruitStatus.RECRUITING)
                .maxRecruitCount(300)
                .category(category)
                .build());
        Member member = memberRepository.save(Member.builder().username("user0").build());

        Long memberId = member.getId();
        Long groupId = group.getId();

        // when
        groupLikeService.likeGroup(groupId, memberId);

        // then
        long likeCount = groupLikeRepository.countByGroupIdAndMemberId(groupId, memberId);
        assertThat(likeCount).isEqualTo(1);
    }

    @Test
    @DisplayName("그룹 좋아요 취소")
    void unlikeGroupTest() {
        // given
        Category category = categoryRepository.save(Category.builder().name("카테고리").build());
        Group group = groupRepository.save(Group.builder()
                .name("test group")
                .province("test province")
                .city("test city")
                .town("test town")
                .description("test description")
                .recruitStatus(RecruitStatus.RECRUITING)
                .maxRecruitCount(300)
                .category(category)
                .build());
        Member member = memberRepository.save(Member.builder().username("user0").build());

        Long memberId = member.getId();
        Long groupId = group.getId();

        groupLikeService.likeGroup(groupId, memberId);

        // when
        groupLikeService.unlikeGroup(groupId, memberId);

        // then
        long likeCount = groupLikeRepository.countByGroupIdAndMemberId(groupId, memberId);
        assertThat(likeCount).isEqualTo(0);
    }
}
