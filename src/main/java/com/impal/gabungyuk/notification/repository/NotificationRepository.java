package com.impal.gabungyuk.notification.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;

import com.impal.gabungyuk.notification.entity.Notification;

public interface NotificationRepository extends JpaRepository<Notification, Long> {

    List<Notification> findByRecipientUserIdOrderByCreatedAtDesc(Integer recipientUserId);

    List<Notification> findByRecipientUserIdAndIsReadFalseOrderByCreatedAtDesc(Integer recipientUserId);

    Long countByRecipientUserIdAndIsReadFalse(Integer recipientUserId);
}