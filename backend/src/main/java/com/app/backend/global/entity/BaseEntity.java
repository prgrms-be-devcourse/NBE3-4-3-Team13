package com.app.backend.global.entity;

import jakarta.persistence.Column;
import jakarta.persistence.EntityListeners;
import jakarta.persistence.MappedSuperclass;
import lombok.Getter;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.LastModifiedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import java.time.LocalDateTime;

@MappedSuperclass
@EntityListeners(AuditingEntityListener.class)
@Getter
public class BaseEntity {

    @CreatedDate
    @Column(nullable = false, updatable = false)
    public LocalDateTime createdAt;

    @LastModifiedDate
    @Column(nullable = false)
    public LocalDateTime modifiedAt;

    @Column(nullable = false)
    public Boolean disabled = false;

    public void activate() {
        disabled = false;
    }

    public void deactivate() {
        disabled = true;
    }

}
