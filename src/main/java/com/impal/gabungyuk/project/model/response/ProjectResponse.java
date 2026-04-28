package com.impal.gabungyuk.project.model.response;

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
    private String category;
    private String status;
    private String repositoryLink;   
    private String fileUrl; 
    
}
