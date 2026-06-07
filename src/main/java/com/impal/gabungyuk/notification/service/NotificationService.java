package com.impal.gabungyuk.notification.service;

import java.time.LocalDateTime;
import java.util.List;

import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

import com.impal.gabungyuk.core.service.TokenService;
import com.impal.gabungyuk.core.service.TimezoneService;
import com.impal.gabungyuk.notification.entity.Notification;
import com.impal.gabungyuk.notification.model.response.NotificationResponse;
import com.impal.gabungyuk.notification.model.response.UnreadNotificationResponse;
import com.impal.gabungyuk.notification.repository.NotificationRepository;

@Service
public class NotificationService {

    private final NotificationRepository notificationRepository;
    private final TokenService tokenService;
    private final TimezoneService timezoneService;

    public NotificationService(
            NotificationRepository notificationRepository,
            TokenService tokenService,
            TimezoneService timezoneService
    ) {
        this.notificationRepository = notificationRepository;
        this.tokenService = tokenService;
        this.timezoneService = timezoneService;
    }

    public Notification createNotification(
            Integer recipientUserId,
            Integer actorUserId,
            Integer projectId,
            Integer collaborationId,
            String type,
            String title,
            String message
    ) {
        Notification notification = Notification.builder()
                .recipientUserId(recipientUserId)
                .actorUserId(actorUserId)
                .projectId(projectId)
                .collaborationId(collaborationId)
                .type(type)
                .title(title)
                .message(message)
                .isRead(false)
                .createdAt(LocalDateTime.now())
                .readAt(null)
                .build();

        return notificationRepository.save(notification);
    }

    public List<NotificationResponse> getMyNotifications(String authorizationHeader) {
        Integer userId = tokenService.extractUserIdFromAuthorizationHeader(authorizationHeader);
        String viewerTz = timezoneService.getUserTimezoneOrDefault(userId);

        return notificationRepository.findByRecipientUserIdOrderByCreatedAtDesc(userId)
                .stream()
                .map(notification -> mapToResponse(notification, viewerTz))
                .toList();
    }

    public UnreadNotificationResponse getMyUnreadNotifications(String authorizationHeader) {
        Integer userId = tokenService.extractUserIdFromAuthorizationHeader(authorizationHeader);
        String viewerTz = timezoneService.getUserTimezoneOrDefault(userId);

        Long unreadCount = notificationRepository.countByRecipientUserIdAndIsReadFalse(userId);

        List<NotificationResponse> notifications = notificationRepository
                .findByRecipientUserIdAndIsReadFalseOrderByCreatedAtDesc(userId)
                .stream()
                .map(notification -> mapToResponse(notification, viewerTz))
                .toList();

        return UnreadNotificationResponse.builder()
                .unreadCount(unreadCount)
                .notifications(notifications)
                .build();
    }

    public NotificationResponse markAsRead(String authorizationHeader, Long notificationId) {
        Integer userId = tokenService.extractUserIdFromAuthorizationHeader(authorizationHeader);

        Notification notification = notificationRepository.findById(notificationId)
                .orElseThrow(() -> new ResponseStatusException(
                        HttpStatus.NOT_FOUND,
                        "Notification not found"
                ));

        if (!notification.getRecipientUserId().equals(userId)) {
            throw new ResponseStatusException(
                    HttpStatus.FORBIDDEN,
                    "You are not allowed to access this notification"
            );
        }

        notification.setIsRead(true);
        notification.setReadAt(LocalDateTime.now());

        Notification updated = notificationRepository.save(notification);

        String viewerTz = timezoneService.getUserTimezoneOrDefault(userId);
        return mapToResponse(updated, viewerTz);
    }

    public List<NotificationResponse> markAllAsRead(String authorizationHeader) {
        Integer userId = tokenService.extractUserIdFromAuthorizationHeader(authorizationHeader);
        String viewerTz = timezoneService.getUserTimezoneOrDefault(userId);

        List<Notification> unreadNotifications = notificationRepository
                .findByRecipientUserIdAndIsReadFalseOrderByCreatedAtDesc(userId);

        LocalDateTime now = LocalDateTime.now();

        unreadNotifications.forEach(notification -> {
            notification.setIsRead(true);
            notification.setReadAt(now);
        });

        List<Notification> savedNotifications = notificationRepository.saveAll(unreadNotifications);

        return savedNotifications.stream()
                .map(notification -> mapToResponse(notification, viewerTz))
                .toList();
    }

