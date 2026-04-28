package com.impal.gabungyuk.collaboration.model.request;

import java.time.LocalDateTime;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class CollaborationRequest {
    private Integer projectId;
    private Integer userId;
    private String role;
    private String status;
    private LocalDateTime joinDate;
}
