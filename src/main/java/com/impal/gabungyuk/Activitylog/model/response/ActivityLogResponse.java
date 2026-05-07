package com.impal.gabungyuk.Activitylog.model.response;

import lombok.*;

import com.fasterxml.jackson.annotation.JsonFormat;
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
    @JsonFormat(pattern = "HH:mm:ss - dd/MM/yyyy")
    private LocalDateTime timestamp;
}