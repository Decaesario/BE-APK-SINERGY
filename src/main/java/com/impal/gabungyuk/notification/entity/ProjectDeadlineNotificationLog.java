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
@Table(name = "project_deadline_notification_logs")
public class ProjectDeadlineNotificationLog {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "deadline_log_id")
    private Long deadlineLogId;

    @Column(name = "project_id", nullable = false)
    private Integer projectId;

    @Column(name = "recipient_user_id", nullable = false)
    private Integer recipientUserId;

    @Column(name = "reminder_type", nullable = false, length = 50)
    private String reminderType;

    @Column(name = "deadline_snapshot", nullable = false)
    private LocalDateTime deadlineSnapshot;

    @Column(name = "sent_at", nullable = false)
    private LocalDateTime sentAt;
}