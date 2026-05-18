package com.impal.gabungyuk.notification.service;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
import java.util.List;

import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

import com.impal.gabungyuk.collaboration.entity.Collaboration;
import com.impal.gabungyuk.collaboration.repository.CollaborationRepository;
import com.impal.gabungyuk.notification.entity.ProjectDeadlineNotificationLog;
import com.impal.gabungyuk.notification.repository.ProjectDeadlineNotificationLogRepository;
import com.impal.gabungyuk.project.entity.Project;
import com.impal.gabungyuk.project.respository.ProjectRepository;

@Service
public class ProjectDeadlineNotificationScheduler {

    private final ProjectRepository projectRepository;
    private final CollaborationRepository collaborationRepository;
    private final NotificationService notificationService;
    private final ProjectDeadlineNotificationLogRepository deadlineLogRepository;

    public ProjectDeadlineNotificationScheduler(
            ProjectRepository projectRepository,
            CollaborationRepository collaborationRepository,
            NotificationService notificationService,
            ProjectDeadlineNotificationLogRepository deadlineLogRepository
    ) {
        this.projectRepository = projectRepository;
        this.collaborationRepository = collaborationRepository;
        this.notificationService = notificationService;
        this.deadlineLogRepository = deadlineLogRepository;
    }

    @Scheduled(cron = "0 0 8 * * *")
    public void checkProjectDeadlineReminders() {
        LocalDate today = LocalDate.now();

        List<Project> projects = projectRepository.findAll();

        for (Project project : projects) {
            if (project.getDeadline() == null) {
                continue;
            }

            LocalDate deadlineDate = project.getDeadline().toLocalDate();
            long daysUntilDeadline = ChronoUnit.DAYS.between(today, deadlineDate);

            if (daysUntilDeadline == 30) {
                sendDeadlineReminder(project, "H30", "30");
            } else if (daysUntilDeadline == 14) {
                sendDeadlineReminder(project, "H14", "14");
            } else if (daysUntilDeadline == 7) {
                sendDeadlineReminder(project, "H7", "7");
            } else if (daysUntilDeadline == 3) {
                sendDeadlineReminder(project, "H3", "3");
            } else if (daysUntilDeadline == 2) {
                sendDeadlineReminder(project, "H2", "2");
            } else if (daysUntilDeadline == 1) {
                sendDeadlineReminder(project, "H1", "1");
            } else if (daysUntilDeadline < 0) {
                sendOverdueReminder(project);
            }
        }
    }

    private void sendDeadlineReminder(Project project, String reminderType, String daysText) {
        List<Integer> recipientUserIds = getProjectMemberUserIds(project);

        for (Integer recipientUserId : recipientUserIds) {
            boolean alreadySent = deadlineLogRepository
                    .existsByProjectIdAndRecipientUserIdAndReminderTypeAndDeadlineSnapshot(
                            project.getProjectId(),
                            recipientUserId,
                            reminderType,
                            project.getDeadline()
                    );

            if (alreadySent) {
                continue;
            }

            notificationService.notifyProjectDeadlineReminder(
                    recipientUserId,
                    project.getProjectId(),
                    daysText,
                    project.getTitle(),
                    project.getDeadline()
            );

            saveDeadlineLog(project, recipientUserId, reminderType);
        }
    }

    private void sendOverdueReminder(Project project) {
        Integer ownerUserId = project.getUser().getIdPengguna();

        boolean canSendOverdue = canSendOverdueNotification(project, ownerUserId);

        if (!canSendOverdue) {
            return;
        }

        notificationService.notifyProjectDeadlineOverdue(
                ownerUserId,
                project.getProjectId(),
                project.getTitle(),
                project.getDeadline()
        );

        saveDeadlineLog(project, ownerUserId, "OVERDUE");
    }

    private boolean canSendOverdueNotification(Project project, Integer recipientUserId) {
        return deadlineLogRepository
                .findTopByProjectIdAndRecipientUserIdAndReminderTypeAndDeadlineSnapshotOrderBySentAtDesc(
                        project.getProjectId(),
                        recipientUserId,
                        "OVERDUE",
                        project.getDeadline()
                )
                .map(log -> log.getSentAt().plusDays(3).isBefore(LocalDateTime.now())
                        || log.getSentAt().plusDays(3).isEqual(LocalDateTime.now()))
                .orElse(true);
    }

    private void saveDeadlineLog(Project project, Integer recipientUserId, String reminderType) {
        ProjectDeadlineNotificationLog log = ProjectDeadlineNotificationLog.builder()
                .projectId(project.getProjectId())
                .recipientUserId(recipientUserId)
                .reminderType(reminderType)
                .deadlineSnapshot(project.getDeadline())
                .sentAt(LocalDateTime.now())
                .build();

        deadlineLogRepository.save(log);
    }

    private List<Integer> getProjectMemberUserIds(Project project) {
        List<Integer> userIds = collaborationRepository
                .findByProjectIdAndStatus(project.getProjectId(), "ACCEPTED")
                .stream()
                .map(Collaboration::getIdPengguna)
                .toList();

        if (!userIds.contains(project.getUser().getIdPengguna())) {
            return new java.util.ArrayList<Integer>() {{
                add(project.getUser().getIdPengguna());
                addAll(userIds);
            }};
        }

        return userIds;
    }
}