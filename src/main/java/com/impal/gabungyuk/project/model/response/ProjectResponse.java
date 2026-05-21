package com.impal.gabungyuk.project.model.response;

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
public class ProjectResponse {
    private Integer id;
    private String title;
    private String description;
    private List<String> category;
    private String status;
    private String repositoryLink;
    private String projectPicture;
    private UserOwnerResponse owner;
    private LocalDateTime deadline;
}