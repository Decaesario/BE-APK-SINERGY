package com.impal.gabungyuk.Activitylog.service;

import com.impal.gabungyuk.Activitylog.entity.ActivityLog;
import com.impal.gabungyuk.Activitylog.model.response.ActivityLogResponse;
import com.impal.gabungyuk.Activitylog.repository.ActivityLogRepository;
import com.impal.gabungyuk.auth.entity.User;
import com.impal.gabungyuk.auth.respository.UserRepository;
import com.impal.gabungyuk.core.service.TokenService;
import com.impal.gabungyuk.core.service.TimezoneService;
import com.impal.gabungyuk.project.entity.Project;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;
import java.util.Map;

@Service
public class ActivityLogService {
    private final ActivityLogRepository activityLogRepository;
    private final UserRepository userRepository;
    private final TokenService tokenService;
    private final TimezoneService timezoneService;

    public ActivityLogService(
            ActivityLogRepository activityLogRepository,
            UserRepository userRepository,
            TokenService tokenService,
            TimezoneService timezoneService
    ) {
        this.activityLogRepository = activityLogRepository;
        this.userRepository = userRepository;
        this.tokenService = tokenService;
        this.timezoneService = timezoneService;
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


public Map<String, Long> getActivityRecap(String authorizationHeader) {
    Integer userId = tokenService.extractUserIdFromAuthorizationHeader(authorizationHeader);

    userRepository.findById(userId)
            .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "User not found"));

    List<ActivityLog> logs = activityLogRepository.findByUser_IdPenggunaOrderByTimestampDesc(userId);
    String viewerTz = timezoneService.getUserTimezoneOrDefault(userId);

    // Group by tanggal (yyyy-MM-dd) → hitung jumlah aktivitas per hari
    return logs.stream()
            .collect(Collectors.groupingBy(
                    log -> timezoneService.convertToUserZone(log.getTimestamp(), viewerTz).toLocalDate().toString(),
                    Collectors.counting()
            ));
}

    public List<ActivityLogResponse> getMyActivityLogs(String authorizationHeader) {
        Integer userId = tokenService.extractUserIdFromAuthorizationHeader(authorizationHeader);

        userRepository.findById(userId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "User not found"));

        List<ActivityLog> logs = activityLogRepository.findByUser_IdPenggunaOrderByTimestampDesc(userId);
        String viewerTz = timezoneService.getUserTimezoneOrDefault(userId);

        return logs.stream()
                .map(log -> ActivityLogResponse.builder()
                        .activityLogId(log.getActivityLogId())
                        .namaLengkap(log.getUser().getNamaLengkap())
                        .projectId(log.getProject() != null ? log.getProject().getProjectId() : null)
                        .message(log.getMessage())      
                        .isRead(log.getIsRead())
                        .timestamp(timezoneService.convertToUserZone(log.getTimestamp(), viewerTz))
                        .build())
                .collect(Collectors.toList());
    }
}
