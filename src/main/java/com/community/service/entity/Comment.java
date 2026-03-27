package com.community.service.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Entity
@Table(
    name = "comments",
    indexes = {
        @Index(name = "idx_comments_target", columnList = "target_type, target_id"),
        @Index(name = "idx_comments_user", columnList = "user_id"),
        @Index(name = "idx_comments_created", columnList = "created_at")
    }
)
public class Comment {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    @Column(name = "target_type", nullable = false, length = 50)
    private String targetType; // 评论对象类型：activity, demand, post等

    @Column(name = "target_id", nullable = false)
    private Long targetId; // 评论对象ID

    @Column(columnDefinition = "TEXT", nullable = false)
    private String content;

    @Column(name = "parent_id")
    private Long parentId; // 父评论ID，用于回复功能

    @Builder.Default
    @Column(name = "created_at", nullable = false)
    private LocalDateTime createdAt = LocalDateTime.now();

    @Builder.Default
    @Column(name = "likes_count", nullable = false)
    private Integer likesCount = 0;
}
