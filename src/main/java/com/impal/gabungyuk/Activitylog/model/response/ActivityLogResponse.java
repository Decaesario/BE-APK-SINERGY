package com.impal.gabungyuk.Activitylog.model.response;

import lombok.*;

import java.time.LocalDateTime;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class ActivityLogResponse {
    private Integer activityLogId;
    private String namaLengkap;
    private Integer projectId;
    private String message;
    private Boolean isRead;
    private LocalDateTime timestamp;
}
