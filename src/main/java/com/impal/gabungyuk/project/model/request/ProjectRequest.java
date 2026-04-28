package com.impal.gabungyuk.project.model.request;

import java.time.LocalDateTime;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ProjectRequest {
    private String title;
    private String description;
    private String category;
    private String status;
    private String repositoryLink;
    private String fileUrl;
}
