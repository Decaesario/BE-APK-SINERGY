package com.impal.gabungyuk.Activitylog.controller;


import com.impal.gabungyuk.Activitylog.model.response.ActivityLogResponse;
import com.impal.gabungyuk.Activitylog.service.ActivityLogService;
import com.impal.gabungyuk.core.model.SuccessResponse;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
public class ActivityLogController {

    private final ActivityLogService activityLogService;

    public ActivityLogController(ActivityLogService activityLogService) {
        this.activityLogService = activityLogService;
    }

    @GetMapping(
            value = "/api/v1/activity-logs",
            produces = MediaType.APPLICATION_JSON_VALUE
    )
    public SuccessResponse<List<ActivityLogResponse>> getMyActivityLogs(
            @RequestHeader("Authorization") String authorizationHeader) {

        List<ActivityLogResponse> response = activityLogService.getMyActivityLogs(authorizationHeader);

        return SuccessResponse.<List<ActivityLogResponse>>builder()
                .status(200)
                .message("Activity logs retrieved successfully")
                .data(response)
                .build();
    }
}
