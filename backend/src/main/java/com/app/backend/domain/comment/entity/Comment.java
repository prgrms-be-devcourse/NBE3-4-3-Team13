package com.app.backend.domain.comment.entity;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

import com.app.backend.domain.member.entity.Member;
import com.app.backend.domain.post.entity.Post;
import com.app.backend.global.entity.BaseEntity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.OneToMany;
import jakarta.persistence.Table;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Entity
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor
@Builder
@Table(name = "tbl_comments")
public class Comment extends BaseEntity {

	@Id
	@Column(name = "comment_id")
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private Long id;


	@Column(nullable = false)
	private String content;

	@ManyToOne
	@JoinColumn(name = "post_id")
	private Post post;

	@ManyToOne
	@JoinColumn(name = "member_id")
	private Member member;

	@ManyToOne(fetch = FetchType.LAZY)
	@JoinColumn(name = "parent_id")
	private Comment parent;

	@Builder.Default
	@OneToMany(mappedBy = "parent")
	private List<Comment> children = new ArrayList<>();


	public void delete() {
		this.deactivate();
	}

	public void update(String content) {
		this.content = content;
	}

	public void addReply(Comment reply) {
		this.children.add(reply);
		reply.parent = this;
	}

	public void removeReply(Comment reply) {
		this.children.remove(reply);
		reply.parent = null;
	}

	public List<Comment> getChildren() {
		return this.children.stream()
			.filter(child -> !child.getDisabled())
			.collect(Collectors.toList());
	}
}
