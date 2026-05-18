package com.impal.gabungyuk.notification.entity;

import java.time.LocalDateTime;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Entity
@Table(name = "notifications")
public class Notification {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "notification_id")
    private Long notificationId;

    @Column(name = "recipient_user_id", nullable = false)
    private Integer recipientUserId;

    @Column(name = "actor_user_id", nullable = true)
    private Integer actorUserId;

    @Column(name = "project_id", nullable = true)
    private Integer projectId;

    @Column(name = "collaboration_id", nullable = true)
    private Integer collaborationId;

    @Column(name = "type", nullable = false, length = 100)
    private String type;

    @Column(name = "title", nullable = false)
    private String title;

    @Column(name = "message", nullable = false, columnDefinition = "TEXT")
    private String message;

    @Column(name = "is_read", nullable = false)
    private Boolean isRead;

    @Column(name = "created_at", nullable = false)
    private LocalDateTime createdAt;

    @Column(name = "read_at", nullable = true)
    private LocalDateTime readAt;
}