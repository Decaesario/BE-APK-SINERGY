package com.impal.gabungyuk.notification.model.response;

import java.util.List;

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
public class UnreadNotificationResponse {

    private Long unreadCount;
    private List<NotificationResponse> notifications;
}