    public void notifyCollaborationRequest(
            Integer ownerUserId,
            Integer requesterUserId,
            Integer projectId,
            Integer collaborationId,
            String requesterName,
            String projectTitle
    ) {
        createNotification(
                ownerUserId,
                requesterUserId,
                projectId,
                collaborationId,
                "COLLABORATION_REQUEST",
                "New Collaboration Request",
                requesterName + " requested to join your project " + projectTitle + "."
        );
    }

    public void notifyCollaborationAccepted(
            Integer requesterUserId,
            Integer ownerUserId,
            Integer projectId,
            Integer collaborationId,
            String projectTitle
    ) {
        createNotification(
                requesterUserId,
                ownerUserId,
                projectId,
                collaborationId,
                "COLLABORATION_ACCEPTED",
                "Collaboration Request Accepted",
                "Your request to join project " + projectTitle + " has been accepted."
        );
    }

    public void notifyCollaborationDeclined(
            Integer requesterUserId,
            Integer ownerUserId,
            Integer projectId,
            Integer collaborationId,
            String projectTitle
    ) {
        createNotification(
                requesterUserId,
                ownerUserId,
                projectId,
                collaborationId,
                "COLLABORATION_DECLINED",
                "Collaboration Request Declined",
                "Your request to join project " + projectTitle + " has been declined."
        );
    }

    public void notifyProjectDescriptionUpdated(
            Integer recipientUserId,
            Integer ownerUserId,
            Integer projectId,
            String projectTitle
    ) {
        createNotification(
                recipientUserId,
                ownerUserId,
                projectId,
                null,
                "PROJECT_DESCRIPTION_UPDATED",
                "Project Description Updated",
                "The description of project " + projectTitle + " has been updated by the owner."
        );
    }

    public void notifyProjectDeadlineUpdated(
            Integer recipientUserId,
            Integer ownerUserId,
            Integer projectId,
            String projectTitle,
            LocalDateTime newDeadline
    ) {
        createNotification(
                recipientUserId,
                ownerUserId,
                projectId,
                null,
                "PROJECT_DEADLINE_UPDATED",
                "Project Deadline Updated",
                "The deadline of project " + projectTitle + " has been changed to " + newDeadline + "."
        );
    }

    public void notifyProjectDeadlineReminder(
            Integer recipientUserId,
            Integer projectId,
            String reminderType,
            String projectTitle,
            LocalDateTime deadline
    ) {
        createNotification(
                recipientUserId,
                null,
                projectId,
                null,
                "PROJECT_DEADLINE_" + reminderType,
                "Project Deadline Reminder",
                "Project " + projectTitle + " deadline is " + reminderType + " days away. Deadline: " + deadline + "."
        );
    }

    public void notifyProjectDeadlineOverdue(
            Integer recipientUserId,
            Integer projectId,
            String projectTitle,
            LocalDateTime deadline
    ) {
        createNotification(
                recipientUserId,
                null,
                projectId,
                null,
                "PROJECT_DEADLINE_OVERDUE",
                "Project Deadline Overdue",
                "Project " + projectTitle + " has passed its deadline. Please update the deadline if the project is still active. Deadline: " + deadline + "."
        );
    }

    private NotificationResponse mapToResponse(Notification notification, String viewerTimezone) {
        return NotificationResponse.builder()
                .notificationId(notification.getNotificationId())
                .recipientUserId(notification.getRecipientUserId())
                .actorUserId(notification.getActorUserId())
                .projectId(notification.getProjectId())
                .collaborationId(notification.getCollaborationId())
                .type(notification.getType())
                .title(notification.getTitle())
                .message(notification.getMessage())
                .isRead(notification.getIsRead())
                .createdAt(timezoneService.convertToUserZone(notification.getCreatedAt(), viewerTimezone))
                .readAt(timezoneService.convertToUserZone(notification.getReadAt(), viewerTimezone))
                .build();
    }
}
