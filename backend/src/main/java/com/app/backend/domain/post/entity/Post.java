package com.app.backend.domain.post.entity;

import com.app.backend.global.entity.BaseEntity;
import jakarta.persistence.*;
import lombok.*;

import java.util.ArrayList;
import java.util.List;

@Entity
@Getter
@Builder
@Table(name = "tbl_posts")
@AllArgsConstructor
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class Post extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "post_id")
    private Long id;

    @Setter
    @Column(nullable = false)
    private String title;

    @Setter
    @Column(nullable = false)
    private String content;

    @Setter
    @Column(nullable = false)
    @Enumerated(EnumType.STRING)
    private PostStatus postStatus;

    @Setter
    @Column(nullable = false)
    private Long memberId;

    @Setter
    @Column(nullable = false)
    private String nickName;

//    Member Entity 연관관계
//    @JoinColumn(name = "member_id")
//    @ManyToOne(fetch = FetchType.LAZY)
//    private Member member;

    @Setter
    @Column(nullable = false)
    private Long groupId;

//    Group Entity 연관관계
//    @JoinColumn(name = "group_id")
//    @ManyToOne(fetch = FetchType.LAZY)
//    private Group group;

    @OneToMany(mappedBy = "post")
    @Builder.Default
    private List<PostLike> likes = new ArrayList<>();

    @Builder.Default
    @Column(nullable = false)
    private int likeCount = 0;

    @Builder.Default
    @Column(nullable = false)
    private Long todayViewCount = 0L;

    @Builder.Default
    @Column(nullable = false)
    private Long totalViewCount = 0L;

    public void addTodayViewCount(Long viewCount) {
        this.todayViewCount += viewCount;
    }

    public void refreshViewCount() {
        totalViewCount += todayViewCount;
        todayViewCount = 0L;
    }

    public void delete(){
        if(!this.getDisabled()){
            deactivate();
        }
    }

    public void addLikeCount() {
        this.likeCount++;
    }

    public void removeLikeCount() {
        if (this.likeCount > 0) {
            this.likeCount--;
        }
    }

}
