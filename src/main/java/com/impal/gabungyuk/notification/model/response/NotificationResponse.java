package com.impal.gabungyuk.notification.model.response;

import java.time.LocalDateTime;

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
public class NotificationResponse {

    private Long notificationId;

    private Integer recipientUserId;
    private Integer actorUserId;
    private Integer projectId;
    private Integer collaborationId;

    private String type;
    private String title;
    private String message;

    private Boolean isRead;

    private LocalDateTime createdAt;
    private LocalDateTime readAt;
}