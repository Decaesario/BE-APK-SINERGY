package com.impal.gabungyuk.notification.controller;

import java.util.List;

import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.*;

import com.impal.gabungyuk.core.model.SuccessResponse;
import com.impal.gabungyuk.notification.model.response.NotificationResponse;
import com.impal.gabungyuk.notification.model.response.UnreadNotificationResponse;
import com.impal.gabungyuk.notification.service.NotificationService;

@RestController
public class NotificationController {

    private final NotificationService notificationService;

    public NotificationController(NotificationService notificationService) {
        this.notificationService = notificationService;
    }

    @GetMapping(
            value = "/api/v1/notifications",
            produces = MediaType.APPLICATION_JSON_VALUE
    )
    public SuccessResponse<List<NotificationResponse>> getMyNotifications(
            @RequestHeader("Authorization") String authorizationHeader
    ) {
        List<NotificationResponse> response = notificationService.getMyNotifications(authorizationHeader);

        return SuccessResponse.<List<NotificationResponse>>builder()
                .status(200)
                .message("Notifications retrieved successfully")
                .data(response)
                .build();
    }

    @GetMapping(
            value = "/api/v1/notifications/unread",
            produces = MediaType.APPLICATION_JSON_VALUE
    )
    public SuccessResponse<UnreadNotificationResponse> getMyUnreadNotifications(
            @RequestHeader("Authorization") String authorizationHeader
    ) {
        UnreadNotificationResponse response = notificationService.getMyUnreadNotifications(authorizationHeader);

        return SuccessResponse.<UnreadNotificationResponse>builder()
                .status(200)
                .message("Unread notifications retrieved successfully")
                .data(response)
                .build();
    }

    @PatchMapping(
            value = "/api/v1/notifications/{notificationId}/read",
            produces = MediaType.APPLICATION_JSON_VALUE
    )
    public SuccessResponse<NotificationResponse> markAsRead(
            @RequestHeader("Authorization") String authorizationHeader,
            @PathVariable Long notificationId
    ) {
        NotificationResponse response = notificationService.markAsRead(
                authorizationHeader,
                notificationId
        );

        return SuccessResponse.<NotificationResponse>builder()
                .status(200)
                .message("Notification marked as read successfully")
                .data(response)
                .build();
    }

    @PatchMapping(
            value = "/api/v1/notifications/read-all",
            produces = MediaType.APPLICATION_JSON_VALUE
    )
    public SuccessResponse<List<NotificationResponse>> markAllAsRead(
            @RequestHeader("Authorization") String authorizationHeader
    ) {
        List<NotificationResponse> response = notificationService.markAllAsRead(authorizationHeader);

        return SuccessResponse.<List<NotificationResponse>>builder()
                .status(200)
                .message("All notifications marked as read successfully")
                .data(response)
                .build();
    }
}