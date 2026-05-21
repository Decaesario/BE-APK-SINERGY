package com.impal.gabungyuk.project.model.request;

import java.time.LocalDateTime;
import java.util.List;

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
    private List<String> category;
    private String status;
    private String repositoryLink;
    private String fileUrl;
    private LocalDateTime deadline;
}