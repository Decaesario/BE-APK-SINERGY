package com.impal.gabungyuk.Activitylog.service;

import com.impal.gabungyuk.Activitylog.entity.ActivityLog;
import com.impal.gabungyuk.Activitylog.model.response.ActivityLogResponse;
import com.impal.gabungyuk.Activitylog.repository.ActivityLogRepository;
import com.impal.gabungyuk.auth.entity.User;
import com.impal.gabungyuk.auth.respository.UserRepository;
import com.impal.gabungyuk.core.service.TokenService;
import com.impal.gabungyuk.project.entity.Project;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;


@Service
public class ActivityLogService {
    private final ActivityLogRepository activityLogRepository;
    private final UserRepository userRepository;
    private final TokenService tokenService;

    public ActivityLogService(ActivityLogRepository activityLogRepository, UserRepository userRepository, TokenService tokenService) {
        this.activityLogRepository = activityLogRepository;
        this.userRepository = userRepository;
        this.tokenService = tokenService;
    }

   public void log(User user, Project project, String message) {
    ActivityLog activityLog = ActivityLog.builder()
            .user(user)
            .project(project)
            .message(message)
            .isRead(false)
            .timestamp(LocalDateTime.now())
            .build();
    activityLogRepository.save(activityLog);
}

    public List<ActivityLogResponse> getMyActivityLogs(String authorizationHeader) {
        Integer userId = tokenService.extractUserIdFromAuthorizationHeader(authorizationHeader);

        userRepository.findById(userId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "User not found"));

        List<ActivityLog> logs = activityLogRepository.findByUser_IdPenggunaOrderByTimestampDesc(userId);

        return logs.stream()
                .map(log -> ActivityLogResponse.builder()
                        .activityLogId(log.getActivityLogId())
                        .namaLengkap(log.getUser().getNamaLengkap())
                        .projectId(log.getProject() != null ? log.getProject().getProjectId() : null)
                        .message(log.getMessage())      
                        .isRead(log.getIsRead())
                        .timestamp(log.getTimestamp())
                        .build())
                .collect(Collectors.toList());
    }
}
